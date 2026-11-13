import os
from unittest.mock import Mock, patch

from django.test import SimpleTestCase, TestCase
from requests import Response, Timeout
from rest_framework.test import APIClient, APIRequestFactory

from .models import KnowledgeBase
from .services.dify_client import DifyClient
from .views import KnowledgeBaseDetailView, KnowledgeBaseListCreateView


class KnowledgeBaseCreatorTests(TestCase):
  def setUp(self):
    self.factory = APIRequestFactory()
    patcher = patch("knowledge.views.get_backend")
    self.backend = patcher.start().return_value
    self.addCleanup(patcher.stop)
    self.backend.create_dataset.return_value = {"id": "dataset-1"}

  def create(self, **fields):
    request = self.factory.post("/api/knowledge-bases/", {"name": "test", **fields}, format="json", HTTP_NAMESPACE="project-a")
    return KnowledgeBaseListCreateView.as_view()(request)

  def test_creator_persistence_and_responses(self):
    response = self.create(creator_username="alice", creator_email="alice@example.com")
    self.assertEqual(response.status_code, 201)
    kb = KnowledgeBase.objects.get()
    self.assertEqual(kb.creator_username, "alice")
    self.assertEqual(kb.creator_email, "alice@example.com")
    self.assertEqual(response.data["data"]["creator_email"], kb.creator_email)
    self.backend.create_dataset.assert_called_once_with(name="test", description="")
    request = self.factory.get("/api/knowledge-bases/", HTTP_NAMESPACE="project-a")
    response = KnowledgeBaseListCreateView.as_view()(request)
    self.assertEqual(response.data[0]["creator_username"], "alice")
    self.backend.get_dataset.return_value = {"creator_username": "dify", "creator_email": "dify@example.com"}
    response = KnowledgeBaseDetailView.as_view()(request, pk=kb.pk)
    self.assertEqual(response.data["data"]["creator_username"], "alice")
    self.assertEqual(response.data["data"]["creator_email"], "alice@example.com")

  def test_legacy_request(self):
    self.assertEqual(self.create().status_code, 201)
    kb = KnowledgeBase.objects.get()
    self.assertEqual((kb.creator_username, kb.creator_email), ("", ""))

  def test_invalid_email(self):
    response = self.create(creator_email="invalid")
    self.assertEqual(response.status_code, 400)
    self.assertIn("creator_email", response.data)
    self.assertFalse(KnowledgeBase.objects.exists())
    self.backend.create_dataset.assert_not_called()

  def test_failure_preserves_creator(self):
    self.backend.create_dataset.side_effect = RuntimeError("unavailable")
    response = self.create(creator_username="alice", creator_email="alice@example.com")
    self.assertEqual(response.status_code, 502)
    kb = KnowledgeBase.objects.get()
    self.assertEqual(kb.status, "failed")
    self.assertEqual(kb.creator_email, "alice@example.com")
    self.assertEqual(response.data["knowledge_base"]["creator_username"], "alice")

  def test_update_preserves_creator(self):
    kb = KnowledgeBase.objects.create(name="old", namespace="project-a", creator_username="alice", creator_email="alice@example.com")
    request = self.factory.patch("/api/knowledge-bases/", {"name": "new", "creator_username": "bob", "creator_email": "bob@example.com"}, format="json", HTTP_NAMESPACE="project-a")
    response = KnowledgeBaseDetailView.as_view()(request, pk=kb.pk)
    self.assertEqual(response.status_code, 200)
    kb.refresh_from_db()
    self.assertEqual(kb.name, "new")
    self.assertEqual((kb.creator_username, kb.creator_email), ("alice", "alice@example.com"))


DATASET_UPDATE_PAYLOAD = {
  "name": "Product Documentation",
  "description": "产品 API 技术文档",
  "embedding_model": "text-embedding-3-small",
  "embedding_model_provider": "langgenius/openai/openai",
  "external_knowledge_api_id": "external-api-1",
  "external_knowledge_id": "external-dataset-1",
  "external_retrieval_model": {
    "score_threshold": 0, "score_threshold_enabled": False, "top_k": 3,
  },
  "indexing_technique": "high_quality",
  "permission": "partial_members",
  "partial_member_list": [{"user_id": "user-1"}],
  "retrieval_model": {
    "search_method": "hybrid_search", "top_k": 3,
    "score_threshold_enabled": False, "score_threshold": None,
    "reranking_enable": True, "reranking_mode": "weighted_score",
    "reranking_model": {"reranking_model_name": "", "reranking_provider_name": ""},
    "weights": {
      "weight_type": "customized",
      "keyword_setting": {"keyword_weight": 0.3},
      "vector_setting": {
        "embedding_model_name": "text-embedding-3-small",
        "embedding_provider_name": "langgenius/openai/openai", "vector_weight": 0.7,
      },
    },
    "metadata_filtering_conditions": {
      "logical_operator": "and",
      "conditions": [{"name": "version", "comparison_operator": "in", "value": [1, 2]}],
    },
  },
}


class DifyDatasetUpdateClientTests(SimpleTestCase):
  @patch("knowledge.services.dify_client.requests.patch")
  def test_forwards_patch_fields_without_injecting_defaults(self, patch_request):
    backend_response = Mock()
    backend_response.json.return_value = {"id": "dataset-1", "embedding_available": True}
    patch_request.return_value = backend_response
    payloads = (
      DATASET_UPDATE_PAYLOAD,
      {"name": "new"},
      {"description": "", "partial_member_list": [], "indexing_technique": None},
    )
    with patch.dict(os.environ, {
      "DIFY_BASE_URL": "https://api.dify.ai/", "DIFY_API_KEY": "secret",
    }):
      for payload in payloads:
        with self.subTest(payload=payload):
          result = DifyClient().update_dataset("dataset-1", **payload)
          self.assertEqual(result, backend_response.json.return_value)
          patch_request.assert_called_with(
            "https://api.dify.ai/v1/datasets/dataset-1",
            headers={"Authorization": "Bearer secret", "Content-Type": "application/json"},
            json=payload, timeout=30,
          )
    self.assertEqual(backend_response.raise_for_status.call_count, len(payloads))


class KnowledgeBaseUpdateTests(TestCase):
  def setUp(self):
    self.factory = APIRequestFactory()
    patcher = patch("knowledge.views.get_backend")
    self.backend = patcher.start().return_value
    self.addCleanup(patcher.stop)
    self.backend.update_dataset.return_value = {}
    self.kb = KnowledgeBase.objects.create(
      name="old", description="old description", namespace="project-a",
      backend_dataset_id="dataset-1", creator_username="alice",
      creator_email="alice@example.com",
    )

  def update(self, payload, namespace="project-a"):
    request = self.factory.patch(
      "/api/knowledge-bases/", payload, format="json", HTTP_NAMESPACE=namespace,
    )
    return KnowledgeBaseDetailView.as_view()(request, pk=self.kb.pk)

  def test_forwards_all_fields_and_returns_backend_patch_result(self):
    backend_info = {
      "id": "dataset-1", "name": DATASET_UPDATE_PAYLOAD["name"],
      "embedding_model": "text-embedding-3-small", "embedding_available": True,
      "retrieval_model_dict": DATASET_UPDATE_PAYLOAD["retrieval_model"],
      "partial_member_list": ["user-1"], "external_knowledge_info": None,
      "document_count": 0, "created_by": "backend-user", "updated_at": 1741267200,
      "creator_username": "backend-name", "creator_email": "backend@example.com",
      "namespace": "another-project", "backend_dataset_id": "wrong-dataset",
    }
    self.backend.update_dataset.return_value = backend_info
    response = self.update(DATASET_UPDATE_PAYLOAD)

    self.assertEqual(response.status_code, 200)
    self.backend.update_dataset.assert_called_once_with(dataset_id="dataset-1", **DATASET_UPDATE_PAYLOAD)
    self.backend.get_dataset.assert_not_called()
    data = response.data["data"]
    self.assertEqual(data["id"], str(self.kb.pk))
    self.assertEqual(data["backend_dataset_id"], "dataset-1")
    self.assertEqual(data["namespace"], "project-a")
    self.assertEqual(data["creator_username"], "alice")
    self.assertEqual(data["creator_email"], "alice@example.com")
    for field in ("embedding_model", "embedding_available", "retrieval_model_dict",
                  "partial_member_list", "external_knowledge_info", "document_count",
                  "created_by", "updated_at"):
      self.assertEqual(data[field], backend_info[field])
    self.kb.refresh_from_db()
    self.assertEqual(self.kb.name, DATASET_UPDATE_PAYLOAD["name"])
    self.assertEqual(self.kb.description, DATASET_UPDATE_PAYLOAD["description"])

  def test_updates_backend_configuration_without_name_or_description(self):
    payload = {"embedding_model": "bge-m3", "embedding_model_provider": "custom/provider/model"}
    response = self.update(payload)
    self.assertEqual(response.status_code, 200)
    self.backend.update_dataset.assert_called_once_with(dataset_id="dataset-1", **payload)
    self.kb.refresh_from_db()
    self.assertEqual((self.kb.name, self.kb.description), ("old", "old description"))

  def test_preserves_explicit_empty_false_zero_and_null_values(self):
    payload = {
      "description": "", "partial_member_list": [], "indexing_technique": None,
      "external_retrieval_model": {"score_threshold": 0, "score_threshold_enabled": False},
      "retrieval_model": {
        "search_method": "keyword_search", "top_k": 1, "reranking_enable": False,
        "score_threshold_enabled": False, "score_threshold": None, "weights": None,
        "metadata_filtering_conditions": None,
      },
    }
    response = self.update(payload)
    self.assertEqual(response.status_code, 200)
    self.backend.update_dataset.assert_called_once_with(dataset_id="dataset-1", **payload)
    self.kb.refresh_from_db()
    self.assertEqual(self.kb.description, "")

  def test_does_not_send_or_update_omitted_fields(self):
    response = self.update({"name": "new"})
    self.assertEqual(response.status_code, 200)
    self.backend.update_dataset.assert_called_once_with(dataset_id="dataset-1", name="new")
    self.kb.refresh_from_db()
    self.assertEqual(self.kb.description, "old description")

  def test_ignores_platform_identity_and_creator_updates(self):
    response = self.update({
      "name": "new", "backend_type": "other", "backend_dataset_id": "other",
      "namespace": "other", "creator_username": "bob", "creator_email": "bob@example.com",
    })
    self.assertEqual(response.status_code, 200)
    self.backend.update_dataset.assert_called_once_with(dataset_id="dataset-1", name="new")
    self.kb.refresh_from_db()
    self.assertEqual(self.kb.backend_type, "dify")
    self.assertEqual(self.kb.backend_dataset_id, "dataset-1")
    self.assertEqual(self.kb.namespace, "project-a")
    self.assertEqual(self.kb.creator_username, "alice")

  def test_backend_failure_leaves_local_values_unchanged(self):
    self.backend.update_dataset.side_effect = RuntimeError("unavailable")
    response = self.update({"name": "new", "description": "new description"})
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.data["code"], "BACKEND_UPDATE_FAILED")
    self.kb.refresh_from_db()
    self.assertEqual((self.kb.name, self.kb.description), ("old", "old description"))

  def test_rejects_empty_or_invalid_updates_before_calling_backend(self):
    payloads = (
      {}, {"name": ""}, {"name": "x" * 41}, {"description": "x" * 401},
      {"permission": "public"}, {"indexing_technique": "unknown"},
      {"partial_member_list": ["user-1"]}, {"partial_member_list": [{}]},
      {"retrieval_model": {}}, {"external_retrieval_model": {"top_k": 0}},
      {"retrieval_model": {
        "search_method": "semantic_search", "top_k": 3,
        "score_threshold_enabled": False, "reranking_enable": True,
      }},
    )
    for payload in payloads:
      with self.subTest(payload=payload):
        self.assertEqual(self.update(payload).status_code, 400)
    self.backend.update_dataset.assert_not_called()
    self.kb.refresh_from_db()
    self.assertEqual(self.kb.name, "old")

  def test_requires_correct_namespace(self):
    self.assertEqual(self.update({"name": "new"}, namespace="").status_code, 400)
    self.assertEqual(self.update({"name": "new"}, namespace="project-b").status_code, 404)
    self.backend.update_dataset.assert_not_called()

  def test_unsynced_dataset_allows_local_update_but_rejects_backend_configuration(self):
    self.kb.backend_dataset_id = ""
    self.kb.save(update_fields=["backend_dataset_id"])
    response = self.update({"name": "new", "permission": "only_me"})
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.data["code"], "DATASET_NOT_FOUND")
    self.kb.refresh_from_db()
    self.assertEqual(self.kb.name, "old")
    self.assertEqual(self.update({"name": "new"}).status_code, 200)
    self.kb.refresh_from_db()
    self.assertEqual(self.kb.name, "new")
    self.backend.update_dataset.assert_not_called()


class DifyModelsListTests(SimpleTestCase):
  def setUp(self):
    self.client = APIClient()
    environment = patch.dict(os.environ, {
      "DIFY_BASE_URL": "https://api.dify.ai/", "DIFY_API_KEY": "dify-secret",
    })
    environment.start()
    self.addCleanup(environment.stop)
    request_patcher = patch("knowledge.services.dify_client.requests.get")
    self.get_request = request_patcher.start()
    self.addCleanup(request_patcher.stop)
    self.backend_response = Mock()
    self.get_request.return_value = self.backend_response

  def test_returns_dify_provider_array_and_preserves_complete_model_fields(self):
    for model_type in ("text-embedding", "rerank", "llm", "tts", "speech2text", "moderation"):
      with self.subTest(model_type=model_type):
        providers = [{
          "icon_small": {"en_US": "https://example.com/icon.svg", "zh_Hans": "https://example.com/icon.svg"},
          "icon_small_dark": None,
          "label": {"en_US": "OpenAI", "zh_Hans": "OpenAI"},
          "provider": "langgenius/openai/openai", "status": "active", "tenant_id": "workspace-1",
          "models": [{
            "deprecated": False, "features": None, "fetch_from": "predefined-model",
            "has_invalid_load_balancing_configs": False,
            "label": {"en_US": "test-model", "zh_Hans": "测试模型"},
            "load_balancing_enabled": False, "model": "test-model",
            "model_properties": {"context_size": 8191}, "model_type": model_type,
            "status": "active", "future_backend_field": {"supported": True},
          }],
        }]
        self.backend_response.json.return_value = {"data": providers}

        response = self.client.get(f"/api/models/{model_type}/", HTTP_NAMESPACE="project-a")

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"code": 0, "message": "success", "data": providers})
        self.get_request.assert_called_with(
          f"https://api.dify.ai/v1/workspaces/current/models/model-types/{model_type}",
          headers={"Authorization": "Bearer dify-secret"}, timeout=30,
        )
    self.assertEqual(self.get_request.call_count, 6)
    self.assertEqual(self.backend_response.raise_for_status.call_count, 6)

  def test_returns_empty_dify_model_list(self):
    self.backend_response.json.return_value = {"data": []}
    response = self.client.get("/api/models/text-embedding/", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.json()["data"], [])

  def test_rejects_invalid_model_type_without_calling_backend(self):
    response = self.client.get("/api/models/unknown/", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.json()["code"], "INVALID_MODEL_TYPE")
    self.get_request.assert_not_called()

  def test_requires_namespace_without_calling_backend(self):
    response = self.client.get("/api/models/rerank/")
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.json()["code"], "NAMESPACE_REQUIRED")
    self.get_request.assert_not_called()

  def test_preserves_dify_error_reason(self):
    backend_response = Response()
    backend_response.status_code = 401
    backend_response._content = b'{"code":"unauthorized","message":"Invalid API token"}'
    self.get_request.return_value = backend_response
    response = self.client.get("/api/models/text-embedding/", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.json(), {
      "code": "BACKEND_GET_MODELS_FAILED", "message": "获取 Dify 可用模型失败",
      "detail": "Invalid API token", "backend_status": 401, "backend_code": "unauthorized",
    })

  def test_handles_non_json_backend_error(self):
    backend_response = Response()
    backend_response.status_code = 503
    backend_response._content = b'upstream unavailable'
    self.get_request.return_value = backend_response
    response = self.client.get("/api/models/llm/", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.json()["detail"], "upstream unavailable")
    self.assertEqual(response.json()["backend_status"], 503)

  def test_handles_backend_timeout(self):
    self.get_request.side_effect = Timeout("request timed out")
    response = self.client.get("/api/models/rerank/", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.json()["code"], "BACKEND_GET_MODELS_FAILED")
    self.assertEqual(response.json()["detail"], "request timed out")

  def test_rejects_malformed_backend_list(self):
    for payload in ({}, {"data": None}, {"data": {}}, [], {"data": "models"}):
      with self.subTest(payload=payload):
        self.backend_response.json.return_value = payload
        response = self.client.get("/api/models/rerank/", HTTP_NAMESPACE="project-a")
        self.assertEqual(response.status_code, 502)
        self.assertEqual(response.json()["detail"], "Dify 返回的模型列表格式不正确")
