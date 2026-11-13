from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from src.data_labeling_service.api.annotations import router as annotations_router
from src.data_labeling_service.api.auth import router as auth_router
from src.data_labeling_service.api.datasets import router as datasets_router
from src.data_labeling_service.api.exports import router as exports_router
from src.data_labeling_service.api.ml_backends import router as ml_backends_router
from src.data_labeling_service.api.models import router as models_router
from src.data_labeling_service.api.predictions import router as predictions_router
from src.data_labeling_service.api.projects import router as projects_router
from src.data_labeling_service.api.templates import router as templates_router
from src.data_labeling_service.ls_client import get_client


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时初始化一次客户端，验证连接
    client = get_client()
    try:
        me = client.users.whoami()
        print(f"[启动] 已连接 Label Studio，用户: {me.username}")
    except Exception as e:
        print(f"[启动] 连接 Label Studio 失败: {e}")
    yield
    # 关闭时如有需要可清理资源


app = FastAPI(
    title="Data Labeling Service",
    description="封装 Label Studio 接口的 FastAPI 服务",
    version="0.1.0",
    lifespan=lifespan,
    debug=True,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(projects_router)
app.include_router(datasets_router)
app.include_router(annotations_router)
app.include_router(exports_router)
app.include_router(models_router)
app.include_router(predictions_router)
app.include_router(ml_backends_router)
app.include_router(templates_router)


@app.get("/health")
async def health():
    return {"status": "ok"}