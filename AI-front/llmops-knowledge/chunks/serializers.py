from pathlib import Path

from django.conf import settings
from rest_framework import serializers


class SegmentImageUploadSerializer(serializers.Serializer):
  file = serializers.FileField(allow_empty_file=False)

  def validate_file(self, uploaded_file):
    extension = Path(uploaded_file.name).suffix.lower()
    formats = {
      ".jpg": ("image/jpeg", (b"\xff\xd8\xff",)),
      ".jpeg": ("image/jpeg", (b"\xff\xd8\xff",)),
      ".png": ("image/png", (b"\x89PNG\r\n\x1a\n",)),
      ".gif": ("image/gif", (b"GIF87a", b"GIF89a")),
    }
    if extension not in formats:
      raise serializers.ValidationError("仅支持 jpg、jpeg、png、gif 图片")
    size_limit = settings.ATTACHMENT_IMAGE_FILE_SIZE_LIMIT
    if uploaded_file.size > size_limit * 1024 * 1024:
      raise serializers.ValidationError(f"每张图片不能超过 {size_limit}MB")
    mime_type, signatures = formats[extension]
    if uploaded_file.content_type not in (mime_type, "application/octet-stream", None, ""):
      raise serializers.ValidationError("图片 MIME 类型与扩展名不一致")
    header = uploaded_file.read(12)
    uploaded_file.seek(0)
    if not header.startswith(signatures):
      raise serializers.ValidationError("图片文件头与扩展名不一致")
    uploaded_file.content_type = mime_type
    return uploaded_file


class AttachmentIDField(serializers.CharField):
  def to_internal_value(self, data):
    if not isinstance(data, str) or not data.strip():
      raise serializers.ValidationError("文件 ID 必须为非空字符串")
    return super().to_internal_value(data)


class SegmentImageFileSerializer(serializers.Serializer):
  id = serializers.CharField(help_text="Dify 文件 ID，用于 attachment_ids")
  name = serializers.CharField()
  size = serializers.IntegerField()
  extension = serializers.CharField()
  mime_type = serializers.CharField(allow_null=True, required=False)
  created_by = serializers.CharField(required=False)
  created_at = serializers.CharField(allow_null=True, required=False)


class SegmentImageUploadResponseSerializer(serializers.Serializer):
  code = serializers.IntegerField()
  message = serializers.CharField()
  data = SegmentImageFileSerializer()


class SegmentAttachmentsSerializer(serializers.Serializer):

  def get_fields(self):
    fields = super().get_fields()
    fields["attachment_ids"] = serializers.ListField(
      child=AttachmentIDField(allow_blank=False, trim_whitespace=False),
      max_length=settings.SINGLE_CHUNK_ATTACHMENT_LIMIT,
      required=False, allow_null=True,
      help_text="图片上传接口返回的 Dify 文件 ID；更新时省略表示保留，空数组表示清空",
    )
    return fields


class SegmentCreateSerializer(SegmentAttachmentsSerializer):
  content = serializers.CharField(help_text="分段文字内容，不可为空")
  answer = serializers.CharField(required=False, allow_blank=True, allow_null=True)
  keywords = serializers.ListField(child=serializers.CharField(), required=False, allow_null=True)


class SegmentsCreateRequestSerializer(serializers.Serializer):
  segments = SegmentCreateSerializer(many=True, allow_empty=False)


class SegmentUpdateSerializer(SegmentCreateSerializer):
  content = serializers.CharField(required=False)
  enabled = serializers.BooleanField(required=False)
  regenerate_child_chunks = serializers.BooleanField(required=False)
  summary = serializers.CharField(required=False, allow_blank=True, allow_null=True)


class SegmentUpdateRequestSerializer(serializers.Serializer):
  segment = SegmentUpdateSerializer()


class SegmentAttachmentResponseSerializer(SegmentImageFileSerializer):
  source_url = serializers.CharField(required=False, allow_blank=True, help_text="Dify 原始图片地址")
  preview_url = serializers.CharField(help_text="本站图片预览相对地址，含 namespace 参数，可直接用作 img src")


class SegmentResponseSerializer(serializers.Serializer):
  id = serializers.CharField()
  content = serializers.CharField(required=False)
  attachments = SegmentAttachmentResponseSerializer(many=True, required=False)


class SegmentListResultSerializer(serializers.Serializer):
  data = SegmentResponseSerializer(many=True)
  doc_form = serializers.CharField(required=False)
  total = serializers.IntegerField(required=False)
  has_more = serializers.BooleanField(required=False)
  limit = serializers.IntegerField(required=False)
  page = serializers.IntegerField(required=False)


class SegmentListResponseSerializer(serializers.Serializer):
  code = serializers.IntegerField()
  message = serializers.CharField()
  data = SegmentListResultSerializer()


class SegmentDetailResultSerializer(serializers.Serializer):
  data = SegmentResponseSerializer()
  doc_form = serializers.CharField(required=False)


class SegmentDetailResponseSerializer(serializers.Serializer):
  code = serializers.IntegerField()
  message = serializers.CharField()
  data = SegmentDetailResultSerializer()


class SegmentUUIDField(serializers.UUIDField):
  def to_internal_value(self, data):
    if not isinstance(data, str):
      raise serializers.ValidationError("分段 ID 必须为 UUID 字符串")
    return super().to_internal_value(data)


class SegmentsBatchRequestSerializer(serializers.Serializer):
  action = serializers.ChoiceField(choices=["enable", "disable", "delete"])
  segment_ids = serializers.ListField(
    child=SegmentUUIDField(), allow_empty=False, max_length=100,
    help_text="当前文档的 Dify 分段 ID，一次最多 100 个，重复 ID 自动去重",
  )

  def validate_segment_ids(self, values):
    return list(dict.fromkeys(str(value) for value in values))


class SegmentBatchItemResponseSerializer(serializers.Serializer):
  segment_id = serializers.CharField()
  success = serializers.BooleanField()
  unchanged = serializers.BooleanField(required=False, help_text="已处于目标启用状态，无需修改")
  code = serializers.CharField(required=False)
  message = serializers.CharField(required=False)
  backend_status = serializers.IntegerField(required=False)
  backend_code = serializers.CharField(required=False)


class SegmentsBatchResultSerializer(serializers.Serializer):
  action = serializers.ChoiceField(choices=["enable", "disable", "delete"])
  total = serializers.IntegerField()
  success_count = serializers.IntegerField()
  failure_count = serializers.IntegerField()
  results = SegmentBatchItemResponseSerializer(many=True)


class SegmentsBatchResponseSerializer(serializers.Serializer):
  code = serializers.JSONField(help_text="全部成功为 0，部分失败为 BATCH_PARTIALLY_FAILED，全部失败为 BATCH_FAILED")
  message = serializers.CharField()
  data = SegmentsBatchResultSerializer()
