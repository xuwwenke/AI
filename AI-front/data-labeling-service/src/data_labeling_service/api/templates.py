import requests
from fastapi import APIRouter, HTTPException

from ..config import get_ls_config
from ..schemas.templates import TemplateItem, TemplateListResponse

router = APIRouter(prefix="/api/templates", tags=["templates"])

REQUEST_TIMEOUT = 15


def _get_access_token(base_url: str, api_key: str) -> str:
  """把配置中的 JWT refresh token 换取 access token。

  本服务配置的 API Token 为 refresh token，Label Studio 的原生 REST 接口
  要求以 `Authorization: Bearer <access>` 认证，因此先换取 access token。
  """
  try:
    response = requests.post(
      f"{base_url}/api/token/refresh/",
      json={"refresh": api_key},
      timeout=REQUEST_TIMEOUT,
    )
  except requests.RequestException as e:
    raise HTTPException(status_code=502, detail=f"刷新 Label Studio API Token 失败: {e}")

  if response.status_code != 200:
    raise HTTPException(
      status_code=502,
      detail=f"刷新 Label Studio API Token 失败: {response.text[:300]}",
    )

  access_token = response.json().get("access")
  if not access_token:
    raise HTTPException(status_code=502, detail="Label Studio 未返回 access token")
  return access_token


@router.get("", response_model=TemplateListResponse)
async def list_templates() -> TemplateListResponse:
  """
  获取 Label Studio 内置的标注模板（labeling templates）与分组。

  返回结构与 Label Studio 的 `GET /api/templates/` 一致：
  - `templates`：每个模板的名称、分组、预览图、说明以及标注界面配置 `config`，
    其中 `config` 可直接用作创建项目时的 `label_config`；
  - `groups`：模板分组名称列表，如 Computer Vision、Natural Language Processing。
  """
  base_url, api_key = get_ls_config()
  access_token = _get_access_token(base_url, api_key)

  try:
    response = requests.get(
      f"{base_url}/api/templates/",
      headers={"Authorization": f"Bearer {access_token}"},
      timeout=REQUEST_TIMEOUT,
    )
  except requests.RequestException as e:
    raise HTTPException(status_code=502, detail=f"获取 Label Studio 标注模板失败: {e}")

  if response.status_code != 200:
    raise HTTPException(
      status_code=response.status_code,
      detail=f"获取 Label Studio 标注模板失败: {response.text[:300]}",
    )

  data = response.json()
  return TemplateListResponse(
    templates=[TemplateItem(**t) for t in data.get("templates") or []],
    groups=data.get("groups") or [],
  )
