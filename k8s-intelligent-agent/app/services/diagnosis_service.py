from app.schemas.diagnosis import PodInfo, ContainerStatus, DeploymentInfo, DeploymentCondition, PodDiagnosisEvidence, WorkloadDiagnosisReport


class DiagnosisService:
  def __init__(self, core_v1_api, app_v1_api):
    self.core_v1_api = core_v1_api
    self.app_v1_api = app_v1_api

  def _build_container_statuses(self, container_statuses) -> list[ContainerStatus]:
    """将 Kubernetes 容器状态转换为 ContainerStatus。"""
    result = []
    for container in container_statuses or []:
      state = "Unknown"
      reason = None
      message = None

      if container.state:
        if container.state.running:
          state = "Running"

        elif container.state.waiting:
          state = "Waiting"
          reason = container.state.waiting.reason
          message = container.state.waiting.message

        elif container.state.terminated:
          state = "Terminated"
          reason = container.state.terminated.reason
          message = container.state.terminated.message

      result.append(
        ContainerStatus(
          name=container.name,
          ready=container.ready,
          restart_count=container.restart_count or 0,
          state=state,
          reason=reason,
          message=message,
        )
      )

    return result

  def _build_pod_info(self, pod) -> PodInfo:
    """将 Kubernetes Pod 对象转换为 PodInfo。"""
    # container_statuses = pod.status.container_statuses or []
    # containers = []
    # for container in container_statuses:
    #   # 默认状态
    #   state = "Unknown"
    #   reason = None
    #   message = None

    #   if container.state:
    #     if container.state.running:
    #       state = "Running"
    #     elif container.state.waiting:
    #       state = "Waiting"
    #       reason = container.state.waiting.reason
    #       message = container.state.waiting.message
    #     elif container.state.terminated:
    #       state = "Terminated"
    #       reason = container.state.terminated.reason
    #       message = container.state.terminated.message

    #   containers.append(
    #     ContainerStatus(
    #       name=container.name,
    #       ready=container.ready,
    #       restart_count=container.restart_count or 0,
    #       state=state,
    #       reason=reason,
    #       message=message,
    #     )
    #   )
    # restart_count = sum(container.restart_count or 0 for container in containers)

    # # Pod 状态
    # phase = pod.status.phase
    # status = phase or "Unknown"

    # # 进一步识别容器异常状态
    # for container in containers:
    #   if container.reason in (
    #     "CrashLoopBackOff",
    #     "ImagePullBackOff",
    #     "ErrImagePull",
    #     "CreateContainerConfigError",
    #     "CreateContainerError",
    #   ):
    #     status = container.reason
    #     break
    # return PodInfo(
    #   name=pod.metadata.name,
    #   namespace=pod.metadata.namespace,
    #   status=status,
    #   phase=phase,
    #   node_name=pod.spec.node_name,
    #   pod_ip=pod.status.pod_ip,
    #   restart_count=restart_count,
    #   containers=containers,
    #   creation_timestamp=(
    #     pod.metadata.creation_timestamp.isoformat()
    #     if pod.metadata.creation_timestamp
    #     else None
    #   ),
    # )
    # 普通容器状态
    containers = self._build_container_statuses(pod.status.container_statuses)
    # Init Container 状态
    init_containers = self._build_container_statuses(pod.status.init_container_statuses)

    # 汇总重启次数
    restart_count = sum(container.restart_count or 0 for container in containers + init_containers)
    phase = pod.status.phase or "Unknown"
    status = phase
    # 优先识别 Init Container 异常
    all_containers = init_containers + containers
    abnormal_reasons = {
      "CrashLoopBackOff",
      "ImagePullBackOff",
      "ErrImagePull",
      "CreateContainerConfigError",
      "CreateContainerError",
    }
    for container in all_containers:
      if container.reason in abnormal_reasons:
        status = container.reason
        break

    return PodInfo(
      name=pod.metadata.name,
      namespace=pod.metadata.namespace,
      status=status,
      phase=phase,
      node_name=pod.spec.node_name,
      pod_ip=pod.status.pod_ip,
      restart_count=restart_count,
      containers=containers,
      init_containers=init_containers,
      creation_timestamp=(
        pod.metadata.creation_timestamp.isoformat()
        if pod.metadata.creation_timestamp
        else None
      ),
    )


  def list_pods(self, namespace: str) -> list[PodInfo]:
    """查询 Namespace 下所有 Pod。"""
    response = self.core_v1_api.list_namespaced_pod(namespace=namespace)

    # result = []
    # for pod in response.items:
    #   containers = []
    #   restart_count = 0
    #   container_statuses = (pod.status.container_statuses or [])
    #   for container in container_statuses:
    #     restart_count += container.restart_count
    #     state = "Unknown"
    #     reason = None
    #     message = None
    #     if container.state.running:
    #       state = "Running"
    #     elif container.state.waiting:
    #       state = "Waiting"
    #       reason = container.state.waiting.reason
    #       message = container.state.waiting.message

    #     elif container.state.terminated:
    #       state = "Terminated"
    #       reason = container.state.terminated.reason
    #       message = container.state.terminated.message

    #     containers.append(
    #       ContainerStatus(
    #         name=container.name,
    #         ready=container.ready,
    #         restart_count=container.restart_count,
    #         state=state,
    #         reason=reason,
    #         message=message,
    #       )
    #     )

    #   pod_info = PodInfo(
    #     name=pod.metadata.name,
    #     namespace=pod.metadata.namespace,
    #     status=pod.status.phase or "Unknown",
    #     phase=pod.status.phase,
    #     node_name=pod.spec.node_name,
    #     pod_ip=pod.status.pod_ip,
    #     restart_count=restart_count,
    #     containers=containers,
    #     creation_timestamp=(
    #       pod.metadata.creation_timestamp.isoformat()
    #       if pod.metadata.creation_timestamp
    #       else None
    #     ),
    #   )

    #   result.append(pod_info)

    # return result
    return [self._build_pod_info(pod) for pod in response.items]

  def list_pod_events(self, namespace: str, pod_name: str) -> list[dict]:
    """查询指定 Pod 的 Events。"""
    response = self.core_v1_api.list_namespaced_event(
      namespace=namespace,
      field_selector=(
        f"involvedObject.name={pod_name},"
        "involvedObject.kind=Pod"
      )
    )

    result = []
    for event in response.items:
      result.append({
        "type": event.type,
        "reason": event.reason,
        "message": event.message,
        "count": event.count,
        "involved_object": {
          "kind": event.involved_object.kind,
          "name": event.involved_object.name,
          "namespace": event.involved_object.namespace,
        },
        "first_timestamp": (
          event.first_timestamp.isoformat()
          if event.first_timestamp else None
        ),
        "last_timestamp": (
          event.last_timestamp.isoformat()
          if event.last_timestamp else None
        ),
        "source": (
          event.source.component
          if event.source else None
        ),
      })
    
     # 按最近发生时间倒序排列
    result.sort(
      key=lambda item: item["last_timestamp"] or "",
      reverse=True,
    )
    return result

  def get_pod_logs(self, namespace: str, pod_name: str, container: str | None = None, previous: bool = False, tail_lines: int = 100) -> str:
    """获取 Pod 容器日志。"""
    return self.core_v1_api.read_namespaced_pod_log(
      namespace=namespace,
      name=pod_name,
      container=container,
      previous=previous,
      tail_lines=tail_lines,
      timestamps=True,
    )

  # deploy
  def get_deployment_status(self, namespace: str, deployment_name: str) -> DeploymentInfo:
    """查询 Kubernetes Deployment 状态。"""
    deployment = self.app_v1_api.read_namespaced_deployment(
      name=deployment_name,
      namespace=namespace,
    )

    spec = deployment.spec
    status = deployment.status

    conditions = []
    for condition in status.conditions or []:
      conditions.append(
        DeploymentCondition(
          type=condition.type,
          status=condition.status,
          reason=condition.reason,
          message=condition.message,
          last_update_time=(
            condition.last_update_time.isoformat()
            if condition.last_update_time
            else None
          ),
          last_transition_time=(
            condition.last_transition_time.isoformat()
            if condition.last_transition_time
            else None
          ),
        )
      )
    return DeploymentInfo(
      name=deployment.metadata.name,
      namespace=deployment.metadata.namespace,

      desired_replicas=spec.replicas or 1,
      replicas=status.replicas or 0,
      ready_replicas=status.ready_replicas or 0,
      available_replicas=status.available_replicas or 0,
      updated_replicas=status.updated_replicas or 0,
      unavailable_replicas=status.unavailable_replicas or 0,

      observed_generation=status.observed_generation or 0,
      generation=deployment.metadata.generation or 0,

      strategy=(spec.strategy.type if spec.strategy else None),

      conditions=conditions,
    )

  def list_deployment_pods(self, namespace: str, deployment_name: str) -> list[PodInfo]:
    """查询 Deployment 管理的 Pod。"""
    deployment = self.app_v1_api.read_namespaced_deployment(
      name=deployment_name,
      namespace=namespace,
    )

    selector = deployment.spec.selector.match_labels or {}
    if not selector:
      raise ValueError(f"Deployment {deployment_name} has no match_labels selector")
    label_selector = ",".join([f"{k}={v}" for k, v in selector.items()])
    response = self.core_v1_api.list_namespaced_pod(
      namespace=namespace,
      label_selector=label_selector,
    )
    return [self._build_pod_info(pod) for pod in response.items]

  def collect_pod_evidence(self, namespace: str, pod: PodInfo) -> PodDiagnosisEvidence:
    """收集单个 Pod 的诊断证据。"""
    evidence = PodDiagnosisEvidence(
      pod=pod,
    )
    # 1. 收集 Pod Events
    try:
      evidence.events = self.list_pod_events(namespace=namespace, pod_name=pod.name)
    except Exception as exc:
      evidence.collection_errors.append(f"获取 Pod Events 失败: {exc}")
    
    # 2. 收集发生过重启的容器的 Previous Logs
    all_containers = (
      pod.init_containers + pod.containers
    )

    for container in all_containers:
      if container.restart_count <= 0:
        continue
      try:
        logs = self.get_pod_logs(
          namespace = namespace,
          pod_name = pod.name,
          container = container.name,
          previous = True,
          tail_lines = 100,
        )
        evidence.previous_logs[container.name] = logs
      except Exception as exc:
        evidence.collection_errors.append(f"获取容器 {container.name} 的 Previous Logs 失败: {exc}")

    return evidence

  # Deployment 综合证据采集
  def diagnose_deployment(self, namespace: str, deployment_name: str) -> WorkloadDiagnosisReport:
    """收集 Deployment 及其关联 Pod 的诊断证据。"""
    # 1. 获取deployment 状态
    deployment = self.get_deployment_status(namespace=namespace, deployment_name=deployment_name)
    # 2. 获取 Deployment 关联的 Pod
    pods = self.list_deployment_pods(namespace=namespace, deployment_name=deployment_name)
    # 3. 收集每个 Pod 的诊断证据
    pod_evidences = []
    for pod in pods:
      pod_evidences.append(self.collect_pod_evidence(namespace=namespace, pod=pod))

    # 4. 组装综合报告
    return WorkloadDiagnosisReport(
      namespace=namespace,
      deployment=deployment,
      pods=pod_evidences,
    )