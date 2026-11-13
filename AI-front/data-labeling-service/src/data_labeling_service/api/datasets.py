import mimetypes
import os
from urllib.parse import unquote, urlparse

import requests
from fastapi import APIRouter, Body, File, HTTPException, Path, UploadFile, status

from ..ls_client import get_client
from ..schemas.datasets import (
  DatasetFileResult,
  DatasetUploadResponse,
  DatasetUrlUploadRequest,
)

router = APIRouter(prefix="/api/projects", tags=["datasets"])

# 允许上传的图片格式
IMAGE_EXTENSIONS = {".bmp", ".gif", ".jpg", ".jpeg", ".png", ".svg", ".webp"}

# 允许上传的其他数据格式
DATA_EXTENSIONS = {
  ".csv",
  ".tsv",
  ".json",
  ".txt",
  ".html",
  ".htm",
  ".xml",
  ".mp4",
  ".webm",
  ".wav",
  ".mp3",
  ".flac",
  ".m4a",
  ".ogg",
  ".pdf",
}

ALLOWED_EXTENSIONS = IMAGE_EXTENSIONS | DATA_EXTENSIONS

# Label Studio 单次导入上限为 200MB
MAX_FILE_BYTES = 200 * 1024 * 1024
DOWNLOAD_TIMEOUT = 60


def _check_project(project_id: int) -> None:
  """校验项目是否存在。"""
  try:
    get_client().projects.get(project_id)
  except Exception as e:
    if getattr(e, "status_code", None) == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    raise HTTPException(status_code=502, detail=f"Label Studio API 错误: {e}")


def _check_extension(filename: str) -> None:
  """校验文件扩展名是否在支持的格式白名单内。"""
  ext = os.path.splitext(filename)[1].lower()
  if ext not in ALLOWED_EXTENSIONS:
    raise HTTPException(
      status_code=status.HTTP_400_BAD_REQUEST,
      detail=(
        f"不支持的文件格式: {ext or '(无扩展名)'}。"
        f"图片支持: {', '.join(sorted(IMAGE_EXTENSIONS))}；"
        f"其他数据支持: {', '.join(sorted(DATA_EXTENSIONS))}"
      ),
    )


def _import_file(project_id: int, filename: str, content: bytes) -> DatasetFileResult:
  """把单个文件的内容导入到项目，返回导入结果。"""
  content_type = mimetypes.guess_type(filename)[0] or "application/octet-stream"
  response = get_client()._client_wrapper.httpx_client.request(
    f"/api/projects/{project_id}/import",
    method="POST",
    files={"file": [(filename, content, content_type)]},
    data={"return_task_ids": "true"},
  )

  if response.status_code >= 400:
    return DatasetFileResult(filename=filename, success=False, error=response.text[:300])

  body = response.json()
  upload_ids = body.get("file_upload_ids") or []
  return DatasetFileResult(
    filename=filename,
    success=True,
    file_upload_id=upload_ids[0] if upload_ids else None,
    task_count=body.get("task_count") or 0,
  )


def _download(url: str) -> bytes:
  """下载 URL 指向的文件内容，限制最大 200MB。"""
  try:
    with requests.get(url, timeout=DOWNLOAD_TIMEOUT, stream=True) as response:
      response.raise_for_status()
      content = response.content
  except requests.RequestException as e:
    raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY, detail=f"文件下载失败: {e}")

  if len(content) > MAX_FILE_BYTES:
    raise HTTPException(status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, detail="文件大小超过 200MB 限制")
  return content


def _filename_from_url(url: str) -> str:
  """从 URL 中解析文件名。"""
  return os.path.basename(unquote(urlparse(url).path))


def _summarize(project_id: int, results: list[DatasetFileResult]) -> DatasetUploadResponse:
  return DatasetUploadResponse(
    project_id=project_id,
    total=len(results),
    success_count=sum(1 for r in results if r.success),
    failed_count=sum(1 for r in results if not r.success),
    task_count=sum(r.task_count for r in results),
    results=results,
  )


@router.post("/{project_id}/datasets/upload", response_model=DatasetUploadResponse)
async def upload_dataset_files(
  project_id: int = Path(description="目标项目 ID"),
  files: list[UploadFile] = File(description="数据集文件，支持同时上传多个"),
) -> DatasetUploadResponse:
  """
  向指定项目上传数据集文件（本地文件）。

  - 图片格式：bmp, gif, jpg, jpeg, png, svg, webp
  - 其他数据格式：csv, tsv, json, txt, html, htm, xml, mp4, webm, wav, mp3, flac, m4a, ogg, pdf
  """
  if not files:
    raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="请至少上传一个文件")

  filenames = [os.path.basename(f.filename or "") for f in files]
  for filename in filenames:
    _check_extension(filename)

  _check_project(project_id)

  results = []
  for filename, upload in zip(filenames, files):
    content = await upload.read()
    if len(content) > MAX_FILE_BYTES:
      results.append(DatasetFileResult(filename=filename, success=False, error="文件大小超过 200MB 限制"))
      continue
    results.append(_import_file(project_id, filename, content))

  return _summarize(project_id, results)


@router.post("/{project_id}/datasets/upload-by-url", response_model=DatasetUploadResponse)
async def upload_dataset_by_url(
  project_id: int = Path(description="目标项目 ID"),
  payload: DatasetUrlUploadRequest = Body(description="数据集 URL 列表"),
) -> DatasetUploadResponse:
  """
  通过 URL 向指定项目上传数据集。

  服务会先下载 URL 指向的文件（仅支持图片与其他数据格式），再导入到项目中，
  因此要求 URL 对数据标注服务可访问。
  """
  if not payload.urls:
    raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="请至少提供一个 URL")

  _check_project(project_id)

  results = []
  for url in payload.urls:
    filename = _filename_from_url(url)
    try:
      _check_extension(filename)
      content = _download(url)
    except HTTPException as e:
      results.append(DatasetFileResult(filename=url, success=False, error=e.detail))
      continue
    results.append(_import_file(project_id, filename, content))

  return _summarize(project_id, results)
