from fastapi import APIRouter, Body, HTTPException, Path, Query, status
from label_studio_sdk.core.api_error import ApiError

from ..ls_client import get_client
from ..schemas.models import (
  ModelActionResponse,
  ModelConnectRequest,
  ModelDeleteResponse,
  ModelItem,
  ModelListResponse,
  ModelPredictRequest,
  ModelPredictTestResponse,
  ModelTrainRequest,
  ModelUpdateRequest,
  ModelVersionsResponse,
)

router = APIRouter(prefix="/api", tags=["models"])


def _raise_api_error(e: ApiError) -> None:
  raise HTTPException(
    status_code=e.status_code or 502,
    detail=f"Label Studio API 错误: {e.body}",
  )


def _model_native_request(
  method: str,
  path: str,
  model_id: int,
  *,
  params: dict | None = None,
) -> dict:
  """直接调用 Label Studio 原生 REST 接口，返回 JSON 结果。

  认证头（Bearer access token）由 SDK 底层的 httpx 客户端自动附加。
  """
  response = get_client()._client_wrapper.httpx_client.request(
    path, method=method, params=params
  )

  if response.status_code >= 400:
    if response.status_code == 404:
      raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
    try:
      body = response.json()
    except ValueError:
      body = {}
    detail = body.get("detail") if isinstance(body, dict) else None
    raise HTTPException(
      status_code=response.status_code,
      detail=f"Label Studio API 错误: {detail or response.text[:300]}",
    )

  return response.json()


def _to_model(raw) -> ModelItem:
  """把 SDK 返回的 ML Backend 对象转换为统一的响应结构。"""
  data = raw.model_dump() if hasattr(raw, "model_dump") else dict(raw)

  return ModelItem(
    id=data.get("id"),
    project=data.get("project"),
    title=data.get("title") or "",
    url=data.get("url") or "",
    description=data.get("description") or "",
    auth_method=data.get("auth_method"),
    basic_auth_user=data.get("basic_auth_user"),
    basic_auth_pass_is_set=data.get("basic_auth_pass_is_set"),
    is_interactive=bool(data.get("is_interactive")),
    auto_update=data.get("auto_update"),
    model_version=data.get("model_version"),
    timeout=data.get("timeout"),
    state=data.get("state"),
    readable_state=data.get("readable_state"),
    error_message=data.get("error_message"),
    extra_params=data.get("extra_params"),
    created_at=str(data["created_at"]) if data.get("created_at") else None,
    updated_at=str(data["updated_at"]) if data.get("updated_at") else None,
  )


@router.post(
  "/projects/{project_id}/models",
  response_model=ModelItem,
  status_code=status.HTTP_201_CREATED,
)
async def connect_model(
  project_id: int = Path(description="项目 ID"),
  payload: ModelConnectRequest = Body(description="模型连接配置，对应 Connect Model 表单"),
) -> ModelItem:
  """
  把一个模型服务（ML Backend）连接到项目，等同于项目设置 → Model → Connect Model。

  连接成功后，项目中的任务即可使用该模型进行预测（预标注/交互式标注）。

  - `url` 为模型服务地址，例如本服务使用的 Ollama ML Backend：`http://localhost:9090`
  - `title` 为模型名称，用于在项目中区分不同模型
  - 模型服务需要用户名密码时，`auth_method` 设为 `BASIC_AUTH` 并填写 `basic_auth_user` / `basic_auth_pass`
  """
  client = get_client()
  fields = {k: v for k, v in payload.model_dump(exclude_unset=True).items() if v is not None}

  try:
    model = client.ml.create(project=project_id, **fields)
    return _to_model(model)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"模型连接配置不合法或模型服务不可访问: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.get("/projects/{project_id}/models", response_model=ModelListResponse)
async def list_project_models(
  project_id: int = Path(description="项目 ID"),
  is_interactive: bool | None = Query(None, description="仅返回用于交互式预标注的模型"),
) -> ModelListResponse:
  """获取项目已连接的模型列表，返回结果中包含模型状态（state/readable_state）。"""
  client = get_client()

  try:
    models = client.ml.list(project=project_id, is_interactive=is_interactive)
    items = [_to_model(m) for m in (models or [])]
    return ModelListResponse(project_id=project_id, total=len(items), items=items)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.get("/models/{model_id}", response_model=ModelItem)
async def get_model(model_id: int = Path(description="模型连接 ID")) -> ModelItem:
  """获取单个模型连接的详情，包含连接地址、状态与当前模型版本等信息。"""
  client = get_client()

  try:
    model = client.ml.get(model_id)
    return _to_model(model)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.patch("/models/{model_id}", response_model=ModelItem)
async def update_model(
  model_id: int = Path(description="模型连接 ID"),
  payload: ModelUpdateRequest = Body(description="需要更新的模型连接字段"),
) -> ModelItem:
  """更新模型连接配置，只需提交要修改的字段（如更换 url、切换交互式预标注）。"""
  fields = {k: v for k, v in payload.model_dump(exclude_unset=True).items() if v is not None}
  if not fields:
    raise HTTPException(status_code=400, detail="请至少提供一个需要更新的字段")

  client = get_client()

  try:
    # Label Studio 在 PATCH 时会调用 MLBackend.setup_() 校验连接，
    # 而该方法的 project 为必填参数；调用方通常不会传，这里用现有连接的所属项目补齐。
    fields["project"] = client.ml.get(model_id).project
    model = client.ml.update(model_id, **fields)
    return _to_model(model)

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
    if e.status_code == 400:
      raise HTTPException(status_code=400, detail=f"模型连接配置不合法: {e.body}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.delete("/models/{model_id}", response_model=ModelDeleteResponse)
async def disconnect_model(model_id: int = Path(description="模型连接 ID")) -> ModelDeleteResponse:
  """断开模型与项目的连接，该操作不会影响模型服务本身。"""
  client = get_client()

  try:
    client.ml.delete(model_id)
    return ModelDeleteResponse(id=model_id, message="已断开模型连接")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.post("/models/{model_id}/train", response_model=ModelActionResponse)
async def train_model(
  model_id: int = Path(description="模型连接 ID"),
  payload: ModelTrainRequest | None = Body(None, description="训练参数"),
) -> ModelActionResponse:
  """使用项目中已标注的任务触发模型训练（微调）。"""
  client = get_client()
  fields = {k: v for k, v in (payload or ModelTrainRequest()).model_dump(exclude_unset=True).items() if v is not None}

  try:
    client.ml.train(model_id, **fields)
    return ModelActionResponse(id=model_id, message="已触发模型训练")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.post("/models/{model_id}/predict", response_model=ModelActionResponse)
async def predict_with_model(
  model_id: int = Path(description="模型连接 ID"),
  payload: ModelPredictRequest | None = Body(None, description="预测参数"),
) -> ModelActionResponse:
  """
  使用指定模型为项目内尚无预测的任务批量生成预测，用于预标注与主动学习。

  注意：该能力依赖 Label Studio Enterprise，Community 版会返回错误。
  """
  client = get_client()
  fields = {k: v for k, v in (payload or ModelPredictRequest()).model_dump(exclude_unset=True).items() if v is not None}

  try:
    client.ml.predict_all_tasks(model_id, **fields)
    return ModelActionResponse(id=model_id, message="已触发模型预测")

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")


@router.post("/models/{model_id}/predict/test", response_model=ModelPredictTestResponse)
async def test_model_prediction(
  model_id: int = Path(description="模型连接 ID"),
  random: bool = Query(True, description="随机取项目内的一个任务进行测试预测（Label Studio 仅实现了该模式）"),
) -> ModelPredictTestResponse:
  """测试模型预测：随机取项目内的一个任务，调用模型服务执行一次预测并返回原始结果。

  直接调用 Label Studio 的 `POST /api/ml/{id}/predict/test?random=true`，
  对应项目设置 → Model → Test（用于验证模型服务连接与预测是否正常）。
  """
  body = _model_native_request(
    "POST",
    f"/api/ml/{model_id}/predict/test",
    model_id,
    params={"random": "true"} if random else None,
  )

  return ModelPredictTestResponse(
    model_id=model_id,
    status=body.get("status"),
    error_message=body.get("error_message"),
    url=body.get("url"),
    task=body.get("task"),
    request=body.get("request"),
    response=body.get("response"),
  )


@router.get("/models/{model_id}/versions", response_model=ModelVersionsResponse)
async def list_model_versions(model_id: int = Path(description="模型连接 ID")) -> ModelVersionsResponse:
  """获取模型服务提供的可用版本列表，用于选择 model_version。"""
  client = get_client()

  try:
    result = client.ml.list_model_versions(model_id)
    data = result.model_dump() if hasattr(result, "model_dump") else dict(result)
    return ModelVersionsResponse(
      id=model_id,
      versions=data.get("versions") or [],
      message=data.get("message"),
    )

  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"模型连接不存在: {model_id}")
    _raise_api_error(e)
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"服务器内部错误: {e}")
