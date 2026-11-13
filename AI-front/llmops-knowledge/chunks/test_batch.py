import json
import os
from unittest.mock import patch
from uuid import uuid4

from django.test import TestCase
from django.urls import reverse
from requests import Response, Timeout
from rest_framework.test import APIClient

from knowledge.models import KnowledgeBase, KnowledgeDocument


IDS = ["01234567-89ab-cdef-0123-456789abcdef", "11234567-89ab-cdef-0123-456789abcdef", "21234567-89ab-cdef-0123-456789abcdef"]


def backend_response(data=None, status=200):
  response = Response()
  response.status_code = status
  response._content = json.dumps(data).encode() if data is not None else b""
  return response


class SegmentsBatchTests(TestCase):
  def setUp(self):
    self.client = APIClient()
    self.kb = KnowledgeBase.objects.create(name="test", namespace="project-a", backend_dataset_id="dataset-1")
    self.doc = KnowledgeDocument.objects.create(knowledge_base=self.kb, file_name="test.md", backend_document_id="document-1")
    self.kwargs = {"knowledge_base_id": self.kb.id, "document_id": self.doc.id}
    self.url = reverse("segments-batch", kwargs=self.kwargs)
    patcher = patch.dict(os.environ, {"DIFY_BASE_URL": "https://dify.example", "DIFY_API_KEY": "secret"})
    patcher.start()
    self.addCleanup(patcher.stop)
    for method in ("get", "post", "delete"):
      patcher = patch(f"knowledge.services.dify_client.requests.{method}")
      setattr(self, method, patcher.start())
      self.addCleanup(patcher.stop)
    self.delete.return_value = backend_response(status=204)

  def batch(self, action="enable", segment_ids=None, namespace="project-a"):
    headers = {"HTTP_NAMESPACE": namespace} if namespace is not None else {}
    return self.client.post(self.url, {
      "action": action, "segment_ids": IDS[:2] if segment_ids is None else segment_ids,
    }, format="json", **headers)

  def test_enable_and_disable_update_only_status_and_keep_scope(self):
    for action, enabled in (("enable", True), ("disable", False)):
      with self.subTest(action=action):
        self.get.reset_mock()
        self.post.reset_mock()
        self.get.return_value = backend_response({"data": {"enabled": not enabled}})
        self.post.return_value = backend_response({"data": {"enabled": enabled}})
        response = self.batch(action)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["code"], 0)
        self.assertEqual(response.data["data"]["success_count"], 2)
        self.assertEqual(response.data["data"]["failure_count"], 0)
        self.assertEqual(self.get.call_count, 2)
        self.assertEqual(self.post.call_count, 2)
        for index, call in enumerate(self.post.call_args_list):
          self.assertEqual(call.args[0], f"https://dify.example/v1/datasets/dataset-1/documents/document-1/segments/{IDS[index]}")
          self.assertEqual(call.kwargs["json"], {"segment": {"enabled": enabled}})
        self.delete.assert_not_called()

  def test_repeating_status_action_is_success_without_update(self):
    for action, enabled in (("enable", True), ("disable", False)):
      with self.subTest(action=action):
        self.get.return_value = backend_response({"data": {"enabled": enabled}})
        response = self.batch(action)
        self.assertEqual(response.status_code, 200)
        self.assertTrue(all(item["unchanged"] for item in response.data["data"]["results"]))
    self.post.assert_not_called()

  def test_deletes_each_segment_using_dify_delete_and_accepts_204(self):
    response = self.batch("delete")
    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.data["data"]["total"], 2)
    self.assertEqual(self.delete.call_count, 2)
    self.assertEqual(self.delete.call_args_list[0].args[0], f"https://dify.example/v1/datasets/dataset-1/documents/document-1/segments/{IDS[0]}")
    self.get.assert_not_called()
    self.post.assert_not_called()

  def test_canonicalizes_and_deduplicates_ids_in_original_order(self):
    response = self.batch("delete", [IDS[1], IDS[0].upper(), IDS[1], IDS[0]])
    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.data["data"]["total"], 2)
    self.assertEqual([item["segment_id"] for item in response.data["data"]["results"]], [IDS[1], IDS[0]])
    self.assertEqual(self.delete.call_count, 2)

  def test_partial_delete_failure_keeps_success_and_continues(self):
    self.delete.side_effect = [
      backend_response(status=204),
      backend_response({"code": "not_found", "message": "Segment not found"}, status=404),
      backend_response(status=204),
    ]
    response = self.batch("delete", IDS)
    self.assertEqual(response.status_code, 207)
    self.assertEqual(response.data["code"], "BATCH_PARTIALLY_FAILED")
    data = response.data["data"]
    self.assertEqual((data["success_count"], data["failure_count"]), (2, 1))
    self.assertEqual(data["results"][1]["backend_status"], 404)
    self.assertEqual(data["results"][1]["backend_code"], "not_found")
    self.assertEqual(data["results"][1]["message"], "Segment not found")
    self.assertTrue(data["results"][2]["success"])

  def test_all_failed_results_include_transport_and_non_json_errors(self):
    self.delete.side_effect = [Timeout("timeout"), backend_response(status=500)]
    response = self.batch("delete")
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.data["code"], "BATCH_FAILED")
    self.assertEqual(response.data["data"]["failure_count"], 2)
    self.assertEqual(response.data["data"]["results"][0]["message"], "timeout")
    self.assertEqual(response.data["data"]["results"][1]["backend_status"], 500)

  def test_status_lookup_or_update_failure_does_not_stop_remaining_items(self):
    self.get.side_effect = [backend_response(status=404), backend_response({"data": {"enabled": False}}), backend_response({"data": {"enabled": True}})]
    self.post.return_value = backend_response({"message": "indexing"}, status=409)
    response = self.batch("enable", IDS)
    self.assertEqual(response.status_code, 207)
    results = response.data["data"]["results"]
    self.assertEqual([item["success"] for item in results], [False, False, True])
    self.assertEqual(results[1]["message"], "indexing")
    self.assertTrue(results[2]["unchanged"])
    self.post.assert_called_once()

  def test_rejects_backend_silent_failure_to_change_status(self):
    self.get.return_value = backend_response({"data": {"enabled": False}})
    self.post.return_value = backend_response({"data": {"enabled": False}})
    response = self.batch("enable", IDS[:1])
    self.assertEqual(response.status_code, 502)
    self.assertIn("目标启用状态", response.data["data"]["results"][0]["message"])

  def test_validates_entire_request_before_any_backend_call(self):
    cases = [
      {"action": "archive", "segment_ids": IDS[:1]},
      {"action": "enable", "segment_ids": []},
      {"action": "delete", "segment_ids": IDS[0]},
      {"action": "delete", "segment_ids": [IDS[0], "invalid"]},
      {"action": "delete", "segment_ids": [123]},
      {"action": "delete", "segment_ids": [None]},
      {"action": "delete", "segment_ids": [str(uuid4()) for _ in range(101)]},
      {"action": "enable"}, {"segment_ids": IDS},
    ]
    for payload in cases:
      with self.subTest(payload=payload):
        response = self.client.post(self.url, payload, format="json", HTTP_NAMESPACE="project-a")
        self.assertEqual(response.status_code, 400)
    self.get.assert_not_called()
    self.post.assert_not_called()
    self.delete.assert_not_called()

  def test_requires_namespace_and_document_ownership(self):
    self.assertEqual(self.batch(namespace=None).status_code, 400)
    self.assertEqual(self.batch(namespace="other-project").status_code, 404)
    other_kb = KnowledgeBase.objects.create(name="other", namespace="project-a")
    self.url = reverse("segments-batch", kwargs={**self.kwargs, "knowledge_base_id": other_kb.id})
    self.assertEqual(self.batch().status_code, 404)
    self.get.assert_not_called()
    self.post.assert_not_called()
    self.delete.assert_not_called()

  def test_rejects_unsynced_resources(self):
    self.kb.backend_dataset_id = ""
    self.kb.save()
    self.assertEqual(self.batch().data["code"], "DATASET_NOT_FOUND")
    self.kb.backend_dataset_id = "dataset-1"
    self.kb.save()
    self.doc.backend_document_id = ""
    self.doc.save()
    self.assertEqual(self.batch().data["code"], "DOCUMENT_NOT_SYNCED")
    self.get.assert_not_called()
    self.post.assert_not_called()
    self.delete.assert_not_called()
