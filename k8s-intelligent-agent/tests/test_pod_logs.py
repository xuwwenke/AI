from app.infrastructure.k8s_client import get_k8s_client
from app.services.diagnosis_service import DiagnosisService


def main():
  k8s = get_k8s_client()
  service = DiagnosisService(core_v1_api=k8s.core_v1)
  logs = service.get_pod_logs(
    namespace="ailabx",
    pod_name="kubeflow-dashboard-schedule-798d586644-pqm8f",
    previous=True,
    tail_lines=100,
  )

  print(f"日志内容：\n{logs}")


if __name__ == "__main__":
  main()