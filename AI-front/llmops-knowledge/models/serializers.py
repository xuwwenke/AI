from rest_framework import serializers

from .models import ModelProvider
from .services import encrypt_credentials


class ModelProviderCredentialCreateSerializer(serializers.Serializer):
  name = serializers.CharField(max_length=255)
  supported_model_types = serializers.ListField(
    child=serializers.ChoiceField(choices=[
      "llm", "text-embedding", "rerank", "tts", "speech2text", "moderation",
    ]),
    allow_empty=False,
    required=False,
    default=["llm"],
  )
  models = serializers.ListField(
    child=serializers.CharField(max_length=255),
    allow_empty=True,
    required=False,
    default=list,
    write_only=True,
  )
  credentials = serializers.DictField(
    child=serializers.JSONField(),
    allow_empty=False,
    write_only=True,
  )

  def validate_name(self, value):
    value = value.strip()
    if not value:
      raise serializers.ValidationError("凭据名称不能为空")
    return value

  def create(self, validated_data):
    credentials = validated_data.pop("credentials")
    models = validated_data.pop("models")
    return ModelProvider.objects.create(
      **validated_data,
      credentials_encrypted=encrypt_credentials(credentials),
      credential_fields=sorted(credentials.keys()),
      model_definitions=sorted(set(models)),
    )


class ModelProviderSerializer(serializers.ModelSerializer):
  credentials_configured = serializers.SerializerMethodField()
  models = serializers.ListField(source="model_definitions", read_only=True)

  class Meta:
    model = ModelProvider
    fields = [
      "id", "namespace", "provider", "name", "status",
      "credential_fields", "supported_model_types", "models",
      "credentials_configured", "created_at", "updated_at",
    ]
    read_only_fields = fields

  def get_credentials_configured(self, obj) -> bool:
    return bool(obj.credentials_encrypted)


class ModelInvocationSerializer(serializers.Serializer):
  provider = serializers.CharField(max_length=255)
  credential_name = serializers.CharField(max_length=255, required=False, allow_blank=False)
  provider_id = serializers.UUIDField(required=False)
  model = serializers.CharField(max_length=255)

  def validate(self, attrs):
    if not attrs.get("provider_id") and not attrs.get("credential_name"):
      raise serializers.ValidationError("provider_id 和 credential_name 至少需要提供一个")
    return attrs


class EmbeddingInvocationSerializer(ModelInvocationSerializer):
  texts = serializers.ListField(
    child=serializers.CharField(allow_blank=True), allow_empty=False, max_length=256,
  )
  input_type = serializers.ChoiceField(
    choices=["document", "query"], required=False, default="document",
  )
  dimensions = serializers.IntegerField(required=False, min_value=1)


class TokenCountInvocationSerializer(ModelInvocationSerializer):
  texts = serializers.ListField(
    child=serializers.CharField(allow_blank=True), allow_empty=False, max_length=256,
  )


class RerankInvocationSerializer(ModelInvocationSerializer):
  query = serializers.CharField(allow_blank=False)
  documents = serializers.ListField(
    child=serializers.CharField(allow_blank=True), allow_empty=False, max_length=1000,
  )
  top_n = serializers.IntegerField(required=False, min_value=1)
  score_threshold = serializers.FloatField(required=False, min_value=0, max_value=1)

  def validate(self, attrs):
    attrs = super().validate(attrs)
    top_n = attrs.get("top_n")
    if top_n is not None and top_n > len(attrs["documents"]):
      raise serializers.ValidationError({"top_n": "top_n 不能大于 documents 数量"})
    return attrs
