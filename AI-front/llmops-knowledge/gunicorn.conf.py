"""可通过环境变量调整的容器进程配置。"""

import os


bind = "0.0.0.0:8000"
worker_class = "gthread"
workers = int(os.environ.get("GUNICORN_WORKERS", "2"))
threads = int(os.environ.get("GUNICORN_THREADS", "4"))
timeout = int(os.environ.get("GUNICORN_TIMEOUT", "180"))
graceful_timeout = int(os.environ.get("GUNICORN_GRACEFUL_TIMEOUT", "30"))
keepalive = 5
worker_tmp_dir = "/tmp"
accesslog = "-"
errorlog = "-"
capture_output = True
