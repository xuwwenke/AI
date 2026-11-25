
from langchain_core.language_models import BaseChatModel
from langchain_openai import ChatOpenAI
from langchain_ollama import ChatOllama

from app.core.config import settings

def get_chat_model() -> BaseChatModel:
  """
  创建聊天模型。

  支持：
  1. Ollama
  2. OpenAI API
  3. OpenAI 兼容 API
  """

  if settings.llm_provider == "ollama":
    return ChatOllama(
      model=settings.llm_model,
      base_url=settings.ollama_base_url,
      temperature=0,
    )

  if settings.llm_provider == "openai":
    if not settings.openai_api_key:
      raise ValueError("OPENAI_API_KEY 未配置")

    return ChatOpenAI(
      model=settings.llm_model,
      api_key=settings.openai_api_key,
      base_url=settings.openai_base_url,
      temperature=0,
      timeout=60,
      max_retries=2,
    )

  raise ValueError(f"不支持的模型提供商: {settings.llm_provider}")

if __name__ == "__main__":
  llm = get_chat_model()
  print(llm.invoke("你好"))
