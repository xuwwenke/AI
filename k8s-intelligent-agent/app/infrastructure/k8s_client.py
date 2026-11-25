from kubernetes import client, config
from app.core.config import settings

class KubernetesClient:
  """Kubernetes 客户端管理器。"""
  def __init__(self):
    configuration = client.Configuration()
    if settings.k8s_in_cluster:
      config.load_incluster_config(client_configuration=configuration)
    else:
      kubeconfig = settings.k8s_kubeconfig or None
      config.load_kube_config(config_file = kubeconfig, client_configuration=configuration)

    self.api_client = client.ApiClient(configuration=configuration)
    self.core_v1 = client.CoreV1Api(api_client=self.api_client)
    self.app_v1 = client.AppsV1Api(api_client=self.api_client)

  def _close(self):
    self.api_client.close()

_k8s_client = None

def get_k8s_client() -> KubernetesClient:
  """获取 Kubernetes 客户端单例。"""
  global _k8s_client
  if _k8s_client is None:
    _k8s_client = KubernetesClient()
  return _k8s_client