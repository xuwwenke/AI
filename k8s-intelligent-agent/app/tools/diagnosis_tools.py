from functools import lru_cache
from langchain_core.tools import tool
from app.services.diagnosis_service import DiagnosisService
from app.services.diagnosis_rules import DiagnosisRuleEngine
from app.infrastructure.k8s_client import get_k8s_client
import json

_diagnosis_service = None

@lru_cache(maxsize=1)
def get_diagnosis_service() -> DiagnosisService:
  global _diagnosis_service
  if _diagnosis_service is None:
    clients = get_k8s_client()
    _diagnosis_service = DiagnosisService(core_v1_api=clients.core_v1, app_v1_api=clients.app_v1)
  return _diagnosis_service


@tool
def list_pods(namespace: str) -> list[dict]:
  """
  查询指定 Kubernetes 命名空间下的所有 Pod。

  参数：
      namespace: Kubernetes 命名空间名称。

  返回：
      Pod 名称、运行状态、所在节点、Pod IP、
      容器状态、容器重启次数等信息。

  当用户询问 Pod 状态、异常 Pod、
  容器运行情况时调用。
  """

  service = get_diagnosis_service()
  pods = service.list_pods(namespace)
  return [pod.model_dump(mode="json") for pod in pods]

@tool
def get_pod_logs(namespace: str, pod_name: str, container: str | None = None, previous: bool = True, tail_lines: int = 100) -> str:
  """
  获取 Kubernetes Pod 容器日志。

  Args:
      namespace: Pod 所属 Namespace。
      pod_name: Pod 名称。
      container: 容器名称，不指定时使用默认容器。
      previous: 是否获取上一次终止的容器日志。
      tail_lines: 获取最近多少行日志。
  """

  service = get_diagnosis_service()
  logs = service.get_pod_logs(
    namespace=namespace,
    pod_name=pod_name,
    container=container,
    previous=previous,
    tail_lines=tail_lines,
  )
  return logs


@tool
def get_deployment_status(namespace: str, deployment_name: str) -> dict:
  """
  查询 Kubernetes Deployment 的运行状态。

  Args:
      namespace: Deployment 所属 Namespace。
      deployment_name: Deployment 名称。
  """
  service = get_diagnosis_service()
  deployment = service.get_deployment_status(namespace=namespace, deployment_name=deployment_name)
  return deployment.model_dump(mode="json")

@tool
def diagnose_deployment(namespace: str, deployment_name: str) -> str:
  """
  诊断 Kubernetes 中指定 Namespace 下的 Deployment。

  自动采集 Deployment 状态、关联 Pod 状态、Pod Events、
  容器 Previous Logs，并通过规则引擎识别异常。

  Args:
      namespace: Kubernetes Namespace，例如 ailabx。
      deployment_name: Deployment 名称，例如 kubeflow-dashboard-schedule。

  Returns:
      JSON 格式的诊断结果，包含当前健康状态、历史风险和诊断发现。
  """

  try:
    # 1. 获取 Kubernetes 客户端
    clients = get_k8s_client()

    # 2. 创建诊断 Service
    service = DiagnosisService(
      core_v1_api=clients.core_v1,
      app_v1_api=clients.app_v1,
    )

    # 3. 采集 Kubernetes 诊断证据
    report = service.diagnose_deployment(
      namespace=namespace,
      deployment_name=deployment_name,
    )
    # 4. 执行规则诊断
    engine = DiagnosisRuleEngine()
    result = engine.diagnose(report)

    # 5. 返回 JSON 字符串
    return result.model_dump_json(indent=2)

  except Exception as exc:
    # 将工具执行错误反馈给 Agent
    return json.dumps(
      {
        "error": "Deployment 诊断失败",
        "namespace": namespace,
        "deployment_name": deployment_name,
        "detail": str(exc),
      },
      ensure_ascii=False,
      indent=2,
    )

diagnosis_tools = [
  list_pods,
  get_pod_logs,
  get_deployment_status,
  diagnose_deployment,
]

