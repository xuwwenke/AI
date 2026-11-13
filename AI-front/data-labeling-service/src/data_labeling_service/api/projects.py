from fastapi import APIRouter, Body, HTTPException, Query, status
from label_studio_sdk.core.api_error import ApiError

from ..ls_client import get_client
from ..schemas.projects import (
  LabelConfigRequest,
  LabelConfigResponse,
  LabelConfigValidateResponse,
  ProjectCreateRequest,
  ProjectCreatedBy,
  ProjectDeleteResponse,
  ProjectDetail,
  ProjectItem,
  ProjectListResponse,
  ProjectModelVersionsDeleteRequest,
  ProjectModelVersionsDeleteResponse,
  ProjectModelVersionsResponse,
  ProjectSummaryResetResponse,
  ProjectUpdateRequest,
  ReimportRequest,
  ReimportResponse,
  SampleTaskRequest,
  SampleTaskResponse,
)

router = APIRouter(prefix="/api/projects", tags=["projects"])

DEFAULT_LABEL_CONFIG = "<View></View>"


def _get(source, key, default=None):
  """兼容 SDK 对象与原生 JSON dict 的字段读取。"""
  if isinstance(source, dict):
    return source.get(key, default)
  return getattr(source, key, default)


def _get_stats(project) -> dict:
  """从 Label Studio 项目对象中提取任务/标注统计信息。"""
  return {
    "task_number": _get(project, "task_number", 0) or 0,
    "finished_task_number": _get(project, "finished_task_number", 0) or 0,
    "num_tasks_with_annotations": _get(project, "num_tasks_with_annotations", 0) or 0,
    "total_annotations_number": _get(project, "total_annotations_number", 0) or 0,
    "total_predictions_number": _get(project, "total_predictions_number", 0) or 0,
    "useful_annotation_number": _get(project, "useful_annotation_number", 0) or 0,
    "ground_truth_number": _get(project, "ground_truth_number", 0) or 0,
    "skipped_annotations_number": _get(project, "skipped_annotations_number", 0) or 0,
    "queue_total": _get(project, "queue_total", 0) or 0,
    "queue_done": _get(project, "queue_done", 0) or 0,
  }


def _to_item(project) -> ProjectItem:
  return ProjectItem(
    id=_get(project, "id"),
    title=_get(project, "title") or "",
    description=_get(project, "description") or "",
    created_at=str(_get(project, "created_at")) if _get(project, "created_at") else None,
    **_get_stats(project),
  )


def _to_created_by(user) -> ProjectCreatedBy | None:
  """把 Label Studio 的用户对象（或原生 dict）转换为项目创建者信息。"""
  if not user:
    return None
  return ProjectCreatedBy(
    id=_get(user, "id"),
    username=_get(user, "username"),
    first_name=_get(user, "first_name"),
    last_name=_get(user, "last_name"),
    email=_get(user, "email"),
    avatar=_get(user, "avatar"),
  )


def _to_control_weights(weights) -> dict | None:
  """control_weights 的值可能是 SDK 对象，这里统一转换为纯 dict。"""
  if weights is None:
    return None
  return {k: (v.model_dump() if hasattr(v, "model_dump") else v) for k, v in weights.items()}


def _to_detail(project) -> ProjectDetail:
  return ProjectDetail(
    id=_get(project, "id"),
    title=_get(project, "title") or "",
    description=_get(project, "description") or "",
    label_config=_get(project, "label_config") or "",
    expert_instruction=_get(project, "expert_instruction") or "",
    created_by=_to_created_by(_get(project, "created_by")),
    organization=_get(project, "organization"),
    color=_get(project, "color"),
    is_published=_get(project, "is_published"),
    is_draft=_get(project, "is_draft"),
    created_at=str(_get(project, "created_at")) if _get(project, "created_at") else None,
    pinned_at=str(_get(project, "pinned_at")) if _get(project, "pinned_at") else None,
    state=_get(project, "state"),
    model_version=_get(project, "model_version"),
    maximum_annotations=_get(project, "maximum_annotations"),
    min_annotations_to_start_training=_get(project, "min_annotations_to_start_training"),
    start_training_on_annotation_update=_get(project, "start_training_on_annotation_update"),
    show_instruction=_get(project, "show_instruction"),
    show_skip_button=_get(project, "show_skip_button"),
    show_annotation_history=_get(project, "show_annotation_history"),
    show_collab_predictions=_get(project, "show_collab_predictions"),
    show_ground_truth_first=_get(project, "show_ground_truth_first"),
    show_overlap_first=_get(project, "show_overlap_first"),
    enable_empty_annotation=_get(project, "enable_empty_annotation"),
    evaluate_predictions_automatically=_get(project, "evaluate_predictions_automatically"),
    reveal_preannotations_interactively=_get(project, "reveal_preannotations_interactively"),
    annotator_evaluation_enabled=_get(project, "annotator_evaluation_enabled"),
    overlap_cohort_percentage=_get(project, "overlap_cohort_percentage"),
    sampling=_get(project, "sampling"),
    skip_queue=_get(project, "skip_queue"),
    task_data_login=_get(project, "task_data_login"),
    task_data_password=_get(project, "task_data_password"),
    config_has_control_tags=_get(project, "config_has_control_tags"),
    config_suitable_for_bulk_annotation=_get(project, "config_suitable_for_bulk_annotation"),
    control_weights=_to_control_weights(_get(project, "control_weights")),
    parsed_label_config=_get(project, "parsed_label_config"),
    **_get_stats(project),
  )


def _raise_api_error(e: ApiError) -> None:
  raise HTTPException(
    status_code=e.status_code or 502,
    detail=f"Label Studio API 错误: {e.body}",
  )


def _project_native_request(
  method: str,
  path: str,
  payload: dict | None,
  project_id: int,
  params: dict | None = None,
):
  """直接调用 Label Studio 原生 REST 接口，返回解析后的 JSON（无响应体时返回 None）。

  认证头（Bearer access token）由 SDK 底层的 httpx 客户端自动附加。
  """
  response = get_client()._client_wrapper.httpx_client.request(
    path, method=method, json=payload, params=params
  )

  if response.status_code >= 400:
    if response.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    try:
      body = response.json()
    except ValueError:
      body = {}
    detail = body.get("detail") if isinstance(body, dict) else None
    raise HTTPException(
      status_code=response.status_code,
      detail=f"Label Studio API 错误: {detail or response.text[:300]}",
    )

  if not response.content:
    return None
  try:
    return response.json()
  except ValueError:
    return None


@router.get("/", response_model=ProjectListResponse)
async def list_projects(
  page: int = Query(1, ge=1, description="页码，从 1 开始"),
  page_size: int = Query(20, ge=1, le=100, description="每页数量，最大 100"),
  search: str | None = Query(None, description="按项目标题或描述搜索"),
  ordering: str | None = Query(None, description="排序字段，如 -created_at"),
) -> ProjectListResponse:
  """
  获取 Label Studio 中的项目列表（分页）。

  返回结果包含每个项目的任务总数、已标注任务数、标注总数、
  AI 辅助标注（预测）数量等统计信息。
  """
  client = get_client()

  try:
    # SDK 的 list() 返回分页对象，items 为当前页数据，response.count 为总条数
    paginated = client.projects.list(
      page=page,
      page_size=page_size,
      search=search,
      ordering=ordering,
    )
    items = [_to_item(p) for p in (paginated.items or [])]
    total = getattr(paginated.response, "count", len(items))

    return ProjectListResponse(
      total=total,
      page=page,
      page_size=page_size,
      items=items,
    )

  except ApiError as e:
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.post("/", response_model=ProjectDetail, status_code=status.HTTP_201_CREATED)
async def create_project(payload: ProjectCreateRequest) -> ProjectDetail:
  """在 Label Studio 中创建一个新项目。"""
  client = get_client()

  try:
    project = client.projects.create(
      title=payload.title,
      description=payload.description,
      label_config=payload.label_config or DEFAULT_LABEL_CONFIG,
    )
    return _to_detail(project)

  except ApiError as e:
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.get("/{project_id}", response_model=ProjectDetail)
async def get_project(project_id: int) -> ProjectDetail:
  """获取单个项目的详情，包含已标注/总数、AI 辅助标注数量等统计信息。"""
  client = get_client()

  try:
    project = client.projects.get(project_id)
    return _to_detail(project)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.patch("/{project_id}", response_model=ProjectDetail)
async def update_project(
  project_id: int,
  payload: ProjectUpdateRequest = Body(description="需要更新的项目设置字段"),
) -> ProjectDetail:
  """更新项目设置（对应项目设置页中的可编辑项）。

  直接调用 Label Studio 的 `PATCH /api/projects/{id}/`，仅需提交要修改的字段，
  例如 `title`、`maximum_annotations`、`sampling`、`expert_instruction` 等。
  返回更新后的完整项目详情。
  """
  fields = {k: v for k, v in payload.model_dump(exclude_unset=True).items() if v is not None}
  if not fields:
    raise HTTPException(status_code=400, detail="请至少提供一个需要更新的字段")

  body = _project_native_request("PATCH", f"/api/projects/{project_id}/", fields, project_id)
  return _to_detail(body)


@router.put("/{project_id}/label-config", response_model=LabelConfigResponse)
async def set_label_config(project_id: int, payload: LabelConfigRequest) -> LabelConfigResponse:
  """设置项目的 labeling interface（标注界面配置）。"""
  client = get_client()

  try:
    project = client.projects.update(project_id, label_config=payload.label_config)
    return LabelConfigResponse(
      id=project_id,
      label_config=project.label_config or payload.label_config,
    )

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"标注界面配置不合法: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.post("/{project_id}/sample-task", response_model=SampleTaskResponse)
async def generate_sample_task(project_id: int, payload: SampleTaskRequest) -> SampleTaskResponse:
  """根据 label_config 生成一个示例任务，用于预览标注界面。

  直接调用 Label Studio 的 `POST /api/projects/{id}/sample-task/`。
  `label_config` 为必填，通常传入项目当前的标注配置或待预览的新配置。
  """
  body = _project_native_request(
    "POST",
    f"/api/projects/{project_id}/sample-task/",
    {
      "label_config": payload.label_config,
      "include_annotation_and_prediction": payload.include_annotation_and_prediction,
    },
    project_id,
  )
  return SampleTaskResponse(sample_task=body.get("sample_task") or {})


@router.post("/{project_id}/validate", response_model=LabelConfigValidateResponse)
async def validate_label_config(
  project_id: int,
  payload: LabelConfigRequest,
) -> LabelConfigValidateResponse:
  """校验 label_config 在该项目下是否合法。

  直接调用 Label Studio 的 `POST /api/projects/{id}/validate/`。
  配置合法时返回该配置相对项目当前配置是否包含实质性变更；
  配置非法时透传 Label Studio 的 400 校验错误（`detail` 中为具体原因）。
  """
  body = _project_native_request(
    "POST",
    f"/api/projects/{project_id}/validate/",
    {"label_config": payload.label_config},
    project_id,
  )
  return LabelConfigValidateResponse(
    config_essential_data_has_changed=body.get("config_essential_data_has_changed"),
  )


@router.post("/{project_id}/reimport", response_model=ReimportResponse)
async def reimport_project(project_id: int, payload: ReimportRequest) -> ReimportResponse:
  """重新导入项目内已上传的文件（对应 数据导入 → Reimport）。

  直接调用 Label Studio 的 `POST /api/projects/{id}/reimport`。
  需要提供 `file_upload_ids`（已上传文件的 ID，可通过文件上传接口获取）；
  社区版会同步返回导入统计结果，企业版返回异步任务 ID（`reimport`）。
  """
  body = _project_native_request(
    "POST",
    f"/api/projects/{project_id}/reimport",
    {
      "file_upload_ids": payload.file_upload_ids,
      "files_as_tasks_list": payload.files_as_tasks_list,
    },
    project_id,
  )
  return ReimportResponse(
    task_count=body.get("task_count") or 0,
    annotation_count=body.get("annotation_count") or 0,
    prediction_count=body.get("prediction_count") or 0,
    duration=body.get("duration") or 0,
    file_upload_ids=body.get("file_upload_ids") or [],
    found_formats=body.get("found_formats"),
    data_columns=body.get("data_columns") or [],
    reimport=body.get("reimport"),
  )


@router.get("/{project_id}/model-versions", response_model=ProjectModelVersionsResponse)
async def list_project_model_versions(
  project_id: int,
  extended: bool = Query(False, description="是否返回扩展信息（含 static/live 分组）"),
  include_live_models: bool = Query(False, description="扩展模式下是否同时返回在线模型连接"),
  limit: int | None = Query(None, description="返回的模型版本数量上限"),
) -> ProjectModelVersionsResponse:
  """获取项目可用的模型版本列表（对应项目设置 → Model → Version）。

  直接调用 Label Studio 的 `GET /api/projects/{id}/model-versions/`。
  非扩展模式返回模型版本名称列表；扩展模式额外返回版本详情，
  当 `include_live_models=true` 时还会返回在线模型连接（live）。
  """
  params: dict = {
    "extended": str(extended).lower(),
    "include_live_models": str(include_live_models).lower(),
  }
  if limit is not None:
    params["limit"] = limit

  body = _project_native_request(
    "GET",
    f"/api/projects/{project_id}/model-versions/",
    None,
    project_id,
    params=params,
  )

  if extended:
    body = body or {}
    return ProjectModelVersionsResponse(
      extended=True,
      static=body.get("static") or [],
      live=body.get("live"),
    )
  return ProjectModelVersionsResponse(extended=False, versions=body or [])


@router.delete("/{project_id}/model-versions", response_model=ProjectModelVersionsDeleteResponse)
async def delete_project_model_versions(
  project_id: int,
  payload: ProjectModelVersionsDeleteRequest = Body(description="要删除预测的模型版本"),
) -> ProjectModelVersionsDeleteResponse:
  """删除指定模型版本在该项目中产生的全部预测。

  直接调用 Label Studio 的 `DELETE /api/projects/{id}/model-versions/`，
  请求体需携带 `model_version`。返回删除的预测数量。
  """
  body = _project_native_request(
    "DELETE",
    f"/api/projects/{project_id}/model-versions/",
    {"model_version": payload.model_version},
    project_id,
  )
  count = body if isinstance(body, int) else 0

  return ProjectModelVersionsDeleteResponse(
    project_id=project_id,
    model_version=payload.model_version,
    deleted_predictions=count,
  )


@router.post("/{project_id}/summary/reset", response_model=ProjectSummaryResetResponse)
async def reset_project_summary(project_id: int) -> ProjectSummaryResetResponse:
  """重置项目汇总信息（summary），从零重新计算 created_labels 等统计字段。

  直接调用 Label Studio 的 `POST /api/projects/{id}/summary/reset/`，
  适用于标注区域标签发生变更后、统计字段未正确更新的场景。
  """
  _project_native_request("POST", f"/api/projects/{project_id}/summary/reset/", None, project_id)
  return ProjectSummaryResetResponse(project_id=project_id)


@router.delete("/{project_id}", response_model=ProjectDeleteResponse)
async def delete_project(project_id: int) -> ProjectDeleteResponse:
  """删除指定项目。"""
  client = get_client()

  try:
    client.projects.delete(project_id)
    return ProjectDeleteResponse(id=project_id, message="删除成功")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")
