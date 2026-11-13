from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

import os
from urllib.parse import quote_plus

user = os.getenv("MYSQL_USER", "root")
pwd = quote_plus(os.getenv("MYSQL_PASSWORD", "netDc@#su"))

host = os.getenv("MYSQL_HOST", "mysql1-mysql.ailabx.svc.cluster.local")

MYSQL_URL = f"mysql+pymysql://{user}:{pwd}@{host}:3306/kubeflow"

# MYSQL_URL = (
    # "mysql+pymysql://root:netDc@#su@mysql1-mysql.ailabx.svc.cluster.local:3306/kubeflow"
# )

engine = create_engine(
    MYSQL_URL,
    pool_pre_ping=True
)

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine
)

Base = declarative_base()