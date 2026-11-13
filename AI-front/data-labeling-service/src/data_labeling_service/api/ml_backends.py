from fastapi import APIRouter, Body, HTTPException, Path, Query, status

from ..k8s.k_client import get_k8s_client
from ..schemas.ml_backends import (
  MlBackendCreateRequest,
  MlBackendDeleteResponse,
  MlBackendDeployResponse,
)

router = APIRouter(prefix="/api/ml-backends", tags=["ml-backends"])


@router.post("", response_model=MlBackendDeployResponse, status_code=status.HTTP_201_CREATED)
async def create_ml_backend(
  payload: MlBackendCreateRequest = Body(description="ML Backend 部署配置，对应 Deployment 与同名 Service"),
) -> MlBackendDeployResponse:
  """
  部署一个 Label Studio ML Backend 服务：创建一个 Deployment 以及与其同名的 Service。

  - `name` 同时作为 Deployment 与 Service 的名称
  - `port` 为 ML Backend 监听端口，默认 9090，同时作为 Service 与容器端口
  - 部署完成后，可用 `http://<service>.<namespace>:<port>` 作为模型服务地址连接到项目
  """
  try:
    k8s = get_k8s_client()
    k8s.create_ml_backend(
      namespace=payload.namespace,
      name=payload.name,
      image=payload.image,
      port=payload.port,
      replicas=payload.replicas,
      labels=payload.labels,
      annotations=payload.annotations,
      command=payload.command,
      args=payload.args,
      env=payload.env,
      node_selector=payload.node_selector,
      resource_memory=payload.resource_memory,
      resource_cpu=payload.resource_cpu,
      resource_gpu=payload.resource_gpu,
      image_pull_policy=payload.image_pull_policy,
      image_pull_secrets=payload.image_pull_secrets,
      volume_mount=payload.volume_mount,
      working_dir=payload.working_dir,
      hostAliases=payload.host_aliases,
      privileged=payload.privileged,
      accounts=payload.service_account,
      scheduler_name=payload.scheduler_name,
      health=payload.health,
      service_type=payload.service_type,
    )
    return MlBackendDeployResponse(
      name=payload.name,
      namespace=payload.namespace,
      deployment=payload.name,
      service=payload.name,
      message=f"ML Backend {payload.name} 创建成功",
    )
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"创建 ML Backend 失败: {e}")


@router.delete("/{name}", response_model=MlBackendDeleteResponse)
async def delete_ml_backend(
  name: str = Path(description="ML Backend 名称（Deployment 与 Service 同名）"),
  namespace: str = Query("default", description="所在命名空间"),
) -> MlBackendDeleteResponse:
  """删除 ML Backend：同时删除 Deployment 与同名 Service。"""
  try:
    k8s = get_k8s_client()
    k8s.delete_ml_backend(namespace=namespace, name=name)
    return MlBackendDeleteResponse(
      name=name,
      namespace=namespace,
      message=f"ML Backend {name} 已删除（Deployment 与 Service）",
    )
  except Exception as e:
    raise HTTPException(status_code=500, detail=f"删除 ML Backend 失败: {e}")
