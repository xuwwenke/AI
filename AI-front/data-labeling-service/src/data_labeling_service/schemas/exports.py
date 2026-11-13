from pydantic import BaseModel, Field


class ExportFormat(BaseModel):
  """一个可用的导出格式。"""

  name: str = Field(description="导出格式标识，导出时作为 export_type 传入")
  title: str = Field(description="导出格式名称")
  description: str | None = Field(None, description="导出格式说明")
  link: str | None = Field(None, description="官方文档链接")
  tags: list[str] = Field(default_factory=list, description="适用的任务类型")
  disabled: bool = Field(False, description="当前项目的标注配置是否不适用该格式")


class ExportFormatListResponse(BaseModel):
  """项目支持的导出格式列表。"""

  project_id: int = Field(description="项目 ID")
  total: int = Field(description="导出格式数量")
  formats: list[ExportFormat] = Field(default_factory=list, description="导出格式列表")
