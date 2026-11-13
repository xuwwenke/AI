
import os
import requests
import dotenv
import json
from urllib.parse import parse_qs, urlsplit

from .base import BaseKnowledgeBackend, SegmentAttachmentError, SegmentImageNotFoundError

dotenv.load_dotenv()

class DifyClient(BaseKnowledgeBackend):
  def __init__(self):
    self.base_url = os.environ["DIFY_BASE_URL"].rstrip("/")
    self.api_key = os.environ["DIFY_API_KEY"]
    self.files_base_url = (os.environ.get("DIFY_FILES_BASE_URL") or self.base_url).rstrip("/")

  def create_dataset(self, name: str, description: str = "", **kwargs):
    url = f"{self.base_url}/v1/datasets"

    payload = {
      "name": name,
      "description": description,
    }

    # Dify 创建知识库支持的可选参数，按需透传
    optional_fields = [
      "embedding_model",
      "embedding_model_provider",
      "external_knowledge_api_id",
      "external_knowledge_id",
      "indexing_technique",
      "permission",
      "provider",
      "retrieval_model",
      "summary_index_setting",
    ]
    for field in optional_fields:
      value = kwargs.get(field)
      if value is not None:
        payload[field] = value

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json=payload,
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def upload_document(self, dataset_id: str, uploaded_file, indexing_technique: str = "economy",
                      embedding_model: str = None, embedding_model_provider: str = None, **kwargs):
    url = (
      f"{self.base_url}/v1/datasets/"
      f"{dataset_id}/document/create-by-file"
    )

    headers = {
      "Authorization": f"Bearer {self.api_key}",
    }

    # 构建 Dify data 字段：仅包含非空值，process_rule 默认走 automatic
    payload = {
      "indexing_technique": indexing_technique,
    }
    if embedding_model:
      payload["embedding_model"] = embedding_model
    if embedding_model_provider:
      payload["embedding_model_provider"] = embedding_model_provider

    # 额外可选参数透传
    for field in ("doc_form", "doc_language", "process_rule", "retrieval_model"):
      value = kwargs.get(field)
      if value is not None and value != {} and value != []:
        payload[field] = value

    # process_rule 未指定时使用 Dify 默认（automatic 模式）
    if "process_rule" not in payload:
      payload["process_rule"] = {
        "mode": "automatic",
        "rules": {},
      }

    # 不要手动设置 multipart/form-data 的 Content-Type，
    # requests 会自动生成带 boundary 的请求头。
    files = {
      "file": (
        uploaded_file.name,
        uploaded_file,
        getattr(uploaded_file, "content_type", None)
        or "application/octet-stream",
      ),
    }
    form_data = {
      "data": json.dumps(payload, ensure_ascii=False),
    }

    try:
      response = requests.post(url, headers=headers, data=form_data, files=files, timeout=(10, 120))
      if not response.ok:
        raise RuntimeError(
          f"Dify 上传失败，HTTP {response.status_code}: "
          f"{response.text}"
        )
      return response.json()
    finally:
      uploaded_file.close()

  def create_document_from_text(self, dataset_id: str, name: str, text: str, **kwargs):
    url = (
      f"{self.base_url}/v1/datasets/"
      f"{dataset_id}/document/create-by-text"
    )

    payload = {
      "name": name,
      "text": text,
    }

    # 额外可选参数透传：仅包含非空值
    optional_fields = (
      "indexing_technique",
      "embedding_model",
      "embedding_model_provider",
      "doc_form",
      "doc_language",
      "process_rule",
      "retrieval_model",
      "original_document_id",
    )
    for field in optional_fields:
      value = kwargs.get(field)
      if value is not None and value != "" and value != {} and value != []:
        payload[field] = value

    # process_rule 未指定时使用 Dify 默认（automatic 模式）
    if "process_rule" not in payload:
      payload["process_rule"] = {
        "mode": "automatic",
        "rules": {},
      }

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json=payload,
      timeout=(10, 120),
    )
    if not response.ok:
      raise RuntimeError(
        f"Dify 文本创建文档失败，HTTP {response.status_code}: "
        f"{response.text}"
      )
    return response.json()

  def update_document_by_file(self, dataset_id: str, document_id: str, uploaded_file, **kwargs):
    url = (
      f"{self.base_url}/v1/datasets/"
      f"{dataset_id}/documents/{document_id}"
    )

    headers = {
      "Authorization": f"Bearer {self.api_key}",
    }

    # 更新文档不接受 indexing_technique，其余可选参数透传
    payload = {}
    optional_fields = (
      "embedding_model",
      "embedding_model_provider",
      "doc_form",
      "doc_language",
      "process_rule",
      "retrieval_model",
    )
    for field in optional_fields:
      value = kwargs.get(field)
      if value is not None and value != "" and value != {} and value != []:
        payload[field] = value

    # process_rule 未指定时使用 Dify 默认（automatic 模式）
    if "process_rule" not in payload:
      payload["process_rule"] = {
        "mode": "automatic",
        "rules": {},
      }

    files = {
      "file": (
        uploaded_file.name,
        uploaded_file,
        getattr(uploaded_file, "content_type", None)
        or "application/octet-stream",
      ),
    }
    form_data = {
      "data": json.dumps(payload, ensure_ascii=False),
    }

    try:
      response = requests.patch(url, headers=headers, data=form_data, files=files, timeout=(10, 120))
      if not response.ok:
        raise RuntimeError(
          f"Dify 更新文档失败，HTTP {response.status_code}: "
          f"{response.text}"
        )
      return response.json()
    finally:
      uploaded_file.close()

  def get_dataset(self, dataset_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def delete_dataset(self, dataset_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}"

    response = requests.delete(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    # Dify 删除接口返回 204 No Content，响应体为空，直接返回空字典
    if response.status_code == 204 or not response.content:
      return {}
    return response.json()

  def update_dataset(self, dataset_id: str, name: str = None, description: str = None,
                     permission: str = None, **kwargs):
    url = f"{self.base_url}/v1/datasets/{dataset_id}"

    payload = {}
    if name is not None:
      payload["name"] = name
    if description is not None:
      payload["description"] = description
    if permission is not None:
      payload["permission"] = permission

    for field in (
      "embedding_model", "embedding_model_provider",
      "external_knowledge_api_id", "external_knowledge_id",
      "external_retrieval_model", "indexing_technique",
      "partial_member_list", "retrieval_model",
    ):
      if field in kwargs:
        payload[field] = kwargs[field]

    response = requests.patch(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json=payload,
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def retrieve(self, dataset_id: str, query: str, retrieval_model: dict = None):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/retrieve"

    payload = {
      "query": query,
    }
    if retrieval_model is not None:
      payload["retrieval_model"] = retrieval_model

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json=payload,
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def list_documents(self, dataset_id: str, page: int = 1, limit: int = 20):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents"

    params = {
      "page": page,
      "limit": limit,
    }

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      params=params,
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def get_document(self, dataset_id: str, document_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def get_document_indexing_status(self, dataset_id: str, batch: str):
    url = (
      f"{self.base_url}/v1/datasets/{dataset_id}/documents/"
      f"{batch}/indexing-status"
    )

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def delete_document(self, dataset_id: str, document_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}"

    response = requests.delete(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    # Dify 删除接口返回 204 No Content，响应体为空，直接返回空字典
    if response.status_code == 204 or not response.content:
      return {}
    return response.json()

  def download_document(self, dataset_id: str, document_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}/download"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    result = response.json()
    # Dify 自部署环境可能返回相对路径（如 /files/...），这里补全为完整 URL
    file_url = result.get("url", "")
    if file_url and file_url.startswith("/"):
      result["url"] = f"{self.base_url}{file_url}"
    return result

  def update_documents_status(self, dataset_id: str, action: str, document_ids: list):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/status/{action}"

    response = requests.patch(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "document_ids": document_ids,
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def get_segment_detail(self, dataset_id: str, document_id: str, segment_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}/segments/{segment_id}"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def update_segment(self, dataset_id: str, document_id: str, segment_id: str, segment: dict):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}/segments/{segment_id}"

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "segment": segment,
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def delete_segment(self, dataset_id: str, document_id: str, segment_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}/segments/{segment_id}"

    response = requests.delete(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    # Dify 删除接口返回 204 No Content，响应体为空，直接返回空字典
    if response.status_code == 204 or not response.content:
      return {}
    return response.json()

  def get_segment_image(self, dataset_id: str, document_id: str, segment_id: str, file_id: str):
    # 每次重新查询分段，既校验文件归属，也取得尚未过期的签名。
    segment = self.get_segment_detail(dataset_id, document_id, segment_id)["data"]
    attachment = next(
      (item for item in (segment.get("attachments") or []) if item.get("id") == file_id),
      None,
    )
    if attachment is None:
      raise SegmentImageNotFoundError("该图片不属于当前分段，或已被移除")

    source = urlsplit(attachment.get("source_url") or "")
    allowed_paths = {f"/files/{file_id}/image-preview", f"/files/{file_id}/file-preview"}
    file_path = next((path for path in allowed_paths if source.path.endswith(path)), None)
    signature = parse_qs(source.query)
    if not file_path or not all(signature.get(key) for key in ("timestamp", "nonce", "sign")):
      raise ValueError("Dify 未返回有效的图片签名地址")
    # 固定使用配置的 Dify 文件服务地址，保留签名参数；不访问附件提供的主机。
    # 文件签名接口不需要 API Key，禁止携带 Key 或跟随重定向。
    response = requests.get(
      f"{self.files_base_url}{file_path}?{source.query}",
      timeout=30, stream=True, allow_redirects=False,
    )
    try:
      response.raise_for_status()
      if response.status_code != 200:
        raise ValueError("Dify 图片读取返回了非 200 状态")
      content_type = response.headers.get("Content-Type", "").split(";", 1)[0].strip().lower()
      if content_type not in {"image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/tiff", "image/avif"}:
        raise ValueError("Dify 返回的内容不是支持预览的图片")
      # 分段图片通常不超过 2MiB；为历史附件保留余量，同时限制代理的内存占用。
      max_size = 20 * 1024 * 1024
      if int(response.headers.get("Content-Length", 0)) > max_size:
        raise ValueError("图片超过预览大小限制（20MiB）")
      chunks = []
      size = 0
      for chunk in response.iter_content(chunk_size=64 * 1024):
        size += len(chunk)
        if size > max_size:
          raise ValueError("图片超过预览大小限制（20MiB）")
        chunks.append(chunk)
      if not size:
        raise ValueError("Dify 返回的图片内容为空")
      return {
        "content": b"".join(chunks), "content_type": content_type,
        "name": attachment.get("name") or f"{file_id}.image",
      }
    finally:
      response.close()

  def upload_segment_image(self, uploaded_file):
    # 知识库 API Key 不能调用需要应用 Key 的 /v1/files/upload。
    # pipeline 上传接口把文件存入同一工作空间，可用作分段附件。
    response = requests.post(
      f"{self.base_url}/v1/datasets/pipeline/file-upload",
      headers={"Authorization": f"Bearer {self.api_key}"},
      files={"file": (uploaded_file.name, uploaded_file, uploaded_file.content_type)},
      timeout=60,
    )
    response.raise_for_status()
    result = response.json()
    if not isinstance(result, dict) or not result.get("id"):
      raise ValueError("Dify 上传响应缺少文件 ID")
    return result

  def add_segments(self, dataset_id: str, document_id: str, segments: list):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}/segments"

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "segments": segments,
      },
      timeout=30,
    )
    response.raise_for_status()
    result = response.json()
    if not any(segment.get("attachment_ids") for segment in segments):
      return result

    # 部分 Dify 版本的批量创建接口接收 attachment_ids，却未保存附件绑定。
    # 只对创建结果中缺失附件的分段补一次更新，避免重复创建分段。
    created_segments = result.get("data") if isinstance(result, dict) else None
    if (
      not isinstance(created_segments, list)
      or len(created_segments) != len(segments)
      or any(not isinstance(item, dict) or not item.get("id") for item in created_segments)
    ):
      raise SegmentAttachmentError(result, [{"message": "Dify 创建响应缺少可匹配的分段 ID，需核对创建结果"}])
    failed_segments = []
    for index, (segment, created) in enumerate(zip(segments, created_segments)):
      attachment_ids = segment.get("attachment_ids")
      if not attachment_ids:
        continue
      try:
        existing_ids = {item["id"] for item in (created.get("attachments") or [])}
        if existing_ids == set(attachment_ids):
          continue
        updated = self.update_segment(
          dataset_id, document_id, created["id"],
          {"attachment_ids": attachment_ids},
        )["data"]
        # 保留创建响应中的其他字段，并返回补关联后的附件信息。
        created.update(updated)
        if {item["id"] for item in (created.get("attachments") or [])} != set(attachment_ids):
          raise ValueError("Dify 未保存全部图片附件，请检查知识库索引配置和文件 ID")
      except Exception as exc:
        failed_segments.append({
          "index": index, "segment_id": created.get("id"),
          "attachment_ids": attachment_ids, "message": str(exc),
        })
    if failed_segments:
      raise SegmentAttachmentError(result, failed_segments)
    return result

  def get_document_segments(self, dataset_id: str, document_id: str, page: int = 1,
                            limit: int = 20, status: list = None, keyword: str = None):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/{document_id}/segments"

    params = {
      "page": page,
      "limit": limit,
    }
    if status:
      # Dify 接收重复的 status 参数来表示数组
      params["status"] = status
    if keyword:
      params["keyword"] = keyword

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      params=params,
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  # 元数据
  def create_metadata(self, dataset_id: str, name: str, type: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/metadata"

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "name": name,
        "type": type,
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def update_metadata(self, dataset_id: str, metadata_id: str, name: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/metadata/{metadata_id}"

    response = requests.patch(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "name": name,
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def delete_metadata(self, dataset_id: str, metadata_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/metadata/{metadata_id}"

    response = requests.delete(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    if response.status_code == 204 or not response.content:
      return {}
    return response.json()

  def get_metadata(self, dataset_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/metadata"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def get_built_in_metadata(self, dataset_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/metadata/built-in"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def update_built_in_metadata_status(self, dataset_id: str, action: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/metadata/built-in/{action}"

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def update_documents_metadata(self, dataset_id: str, operation_data: list):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/documents/metadata"

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "operation_data": operation_data,
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def create_tag(self, name: str):
    url = f"{self.base_url}/v1/datasets/tags"

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "name": name,
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def list_tags(self):
    url = f"{self.base_url}/v1/datasets/tags"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def update_tag(self, tag_id: str, name: str):
    url = f"{self.base_url}/v1/datasets/tags"

    response = requests.patch(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "tag_id": tag_id,
        "name": name,
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def delete_tag(self, tag_id: str):
    url = f"{self.base_url}/v1/datasets/tags"

    response = requests.delete(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "tag_id": tag_id,
      },
      timeout=30,
    )
    response.raise_for_status()
    if response.status_code == 204:
      return {}
    return response.json()

  def bind_tags(self, tag_ids: list, target_id: str):
    url = f"{self.base_url}/v1/datasets/tags/binding"

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json={
        "tag_ids": tag_ids,
        "target_id": target_id,
      },
      timeout=30,
    )
    response.raise_for_status()
    if response.status_code == 204:
      return {}
    return response.json()

  def unbind_tags(self, target_id: str, tag_ids: list = None, tag_id: str = None):
    url = f"{self.base_url}/v1/datasets/tags/unbinding"

    payload = {
      "target_id": target_id,
    }
    if tag_ids is not None:
      payload["tag_ids"] = tag_ids
    if tag_id is not None:
      payload["tag_id"] = tag_id

    response = requests.post(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
        "Content-Type": "application/json",
      },
      json=payload,
      timeout=30,
    )
    response.raise_for_status()
    if response.status_code == 204:
      return {}
    return response.json()

  def get_dataset_tags(self, dataset_id: str):
    url = f"{self.base_url}/v1/datasets/{dataset_id}/tags"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()

  def get_models(self, model_type: str):
    url = f"{self.base_url}/v1/workspaces/current/models/model-types/{model_type}"

    response = requests.get(
      url,
      headers={
        "Authorization": f"Bearer {self.api_key}",
      },
      timeout=30,
    )
    response.raise_for_status()
    return response.json()
