from pydantic import BaseModel, Field


class UserInfo(BaseModel):
  """Label Studio 用户信息。"""

  id: int = Field(description="用户 ID")
  username: str = Field(description="用户名")
  email: str | None = Field(None, description="邮箱，登录时使用")
  first_name: str | None = Field(None, description="名")
  last_name: str | None = Field(None, description="姓")
  is_active: bool | None = Field(None, description="账号是否可用")


class RegisterRequest(BaseModel):
  """注册请求。"""

  email: str = Field(min_length=1, description="邮箱，用于登录，需唯一")
  password: str = Field(min_length=8, description="密码，至少 8 位")


class RegisterResponse(BaseModel):
  """注册结果。"""

  token: str = Field(
    description="该用户的 Label Studio JWT API Token，可用 Authorization: Bearer 调用 Label Studio 接口"
  )
  user: UserInfo = Field(description="创建的用户信息")
  message: str = Field("注册成功", description="操作结果说明")


class LoginRequest(BaseModel):
  """登录请求。"""

  email: str = Field(min_length=1, description="登录邮箱")
  password: str = Field(min_length=1, description="密码")


class LoginResponse(BaseModel):
  """登录结果。"""

  token: str = Field(
    description="该用户的 Label Studio JWT API Token，可用 Authorization: Bearer 调用 Label Studio 接口"
  )
  user: UserInfo = Field(description="登录的用户信息")
  message: str = Field("登录成功", description="操作结果说明")
