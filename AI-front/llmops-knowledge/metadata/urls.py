from django.urls import path
from .views import MetadataListView, MetadataDetailView, BuiltInMetadataView


class BuiltInMetadataGetView(BuiltInMetadataView):
  http_method_names = ["get"]


class BuiltInMetadataPostView(BuiltInMetadataView):
  http_method_names = ["post"]


urlpatterns = [
  path("knowledge-bases/<uuid:knowledge_base_id>/metadata/built-in/<str:action>/", BuiltInMetadataPostView.as_view(), name="knowledge-base-metadata-built-in-status"),
  path("knowledge-bases/<uuid:knowledge_base_id>/metadata/built-in/", BuiltInMetadataGetView.as_view(), name="knowledge-base-metadata-built-in"),
  path("knowledge-bases/<uuid:knowledge_base_id>/metadata/<uuid:metadata_id>/", MetadataDetailView.as_view(), name="knowledge-base-metadata-detail"),
  path("knowledge-bases/<uuid:knowledge_base_id>/metadata/", MetadataListView.as_view(), name="knowledge-base-metadata-list"),
]