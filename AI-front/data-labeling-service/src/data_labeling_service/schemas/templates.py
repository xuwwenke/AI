from pydantic import BaseModel, Field


class TemplateItem(BaseModel):
  """Label Studio 内置的一标注模板（labeling template）。"""

  title: str = Field(description="模板名称")
  type: str | None = Field(None, description="模板类型，如 community")
  group: str | None = Field(None, description="所属分组，如 Computer Vision")
  order: int | None = Field(None, description="分组内排序序号")
  image: str | None = Field(None, description="模板预览图地址")
  details: str | None = Field(None, description="模板说明（HTML）")
  source_repo: str | None = Field(None, description="模板来源仓库")
  config: str = Field(description="标注界面配置，可直接作为 project 的 label_config")


class TemplateListResponse(BaseModel):
  """标注模板列表，结构与 Label Studio /api/templates/ 保持一致。"""

  templates: list[TemplateItem] = Field(description="模板列表")
  groups: list[str] = Field(description="模板分组列表")
