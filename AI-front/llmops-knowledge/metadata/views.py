from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import JSONParser
from drf_spectacular.utils import extend_schema, OpenApiParameter
from drf_spectacular.types import OpenApiTypes
from django.shortcuts import get_object_or_404

from knowledge.models import KnowledgeBase
from knowledge.services.factory import get_backend


NAMESPACE_PARAMETER = OpenApiParameter(
  name="namespace",
  type=OpenApiTypes.STR,
  location=OpenApiParameter.HEADER,
  required=True,
  description="项目隔离标识",
)

VALID_METADATA_TYPES = ("string", "number", "time")


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


class MetadataListView(APIView):
  """知识库元数据字段管理"""
  parser_classes = [JSONParser]

  @extend_schema(tags=['metadata'], parameters=[NAMESPACE_PARAMETER], description="获取指定知识库的元数据字段列表")
  def get(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error
  
    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)
    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error
    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.get_metadata(
        dataset_id=knowledge_base.backend_dataset_id,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_LIST_METADATA_FAILED",
        "message": "获取元数据字段列表失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)
    return Response(result)

  @extend_schema(
    tags=["metadata"],
    parameters=[NAMESPACE_PARAMETER],
    description="为指定知识库创建元数据字段",
  )
  def post(self, request, knowledge_base_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    name = request.data.get("name")
    field_type = request.data.get("type")

    if not name or not isinstance(name, str):
      return Response({
        "code": "INVALID_NAME",
        "message": "name 必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    if len(name) > 255:
      return Response({
        "code": "INVALID_NAME",
        "message": "name 不能超过 255 个字符",
      }, status=status.HTTP_400_BAD_REQUEST)

    if not field_type or field_type not in VALID_METADATA_TYPES:
      return Response({
        "code": "INVALID_TYPE",
        "message": f"type 必须为以下之一: {', '.join(VALID_METADATA_TYPES)}",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.create_metadata(
        dataset_id=knowledge_base.backend_dataset_id,
        name=name,
        type=field_type,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_CREATE_METADATA_FAILED",
        "message": "创建元数据字段失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })


class MetadataDetailView(APIView):
  """更新 / 删除知识库元数据字段"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["metadata"],
    parameters=[NAMESPACE_PARAMETER],
    description="重命名指定知识库的元数据字段",
  )
  def patch(self, request, knowledge_base_id, metadata_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    name = request.data.get("name")

    if not name or not isinstance(name, str):
      return Response({
        "code": "INVALID_NAME",
        "message": "name 必须为非空字符串",
      }, status=status.HTTP_400_BAD_REQUEST)

    if len(name) > 255:
      return Response({
        "code": "INVALID_NAME",
        "message": "name 不能超过 255 个字符",
      }, status=status.HTTP_400_BAD_REQUEST)

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.update_metadata(
        dataset_id=knowledge_base.backend_dataset_id,
        metadata_id=str(metadata_id),
        name=name,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_UPDATE_METADATA_FAILED",
        "message": "更新元数据字段失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["metadata"],
    parameters=[NAMESPACE_PARAMETER],
    description="删除指定知识库的元数据字段",
  )
  def delete(self, request, knowledge_base_id, metadata_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      backend.delete_metadata(
        dataset_id=knowledge_base.backend_dataset_id,
        metadata_id=str(metadata_id),
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_DELETE_METADATA_FAILED",
        "message": "删除元数据字段失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response(status=status.HTTP_204_NO_CONTENT)


class BuiltInMetadataView(APIView):
  """获取 / 启用 / 停用知识库内置元数据字段"""

  VALID_ACTIONS = ("enable", "disable")

  @extend_schema(
    tags=["metadata"],
    parameters=[NAMESPACE_PARAMETER],
    description="获取指定知识库的内置元数据字段列表",
  )
  def get(self, request, knowledge_base_id, action=None):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.get_built_in_metadata(
        dataset_id=knowledge_base.backend_dataset_id,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_GET_BUILTIN_METADATA_FAILED",
        "message": "获取内置元数据字段失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["metadata"],
    parameters=[NAMESPACE_PARAMETER],
    description="启用或停用知识库的内置元数据字段",
  )
  def post(self, request, knowledge_base_id, action=None):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error

    if not action or action not in self.VALID_ACTIONS:
      return Response({
        "code": "INVALID_ACTION",
        "message": f"action 必须为以下之一: {', '.join(self.VALID_ACTIONS)}",
      }, status=status.HTTP_400_BAD_REQUEST)

    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)

    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND",
        "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.update_built_in_metadata_status(
        dataset_id=knowledge_base.backend_dataset_id,
        action=action,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_UPDATE_BUILTIN_METADATA_FAILED",
        "message": "更新内置元数据字段状态失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })