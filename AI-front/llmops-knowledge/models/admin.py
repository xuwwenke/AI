from django.contrib import admin

from .models import ModelProvider


@admin.register(ModelProvider)
class ModelProviderAdmin(admin.ModelAdmin):
  list_display = ("name", "provider", "namespace", "status", "created_at")
  list_filter = ("status", "provider")
  search_fields = ("name", "provider", "namespace")
  readonly_fields = ("credentials_encrypted", "credential_fields", "created_at", "updated_at")
