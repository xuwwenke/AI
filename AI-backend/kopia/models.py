from sqlalchemy import (
    Column,
    BigInteger,
    String,
    DateTime
)
from database import Base

class BackupTask(Base):
    __tablename__ = "pvc_backup_task"
    
    id = Column(
        BigInteger,
        primary_key=True,
        autoincrement=True
    )
    namespace_name = Column(String(128))
    pvc_name = Column(String(255))
    job_name = Column(String(255))
    task_type = Column(String(20))
    status = Column(String(20))
    create_time = Column(DateTime)
    finish_time = Column(DateTime)


class Snapshot(Base):
    __tablename__ = "pvc_snapshot"

    id = Column(
        BigInteger,
        primary_key=True,
        autoincrement=True
    )
    task_id = Column(BigInteger)
    namespace_name = Column(String(128))
    pvc_name = Column(String(255))
    snapshot_id = Column(String(255))
    create_time = Column(DateTime)