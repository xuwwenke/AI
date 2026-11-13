from django.urls import path
from .views import (
  DocumentListView, DocumentDetailView, DocumentFromTextView,
  DocumentDownloadView, DocumentsStatusUpdateView,
  DocumentMetadataUpdateView, DocumentIndexingStatusView,
)

urlpatterns = [
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/", DocumentListView.as_view(), name="document-list-create"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/text/", DocumentFromTextView.as_view(), name="document-create-by-text"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/metadata/", DocumentMetadataUpdateView.as_view(), name="documents-metadata-update"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/status/<str:action>/", DocumentsStatusUpdateView.as_view(), name="documents-status-update"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<str:batch>/indexing-status/", DocumentIndexingStatusView.as_view(), name="document-indexing-status"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<uuid:document_id>/download/", DocumentDownloadView.as_view(), name="document-download"),
  path("knowledge-bases/<uuid:knowledge_base_id>/documents/<uuid:document_id>/", DocumentDetailView.as_view(), name="document-detail"),
]
