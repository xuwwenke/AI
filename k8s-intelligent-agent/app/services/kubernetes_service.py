# parse_quantity() 返回的是标准化后的数值，CPU 的 7500m 会转换成 7.5，内存会转换成字节数。
from kubernetes.utils.quantity import parse_quantity
from app.infrastructure.k8s_client import get_k8s_client

class KubernetesService:
  """Kubernetes 资源查询服务。"""
  def __init__(self):
    self.k8s = get_k8s_client()

  @staticmethod
  def _get_ready_status(node) -> str:
    conditions = node.status.conditions or []
    for condition in conditions:
      if condition.type == "Ready":
        return condition.status
    return "Unknown"
  
  @staticmethod
  def _get_node_addresses(node) -> dict:
    """获取节点 IP 地址。"""
    result = {}
    for address in node.status.addresses or []:
      result[address.type] = address.address
    return result

  def list_node(self) -> list[dict]:
    """获取集群节点基础信息。"""
    result = self.k8s.core_v1.list_node()
    nodes = []
    for node in result.items:
      capacity = node.status.capacity or {}
      allocatable = node.status.allocatable or {}

      addresses = self._get_node_addresses(node)

      cpu_capacity = parse_quantity(capacity.get("cpu", "0"))
      memory_capacity = parse_quantity(capacity.get("memory", "0"))
      cpu_allocatable = parse_quantity(allocatable.get("cpu", "0"))
      memory_allocatable = parse_quantity(allocatable.get("memory", "0"))
      ready_status = self._get_ready_status(node)
      nodes.append({
        "name": node.metadata.name,
        "status": ready_status,
        "hostname": addresses.get("Hostname"),
        "internal_ip": addresses.get("InternalIP"),
        "external_ip": addresses.get("ExternalIP"),

        "cpu_capacity": float(cpu_capacity),
        "memory_capacity_bytes": int(memory_capacity),
        "cpu_allocatable": float(cpu_allocatable),
        "memory_allocatable_bytes": int(memory_allocatable),
      })
    return nodes