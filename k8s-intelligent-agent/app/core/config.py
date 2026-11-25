# 环境相关配置

from functools import lru_cache
from typing import Literal
from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

# BaseSettings：自动从环境变量和 .env 加载配置。
# Literal：限制模型提供商只能是 ollama 或 openai。
# lru_cache：避免每次调用都重新读取配置。
# Field：后续可以为配置增加长度、范围等约束。

class Settings(BaseSettings):
  """应用全局配置。"""

  model_config = SettingsConfigDict(
    env_file=".env",
    env_file_encoding="utf-8",
    case_sensitive=False,
    extra="ignore",
  )

  # Application
  app_name: str = "k8s-intelligent-agent"
  app_env: str = "development"
  debug: bool = True

  # LLM
  llm_provider: Literal["ollama", "openai"] = "ollama"
  llm_model: str = "qwen2.5:0.5b"

  # Ollama
  ollama_base_url: str = "http://localhost:11434"

  # OpenAI-compatible API
  openai_api_key: str = ""
  openai_base_url: str = "https://api.openai.com/v1"

  # Kubernetes
  k8s_in_cluster: bool = False
  k8s_kubeconfig: str = "/root/.kube/config"

  # Prometheus
  prometheus_url: str = "http://localhost:9090"
  prometheus_timeout: float = 10.0

  # Agent
  agent_max_iterations: int = 5


@lru_cache # 避免每次调用都重新读取配置
def get_settings() -> Settings:
  """全局配置单例。"""
  return Settings()


settings = get_settings()
