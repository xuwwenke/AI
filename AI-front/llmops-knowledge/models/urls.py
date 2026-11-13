from django.urls import path

from .views import EmbeddingInvocationView, ModelProviderCredentialCreateView, RerankInvocationView, TokenCountInvocationView


urlpatterns = [
  path("internal/models/embeddings/", EmbeddingInvocationView.as_view(), name="model-embeddings"),
  path("internal/models/embeddings/token-count/", TokenCountInvocationView.as_view(), name="model-token-count"),
  path("internal/models/rerank/", RerankInvocationView.as_view(), name="model-rerank"),
  path(
    "model-providers/<path:provider>/credentials/",
    ModelProviderCredentialCreateView.as_view(),
    name="model-provider-credential-create",
  ),
]

