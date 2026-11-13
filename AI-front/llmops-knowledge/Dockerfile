
FROM python:3.11-slim-bookworm AS builder

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1 \
    PIP_INDEX_URL=https://pypi.tuna.tsinghua.edu.cn/simple \
    PIP_TRUSTED_HOST=pypi.tuna.tsinghua.edu.cn

WORKDIR /app

# 1. 配置 Debian APT 镜像源
RUN sed -i \
    's|http://deb.debian.org/debian|https://mirrors.tuna.tsinghua.edu.cn/debian|g' \
    /etc/apt/sources.list.d/debian.sources \
    && sed -i \
    's|http://deb.debian.org/debian-security|https://mirrors.tuna.tsinghua.edu.cn/debian-security|g' \
    /etc/apt/sources.list.d/debian.sources

# 2. 安装 mysqlclient 编译依赖
RUN apt-get update \
    && apt-get install --no-install-recommends -y \
       build-essential \
       default-libmysqlclient-dev \
       pkg-config \
    && rm -rf /var/lib/apt/lists/*

# 3. 安装 Python 依赖
COPY requirements.txt ./

RUN python -m venv /app/.venv \
    && /app/.venv/bin/python -m pip install \
       --no-cache-dir \
       -r requirements.txt \
    && /app/.venv/bin/python -m pip check

# 4. 复制 Django 项目
COPY config ./config
COPY knowledge ./knowledge
COPY documents ./documents
COPY chunks ./chunks
COPY metadata ./metadata
COPY models ./models
COPY tags ./tags
COPY manage.py gunicorn.conf.py ./

# 5. 收集静态资源
RUN DJANGO_SETTINGS_MODULE=config.production \
    DJANGO_SECRET_KEY=build-only-placeholder-not-used-at-runtime \
    DJANGO_ALLOWED_HOSTS=localhost \
    .venv/bin/python manage.py collectstatic --noinput


# ================= Runtime =================

FROM python:3.11-slim-bookworm AS runtime

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    DJANGO_SETTINGS_MODULE=config.production \
    PATH="/app/.venv/bin:$PATH" \
    HOME=/tmp

WORKDIR /app

# 6. 配置运行阶段 Debian 镜像源
RUN sed -i \
    's|http://deb.debian.org/debian|https://mirrors.tuna.tsinghua.edu.cn/debian|g' \
    /etc/apt/sources.list.d/debian.sources \
    && sed -i \
    's|http://deb.debian.org/debian-security|https://mirrors.tuna.tsinghua.edu.cn/debian-security|g' \
    /etc/apt/sources.list.d/debian.sources

# 7. 安装运行时依赖
RUN apt-get update \
    && apt-get install --no-install-recommends -y \
       libmariadb3 \
       ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app \
       --no-create-home --home-dir /tmp app

# 8. 复制构建产物
COPY --from=builder /app /app

USER 10001:10001

EXPOSE 8000
STOPSIGNAL SIGTERM

CMD ["gunicorn", "--config", "gunicorn.conf.py", "config.wsgi:application"]
