from flask_appbuilder import Model
from sqlalchemy.orm import relationship
from sqlalchemy import Column, Integer, String, ForeignKey, DateTime
from myapp.models.helpers import AuditMixinNullable
from myapp.models.base import MyappModelBase
from myapp.models.model_team import Project
import datetime

class Experiment(Model, AuditMixinNullable, MyappModelBase):
    __tablename__ = 'ai_experiment'
    
    id = Column(Integer, primary_key=True, autoincrement=True, comment='id主键')
    name = Column(String(100), nullable=False, comment='实验名称')
    project_id = Column(Integer, ForeignKey('project.id'), comment='项目组id')
    project = relationship("Project", foreign_keys=[project_id], lazy='selectin')
    
    start_time = Column(DateTime, comment='开始时间')
    end_time = Column(DateTime, comment='结束时间')
    status = Column(Integer, comment='状态')
    model_name = Column(String(255), comment='模型名称')
    config = Column(String(2048), comment='配置信息')
    desc = Column(String(1024), comment='描述')


class ExperimentPipeline(Model, MyappModelBase):
    __tablename__ = 'ai_experiment_pipeline'
    
    id = Column(Integer, primary_key=True, autoincrement=True, comment='id主键')
    experiment_id = Column(Integer, ForeignKey('ai_experiment.id'), nullable=False, comment='实验id')
    pipeline_id = Column(Integer, ForeignKey('pipeline.id'), nullable=False, comment='流水线id')
    
    experiment = relationship("Experiment", foreign_keys=[experiment_id], lazy='selectin')
    pipeline = relationship("Pipeline", foreign_keys=[pipeline_id], lazy='selectin')

    def __repr__(self):
        return f"<ExperimentPipeline: {self.id}>"