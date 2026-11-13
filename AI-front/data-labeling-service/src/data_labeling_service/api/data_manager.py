from typing import Any

from fastapi import APIRouter, Body, HTTPException, Query

from ..ls_client import get_client
from ..schemas.data_manager import ViewsResetResponse

router = APIRouter(prefix="/api/dm", tags=["data-manager"])


def _native_request(method: str, path: str, *, params: dict | None = None, payload: dict | None = None):
  """直接调用 Label Studio 原生 REST 接口，返回解析后的 JSON（无响应体时返回 None）。

  认证头（Bearer access token）由 SDK 底层的 httpx 客户端自动附加。
  """
  response = get_client()._client_wrapper.httpx_client.request(
    path, method=method, params=params, json=payload
  )

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


@router.get("/views", response_model=list[dict[str, Any]])
async def list_views(
  project: int = Query(..., description="项目 ID"),
) -> list[dict[str, Any]]:
  """获取项目下的 Data Manager 视图列表。

  直接调用 Label Studio 原生 `GET /api/dm/views?project={id}`。
  """
  return _native_request("GET", "/api/dm/views", params={"project": project}) or []


@router.post("/views", response_model=dict[str, Any])
async def create_view(
  payload: dict[str, Any] = Body(..., description="视图定义，通常包含 data（filters/columns/ordering 等），可含 project"),
  project: int | None = Query(None, description="项目 ID；请求体未含 project 时自动注入"),
  tabID: int | None = Query(None, description="前端标签页 ID（Label Studio 后端不使用，仅原样透传）"),
) -> dict[str, Any]:
  """创建 Data Manager 视图。

  直接调用 Label Studio 原生 `POST /api/dm/views?project={id}&tabID={tabID}`。
  视图排序（order）由服务端自动分配，返回创建后的视图对象。
  """
  body = dict(payload)
  if project is not None:
    body.setdefault("project", project)

  params: dict = {}
  if tabID is not None:
    params["tabID"] = tabID

  return _native_request("POST", "/api/dm/views", params=params or None, payload=body) or {}


@router.get("/project", response_model=dict[str, Any])
async def get_dm_project(
  project: int = Query(..., description="项目 ID"),
) -> dict[str, Any]:
  """获取 Data Manager 视角下的项目信息（含解析后的标签配置等）。

  直接调用 Label Studio 原生 `GET /api/dm/project?project={id}`。
  """
  return _native_request("GET", "/api/dm/project", params={"project": project}) or {}


@router.delete("/views/reset", response_model=ViewsResetResponse)
async def reset_views(
  project: int = Query(..., description="要重置视图的项目 ID"),
) -> ViewsResetResponse:
  """删除指定项目下的所有 Data Manager 视图，恢复为默认视图。

  直接调用 Label Studio 的 `DELETE /api/dm/views/reset?project={id}`。
  """
  _native_request("DELETE", "/api/dm/views/reset", params={"project": project})
  return ViewsResetResponse(project=project)
