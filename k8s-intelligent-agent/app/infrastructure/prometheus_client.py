
import httpx

from app.core.config import settings

class PrometheusClient:
  """Prometheus HTTP API 客户端。"""

  def __init__(self):
    self.base_url = settings.prometheus_url.rstrip("/")
    self.timeout = settings.prometheus_timeout

  def query(self, promql: str) -> dict:
    """执行即时 PromQL 查询。"""
    url = f"{self.base_url}/api/v1/query"
    response = httpx.get(url, params={"query": promql}, timeout=self.timeout)
    response.raise_for_status()
    result = response.json()
    if result.get("status") != "success":
      raise RuntimeError(f"Prometheus 查询失败: {result}")
    return result["data"]

  def health_check(self) -> dict:
    """检查 Prometheus 服务状态。"""

    url = f"{self.base_url}/api/v1/status/buildinfo"
    response = httpx.get(url, timeout=self.timeout)
    response.raise_for_status()
    return response.json()

  def close(self):
    pass


_prometheus_client = None


def get_prometheus_client() -> PrometheusClient:
    global _prometheus_client

    if _prometheus_client is None:
        _prometheus_client = PrometheusClient()

    return _prometheus_client
