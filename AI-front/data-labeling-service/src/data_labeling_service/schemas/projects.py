from typing import Any

from pydantic import BaseModel, Field


class ProjectStats(BaseModel):
  """项目任务/标注统计信息。"""

  task_number: int = Field(0, description="任务总数")
  finished_task_number: int = Field(0, description="已完成（已标注）任务数")
  num_tasks_with_annotations: int = Field(0, description="包含标注的任务数")
  total_annotations_number: int = Field(0, description="标注总数")
  total_predictions_number: int = Field(0, description="AI 辅助标注（预测）总数")
  useful_annotation_number: int = Field(0, description="有效标注数（不含跳过与标准答案）")
  ground_truth_number: int = Field(0, description="标准答案（Ground Truth）数量")
  skipped_annotations_number: int = Field(0, description="被跳过的标注数")
  queue_total: int = Field(0, description="标注队列总任务数")
  queue_done: int = Field(0, description="标注队列已完成任务数")


class ProjectItem(ProjectStats):
  """项目列表项。"""

  id: int
  title: str
  description: str = ""
  created_at: str | None = None


class ProjectCreatedBy(BaseModel):
  """项目创建者信息。"""

  id: int | None = None
  username: str | None = None
  first_name: str | None = None
  last_name: str | None = None
  email: str | None = None
  avatar: str | None = None


class ProjectDetail(ProjectStats):
  """项目详情。"""

  id: int
  title: str
  description: str = ""
  label_config: str = ""
  expert_instruction: str = ""
  created_by: ProjectCreatedBy | None = None
  organization: int | None = None
  color: str | None = None
  is_published: bool | None = None
  is_draft: bool | None = None
  created_at: str | None = None
  pinned_at: str | None = None
  state: str | None = None
  model_version: str | None = None
  maximum_annotations: int | None = None
  min_annotations_to_start_training: int | None = None
  start_training_on_annotation_update: bool | None = None
  show_instruction: bool | None = None
  show_skip_button: bool | None = None
  show_annotation_history: bool | None = None
  show_collab_predictions: bool | None = None
  show_ground_truth_first: bool | None = None
  show_overlap_first: bool | None = None
  enable_empty_annotation: bool | None = None
  evaluate_predictions_automatically: bool | None = None
  reveal_preannotations_interactively: bool | None = None
  annotator_evaluation_enabled: bool | None = None
  overlap_cohort_percentage: int | None = None
  sampling: str | None = None
  skip_queue: str | None = None
  task_data_login: str | None = None
  task_data_password: str | None = None
  config_has_control_tags: bool | None = None
  config_suitable_for_bulk_annotation: bool | None = None
  control_weights: dict[str, Any] | None = None
  parsed_label_config: dict[str, Any] | None = None


class ProjectListResponse(BaseModel):
  total: int = Field(description="项目总数")
  page: int = Field(description="当前页码")
  page_size: int = Field(description="每页数量")
  items: list[ProjectItem]


class ProjectCreateRequest(BaseModel):
  title: str = Field(description="项目标题")
  description: str = Field("", description="项目描述")
  label_config: str = Field("", description="标注配置（XML），为空时使用默认配置")


class ProjectDeleteResponse(BaseModel):
  id: int = Field(description="被删除的项目 ID")
  message: str = Field("删除成功", description="操作结果说明")


class LabelConfigRequest(BaseModel):
  label_config: str = Field(description="标注界面配置（Label Studio XML）")


class LabelConfigResponse(BaseModel):
  id: int = Field(description="项目 ID")
  label_config: str = Field(description="设置后的标注界面配置")
  message: str = Field("设置成功", description="操作结果说明")


class SampleTaskRequest(BaseModel):
  label_config: str = Field(description="标注界面配置（Label Studio XML）")
  include_annotation_and_prediction: bool = Field(
    False,
    description="是否同时生成带标注与预测的完整示例任务",
  )


class SampleTaskResponse(BaseModel):
  sample_task: dict[str, Any] = Field(description="根据 label_config 生成的示例任务数据")


class LabelConfigValidateResponse(BaseModel):
  config_essential_data_has_changed: bool | None = Field(
    None,
    description="该配置相对项目当前配置是否包含实质性变更，无法判断时为 null",
  )


class ProjectUpdateRequest(BaseModel):
  """更新项目设置的请求体，仅需提交要修改的字段（对应项目设置页可编辑项）。"""

  title: str | None = Field(None, description="项目标题")
  description: str | None = Field(None, description="项目描述")
  label_config: str | None = Field(None, description="标注界面配置（Label Studio XML）")
  expert_instruction: str | None = Field(None, description="标注说明（HTML）")
  show_instruction: bool | None = Field(None, description="标注前是否展示说明")
  show_skip_button: bool | None = Field(None, description="是否显示跳过按钮")
  enable_empty_annotation: bool | None = Field(None, description="是否允许提交空标注")
  show_annotation_history: bool | None = Field(None, description="是否向标注员展示标注历史")
  color: str | None = Field(None, description="项目颜色")
  maximum_annotations: int | None = Field(None, description="每个任务的最大标注数")
  is_published: bool | None = Field(None, description="是否发布给标注员")
  model_version: str | None = Field(None, description="指定模型版本")
  is_draft: bool | None = Field(None, description="项目是否处于草稿（创建中）状态")
  min_annotations_to_start_training: int | None = Field(None, description="触发模型训练所需的最少已完成任务数")
  start_training_on_annotation_update: bool | None = Field(None, description="标注提交/更新后是否立即启动训练")
  show_collab_predictions: bool | None = Field(None, description="标注员是否可见模型预测")
  sampling: str | None = Field(
    None,
    description="采样策略：Sequential sampling / Uniform sampling / Uncertainty sampling",
  )
  show_ground_truth_first: bool | None = Field(None, description="是否优先展示标准答案任务")
  annotator_evaluation_enabled: bool | None = Field(None, description="是否启用标注员评估")
  show_overlap_first: bool | None = Field(None, description="是否优先展示重叠任务")
  overlap_cohort_percentage: int | None = Field(None, description="重叠任务百分比")
  task_data_login: str | None = Field(None, description="任务数据访问用户名")
  task_data_password: str | None = Field(None, description="任务数据访问密码")
  control_weights: dict[str, Any] | None = Field(None, description="各控制标签的权重配置")
  evaluate_predictions_automatically: bool | None = Field(None, description="加载任务时是否自动获取并展示预测")
  skip_queue: str | None = Field(
    None,
    description="跳过队列策略：REQUEUE_FOR_ME / REQUEUE_FOR_OTHERS / IGNORE_SKIPPED",
  )
  reveal_preannotations_interactively: bool | None = Field(None, description="是否以交互方式展示预标注")
  organization: int | None = Field(None, description="所属组织 ID")


class ReimportRequest(BaseModel):
  """重新导入任务的请求体（对应项目 → 数据导入 → Reimport）。"""

  file_upload_ids: list[int] = Field(
    default_factory=list,
    description="要重新导入的文件上传 ID 列表，为空时不做任何处理",
  )
  files_as_tasks_list: bool = Field(
    True,
    description="是否把每个文件当作一个任务（否则按文件内的每条记录生成任务）",
  )


class ReimportResponse(BaseModel):
  """重新导入任务的结果。"""

  task_count: int = Field(0, description="导入的任务数")
  annotation_count: int = Field(0, description="导入的标注数")
  prediction_count: int = Field(0, description="导入的预测数")
  duration: float = Field(0, description="处理耗时（秒）")
  file_upload_ids: list[int] = Field(default_factory=list, description="本次处理的文件上传 ID 列表")
  found_formats: dict[str, Any] | None = Field(None, description="检测到的文件格式统计")
  data_columns: list[Any] = Field(default_factory=list, description="数据列")
  reimport: int | None = Field(
    None,
    description="异步重导任务 ID（企业版返回，可用于查询进度）",
  )


class ProjectModelVersionsResponse(BaseModel):
  """项目可用的模型版本列表（对应项目设置 → Model → Version）。"""

  extended: bool = Field(False, description="是否返回扩展信息（含 static/live 分组）")
  versions: list[Any] = Field(default_factory=list, description="模型版本列表（非扩展模式）")
  static: list[Any] | None = Field(None, description="静态（训练产出）模型版本列表（扩展模式）")
  live: list[Any] | None = Field(
    None,
    description="在线模型连接列表（扩展模式且 include_live_models=true 时返回）",
  )


class ProjectModelVersionsDeleteRequest(BaseModel):
  """删除某个模型版本在该项目下产生的全部预测。"""

  model_version: str = Field(description="要删除预测的模型版本名称")


class ProjectModelVersionsDeleteResponse(BaseModel):
  """删除模型版本预测的结果。"""

  project_id: int = Field(description="项目 ID")
  model_version: str = Field(description="被删除的模型版本")
  deleted_predictions: int = Field(0, description="删除的预测数量")


class ProjectSummaryResetResponse(BaseModel):
  """项目汇总（summary）重置结果。"""

  project_id: int = Field(description="项目 ID")
  status: str = Field("ok", description="操作状态")
  message: str = Field("汇总数据已重新计算", description="操作结果说明")
