
from app.infrastructure.llm import get_chat_model


def main():
  model = get_chat_model()
  response = model.invoke("请用一句话介绍 Kubernetes。")
  print("模型响应：")
  print(response.content)


if __name__ == "__main__":
  main()
