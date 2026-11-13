from django.urls import path
from .views import DocumentSegmentsListView, SegmentDetailView, SegmentImageUploadView, SegmentImagePreviewView, SegmentsBatchView

urlpatterns = [
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<uuid:document_id>/segments/images/upload/", SegmentImageUploadView.as_view(), name="segment-image-upload"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<uuid:document_id>/segments/<str:segment_id>/images/<str:file_id>/preview/", SegmentImagePreviewView.as_view(), name="segment-image-preview"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<uuid:document_id>/segments/", DocumentSegmentsListView.as_view(), name="document-segments-list"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<uuid:document_id>/segments/batch/", SegmentsBatchView.as_view(), name="segments-batch"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<uuid:document_id>/segments/<str:segment_id>/", SegmentDetailView.as_view(), name="segment-detail"),
]
