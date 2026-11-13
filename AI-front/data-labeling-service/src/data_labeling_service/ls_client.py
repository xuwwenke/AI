from label_studio_sdk import LabelStudio

from .config import get_ls_config

_client: LabelStudio | None = None

def get_client() -> LabelStudio:
  """获取全局复用的 Label Studio 同步客户端。"""
  global _client
  if _client is None:
    url, api_key = get_ls_config()
    _client = LabelStudio(base_url=url, api_key=api_key)
  return _client


def get_client_for_token(token: str) -> LabelStudio:
  """以指定用户的 Token 创建 Label Studio 客户端，用于需要区分用户的接口。

  不复用全局客户端，避免不同用户的身份相互串用。
  """
  url, _ = get_ls_config()
  return LabelStudio(base_url=url, api_key=token)