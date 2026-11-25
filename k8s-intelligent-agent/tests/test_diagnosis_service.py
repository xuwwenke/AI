from app.infrastructure.k8s_client import get_k8s_client
from app.services.diagnosis_service import DiagnosisService


def main():
  clients = get_k8s_client()
  service = DiagnosisService(
    core_v1_api=clients.core_v1,
    app_v1_api=clients.app_v1,
  )
  pods = service.list_pods("ailabx")
  for pod in pods:
    print(pod.model_dump(mode="json"))


if __name__ == "__main__":
  main()