from typing import Any, Literal

from pydantic import BaseModel, Field

AuthMethod = Literal["NONE", "BASIC_AUTH"]


class ModelItem(BaseModel):
  """已连接到项目的模型（Label Studio ML Backend）。"""

  id: int = Field(description="模型连接 ID")
  project: int = Field(description="所属项目 ID")
  title: str = Field("", description="模型名称")
  url: str = Field("", description="模型服务地址（ML Backend URL）")
  description: str = Field("", description="模型描述")
  auth_method: str | None = Field(None, description="访问模型服务的鉴权方式：NONE 或 BASIC_AUTH")
  basic_auth_user: str | None = Field(None, description="Basic Auth 用户名")
  basic_auth_pass_is_set: bool | None = Field(None, description="是否已设置 Basic Auth 密码")
  is_interactive: bool = Field(False, description="是否用于交互式预标注")
  auto_update: bool | None = Field(None, description="是否自动从模型服务获取最新版本")
  model_version: str | None = Field(None, description="当前模型版本")
  timeout: float | None = Field(None, description="模型响应超时时间（秒）")
  state: str | None = Field(None, description="模型状态码：CO 已连接、DI 已断开、ER 错误、TR 训练中、PR 预测中")
  readable_state: str | None = Field(None, description="模型状态的可读描述")
  error_message: str | None = Field(None, description="错误状态下的错误信息")
  extra_params: Any = Field(None, description="连接模型时传给模型服务的额外参数")
  created_at: str | None = Field(None, description="创建时间")
  updated_at: str | None = Field(None, description="最后更新时间")


class ModelListResponse(BaseModel):
  """项目已连接的模型列表。"""

  project_id: int = Field(description="项目 ID")
  total: int = Field(description="模型数量")
  items: list[ModelItem] = Field(default_factory=list, description="模型列表")


class ModelConnectRequest(BaseModel):
  """连接模型的请求体（对应项目设置 → Model → Connect Model）。"""

  url: str = Field(description="模型服务地址，如 http://localhost:9090")
  title: str = Field(description="模型名称")
  description: str = Field("", description="模型描述")
  auth_method: AuthMethod = Field("NONE", description="访问模型服务的鉴权方式：NONE 或 BASIC_AUTH")
  basic_auth_user: str | None = Field(None, description="Basic Auth 用户名")
  basic_auth_pass: str | None = Field(None, description="Basic Auth 密码")
  is_interactive: bool = Field(False, description="是否用于交互式预标注")
  auto_update: bool = Field(False, description="是否自动从模型服务获取最新版本")
  model_version: str | None = Field(None, description="指定模型版本")
  timeout: float | None = Field(None, description="模型响应超时时间（秒）")
  extra_params: Any = Field(None, description="传给模型服务的额外参数")


class ModelUpdateRequest(BaseModel):
  """更新模型连接的请求体，仅需提交要修改的字段。"""

  url: str | None = Field(None, description="模型服务地址")
  title: str | None = Field(None, description="模型名称")
  description: str | None = Field(None, description="模型描述")
  auth_method: AuthMethod | None = Field(None, description="访问模型服务的鉴权方式：NONE 或 BASIC_AUTH")
  basic_auth_user: str | None = Field(None, description="Basic Auth 用户名")
  basic_auth_pass: str | None = Field(None, description="Basic Auth 密码")
  is_interactive: bool | None = Field(None, description="是否用于交互式预标注")
  auto_update: bool | None = Field(None, description="是否自动从模型服务获取最新版本")
  model_version: str | None = Field(None, description="指定模型版本")
  timeout: float | None = Field(None, description="模型响应超时时间（秒）")
  extra_params: Any = Field(None, description="传给模型服务的额外参数")


class ModelDeleteResponse(BaseModel):
  """断开模型连接的结果。"""

  id: int = Field(description="被断开的模型连接 ID")
  message: str = Field("已断开模型连接", description="操作结果说明")


class ModelTrainRequest(BaseModel):
  """训练模型的请求体。"""

  use_ground_truth: bool | None = Field(None, description="是否把标准答案（Ground Truth）纳入训练")


class ModelPredictRequest(BaseModel):
  """使用模型为项目内任务批量生成预测的请求体。"""

  batch_size: int | None = Field(None, description="本次预测的任务数量，为空时由 Label Studio 计算")


class ModelActionResponse(BaseModel):
  """模型操作（训练、预测等）的结果。"""

  id: int = Field(description="模型连接 ID")
  message: str = Field(description="操作结果说明")


class ModelVersionsResponse(BaseModel):
  """模型可用版本列表。"""

  id: int = Field(description="模型连接 ID")
  versions: list[str] = Field(default_factory=list, description="可用版本列表")
  message: str | None = Field(None, description="操作结果说明")


class ModelPredictTestResponse(BaseModel):
  """测试预测（Test Prediction）的结果，对应项目设置 → Model → Test。"""

  model_id: int = Field(description="模型连接 ID")
  status: int | None = Field(None, description="模型服务返回的 HTTP 状态码")
  error_message: str | None = Field(None, description="预测失败时的错误信息")
  url: str | None = Field(None, description="实际请求的模型预测地址")
  task: dict[str, Any] | None = Field(None, description="本次测试使用的任务数据")
  request: dict[str, Any] | None = Field(None, description="发送给模型服务的请求体")
  response: Any = Field(None, description="模型服务返回的预测结果")
