from typing import Any

import httpx
from fastapi import APIRouter, Body, HTTPException, Path
from label_studio_sdk.core.api_error import ApiError

from ..ls_client import get_client
from ..schemas.predictions import (
  AiAnnotationRequest,
  AiAnnotationResponse,
  PredictionCreateRequest,
  PredictionDeleteResponse,
  PredictionItem,
  PredictionListResponse,
  PredictionResponse,
  PredictionUpdateRequest,
)

router = APIRouter(prefix="/api", tags=["predictions"])


def _raise_api_error(e: ApiError) -> None:
  raise HTTPException(
    status_code=e.status_code or 502,
    detail=f"Label Studio API 错误: {e.body}",
  )


def _resolve_model(client, project_id: int, model_id: int | None):
  """确定本次生成 AI 辅助标注使用的模型连接：优先使用指定模型，否则取项目下已连接的模型。"""
  if model_id is not None:
    try:
      backend = client.ml.get(id=model_id)
    except ApiError as e:
      if e.status_code == 404:
        raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
      _raise_api_error(e)

    if getattr(backend, "project", None) != project_id:
      raise HTTPException(status_code=400, detail=f"模型连接 {model_id} 不属于项目 {project_id}")
    return backend

  backends = client.ml.list(project=project_id) or []
  if not backends:
    raise HTTPException(
      status_code=400,
      detail=f"项目 {project_id} 未连接任何模型，请先通过连接模型接口添加模型服务",
    )

  for backend in backends:
    if getattr(backend, "state", None) == "CO":
      return backend
  return backends[0]


def _ls_native_post(path: str, *, json: dict | None = None) -> dict:
  """直接调用 Label Studio 原生 REST 接口，返回 JSON 结果。

  认证头（Bearer access token）由 SDK 底层的 httpx 客户端自动附加，请求由
  Label Studio 在集群内发起，因此可访问仅集群内可达的模型服务地址。
  """
  response = get_client()._client_wrapper.httpx_client.request(path, method="POST", json=json)

  try:
    body = response.json()
  except ValueError:
    body = None

  if response.status_code >= 400:
    detail = body.get("detail") if isinstance(body, dict) else None
    raise HTTPException(
      status_code=response.status_code,
      detail=f"Label Studio API 错误: {detail or response.text[:300]}",
    )

  return body if isinstance(body, dict) else {}


def _call_interactive_backend(backend, task_id: int) -> dict[str, Any]:
  """通过 Label Studio 原生接口调用交互式模型，返回该任务的预测结果。

  交互式模型（is_interactive=True）不支持批量 /predict，只能针对单个任务发起
  请求；该请求由 Label Studio 在集群内执行，避免宿主机无法解析集群内网地址。
  """
  body = _ls_native_post(f"/api/ml/{backend.id}/interactive-annotating", json={"task": task_id})

  errors = body.get("errors")
  if errors:
    raise HTTPException(status_code=502, detail=f"调用模型服务失败: {'; '.join(str(e) for e in errors)}")

  data = body.get("data") or {}
  result = data.get("result") or []
  if not result:
    raise HTTPException(status_code=502, detail=f"模型服务未返回预测结果: {body}")

  return {
    "result": result,
    "score": data.get("score"),
    "model_version": data.get("model_version") or body.get("model_version"),
  }


def _call_model_backend(
  backend,
  task_id: int,
  data: Any,
  label_config: str | None,
  project_id: int,
) -> dict[str, Any]:
  """调用模型服务生成该任务的预测结果。

  交互式模型走 Label Studio 原生的 interactive-annotating 接口（集群内执行）；
  其余模型沿用模型服务的 /predict 批量接口。
  """
  if getattr(backend, "is_interactive", False):
    return _call_interactive_backend(backend, task_id)

  if getattr(backend, "auth_method", "NONE") == "BASIC_AUTH":
    raise HTTPException(
      status_code=400,
      detail="模型服务使用 Basic Auth，Label Studio 不返回密码，无法代为调用预测接口",
    )

  url = f"{(getattr(backend, 'url', '') or '').rstrip('/')}/predict"
  timeout = float(getattr(backend, "timeout", None) or 120)

  try:
    response = httpx.post(
      url,
      json={
        "tasks": [{"id": task_id, "data": data or {}}],
        "label_config": label_config,
        "project": str(project_id),
      },
      timeout=timeout,
    )
    response.raise_for_status()
    body = response.json()
  except httpx.HTTPStatusError as e:
    raise HTTPException(status_code=502, detail=f"模型服务返回错误: {e.response.status_code} {e.response.text}")
  except httpx.HTTPError as e:
    raise HTTPException(status_code=502, detail=f"调用模型服务失败: {e}")
  except ValueError as e:
    raise HTTPException(status_code=502, detail=f"模型服务返回内容无法解析: {e}")

  results = body.get("results") or []
  if not results:
    raise HTTPException(status_code=502, detail=f"模型服务未返回预测结果: {body}")

  return {**results[0], "model_version": results[0].get("model_version") or body.get("model_version")}


def _delete_same_version(client, task_id: int, model_version: str | None) -> None:
  """删除任务下相同模型版本的旧预测，避免重复点击时堆积预标注。"""
  if not model_version:
    return

  for prediction in client.predictions.list(task=task_id) or []:
    if getattr(prediction, "model_version", None) == model_version and getattr(prediction, "id", None):
      client.predictions.delete(id=prediction.id)


def _to_prediction(raw: Any) -> PredictionItem:
  """把 SDK 返回的预测（dict 或 pydantic 模型）转换为统一的响应结构。"""
  data = raw.model_dump() if hasattr(raw, "model_dump") else dict(raw)

  return PredictionItem(
    id=data.get("id"),
    task=data.get("task"),
    project=data.get("project"),
    result=data.get("result") or [],
    score=data.get("score"),
    model_version=data.get("model_version"),
    model=data.get("model"),
    model_run=data.get("model_run"),
    cluster=data.get("cluster"),
    mislabeling=data.get("mislabeling"),
    neighbors=data.get("neighbors"),
    created_at=str(data["created_at"]) if data.get("created_at") else None,
    updated_at=str(data["updated_at"]) if data.get("updated_at") else None,
  )


@router.get("/tasks/{task_id}/predictions", response_model=PredictionListResponse)
async def list_task_predictions(task_id: int = Path(description="任务 ID")) -> PredictionListResponse:
  """
  获取指定任务下的全部模型预测（AI 辅助标注）。

  预标注在标注界面中展示为可接受的建议结果，模型版本可通过 model_version 区分。
  """
  client = get_client()

  try:
    predictions = client.predictions.list(task=task_id)
    items = [_to_prediction(p) for p in (predictions or [])]

    return PredictionListResponse(task_id=task_id, total=len(items), items=items)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"任务不存在: {task_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.post("/tasks/{task_id}/predictions", response_model=PredictionResponse, status_code=201)
async def create_task_prediction(
  task_id: int = Path(description="任务 ID"),
  payload: PredictionCreateRequest = Body(description="预测结果"),
) -> PredictionResponse:
  """
  为指定任务添加一条模型预测（AI 辅助标注/预标注）。

  该接口对应 Label Studio 官方 SDK 的 client.predictions.create(task=task.id, **prediction.model_dump())，
  写入后该预测会作为预标注展示在标注界面，并可在数据管理器中按 model_version 筛选。

  result 的结构由项目的标注界面配置决定，例如 Choices 标注为：
  [{"from_name": "label", "to_name": "text", "type": "choices", "value": {"choices": ["0"]}}]
  """
  fields = {k: v for k, v in payload.model_dump(exclude_unset=True).items() if v is not None}
  result = fields.pop("result", None)

  client = get_client()

  try:
    prediction = client.predictions.create(task=task_id, result=result or [], **fields)
    return PredictionResponse(**_to_prediction(prediction).model_dump(), message="创建成功")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"任务不存在: {task_id}")
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"预测结果不符合标注界面配置: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.get("/predictions/{prediction_id}", response_model=PredictionItem)
async def get_prediction(prediction_id: int = Path(description="预测 ID")) -> PredictionItem:
  """获取单条模型预测的详情。"""
  client = get_client()

  try:
    prediction = client.predictions.get(id=prediction_id)
    return _to_prediction(prediction)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"预测不存在: {prediction_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.patch("/predictions/{prediction_id}", response_model=PredictionResponse)
async def update_prediction(
  prediction_id: int = Path(description="预测 ID"),
  payload: PredictionUpdateRequest = Body(description="需要更新的预测字段"),
) -> PredictionResponse:
  """更新一条模型预测，只需提交要修改的字段。"""
  fields = {k: v for k, v in payload.model_dump(exclude_unset=True).items() if v is not None}
  if not fields:
    raise HTTPException(status_code=400, detail="请至少提供一个需要更新的字段")

  client = get_client()

  try:
    prediction = client.predictions.update(id=prediction_id, **fields)
    return PredictionResponse(**_to_prediction(prediction).model_dump(), message="更新成功")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"预测不存在: {prediction_id}")
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"预测结果不符合标注界面配置: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.delete("/predictions/{prediction_id}", response_model=PredictionDeleteResponse)
async def delete_prediction(prediction_id: int = Path(description="预测 ID")) -> PredictionDeleteResponse:
  """删除一条模型预测，该操作不可撤销。"""
  client = get_client()

  try:
    client.predictions.delete(id=prediction_id)
    return PredictionDeleteResponse(id=prediction_id, message="删除成功")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"预测不存在: {prediction_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


# 该接口会同步等待模型服务推理（可能耗时较长），因此用同步函数交给 FastAPI 线程池执行，避免阻塞事件循环
@router.post("/tasks/{task_id}/ai-annotation", response_model=AiAnnotationResponse)
def create_task_ai_annotation(
  task_id: int = Path(description="任务 ID"),
  payload: AiAnnotationRequest | None = Body(None, description="AI 辅助标注参数"),
) -> AiAnnotationResponse:
  """
  为指定任务生成 AI 辅助标注（预标注），前端点击某条记录时调用该接口即可。

  流程：读取任务数据 -> 调用项目已连接模型服务的 /predict -> 把结果写入该任务的预测，
  写入后标注界面即可看到模型给出的建议结果（Auto-Annotation）。

  - model_id 缺省时自动使用项目下状态为已连接（CO）的模型
  - save=false 时只返回模型结果用于预览，不写入预测
  - overwrite=true 时会先删除该任务下相同模型版本的旧预测，重复点击不会堆积预标注

  项目未连接模型返回 400，模型服务调用失败返回 502。
  """
  options = payload or AiAnnotationRequest()
  client = get_client()

  try:
    task = client.tasks.get(id=str(task_id))
  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"任务不存在: {task_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")

  project_id = task.project

  try:
    backend = _resolve_model(client, project_id, options.model_id)
    project = client.projects.get(project_id)
  except HTTPException:
    raise
  except ApiError as e:
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")

  prediction = _call_model_backend(
    backend=backend,
    task_id=task_id,
    data=getattr(task, "data", None),
    label_config=getattr(project, "label_config", None),
    project_id=project_id,
  )

  result = prediction.get("result") or []
  if not result:
    raise HTTPException(status_code=502, detail="模型服务未返回可用的标注结果")

  model_version = options.model_version or prediction.get("model_version")

  if not options.save:
    return AiAnnotationResponse(
      task=task_id,
      project=project_id,
      result=result,
      score=prediction.get("score"),
      model=backend.id,
      model_version=model_version,
      model_title=backend.title,
      saved=False,
      message="生成成功（未写入预标注）",
    )

  try:
    if options.overwrite:
      _delete_same_version(client, task_id, model_version)

    saved = client.predictions.create(
      task=task_id,
      result=result,
      score=prediction.get("score"),
      model=backend.id,
      model_version=model_version,
    )
    return AiAnnotationResponse(
      **_to_prediction(saved).model_dump(),
      model_title=backend.title,
      saved=True,
      message="生成成功",
    )

  except ApiError as e:
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"预测结果不符合标注界面配置: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")
