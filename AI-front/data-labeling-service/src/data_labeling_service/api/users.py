from typing import Any

from fastapi import APIRouter, HTTPException, Query

from ..ls_client import get_client

router = APIRouter(prefix="/api/users", tags=["users"])


def _native_request(path: str, params: dict | None = None):
  """直接调用 Label Studio 原生 REST 接口，返回解析后的 JSON（无响应体时返回 None）。

  认证头（Bearer access token）由 SDK 底层的 httpx 客户端自动附加。
  """
  response = get_client()._client_wrapper.httpx_client.request(path, method="GET", params=params)

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


@router.get("", response_model=list[dict[str, Any]])
async def list_users(
  project: int | None = Query(None, description="项目 ID（原样透传；Label Studio 当前返回组织内全部用户）"),
) -> list[dict[str, Any]]:
  """获取用户列表。

  直接调用 Label Studio 原生 `GET /api/users`，返回当前组织下的全部用户。
  `project` 参数会原样透传（Label Studio 该接口暂不按项目过滤）。
  """
  params = {"project": project} if project is not None else None
  return _native_request("/api/users", params=params) or []
