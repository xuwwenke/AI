from app.agents.k8s_agent import create_k8s_agent


def test_k8s_agent():
  agent = create_k8s_agent()
  # question = "当前 Kubernetes 集群的资源使用情况怎么样？"
  question = "哪些节点没有监控数据？"
  result = agent.invoke({
    "messages": [
      {
        "role": "user",
        "content": question,
      }
    ]
  })
  print("\n========== Agent 对话过程 ==========")
  # 打印完整消息，观察模型是否调用了工具
  for message in result["messages"]:
    print(f"\n消息类型：{message.type}")
    print(f"消息内容：{message.content}")

    # AIMessage 如果发起了工具调用，会有 tool_calls
    if getattr(message, "tool_calls", None):
      print("工具调用：", message.tool_calls)
  print("\n========== Agent 最终回答 ==========")
  print(result["messages"][-1].content)


if __name__ == "__main__":
  test_k8s_agent()