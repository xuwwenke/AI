from typing import Any

from pydantic import BaseModel, Field


class PredictionItem(BaseModel):
  """一条模型预测（AI 辅助标注）。"""

  id: int | None = Field(None, description="预测 ID")
  task: int | None = Field(None, description="所属任务 ID")
  project: int | None = Field(None, description="所属项目 ID")
  result: list[dict[str, Any]] = Field(default_factory=list, description="预测结果列表，结构由标注界面配置决定")
  score: float | None = Field(None, description="预测置信度")
  model_version: str | None = Field(None, description="产生该预测的模型版本")
  model: int | None = Field(None, description="产生该预测的模型连接 ID")
  model_run: int | None = Field(None, description="产生该预测的模型版本运行 ID")
  cluster: int | None = Field(None, description="所属聚类 ID")
  mislabeling: float | None = Field(None, description="关联任务的误标分数")
  neighbors: Any = Field(None, description="最相近的任务 ID 列表")
  created_at: str | None = Field(None, description="创建时间")
  updated_at: str | None = Field(None, description="最后更新时间")


class PredictionResponse(PredictionItem):
  """预测创建/更新结果。"""

  message: str = Field("操作成功", description="操作结果说明")


class PredictionListResponse(BaseModel):
  """任务下的预测列表。"""

  task_id: int = Field(description="任务 ID")
  total: int = Field(description="预测数量")
  items: list[PredictionItem] = Field(default_factory=list, description="预测列表")


class PredictionDeleteResponse(BaseModel):
  """预测删除结果。"""

  id: int = Field(description="被删除的预测 ID")
  message: str = Field("删除成功", description="操作结果说明")


class PredictionCreateRequest(BaseModel):
  """为任务添加模型预测的请求体。"""

  result: list[dict[str, Any]] = Field(
    description="预测结果列表，结构由标注界面配置决定，如 Choices 为 "
    '[{"from_name": "label", "to_name": "text", "type": "choices", "value": {"choices": ["1"]}}]',
  )
  score: float | None = Field(None, description="预测置信度")
  model_version: str | None = Field(None, description="产生该预测的模型版本，用于在数据管理器中筛选预标注")
  model: int | None = Field(None, description="产生该预测的模型连接 ID")
  model_run: int | None = Field(None, description="产生该预测的模型版本运行 ID")
  cluster: int | None = Field(None, description="所属聚类 ID")
  mislabeling: float | None = Field(None, description="关联任务的误标分数")
  neighbors: Any = Field(None, description="最相近的任务 ID 列表")


class PredictionUpdateRequest(BaseModel):
  """更新预测的请求体，仅需提交要修改的字段。"""

  result: list[dict[str, Any]] | None = Field(None, description="预测结果列表")
  score: float | None = Field(None, description="预测置信度")
  model_version: str | None = Field(None, description="产生该预测的模型版本")
  model: int | None = Field(None, description="产生该预测的模型连接 ID")
  model_run: int | None = Field(None, description="产生该预测的模型版本运行 ID")
  cluster: int | None = Field(None, description="所属聚类 ID")
  mislabeling: float | None = Field(None, description="关联任务的误标分数")
  neighbors: Any = Field(None, description="最相近的任务 ID 列表")


class AiAnnotationRequest(BaseModel):
  """为任务生成 AI 辅助标注的请求体。"""

  model_id: int | None = Field(None, description="指定使用的模型连接 ID，缺省时自动使用项目下已连接的模型")
  model_version: str | None = Field(None, description="写入预标注时使用的模型版本标记，缺省使用模型服务返回的版本")
  overwrite: bool = Field(True, description="是否先删除该任务下相同模型版本的旧预测，避免重复预标注")
  save: bool = Field(True, description="是否把预测写入任务作为预标注，false 时仅返回模型结果不落库")


class AiAnnotationResponse(PredictionItem):
  """AI 辅助标注生成结果。"""

  model_title: str | None = Field(None, description="产生该预测的模型名称")
  saved: bool = Field(True, description="预测是否已写入任务作为预标注")
  message: str = Field("生成成功", description="操作结果说明")

