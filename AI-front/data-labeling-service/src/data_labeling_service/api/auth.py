import requests
from fastapi import APIRouter, Body, HTTPException, status
from label_studio_sdk.core.api_error import ApiError

from ..config import get_ls_config
from ..ls_client import get_client
from ..schemas.auth import (
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  UserInfo,
)

router = APIRouter(prefix="/api/auth", tags=["auth"])

REQUEST_TIMEOUT = 15


def _raise_api_error(e: ApiError) -> None:
  raise HTTPException(
    status_code=e.status_code or 502,
    detail=f"Label Studio API 错误: {str(e.body)[:300]}",
  )


def _to_user_info(user) -> UserInfo:
  """把 SDK / API 返回的用户（pydantic 模型或 dict）转换为统一的用户信息。"""
  data = user.model_dump() if hasattr(user, "model_dump") else dict(user)

  return UserInfo(
    id=data["id"],
    username=data.get("username") or "",
    email=data.get("email"),
    first_name=data.get("first_name"),
    last_name=data.get("last_name"),
    is_active=data.get("is_active"),
  )


def _email_exists(email: str) -> bool:
  """检查 Label Studio 中是否已存在该邮箱的用户。"""
  target = email.strip().lower()
  return any((u.email or "").strip().lower() == target for u in get_client().users.list())


def _revoke_api_tokens(base_url: str, session: requests.Session) -> None:
  """撤销该用户在 Label Studio 中已有的 API Token。

  Label Studio 每个用户只允许一个有效 Token，且明文仅在创建时返回一次。
  这里先撤销旧 Token，保证注册/登录每次都能拿到可直接使用的新 Token。
  撤销接口只依赖 Token 中的 jti，因此用 GET /api/token/ 返回的截断 Token 即可
  （Label Studio 前端的 Revoke 按钮也是同样的做法）。
  """
  try:
    response = session.get(f"{base_url}/api/token/", timeout=REQUEST_TIMEOUT)
  except requests.RequestException as e:
    raise HTTPException(status_code=502, detail=f"获取 Label Studio API Token 列表失败: {e}")

  if response.status_code != 200:
    raise HTTPException(
      status_code=502,
      detail=f"获取 Label Studio API Token 列表失败: {response.text[:300]}",
    )

  csrf_token = session.cookies.get("csrftoken")
  if not csrf_token:
    raise HTTPException(status_code=502, detail="未获取到 Label Studio CSRF Token")

  for item in response.json():
    token = item.get("token")
    if not token:
      continue

    try:
      revoke = session.post(
        f"{base_url}/api/token/blacklist/",
        json={"refresh": token},
        headers={"X-CSRFToken": csrf_token, "Referer": base_url},
        timeout=REQUEST_TIMEOUT,
      )
    except requests.RequestException as e:
      raise HTTPException(status_code=502, detail=f"撤销 Label Studio 旧 API Token 失败: {e}")

    # 204 为撤销成功，404 表示该 Token 已失效，其余视为失败
    if revoke.status_code not in (204, 404):
      raise HTTPException(
        status_code=502,
        detail=f"撤销 Label Studio 旧 API Token 失败: {revoke.text[:300]}",
      )


def _create_api_token(base_url: str, session: requests.Session) -> str:
  """用已登录的会话为该用户创建 Label Studio JWT API Token。

  Label Studio 每个用户只能有一个有效 Token，且明文仅在创建时返回一次，
  这里先撤销该用户已有的 Token，因此重复注册/登录都能拿到新的可用 Token。
  """
  _revoke_api_tokens(base_url, session)

  csrf_token = session.cookies.get("csrftoken")
  if not csrf_token:
    raise HTTPException(status_code=502, detail="未获取到 Label Studio CSRF Token")

  try:
    response = session.post(
      f"{base_url}/api/token/",
      headers={"X-CSRFToken": csrf_token, "Referer": base_url},
      timeout=REQUEST_TIMEOUT,
    )
  except requests.RequestException as e:
    raise HTTPException(status_code=502, detail=f"创建 Label Studio API Token 失败: {e}")

  if response.status_code == 409:
    raise HTTPException(
      status_code=409,
      detail=(
        "该用户在 Label Studio 中已存在有效的 API Token，"
        "Label Studio 仅在创建时返回 Token 明文、无法重复获取；"
        "可在 Label Studio 的 Account Settings 中撤销旧 Token 后重新登录。"
      ),
    )

  if response.status_code != 201:
    raise HTTPException(
      status_code=502,
      detail=f"创建 Label Studio API Token 失败: {response.text[:300]}",
    )

  token = response.json().get("token")
  if not token:
    raise HTTPException(status_code=502, detail="Label Studio 未返回 API Token")
  return token


@router.post("/register", response_model=RegisterResponse, status_code=status.HTTP_201_CREATED)
async def register(payload: RegisterRequest = Body(description="注册信息")) -> RegisterResponse:
  """
  注册用户，实际在 Label Studio 中创建账号。

  Label Studio 没有可设置密码的 JSON 建用户接口（/api/users/ 会忽略 password），
  因此这里走它的注册表单接口 /user/signup/，注册出的账号可直接用于登录。
  注册成功后的会话已处于登录态，与登录接口一致地返回该用户的 JWT API Token。
  """
  base_url, _ = get_ls_config()
  signup_url = f"{base_url}/user/signup/"

  try:
    exists = _email_exists(payload.email)
  except ApiError as e:
    _raise_api_error(e)

  if exists:
    raise HTTPException(status_code=409, detail="该邮箱已被注册")

  with requests.Session() as session:
    # 1. 打开注册页，获取 CSRF Token（Label Studio 注册接口有 CSRF 校验）
    try:
      page = session.get(signup_url, timeout=REQUEST_TIMEOUT)
      page.raise_for_status()
    except requests.RequestException as e:
      raise HTTPException(status_code=502, detail=f"无法访问 Label Studio: {e}")

    csrf_token = session.cookies.get("csrftoken")
    if not csrf_token:
      raise HTTPException(status_code=502, detail="未获取到 Label Studio CSRF Token")

    # 2. 提交邮箱 + 密码，Label Studio 会创建用户并返回重定向
    try:
      response = session.post(
        signup_url,
        data={"email": payload.email, "password": payload.password},
        headers={"X-CSRFToken": csrf_token, "Referer": signup_url},
        timeout=REQUEST_TIMEOUT,
        allow_redirects=False,
      )
    except requests.RequestException as e:
      raise HTTPException(status_code=502, detail=f"Label Studio 注册请求失败: {e}")

    # 注册成功返回 3xx 重定向；校验失败会重新渲染注册页（200）
    if response.status_code not in (301, 302):
      raise HTTPException(
        status_code=400,
        detail="注册失败，邮箱或密码不符合 Label Studio 的要求",
      )

    # 3. 读取注册出的用户信息
    try:
      whoami = session.get(f"{base_url}/api/current-user/whoami", timeout=REQUEST_TIMEOUT)
    except requests.RequestException as e:
      raise HTTPException(status_code=502, detail=f"获取注册用户信息失败: {e}")

    if whoami.status_code != 200:
      raise HTTPException(
        status_code=502,
        detail=f"获取注册用户信息失败: {whoami.text[:300]}",
      )

    # 4. 注册后的会话已登录，为该用户创建 JWT API Token（与登录接口一致）
    token = _create_api_token(base_url, session)
    return RegisterResponse(token=token, user=_to_user_info(whoami.json()))


@router.post("/login", response_model=LoginResponse)
async def login(payload: LoginRequest = Body(description="登录信息")) -> LoginResponse:
  """
  登录，校验 Label Studio 账号密码。

  登录成功后为该用户在 Label Studio 创建 JWT API Token，并连同用户信息一起返回，
  前端可凭该 Token 以 `Authorization: Bearer <token>` 直接调用 Label Studio 接口
  （若为 refresh token，需先用 `POST /api/token/refresh/` 换取 access token）。
  """
  base_url, _ = get_ls_config()
  login_url = f"{base_url}/user/login/"

  with requests.Session() as session:
    # 1. 打开登录页，获取 CSRF Token（Label Studio 登录接口有 CSRF 校验）
    try:
      page = session.get(login_url, timeout=REQUEST_TIMEOUT)
      page.raise_for_status()
    except requests.RequestException as e:
      raise HTTPException(status_code=502, detail=f"无法访问 Label Studio: {e}")

    csrf_token = session.cookies.get("csrftoken")
    if not csrf_token:
      raise HTTPException(status_code=502, detail="未获取到 Label Studio CSRF Token")

    # 2. 提交邮箱 + 密码，Label Studio 以 session 形式维持登录状态
    try:
      session.post(
        login_url,
        data={"email": payload.email, "password": payload.password},
        headers={"X-CSRFToken": csrf_token, "Referer": login_url},
        timeout=REQUEST_TIMEOUT,
        allow_redirects=False,
      )
    except requests.RequestException as e:
      raise HTTPException(status_code=502, detail=f"Label Studio 登录请求失败: {e}")

    # 3. 用登录后的会话校验凭据，未登录时 whoami 返回 401
    try:
      whoami = session.get(f"{base_url}/api/current-user/whoami", timeout=REQUEST_TIMEOUT)
    except requests.RequestException as e:
      raise HTTPException(status_code=502, detail=f"校验登录状态失败: {e}")

    if whoami.status_code == 401:
      raise HTTPException(status_code=401, detail="邮箱或密码错误")
    if whoami.status_code != 200:
      raise HTTPException(status_code=502, detail=f"校验登录状态失败: {whoami.text[:300]}")

    token = _create_api_token(base_url, session)
    return LoginResponse(token=token, user=_to_user_info(whoami.json()))
