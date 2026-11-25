# 6 类规则：
# 1. Deployment 副本不可用。
# 2. Pod 处于 Failed 或 Unknown 状态。
# 3. 容器处于异常等待状态。
# 4. 容器重启次数过多。
# 5. Kubernetes Warning Events。
# 6. Previous Logs 中存在常见故障关键词。


import re

from app.schemas.diagnosis import (
  DiagnosisFinding,
  DiagnosisResult,
  WorkloadDiagnosisReport,
)


class DiagnosisRuleEngine:
  """Kubernetes Workload 规则诊断引擎。"""

  # 单个容器累计重启次数达到该值时，产生 Warning
  RESTART_WARNING_THRESHOLD = 5

  # 常见异常容器等待原因
  WAITING_ERROR_REASONS = {
    "CrashLoopBackOff",
    "ImagePullBackOff",
    "ErrImagePull",
    "CreateContainerConfigError",
    "CreateContainerError",
    "RunContainerError",
  }

  # Previous Logs 中常见故障关键词
  LOG_PATTERNS = {
    "DATABASE_CONNECTION_REFUSED": {
      "pattern": r"connection refused",
      "title": "历史数据库连接被拒绝",
      "message": "容器历史日志中出现数据库连接被拒绝的错误。",
      "recommendations": [
        "检查数据库 Pod 是否正常运行。",
        "检查数据库 Service 和 Endpoints 是否正确。",
        "检查数据库监听端口及网络连通性。",
      ],
    },
    "DATABASE_CONNECTION_TIMEOUT": {
      "pattern": r"connection timed out|connect timeout",
      "title": "历史数据库连接超时",
      "message": "容器历史日志中出现数据库连接超时。",
      "recommendations": [
        "检查数据库服务是否可达。",
        "检查集群网络、NetworkPolicy 和防火墙规则。",
        "检查数据库负载及连接数。",
      ],
    },
    "OUT_OF_MEMORY": {
      "pattern": r"out of memory|oomkilled|killed process",
      "title": "日志中存在内存不足线索",
      "message": "容器日志中出现内存不足或进程被终止的相关信息。",
      "recommendations": [
        "检查容器内存 requests 和 limits。",
        "检查 Pod 是否发生 OOMKilled。",
        "结合节点内存使用情况判断是否存在内存压力。",
      ],
    },
    "APPLICATION_EXCEPTION": {
      "pattern": r"traceback \(most recent call last\)|uncaught exception",
      "title": "应用发生异常",
      "message": "容器历史日志中出现未捕获异常或 Python Traceback。",
      "recommendations": [
        "检查异常堆栈中的第一处业务代码错误。",
        "结合异常发生时间检查配置、依赖服务和最近变更。",
      ],
    },
  }

  def diagnose(
    self,
    report: WorkloadDiagnosisReport,
  ) -> DiagnosisResult:
    """根据 Deployment 和 Pod 证据生成规则诊断结果。"""

    findings: list[DiagnosisFinding] = []

    self._check_deployment(report, findings)

    for pod_evidence in report.pods:
      self._check_pod(pod_evidence, findings)

    health_status = self._calculate_health_status(
      findings,
      has_pods=bool(report.pods),
    )

    risk_status = self._calculate_risk_status(findings)

    return DiagnosisResult(
      namespace=report.namespace,
      workload_name=report.deployment.name,
      health_status=health_status,
      risk_status=risk_status,
      findings=findings,
    )

  def _check_deployment(self, report, findings):
    deployment = report.deployment

    if deployment.unavailable_replicas > 0:
      findings.append(
        DiagnosisFinding(
          code="DEPLOYMENT_UNAVAILABLE",
          title="Deployment 存在不可用副本",
          severity="critical",
          category="current",
          message=(
            f"Deployment 当前有 "
            f"{deployment.unavailable_replicas} 个不可用副本。"
          ),
          evidence=[
            f"desired_replicas={deployment.desired_replicas}",
            f"replicas={deployment.replicas}",
            f"ready_replicas={deployment.ready_replicas}",
            f"available_replicas={deployment.available_replicas}",
            f"unavailable_replicas={deployment.unavailable_replicas}",
          ],
          recommendations=[
            "检查关联 Pod 的状态。",
            "检查 Pod Events 和容器日志。",
            "检查 Deployment 副本数及调度情况。",
          ],
        )
      )

    for condition in deployment.conditions:
      if condition.status.lower() == "false":
        findings.append(
          DiagnosisFinding(
            code=f"DEPLOYMENT_CONDITION_{condition.type.upper()}_FALSE",
            title=f"Deployment Condition {condition.type} 异常",
            severity="warning",
            category="current",
            message=condition.message or condition.reason or (
              f"Deployment Condition {condition.type} 为 False。"
            ),
            evidence=[
              f"type={condition.type}",
              f"status={condition.status}",
              f"reason={condition.reason}",
            ],
            recommendations=[
              "检查 Deployment Condition 的 reason 和 message。",
              "结合关联 Pod 状态进一步排查。",
            ],
          )
        )

  def _check_pod(self, pod_evidence, findings):
    pod = pod_evidence.pod

    if pod.phase in {"Failed", "Unknown"}:
      findings.append(
        DiagnosisFinding(
          code="POD_ABNORMAL_PHASE",
          title="Pod 处于异常阶段",
          severity="critical",
          category="current",
          message=f"Pod {pod.name} 当前阶段为 {pod.phase}。",
          evidence=[
            f"pod={pod.name}",
            f"phase={pod.phase}",
            f"node={pod.node_name}",
          ],
          recommendations=[
            "执行 kubectl describe pod 查看详细状态。",
            "检查 Pod Events 和容器日志。",
          ],
        )
      )

    all_containers = pod.init_containers + pod.containers

    for container in all_containers:
      self._check_container(
        pod_name=pod.name,
        container=container,
        findings=findings,
      )

    self._check_events(
      pod_name=pod.name,
      events=pod_evidence.events,
      findings=findings,
    )

    self._check_previous_logs(
      pod_name=pod.name,
      logs=pod_evidence.previous_logs,
      findings=findings,
    )

    for error in pod_evidence.collection_errors:
      findings.append(
        DiagnosisFinding(
          code="EVIDENCE_COLLECTION_FAILED",
          title="部分诊断证据采集失败",
          severity="info",
          category="unknown",
          message=error,
          evidence=[f"pod={pod.name}"],
          recommendations=[
            "检查 Kubernetes API 返回的错误信息。",
            "确认对应容器或事件数据是否仍然存在。",
          ],
        )
      )

  def _check_container(self, pod_name, container, findings):
    if container.restart_count >= self.RESTART_WARNING_THRESHOLD:
      findings.append(
        DiagnosisFinding(
          code="CONTAINER_HIGH_RESTART_COUNT",
          title="容器历史重启次数较多",
          severity="warning",
          category="historical",
          message=(
            f"Pod {pod_name} 中容器 {container.name} "
            f"累计重启 {container.restart_count} 次。"
          ),
          evidence=[
            f"pod={pod_name}",
            f"container={container.name}",
            f"restart_count={container.restart_count}",
            f"current_state={container.state}",
            f"ready={container.ready}",
          ],
          recommendations=[
            "检查容器 Previous Logs。",
            "检查 Pod Events 中的 BackOff 和 Unhealthy 事件。",
            "结合重启时间分析是否存在依赖服务故障。",
          ],
        )
      )

    state = (container.state or "").lower()
    reason = container.reason or ""

    if state == "waiting" and reason in self.WAITING_ERROR_REASONS:
      findings.append(
        DiagnosisFinding(
          code="CONTAINER_WAITING_ERROR",
          title="容器处于异常等待状态",
          severity="critical",
          category="current",
          message=(
            f"容器 {container.name} 当前等待原因：{reason}。"
          ),
          evidence=[
            f"pod={pod_name}",
            f"container={container.name}",
            f"reason={reason}",
            f"message={container.message}",
          ],
          recommendations=[
            "检查容器镜像、启动命令和配置。",
            "检查 Pod Events 中的详细错误。",
          ],
        )
      )

  def _check_events(self, pod_name, events, findings):
    for event in events:
      event_type = str(event.get("type") or "")
      reason = str(event.get("reason") or "")
      message = str(event.get("message") or "")

      if event_type.lower() != "warning":
        continue

      findings.append(
        DiagnosisFinding(
          code=f"K8S_EVENT_{reason.upper() or 'WARNING'}",
          title=f"Kubernetes Warning Event: {reason}",
          severity="warning",
          category="event",
          message=message or reason,
          evidence=[
            f"pod={pod_name}",
            f"reason={reason}",
            f"count={event.get('count', 1)}",
            f"last_timestamp={event.get('last_timestamp')}",
          ],
          recommendations=[
            "结合 Event 时间和 Pod 当前状态判断是否仍然存在。",
            "根据 Event reason 检查调度、网络、存储或容器启动问题。",
          ],
        )
      )

  def _check_previous_logs(self, pod_name, logs, findings):
    for container_name, content in logs.items():
      if not content:
        continue

      for code, rule in self.LOG_PATTERNS.items():
        if not re.search(
          rule["pattern"],
          content,
          flags=re.IGNORECASE,
        ):
          continue

        findings.append(
          DiagnosisFinding(
            code=code,
            title=rule["title"],
            severity="warning",
            category="historical",
            message=(
              f"Pod {pod_name} 的容器 {container_name} "
              f"Previous Logs 中发现相关错误。"
              f"{rule['message']}"
            ),
            evidence=[
              f"pod={pod_name}",
              f"container={container_name}",
              f"matched_pattern={rule['pattern']}",
            ],
            recommendations=rule["recommendations"],
          )
        )

  @staticmethod
  def _calculate_health_status(findings, has_pods):
    """只根据当前异常计算健康状态。"""

    if not has_pods:
      return "unknown"

    current_findings = [
      item for item in findings
      if item.category == "current"
    ]

    if any(
      item.severity == "critical"
      for item in current_findings
    ):
      return "critical"

    if any(
      item.severity == "warning"
      for item in current_findings
    ):
      return "warning"

    # 部分证据采集失败，无法充分确认当前状态
    if any(
      item.code == "EVIDENCE_COLLECTION_FAILED"
      for item in findings
    ):
      return "unknown"

    return "healthy"

  @staticmethod
  def _calculate_risk_status(findings):
    """根据历史故障和事件线索计算风险状态。"""

    risk_categories = {"historical", "event"}
    risk_findings = [
      item for item in findings
      if item.category in risk_categories
    ]

    if risk_findings:
      return "elevated"

    return "none"

