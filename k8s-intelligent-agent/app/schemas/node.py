
from typing import Optional
from pydantic import BaseModel, Field

# Optional[float]：监控指标缺失时返回 None，不能伪造为 0。
# warnings：可以告诉 Agent 某个节点为什么缺少指标。
# cpu_usage_cores：返回实际使用 CPU 核数，而不只是百分比。
# cpu_allocatable_usage_percent：额外计算相对于可调度 CPU 的比例。

class NodeResource(BaseModel):
  """单个节点资源信息。"""
  name: str
  status: str

  cpu_capacity: float
  cpu_allocatable: float
  cpu_usage_cores: Optional[float] = None
  cpu_usage_percent: Optional[float] = None
  cpu_allocatable_usage_percent: Optional[float] = None

  memory_capacity_bytes: int
  memory_allocatable_bytes: int
  memory_used_bytes: Optional[int] = None
  memory_total_bytes: Optional[int] = None
  memory_usage_percent: Optional[float] = None

  instance: Optional[str] = None

  cpu_metric_available: bool = False
  memory_metric_available: bool = False

  warnings: list[str] = []


class ClusterResourceSummary(BaseModel):
  """集群资源汇总。"""

  total_nodes: int
  ready_nodes: int

  total_cpu_cores: float
  used_cpu_cores: float
  measured_cpu_capacity_cores: float

  total_memory_bytes: int
  used_memory_bytes: int
  measured_memory_total_bytes: int

  cpu_usage_percent: Optional[float] = None
  memory_usage_percent: Optional[float] = None

  high_cpu_nodes: list[str] = Field(default_factory=list)
  high_memory_nodes: list[str] = Field(default_factory=list)