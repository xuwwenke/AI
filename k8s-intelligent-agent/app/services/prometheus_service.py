# 负责 PromQL 和指标转换的 Service
from app.infrastructure.prometheus_client import (
  get_prometheus_client,
)


class PrometheusService:
  """节点监控指标服务。"""
  CPU_QUERY = """
  100 * (
    1 -
    avg by (instance) (
      rate(
          node_cpu_seconds_total{mode="idle"}[5m]
      )
    )
  )
  """

  MEMORY_USED_QUERY = """
  node_memory_MemTotal_bytes
  -
  node_memory_MemAvailable_bytes
  """

  MEMORY_TOTAL_QUERY = """
  node_memory_MemTotal_bytes
  """

  MEMORY_QUERY = """
  100 * (
    1 -
    node_memory_MemAvailable_bytes
    /
    node_memory_MemTotal_bytes
  )
  """

  NODE_INFO_QUERY = """
  node_uname_info
  """

  def __init__(self):
    self.client = get_prometheus_client()

  @staticmethod
  def _parse_vector(data: dict) -> dict:
    """将 Prometheus Vector 转换成字典。"""
    result = {}
    for item in data.get("result", []):
      labels = item.get("metric", {})
      instance = labels.get("instance")
      if not instance:
        continue
      value = float(item["value"][1])
      # 同一个 instance 如果出现重复序列，
      # 暂时保留最后一条；正常情况下应确保
      # Prometheus 目标配置不会产生重复采集。
      result[instance] = value

    return result

  def get_node_name_mapping(self) -> dict:
    """
    获取 Prometheus instance 到节点名称的映射。
    例如：
    192.168.246.151:9100 -> k8smaster01
    """
    data = self.client.query(self.NODE_INFO_QUERY)
    mapping = {}
    for item in data.get("result", []):
      labels = item.get("metric", {})
      instance = labels.get("instance")
      node_name = labels.get("nodename")
      if instance and node_name:
        mapping[instance] = node_name

    return mapping

  def get_node_metrics(self) -> dict:
    """获取所有节点的 CPU 和内存使用率。"""
    cpu = self._parse_vector(self.client.query(self.CPU_QUERY))
    memory_used = self._parse_vector(self.client.query(self.MEMORY_USED_QUERY))

    memory_total = self._parse_vector(self.client.query(self.MEMORY_TOTAL_QUERY))
    name_mapping = self.get_node_name_mapping()
    node_mapping = self.get_node_name_mapping()

    instances = (
      set(cpu)
      | set(memory_used)
      | set(memory_total)
    )
    result = {}
    
    for instance in instances:
      total = memory_total.get(instance)
      used = memory_used.get(instance)

      memory_percent = None
      if total is not None and total > 0 and used is not None:
        memory_percent = used / total * 100

      result[instance] = {
        "instance": instance,
        "node_name": node_mapping.get(instance),
        "cpu_usage_percent": cpu.get(instance),
        "memory_used_bytes": int(used) if used is not None else None,
        "memory_total_bytes": int(total) if total is not None else None,
        "memory_usage_percent": memory_percent,
      }

    return result
