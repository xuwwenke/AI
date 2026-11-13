from unittest.mock import Mock, patch

from django.test import TestCase, override_settings
from rest_framework.test import APIClient

from .models import ModelProvider
from .services import decrypt_credentials


class ModelProviderCredentialCreateTests(TestCase):
  def setUp(self):
    self.client = APIClient()
    self.url = "/api/model-providers/openai/credentials/"
    self.payload = {
      "name": "production",
      "credentials": {"api_key": "must-not-leak", "base_url": "https://api.example.com"},
    }

  def test_create_provider_credential_locally(self):
    response = self.client.post(self.url, self.payload, format="json", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 201)
    data = response.json()["data"]
    self.assertEqual(data["provider"], "openai")
    self.assertEqual(data["namespace"], "project-a")
    self.assertEqual(data["credential_fields"], ["api_key", "base_url"])
    self.assertNotIn("credentials", data)
    self.assertNotIn("must-not-leak", response.content.decode())

    instance = ModelProvider.objects.get(id=data["id"])
    self.assertNotIn(b"must-not-leak", bytes(instance.credentials_encrypted))
    self.assertEqual(decrypt_credentials(instance.credentials_encrypted), self.payload["credentials"])

  def test_namespace_is_required(self):
    response = self.client.post(self.url, self.payload, format="json")
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.json()["code"], "NAMESPACE_REQUIRED")

  def test_rejects_duplicate_name_in_same_namespace(self):
    self.client.post(self.url, self.payload, format="json", HTTP_NAMESPACE="project-a")
    response = self.client.post(self.url, self.payload, format="json", HTTP_NAMESPACE="project-a")
    self.assertEqual(response.status_code, 409)
    self.assertEqual(response.json()["code"], "MODEL_PROVIDER_ALREADY_EXISTS")

  def test_same_provider_name_can_be_used_in_another_namespace(self):
    first = self.client.post(self.url, self.payload, format="json", HTTP_NAMESPACE="project-a")
    second = self.client.post(self.url, self.payload, format="json", HTTP_NAMESPACE="project-b")
    self.assertEqual(first.status_code, 201)
    self.assertEqual(second.status_code, 201)

  def test_rejects_empty_credentials(self):
    response = self.client.post(
      self.url,
      {"name": "production", "credentials": {}},
      format="json",
      HTTP_NAMESPACE="project-a",
    )
    self.assertEqual(response.status_code, 400)


class ModelsListTests(TestCase):
  def setUp(self):
    self.client = APIClient()

  def create_provider(self, namespace="project-a", **overrides):
    payload = {
      "name": "production",
      "credentials": {
        "api_key": "secret",
        "base_url": "https://models.example.com/v1",
      },
      "supported_model_types": ["llm"],
      "models": ["model-b", "model-a"],
    }
    payload.update(overrides)
    return self.client.post(
      "/api/model-providers/openai-compatible/credentials/",
      payload,
      format="json",
      HTTP_NAMESPACE=namespace,
    )

  def test_lists_added_providers_and_configured_models(self):
    self.create_provider()
    self.create_provider(namespace="another-project", name="hidden")

    response = self.client.get("/api/local-models/llm/", HTTP_NAMESPACE="project-a")

    self.assertEqual(response.status_code, 200)
    providers = response.json()["data"]["providers"]
    self.assertEqual(len(providers), 1)
    self.assertEqual(providers[0]["provider"], "openai-compatible")
    self.assertEqual(providers[0]["credential_name"], "production")
    self.assertEqual(
      [item["model"] for item in providers[0]["models"]],
      ["model-a", "model-b"],
    )
    self.assertEqual(providers[0]["models"][0]["tags"], ["LLM", "CHAT"])

  @patch("models.services.requests.get")
  def test_deepseek_uses_predefined_catalog_with_capability_tags(self, get):
    response = self.client.post(
      "/api/model-providers/deepseek/credentials/",
      {
        "name": "deepseek-production",
        "credentials": {
          "api_key": "secret",
          "endpoint_url": "https://api.deepseek.com",
        },
      },
      format="json",
      HTTP_NAMESPACE="project-a",
    )
    self.assertEqual(response.status_code, 201)

    response = self.client.get("/api/local-models/llm/", HTTP_NAMESPACE="project-a")

    models = response.json()["data"]["providers"][0]["models"]
    provider = response.json()["data"]["providers"][0]
    self.assertEqual(provider["label"]["zh_Hans"], "深度求索")
    self.assertEqual(len(models), 4)
    flash = next(item for item in models if item["model"] == "deepseek-flash")
    self.assertEqual(flash["fetch_from"], "predefined-model")
    self.assertEqual(flash["model_properties"]["context_size"], 1048576)
    self.assertEqual(
      flash["tags"],
      ["LLM", "CHAT", "1048K", "REASONING", "TOOL CALL", "VISION"],
    )
    get.assert_not_called()

  @patch("models.services.requests.get")
  def test_discovers_openai_compatible_models(self, get):
    upstream_response = Mock()
    upstream_response.json.return_value = {
      "data": [{"id": "remote-b"}, {"id": "remote-a"}],
    }
    upstream_response.raise_for_status.return_value = None
    get.return_value = upstream_response
    self.create_provider(models=[])

    response = self.client.get("/api/local-models/llm/", HTTP_NAMESPACE="project-a")

    self.assertEqual(response.status_code, 200)
    models = response.json()["data"]["providers"][0]["models"]
    self.assertEqual([item["model"] for item in models], ["remote-a", "remote-b"])
    get.assert_called_once_with(
      "https://models.example.com/v1/models",
      headers={"Authorization": "Bearer secret"},
      timeout=(5, 20),
    )

  @patch("models.services.requests.get")
  def test_marks_only_failed_provider_as_error(self, get):
    import requests
    get.side_effect = requests.ConnectionError("network details")
    self.create_provider(models=[])
    response = self.client.get("/api/local-models/llm/", HTTP_NAMESPACE="project-a")

    provider = response.json()["data"]["providers"][0]
    self.assertEqual(provider["status"], "error")
    self.assertEqual(provider["models"], [])
    self.assertNotIn("network details", provider["error"])


@override_settings(MODEL_GATEWAY_SERVICE_TOKEN="gateway-secret")
class InternalModelGatewayTests(TestCase):
  def setUp(self):
    self.client = APIClient()
    self.headers = {
      "HTTP_NAMESPACE": "project-a",
      "HTTP_AUTHORIZATION": "Bearer gateway-secret",
    }
    response = self.client.post(
      "/api/model-providers/openai-compatible/credentials/",
      {
        "name": "production",
        "supported_model_types": ["text-embedding", "rerank"],
        "models": ["bge-m3", "bge-reranker-v2-m3"],
        "credentials": {"api_key": "upstream-secret", "base_url": "https://models.example.com/v1"},
      },
      format="json",
      HTTP_NAMESPACE="project-a",
    )
    self.assertEqual(response.status_code, 201)
    self.provider_id = response.json()["data"]["id"]

  def payload(self, model):
    return {
      "provider": "openai-compatible",
      "credential_name": "production",
      "model": model,
    }

  def test_requires_service_token(self):
    response = self.client.post(
      "/api/internal/models/embeddings/token-count/",
      {**self.payload("bge-m3"), "texts": ["hello"]},
      format="json",
      HTTP_NAMESPACE="project-a",
    )
    self.assertEqual(response.status_code, 403)

  def test_accepts_configured_service_token(self):
    response = self.client.post(
      "/api/internal/models/embeddings/token-count/",
      {**self.payload("bge-m3"), "texts": ["hello"]},
      format="json",
      **self.headers,
    )
    self.assertEqual(response.status_code, 200)

  def test_does_not_cross_namespace(self):
    response = self.client.post(
      "/api/internal/models/embeddings/token-count/",
      {**self.payload("bge-m3"), "texts": ["hello"]},
      format="json",
      HTTP_NAMESPACE="project-b",
      HTTP_AUTHORIZATION="Bearer gateway-secret",
    )
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.json()["code"], "MODEL_PROVIDER_ERROR")

  @patch("models.services.requests.post")
  def test_invokes_openai_compatible_embeddings(self, post):
    upstream = Mock()
    upstream.raise_for_status.return_value = None
    upstream.json.return_value = {
      "model": "bge-m3",
      "data": [
        {"index": 1, "embedding": [0.3, 0.4]},
        {"index": 0, "embedding": [0.1, 0.2]},
      ],
      "usage": {"prompt_tokens": 7, "total_tokens": 7},
    }
    post.return_value = upstream

    response = self.client.post(
      "/api/internal/models/embeddings/",
      {**self.payload("bge-m3"), "texts": ["first", "second"], "input_type": "document"},
      format="json",
      **self.headers,
    )

    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.json()["data"]["embeddings"], [[0.1, 0.2], [0.3, 0.4]])
    self.assertEqual(response.json()["data"]["usage"]["tokens"], 7)
    post.assert_called_once_with(
      "https://models.example.com/v1/embeddings",
      headers={"Content-Type": "application/json", "Authorization": "Bearer upstream-secret"},
      json={"model": "bge-m3", "input": ["first", "second"]},
      timeout=(5, 60.0),
    )

  def test_estimates_token_counts_when_no_tokenizer_is_configured(self):
    response = self.client.post(
      "/api/internal/models/embeddings/token-count/",
      {**self.payload("bge-m3"), "texts": ["你好 world", ""]},
      format="json",
      **self.headers,
    )
    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.json()["data"]["token_counts"], [4, 0])
    self.assertEqual(response.json()["data"]["method"], "estimated")

  @patch("models.services.requests.post")
  def test_invokes_and_normalizes_rerank(self, post):
    upstream = Mock()
    upstream.raise_for_status.return_value = None
    upstream.json.return_value = {"results": [
      {"index": 0, "relevance_score": 0.2},
      {"index": 2, "relevance_score": 0.9},
      {"index": 1, "relevance_score": 0.7},
    ]}
    post.return_value = upstream
    response = self.client.post(
      "/api/internal/models/rerank/",
      {
        **self.payload("bge-reranker-v2-m3"),
        "query": "报销流程",
        "documents": ["A", "B", "C"],
        "top_n": 2,
        "score_threshold": 0.5,
      },
      format="json",
      **self.headers,
    )
    self.assertEqual(response.status_code, 200)
    self.assertEqual(response.json()["data"]["results"], [
      {"index": 2, "score": 0.9, "document": "C"},
      {"index": 1, "score": 0.7, "document": "B"},
    ])

  @patch("models.services.requests.post")
  def test_hides_upstream_error_details(self, post):
    import requests
    response_mock = Mock(status_code=401)
    post.side_effect = requests.HTTPError("api key leaked", response=response_mock)
    response = self.client.post(
      "/api/internal/models/embeddings/",
      {**self.payload("bge-m3"), "texts": ["hello"]},
      format="json",
      **self.headers,
    )
    self.assertEqual(response.status_code, 502)
    self.assertEqual(response.json()["message"], "上游模型服务请求失败（HTTP 401）")
    self.assertNotIn("api key leaked", response.content.decode())

  def test_rejects_unconfigured_model(self):
    response = self.client.post(
      "/api/internal/models/embeddings/token-count/",
      {**self.payload("unknown-model"), "texts": ["hello"]},
      format="json",
      **self.headers,
    )
    self.assertEqual(response.status_code, 400)
    self.assertEqual(response.json()["code"], "MODEL_PROVIDER_ERROR")
