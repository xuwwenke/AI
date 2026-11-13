from typing import Any

from pydantic import BaseModel, Field


class AnnotationItem(BaseModel):
  """一条标注结果。"""

  id: int | None = Field(None, description="标注 ID")
  task: int | None = Field(None, description="所属任务 ID")
  project: int | None = Field(None, description="所属项目 ID")
  result: list[dict[str, Any]] = Field(default_factory=list, description="标注结果列表，结构由标注界面配置决定")
  completed_by: int | None = Field(None, description="标注人用户 ID")
  created_username: str | None = Field(None, description="标注人用户名")
  created_at: str | None = Field(None, description="创建时间")
  updated_at: str | None = Field(None, description="最后更新时间")
  lead_time: float | None = Field(None, description="标注耗时（秒）")
  was_cancelled: bool = Field(False, description="是否为跳过（取消）的标注")
  ground_truth: bool = Field(False, description="是否标记为标准答案（Ground Truth）")


class AnnotationResponse(AnnotationItem):
  """标注提交/更新结果。"""

  message: str = Field("操作成功", description="操作结果说明")


class AnnotationDeleteResponse(BaseModel):
  """标注删除结果。"""

  id: int = Field(description="被删除的标注 ID")
  message: str = Field("删除成功", description="操作结果说明")


class TaskItem(BaseModel):
  """任务列表项。"""

  id: int = Field(description="任务 ID")
  inner_id: int | None = Field(None, description="任务在项目内的序号")
  project: int | None = Field(None, description="所属项目 ID")
  data: dict[str, Any] = Field(default_factory=dict, description="任务数据，字段由标注界面配置决定")
  total_annotations: int = Field(0, description="该任务的标注数量")
  total_predictions: int = Field(0, description="该任务的 AI 辅助标注（预测）数量")
  is_labeled: bool = Field(False, description="是否已完成标注")
  created_at: str | None = Field(None, description="创建时间")


class TaskDetail(TaskItem):
  """任务详情。"""

  annotations: list[AnnotationItem] = Field(default_factory=list, description="该任务的标注结果列表")
  predictions: list[dict[str, Any]] = Field(default_factory=list, description="该任务的 AI 辅助标注（预测）列表")


class TaskListResponse(BaseModel):
  """任务列表响应。"""

  project_id: int = Field(description="项目 ID")
  total: int = Field(description="符合筛选条件的任务总数")
  page: int = Field(description="当前页码")
  page_size: int = Field(description="每页数量")
  items: list[TaskItem] = Field(default_factory=list, description="任务列表")


class DataManagerTaskListResponse(BaseModel):
  """Data Manager 任务列表（原生 `GET /api/tasks` 的响应结构）。"""

  total_annotations: int = Field(0, description="当前筛选条件下的标注总数")
  total_predictions: int = Field(0, description="当前筛选条件下的预测总数")
  total: int = Field(0, description="当前筛选条件下的任务总数")
  tasks: list[dict[str, Any]] = Field(default_factory=list, description="任务列表")


class AnnotationSubmitRequest(BaseModel):
  """提交标注结果的请求体。"""

  result: list[dict[str, Any]] = Field(
    default_factory=list,
    description="标注结果列表，结构由标注界面配置决定，如 Choices 为 "
    '[{"from_name": "label", "to_name": "text", "type": "choices", "value": {"choices": ["0"]}}]',
  )
  lead_time: float | None = Field(None, description="标注耗时（秒）")
  was_cancelled: bool = Field(False, description="是否跳过该任务")
  ground_truth: bool = Field(False, description="是否标记为标准答案（Ground Truth）")


class AnnotationUpdateRequest(BaseModel):
  """更新标注结果的请求体，仅需提交要修改的字段。"""

  result: list[dict[str, Any]] | None = Field(None, description="标注结果列表")
  lead_time: float | None = Field(None, description="标注耗时（秒）")
  was_cancelled: bool | None = Field(None, description="是否跳过该任务")
  ground_truth: bool | None = Field(None, description="是否标记为标准答案（Ground Truth）")
