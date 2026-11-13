from typing import Any

from pydantic import BaseModel, Field


class MlBackendCreateRequest(BaseModel):
  """创建 Label Studio ML Backend 服务的请求体（Deployment + 同名 Service）。"""

  name: str = Field(description="Deployment 与 Service 的名称（两者同名）")
  namespace: str = Field("default", description="所在命名空间")
  image: str = Field(description="ML Backend 容器镜像")
  port: int = Field(9090, description="ML Backend 监听端口，同时作为 Service 与容器端口")
  replicas: int = Field(1, description="副本数")
  labels: dict[str, str] | None = Field(None, description="标签与 Service 选择器，默认 {app: name}")
  annotations: dict[str, str] | None = Field(None, description="Deployment 注解")
  command: list[str] | None = Field(None, description="容器启动命令")
  args: list[str] | None = Field(None, description="容器启动参数")
  env: dict[str, str] | None = Field(None, description="容器环境变量")
  node_selector: dict[str, str] | None = Field(None, description="节点选择器")
  resource_cpu: str | None = Field(None, description="CPU 请求/限制，如 500m")
  resource_memory: str | None = Field(None, description="内存请求/限制，如 512Mi")
  resource_gpu: str | None = Field(None, description="GPU 请求/限制，如 1")
  image_pull_policy: str = Field("IfNotPresent", description="镜像拉取策略")
  image_pull_secrets: list[str] | None = Field(None, description="镜像拉取密钥名称列表")
  volume_mount: list[dict[str, Any]] | None = Field(None, description="挂载配置，如 [{name, mountPath, subPath}]")
  working_dir: str | None = Field(None, description="容器工作目录")
  host_aliases: list[dict[str, Any]] | None = Field(None, description="hostAliases 配置，如 [{ip, hostnames}]")
  privileged: bool = Field(False, description="是否以特权模式运行")
  service_account: str | None = Field(None, description="Pod 使用的 ServiceAccount 名称")
  scheduler_name: str = Field("default-scheduler", description="调度器名称")
  health: dict[str, Any] | None = Field(None, description="健康检查配置（liveness/readiness/startup）")
  service_type: str = Field("ClusterIP", description="Service 类型：ClusterIP、NodePort、LoadBalancer")


class MlBackendDeployResponse(BaseModel):
  """ML Backend 部署结果。"""

  name: str = Field(description="ML Backend 名称")
  namespace: str = Field(description="所在命名空间")
  deployment: str = Field(description="已创建的 Deployment 名称")
  service: str = Field(description="已创建的同名 Service 名称")
  message: str = Field(description="操作结果说明")


class MlBackendDeleteResponse(BaseModel):
  """ML Backend 删除结果。"""

  name: str = Field(description="ML Backend 名称")
  namespace: str = Field(description="所在命名空间")
  message: str = Field(description="操作结果说明")
