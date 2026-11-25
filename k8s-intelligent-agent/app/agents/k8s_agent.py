from langchain.agents import create_agent

from app.infrastructure.llm import get_chat_model
from app.tools.node_tools import node_tools
from app.tools.diagnosis_tools import diagnose_deployment
from app.agents.prompts import K8S_AGENT_SYSTEM_PROMPT

# 汇总所有 Kubernetes 工具
k8s_tools = [
  *node_tools,
  diagnose_deployment,
]

def create_k8s_agent():
  """
  创建 Kubernetes 智能运维 Agent。
  """
  # 获取模型，支持 Ollama 和 OpenAI 兼容接口
  model = get_chat_model()
  # 创建 Agent
  agent = create_agent(
    model=model,
    tools=k8s_tools,
    system_prompt=K8S_AGENT_SYSTEM_PROMPT,
  )

  return agent