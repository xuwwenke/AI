FROM python:3.11-slim

WORKDIR /app

# 安装 uv
RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple  --no-cache-dir uv

# 复制依赖配置和锁文件
COPY pyproject.toml uv.lock README.md ./

# 复制源码
COPY main.py ./
COPY src/ ./src/

# 根据锁文件安装项目及依赖
RUN uv sync --frozen --no-dev

# 配置 Python 环境
ENV PATH="/app/.venv/bin:$PATH"
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1

EXPOSE 8000

# 暂定入口为 main.py 中的 app 对象
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
