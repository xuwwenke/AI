
from app.agents.k8s_agent import create_k8s_agent


def main():
    agent = create_k8s_agent()

    result = agent.invoke(
        {
            "messages": [
                {
                    "role": "user",
                    "content": (
                        "帮我分析 ailabx 命名空间下的 "
                        "kubeflow-dashboard-schedule "
                        "Deployment 运行情况，"
                        "重点分析是否存在异常和历史风险。"
                    ),
                }
            ]
        }
    )

    print("\n========== Agent Conversation ==========")

    for message in result["messages"]:
        print(f"\n[{message.type}]")
        print(message.content)

        # 打印工具调用信息
        if hasattr(message, "tool_calls") and message.tool_calls:
            for tool_call in message.tool_calls:
                print("Tool:", tool_call["name"])
                print("Arguments:", tool_call["args"])


if __name__ == "__main__":
    main()
