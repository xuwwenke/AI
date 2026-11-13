
import json
from rest_framework import serializers
from knowledge.models import KnowledgeDocument


class DocumentSerializer(serializers.ModelSerializer):
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


class DocumentIndexingStatusItemSerializer(serializers.Serializer):
  """Dify 文档索引状态条目"""
  cleaning_completed_at = serializers.FloatField(allow_null=True)
  completed_at = serializers.FloatField(allow_null=True)
  completed_segments = serializers.IntegerField()
  error = serializers.CharField(allow_null=True, allow_blank=True)
  id = serializers.CharField()
  indexing_status = serializers.ChoiceField(choices=[
    "waiting", "parsing", "cleaning", "splitting", "indexing", "completed", "error",
  ])
  parsing_completed_at = serializers.FloatField(allow_null=True)
  paused_at = serializers.FloatField(allow_null=True)
  processing_started_at = serializers.FloatField(allow_null=True)
  splitting_completed_at = serializers.FloatField(allow_null=True)
  stopped_at = serializers.FloatField(allow_null=True)
  total_segments = serializers.IntegerField()


class DocumentIndexingStatusSerializer(serializers.Serializer):
  data = DocumentIndexingStatusItemSerializer(many=True)


# ============ Dify 文档创建可选参数序列化器 ============

class PreProcessingRuleSerializer(serializers.Serializer):
  id = serializers.ChoiceField(
    choices=["remove_stopwords", "remove_extra_spaces", "remove_urls_emails"],
    required=False,
  )
  enabled = serializers.BooleanField(required=False)


class SegmentationSerializer(serializers.Serializer):
  separator = serializers.CharField(required=False, allow_blank=True, default="")
  max_tokens = serializers.IntegerField(required=False, min_value=1)
  chunk_overlap = serializers.IntegerField(required=False, min_value=0, default=0)


class ProcessRuleRulesSerializer(serializers.Serializer):
  pre_processing_rules = PreProcessingRuleSerializer(many=True, required=False)
  segmentation = SegmentationSerializer(required=False)


class ProcessRuleSerializer(serializers.Serializer):
  mode = serializers.ChoiceField(
    choices=["automatic", "custom", "hierarchical"],
    required=True,
  )
  rules = ProcessRuleRulesSerializer(required=False)


class RerankingModelSerializer(serializers.Serializer):
  reranking_provider_name = serializers.CharField(required=False, allow_blank=True)
  reranking_model_name = serializers.CharField(required=False, allow_blank=True)


class VectorSettingSerializer(serializers.Serializer):
  vector_weight = serializers.FloatField(required=False)
  embedding_provider_name = serializers.CharField(required=False, allow_blank=True)
  embedding_model_name = serializers.CharField(required=False, allow_blank=True)


class KeywordSettingSerializer(serializers.Serializer):
  keyword_weight = serializers.FloatField(required=False)


class WeightsSerializer(serializers.Serializer):
  weight_type = serializers.ChoiceField(
    choices=["semantic_first", "keyword_first", "customized"],
    required=False,
  )
  vector_setting = VectorSettingSerializer(required=False)
  keyword_setting = KeywordSettingSerializer(required=False)


class MetadataConditionSerializer(serializers.Serializer):
  name = serializers.CharField(required=True)
  comparison_operator = serializers.CharField(required=True)
  value = serializers.CharField(required=False, allow_blank=True)


class MetadataFilteringConditionsSerializer(serializers.Serializer):
  logical_operator = serializers.ChoiceField(
    choices=["and", "or"],
    required=False,
    allow_null=True,
    default="and",
  )
  conditions = MetadataConditionSerializer(many=True, required=False, allow_null=True)


class RetrievalModelSerializer(serializers.Serializer):
  search_method = serializers.ChoiceField(
    choices=["keyword_search", "semantic_search", "full_text_search", "hybrid_search"],
    required=True,
  )
  top_k = serializers.IntegerField(required=True, min_value=1)
  score_threshold_enabled = serializers.BooleanField(required=True)
  score_threshold = serializers.FloatField(required=False, allow_null=True)
  reranking_enable = serializers.BooleanField(required=True)
  reranking_mode = serializers.ChoiceField(
    choices=["reranking_model", "weighted_score"],
    required=False,
    allow_null=True,
  )
  reranking_model = RerankingModelSerializer(required=False, allow_null=True)
  weights = WeightsSerializer(required=False, allow_null=True)
  metadata_filtering_conditions = MetadataFilteringConditionsSerializer(
    required=False, allow_null=True,
  )


class DocumentCreateOptionsSerializer(serializers.Serializer):
  """文档创建（文本/文件）共用的可选参数"""
  indexing_technique = serializers.ChoiceField(
    choices=["high_quality", "economy"],
    required=False,
    allow_null=True,
    default=None,
  )
  embedding_model = serializers.CharField(required=False, allow_blank=True, allow_null=True, default=None)
  embedding_model_provider = serializers.CharField(required=False, allow_blank=True, allow_null=True, default=None)
  doc_form = serializers.ChoiceField(
    choices=["text_model", "hierarchical_model", "qa_model"],
    required=False,
    allow_null=True,
    default=None,
  )
  doc_language = serializers.CharField(required=False, allow_blank=True, allow_null=True, default=None)
  process_rule = ProcessRuleSerializer(required=False, allow_null=True, default=None)
  retrieval_model = RetrievalModelSerializer(required=False, allow_null=True, default=None)


class TextDocumentCreateSerializer(DocumentCreateOptionsSerializer):
  """从文本创建文档的请求体"""
  name = serializers.CharField(required=True, allow_blank=False)
  text = serializers.CharField(required=True, allow_blank=False)
  original_document_id = serializers.CharField(required=False, allow_blank=True, allow_null=True, default=None)


class DocumentUpdateOptionsSerializer(serializers.Serializer):
  """通过文件更新文档的可选参数（不包含 indexing_technique、name、text）"""
  embedding_model = serializers.CharField(required=False, allow_blank=True, allow_null=True, default=None)
  embedding_model_provider = serializers.CharField(required=False, allow_blank=True, allow_null=True, default=None)
  doc_form = serializers.ChoiceField(
    choices=["text_model", "hierarchical_model", "qa_model"],
    required=False,
    allow_null=True,
    default=None,
  )
  doc_language = serializers.CharField(required=False, allow_blank=True, allow_null=True, default=None)
  process_rule = ProcessRuleSerializer(required=False, allow_null=True, default=None)
  retrieval_model = RetrievalModelSerializer(required=False, allow_null=True, default=None)


def parse_json_form_field(value):
  """
  处理 multipart/form-data 中以 JSON 字符串形式提交的复杂字段（process_rule、retrieval_model）。
  - 如果是 dict，直接返回
  - 如果是字符串，尝试 JSON 解析；解析失败返回原值（交给序列化器校验）
  - 如果是 None 或空字符串，返回 None
  """
  if value is None or value == "":
    return None
  if isinstance(value, dict):
    return value
  if isinstance(value, str):
    try:
      return json.loads(value)
    except (ValueError, TypeError):
      return value
  return value
