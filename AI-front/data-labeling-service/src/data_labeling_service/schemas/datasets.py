from pydantic import BaseModel, Field


class DatasetFileResult(BaseModel):
  """单个数据集文件的导入结果。"""

  filename: str = Field(description="文件名或 URL")
  success: bool = Field(description="是否导入成功")
  file_upload_id: int | None = Field(None, description="Label Studio 文件上传 ID")
  task_count: int = Field(0, description="该文件创建的任务数")
  error: str | None = Field(None, description="失败原因")


class DatasetUploadResponse(BaseModel):
  """数据集上传结果汇总。"""

  project_id: int = Field(description="目标项目 ID")
  total: int = Field(description="提交的数据集数量")
  success_count: int = Field(description="导入成功数量")
  failed_count: int = Field(description="导入失败数量")
  task_count: int = Field(description="新增任务总数")
  results: list[DatasetFileResult] = Field(description="每个数据集的导入结果")


class DatasetUrlUploadRequest(BaseModel):
  """通过 URL 上传数据集的请求体。"""

  urls: list[str] = Field(description="数据集文件 URL 列表")
