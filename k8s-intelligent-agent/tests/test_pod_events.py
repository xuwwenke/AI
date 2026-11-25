from app.infrastructure.k8s_client import get_k8s_client
from app.services.diagnosis_service import DiagnosisService


def main():
  clients = get_k8s_client()
  service = DiagnosisService(
    core_v1_api=clients.core_v1
  )
  events = service.list_pod_events(
    namespace="ailabx",
    pod_name="llmops-service-fcb665fc7-5t64h",
  )
  print(f"查询到 {len(events)} 条 Events")
  for event in events:
    print("\n" + "=" * 60)
    print("类型:", event["type"])
    print("原因:", event["reason"])
    print("次数:", event["count"])
    print("消息:", event["message"])
    print("最后发生时间:", event["last_timestamp"])


if __name__ == "__main__":
  main()