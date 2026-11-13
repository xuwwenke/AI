import os
from unittest.mock import Mock, patch

from requests import HTTPError, Response, Timeout
from django.test import TestCase
from django.urls import reverse
from rest_framework.test import APIClient

from knowledge.models import KnowledgeBase, KnowledgeDocument
from knowledge.services.dify_client import DifyClient


class DifyDocumentIndexingStatusTests(TestCase):
  @patch("knowledge.services.dify_client.requests.get")
  def test_client_calls_dify_indexing_status_endpoint(self, get):
    response = Mock()
    response.json.return_value = {"data": []}
    get.return_value = response

    with patch.dict(os.environ, {
      "DIFY_BASE_URL": "https://api.dify.ai/",
      "DIFY_API_KEY": "secret",
    }):
      result = DifyClient().get_document_indexing_status("dataset-1", "batch-1")

    self.assertEqual(result, {"data": []})
    get.assert_called_once_with(
      "https://api.dify.ai/v1/datasets/dataset-1/documents/batch-1/indexing-status",
      headers={"Authorization": "Bearer secret"},
      timeout=30,
    )
    response.raise_for_status.assert_called_once_with()


class DocumentIndexingStatusViewTests(TestCase):
  def setUp(self):
    self.client = APIClient()
    self.knowledge_base = KnowledgeBase.objects.create(
      name="test",
      namespace="project-a",
      backend_type="dify",
      backend_dataset_id="dataset-1",
    )

  @patch("documents.views.get_backend")
  def test_returns_indexing_progress(self, get_backend):
    progress = {
      "data": [{
        "cleaning_completed_at": 1741267200,
        "completed_at": 1741267200,
        "completed_segments": 5,
        "error": None,
        "id": "document-1",
        "indexing_status": "completed",
        "parsing_completed_at": 1741267200,
        "paused_at": None,
        "processing_started_at": 1741267200,
        "splitting_completed_at": 1741267200,
        "stopped_at": None,
        "total_segments": 5,
      }],
    }
    backend = get_backend.return_value
    backend.get_document_indexing_status.return_value = progress

    response = self.client.get(
      reverse("document-indexing-status", kwargs={
        "knowledge_base_id": self.knowledge_base.id,
        "batch": "batch-1",
      }),
      HTTP_NAMESPACE="project-a",
    )

    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.data, {"code": 0, "message": "success", "data": progress})
    backend.get_document_indexing_status.assert_called_once_with(
      dataset_id="dataset-1",
      batch="batch-1",
    )

  @patch("documents.views.get_backend")
  def test_backend_failure_returns_bad_gateway(self, get_backend):
    get_backend.return_value.get_document_indexing_status.side_effect = RuntimeError("unavailable")

    response = self.client.get(
      reverse("document-indexing-status", kwargs={
        "knowledge_base_id": self.knowledge_base.id,
        "batch": "batch-1",
      }),
      HTTP_NAMESPACE="project-a",
    )

    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.data["code"], "BACKEND_GET_INDEXING_STATUS_FAILED")


class DocumentsStatusUpdateViewTests(TestCase):
  def setUp(self):
    self.client = APIClient()
    self.knowledge_base = KnowledgeBase.objects.create(
      name="test", namespace="project-a", backend_dataset_id="dataset-1",
    )
    self.document = KnowledgeDocument.objects.create(
      knowledge_base=self.knowledge_base, file_name="test.txt",
      backend_document_id="backend-document-1",
    )
    self.url = reverse("documents-status-update", kwargs={
      "knowledge_base_id": self.knowledge_base.id, "action": "enable",
    })

  def enable_document(self):
    return self.client.patch(
      self.url, {"document_ids": [str(self.document.id)]},
      format="json", HTTP_NAMESPACE="project-a",
    )

  @patch("knowledge.services.dify_client.requests.patch")
  def test_enables_document_using_backend_id(self, patch_request):
    backend_response = Response()
    backend_response.status_code = 200
    backend_response._content = b'{"result":"success"}'
    patch_request.return_value = backend_response
    with patch.dict(os.environ, {
      "DIFY_BASE_URL": "https://api.dify.ai/", "DIFY_API_KEY": "secret",
    }):
      response = self.enable_document()

    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.data["data"], {"result": "success"})
    patch_request.assert_called_once_with(
      "https://api.dify.ai/v1/datasets/dataset-1/documents/status/enable",
      headers={"Authorization": "Bearer secret", "Content-Type": "application/json"},
      json={"document_ids": ["backend-document-1"]}, timeout=30,
    )

  @patch("knowledge.services.dify_client.requests.patch")
  def test_preserves_backend_bad_request_reason(self, patch_request):
    backend_response = Response()
    backend_response.status_code = 400
    backend_response._content = (
      b'{"code":"invalid_action","message":"Document is being indexed, please try again later"}'
    )
    patch_request.return_value = backend_response
    response = self.enable_document()

    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.data, {
      "code": "BACKEND_UPDATE_STATUS_FAILED",
      "message": "批量更新文档状态失败",
      "detail": "Document is being indexed, please try again later",
      "backend_code": "invalid_action",
      "backend_status": 400,
    })

  @patch("documents.views.get_backend")
  def test_handles_non_json_backend_errors(self, get_backend):
    for payload in (b'upstream unavailable', b'[]', b'{}', b''):
      with self.subTest(payload=payload):
        backend_response = Response()
        backend_response.status_code = 503
        backend_response._content = payload
        get_backend.return_value.update_documents_status.side_effect = HTTPError(
          "backend unavailable", response=backend_response,
        )
        response = self.enable_document()

        self.assertEqual(response.status_code, 502)
        self.assertEqual(response.data["backend_status"], 503)
        self.assertEqual(response.data["detail"], payload.decode() or "backend unavailable")

  @patch("documents.views.get_backend")
  def test_keeps_backend_authentication_failures_as_gateway_errors(self, get_backend):
    for backend_status in (401, 403):
      with self.subTest(backend_status=backend_status):
        backend_response = Response()
        backend_response.status_code = backend_status
        backend_response._content = b'{"code":"unauthorized","message":"Invalid API key"}'
        get_backend.return_value.update_documents_status.side_effect = HTTPError(
          response=backend_response,
        )
        response = self.enable_document()
        self.assertEqual(response.status_code, 502)
        self.assertEqual(response.data["backend_status"], backend_status)

  @patch("documents.views.get_backend")
  def test_keeps_transport_failures_as_gateway_errors(self, get_backend):
    for error in (Timeout("timed out"), HTTPError("no response")):
      with self.subTest(error=error):
        get_backend.return_value.update_documents_status.side_effect = error
        response = self.enable_document()

        self.assertEqual(response.status_code, 502)
        self.assertEqual(response.data["detail"], str(error))
        self.assertNotIn("backend_status", response.data)

  @patch("documents.views.get_backend")
  def test_rejects_document_from_another_namespace(self, get_backend):
    other_base = KnowledgeBase.objects.create(name="other", namespace="project-b")
    other_document = KnowledgeDocument.objects.create(
      knowledge_base=other_base, file_name="other.txt", backend_document_id="other-backend-id",
    )
    response = self.client.patch(
      self.url, {"document_ids": [str(other_document.id)]},
      format="json", HTTP_NAMESPACE="project-a",
    )

    self.assertEqual(response.status_code, 400)
    get_backend.return_value.update_documents_status.assert_not_called()
