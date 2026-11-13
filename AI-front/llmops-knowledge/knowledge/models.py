from django.db import models

# Create your models here.
import uuid

class KnowledgeBase(models.Model):
  class Status(models.TextChoices):
    PENDING = "pending", "待创建"
    READY = "ready", "可用"
    FAILED = "failed", "失败"

  # 平台内部知识库 ID
  id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
  name = models.CharField(max_length=128)
  description = models.TextField(blank=True, default="")
  # 项目隔离标识，用于区分不同项目的知识库
  namespace = models.CharField(max_length=128, blank=True, default="", db_index=True)
  # 创建时由 LLMOps 平台传入的登录用户信息快照
  creator_username = models.CharField(max_length=150, blank=True, default="", verbose_name="创建者用户名")
  creator_email = models.EmailField(max_length=254, blank=True, default="", verbose_name="创建者邮箱")
  # 暂时使用 Dify 作为底层知识库服务
  backend_type = models.CharField(max_length=32, default="dify")

  # Dify 创建成功后返回的 Dataset ID
  backend_dataset_id = models.CharField(max_length=128, blank=True, default="")

  status = models.CharField(max_length=16, choices=Status.choices, default=Status.PENDING)
  created_at = models.DateTimeField(auto_now_add=True)
  updated_at = models.DateTimeField(auto_now=True)
  error_message = models.TextField(blank=True, default="", verbose_name="错误信息")

  class Meta:
    db_table = "knowledge_base"
    ordering = ["-created_at"]

  def __str__(self):
    return self.name


class KnowledgeDocument(models.Model):
  STATUS_CHOICES = [
    ("pending", "等待处理"),
    ("indexing", "索引中"),
    ("ready", "已完成"),
    ("failed", "失败"),
  ]

  id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
  # 外键关联知识库，删除知识库时级联删除文档，文档状态为失败
  knowledge_base = models.ForeignKey("knowledge.KnowledgeBase", on_delete=models.CASCADE, related_name="documents")
  file_name = models.CharField(max_length=255)
  file_size = models.BigIntegerField(default=0)
  content_type = models.CharField(max_length=100, blank=True, default="")

  # Dify 返回的文档 ID 和索引批次 ID
  backend_document_id = models.CharField(max_length=100, blank=True, default="")
  backend_batch_id = models.CharField(max_length=100, blank=True, default="")

  status = models.CharField(max_length=20, choices=STATUS_CHOICES, default="pending")
  error_message = models.TextField(blank=True, default="")

  created_at = models.DateTimeField(auto_now_add=True)
  updated_at = models.DateTimeField(auto_now=True)

  class Meta:
    db_table = "knowledge_document"
    ordering = ["-created_at"]

  def __str__(self):
    return self.file_name