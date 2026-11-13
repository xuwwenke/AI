from django.shortcuts import render

# Create your views here.
from .models import KnowledgeBase, KnowledgeDocument
from .serializers import (
  KnowledgeBaseSerializer,
  KnowledgeBaseUpdateSerializer,
  KnowledgeBaseUpdateResponseSerializer,
  KnowledgeDocumentSerializer,
  KnowledgeBaseRetrieveSerializer,
  DifyModelsResponseSerializer,
)
from requests import HTTPError
from django.db import transaction
from rest_framework.response import Response
from rest_framework import status
from rest_framework import generics
from rest_framework.parsers import JSONParser
from django.shortcuts import get_object_or_404
from rest_framework.views import APIView
from drf_spectacular.utils import extend_schema, OpenApiParameter
from drf_spectacular.types import OpenApiTypes
from .services.factory import get_backend
from models.models import ModelProvider
from models.provider_catalogs import get_provider_metadata
from models.serializers import ModelProviderSerializer
from models.services import get_provider_models


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


# ListCreateAPIView 是 DRF 提供的通用视图：
# - GET：查询知识库列表。
# - POST：校验请求数据，并创建知识库记录。
class KnowledgeBaseListCreateView(generics.ListCreateAPIView):
  queryset = KnowledgeBase.objects.all()
  serializer_class = KnowledgeBaseSerializer

  @extend_schema(tags=["knowledge-bases"], parameters=[NAMESPACE_PARAMETER])
  def get(self, request, *args, **kwargs):
    return super().get(request, *args, **kwargs)

  @extend_schema(tags=["knowledge-bases"], parameters=[NAMESPACE_PARAMETER])
  def post(self, request, *args, **kwargs):
    return self.create(request, *args, **kwargs)

  def get_queryset(self):
    queryset = super().get_queryset()
    namespace = self.request.headers.get("namespace")
    if namespace:
      queryset = queryset.filter(namespace=namespace)
    else:
      queryset = queryset.none()
    return queryset

  def create(self, request, *args, **kwargs):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    # 1. 验证请求参数
    serializer = self.get_serializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    # 提前收集 Dify 创建知识库的可选参数（仅传非空值），
    # 必须在 serializer.save() 之前获取，因为 create() 会把这些字段从 validated_data 中剔除。
    # 空字符串、None、空字典、空列表都视为"未指定"，不传给 Dify，让 Dify 使用默认值。
    dify_create_params = {}
    for field in [
      "embedding_model", "embedding_model_provider",
      "external_knowledge_api_id", "external_knowledge_id",
      "indexing_technique", "permission", "provider",
      "retrieval_model", "summary_index_setting",
    ]:
      value = serializer.validated_data.get(field)
      if value is not None and value != "" and value != {} and value != []:
        dify_create_params[field] = value

    # 2. 先创建平台自己的知识库记录，namespace 从请求头注入
    with transaction.atomic():
      knowledge_base = serializer.save(status="pending", namespace=namespace)

    # 3. 根据 backend_type 获取对应的后端客户端，调用其创建知识库接口
    backend, error = get_backend_or_error(knowledge_base.backend_type)
    if error:
      knowledge_base.status = "failed"
      knowledge_base.error_message = error.data.get("message", "")
      knowledge_base.save(update_fields=["status", "error_message", "updated_at"])
      return error

    try:

      result = backend.create_dataset(
        name=knowledge_base.name,
        description=knowledge_base.description,
        **dify_create_params,
      )
      # 4. 保存后端返回的知识库 ID
      knowledge_base.backend_dataset_id = result["id"]
      knowledge_base.status = "ready"
      knowledge_base.error_message = ""
      knowledge_base.save(
        update_fields=["backend_dataset_id", "status", "error_message", "updated_at"]
      )

    except Exception as exc:
      # 5. 保留本地记录，方便查看和重试
      knowledge_base.status = "failed"
      knowledge_base.error_message = str(exc)
      knowledge_base.save(
        update_fields=["status", "error_message", "updated_at"]
      )
      return Response({
        "code": "BACKEND_CREATE_FAILED",
        "message": "平台知识库记录已创建，但后端知识库创建失败",
        "knowledge_base": KnowledgeBaseSerializer(knowledge_base).data,
      }, status=status.HTTP_502_BAD_GATEWAY)

    # 6. 返回创建结果
    return Response({
      "code": 0,
      "message": "知识库创建成功",
      "data": KnowledgeBaseSerializer(knowledge_base).data,
    }, status=status.HTTP_201_CREATED)


class KnowledgeBaseDetailView(APIView):
  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="获取知识库详情，合并后端侧信息",
  )
  def get(self, request, pk):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=pk, namespace=namespace)
    data = KnowledgeBaseSerializer(knowledge_base).data

    if knowledge_base.backend_dataset_id:
      backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
      if backend_error:
        return backend_error
      try:
        backend_info = backend.get_dataset(knowledge_base.backend_dataset_id)
      except Exception as exc:
        return Response({
          "code": "BACKEND_GET_DATASET_FAILED",
          "message": "获取后端知识库详情失败",
          "detail": str(exc),
          "knowledge_base": data,
        }, status=status.HTTP_502_BAD_GATEWAY)

      # 平台 ID 用于路由，保留本地值；其余后端字段直接合并到顶层
      platform_id = data["id"]
      data.update(backend_info)
      data["id"] = platform_id
      data["creator_username"] = knowledge_base.creator_username
      data["creator_email"] = knowledge_base.creator_email
    else:
      data.update({
        "app_count": 0,
        "author_name": None,
        "document_count": 0,
        "embedding_model": None,
        "indexing_technique": None,
        "permission": None,
        "provider": None,
        "word_count": 0,
      })

    return Response({
      "code": 0,
      "message": "获取知识库详情成功",
      "data": data,
    }, status=status.HTTP_200_OK)

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="删除知识库（同时删除后端远端）",
  )
  def delete(self, request, pk):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=pk, namespace=namespace)

    # 先调用后端删除远端知识库
    if knowledge_base.backend_dataset_id:
      backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
      if backend_error:
        return backend_error
      try:
        backend.delete_dataset(knowledge_base.backend_dataset_id)
      except Exception as exc:
        return Response({
          "code": "BACKEND_DELETE_FAILED",
          "message": "后端知识库删除失败，本地记录未删除",
          "detail": str(exc),
        }, status=status.HTTP_502_BAD_GATEWAY)

    # 后端删除成功后，删除本地记录（级联删除文档）
    knowledge_base.delete()

    return Response({
      "code": 0,
      "message": "知识库删除成功",
    }, status=status.HTTP_200_OK)

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="部分更新知识库名称、描述、嵌入模型、访问权限、指定成员及检索配置，合并返回后端更新结果",
    request=KnowledgeBaseUpdateSerializer,
    responses={200: KnowledgeBaseUpdateResponseSerializer},
  )
  def patch(self, request, pk):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=pk, namespace=namespace)

    serializer = KnowledgeBaseUpdateSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)
    update_data = serializer.validated_data
    if not update_data:
      return Response({
        "code": "NO_FIELDS_TO_UPDATE",
        "message": "没有可更新的字段",
      }, status=status.HTTP_400_BAD_REQUEST)

    if not knowledge_base.backend_dataset_id and set(update_data) - {"name", "description"}:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法更新后端配置",
      }, status=status.HTTP_400_BAD_REQUEST)

    # 先调用后端更新远端，成功后再更新本地
    backend_info = {}
    if knowledge_base.backend_dataset_id:
      backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
      if backend_error:
        return backend_error
      try:
        backend_info = backend.update_dataset(
          dataset_id=knowledge_base.backend_dataset_id,
          **update_data,
        )
      except Exception as exc:
        return Response({
          "code": "BACKEND_UPDATE_FAILED",
          "message": "后端知识库更新失败，本地记录未更新",
          "detail": str(exc),
        }, status=status.HTTP_502_BAD_GATEWAY)

    local_fields = [field for field in ("name", "description") if field in update_data]
    if local_fields:
      for field in local_fields:
        setattr(knowledge_base, field, update_data[field])
      knowledge_base.save(update_fields=[*local_fields, "updated_at"])

    data = KnowledgeBaseSerializer(knowledge_base).data
    # PATCH 已返回完整后端详情；保留平台路由、项目归属和创建者快照。
    platform_fields = {
      field: data[field] for field in (
        "id", "namespace", "backend_type", "backend_dataset_id",
        "creator_username", "creator_email",
      )
    }
    data.update(backend_info)
    data.update(platform_fields)

    return Response({
      "code": 0,
      "message": "知识库更新成功",
      "data": data,
    }, status=status.HTTP_200_OK)


class KnowledgeBaseRetrieveView(APIView):
  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="从知识库检索分段（测试检索）",
    request=KnowledgeBaseRetrieveSerializer,
  )
  def post(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    backend, error = get_backend_or_error(knowledge_base.backend_type)
    if error:
      return error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法检索",
      }, status=status.HTTP_400_BAD_REQUEST)

    serializer = KnowledgeBaseRetrieveSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    query = serializer.validated_data["query"]
    retrieval_model = serializer.validated_data.get("retrieval_model")

    try:
      result = backend.retrieve(
        dataset_id=knowledge_base.backend_dataset_id,
        query=query,
        retrieval_model=retrieval_model,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_RETRIEVE_FAILED",
        "message": "后端知识库检索失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "检索成功",
      "data": result,
    }, status=status.HTTP_200_OK)


class TagCreateView(APIView):
  """获取 / 创建 / 更新知识库标签"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="获取知识库标签列表",
  )
  def get(self, request):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    backend, backend_error = get_backend_or_error("dify")
    if backend_error:
      return backend_error

    try:
      result = backend.list_tags()
    except Exception as exc:
      return Response({
        "code": "BACKEND_LIST_TAGS_FAILED",
        "message": "获取标签列表失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="创建知识库标签",
  )
  def post(self, request):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    name = request.data.get("name")
    if not name or not isinstance(name, str):
      return Response({
        "code": "INVALID_NAME",
        "message": "name 必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    name = name.strip()
    if len(name) < 1 or len(name) > 50:
      return Response({
        "code": "INVALID_NAME",
        "message": "name 长度必须在 1-50 个字符之间",
      }, status=status.HTTP_400_BAD_REQUEST)

    backend, backend_error = get_backend_or_error("dify")
    if backend_error:
      return backend_error

    try:
      result = backend.create_tag(name=name)
    except Exception as exc:
      return Response({
        "code": "BACKEND_CREATE_TAG_FAILED",
        "message": "创建标签失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="重命名知识库标签",
  )
  def patch(self, request):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    tag_id = request.data.get("tag_id")
    name = request.data.get("name")

    if not tag_id or not isinstance(tag_id, str):
      return Response({
        "code": "INVALID_TAG_ID",
        "message": "tag_id 必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    if not name or not isinstance(name, str):
      return Response({
        "code": "INVALID_NAME",
        "message": "name 必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    name = name.strip()
    if len(name) < 1 or len(name) > 50:
      return Response({
        "code": "INVALID_NAME",
        "message": "name 长度必须在 1-50 个字符之间",
      }, status=status.HTTP_400_BAD_REQUEST)

    backend, backend_error = get_backend_or_error("dify")
    if backend_error:
      return backend_error

    try:
      result = backend.update_tag(tag_id=tag_id, name=name)
    except Exception as exc:
      return Response({
        "code": "BACKEND_UPDATE_TAG_FAILED",
        "message": "更新标签失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="删除知识库标签",
  )
  def delete(self, request):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    tag_id = request.data.get("tag_id")
    if not tag_id or not isinstance(tag_id, str):
      return Response({
        "code": "INVALID_TAG_ID",
        "message": "tag_id 必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    backend, backend_error = get_backend_or_error("dify")
    if backend_error:
      return backend_error

    try:
      backend.delete_tag(tag_id=tag_id)
    except Exception as exc:
      return Response({
        "code": "BACKEND_DELETE_TAG_FAILED",
        "message": "删除标签失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
    }, status=status.HTTP_200_OK)


class TagBindingView(APIView):
  """绑定标签到知识库"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="绑定标签到知识库",
  )
  def post(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法绑定标签",
      }, status=status.HTTP_400_BAD_REQUEST)

    tag_ids = request.data.get("tag_ids")
    if not isinstance(tag_ids, list) or len(tag_ids) < 1:
      return Response({
        "code": "INVALID_TAG_IDS",
        "message": "tag_ids 必须为非空数组",
      }, status=status.HTTP_400_BAD_REQUEST)

    if not all(isinstance(tid, str) and tid for tid in tag_ids):
      return Response({
        "code": "INVALID_TAG_IDS",
        "message": "tag_ids 中的每个元素必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    try:
      backend.bind_tags(tag_ids=tag_ids, target_id=knowledge_base.backend_dataset_id)
    except Exception as exc:
      return Response({
        "code": "BACKEND_BIND_TAGS_FAILED",
        "message": "绑定标签失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
    }, status=status.HTTP_200_OK)


class TagUnbindingView(APIView):
  """解除标签与知识库的绑定"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="解除标签与知识库的绑定",
  )
  def post(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法解绑标签",
      }, status=status.HTTP_400_BAD_REQUEST)

    tag_ids = request.data.get("tag_ids")
    tag_id = request.data.get("tag_id")

    if tag_ids is None and tag_id is None:
      return Response({
        "code": "INVALID_TAG_IDS",
        "message": "tag_ids 与 tag_id 不能同时为空",
      }, status=status.HTTP_400_BAD_REQUEST)

    if tag_ids is not None:
      if not isinstance(tag_ids, list) or len(tag_ids) < 1:
        return Response({
          "code": "INVALID_TAG_IDS",
          "message": "tag_ids 必须为非空数组",
        }, status=status.HTTP_400_BAD_REQUEST)
      if not all(isinstance(tid, str) and tid for tid in tag_ids):
        return Response({
          "code": "INVALID_TAG_IDS",
          "message": "tag_ids 中的每个元素必须为非空字符串",
        }, status=status.HTTP_400_BAD_REQUEST)

    if tag_id is not None and (not isinstance(tag_id, str) or not tag_id):
      return Response({
        "code": "INVALID_TAG_ID",
        "message": "tag_id 必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    try:
      backend.unbind_tags(
        target_id=knowledge_base.backend_dataset_id,
        tag_ids=tag_ids,
        tag_id=tag_id,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_UNBIND_TAGS_FAILED",
        "message": "解绑标签失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
    }, status=status.HTTP_200_OK)


class DatasetTagsView(APIView):
  """获取绑定到知识库的标签列表"""

  @extend_schema(
    tags=["knowledge-bases"],
    parameters=[NAMESPACE_PARAMETER],
    description="获取绑定到知识库的标签列表",
  )
  def get(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库，无法获取绑定标签",
      }, status=status.HTTP_400_BAD_REQUEST)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    try:
      result = backend.get_dataset_tags(dataset_id=knowledge_base.backend_dataset_id)
    except Exception as exc:
      return Response({
        "code": "BACKEND_GET_DATASET_TAGS_FAILED",
        "message": "获取知识库绑定标签失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })


VALID_MODEL_TYPES = {"text-embedding", "rerank", "llm", "tts", "speech2text", "moderation"}


class ModelsView(APIView):
  """获取 Dify 当前工作空间指定类型的可用模型列表。"""

  @extend_schema(
    tags=["models"],
    parameters=[
      NAMESPACE_PARAMETER,
      OpenApiParameter(
        name="model_type", type=OpenApiTypes.STR, location=OpenApiParameter.PATH,
        required=True, enum=sorted(VALID_MODEL_TYPES),
        description="知识库嵌入模型使用 text-embedding，重排序模型使用 rerank",
      ),
    ],
    responses={200: DifyModelsResponseSerializer},
    description="获取 Dify API Key 所属工作空间的可用模型，data 为供应商数组，保留后端完整模型字段",
  )
  def get(self, request, model_type):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    if model_type not in VALID_MODEL_TYPES:
      return Response({
        "code": "INVALID_MODEL_TYPE",
        "message": f"model_type 必须为以下值之一: {', '.join(sorted(VALID_MODEL_TYPES))}",
      }, status=status.HTTP_400_BAD_REQUEST)

    backend, backend_error = get_backend_or_error("dify")
    if backend_error:
      return backend_error

    try:
      result = backend.get_models(model_type=model_type)
      if not isinstance(result, dict) or not isinstance(result.get("data"), list):
        raise ValueError("Dify 返回的模型列表格式不正确")
    except Exception as exc:
      error_data = {
        "code": "BACKEND_GET_MODELS_FAILED",
        "message": "获取 Dify 可用模型失败",
        "detail": str(exc),
      }
      if isinstance(exc, HTTPError) and exc.response is not None:
        backend_response = exc.response
        error_data["backend_status"] = backend_response.status_code
        try:
          backend_error = backend_response.json()
        except ValueError:
          backend_error = None
        error_data["detail"] = backend_response.text or str(exc)
        if isinstance(backend_error, dict):
          error_data["detail"] = backend_error.get("message") or error_data["detail"]
          if backend_error.get("code"):
            error_data["backend_code"] = backend_error["code"]
      return Response(error_data, status=status.HTTP_502_BAD_GATEWAY)

    return Response({"code": 0, "message": "success", "data": result["data"]})


class LocalModelsView(APIView):
  """获取本平台自管供应商的可用模型列表。"""

  serializer_class = ModelProviderSerializer

  @extend_schema(
    tags=["models"],
    parameters=[NAMESPACE_PARAMETER],
    description="获取当前 namespace 下本平台自管供应商的模型列表，不查询 Dify",
  )
  def get(self, request, model_type):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    if model_type not in VALID_MODEL_TYPES:
      return Response({
        "code": "INVALID_MODEL_TYPE",
        "message": f"model_type 必须为以下值之一: {', '.join(sorted(VALID_MODEL_TYPES))}",
      }, status=status.HTTP_400_BAD_REQUEST)

    providers = ModelProvider.objects.filter(
      namespace=namespace,
      status=ModelProvider.Status.ACTIVE,
    )

    result = []
    for provider in providers:
      if model_type not in provider.supported_model_types:
        continue
      provider_metadata = get_provider_metadata(provider.provider)
      item = {
        "id": str(provider.id),
        "provider": provider.provider,
        "label": provider_metadata["label"],
        "credential_name": provider.name,
        "supported_model_types": provider.supported_model_types,
        "status": "active",
        "models": [],
      }
      try:
        item["models"] = get_provider_models(provider, model_type)
      except ValueError as exc:
        item["status"] = "error"
        item["error"] = str(exc)
      result.append(item)

    return Response({
      "code": 0,
      "message": "success",
      "data": {"providers": result},
    })
