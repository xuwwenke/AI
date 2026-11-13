from django.urls import path

from .views import (
  KnowledgeBaseListCreateView,
  KnowledgeBaseDetailView,
  KnowledgeBaseRetrieveView,
  TagCreateView,
  TagBindingView,
  TagUnbindingView,
  DatasetTagsView,
  ModelsView,
  LocalModelsView,
)

urlpatterns = [
  path("knowledge-bases/tags/", TagCreateView.as_view(), name="tag-create"),
  path("knowledge-bases/<uuid:knowledge_base_id>/tags/", DatasetTagsView.as_view(), name="dataset-tags"),
  path("knowledge-bases/<uuid:knowledge_base_id>/tags/binding/", TagBindingView.as_view(), name="tag-binding"),
  path("knowledge-bases/<uuid:knowledge_base_id>/tags/unbinding/", TagUnbindingView.as_view(), name="tag-unbinding"),
  path("models/<str:model_type>/", ModelsView.as_view(), name="models"),
  path("local-models/<str:model_type>/", LocalModelsView.as_view(), name="local-models"),
  path("knowledge-bases/", KnowledgeBaseListCreateView.as_view(), name="knowledge-base-list-create"),
  path("knowledge-bases/<uuid:pk>/", KnowledgeBaseDetailView.as_view(), name="knowledge-base-detail"),
  path("knowledge-bases/<uuid:knowledge_base_id>/retrieve/", KnowledgeBaseRetrieveView.as_view(), name="knowledge-base-retrieve"),
]
