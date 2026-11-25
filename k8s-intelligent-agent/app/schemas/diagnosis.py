from pydantic import BaseModel, Field

class ContainerStatus(BaseModel):
  """容器运行状态。"""
  name: str
  ready: bool
  restart_count: int = 0
  state: str
  reason: str | None = None
  message: str | None = None


class PodInfo(BaseModel):
  """Pod 基础信息。"""
  name: str
  namespace: str
  status: str
  phase: str | None = None
  node_name: str | None = None
  pod_ip: str | None = None
  restart_count: int = 0
  containers: list[ContainerStatus] = Field(default_factory=list)
  # 初始化容器
  # NAME        READY   STATUS
  # my-app      0/1     Init:CrashLoopBackOff
  init_containers: list[ContainerStatus] = Field(default_factory=list)
  creation_timestamp: str | None = None


class DeploymentCondition(BaseModel):
  type: str
  status: str
  reason: str | None = None
  message: str | None = None
  last_update_time: str | None = None
  last_transition_time: str | None = None


class DeploymentInfo(BaseModel):
  name: str
  namespace: str

  desired_replicas: int = 1 # 期望副本数
  replicas: int = 0
  ready_replicas: int = 0
  available_replicas: int = 0
  updated_replicas: int = 0
  unavailable_replicas: int = 0

  observed_generation: int = 0
  generation: int = 0

  strategy: str | None = None

  conditions: list[DeploymentCondition] = Field(default_factory=list)
  

# WorkloadDiagnosisReport：收集到的完整诊断数据。
# DiagnosisResult：基于数据形成的诊断结论。

# 综合诊断的数据结构
# class DiagnosisResult(BaseModel):
#   namespace: str
#   target: str

#   health_status: str
#   severity: str

#   summary: str

#   findings: list[str] = Field(default_factory=list)
#   recommendations: list[str] = Field(default_factory=list)

# 完整诊断报告
# class WorkloadDiagnosisReport(BaseModel):
#   namespace: str
#   deployment: DeploymentInfo

#   pods: list[PodInfo] = Field(default_factory=list)

#   diagnosis: DiagnosisResult


class PodDiagnosisEvidence(BaseModel):
  """单个 Pod 的诊断证据。"""

  pod: PodInfo

  # Pod 关联的 Kubernetes Events
  events: list[dict] = Field(default_factory=list)

  # 容器上一次运行的日志，key 为容器名称
  previous_logs: dict[str, str] = Field(default_factory=dict)

  # 数据采集过程中发生的错误
  collection_errors: list[str] = Field(default_factory=list)


class WorkloadDiagnosisReport(BaseModel):
  """Deployment 维度的综合诊断报告。"""

  namespace: str
  deployment: DeploymentInfo

  pods: list[PodDiagnosisEvidence] = Field(
    default_factory=list
  )


class DiagnosisFinding(BaseModel):
  """一条规则诊断发现。"""

  code: str
  title: str

  # critical / warning / info
  severity: str

  # 当前问题还是历史问题
  category: str = "current"

  message: str

  # 触发规则的证据
  evidence: list[str] = Field(default_factory=list)

  # 建议采取的操作
  recommendations: list[str] = Field(default_factory=list)


class DiagnosisResult(BaseModel):
  """规则诊断结果。"""

  namespace: str
  workload_name: str

  # 当前健康状态：healthy / warning / critical / unknown
  health_status: str = "unknown"

  # 历史风险：none / elevated
  risk_status: str = "none"

  findings: list[DiagnosisFinding] = Field(default_factory=list)