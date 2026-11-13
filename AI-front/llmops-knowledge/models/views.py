import re
import secrets

from django.conf import settings
from django.db import IntegrityError, transaction
from drf_spectacular.utils import OpenApiParameter, extend_schema
from drf_spectacular.types import OpenApiTypes
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .serializers import (
  EmbeddingInvocationSerializer, ModelProviderCredentialCreateSerializer,
  ModelProviderSerializer, RerankInvocationSerializer, TokenCountInvocationSerializer,
)
from .services import ModelGatewayError, count_tokens, invoke_embeddings, invoke_rerank, resolve_model_provider


NAMESPACE_PARAMETER = OpenApiParameter(
  name="namespace",
  type=OpenApiTypes.STR,
  location=OpenApiParameter.HEADER,
  required=True,
  description="项目隔离标识",
)


class ModelProviderCredentialCreateView(APIView):
  """在本平台添加模型供应商配置。"""

  serializer_class = ModelProviderCredentialCreateSerializer

  @extend_schema(
    tags=["model-providers"],
    parameters=[NAMESPACE_PARAMETER],
    request=ModelProviderCredentialCreateSerializer,
    responses={201: ModelProviderSerializer},
    description="添加模型供应商凭据；credentials 会加密保存且不会通过接口返回。",
  )
  def post(self, request, provider):
    namespace = request.headers.get("namespace", "").strip()
    if not namespace:
      return Response({
        "code": "NAMESPACE_REQUIRED",
        "message": "缺少 namespace 请求头",
      }, status=status.HTTP_400_BAD_REQUEST)

    if not re.fullmatch(r"[A-Za-z0-9_.-]+(?:/[A-Za-z0-9_.-]+)*", provider):
      return Response({
        "code": "INVALID_PROVIDER",
        "message": "模型供应商标识格式不正确",
      }, status=status.HTTP_400_BAD_REQUEST)

    serializer = self.serializer_class(data=request.data)
    serializer.is_valid(raise_exception=True)

    try:
      with transaction.atomic():
        model_provider = serializer.save(namespace=namespace, provider=provider)
    except IntegrityError:
      return Response({
        "code": "MODEL_PROVIDER_ALREADY_EXISTS",
        "message": "该 namespace 下已存在同名的供应商凭据",
      }, status=status.HTTP_409_CONFLICT)
    except ValueError as exc:
      return Response({
        "code": "CREDENTIAL_ENCRYPTION_ERROR",
        "message": str(exc),
      }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    return Response({
      "code": 0,
      "message": "模型供应商添加成功",
      "data": ModelProviderSerializer(model_provider).data,
    }, status=status.HTTP_201_CREATED)


class InternalModelAPIView(APIView):
  model_type = None

  def initial(self, request, *args, **kwargs):
    super().initial(request, *args, **kwargs)
    expected = settings.MODEL_GATEWAY_SERVICE_TOKEN
    authorization = request.headers.get("Authorization", "")
    supplied = authorization[7:] if authorization.startswith("Bearer ") else ""
    if not expected or not supplied or not secrets.compare_digest(supplied, expected):
      from rest_framework.exceptions import AuthenticationFailed
      raise AuthenticationFailed("无效的模型网关服务令牌")

  def validate_request(self, request, serializer_class):
    namespace = request.headers.get("namespace", "").strip()
    if not namespace:
      return None, None, Response({"code": "NAMESPACE_REQUIRED", "message": "缺少 namespace 请求头"}, status=400)
    serializer = serializer_class(data=request.data)
    serializer.is_valid(raise_exception=True)
    try:
      provider = resolve_model_provider(namespace, serializer.validated_data, self.model_type)
    except ModelGatewayError as exc:
      return None, None, Response({"code": "MODEL_PROVIDER_ERROR", "message": str(exc)}, status=400)
    return provider, serializer.validated_data, None

  def gateway_response(self, callback):
    try:
      data = callback()
    except ModelGatewayError as exc:
      return Response({"code": "MODEL_UPSTREAM_ERROR", "message": str(exc)}, status=502)
    return Response({"code": 0, "message": "success", "data": data})


class EmbeddingInvocationView(InternalModelAPIView):
  model_type = "text-embedding"
  serializer_class = EmbeddingInvocationSerializer

  @extend_schema(tags=["internal-models"], parameters=[NAMESPACE_PARAMETER], request=EmbeddingInvocationSerializer)
  def post(self, request):
    provider, data, error = self.validate_request(request, EmbeddingInvocationSerializer)
    if error:
      return error
    return self.gateway_response(lambda: invoke_embeddings(provider, data["model"], data["texts"], data["input_type"], data.get("dimensions")))


class TokenCountInvocationView(InternalModelAPIView):
  model_type = "text-embedding"
  serializer_class = TokenCountInvocationSerializer

  @extend_schema(tags=["internal-models"], parameters=[NAMESPACE_PARAMETER], request=TokenCountInvocationSerializer)
  def post(self, request):
    provider, data, error = self.validate_request(request, TokenCountInvocationSerializer)
    if error:
      return error
    return self.gateway_response(lambda: count_tokens(provider, data["model"], data["texts"]))


class RerankInvocationView(InternalModelAPIView):
  model_type = "rerank"
  serializer_class = RerankInvocationSerializer

  @extend_schema(tags=["internal-models"], parameters=[NAMESPACE_PARAMETER], request=RerankInvocationSerializer)
  def post(self, request):
    provider, data, error = self.validate_request(request, RerankInvocationSerializer)
    if error:
      return error
    return self.gateway_response(lambda: invoke_rerank(provider, data["model"], data["query"], data["documents"], data.get("top_n"), data.get("score_threshold")))
