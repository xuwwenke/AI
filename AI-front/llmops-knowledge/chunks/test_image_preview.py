import os
from unittest.mock import Mock, patch
from urllib.parse import parse_qs, urlsplit

from django.test import SimpleTestCase, TestCase
from django.urls import reverse
from requests import HTTPError, Response, Timeout
from rest_framework.test import APIClient

from knowledge.models import KnowledgeBase, KnowledgeDocument
from knowledge.services.base import SegmentImageNotFoundError
from knowledge.services.dify_client import DifyClient
from .tests import PNG


class SegmentImagePreviewViewTests(TestCase):
  def setUp(self):
    self.client = APIClient()
    self.kb = KnowledgeBase.objects.create(name="test", namespace="project-a", backend_dataset_id="dataset-1")
    self.doc = KnowledgeDocument.objects.create(knowledge_base=self.kb, file_name="test.md", backend_document_id="document-1")
    self.kwargs = {"knowledge_base_id": self.kb.id, "document_id": self.doc.id}
    self.url = reverse("segment-image-preview", kwargs={**self.kwargs, "segment_id": "segment-1", "file_id": "file-1"})
    patcher = patch("chunks.views.get_backend")
    self.backend = patcher.start().return_value
    self.addCleanup(patcher.stop)
    self.backend.get_segment_image.return_value = {"content": PNG, "content_type": "image/png", "name": "图片.png"}

  def test_image_can_be_used_as_img_src_with_namespace_query(self):
    response = self.client.get(self.url, {"namespace": "project-a"}, HTTP_ACCEPT="image/png")
    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.content, PNG)
    self.assertEqual(response["Content-Type"], "image/png")
    self.assertTrue(response["Content-Disposition"].startswith("inline;"))
    self.assertEqual(int(response["Content-Length"]), len(PNG))
    self.assertEqual(response["Cache-Control"], "private, no-store")
    self.assertEqual(response["X-Content-Type-Options"], "nosniff")
    self.backend.get_segment_image.assert_called_once_with(dataset_id="dataset-1", document_id="document-1", segment_id="segment-1", file_id="file-1")

  def test_header_namespace_takes_precedence(self):
    response = self.client.get(self.url, {"namespace": "other"}, HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 200)

  def test_namespace_and_document_ownership_are_checked(self):
    response = self.client.get(self.url, HTTP_ACCEPT="image/png")
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response["Content-Type"], "application/json")
    self.assertEqual(response.json()["code"], "NAMESPACE_REQUIRED")
    response = self.client.get(self.url, {"namespace": "wrong"}, HTTP_ACCEPT="image/png")
    self.assertEqual(response.status_code, 404)
    self.assertEqual(response["Content-Type"], "application/json")
    other_kb = KnowledgeBase.objects.create(name="other", namespace="project-a")
    url = reverse("segment-image-preview", kwargs={**self.kwargs, "knowledge_base_id": other_kb.id, "segment_id": "segment-1", "file_id": "file-1"})
    self.assertEqual(self.client.get(url, HTTP_NAMESPACE="project-a").status_code, 404)
    self.backend.get_segment_image.assert_not_called()

  def test_unsynced_resources_are_rejected(self):
    self.kb.backend_dataset_id = ""
    self.kb.save()
    self.assertEqual(self.client.get(self.url, HTTP_NAMESPACE="project-a").data["code"], "DATASET_NOT_FOUND")
    self.kb.backend_dataset_id = "dataset-1"
    self.kb.save()
    self.doc.backend_document_id = ""
    self.doc.save()
    self.assertEqual(self.client.get(self.url, HTTP_NAMESPACE="project-a").data["code"], "DOCUMENT_NOT_SYNCED")
    self.backend.get_segment_image.assert_not_called()

  def test_unattached_image_is_not_found(self):
    self.backend.get_segment_image.side_effect = SegmentImageNotFoundError("removed")
    response = self.client.get(self.url, HTTP_NAMESPACE="project-a", HTTP_ACCEPT="image/*")
    self.assertEqual(response.status_code, 404)
    self.assertEqual(response.json()["code"], "SEGMENT_IMAGE_NOT_FOUND")

  def test_backend_errors_return_json_for_image_requests(self):
    for backend_status, expected in ((404, 404), (403, 502), (500, 502)):
      with self.subTest(backend_status=backend_status):
        upstream = Response()
        upstream.status_code = backend_status
        self.backend.get_segment_image.side_effect = HTTPError("failed", response=upstream)
        response = self.client.get(self.url, HTTP_NAMESPACE="project-a", HTTP_ACCEPT="image/png")
        self.assertEqual(response.status_code, expected)
        self.assertEqual(response["Content-Type"], "application/json")
    self.backend.get_segment_image.side_effect = Timeout("timeout")
    self.assertEqual(self.client.get(self.url, HTTP_NAMESPACE="project-a").status_code, 502)

  def test_segment_responses_include_preview_url_and_preserve_source(self):
    segment = {"id": "segment-1", "content": "test", "attachments": [{"id": "file-1", "source_url": "https://internal/signed"}]}
    self.backend.get_document_segments.return_value = {"data": [segment], "total": 1}
    self.backend.get_segment_detail.return_value = {"data": segment}
    self.backend.add_segments.return_value = {"data": [segment]}
    self.backend.update_segment.return_value = {"data": segment}
    list_url = reverse("document-segments-list", kwargs=self.kwargs)
    detail_url = reverse("segment-detail", kwargs={**self.kwargs, "segment_id": "segment-1"})
    responses = [
      self.client.get(list_url, HTTP_NAMESPACE="project-a"),
      self.client.get(detail_url, HTTP_NAMESPACE="project-a"),
      self.client.post(list_url, {"segments": [{"content": "test"}]}, format="json", HTTP_NAMESPACE="project-a"),
      self.client.post(detail_url, {"segment": {"content": "test"}}, format="json", HTTP_NAMESPACE="project-a"),
    ]
    for response in responses:
      with self.subTest(response=response.data):
        self.assertEqual(response.status_code, 200)
        data = response.data["data"]["data"]
        attachment = (data[0] if isinstance(data, list) else data)["attachments"][0]
        self.assertEqual(attachment["preview_url"], self.url + "?namespace=project-a")
        self.assertEqual(attachment["source_url"], "https://internal/signed")

  def test_namespace_in_preview_url_is_urlencoded(self):
    namespace = "项目 A&other=value"
    self.kb.namespace = namespace
    self.kb.save()
    self.backend.get_segment_detail.return_value = {"data": {"id": "segment-1", "attachments": [{"id": "file-1"}]}}
    url = reverse("segment-detail", kwargs={**self.kwargs, "segment_id": "segment-1"})
    response = self.client.get(url, HTTP_NAMESPACE=namespace)
    preview_url = response.data["data"]["data"]["attachments"][0]["preview_url"]
    self.assertEqual(parse_qs(urlsplit(preview_url).query), {"namespace": [namespace]})


class DifySegmentImagePreviewClientTests(SimpleTestCase):
  def setUp(self):
    patcher = patch.dict(os.environ, {"DIFY_BASE_URL": "https://dify.example", "DIFY_API_KEY": "secret", "DIFY_FILES_BASE_URL": "http://dify-internal/prefix/"})
    patcher.start()
    self.addCleanup(patcher.stop)
    self.backend = DifyClient()
    self.attachment = {"id": "file-1", "name": "test.png", "source_url": "https://browser-only/prefix/files/file-1/image-preview?timestamp=123&nonce=abc&sign=xyz%3D"}
    self.backend.get_segment_detail = Mock(return_value={"data": {"attachments": [self.attachment]}})
    patcher = patch("knowledge.services.dify_client.requests.get")
    self.get = patcher.start()
    self.addCleanup(patcher.stop)
    self.upstream = self.get.return_value
    self.upstream.status_code = 200
    self.upstream.headers = {"Content-Type": "image/png"}
    self.upstream.iter_content.return_value = [PNG[:10], PNG[10:]]

  def read(self):
    return self.backend.get_segment_image("dataset-1", "document-1", "segment-1", "file-1")

  def test_reads_signed_image_through_configured_host_without_api_key(self):
    result = self.read()
    self.assertEqual(result, {"content": PNG, "content_type": "image/png", "name": "test.png"})
    self.backend.get_segment_detail.assert_called_once_with("dataset-1", "document-1", "segment-1")
    self.get.assert_called_once_with("http://dify-internal/prefix/files/file-1/image-preview?timestamp=123&nonce=abc&sign=xyz%3D", timeout=30, stream=True, allow_redirects=False)
    self.upstream.close.assert_called_once_with()

  def test_files_address_defaults_to_api_address(self):
    os.environ.pop("DIFY_FILES_BASE_URL")
    self.assertEqual(DifyClient().files_base_url, "https://dify.example")

  def test_supports_relative_file_preview_url(self):
    self.attachment["source_url"] = "/files/file-1/file-preview?timestamp=123&nonce=abc&sign=xyz"
    self.read()
    self.assertTrue(self.get.call_args.args[0].startswith("http://dify-internal/prefix/files/file-1/file-preview?"))

  def test_checks_membership_before_requesting_image(self):
    self.backend.get_segment_detail.return_value = {"data": {"attachments": []}}
    with self.assertRaises(SegmentImageNotFoundError):
      self.read()
    self.get.assert_not_called()

  def test_rejects_unsigned_or_unrelated_paths(self):
    for source in ("", "https://other/image.png", "/files/file-1/file-preview", "/files/another/image-preview?timestamp=1&nonce=x&sign=x"):
      with self.subTest(source=source):
        self.attachment["source_url"] = source
        with self.assertRaises(ValueError):
          self.read()
    self.get.assert_not_called()

  def test_does_not_return_html_or_follow_redirects(self):
    for content_type, status_code in (("text/html", 200), ("image/svg+xml", 200), ("image/png", 302)):
      with self.subTest(content_type=content_type, status_code=status_code):
        self.upstream.headers = {"Content-Type": content_type}
        self.upstream.status_code = status_code
        with self.assertRaises(ValueError):
          self.read()
    self.assertEqual(self.upstream.close.call_count, 3)

  def test_limits_both_declared_and_streamed_image_size(self):
    self.upstream.headers["Content-Length"] = str(21 * 1024 * 1024)
    with self.assertRaisesRegex(ValueError, "大小限制"):
      self.read()
    self.upstream.headers.pop("Content-Length")
    self.upstream.iter_content.return_value = [b"x" * (1024 * 1024)] * 21
    with self.assertRaisesRegex(ValueError, "大小限制"):
      self.read()
    self.assertEqual(self.upstream.close.call_count, 2)

  def test_closes_upstream_on_read_failure(self):
    self.upstream.iter_content.side_effect = Timeout("timeout")
    with self.assertRaises(Timeout):
      self.read()
    self.upstream.close.assert_called_once_with()
