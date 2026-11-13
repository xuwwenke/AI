from fastapi import FastAPI, HTTPException, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import requests
import time
import uuid
import re

import os

from datetime import datetime

from database import SessionLocal
from service.backup_service import BackupService
from service.snapshot_service import SnapshotService


app = FastAPI()


class ErrorResponse(BaseModel):
    code: int
    message: str
    detail: str = None
    timestamp: datetime = None

    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "code": exc.status_code,
            "message": exc.detail,
            "detail": None,
            "timestamp": datetime.now().isoformat()
        }
    )


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc):
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "code": status.HTTP_422_UNPROCESSABLE_ENTITY,
            "message": "请求参数校验失败",
            "detail": exc.errors(),
            "timestamp": datetime.now().isoformat()
        }
    )


@app.exception_handler(Exception)
async def generic_exception_handler(request, exc):
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "code": status.HTTP_500_INTERNAL_SERVER_ERROR,
            "message": "服务器内部错误",
            "detail": str(exc),
            "timestamp": datetime.now().isoformat()
        }
    )



TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token"
CA_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"

K8S_HOST = "https://kubernetes.default.svc"

MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "http://minio.kubeflow.svc.cluster.local:9000")
AWS_ACCESS_KEY_ID = os.getenv("AWS_ACCESS_KEY_ID", "minio")
AWS_SECRET_ACCESS_KEY = os.getenv("AWS_SECRET_ACCESS_KEY", "minio123")
KOPIA_PASSWORD = os.getenv("KOPIA_PASSWORD", "KopiaRepo@2026")


GROUP = "kubeflow.org"
VERSION = "v1alpha1"
PLURAL = "pvcviewers"


def get_token():
    with open(TOKEN_PATH, "r") as f:
        return f.read().strip()


def get_headers():
    return {
        "Authorization": f"Bearer {get_token()}",
        "Content-Type": "application/json"
    }


class PVCViewerCreate(BaseModel):
    namespace: str
    pvc_name: str


class PVCViewerDelete(BaseModel):
    namespace: str
    name: str


# pvc备份
class PVCBackupRequest(BaseModel):
    namespace: str
    pvc_name: str


# pvc恢复
class PVCRestoreRequest(BaseModel):
    namespace: str
    pvc_name: str
    snapshot_id: str
    source_pvc_name: str = None


class SnapshotDeleteRequest(BaseModel):
    namespace: str
    pvc_name: str
    snapshot_id: str

@app.get("/")
def health():
    return {"status": "ok"}


def wait_pvcviewer_deleted(namespace, name, timeout=60):

    viewer_url = (
        f"{K8S_HOST}/apis/{GROUP}/{VERSION}/namespaces/"
        f"{namespace}/{PLURAL}/{name}"
    )

    for _ in range(timeout):

        resp = requests.get(
            viewer_url,
            headers=get_headers(),
            verify=CA_PATH,
            timeout=10
        )

        if resp.status_code == 404:
            return True

        time.sleep(1)

    return False


def wait_pods_deleted(namespace, pvcviewer_name, timeout=60):

    label_selector = (
        f"app.kubernetes.io/instance=pvcviewer-{pvcviewer_name}"
    )

    pods_url = (
        f"{K8S_HOST}/api/v1/namespaces/{namespace}/pods"
        f"?labelSelector={label_selector}"
    )

    for _ in range(timeout):

        resp = requests.get(
            pods_url,
            headers=get_headers(),
            verify=CA_PATH,
            timeout=10
        )

        if resp.status_code != 200:
            time.sleep(1)
            continue

        items = resp.json().get("items", [])

        active_pods = [
            pod for pod in items
            if not pod["metadata"].get("deletionTimestamp")
        ]

        if len(active_pods) == 0:
            return True

        time.sleep(1)

    return False


def wait_pod_ready(namespace, pvcviewer_name, timeout=180):

    label_selector = (
        f"app.kubernetes.io/instance=pvcviewer-{pvcviewer_name}"
    )

    pods_url = (
        f"{K8S_HOST}/api/v1/namespaces/{namespace}/pods"
        f"?labelSelector={label_selector}"
    )

    for _ in range(timeout):

        resp = requests.get(
            pods_url,
            headers=get_headers(),
            verify=CA_PATH,
            timeout=10
        )

        if resp.status_code != 200:
            time.sleep(1)
            continue

        items = resp.json().get("items", [])

        for pod in items:

            metadata = pod.get("metadata", {})
            status = pod.get("status", {})

            if metadata.get("deletionTimestamp"):
                continue

            if status.get("phase") != "Running":
                continue

            conditions = status.get("conditions", [])

            ready = False

            for condition in conditions:

                if (
                    condition.get("type") == "Ready"
                    and condition.get("status") == "True"
                ):
                    ready = True
                    break

            if not ready:
                continue

            container_statuses = status.get(
                "containerStatuses",
                []
            )

            ready_count = sum(
                1
                for c in container_statuses
                if c.get("ready")
            )

            total_count = len(container_statuses)

            return {
                "name": metadata.get("name"),
                "phase": status.get("phase"),
                "ready": f"{ready_count}/{total_count}",
                "pod_ip": status.get("podIP"),
                "host_ip": status.get("hostIP"),
                "labels": metadata.get("labels", {})
            }

        time.sleep(1)

    return None


def wait_job_completed(namespace, job_name, timeout=600):

    job_url = (
        f"{K8S_HOST}/apis/batch/v1/"
        f"namespaces/{namespace}/jobs/{job_name}"
    )

    for _ in range(timeout):

        resp = requests.get(
            job_url,
            headers=get_headers(),
            verify=CA_PATH,
            timeout=10
        )

        if resp.status_code != 200:
            time.sleep(1)
            continue

        status = resp.json().get(
            "status",
            {}
        )

        if status.get("succeeded"):
            return True

        if status.get("failed"):
            raise Exception(
                f"Backup Job {job_name} failed"
            )

        time.sleep(1)

    raise Exception(
        f"Backup Job {job_name} timeout"
    )

def get_job_pod(namespace, job_name):

    url = (
        f"{K8S_HOST}/api/v1/namespaces/"
        f"{namespace}/pods"
        f"?labelSelector=batch.kubernetes.io/job-name={job_name}"
    )

    resp = requests.get(
        url,
        headers=get_headers(),
        verify=CA_PATH
    )

    items = resp.json().get(
        "items",
        []
    )

    if not items:
        return None

    return items[0]["metadata"]["name"]

def get_pod_log(namespace, pod_name):

    url = (
        f"{K8S_HOST}/api/v1/namespaces/"
        f"{namespace}/pods/{pod_name}/log"
    )

    resp = requests.get(
        url,
        headers=get_headers(),
        verify=CA_PATH
    )

    if resp.status_code != 200:
        raise Exception(
            "read pod log failed"
        )

    return resp.text

def parse_snapshot_id(log_text):

    match = re.search(
        r"SNAPSHOT_ID=(.+)",
        log_text
    )

    if not match:
        return None

    return match.group(1).strip()


def create_restore_job(namespace, pvc_name, snapshot_id, source_pvc_name=None):
    repo_pvc_name = source_pvc_name if source_pvc_name else pvc_name

    job_name = f"restore-{pvc_name}-{str(uuid.uuid4())[:8]}"

    job_body = {
        "apiVersion": "batch/v1",
        "kind": "Job",
        "metadata": {
            "name": job_name,
            "namespace": namespace
        },
        "spec": {
            "backoffLimit": 0,
            "ttlSecondsAfterFinished": 60,

            "template": {
                "spec": {
                    "restartPolicy": "Never",

                    "containers": [
                        {
                            "name": "restore",
                            "image": "docker-registry:81/harbor-ailabx/kopia:0.23.0",

                            "resources": {
                                "requests": {
                                    "cpu": "100m",
                                    "memory": "256Mi"
                                },
                                "limits": {
                                    "cpu": "1",
                                    "memory": "1Gi"
                                }
                            },

                            # "envFrom": [
                            #     {
                            #         "secretRef": {
                            #             "name": "kopia-secret"
                            #         }
                            #     }
                            # ],

                            "env": [
                                {
                                    "name": "PVC_NAME",
                                    "value": pvc_name
                                },
                                {
                                    "name": "PVC_NAMESPACE",
                                    "value": namespace
                                },
                                {
                                    "name": "SNAPSHOT_ID",
                                    "value": snapshot_id
                                },
                                {
                                    "name": "REPO_PVC_NAME",
                                    "value": repo_pvc_name
                                },
                                {
                                    "name": "MINIO_ENDPOINT",
                                    "value": MINIO_ENDPOINT
                                },
                                {
                                    "name": "AWS_ACCESS_KEY_ID",
                                    "value": AWS_ACCESS_KEY_ID
                                },
                                {
                                    "name": "AWS_SECRET_ACCESS_KEY",
                                    "value": AWS_SECRET_ACCESS_KEY
                                },
                                {
                                    "name": "KOPIA_PASSWORD",
                                    "value": KOPIA_PASSWORD
                                }
                            ],

                            "command": [
                                "/bin/sh",
                                "-c",
                                """
set -e

echo "================================="
echo "Kopia Restore -> /data/restore"
echo "================================="

REPO_PREFIX="${PVC_NAMESPACE}/${REPO_PVC_NAME}/"
RESTORE_PVC="${PVC_NAME}"

echo "Namespace=${PVC_NAMESPACE}"
echo "Source PVC=${REPO_PVC_NAME}"
echo "Restore PVC=${RESTORE_PVC}"
echo "Snapshot=${SNAPSHOT_ID}"

echo "Connecting repository with prefix: ${REPO_PREFIX}"

if kopia repository connect s3 \\
  --bucket=kopia-backup \\
  --prefix="${REPO_PREFIX}" \\
  --endpoint="${MINIO_ENDPOINT}" \\
  --access-key="${AWS_ACCESS_KEY_ID}" \\
  --secret-access-key="${AWS_SECRET_ACCESS_KEY}" \\
  --password="${KOPIA_PASSWORD}" \\
  --disable-tls \
  --override-hostname="backup-${PVC_NAME}"
then
  echo "Repository connected"
else
  echo "Repository not found, creating new one..."
  
  kopia repository create s3 \\
    --bucket=kopia-backup \\
    --prefix="${REPO_PREFIX}" \\
    --endpoint="${MINIO_ENDPOINT}" \\
    --access-key="${AWS_ACCESS_KEY_ID}" \\
    --secret-access-key="${AWS_SECRET_ACCESS_KEY}" \\
    --password="${KOPIA_PASSWORD}" \\
    --disable-tls \
    --override-hostname="backup-${PVC_NAME}"

  kopia repository connect s3 \\
    --bucket=kopia-backup \\
    --prefix="${REPO_PREFIX}" \\
    --endpoint="${MINIO_ENDPOINT}" \\
    --access-key="${AWS_ACCESS_KEY_ID}" \\
    --secret-access-key="${AWS_SECRET_ACCESS_KEY}" \\
    --password="${KOPIA_PASSWORD}" \\
    --disable-tls \
    --override-hostname="backup-${PVC_NAME}"
fi

echo "===== Repo Status ====="
kopia repository status

echo "===== Snapshot List ====="
kopia snapshot list --all

echo "Using snapshot ID: ${SNAPSHOT_ID}"

echo "Preparing restore directory /data/restore ..."
mkdir -p /data/restore

echo "Restoring snapshot to /data/restore ..."

kopia snapshot restore "${SNAPSHOT_ID}" /data/restore

echo "===== Restore Completed ====="
echo "Checking restored data:"
ls -lah /data/restore

echo "DONE"
"""
                            ],

                            "volumeMounts": [
                                {
                                    "name": "pvc",
                                    "mountPath": "/data"
                                }
                            ]
                        }
                    ],

                    "volumes": [
                        {
                            "name": "pvc",
                            "persistentVolumeClaim": {
                                "claimName": pvc_name
                            }
                        }
                    ]
                }
            }
        }
    }

    url = (
        f"{K8S_HOST}/apis/batch/v1/namespaces/"
        f"{namespace}/jobs"
    )

    resp = requests.post(
        url,
        headers=get_headers(),
        json=job_body,
        verify=CA_PATH,
        timeout=30
    )

    if resp.status_code >= 300:
        raise HTTPException(
            status_code=resp.status_code,
            detail=resp.text
        )

    return {
        "job_name": job_name,
        "job": resp.json()
    }


def create_backup_job(namespace, pvc_name):

    job_name = f"backup-{pvc_name}-{str(uuid.uuid4())[:8]}"

    job_body = {
        "apiVersion": "batch/v1",
        "kind": "Job",
        "metadata": {
            "name": job_name,
            "namespace": namespace
        },
        "spec": {
            # Job完成后1分钟自动删除
            "ttlSecondsAfterFinished": 60,

            "template": {
                "spec": {
                    "restartPolicy": "Never",

                    "containers": [
                        {
                            "name": "kopia",
                            "image": "docker-registry:81/harbor-ailabx/kopia:0.23.0",

                            "resources": {
                                "requests": {
                                    "cpu": "100m",
                                    "memory": "256Mi"
                                },
                                "limits": {
                                    "cpu": "1",
                                    "memory": "1Gi"
                                }
                            },

                            # "envFrom": [
                            #     {
                            #         "secretRef": {
                            #             "name": "kopia-secret"
                            #         }
                            #     }
                            # ],

                            "env": [
                                {
                                    "name": "PVC_NAME",
                                    "value": pvc_name
                                },
                                {
                                    "name": "PVC_NAMESPACE",
                                    "value": namespace
                                },
                                {
                                    "name": "MINIO_ENDPOINT",
                                    "value": MINIO_ENDPOINT
                                },
                                {
                                    "name": "AWS_ACCESS_KEY_ID",
                                    "value": AWS_ACCESS_KEY_ID
                                },
                                {
                                    "name": "AWS_SECRET_ACCESS_KEY",
                                    "value": AWS_SECRET_ACCESS_KEY
                                },
                                {
                                    "name": "KOPIA_PASSWORD",
                                    "value": KOPIA_PASSWORD
                                }
                            ],

                            "command": [
                                "/bin/sh",
                                "-c",
                                """
set -e

PREFIX="${PVC_NAMESPACE}/${PVC_NAME}/"

echo "Namespace=${PVC_NAMESPACE}"
echo "PVC=${PVC_NAME}"

if kopia repository connect s3 \
  --bucket=kopia-backup \
  --prefix="${PREFIX}" \
  --endpoint="${MINIO_ENDPOINT}" \
  --access-key="${AWS_ACCESS_KEY_ID}" \
  --secret-access-key="${AWS_SECRET_ACCESS_KEY}" \
  --password="${KOPIA_PASSWORD}" \
  --disable-tls \
  --override-hostname="backup-${PVC_NAME}"
then
  echo "Repository connected"
else

  kopia repository create s3 \
    --bucket=kopia-backup \
    --prefix="${PREFIX}" \
    --endpoint="${MINIO_ENDPOINT}" \
    --access-key="${AWS_ACCESS_KEY_ID}" \
    --secret-access-key="${AWS_SECRET_ACCESS_KEY}" \
    --password="${KOPIA_PASSWORD}" \
    --disable-tls \
    --override-hostname="backup-${PVC_NAME}"

  kopia repository connect s3 \
    --bucket=kopia-backup \
    --prefix="${PREFIX}" \
    --endpoint="${MINIO_ENDPOINT}" \
    --access-key="${AWS_ACCESS_KEY_ID}" \
    --secret-access-key="${AWS_SECRET_ACCESS_KEY}" \
    --password="${KOPIA_PASSWORD}" \
    --disable-tls \
    --override-hostname="backup-${PVC_NAME}"
fi

SNAPSHOT_ID=$(
  kopia snapshot create /data \
    --description="namespace=${PVC_NAMESPACE},pvc=${PVC_NAME}" \
    --tags "namespace:${PVC_NAMESPACE}" \
    --tags "pvc:${PVC_NAME}" \
    --json \
  | grep '"id"' \
  | head -n 1 \
  | awk -F'"' '{print $4}'
)

echo "SNAPSHOT_ID=${SNAPSHOT_ID}"

kopia maintenance run --full

echo "Backup completed"

kopia snapshot list --all
"""
                            ],

                            "volumeMounts": [
                                {
                                    "name": "pvc",
                                    "mountPath": "/data"
                                }
                            ]
                        }
                    ],

                    "volumes": [
                        {
                            "name": "pvc",
                            "persistentVolumeClaim": {
                                "claimName": pvc_name
                            }
                        }
                    ]
                }
            }
        }
    }

    url = (
        f"{K8S_HOST}/apis/batch/v1/namespaces/"
        f"{namespace}/jobs"
    )

    resp = requests.post(
        url,
        headers=get_headers(),
        json=job_body,
        verify=CA_PATH,
        timeout=30
    )

    if resp.status_code >= 300:
        raise HTTPException(
            status_code=resp.status_code,
            detail=resp.text
        )

    return {
        "job_name": job_name,
        "job": resp.json()
    }

def create_delete_snapshot_job(namespace, pvc_name, snapshot_id):

    job_name = f"delete-snapshot-{pvc_name}-{str(uuid.uuid4())[:8]}"

    job_body = {
        "apiVersion": "batch/v1",
        "kind": "Job",
        "metadata": {
            "name": job_name,
            "namespace": namespace
        },
        "spec": {
            "ttlSecondsAfterFinished": 60,
            "template": {
                "spec": {
                    "restartPolicy": "Never",
                    "containers": [
                        {
                            "name": "kopia",
                            "image": "docker-registry:81/harbor-ailabx/kopia:0.23.0",
                            "resources": {
                                "requests": {
                                    "cpu": "100m",
                                    "memory": "256Mi"
                                },
                                "limits": {
                                    "cpu": "1",
                                    "memory": "1Gi"
                                }
                            },
                            # "envFrom": [
                            #     {
                            #         "secretRef": {
                            #             "name": "kopia-secret"
                            #         }
                            #     }
                            # ],
                            "env": [
                                {
                                    "name": "PVC_NAME",
                                    "value": pvc_name
                                },
                                {
                                    "name": "PVC_NAMESPACE",
                                    "value": namespace
                                },
                                {
                                    "name": "SNAPSHOT_ID",
                                    "value": snapshot_id
                                },
                                {
                                    "name": "MINIO_ENDPOINT",
                                    "value": MINIO_ENDPOINT
                                },
                                {
                                    "name": "AWS_ACCESS_KEY_ID",
                                    "value": AWS_ACCESS_KEY_ID
                                },
                                {
                                    "name": "AWS_SECRET_ACCESS_KEY",
                                    "value": AWS_SECRET_ACCESS_KEY
                                },
                                {
                                    "name": "KOPIA_PASSWORD",
                                    "value": KOPIA_PASSWORD
                                }
                            ],
                            "command": [
                                "/bin/sh",
                                "-c",
                                """
set -e

PREFIX="${PVC_NAMESPACE}/${PVC_NAME}/"

echo "Namespace=${PVC_NAMESPACE}"
echo "PVC=${PVC_NAME}"
echo "Snapshot ID=${SNAPSHOT_ID}"

if kopia repository connect s3 \\
  --bucket=kopia-backup \\
  --prefix="${PREFIX}" \\
  --endpoint="${MINIO_ENDPOINT}" \\
  --access-key="${AWS_ACCESS_KEY_ID}" \\
  --secret-access-key="${AWS_SECRET_ACCESS_KEY}" \\
  --password="${KOPIA_PASSWORD}" \\
  --disable-tls \\
  --override-hostname="backup-${PVC_NAME}"
then
  echo "Repository connected"
else
  echo "Repository not found"
  exit 1
fi

echo "Deleting snapshot ${SNAPSHOT_ID}..."
kopia snapshot delete "${SNAPSHOT_ID}" --delete

echo "Snapshot deleted successfully"

kopia snapshot list --all
"""
                            ],
                            "volumeMounts": [
                                {
                                    "name": "pvc",
                                    "mountPath": "/data"
                                }
                            ]
                        }
                    ],
                    "volumes": [
                        {
                            "name": "pvc",
                            "persistentVolumeClaim": {
                                "claimName": pvc_name
                            }
                        }
                    ]
                }
            }
        }
    }

    url = (
        f"{K8S_HOST}/apis/batch/v1/namespaces/"
        f"{namespace}/jobs"
    )

    resp = requests.post(
        url,
        headers=get_headers(),
        json=job_body,
        verify=CA_PATH,
        timeout=30
    )

    if resp.status_code >= 300:
        raise HTTPException(
            status_code=resp.status_code,
            detail=resp.text
        )

    return {
        "job_name": job_name,
        "job": resp.json()
    }

@app.post("/create")
def create_pvcviewer(req: PVCViewerCreate):

    name = req.pvc_name

    viewer_url = (
        f"{K8S_HOST}/apis/{GROUP}/{VERSION}/namespaces/"
        f"{req.namespace}/{PLURAL}"
    )

    delete_url = (
        f"{K8S_HOST}/apis/{GROUP}/{VERSION}/namespaces/"
        f"{req.namespace}/{PLURAL}/{name}"
    )

    try:

        requests.delete(
            delete_url,
            headers=get_headers(),
            verify=CA_PATH,
            timeout=10
        )

        wait_pvcviewer_deleted(
            req.namespace,
            name
        )

        wait_pods_deleted(
            req.namespace,
            name
        )

        body = {
            "apiVersion": f"{GROUP}/{VERSION}",
            "kind": "PVCViewer",
            "metadata": {
                "name": name
            },
            "spec": {
                "pvc": req.pvc_name,
                "rwoScheduling": True,
                "podSpec": {
                    "volumes": [
                        {
                            "name": "workspace",
                            "persistentVolumeClaim": {
                                "claimName": req.pvc_name
                            }
                        }
                    ],
                    "containers": [
                        {
                            "name": "viewer",
                            "image": "docker-registry:81/harbor-ailabx/filebrowser:v2.23.0",
                            "args": [
                                "--noauth",
                                "--root",
                                "/mnt/"
                            ],
                            "volumeMounts": [
                                {
                                    "name": "workspace",
                                    "mountPath": "/mnt"
                                }
                            ],
                            "resources": {
                                "requests": {
                                    "cpu": "100m",
                                    "memory": "128Mi"
                                },
                                "limits": {
                                    "cpu": "500m",
                                    "memory": "512Mi"
                                }
                            }
                        }
                    ]
                }
            }
        }

        resp = requests.post(
            viewer_url,
            headers=get_headers(),
            json=body,
            verify=CA_PATH,
            timeout=30
        )

        if resp.status_code >= 300:
            raise HTTPException(
                status_code=resp.status_code,
                detail=resp.text
            )

        pod_info = wait_pod_ready(
            req.namespace,
            name,
            timeout=180
        )

        if not pod_info:
            raise HTTPException(
                status_code=500,
                detail="PVCViewer Pod is not Ready within 180 seconds"
            )

        return {
            "message": "PVCViewer created successfully",
            "pod": pod_info,
            "pvcviewer": resp.json()
        }

    except requests.exceptions.RequestException as e:
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )


@app.post("/delete")
def delete_pvcviewer(req: PVCViewerDelete):

    viewer_url = (
        f"{K8S_HOST}/apis/{GROUP}/{VERSION}/namespaces/"
        f"{req.namespace}/{PLURAL}/{req.name}"
    )

    try:

        resp = requests.delete(
            viewer_url,
            headers=get_headers(),
            verify=CA_PATH,
            timeout=10
        )

        result = {
            "message": "PVCViewer deleted"
        }

        if resp.text:
            try:
                result["pvcviewer"] = resp.json()
            except Exception:
                result["response"] = resp.text

        return result

    except requests.exceptions.RequestException as e:
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

@app.post("/backup")
def backup_pvc(req: PVCBackupRequest):

    db = SessionLocal()

    try:
        running_task = BackupService.check_running_backup(
            db,
            req.namespace,
            req.pvc_name
        )

        if running_task:
            raise HTTPException(
                status_code=409,
                detail=f"PVC [{req.pvc_name}] 在命名空间 [{req.namespace}] 下已有备份任务正在运行。"
                       f"任务状态: {running_task.status}, Job名称: {running_task.job_name}"
            )

        result = create_backup_job(
            req.namespace,
            req.pvc_name
        )

        task = BackupService.create_backup_task(
            db,
            req.namespace,
            req.pvc_name,
            result["job_name"]
        )

        try:
            wait_job_completed(
                req.namespace,
                result["job_name"]
            )
        except Exception as e:
            BackupService.backup_failed(db, task)
            raise HTTPException(
                status_code=500,
                detail=f"备份任务 [{result['job_name']}] 执行失败: {str(e)}"
            )

        pod_name = get_job_pod(
            req.namespace,
            result["job_name"]
        )

        if not pod_name:
            BackupService.backup_failed(
                db,
                task
            )

            raise HTTPException(
                status_code=500,
                detail=f"无法找到备份Job [{result['job_name']}] 对应的Pod，请检查Kubernetes集群状态"
            )

        log_text = get_pod_log(
            req.namespace,
            pod_name
        )

        snapshot_id = parse_snapshot_id(
            log_text
        )

        if not snapshot_id:

            BackupService.backup_failed(
                db,
                task
            )

            raise HTTPException(
                status_code=500,
                detail=f"备份Job [{result['job_name']}] 执行完成，但未能从Pod日志中提取到快照ID"
            )

        BackupService.backup_success(
            db,
            task,
            snapshot_id
        )

        return {
            "message": "backup success",
            "namespace": req.namespace,
            "pvc_name": req.pvc_name,
            "job_name": result["job_name"],
            "snapshot_id": snapshot_id
        }

    finally:
        db.close()


@app.delete("/snapshot")
def delete_snapshot(req: SnapshotDeleteRequest):

    db = SessionLocal()

    try:
        snapshot = SnapshotService.find_snapshot_by_id(
            db,
            req.snapshot_id
        )

        if not snapshot:
            raise HTTPException(
                status_code=404,
                detail=f"快照ID [{req.snapshot_id}] 不存在"
            )

        if snapshot.namespace_name != req.namespace or snapshot.pvc_name != req.pvc_name:
            raise HTTPException(
                status_code=400,
                detail=f"快照ID [{req.snapshot_id}] 不属于指定的namespace [{req.namespace}] 或PVC [{req.pvc_name}]"
            )

        running_task = BackupService.check_running_backup(
            db,
            req.namespace,
            req.pvc_name
        )

        if running_task:
            raise HTTPException(
                status_code=409,
                detail=f"PVC [{req.pvc_name}] 在命名空间 [{req.namespace}] 下已有任务正在运行。"
                       f"任务状态: {running_task.status}, Job名称: {running_task.job_name}"
            )

        result = create_delete_snapshot_job(
            req.namespace,
            req.pvc_name,
            req.snapshot_id
        )

        task = BackupService.create_backup_task(
            db,
            req.namespace,
            req.pvc_name,
            result["job_name"]
        )

        try:
            wait_job_completed(
                req.namespace,
                result["job_name"]
            )
        except Exception as e:
            BackupService.backup_failed(db, task)
            raise HTTPException(
                status_code=500,
                detail=f"删除快照任务 [{result['job_name']}] 执行失败: {str(e)}"
            )

        SnapshotService.delete_snapshot(
            db,
            req.snapshot_id
        )

        BackupService.task_success(db, task)

        return {
            "message": "snapshot deleted",
            "namespace": req.namespace,
            "pvc_name": req.pvc_name,
            "snapshot_id": req.snapshot_id,
            "job_name": result["job_name"]
        }

    finally:
        db.close()


# 快照统计接口
# @app.get("/snapshot/count")
# def snapshot_count(namespace: str):

#     db = SessionLocal()

#     try:

#         count = SnapshotService.get_snapshot_count(
#             db,
#             namespace
#         )

#         return {
#             "namespace": namespace,
#             "snapshot_count": count
#         }

#     finally:
#         db.close()

@app.get("/snapshot/list")
def snapshot_list(
    namespace: str,
    pvc_name: str
):

    db = SessionLocal()

    try:

        snapshots = SnapshotService.get_snapshot_list_by_pvc(
            db,
            namespace,
            pvc_name
        )

        return [
            {
                "pvc_name": item.pvc_name,
                "snapshot_id": item.snapshot_id,
                "create_time": item.create_time
            }
            for item in snapshots
        ]

    finally:
        db.close()


@app.get("/snapshot/list/namespace")
def snapshot_list_by_namespace(
    namespace: str
):

    db = SessionLocal()

    try:

        snapshot_task_pairs = SnapshotService.get_snapshot_list_by_namespace_with_task(
            db,
            namespace
        )

        result = []
        for snapshot, task in snapshot_task_pairs:
            item = {
                "snapshot_id": snapshot.snapshot_id,
                "namespace": snapshot.namespace_name,
                "pvc_name": snapshot.pvc_name,
                "snapshot_create_time": snapshot.create_time,
                "task_id": task.id if task else None,
                "job_name": task.job_name if task else None,
                "task_status": task.status if task else None,
                "task_create_time": task.create_time if task else None,
                "task_finish_time": task.finish_time if task else None
            }
            result.append(item)

        return {
            "namespace": namespace,
            "total": len(result),
            "backups": result
        }

    finally:
        db.close()


@app.post("/restore")
def restore_pvc(req: PVCRestoreRequest):

    db = SessionLocal()

    try:
        running_task = BackupService.check_running_backup(
            db,
            req.namespace,
            req.pvc_name
        )

        if running_task:
            raise HTTPException(
                status_code=409,
                detail=f"PVC [{req.pvc_name}] 在命名空间 [{req.namespace}] 下已有任务正在运行。"
                       f"任务状态: {running_task.status}, Job名称: {running_task.job_name}"
            )

        result = create_restore_job(
            req.namespace,
            req.pvc_name,
            req.snapshot_id,
            req.source_pvc_name
        )

        task = BackupService.create_backup_task(
            db,
            req.namespace,
            req.pvc_name,
            result["job_name"]
        )

        try:
            wait_job_completed(
                req.namespace,
                result["job_name"]
            )
        except Exception as e:
            BackupService.backup_failed(db, task)
            raise HTTPException(
                status_code=500,
                detail=f"恢复任务 [{result['job_name']}] 执行失败: {str(e)}"
            )

        pod_name = get_job_pod(
            req.namespace,
            result["job_name"]
        )

        if not pod_name:
            BackupService.backup_failed(db, task)
            raise HTTPException(
                status_code=500,
                detail=f"无法找到恢复Job [{result['job_name']}] 对应的Pod，请检查Kubernetes集群状态"
            )

        log_text = get_pod_log(
            req.namespace,
            pod_name
        )

        if "===== Restore Completed =====" not in log_text:
            BackupService.backup_failed(db, task)
            raise HTTPException(
                status_code=500,
                detail=f"恢复任务执行异常，请检查Pod日志: {log_text[-1000:] if len(log_text) > 1000 else log_text}"
            )

        BackupService.restore_success(db, task, req.snapshot_id)

        return {
            "message": "restore success",
            "namespace": req.namespace,
            "pvc_name": req.pvc_name,
            "snapshot_id": req.snapshot_id,
            "job_name": result["job_name"]
        }

    finally:
        db.close()


# @app.get("/storageclass/list")
# def get_storageclasses():
#     try:
#         # url = "https://kubernetes.default.svc/api/v1/storageclasses"
#         url = "https://kubernetes.default.svc/apis/storage.k8s.io/v1/storageclasses"
#         headers = {
#             "Authorization": "Bearer " + open("/var/run/secrets/kubernetes.io/serviceaccount/token").read(),
#             "Accept": "application/json"
#         }
#         response = requests.get(url, headers=headers, verify=False)
        
#         if response.status_code != 200:
#             raise HTTPException(
#                 status_code=response.status_code,
#                 detail=f"Failed to get storageclasses: {response.text}"
#             )
        
#         data = response.json()
#         storageclasses = []
        
#         for item in data.get("items", []):
#             sc = {
#                 "name": item["metadata"]["name"],
#                 "provisioner": item["provisioner"],
#                 "reclaim_policy": item.get("reclaimPolicy", "Delete"),
#                 "volume_binding_mode": item.get("volumeBindingMode", "Immediate"),
#                 "allow_volume_expansion": item.get("allowVolumeExpansion", False),
#                 "parameters": item.get("parameters", {}),
#                 "created_at": item["metadata"]["creationTimestamp"]
#             }
#             storageclasses.append(sc)
        
#         return {
#             "message": "success",
#             "storageclasses": storageclasses,
#             "total": len(storageclasses)
#         }
    
#     except HTTPException:
#         raise
#     except Exception as e:
#         raise HTTPException(
#             status_code=500,
#             detail=f"Error getting storageclasses: {str(e)}"
#         )


@app.get("/overview")
def get_overview(namespace: str = None):
    result = {
        "message": "success",
        "snapshot": {
            "count": 0,
            "by_namespace": {}
        },
        "storageclass": {
            "count": 0,
            "list": []
        }
    }
    
    db = SessionLocal()
    try:
        if namespace:
            snapshot_count = SnapshotService.get_snapshot_count(db, namespace)
            result["snapshot"]["count"] = snapshot_count
            result["snapshot"]["by_namespace"][namespace] = snapshot_count
        else:
            all_snapshots = SnapshotService.get_snapshot_list(db)
            namespace_counts = {}
            total = 0
            for snap in all_snapshots:
                ns = snap.namespace_name
                if ns not in namespace_counts:
                    namespace_counts[ns] = 0
                namespace_counts[ns] += 1
                total += 1
            result["snapshot"]["count"] = total
            result["snapshot"]["by_namespace"] = namespace_counts
    finally:
        db.close()
    
    try:
        # url = "https://kubernetes.default.svc/api/v1/storageclasses"
        url = "https://kubernetes.default.svc/apis/storage.k8s.io/v1/storageclasses"
        headers = {
            "Authorization": "Bearer " + open("/var/run/secrets/kubernetes.io/serviceaccount/token").read(),
            "Accept": "application/json"
        }
        response = requests.get(url, headers=headers, verify=False)
        
        if response.status_code == 200:
            data = response.json()
            storageclasses = []
            for item in data.get("items", []):
                sc = {
                    "name": item["metadata"]["name"],
                    "provisioner": item["provisioner"],
                    "reclaim_policy": item.get("reclaimPolicy", "Delete"),
                    "volume_binding_mode": item.get("volumeBindingMode", "Immediate"),
                    "allow_volume_expansion": item.get("allowVolumeExpansion", False),
                    "parameters": item.get("parameters", {}),
                    "created_at": item["metadata"]["creationTimestamp"]
                }
                storageclasses.append(sc)
            result["storageclass"]["count"] = len(storageclasses)
            result["storageclass"]["list"] = storageclasses
        else:
            result["storageclass"]["count"] = -1
            result["storageclass"]["error"] = f"Failed to get storageclasses: {response.text}"
    
    except Exception as e:
        result["storageclass"]["count"] = -1
        result["storageclass"]["error"] = str(e)
    
    return result