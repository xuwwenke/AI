from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import JSONParser, MultiPartParser
from rest_framework.renderers import JSONRenderer
from drf_spectacular.utils import extend_schema, OpenApiParameter
from drf_spectacular.types import OpenApiTypes
from django.shortcuts import get_object_or_404
from django.http import HttpResponse
from django.urls import reverse
from django.utils.http import content_disposition_header
from urllib.parse import urlencode

from knowledge.models import KnowledgeBase, KnowledgeDocument
from knowledge.services.factory import get_backend
from knowledge.services.base import SegmentAttachmentError, SegmentImageNotFoundError
from requests import HTTPError
from .renderers import ImagePreviewRenderer
from .serializers import (
  SegmentImageUploadSerializer, SegmentAttachmentsSerializer,
  SegmentsCreateRequestSerializer, SegmentUpdateRequestSerializer,
  SegmentImageUploadResponseSerializer,
  SegmentListResponseSerializer, SegmentDetailResponseSerializer,
  SegmentsBatchRequestSerializer, SegmentsBatchResponseSerializer,
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


def add_image_preview_urls(result, knowledge_base_id, document_id, namespace):
  """在分段附件中添加本站地址，保留 Dify 原始字段和响应层级。"""
  if not isinstance(result, dict):
    return
  segments = result.get("data")
  if isinstance(segments, dict):
    segments = [segments]
  if not isinstance(segments, list):
    return
  for segment in segments:
    if not isinstance(segment, dict) or not segment.get("id"):
      continue
    for attachment in segment.get("attachments") or []:
      if not isinstance(attachment, dict) or not attachment.get("id"):
        continue
      path = reverse("segment-image-preview", kwargs={
        "knowledge_base_id": knowledge_base_id, "document_id": document_id,
        "segment_id": segment["id"], "file_id": attachment["id"],
      })
      attachment["preview_url"] = f"{path}?{urlencode({'namespace': namespace})}"


class SegmentImagePreviewView(APIView):
  renderer_classes = [JSONRenderer, ImagePreviewRenderer]

  @extend_schema(
    tags=["chunks"],
    parameters=[
      OpenApiParameter(name="namespace", type=OpenApiTypes.STR, location=OpenApiParameter.HEADER,
                       required=False, description="项目隔离标识，和同名查询参数至少提供一个，请求头优先"),
      OpenApiParameter(name="namespace", type=OpenApiTypes.STR, location=OpenApiParameter.QUERY,
                       required=False, description="供 img src 使用的项目隔离标识"),
    ],
    responses={
      (200, "image/png"): OpenApiTypes.BINARY,
      (200, "image/jpeg"): OpenApiTypes.BINARY,
      (200, "image/gif"): OpenApiTypes.BINARY,
      (200, "image/webp"): OpenApiTypes.BINARY,
      (400, "application/json"): OpenApiTypes.OBJECT,
      (404, "application/json"): OpenApiTypes.OBJECT,
      (502, "application/json"): OpenApiTypes.OBJECT,
    },
    description=(
      "预览指定分段中的图片，成功时直接返回图片二进制（不是 JSON 或重定向）。"
      "校验 namespace、知识库、文档及分段附件归属。"
      "可直接将分段附件的 preview_url 用作 img src；namespace 可通过请求头或查询参数传入。"
    ),
  )
  def get(self, request, knowledge_base_id, document_id, segment_id, file_id):
    namespace = request.headers.get("namespace") or request.query_params.get("namespace")
    if not namespace:
      return Response({
        "code": "NAMESPACE_REQUIRED", "message": "请通过请求头或查询参数提供 namespace",
      }, status=status.HTTP_400_BAD_REQUEST)
    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)
    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)
    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND", "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)
    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED", "message": "该文档尚未同步到后端，无法预览图片",
      }, status=status.HTTP_400_BAD_REQUEST)
    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error
    try:
      image = backend.get_segment_image(
        dataset_id=knowledge_base.backend_dataset_id, document_id=local_doc.backend_document_id,
        segment_id=segment_id, file_id=file_id,
      )
    except SegmentImageNotFoundError as exc:
      return Response({"code": "SEGMENT_IMAGE_NOT_FOUND", "message": str(exc)}, status=status.HTTP_404_NOT_FOUND)
    except HTTPError as exc:
      backend_status = exc.response.status_code if exc.response is not None else None
      response_status = status.HTTP_404_NOT_FOUND if backend_status == 404 else status.HTTP_502_BAD_GATEWAY
      return Response({
        "code": "BACKEND_GET_SEGMENT_IMAGE_FAILED", "message": "读取分段图片失败", "detail": str(exc),
      }, status=response_status)
    except Exception as exc:
      return Response({
        "code": "BACKEND_GET_SEGMENT_IMAGE_FAILED", "message": "读取分段图片失败", "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)
    response = HttpResponse(image["content"], content_type=image["content_type"])
    response["Content-Disposition"] = content_disposition_header(False, image["name"])
    response["Content-Length"] = len(image["content"])
    response["Cache-Control"] = "private, no-store"
    response["X-Content-Type-Options"] = "nosniff"
    return response


class SegmentImageUploadView(APIView):
  """先上传一张分段图片，再用返回的文件 ID 添加或更新分段。"""
  parser_classes = [MultiPartParser]

  @extend_schema(
    tags=["chunks"],
    parameters=[NAMESPACE_PARAMETER],
    request={"multipart/form-data": {
      "type": "object",
      "properties": {"file": {"type": "string", "format": "binary"}},
      "required": ["file"],
    }},
    responses={201: SegmentImageUploadResponseSerializer},
    description=(
      "上传单张分段图片（jpg、jpeg、png、gif，默认最大 2MB）。"
      "返回 data.id 为 Dify 文件 ID，将其放入分段的 attachment_ids。"
      "每个分段默认最多 10 张图片，多张图片请分别上传。"
    ),
  )
  def post(self, request, knowledge_base_id, document_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error
    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)
    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)
    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND", "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)
    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED", "message": "该文档尚未同步到后端，无法上传分段图片",
      }, status=status.HTTP_400_BAD_REQUEST)
    if set(request.FILES) != {"file"} or len(request.FILES.getlist("file")) != 1:
      return Response({
        "code": "INVALID_IMAGE_UPLOAD", "message": "请通过 file 字段上传且仅上传一张图片",
      }, status=status.HTTP_400_BAD_REQUEST)
    serializer = SegmentImageUploadSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)
    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error
    try:
      result = backend.upload_segment_image(serializer.validated_data["file"])
    except HTTPError as exc:
      backend_status = exc.response.status_code if exc.response is not None else None
      response_status = backend_status if backend_status in (400, 413, 415) else status.HTTP_502_BAD_GATEWAY
      return Response({
        "code": "BACKEND_UPLOAD_SEGMENT_IMAGE_FAILED", "message": "上传分段图片失败",
        "detail": str(exc),
      }, status=response_status)
    except Exception as exc:
      return Response({
        "code": "BACKEND_UPLOAD_SEGMENT_IMAGE_FAILED", "message": "上传分段图片失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)
    return Response({
      "code": 0, "message": "图片上传成功", "data": result,
    }, status=status.HTTP_201_CREATED)


class DocumentSegmentsListView(APIView):
  """获取分段列表 / 向文档添加分段"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["chunks"],
    parameters=[
      NAMESPACE_PARAMETER,
      OpenApiParameter(name="page", type=OpenApiTypes.INT, location=OpenApiParameter.QUERY, required=False, description="页码，默认 1"),
      OpenApiParameter(name="limit", type=OpenApiTypes.INT, location=OpenApiParameter.QUERY, required=False, description="每页数量，默认 20，上限 100"),
      OpenApiParameter(name="status", type=OpenApiTypes.STR, location=OpenApiParameter.QUERY, required=False, description="按索引状态筛选，可传多次，如 completed、indexing、error"),
      OpenApiParameter(name="keyword", type=OpenApiTypes.STR, location=OpenApiParameter.QUERY, required=False, description="搜索关键词"),
    ],
    responses={200: SegmentListResponseSerializer},
    description="获取指定文档下的分段列表，附件包含可直接展示的 preview_url；其余 Dify 分段字段原样保留",
  )
  def get(self, request, knowledge_base_id, document_id):
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

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED",
        "message": "该文档尚未同步到后端，无法获取分段",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      page = int(request.query_params.get("page", 1))
    except (TypeError, ValueError):
      page = 1
    if page < 1:
      page = 1

    try:
      limit = int(request.query_params.get("limit", 20))
    except (TypeError, ValueError):
      limit = 20
    if limit < 1:
      limit = 20
    if limit > 100:
      limit = 100

    # status 支持传多次（数组）
    status_list = request.query_params.getlist("status")
    keyword = request.query_params.get("keyword")

    try:
      result = backend.get_document_segments(
        dataset_id=knowledge_base.backend_dataset_id,
        document_id=local_doc.backend_document_id,
        page=page,
        limit=limit,
        status=status_list or None,
        keyword=keyword or None,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_GET_SEGMENTS_FAILED",
        "message": "获取文档分段列表失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    add_image_preview_urls(result, knowledge_base_id, document_id, namespace)
    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["chunks"],
    parameters=[NAMESPACE_PARAMETER],
    request=SegmentsCreateRequestSerializer,
    responses={200: SegmentListResponseSerializer},
    description="向指定文档添加分段（支持批量），attachment_ids 可关联已上传的图片文件 ID",
  )
  def post(self, request, knowledge_base_id, document_id):
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

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED",
        "message": "该文档尚未同步到后端，无法添加分段",
      }, status=status.HTTP_400_BAD_REQUEST)

    segments = request.data.get("segments")
    if not isinstance(segments, list) or len(segments) == 0:
      return Response({
        "code": "INVALID_SEGMENTS",
        "message": "segments 必须为非空数组",
      }, status=status.HTTP_400_BAD_REQUEST)

    for seg in segments:
      if not isinstance(seg, dict):
        return Response({
          "code": "INVALID_SEGMENTS",
          "message": "segments 中每个元素必须为对象",
        }, status=status.HTTP_400_BAD_REQUEST)
      if not seg.get("content"):
        return Response({
          "code": "INVALID_SEGMENTS",
          "message": "每个分段必须包含非空 content 字段",
        }, status=status.HTTP_400_BAD_REQUEST)
      attachment_serializer = SegmentAttachmentsSerializer(data=seg)
      attachment_serializer.is_valid(raise_exception=True)

    try:
      result = backend.add_segments(
        dataset_id=knowledge_base.backend_dataset_id,
        document_id=local_doc.backend_document_id,
        segments=segments,
      )
    except SegmentAttachmentError as exc:
      add_image_preview_urls(exc.result, knowledge_base_id, document_id, namespace)
      return Response({
        "code": "BACKEND_ATTACH_SEGMENT_IMAGES_FAILED", "message": str(exc),
        "data": exc.result, "failed_segments": exc.failed_segments,
      }, status=status.HTTP_502_BAD_GATEWAY)
    except Exception as exc:
      return Response({
        "code": "BACKEND_ADD_SEGMENTS_FAILED",
        "message": "添加分段失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    add_image_preview_urls(result, knowledge_base_id, document_id, namespace)
    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })


class SegmentsBatchView(APIView):
  """批量启用、禁用或删除当前文档下的分段，逐项报告结果。"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["chunks"],
    parameters=[NAMESPACE_PARAMETER],
    request=SegmentsBatchRequestSerializer,
    responses={
      200: SegmentsBatchResponseSerializer,
      207: SegmentsBatchResponseSerializer,
      502: SegmentsBatchResponseSerializer,
    },
    description=(
      "批量操作当前文档的分段。action：enable（启用）、disable（禁用）、delete（删除）。"
      "segment_ids 为非空 UUID 数组，最多 100 个，自动去重。"
      "逐项处理，失败不回滚已成功项；已处于目标启用状态的分段直接成功。"
      "全部成功返回 200，部分失败返回 207，全部失败返回 502；每项结果见 data.results。"
    ),
  )
  def post(self, request, knowledge_base_id, document_id):
    namespace, error = get_namespace_from_request(request)
    if error:
      return error
    knowledge_base = get_object_or_404(KnowledgeBase, id=knowledge_base_id, namespace=namespace)
    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)
    if not knowledge_base.backend_dataset_id:
      return Response({
        "code": "DATASET_NOT_FOUND", "message": "该知识库尚未关联后端知识库",
      }, status=status.HTTP_400_BAD_REQUEST)
    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED", "message": "该文档尚未同步到后端，无法批量操作分段",
      }, status=status.HTTP_400_BAD_REQUEST)
    serializer = SegmentsBatchRequestSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)
    backend, backend_error = get_backend_or_error(knowledge_base.backend_type)
    if backend_error:
      return backend_error

    action = serializer.validated_data["action"]
    results = []
    for segment_id in serializer.validated_data["segment_ids"]:
      item = {"segment_id": segment_id, "success": False}
      scope = {
        "dataset_id": knowledge_base.backend_dataset_id,
        "document_id": local_doc.backend_document_id,
        "segment_id": segment_id,
      }
      try:
        if action == "delete":
          backend.delete_segment(**scope)
        else:
          enabled = action == "enable"
          current = backend.get_segment_detail(**scope)["data"]
          if current["enabled"] is enabled:
            item["unchanged"] = True
          else:
            updated = backend.update_segment(**scope, segment={"enabled": enabled})["data"]
            if updated["enabled"] is not enabled:
              raise ValueError("Dify 未将分段更新为目标启用状态")
        item["success"] = True
      except Exception as exc:
        item.update(code="BACKEND_SEGMENT_OPERATION_FAILED", message=str(exc))
        if isinstance(exc, HTTPError) and exc.response is not None:
          item["backend_status"] = exc.response.status_code
          try:
            detail = exc.response.json()
          except ValueError:
            detail = None
          if isinstance(detail, dict):
            item["message"] = detail.get("message") or detail.get("detail") or str(exc)
            if detail.get("code"):
              item["backend_code"] = detail["code"]
      results.append(item)

    success_count = sum(item["success"] for item in results)
    failure_count = len(results) - success_count
    if not failure_count:
      response_status, code, message = status.HTTP_200_OK, 0, "批量操作成功"
    elif success_count:
      response_status, code, message = status.HTTP_207_MULTI_STATUS, "BATCH_PARTIALLY_FAILED", "部分分段操作失败"
    else:
      response_status, code, message = status.HTTP_502_BAD_GATEWAY, "BATCH_FAILED", "全部分段操作失败"
    return Response({
      "code": code, "message": message,
      "data": {
        "action": action, "total": len(results), "success_count": success_count,
        "failure_count": failure_count, "results": results,
      },
    }, status=response_status)


class SegmentDetailView(APIView):
  """获取/更新单个分段"""
  parser_classes = [JSONParser]

  @extend_schema(
    tags=["chunks"],
    parameters=[NAMESPACE_PARAMETER],
    responses={200: SegmentDetailResponseSerializer},
    description="获取指定文档下的单个分段详情，附件包含可直接展示的 preview_url；其余 Dify 分段字段原样保留",
  )
  def get(self, request, knowledge_base_id, document_id, segment_id):
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

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED",
        "message": "该文档尚未同步到后端，无法获取分段",
      }, status=status.HTTP_400_BAD_REQUEST)

    try:
      result = backend.get_segment_detail(
        dataset_id=knowledge_base.backend_dataset_id,
        document_id=local_doc.backend_document_id,
        segment_id=segment_id,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_GET_SEGMENT_DETAIL_FAILED",
        "message": "获取分段详情失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    add_image_preview_urls(result, knowledge_base_id, document_id, namespace)
    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["chunks"],
    parameters=[NAMESPACE_PARAMETER],
    request=SegmentUpdateRequestSerializer,
    responses={200: SegmentDetailResponseSerializer},
    description="更新指定文档下的单个分段，attachment_ids 省略或 null 时保留图片，空数组时清空图片",
  )
  def post(self, request, knowledge_base_id, document_id, segment_id):
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

    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)

    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED",
        "message": "该文档尚未同步到后端，无法更新分段",
      }, status=status.HTTP_400_BAD_REQUEST)

    segment = request.data.get("segment")
    if not segment or not isinstance(segment, dict):
      return Response({
        "code": "INVALID_SEGMENT",
        "message": "segment 必须为非空对象",
      }, status=status.HTTP_400_BAD_REQUEST)
    attachment_serializer = SegmentAttachmentsSerializer(data=segment)
    attachment_serializer.is_valid(raise_exception=True)

    try:
      result = backend.update_segment(
        dataset_id=knowledge_base.backend_dataset_id,
        document_id=local_doc.backend_document_id,
        segment_id=segment_id,
        segment=segment,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_UPDATE_SEGMENT_FAILED",
        "message": "更新分段失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)

    add_image_preview_urls(result, knowledge_base_id, document_id, namespace)
    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })

  @extend_schema(
    tags=["chunks"],
    parameters=[NAMESPACE_PARAMETER],
    description="删除指定文档下的单个分段",
  )
  def delete(self, request, knowledge_base_id, document_id, segment_id):
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
    
    local_doc = get_object_or_404(KnowledgeDocument, id=document_id, knowledge_base=knowledge_base)
    if not local_doc.backend_document_id:
      return Response({
        "code": "DOCUMENT_NOT_SYNCED",
        "message": "该文档尚未同步到后端，无法删除分段",
      }, status=status.HTTP_400_BAD_REQUEST)
    
    try:
      result = backend.delete_segment(
        dataset_id=knowledge_base.backend_dataset_id,
        document_id=local_doc.backend_document_id,
        segment_id=segment_id,
      )
    except Exception as exc:
      return Response({
        "code": "BACKEND_DELETE_SEGMENT_FAILED",
        "message": "删除分段失败",
        "detail": str(exc),
      }, status=status.HTTP_502_BAD_GATEWAY)
    
    return Response({
      "code": 0,
      "message": "success",
      "data": result,
    })
