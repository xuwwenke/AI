import base64
import hashlib
import json
import os

import requests
from cryptography.fernet import Fernet, InvalidToken
from django.conf import settings

from .models import ModelProvider
from .provider_catalogs import build_model_tags, get_provider_catalog


def _get_fernet():
  configured_key = os.environ.get("MODEL_CREDENTIAL_ENCRYPTION_KEY")
  if configured_key:
    try:
      return Fernet(configured_key.encode())
    except (TypeError, ValueError) as exc:
      raise ValueError("MODEL_CREDENTIAL_ENCRYPTION_KEY 不是有效的 Fernet 密钥") from exc

  derived_key = base64.urlsafe_b64encode(
    hashlib.sha256(settings.SECRET_KEY.encode()).digest()
  )
  return Fernet(derived_key)


def encrypt_credentials(credentials):
  payload = json.dumps(
    credentials,
    ensure_ascii=False,
    separators=(",", ":"),
    sort_keys=True,
  ).encode()
  return _get_fernet().encrypt(payload)


def decrypt_credentials(encrypted_credentials):
  try:
    payload = _get_fernet().decrypt(bytes(encrypted_credentials))
  except InvalidToken as exc:
    raise ValueError("模型供应商凭据无法解密") from exc
  return json.loads(payload.decode())


DEFAULT_PROVIDER_BASE_URLS = {
  "openai": "https://api.openai.com/v1",
}


def _basic_model_entity(model_id, model_type):
  entity = {
    "model": model_id,
    "label": model_id,
    "model_type": model_type,
    "features": [],
    "model_properties": {"mode": "chat"} if model_type == "llm" else {},
    "fetch_from": "customizable-model",
  }
  entity["tags"] = build_model_tags(entity)
  return entity


def _catalog_model_entities(provider, model_type, configured_models=None):
  catalog = get_provider_catalog(provider, model_type)
  if not catalog:
    return []

  configured_set = set(configured_models or [])
  if configured_set:
    catalog_by_id = {item["model"]: item for item in catalog}
    selected = [
      catalog_by_id.get(model_id) or _basic_model_entity(model_id, model_type)
      for model_id in configured_models
    ]
  else:
    selected = catalog

  result = []
  for item in selected:
    entity = dict(item)
    entity.setdefault("features", [])
    entity.setdefault("model_properties", {})
    entity["fetch_from"] = "predefined-model" if item in catalog else "customizable-model"
    entity["tags"] = build_model_tags(entity)
    result.append(entity)
  return result


def _discover_openai_compatible_models(model_provider, model_type):
  if model_type != "llm":
    raise ValueError(f"{model_type} 类型需要显式配置 models")

  credentials = decrypt_credentials(model_provider.credentials_encrypted)
  base_url = (
    credentials.get("base_url")
    or credentials.get("endpoint_url")
    or credentials.get("api_base_url")
    or DEFAULT_PROVIDER_BASE_URLS.get(model_provider.provider.lower())
  )
  if not base_url:
    raise ValueError("未配置 models 或 API Base URL，无法发现可用模型")

  headers = {}
  api_key = credentials.get("api_key")
  if api_key:
    headers["Authorization"] = f"Bearer {api_key}"

  try:
    response = requests.get(
      f"{base_url.rstrip('/')}/models",
      headers=headers,
      timeout=(5, 20),
    )
    response.raise_for_status()
    payload = response.json()
  except (requests.RequestException, ValueError) as exc:
    raise ValueError("供应商模型列表获取失败") from exc

  items = payload.get("data", payload) if isinstance(payload, dict) else payload
  if not isinstance(items, list):
    raise ValueError("供应商返回了无法识别的模型列表")

  model_ids = []
  for item in items:
    model_id = item.get("id") if isinstance(item, dict) else item
    if isinstance(model_id, str) and model_id:
      model_ids.append(model_id)
  return [_basic_model_entity(model_id, model_type) for model_id in sorted(set(model_ids))]


def get_provider_models(model_provider, model_type):
  """合并预定义目录、显式配置以及远端发现结果。"""
  if model_type not in model_provider.supported_model_types:
    return []

  catalog_models = _catalog_model_entities(
    model_provider.provider,
    model_type,
    model_provider.model_definitions,
  )
  if catalog_models:
    return catalog_models

  if model_provider.model_definitions:
    return [
      _basic_model_entity(model_id, model_type)
      for model_id in model_provider.model_definitions
    ]

  return _discover_openai_compatible_models(model_provider, model_type)


class ModelGatewayError(Exception):
  """可安全返回给内部调用方的模型网关错误。"""


def resolve_model_provider(namespace, data, required_model_type):
  query = ModelProvider.objects.filter(namespace=namespace, status=ModelProvider.Status.ACTIVE)
  if data.get("provider_id"):
    query = query.filter(id=data["provider_id"])
  else:
    query = query.filter(provider=data["provider"], name=data["credential_name"])
  model_provider = query.first()
  if not model_provider:
    raise ModelGatewayError("未找到可用的模型供应商凭据")
  if model_provider.provider != data["provider"]:
    raise ModelGatewayError("provider 与 provider_id 不匹配")
  if required_model_type not in model_provider.supported_model_types:
    raise ModelGatewayError(f"该供应商凭据不支持 {required_model_type}")
  if model_provider.model_definitions and data["model"] not in model_provider.model_definitions:
    raise ModelGatewayError("请求的模型未配置在该供应商凭据中")
  return model_provider


def _runtime_credentials(model_provider):
  try:
    return decrypt_credentials(model_provider.credentials_encrypted)
  except ValueError as exc:
    raise ModelGatewayError("模型供应商凭据无法解密") from exc


def _auth_headers(credentials):
  headers = {"Content-Type": "application/json"}
  if credentials.get("api_key"):
    headers["Authorization"] = f"Bearer {credentials['api_key']}"
  for key, value in (credentials.get("headers") or {}).items():
    if isinstance(key, str) and isinstance(value, str):
      headers[key] = value
  return headers


def _endpoint(credentials, override_key, suffix):
  if credentials.get(override_key):
    return credentials[override_key]
  base_url = credentials.get("base_url") or credentials.get("endpoint_url") or credentials.get("api_base_url")
  if not base_url:
    raise ModelGatewayError(f"缺少 {override_key} 或 base_url")
  return f"{base_url.rstrip('/')}/{suffix.lstrip('/')}"


def _post_upstream(url, credentials, payload):
  try:
    timeout = max(1, min(float(credentials.get("timeout", 60)), 300))
  except (TypeError, ValueError):
    timeout = 60
  try:
    response = requests.post(url, headers=_auth_headers(credentials), json=payload, timeout=(5, timeout))
    response.raise_for_status()
    return response.json()
  except requests.Timeout as exc:
    raise ModelGatewayError("上游模型服务请求超时") from exc
  except requests.RequestException as exc:
    status_code = getattr(exc.response, "status_code", None)
    message = f"上游模型服务请求失败（HTTP {status_code}）" if status_code else "上游模型服务连接失败"
    raise ModelGatewayError(message) from exc
  except ValueError as exc:
    raise ModelGatewayError("上游模型服务返回了无效 JSON") from exc


def invoke_embeddings(model_provider, model, texts, input_type="document", dimensions=None):
  credentials = _runtime_credentials(model_provider)
  payload = {"model": model, "input": texts}
  if dimensions is not None:
    payload["dimensions"] = dimensions
  if credentials.get("send_input_type"):
    payload["input_type"] = input_type
  result = _post_upstream(_endpoint(credentials, "embeddings_url", "embeddings"), credentials, payload)
  items = result.get("data") if isinstance(result, dict) else None
  if not isinstance(items, list):
    raise ModelGatewayError("上游 Embedding 响应缺少 data 数组")
  try:
    embeddings = [item["embedding"] for item in sorted(items, key=lambda item: item.get("index", 0))]
  except (KeyError, TypeError) as exc:
    raise ModelGatewayError("上游 Embedding 响应格式不正确") from exc
  if len(embeddings) != len(texts) or any(not isinstance(item, list) for item in embeddings):
    raise ModelGatewayError("上游 Embedding 返回数量或格式不正确")
  usage = result.get("usage") or {}
  tokens = usage.get("total_tokens", usage.get("prompt_tokens", 0))
  return {"model": result.get("model", model), "embeddings": embeddings, "usage": {"tokens": tokens}}


def _estimate_tokens(text):
  """无 tokenizer 服务时：CJK 字符逐字计数，其他内容按约 4 字节/token。"""
  cjk_count = sum(1 for char in text if "\u3400" <= char <= "\u9fff")
  other_count = len(text.encode("utf-8")) - cjk_count * 3
  return max(1, cjk_count + (max(0, other_count) + 3) // 4) if text else 0


def count_tokens(model_provider, model, texts):
  credentials = _runtime_credentials(model_provider)
  token_count_url = credentials.get("token_count_url")
  if not token_count_url:
    return {"model": model, "token_counts": [_estimate_tokens(text) for text in texts], "method": "estimated"}
  result = _post_upstream(token_count_url, credentials, {"model": model, "texts": texts})
  counts = result.get("token_counts") if isinstance(result, dict) else None
  if not isinstance(counts, list) or len(counts) != len(texts) or any(not isinstance(item, int) or item < 0 for item in counts):
    raise ModelGatewayError("上游 Token Count 响应格式不正确")
  return {"model": model, "token_counts": counts, "method": "upstream"}


def invoke_rerank(model_provider, model, query, documents, top_n=None, score_threshold=None):
  credentials = _runtime_credentials(model_provider)
  payload = {"model": model, "query": query, "documents": documents}
  if top_n is not None:
    payload["top_n"] = top_n
  result = _post_upstream(_endpoint(credentials, "rerank_url", "rerank"), credentials, payload)
  items = result.get("results") if isinstance(result, dict) else result if isinstance(result, list) else None
  if not isinstance(items, list):
    raise ModelGatewayError("上游 Rerank 响应缺少 results 数组")
  normalized = []
  try:
    for item in items:
      index = item["index"]
      score = item.get("relevance_score", item.get("score"))
      if not isinstance(index, int) or not 0 <= index < len(documents) or not isinstance(score, (int, float)):
        raise TypeError
      if score_threshold is None or score >= score_threshold:
        normalized.append({"index": index, "score": float(score), "document": documents[index]})
  except (KeyError, TypeError) as exc:
    raise ModelGatewayError("上游 Rerank 响应格式不正确") from exc
  normalized.sort(key=lambda item: item["score"], reverse=True)
  if top_n is not None:
    normalized = normalized[:top_n]
  return {"model": model, "results": normalized}
