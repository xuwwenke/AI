from collections.abc import Iterator
from datetime import datetime, timezone
from uuid import uuid4

from fastapi import APIRouter, HTTPException, Path, Query
from fastapi.responses import StreamingResponse
from label_studio_sdk.core.api_error import ApiError

from ..ls_client import get_client
from ..schemas.exports import ExportFormat, ExportFormatListResponse

router = APIRouter(prefix="/api/projects", tags=["exports"])

# 导出格式 -> (文件扩展名, Content-Type)
EXPORT_FILE_TYPES = {
  "JSON": (".json", "application/json"),
  "JSON_MIN": (".json", "application/json"),
  "CSV": (".csv", "text/csv"),
  "TSV": (".tsv", "text/tab-separated-values"),
}

# 其他格式（如 COCO、YOLO 等）由 Label Studio 打包为 zip
DEFAULT_FILE_TYPE = (".zip", "application/zip")


def _raise_api_error(e: ApiError) -> None:
  raise HTTPException(
    status_code=e.status_code or 502,
    detail=f"Label Studio API 错误: {str(e.body)[:300]}",
  )


def _check_project(project_id: int) -> None:
  """校验项目是否存在。"""
  try:
    get_client().projects.get(project_id)
  except ApiError as e:
    if e.status_code == 404:
      raise HTTPException(status_code=404, detail=f"项目不存在: {project_id}")
    _raise_api_error(e)


def _list_formats(project_id: int) -> list[ExportFormat]:
  """获取项目可用的导出格式。"""
  try:
    raw_formats = get_client().projects.exports.list_formats(project_id)
  except ApiError as e:
    _raise_api_error(e)

  formats = []
  for item in raw_formats:
    name = item.get("name") or ""
    formats.append(
      ExportFormat(
        name=name,
        title=item.get("title") or name,
        description=item.get("description"),
        link=item.get("link"),
        tags=item.get("tags") or [],
        disabled=bool(item.get("disabled", False)),
      )
    )
  return formats


def _find_format(project_id: int, export_type: str) -> ExportFormat:
  """校验导出格式是否可用，返回对应的格式信息。"""
  formats = _list_formats(project_id)
  for item in formats:
    if item.name.upper() == export_type:
      if item.disabled:
        raise HTTPException(
          status_code=400,
          detail=f"导出格式 {export_type} 不适用于当前项目的标注配置",
        )
      return item

  available = ", ".join(sorted(f.name for f in formats if not f.disabled))
  raise HTTPException(
    status_code=400,
    detail=f"不支持的导出格式: {export_type}。当前项目可用格式: {available}",
  )


def _build_filename(project_id: int, extension: str) -> str:
  stamp = datetime.now(timezone.utc).strftime("%Y-%m-%d-%H-%M")
  return f"project-{project_id}-at-{stamp}-{uuid4().hex[:8]}{extension}"


@router.get("/{project_id}/export-formats", response_model=ExportFormatListResponse)
async def list_export_formats(
  project_id: int = Path(description="项目 ID"),
) -> ExportFormatListResponse:
  """
  获取项目支持的导出文件类型。

  `disabled=true` 的格式表示当前项目的标注配置不适用，导出时会被拒绝。
  """
  _check_project(project_id)

  formats = _list_formats(project_id)
  return ExportFormatListResponse(
    project_id=project_id,
    total=len(formats),
    formats=formats,
  )


@router.get(
  "/{project_id}/export",
  response_class=StreamingResponse,
  responses={
    200: {
      "description": "导出文件，仅包含标注完成的任务",
      "content": {"application/octet-stream": {}},
    }
  },
)
def export_project_data(
  project_id: int = Path(description="项目 ID"),
  export_type: str = Query("JSON", description="导出文件类型，可选值见导出格式列表接口，如 JSON、JSON_MIN、CSV、TSV"),
  download_resources: bool = Query(False, description="是否同时打包任务中的原始资源文件（图片、音视频等），开启后返回 zip 压缩包"),
) -> StreamingResponse:
  """
  导出项目数据，支持选择不同的导出文件类型。

  - 只导出**标注完成**的任务，未标注的任务不会出现在导出文件中
  - `export_type` 选择导出文件类型，可通过导出格式列表接口查询当前项目支持的格式
  - `download_resources=true` 时会把原始资源文件一起打包为 zip，适合需要图片/音视频原文件的场景
  """
  _check_project(project_id)

  key = export_type.strip().upper()
  _find_format(project_id, key)

  if download_resources:
    extension, media_type = DEFAULT_FILE_TYPE
  else:
    extension, media_type = EXPORT_FILE_TYPES.get(key, DEFAULT_FILE_TYPE)

  filename = _build_filename(project_id, extension)

  def _iter_file() -> Iterator[bytes]:
    with get_client().projects.exports.with_raw_response.download_sync(
      project_id,
      download_all_tasks=False,  # 固定为 False，保证只导出已标注任务
      download_resources=download_resources,
      export_type=key,
    ) as response:
      yield from response.data

  return StreamingResponse(
    _iter_file(),
    media_type=media_type,
    headers={"Content-Disposition": f'attachment; filename="{filename}"'},
  )
