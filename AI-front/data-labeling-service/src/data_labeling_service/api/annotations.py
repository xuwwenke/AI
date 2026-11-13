import json
from typing import Any, Literal

from fastapi import APIRouter, Body, Header, HTTPException, Path, Query
from label_studio_sdk import LabelStudio
from label_studio_sdk.core.api_error import ApiError

from ..ls_client import get_client, get_client_for_token
from ..schemas.annotations import (
  AnnotationDeleteResponse,
  AnnotationItem,
  AnnotationResponse,
  AnnotationSubmitRequest,
  AnnotationUpdateRequest,
  DataManagerTaskListResponse,
  TaskDetail,
  TaskItem,
  TaskListResponse,
)

router = APIRouter(prefix="/api", tags=["annotations"])

# 标注状态 -> Label Studio Data Manager 过滤条件（filter:tasks:total_annotations）
STATUS_FILTERS = {
  "unannotated": {"operator": "equal", "value": 0},
  "annotated": {"operator": "greater_or_equal", "value": 1},
}


def _raise_api_error(e: ApiError) -> None:
  raise HTTPException(
    status_code=e.status_code or 502,
    detail=f"Label Studio API 错误: {e.body}",
  )


def _native_request(method: str, path: str, *, params: dict | None = None):
  """直接调用 Label Studio 原生 REST 接口，返回解析后的 JSON（无响应体时返回 None）。

  认证头（Bearer access token）由 SDK 底层的 httpx 客户端自动附加。
  """
  response = get_client()._client_wrapper.httpx_client.request(path, method=method, params=params)

  if response.status_code >= 400:
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


def _client_for_user(authorization: str | None) -> LabelStudio:
  """取请求头中的用户 Token，返回以该用户身份调用 Label Studio 的客户端。"""
  scheme, _, token = (authorization or "").partition(" ")
  if scheme.lower() != "bearer" or not token.strip():
    raise HTTPException(
      status_code=401,
      detail="缺少用户身份，请在 Authorization 请求头中携带登录接口返回的 Token（Bearer <token>）",
    )

  return get_client_for_token(token.strip())


def _build_status_query(status: str) -> str | None:
  """把标注状态筛选转换为 Label Studio 的任务查询条件。"""
  condition = STATUS_FILTERS.get(status)
  if condition is None:
    return None

  return json.dumps(
    {
      "filters": {
        "conjunction": "and",
        "items": [
          {
            "filter": "filter:tasks:total_annotations",
            "type": "Number",
            **condition,
          }
        ],
      }
    }
  )


def _user_id(value: Any) -> int | None:
  """标注人字段既可能是用户 ID，也可能是用户对象，统一取用户 ID。"""
  if isinstance(value, dict):
    return value.get("id")
  return value


def _to_annotation(raw: Any) -> AnnotationItem:
  """把 SDK 返回的标注（dict 或 pydantic 模型）转换为统一的响应结构。"""
  data = raw.model_dump() if hasattr(raw, "model_dump") else dict(raw)

  return AnnotationItem(
    id=data.get("id"),
    task=data.get("task"),
    project=data.get("project"),
    result=data.get("result") or [],
    completed_by=_user_id(data.get("completed_by")),
    created_username=data.get("created_username"),
    created_at=str(data["created_at"]) if data.get("created_at") else None,
    updated_at=str(data["updated_at"]) if data.get("updated_at") else None,
    lead_time=data.get("lead_time"),
    was_cancelled=bool(data.get("was_cancelled")),
    ground_truth=bool(data.get("ground_truth")),
  )


def _to_task_item(task) -> TaskItem:
  return TaskItem(
    id=task.id,
    inner_id=getattr(task, "inner_id", None),
    project=getattr(task, "project", None),
    data=getattr(task, "data", None) or {},
    total_annotations=getattr(task, "total_annotations", 0) or 0,
    total_predictions=getattr(task, "total_predictions", 0) or 0,
    is_labeled=bool(getattr(task, "is_labeled", False)),
    created_at=str(task.created_at) if getattr(task, "created_at", None) else None,
  )


def _to_task_detail(task) -> TaskDetail:
  return TaskDetail(
    **_to_task_item(task).model_dump(),
    annotations=[_to_annotation(a) for a in (getattr(task, "annotations", None) or [])],
    predictions=[
      p.model_dump() if hasattr(p, "model_dump") else dict(p)
      for p in (getattr(task, "predictions", None) or [])
    ],
  )


@router.get("/tasks", response_model=DataManagerTaskListResponse)
async def list_tasks(
  project: int = Query(..., description="项目 ID"),
  page: int = Query(1, ge=1, description="页码，从 1 开始"),
  page_size: int = Query(30, ge=1, description="每页数量"),
  view: int | None = Query(None, description="Data Manager 视图 ID，应用该视图的筛选与排序"),
) -> DataManagerTaskListResponse:
  """获取项目下的任务列表（Data Manager 视图分页）。

  直接调用 Label Studio 原生 `GET /api/tasks?page=&page_size=&view=&project=`，
  返回 Data Manager 的任务列表结构（`total` / `total_annotations` / `total_predictions` / `tasks`）。
  """
  params: dict = {"project": project, "page": page, "page_size": page_size}
  if view is not None:
    params["view"] = view

  body = _native_request("GET", "/api/tasks", params=params) or {}

  return DataManagerTaskListResponse(
    total_annotations=body.get("total_annotations", 0),
    total_predictions=body.get("total_predictions", 0),
    total=body.get("total", 0),
    tasks=body.get("tasks", []) or [],
  )


@router.get("/projects/{project_id}/tasks", response_model=TaskListResponse)
async def list_project_tasks(
  project_id: int = Path(description="项目 ID"),
  page: int = Query(1, ge=1, description="页码，从 1 开始"),
  page_size: int = Query(20, ge=1, le=100, description="每页数量，最大 100"),
  status: Literal["all", "unannotated", "annotated"] = Query(
    "all",
    description="标注状态筛选：all 全部、unannotated 待标注、annotated 已标注",
  ),
) -> TaskListResponse:
  """
  获取项目下的任务列表（分页），用于标注界面的任务列表与待标注任务拉取。

  - status=unannotated 只返回待标注任务，配合 page_size=1 即可拉取下一个待标注任务
  - 页码超出范围时返回空列表
  """
  client = get_client()

  try:
    paginated = client.tasks.list(
      project=project_id,
      page=page,
      page_size=page_size,
      query=_build_status_query(status),
    )
    items = [_to_task_item(t) for t in (paginated.items or [])]

    return TaskListResponse(
      project_id=project_id,
      total=getattr(paginated.response, "total", len(items)),
      page=page,
      page_size=page_size,
      items=items,
    )

  except ApiError as e:
    # Label Studio 对超出范围的页码返回 404 Invalid page，此处按空列表处理
    if e.status_code == 404 and "Invalid page" in str(e.body):
      return TaskListResponse(project_id=project_id, total=0, page=page, page_size=page_size, items=[])
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.get("/tasks/{task_id}", response_model=TaskDetail)
async def get_task(task_id: int = Path(description="任务 ID")) -> TaskDetail:
  """获取单个任务的详情，包含任务数据、标注结果与 AI 辅助标注（预测）。"""
  client = get_client()

  try:
    task = client.tasks.get(id=str(task_id))
    return _to_task_detail(task)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"任务不存在: {task_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.get("/tasks/{task_id}/annotations", response_model=list[AnnotationItem])
async def list_task_annotations(task_id: int = Path(description="任务 ID")) -> list[AnnotationItem]:
  """获取指定任务下的全部标注结果。"""
  client = get_client()

  try:
    annotations = client.annotations.list(id=task_id)
    return [_to_annotation(a) for a in annotations]

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"任务不存在: {task_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.post("/tasks/{task_id}/annotations", response_model=AnnotationResponse, status_code=201)
async def create_task_annotation(
  task_id: int = Path(description="任务 ID"),
  payload: AnnotationSubmitRequest = Body(description="标注结果"),
  authorization: str | None = Header(
    None,
    description="登录接口返回的 Token，格式 `Bearer <token>`，用于把标注记录归属到该用户",
  ),
) -> AnnotationResponse:
  """
  为指定任务提交一条标注结果，等同于标注员在标注界面点击 Submit。

  该接口以 Authorization 请求头中 Token 对应的用户身份提交，
  因此标注记录的标注人（completed_by）为该用户，而不是服务默认账号；
  其他接口不区分用户，仍使用服务默认账号访问。

  result 的结构由项目的标注界面配置决定，例如 Choices 标注为：
  [{"from_name": "label", "to_name": "text", "type": "choices", "value": {"choices": ["0"]}}]
  """
  client = _client_for_user(authorization)

  try:
    annotation = client.annotations.create(
      id=task_id,
      result=payload.result,
      was_cancelled=payload.was_cancelled,
      ground_truth=payload.ground_truth,
      lead_time=payload.lead_time,
    )
    return AnnotationResponse(**_to_annotation(annotation).model_dump(), message="提交成功")

  except ApiError as e:
    if e.status_code == 401:
      raise HTTPException(status_code=401, detail="Token 无效或已过期，请重新登录")
    if e.status_code == 403:
      raise HTTPException(status_code=403, detail=f"该用户没有标注该项目的权限: {e.body}")
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"任务不存在: {task_id}")
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"标注结果不符合标注界面配置: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.put("/annotations/{annotation_id}", response_model=AnnotationResponse)
async def update_annotation(
  annotation_id: int = Path(description="标注 ID"),
  payload: AnnotationUpdateRequest = Body(description="需要更新的标注字段"),
) -> AnnotationResponse:
  """更新一条已有标注，只需提交要修改的字段。"""
  fields = {k: v for k, v in payload.model_dump(exclude_unset=True).items() if v is not None}
  if not fields:
    raise HTTPException(status_code=400, detail="请至少提供一个需要更新的字段")

  client = get_client()

  try:
    annotation = client.annotations.update(id=annotation_id, **fields)
    return AnnotationResponse(**_to_annotation(annotation).model_dump(), message="更新成功")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"标注不存在: {annotation_id}")
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"标注结果不符合标注界面配置: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.delete("/annotations/{annotation_id}", response_model=AnnotationDeleteResponse)
async def delete_annotation(annotation_id: int = Path(description="标注 ID")) -> AnnotationDeleteResponse:
  """删除一条标注，该操作不可撤销。"""
  client = get_client()

  try:
    client.annotations.delete(id=annotation_id)
    return AnnotationDeleteResponse(id=annotation_id, message="删除成功")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"标注不存在: {annotation_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")
