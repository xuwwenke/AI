import os
from dotenv import load_dotenv

load_dotenv()


def get_ls_config() -> tuple[str, str]:
  url = os.getenv("LABEL_STUDIO_URL")
  api_key = os.getenv("LABEL_STUDIO_API_KEY")

  if not url:
    raise EnvironmentError("缺少环境变量 LABEL_STUDIO_URL")
  if not api_key:
    raise EnvironmentError("缺少环境变量 LABEL_STUDIO_API_KEY")

  return url.rstrip("/"), api_key