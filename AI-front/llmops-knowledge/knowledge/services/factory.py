
from .base import BaseKnowledgeBackend
from .dify_client import DifyClient

BACKENDS = {
  "dify": DifyClient,
}


def get_backend(backend_type: str) -> BaseKnowledgeBackend:
  cls = BACKENDS.get(backend_type)
  if cls is None:
    raise ValueError(f"不支持的知识库后端: {backend_type}")
  return cls()


def register_backend(backend_type: str, cls):
  BACKENDS[backend_type] = cls