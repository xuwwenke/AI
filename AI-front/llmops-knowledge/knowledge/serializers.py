
from rest_framework import serializers
from .models import KnowledgeBase, KnowledgeDocument

# ModelSerializer会根据 KnowledgeBase 模型生成对应的序列化和校验逻辑
class KnowledgeBaseSerializer(serializers.ModelSerializer):
  embedding_model = serializers.CharField(required=False, write_only=True, allow_blank=True, allow_null=True, default=None)
  embedding_model_provider = serializers.CharField(required=False, write_only=True, allow_blank=True, allow_null=True, default=None)
  external_knowledge_api_id = serializers.CharField(required=False, write_only=True, allow_blank=True, allow_null=True, default=None)
  external_knowledge_id = serializers.CharField(required=False, write_only=True, allow_blank=True, allow_null=True, default=None)
  indexing_technique = serializers.ChoiceField(
    choices=[("high_quality", "高质量"), ("economy", "经济型")],
    required=False, write_only=True, default=None, allow_null=True,
  )
  permission = serializers.ChoiceField(
    choices=[("only_me", "仅自己"), ("all_team_members", "所有团队成员"), ("partial_members", "指定成员")],
    required=False, write_only=True, default=None, allow_null=True,
  )
  provider = serializers.ChoiceField(
    choices=[("vendor", "内部知识库"), ("external", "外部知识库")],
    required=False, write_only=True, default=None, allow_null=True,
  )
  retrieval_model = serializers.JSONField(required=False, write_only=True, default=None, allow_null=True)
  summary_index_setting = serializers.JSONField(required=False, write_only=True, default=None, allow_null=True)

  class Meta:
    model = KnowledgeBase
    fields = [
      "id",
      "name",
      "description",
      "creator_username",
      "creator_email",
      "namespace",
      "backend_type",
      "backend_dataset_id",
      "status",
      "created_at",
      "updated_at",
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
    read_only_fields = [
      "id",
      "namespace",
      "backend_dataset_id",
      "status",
      "created_at",
      "updated_at",
    ]

  # 这些是仅用于调用后端（如 Dify）创建知识库接口的参数，
  # 不会保存到本地 KnowledgeBase 模型，因此在 create 时需要从 validated_data 中剔除。
  DIFY_CREATE_PARAM_FIELDS = [
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

  def get_fields(self):
    fields = super().get_fields()
    # 创建者信息是创建时的快照，更新时只读。
    if self.instance is not None:
      fields["creator_username"].read_only = True
      fields["creator_email"].read_only = True
    return fields

  def to_internal_value(self, data):
    # Dify 可选参数允许用户传空字符串表示"使用后端默认值"。
    # ChoiceField / JSONField 不接受空字符串，但已设置 allow_null=True，
    # 因此在字段校验前统一把空字符串转为 None。
    normalized = dict(data)
    for field in self.DIFY_CREATE_PARAM_FIELDS:
      if field in normalized and normalized[field] == "":
        normalized[field] = None
    return super().to_internal_value(normalized)

  def create(self, validated_data):
    for field in self.DIFY_CREATE_PARAM_FIELDS:
      validated_data.pop(field, None)
    return super().create(validated_data)


class DatasetPartialMemberSerializer(serializers.Serializer):
  user_id = serializers.CharField()


class DatasetExternalRetrievalModelSerializer(serializers.Serializer):
  score_threshold = serializers.FloatField(required=False)
  score_threshold_enabled = serializers.BooleanField(required=False)
  top_k = serializers.IntegerField(required=False, min_value=1)


class DatasetRerankingModelSerializer(serializers.Serializer):
  reranking_model_name = serializers.CharField(required=False, allow_blank=True)
  reranking_provider_name = serializers.CharField(required=False, allow_blank=True)


class DatasetVectorSettingSerializer(serializers.Serializer):
  embedding_model_name = serializers.CharField(required=False, allow_blank=True)
  embedding_provider_name = serializers.CharField(required=False, allow_blank=True)
  vector_weight = serializers.FloatField(required=False)


class DatasetKeywordSettingSerializer(serializers.Serializer):
  keyword_weight = serializers.FloatField(required=False)


class DatasetWeightsSerializer(serializers.Serializer):
  keyword_setting = DatasetKeywordSettingSerializer(required=False)
  vector_setting = DatasetVectorSettingSerializer(required=False)
  weight_type = serializers.ChoiceField(
    choices=["semantic_first", "keyword_first", "customized"], required=False,
  )


class DatasetMetadataFilteringConditionsSerializer(serializers.Serializer):
  conditions = serializers.ListField(
    child=serializers.DictField(), required=False, allow_null=True,
  )
  logical_operator = serializers.ChoiceField(
    choices=["and", "or"], required=False, allow_null=True,
  )


class DatasetRetrievalModelSerializer(serializers.Serializer):
  reranking_enable = serializers.BooleanField()
  score_threshold_enabled = serializers.BooleanField()
  search_method = serializers.ChoiceField(
    choices=["keyword_search", "semantic_search", "full_text_search", "hybrid_search"],
  )
  top_k = serializers.IntegerField(min_value=1)
  metadata_filtering_conditions = DatasetMetadataFilteringConditionsSerializer(
    required=False, allow_null=True,
  )
  reranking_mode = serializers.ChoiceField(
    choices=["reranking_model", "weighted_score"], required=False, allow_null=True,
  )
  reranking_model = DatasetRerankingModelSerializer(required=False)
  score_threshold = serializers.FloatField(required=False, allow_null=True)
  weights = DatasetWeightsSerializer(required=False, allow_null=True)

  def validate(self, attrs):
    if attrs.get("reranking_enable") and not attrs.get("reranking_mode"):
      raise serializers.ValidationError({"reranking_mode": "启用重排序时必须指定重排序模式"})
    return attrs


class KnowledgeBaseUpdateSerializer(serializers.Serializer):
  """知识库 PATCH 参数；只转发显式提交的字段，不注入默认值。"""
  name = serializers.CharField(required=False, min_length=1, max_length=40)
  description = serializers.CharField(required=False, allow_blank=True, max_length=400)
  embedding_model = serializers.CharField(required=False, allow_blank=True)
  embedding_model_provider = serializers.CharField(required=False, allow_blank=True)
  external_knowledge_api_id = serializers.CharField(required=False, allow_blank=True)
  external_knowledge_id = serializers.CharField(required=False, allow_blank=True)
  external_retrieval_model = DatasetExternalRetrievalModelSerializer(required=False)
  indexing_technique = serializers.ChoiceField(
    choices=["high_quality", "economy"], required=False, allow_null=True,
  )
  permission = serializers.ChoiceField(
    choices=["only_me", "all_team_members", "partial_members"], required=False,
  )
  partial_member_list = DatasetPartialMemberSerializer(many=True, required=False)
  retrieval_model = DatasetRetrievalModelSerializer(required=False)


class KnowledgeBaseUpdateResponseSerializer(serializers.Serializer):
  code = serializers.IntegerField()
  message = serializers.CharField()
  data = serializers.DictField(
    help_text="合并平台知识库字段与 Dify PATCH 完整响应；id 为平台 ID，backend_dataset_id 为 Dify ID",
  )


class DifyLocalizedLabelSerializer(serializers.Serializer):
  en_US = serializers.CharField()
  zh_Hans = serializers.CharField()


class DifyAvailableModelSerializer(serializers.Serializer):
  deprecated = serializers.BooleanField()
  features = serializers.ListField(child=serializers.CharField(), allow_null=True)
  fetch_from = serializers.CharField()
  has_invalid_load_balancing_configs = serializers.BooleanField()
  label = DifyLocalizedLabelSerializer()
  load_balancing_enabled = serializers.BooleanField()
  model = serializers.CharField(help_text="创建或更新知识库时使用的模型标识")
  model_properties = serializers.DictField()
  model_type = serializers.CharField()
  status = serializers.CharField()


class DifyModelProviderSerializer(serializers.Serializer):
  icon_small = DifyLocalizedLabelSerializer()
  icon_small_dark = DifyLocalizedLabelSerializer(allow_null=True)
  label = DifyLocalizedLabelSerializer()
  models = DifyAvailableModelSerializer(many=True)
  provider = serializers.CharField(help_text="Dify 供应商标识，可用于 embedding_model_provider")
  status = serializers.CharField()
  tenant_id = serializers.CharField()


class DifyModelsResponseSerializer(serializers.Serializer):
  code = serializers.IntegerField()
  message = serializers.CharField()
  data = DifyModelProviderSerializer(many=True)


class KnowledgeDocumentSerializer(serializers.ModelSerializer):
  class Meta:
    model = KnowledgeDocument
    fields = [
      "id",
      "knowledge_base",
      "file_name",
      "file_size",
      "content_type",
      "backend_document_id",
      "backend_batch_id",
      "status",
      "error_message",
      "created_at",
      "updated_at",
    ]
    read_only_fields = fields


class RerankingModelSerializer(serializers.Serializer):
  reranking_provider_name = serializers.CharField(required=False, allow_blank=True, default="")
  reranking_model_name = serializers.CharField(required=False, allow_blank=True, default="")


class WeightsSerializer(serializers.Serializer):
  vector_setting = serializers.FloatField(required=False, min_value=0, max_value=1)
  keyword_setting = serializers.FloatField(required=False, min_value=0, max_value=1)


class RetrievalModelSerializer(serializers.Serializer):
  SEARCH_METHOD_CHOICES = [
    ("semantic_search", "向量检索"),
    ("full_text_search", "全文检索"),
    ("hybrid_search", "混合检索"),
  ]
  RERANKING_MODE_CHOICES = [
    ("weighted_score", "加权评分"),
    ("reranking_model", "重排模型"),
  ]

  search_method = serializers.ChoiceField(choices=SEARCH_METHOD_CHOICES, default="semantic_search")
  reranking_enable = serializers.BooleanField(required=False, default=False)
  reranking_mode = serializers.ChoiceField(choices=RERANKING_MODE_CHOICES, required=False, allow_null=True, default=None)
  reranking_model = RerankingModelSerializer(required=False, default=None)
  top_k = serializers.IntegerField(required=False, min_value=1, max_value=100, default=3)
  score_threshold_enabled = serializers.BooleanField(required=False, default=False)
  score_threshold = serializers.FloatField(required=False, min_value=0, max_value=1, allow_null=True, default=None)
  weights = WeightsSerializer(required=False, default=None)

  def validate(self, attrs):
    search_method = attrs.get("search_method")
    weights = attrs.get("weights")
    if search_method == "hybrid_search" and weights is not None:
      vector = weights.get("vector_setting")
      keyword = weights.get("keyword_setting")
      if vector is not None and keyword is not None:
        total = vector + keyword
        if abs(total - 1.0) > 0.001:
          raise serializers.ValidationError("hybrid_search 时 weights 中 vector_setting 与 keyword_setting 之和必须为 1")
    return attrs


class KnowledgeBaseRetrieveSerializer(serializers.Serializer):
  query = serializers.CharField(max_length=2000, trim_whitespace=True)
  retrieval_model = RetrievalModelSerializer(required=False, default=None)
