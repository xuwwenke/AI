import uuid

from django.db import models


class ModelProvider(models.Model):
  class Status(models.TextChoices):
    ACTIVE = "active", "可用"
    DISABLED = "disabled", "已禁用"

  id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
  namespace = models.CharField(max_length=128, db_index=True)
  provider = models.CharField(max_length=255)
  name = models.CharField(max_length=255)
  credentials_encrypted = models.BinaryField()
  credential_fields = models.JSONField(default=list)
  supported_model_types = models.JSONField(default=list)
  model_definitions = models.JSONField(default=list)
  status = models.CharField(max_length=16, choices=Status.choices, default=Status.ACTIVE)
  created_at = models.DateTimeField(auto_now_add=True)
  updated_at = models.DateTimeField(auto_now=True)

  class Meta:
    db_table = "model_providers"
    constraints = [
      models.UniqueConstraint(
        fields=["namespace", "provider", "name"],
        name="unique_model_provider_credential_name",
      ),
    ]
    ordering = ["-created_at"]

  def __str__(self):
    return f"{self.namespace}:{self.provider}:{self.name}"
