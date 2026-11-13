
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser
from rest_framework import serializers
from drf_spectacular.utils import extend_schema, OpenApiParameter, inline_serializer
from drf_spectacular.types import OpenApiTypes
from django.shortcuts import get_object_or_404
from requests import HTTPError

from knowledge.models import KnowledgeBase, KnowledgeDocument
from knowledge.serializers import KnowledgeDocumentSerializer
from knowledge.services.factory import get_backend
from .serializers import (
  DocumentSerializer,
  DocumentCreateOptionsSerializer,
  DocumentUpdateOptionsSerializer,
  TextDocumentCreateSerializer,
  DocumentIndexingStatusSerializer,
  parse_json_form_field,
)


NAMESPACE_PARAMETER = OpenApiParameter(
  name="namespace",
  type=OpenApiTypes.STR,
  location=OpenApiParameter.HEADER,
  required=True,
  description="项目隔离标识",
)


def get_namespace_from_request(request):
  namespace = request.headers.get("namespace")
  if not namespace:
    return None, Response({
      "code": "NAMESPACE_REQUIRED",
      "message": "缺少 namespace 请求头",
    }, status=status.HTTP_400_BAD_REQUEST)
  return namespace, None


def get_backend_or_error(backend_type: str):
  try:
    return get_backend(backend_type), None
  except ValueError as exc:
    return None, Response({
      "code": "BACKEND_NOT_SUPPORTED",
      "message": str(exc),
    }, status=status.HTTP_400_BAD_REQUEST)


def get_knowledge_base_or_404(knowledge_base_id, namespace):
  return get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)


class DocumentListView(APIView):
  """获取知识库下的文档列表 / 上传文档"""
  parser_classes = [MultiPartParser, FormParser]
  MAX_FILE_SIZE = 20 * 1024 * 1024
  ALLOWED_EXTENSIONS = {".pdf", ".docx", ".txt", ".md", ".markdown"}

  @extend_schema(
    tags=["documents"],
    parameters=[
      NAMESPACE_PARAMETER,
      OpenApiParameter(name="page", type=OpenApiTypes.INT, location=OpenApiParameter.QUERY, required=False, description="页码，默认 1"),
      OpenApiParameter(name="limit", type=OpenApiTypes.INT, location=OpenApiParameter.QUERY, required=False, description="每页数量，默认 20"),
    ],
    description="获取知识库下的文档列表（合并后端信息）",
  )
  def get(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      page = int(request.query_params.get("page", 1))
    except (TypeError, ValueError):
      page = 1
    try:
      limit = int(request.query_params.get("limit", 20))
    except (TypeError, ValueError):
      limit = 20

    try:
      backend_result = backend.list_documents(
        dataset_id=knowledge_base.backend_dataset_id,
        page=page,
        limit=limit,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_LIST_DOCUMENTS_FAILED",
        "message": "获取后端文档列表失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    backend_documents = backend_result.get("data", []) if isinstance(backend_result, dict) else []
    backend_ids = {doc.get("id") for doc in backend_documents if doc.get("id")}

    local_docs_qs = KnowledgeDocument.objects.filter(
      knowledge_base=knowledge_base,
      backend_document_id__in=backend_ids,
    )
    local_doc_map = {doc.backend_document_id: doc for doc in local_docs_qs}

    merged_documents = []
    for backend_doc in backend_documents:
      doc_id = backend_doc.get("id")
      local_doc = local_doc_map.get(doc_id)
      if local_doc:
        item = DocumentSerializer(local_doc).data
        item.update({k: v for k, v in backend_doc.items() if k not in item or item[k] in (None, "")})
        item["id"] = str(local_doc.id)
        item["backend_document_id"] = doc_id
      else:
        item = dict(backend_doc)
        item["id"] = doc_id
        item["backend_document_id"] = doc_id
        item["file_name"] = backend_doc.get("name", "")
        item["status"] = backend_doc.get("indexing_status", "unknown")
      merged_documents.append(item)

    response_data = {
      "count": backend_result.get("total", len(merged_documents)) if isinstance(backend_result, dict) else len(merged_documents),
      "page": page,
      "limit": limit,
      "data": merged_documents,
    }

    return Response({
      "code": 0,
      "message": "success",
      "data": response_data,
    })

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="上传文档到指定知识库",
  )
  def post(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, error = get_backend_or_error(knowledge_base.backend_type)
    if error:
      return error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法上传文档",
      }, status=status.HTTP_400_BAD_REQUEST)

    upload_file = request.FILES.get("file")
    if not upload_file:
      return Response({
        "code": "NO_FILE",
        "message": "请上传文件",
      }, status=status.HTTP_400_BAD_REQUEST)

    file_name = upload_file.name
    extension = "." + file_name.rsplit(".", 1)[-1].lower() if "." in file_name else ""
    if extension not in self.ALLOWED_EXTENSIONS:
      return Response({
        "code": "INVALID_FILE_EXTENSION",
        "message": f"文件扩展名 {extension} 不被支持",
      }, status=status.HTTP_400_BAD_REQUEST)

    if upload_file.size > self.MAX_FILE_SIZE:
      return Response({
        "code": "FILE_SIZE_TOO_LARGE",
        "message": "文件大小不能超过 20 MiB",
      }, status=status.HTTP_400_BAD_REQUEST)

    document = KnowledgeDocument.objects.create(
      knowledge_base=knowledge_base,
      file_name=file_name,
      file_size=upload_file.size,
      content_type=upload_file.content_type,
      status="pending",
      error_message="",
    )
    try:
      # 收集并校验文档创建可选参数
      options_data = {
        "indexing_technique": request.data.get("indexing_technique"),
        "embedding_model": request.data.get("embedding_model"),
        "embedding_model_provider": request.data.get("embedding_model_provider"),
        "doc_form": request.data.get("doc_form"),
        "doc_language": request.data.get("doc_language"),
        "process_rule": parse_json_form_field(request.data.get("process_rule")),
        "retrieval_model": parse_json_form_field(request.data.get("retrieval_model")),
      }
      # 去掉值为 None 的字段，使用序列化器默认值
      options_data = {k: v for k, v in options_data.items() if v is not None}
      options_serializer = DocumentCreateOptionsSerializer(data=options_data)
      options_serializer.is_valid(raise_exception=True)

      indexing_technique = options_serializer.validated_data.get("indexing_technique") or "economy"
      embedding_model = options_serializer.validated_data.get("embedding_model") or None
      embedding_model_provider = options_serializer.validated_data.get("embedding_model_provider") or None
      extra_kwargs = {}
      for field in ("doc_form", "doc_language", "process_rule", "retrieval_model"):
        value = options_serializer.validated_data.get(field)
        if value is not None:
          extra_kwargs[field] = value

      result = backend.upload_document(
        dataset_id=knowledge_base.backend_dataset_id,
        uploaded_file=upload_file,
        indexing_technique=indexing_technique,
        embedding_model=embedding_model,
        embedding_model_provider=embedding_model_provider,
        **extra_kwargs,
      )
      backend_document = result.get("document", result)
      backend_document_id = backend_document.get("id")
      backend_batch_id = result.get("batch", "")
      if not backend_document_id:
        raise ValueError(f"后端响应中缺少 document ID: {result}")
      document.backend_document_id = backend_document_id
      document.backend_batch_id = backend_batch_id
      document.status = "indexing"
      document.error_message = ""
      document.save(
        update_fields=["backend_document_id", "backend_batch_id", "status", "error_message", "updated_at"]
      )
    except Exception as exc:
      document.status = "failed"
      document.error_message = str(exc)
      document.save(
        update_fields=["status", "error_message", "updated_at"]
      )
      return Response({
        "code": "BACKEND_UPLOAD_FAILED",
        "message": "平台文档记录已创建，但后端文档上传失败",
        "document": KnowledgeDocumentSerializer(document).data,
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "文档已提交，正在等待索引",
      "data": KnowledgeDocumentSerializer(document).data,
    }, status=status.HTTP_201_CREATED)


class DocumentDetailView(APIView):
  """获取/删除知识库中的单个文档"""

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="获取文档详情（合并后端信息）",
  )
  def get(self, request, knowledge_base_id, document_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if local_doc.backend_document_id:
      try:
        backend_info = backend.get_document(
          dataset_id=knowledge_base.backend_dataset_id,
          document_id=local_doc.backend_document_id,
        )
      except Exception as exc:
        return Response({
          "code": "BACKEND_GET_DOCUMENT_FAILED",
          "message": "获取后端文档详情失败",
          "detail": str(exc),
          "document": DocumentSerializer(local_doc).data,
        }, status=status.HTTP_502_BAD_GATEWAY)

      data = DocumentSerializer(local_doc).data
      platform_id = data["id"]
      data.update(backend_info)
      data["id"] = platform_id
      data["backend_document_id"] = local_doc.backend_document_id
    else:
      data = DocumentSerializer(local_doc).data

    return Response({
      "code": 0,
      "message": "success",
      "data": data,
    })

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="通过重新上传文件更新文档内容",
  )
  def put(self, request, knowledge_base_id, document_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, error = get_backend_or_error(knowledge_base.backend_type)
    if error:
      return error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法更新文档",
      }, status=status.HTTP_400_BAD_REQUEST)

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED",
        "message": "该文档尚未同步到后端，无法更新",
      }, status=status.HTTP_400_BAD_REQUEST)

    upload_file = request.FILES.get("file")
    if not upload_file:
      return Response({
        "code": "NO_FILE",
        "message": "请上传文件",
      }, status=status.HTTP_400_BAD_REQUEST)

    extension = "." + upload_file.name.rsplit(".", 1)[-1].lower() if "." in upload_file.name else ""
    if extension not in DocumentListView.ALLOWED_EXTENSIONS:
      return Response({
        "code": "INVALID_FILE_EXTENSION",
        "message": f"文件扩展名 {extension} 不被支持",
      }, status=status.HTTP_400_BAD_REQUEST)

    if upload_file.size > DocumentListView.MAX_FILE_SIZE:
      return Response({
        "code": "FILE_SIZE_TOO_LARGE",
        "message": "文件大小不能超过 20 MiB",
      }, status=status.HTTP_400_BAD_REQUEST)

    # 收集并校验文档更新可选参数
    options_data = {
      "embedding_model": request.data.get("embedding_model"),
      "embedding_model_provider": request.data.get("embedding_model_provider"),
      "doc_form": request.data.get("doc_form"),
      "doc_language": request.data.get("doc_language"),
      "process_rule": parse_json_form_field(request.data.get("process_rule")),
      "retrieval_model": parse_json_form_field(request.data.get("retrieval_model")),
    }
    options_data = {k: v for k, v in options_data.items() if v is not None}
    options_serializer = DocumentUpdateOptionsSerializer(data=options_data)
    options_serializer.is_valid(raise_exception=True)

    extra_kwargs = {}
    for field in (
      "embedding_model", "embedding_model_provider", "doc_form",
      "doc_language", "process_rule", "retrieval_model",
    ):
      value = options_serializer.validated_data.get(field)
      if value is not None and value != "" and value != {} and value != []:
        extra_kwargs[field] = value

    try:
      result = backend.update_document_by_file(
        dataset_id=knowledge_base.backend_dataset_id,
        document_id=local_doc.backend_document_id,
        uploaded_file=upload_file,
        **extra_kwargs,
      )
      backend_document = result.get("document", result)
      backend_document_id = backend_document.get("id")
      backend_batch_id = result.get("batch", "")
      # 更新本地记录：文件名、大小、类型等
      local_doc.file_name = upload_file.name
      local_doc.file_size = upload_file.size
      local_doc.content_type = upload_file.content_type
      if backend_document_id:
        local_doc.backend_document_id = backend_document_id
      local_doc.backend_batch_id = backend_batch_id
      local_doc.status = "indexing"
      local_doc.error_message = ""
      local_doc.save(
        update_fields=[
          "file_name", "file_size", "content_type",
          "backend_document_id", "backend_batch_id",
          "status", "error_message", "updated_at",
        ]
      )
    except Exception as exc:
      local_doc.status = "failed"
      local_doc.error_message = str(exc)
      local_doc.save(update_fields=["status", "error_message", "updated_at"])
      return Response({
        "code": "BACKEND_UPDATE_DOCUMENT_FAILED",
        "message": "后端文档更新失败",
        "document": KnowledgeDocumentSerializer(local_doc).data,
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "文档已更新，正在等待重新索引",
      "data": KnowledgeDocumentSerializer(local_doc).data,
    })

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="删除文档（同时删除后端远端）",
  )
  def delete(self, request, knowledge_base_id, document_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if local_doc.backend_document_id and knowledge_base.backend_dataset_id:
      try:
        backend.delete_document(
          dataset_id=knowledge_base.backend_dataset_id,
          document_id=local_doc.backend_document_id,
        )
      except Exception as exc:
        return Response({
          "code": "BACKEND_DELETE_DOCUMENT_FAILED",
          "message": "后端文档删除失败，本地记录未删除",
          "detail": str(exc),
        }, status=status.HTTP_502_BAD_GATEWAY)

    local_doc.delete()

    return Response({
      "code": 0,
      "message": "文档删除成功",
    })


class DocumentDownloadView(APIView):
  """获取文档原始文件的下载 URL"""

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="获取文档原始文件的下载 URL（签名 URL，由后端存储服务生成）",
  )
  def get(self, request, knowledge_base_id, document_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED",
        "message": "该文档尚未同步到后端，无法下载",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.download_document(
        dataset_id=knowledge_base.backend_dataset_id,
        document_id=local_doc.backend_document_id,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_DOWNLOAD_DOCUMENT_FAILED",
        "message": "获取文档下载 URL 失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })


class DocumentIndexingStatusView(APIView):
  """获取创建或更新文档批次的索引进度"""

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="根据创建或更新文档时返回的 batch ID 获取文档索引状态及分段进度",
    responses={
      200: inline_serializer(
        name="DocumentIndexingStatusResponse",
        fields={
          "code": serializers.IntegerField(),
          "message": serializers.CharField(),
          "data": DocumentIndexingStatusSerializer(),
        },
      ),
    },
  )
  def get(self, request, knowledge_base_id, batch):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.get_document_indexing_status(
        dataset_id=knowledge_base.backend_dataset_id,
        batch=batch,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_GET_INDEXING_STATUS_FAILED",
        "message": "获取文档索引状态失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })


class DocumentsStatusUpdateView(APIView):
  """批量更新文档状态（启用/停用/归档/恢复）"""
  parser_classes = [JSONParser]

  VALID_ACTIONS = {"enable", "disable", "archive", "un_archive"}

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="批量更新文档状态。action 可选值：enable（启用）、disable（停用）、archive（归档）、un_archive（取消归档）",
  )
  def patch(self, request, knowledge_base_id, action):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    if action not in self.VALID_ACTIONS:
      return Response({
        "code": "INVALID_ACTION",
        "message": f"无效的 action: {action}，可选值: enable, disable, archive, un_archive",
      }, status=status.HTTP_400_BAD_REQUEST)

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    document_ids = request.data.get("document_ids")
    if not document_ids or not isinstance(document_ids, list):
      return Response({
        "code": "INVALID_DOCUMENT_IDS",
        "message": "document_ids 必须为非空数组",
      }, status=status.HTTP_400_BAD_REQUEST)

    # 将平台侧 document_id 转换为后端 backend_document_id
    local_docs = KnowledgeDocument.objects.filter(
      id__in=document_ids,
      knowledge_base=knowledge_base,
    )
    backend_document_ids = [
      doc.backend_document_id for doc in local_docs if doc.backend_document_id
    ]

    if not backend_document_ids:
      return Response({
        "code": "NO_SYNCED_DOCUMENTS",
        "message": "没有已同步到后端的文档可更新",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.update_documents_status(
        dataset_id=knowledge_base.backend_dataset_id,
        action=action,
        document_ids=backend_document_ids,
      )
    except Exception as exc:
      error_data = {
        "code": "BACKEND_UPDATE_STATUS_FAILED",
        "message": "批量更新文档状态失败",
        "detail": str(exc),
      }
      response_status = status.HTTP_502_BAD_GATEWAY
      if isinstance(exc, HTTPError) and exc.response is not None:
        backend_response = exc.response
        error_data["backend_status"] = backend_response.status_code
        try:
          backend_error = backend_response.json()
        except ValueError:
          backend_error = None

        if isinstance(backend_error, dict):
          error_data["detail"] = (
            backend_error.get("message") or backend_error.get("detail")
            or backend_response.text or str(exc)
          )
          if backend_error.get("code"):
            error_data["backend_code"] = backend_error["code"]
        else:
          error_data["detail"] = backend_response.text or str(exc)

        # 后端的业务拒绝应保留状态；鉴权、协议和服务故障仍作为网关错误。
        if backend_response.status_code in {400, 404, 409, 422, 429}:
          response_status = backend_response.status_code

      return Response(error_data, status=response_status)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })


class DocumentMetadataUpdateView(APIView):
  """批量更新文档元数据"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="批量更新文档元数据。每个 operation_data 元素包含 document_id、metadata_list、partial_update",
  )
  def post(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    operation_data = request.data.get("operation_data")
    if not operation_data or not isinstance(operation_data, list):
      return Response({
        "code": "INVALID_OPERATION_DATA",
        "message": "operation_data 必须为非空数组",
      }, status=status.HTTP_400_BAD_REQUEST)

    local_doc_ids = []
    for item in operation_data:
      doc_id = item.get("document_id")
      if not doc_id:
        return Response({
          "code": "INVALID_OPERATION_DATA",
          "message": "每个 operation_data 元素必须包含 document_id",
        }, status=status.HTTP_400_BAD_REQUEST)
      metadata_list = item.get("metadata_list")
      if not isinstance(metadata_list, list):
        return Response({
          "code": "INVALID_OPERATION_DATA",
          "message": "每个 operation_data 元素必须包含 metadata_list 数组",
        }, status=status.HTTP_400_BAD_REQUEST)
      local_doc_ids.append(doc_id)

    local_docs = KnowledgeDocument.objects.filter(
      id__in=local_doc_ids,
      knowledge_base=knowledge_base,
    )
    doc_id_map = {
      str(doc.id): doc.backend_document_id
      for doc in local_docs if doc.backend_document_id
    }

    backend_operation_data = []
    for item in operation_data:
      local_id = str(item["document_id"])
      backend_doc_id = doc_id_map.get(local_id)
      if not backend_doc_id:
        continue
      backend_operation_data.append({
        "document_id": backend_doc_id,
        "metadata_list": item["metadata_list"],
        "partial_update": item.get("partial_update", False),
      })

    if not backend_operation_data:
      return Response({
        "code": "NO_SYNCED_DOCUMENTS",
        "message": "没有已同步到后端的文档可更新",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.update_documents_metadata(
        dataset_id=knowledge_base.backend_dataset_id,
        operation_data=backend_operation_data,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_UPDATE_DOCUMENTS_METADATA_FAILED",
        "message": "批量更新文档元数据失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })


class DocumentFromTextView(APIView):
  """通过文本内容创建文档"""
  parser_classes = [JSONParser, MultiPartParser, FormParser]

  @extend_schema(
    tags=["documents"],
    parameters=[NAMESPACE_PARAMETER],
    description="通过文本内容创建文档到指定知识库",
  )
  def post(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_knowledge_base_or_404(knowledge_base_id, namespace)

    backend, error = get_backend_or_error(knowledge_base.backend_type)
    if error:
      return error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法创建文档",
      }, status=status.HTTP_400_BAD_REQUEST)

    # 支持 application/json 或 multipart/form-data 两种请求方式
    # multipart 场景下 process_rule / retrieval_model 可能是 JSON 字符串
    request_data = dict(request.data)
    for field in ("process_rule", "retrieval_model"):
      if field in request_data:
        request_data[field] = parse_json_form_field(request_data[field])

    serializer = TextDocumentCreateSerializer(data=request_data)
    serializer.is_valid(raise_exception=True)

    validated = serializer.validated_data
    name = validated["name"]
    text = validated["text"]

    # 组装透传给 Dify 的可选参数
    dify_kwargs = {}
    for field in (
      "indexing_technique", "embedding_model", "embedding_model_provider",
      "doc_form", "doc_language", "process_rule", "retrieval_model",
      "original_document_id",
    ):
      value = validated.get(field)
      if value is not None and value != "" and value != {} and value != []:
        dify_kwargs[field] = value

    document = KnowledgeDocument.objects.create(
      knowledge_base=knowledge_base,
      file_name=name,
      file_size=len(text.encode("utf-8")),
      content_type="text/plain",
      status="pending",
      error_message="",
    )

    try:
      result = backend.create_document_from_text(
        dataset_id=knowledge_base.backend_dataset_id,
        name=name,
        text=text,
        **dify_kwargs,
      )
      backend_document = result.get("document", result)
      backend_document_id = backend_document.get("id")
      backend_batch_id = result.get("batch", "")
      if not backend_document_id:
        raise ValueError(f"后端响应中缺少 document ID: {result}")
      document.backend_document_id = backend_document_id
      document.backend_batch_id = backend_batch_id
      document.status = "indexing"
      document.error_message = ""
      document.save(
        update_fields=["backend_document_id", "backend_batch_id", "status", "error_message", "updated_at"]
      )
    except Exception as exc:
      document.status = "failed"
      document.error_message = str(exc)
      document.save(
        update_fields=["status", "error_message", "updated_at"]
      )
      return Response({
        "code": "BACKEND_CREATE_TEXT_DOCUMENT_FAILED",
        "message": "平台文档记录已创建，但后端文本创建文档失败",
        "document": KnowledgeDocumentSerializer(document).data,
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "文档已提交，正在等待索引",
      "data": KnowledgeDocumentSerializer(document).data,
    }, status=status.HTTP_201_CREATED)
