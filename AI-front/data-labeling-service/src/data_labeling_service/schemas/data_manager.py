from pydantic import BaseModel, Field


class ViewsResetResponse(BaseModel):
  """重置项目 Data Manager 视图的结果。"""

  project: int = Field(description="项目 ID")
  status: str = Field("ok", description="操作状态")
  message: str = Field("视图已重置", description="操作结果说明")
