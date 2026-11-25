
from app.services.kubernetes_service import (
  KubernetesService,
)

from app.services.prometheus_service import (
  PrometheusService,
)

from app.schemas.node import (
  NodeResource,
  ClusterResourceSummary,
)


class NodeResourceService:
  """Kubernetes 节点资源分析服务。"""

  HIGH_USAGE_THRESHOLD = 80.0

  def __init__(self):
    self.k8s_service = KubernetesService()
    self.prom_service = PrometheusService()

  @staticmethod
  def _instance_host(instance: str) -> str:
    # Node Exporter 通常使用 host:port
    return instance.rsplit(":", 1)[0]

  def _match_metrics(
    self,
    node: dict,
    metrics: dict,
  ) -> dict | None:

    node_name = node["name"]

    # 优先使用 node_uname_info 映射
    for item in metrics.values():
      if item.get("node_name") == node_name:
        return item

    # 再通过节点 IP / Hostname 匹配
    aliases = {
      node_name,
      node.get("hostname"),
      node.get("internal_ip"),
      node.get("external_ip"),
    }

    aliases.discard(None)

    for instance, item in metrics.items():
      host = self._instance_host(instance)

      if host in aliases:
        return item

    return None

  def list_node_resources(self) -> list[NodeResource]:
    """获取集群所有节点的资源使用情况。"""

    nodes = self.k8s_service.list_node()

    metrics = self.prom_service.get_node_metrics()

    result = []

    for node in nodes:
      metric = self._match_metrics(
        node,
        metrics,
      )

      warnings = []

      cpu_percent = None
      cpu_cores = None
      cpu_allocatable_percent = None

      memory_used = None
      memory_total = None
      memory_percent = None

      if metric is None:
        warnings.append(
          "未找到该节点对应的 Prometheus 监控指标"
        )

      else:
        cpu_percent = metric.get(
          "cpu_usage_percent"
        )

        if cpu_percent is not None:
          cpu_cores = (
            node["cpu_capacity"]
            * cpu_percent / 100
          )

          if node["cpu_allocatable"] > 0:
            cpu_allocatable_percent = (
              cpu_cores
              / node["cpu_allocatable"]
              * 100
            )
        else:
          warnings.append(
            "CPU 监控指标缺失"
          )

        memory_used = metric.get(
          "memory_used_bytes"
        )

        memory_total = metric.get(
          "memory_total_bytes"
        )

        memory_percent = metric.get(
          "memory_usage_percent"
        )

        if memory_percent is None:
          warnings.append(
            "内存监控指标缺失"
          )

      result.append(
        NodeResource(
          name=node["name"],
          status=node["status"],

          cpu_capacity=node["cpu_capacity"],
          cpu_allocatable=node["cpu_allocatable"],
          cpu_usage_cores=cpu_cores,
          cpu_usage_percent=cpu_percent,
          cpu_allocatable_usage_percent=(
            cpu_allocatable_percent
          ),

          memory_capacity_bytes=(
            node["memory_capacity_bytes"]
          ),

          memory_allocatable_bytes=(
            node["memory_allocatable_bytes"]
          ),

          memory_used_bytes=memory_used,
          memory_total_bytes=memory_total,
          memory_usage_percent=memory_percent,

          instance=(
            metric.get("instance")
            if metric else None
          ),

          cpu_metric_available=(
            cpu_percent is not None
          ),

          memory_metric_available=(
            memory_percent is not None
          ),

          warnings=warnings,
        )
      )

    return result

  def get_cluster_summary(
    self,
  ) -> ClusterResourceSummary:
    """获取集群资源汇总。"""

    nodes = self.list_node_resources()

    ready_nodes = [
      node for node in nodes
      if node.status == "True"
    ]

    cpu_nodes = [
      node for node in nodes
      if node.cpu_usage_percent is not None
    ]

    memory_nodes = [
      node for node in nodes
      if node.memory_metric_available
    ]

    total_cpu = sum(
      node.cpu_capacity for node in nodes
    )

    used_cpu = sum(
      node.cpu_usage_cores or 0
      for node in cpu_nodes
    )

    measured_cpu = sum(
      node.cpu_capacity for node in cpu_nodes
    )

    total_memory = sum(
      node.memory_capacity_bytes for node in nodes
    )

    used_memory = sum(
      node.memory_used_bytes or 0
      for node in memory_nodes
    )

    measured_memory = sum(
      node.memory_total_bytes or 0
      for node in memory_nodes
    )

    cpu_percent = (
      used_cpu / measured_cpu * 100
      if measured_cpu > 0 else None
    )

    memory_percent = (
      used_memory / measured_memory * 100
      if measured_memory > 0 else None
    )

    high_cpu_nodes = [
      node.name for node in nodes
      if (
        node.cpu_usage_percent is not None
        and node.cpu_usage_percent
        >= self.HIGH_USAGE_THRESHOLD
      )
    ]

    high_memory_nodes = [
      node.name for node in nodes
      if (
        node.memory_usage_percent is not None
        and node.memory_usage_percent
        >= self.HIGH_USAGE_THRESHOLD
      )
    ]

    return ClusterResourceSummary(
      total_nodes=len(nodes),
      ready_nodes=len(ready_nodes),

      total_cpu_cores=total_cpu,
      used_cpu_cores=used_cpu,

      measured_cpu_capacity_cores=measured_cpu,

      total_memory_bytes=total_memory,
      used_memory_bytes=used_memory,
      measured_memory_total_bytes=measured_memory,

      cpu_usage_percent=cpu_percent,
      memory_usage_percent=memory_percent,

      high_cpu_nodes=high_cpu_nodes,
      high_memory_nodes=high_memory_nodes,
    )
