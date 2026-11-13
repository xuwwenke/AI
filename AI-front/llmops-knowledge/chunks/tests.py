import base64
import os
from unittest.mock import Mock, patch

from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import SimpleTestCase, TestCase, override_settings
from django.urls import reverse
from requests import HTTPError, Response, Timeout
from rest_framework.test import APIClient

from knowledge.models import KnowledgeBase, KnowledgeDocument
from knowledge.services.base import SegmentAttachmentError
from knowledge.services.dify_client import DifyClient


PNG = base64.b64decode(
  "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII="
)


def image(name="example.png", content=PNG, content_type="image/png"):
  return SimpleUploadedFile(name, content, content_type=content_type)


class SegmentImageViewTests(TestCase):
  def setUp(self):
    self.client = APIClient()
    self.kb = KnowledgeBase.objects.create(
      name="test", namespace="project-a", backend_dataset_id="dataset-1",
    )
    self.doc = KnowledgeDocument.objects.create(
      knowledge_base=self.kb, file_name="test.md", backend_document_id="document-1",
    )
    self.kwargs = {"knowledge_base_id": self.kb.id, "document_id": self.doc.id}
    self.upload_url = reverse("segment-image-upload", kwargs=self.kwargs)
    self.segments_url = reverse("document-segments-list", kwargs=self.kwargs)
    self.detail_url = reverse("segment-detail", kwargs={**self.kwargs, "segment_id": "segment-1"})
    patcher = patch("chunks.views.get_backend")
    self.backend = patcher.start().return_value
    self.addCleanup(patcher.stop)
    self.backend.upload_segment_image.return_value = {
      "id": "file-1", "name": "example.png", "mime_type": "image/png",
    }
    self.backend.add_segments.return_value = {"data": [{"id": "segment-1"}]}
    self.backend.update_segment.return_value = {"data": {"id": "segment-1"}}

  def upload(self, data=None, namespace="project-a"):
    headers = {"HTTP_NAMESPACE": namespace} if namespace is not None else {}
    return self.client.post(
      self.upload_url, data if data is not None else {"file": image()},
      format="multipart", **headers,
    )

  def test_upload_then_create_and_update_with_returned_id(self):
    def capture_upload(uploaded):
      # 文件头校验后必须复位，保证发送给 Dify 的图片完整。
      self.assertEqual(uploaded.read(), PNG)
      self.assertEqual(uploaded.content_type, "image/png")
      return self.backend.upload_segment_image.return_value

    self.backend.upload_segment_image.side_effect = capture_upload
    response = self.upload()
    self.assertEqual(response.status_code, 201)
    attachment_id = response.data["data"]["id"]
    segments = [{"content": "图片说明", "attachment_ids": [attachment_id]}]
    response = self.client.post(self.segments_url, {"segments": segments}, format="json", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 200)
    self.backend.add_segments.assert_called_once_with(dataset_id="dataset-1", document_id="document-1", segments=segments)
    for attachments in ([attachment_id], [], None):
      with self.subTest(attachments=attachments):
        segment = {"attachment_ids": attachments}
        response = self.client.post(self.detail_url, {"segment": segment}, format="json", HTTP_NAMESPACE="project-a")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(self.backend.update_segment.call_args.kwargs["segment"], segment)

  def test_namespace_and_document_ownership_are_checked_before_upload(self):
    for namespace, expected in ((None, 400), ("other-project", 404)):
      with self.subTest(namespace=namespace):
        self.assertEqual(self.upload(namespace=namespace).status_code, expected)
    other_kb = KnowledgeBase.objects.create(name="other", namespace="project-a")
    self.upload_url = reverse("segment-image-upload", kwargs={**self.kwargs, "knowledge_base_id": other_kb.id})
    self.assertEqual(self.upload().status_code, 404)
    self.backend.upload_segment_image.assert_not_called()

  def test_rejects_unsynced_dataset_or_document(self):
    self.kb.backend_dataset_id = ""
    self.kb.save()
    self.assertEqual(self.upload().data["code"], "DATASET_NOT_FOUND")
    self.kb.backend_dataset_id = "dataset-1"
    self.kb.save()
    self.doc.backend_document_id = ""
    self.doc.save()
    response = self.upload()
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.data["code"], "DOCUMENT_NOT_SYNCED")
    self.backend.upload_segment_image.assert_not_called()

  def test_rejects_missing_multiple_or_unsupported_files(self):
    cases = [
      {}, {"file": [image(), image()]}, {"file": image(), "other": image()},
      {"file": image("test.txt")}, {"file": image(content=b"not a png")},
      {"file": image(content_type="image/jpeg")}, {"file": image(content=b"")},
    ]
    for data in cases:
      with self.subTest(data=list(data)):
        self.assertEqual(self.upload(data).status_code, 400)
    self.backend.upload_segment_image.assert_not_called()

  @override_settings(ATTACHMENT_IMAGE_FILE_SIZE_LIMIT=1)
  def test_configured_size_limit(self):
    self.assertEqual(self.upload({"file": image(content=PNG + b"x" * (1024 * 1024))}).status_code, 400)
    response = self.upload({"file": image(content=PNG + b"x" * (1024 * 1024 - len(PNG)))})
    self.assertEqual(response.status_code, 201)

  def test_octet_stream_is_normalized_to_image_mime(self):
    self.assertEqual(self.upload({"file": image(content_type="application/octet-stream")}).status_code, 201)
    self.assertEqual(self.backend.upload_segment_image.call_args.args[0].content_type, "image/png")

  def test_backend_timeout_and_rejections(self):
    self.backend.upload_segment_image.side_effect = Timeout("timeout")
    response = self.upload()
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.data["code"], "BACKEND_UPLOAD_SEGMENT_IMAGE_FAILED")
    for backend_status, expected in ((400, 400), (413, 413), (415, 415), (403, 502), (404, 502)):
      with self.subTest(backend_status=backend_status):
        backend_response = Response()
        backend_response.status_code = backend_status
        self.backend.upload_segment_image.side_effect = HTTPError("rejected", response=backend_response)
        self.assertEqual(self.upload().status_code, expected)

  @override_settings(SINGLE_CHUNK_ATTACHMENT_LIMIT=2)
  def test_attachment_validation_on_create_and_update(self):
    for ids in ("file-1", ["a", "b", "c"], [123], [""], ["   "], [{}]):
      with self.subTest(ids=ids):
        segment = {"content": "description", "attachment_ids": ids}
        create = self.client.post(self.segments_url, {"segments": [segment]}, format="json", HTTP_NAMESPACE="project-a")
        update = self.client.post(self.detail_url, {"segment": segment}, format="json", HTTP_NAMESPACE="project-a")
        self.assertEqual(create.status_code, 400)
        self.assertEqual(update.status_code, 400)
    self.backend.add_segments.assert_not_called()
    self.backend.update_segment.assert_not_called()

  def test_text_only_request_keeps_original_payload(self):
    segment = {"content": "text", "keywords": ["keyword"]}
    self.client.post(self.segments_url, {"segments": [segment]}, format="json", HTTP_NAMESPACE="project-a")
    self.assertEqual(self.backend.add_segments.call_args.kwargs["segments"], [segment])
    self.client.post(self.detail_url, {"segment": segment}, format="json", HTTP_NAMESPACE="project-a")
    self.assertEqual(self.backend.update_segment.call_args.kwargs["segment"], segment)

  def test_partial_failure_exposes_created_segments_for_update_retry(self):
    created = {"data": [{"id": "segment-1", "attachments": []}]}
    failures = [{"index": 0, "segment_id": "segment-1", "attachment_ids": ["file-1"]}]
    self.backend.add_segments.side_effect = SegmentAttachmentError(created, failures)
    response = self.client.post(self.segments_url, {"segments": [{"content": "text", "attachment_ids": ["file-1"]}]}, format="json", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.data["code"], "BACKEND_ATTACH_SEGMENT_IMAGES_FAILED")
    self.assertEqual(response.data["data"], created)
    self.assertEqual(response.data["failed_segments"], failures)


class DifySegmentImageClientTests(SimpleTestCase):
  def setUp(self):
    patcher = patch.dict(os.environ, {"DIFY_BASE_URL": "https://dify.example/", "DIFY_API_KEY": "knowledge-key"})
    patcher.start()
    self.addCleanup(patcher.stop)
    self.backend = DifyClient()

  @patch("knowledge.services.dify_client.requests.post")
  def test_multipart_upload_uses_knowledge_key_and_returns_id(self, post):
    post.return_value.json.return_value = {"id": "file-1", "name": "example.png"}
    uploaded = image()
    self.assertEqual(self.backend.upload_segment_image(uploaded)["id"], "file-1")
    post.assert_called_once_with(
      "https://dify.example/v1/datasets/pipeline/file-upload",
      headers={"Authorization": "Bearer knowledge-key"},
      files={"file": ("example.png", uploaded, "image/png")}, timeout=60,
    )
    post.return_value.raise_for_status.assert_called_once_with()

  @patch("knowledge.services.dify_client.requests.post")
  def test_upload_rejects_missing_file_id(self, post):
    post.return_value.json.return_value = {}
    with self.assertRaisesRegex(ValueError, "文件 ID"):
      self.backend.upload_segment_image(image())

  @patch("knowledge.services.dify_client.requests.post")
  def test_upload_propagates_backend_errors(self, post):
    post.return_value.raise_for_status.side_effect = HTTPError("rejected")
    with self.assertRaises(HTTPError):
      self.backend.upload_segment_image(image())

  @patch("knowledge.services.dify_client.requests.post")
  def test_missing_attachments_are_bound_without_recreating_segments(self, post):
    created = Mock()
    created.json.return_value = {"data": [{"id": "segment-1", "attachments": []}, {"id": "segment-2", "content": "text"}], "doc_form": "text_model"}
    updated = Mock()
    updated.json.return_value = {"data": {"id": "segment-1", "attachments": [{"id": "file-1"}]}}
    post.side_effect = [created, updated]
    segments = [{"content": "image", "attachment_ids": ["file-1"]}, {"content": "text"}]
    result = self.backend.add_segments("dataset-1", "document-1", segments)
    self.assertEqual(result["data"][0]["attachments"], [{"id": "file-1"}])
    self.assertEqual(result["data"][1]["content"], "text")
    self.assertEqual(post.call_count, 2)
    self.assertEqual(post.call_args_list[0].kwargs["json"], {"segments": segments})
    self.assertEqual(post.call_args_list[1].args[0], "https://dify.example/v1/datasets/dataset-1/documents/document-1/segments/segment-1")
    self.assertEqual(post.call_args_list[1].kwargs["json"], {"segment": {"attachment_ids": ["file-1"]}})

  @patch("knowledge.services.dify_client.requests.post")
  def test_does_not_update_text_only_or_already_bound_segments(self, post):
    for segment, created in (
      ({"content": "text"}, {"id": "segment-1"}),
      ({"content": "image", "attachment_ids": ["file-1"]}, {"id": "segment-1", "attachments": [{"id": "file-1"}]}),
    ):
      with self.subTest(segment=segment):
        post.reset_mock()
        post.return_value.json.return_value = {"data": [created]}
        self.backend.add_segments("dataset-1", "document-1", [segment])
        post.assert_called_once()

  @patch("knowledge.services.dify_client.requests.post")
  def test_failed_binding_retains_ids_and_attempts_other_segments(self, post):
    created = Mock()
    created.json.return_value = {"data": [{"id": "segment-1", "attachments": []}, {"id": "segment-2", "attachments": []}]}
    updated = Mock()
    updated.json.return_value = {"data": {"id": "segment-2", "attachments": [{"id": "file-2"}]}}
    post.side_effect = [created, Timeout("timeout"), updated]
    with self.assertRaises(SegmentAttachmentError) as caught:
      self.backend.add_segments("dataset-1", "document-1", [
        {"content": "first", "attachment_ids": ["file-1"]},
        {"content": "second", "attachment_ids": ["file-2"]},
      ])
    self.assertEqual(caught.exception.failed_segments[0]["segment_id"], "segment-1")
    self.assertEqual(caught.exception.result["data"][1]["attachments"], [{"id": "file-2"}])

  @patch("knowledge.services.dify_client.requests.post")
  def test_does_not_report_success_if_dify_ignores_attachment_update(self, post):
    post.return_value.json.return_value = {"data": [{"id": "segment-1", "attachments": []}]}
    updated = Mock()
    updated.json.return_value = {"data": {"id": "segment-1", "attachments": []}}
    post.side_effect = [post.return_value, updated]
    with self.assertRaises(SegmentAttachmentError):
      self.backend.add_segments("dataset-1", "document-1", [{"content": "text", "attachment_ids": ["file-1"]}])
