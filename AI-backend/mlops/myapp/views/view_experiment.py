from flask_babel import lazy_gettext as _
from myapp.views.baseSQLA import MyappSQLAInterface as SQLAInterface
from myapp import app, appbuilder, db
from .baseApi import MyappModelRestApi
from myapp.models.model_experiment import Experiment, ExperimentPipeline
from .base import MyappFilter
from flask import g
from sqlalchemy import or_

conf = app.config


class Experiment_Filter(MyappFilter):
    def apply(self, query, func):
        return query.filter(
            or_(
                self.model.owner.contains(g.user.username),
                self.model.owner.contains('*')
            )
        )


class Experiment_ModelView_base():
    label_title = _('实验管理')
    datamodel = SQLAInterface(Experiment)
    base_permissions = ['can_add', 'can_show', 'can_edit', 'can_list', 'can_delete']
    base_order = ("id", "desc")
    order_columns = ['id']
    base_filters = [["id", Experiment_Filter, lambda: []]]

    add_columns = ['name', 'project', 'start_time', 'end_time', 'status', 'model_name', 'config', 'desc']
    show_columns = ['id', 'name', 'project', 'start_time', 'end_time', 'status', 'model_name', 'config', 'desc', 'created_by', 'created_on', 'changed_by', 'changed_on']
    edit_columns = add_columns
    list_columns = ['name', 'project', 'status', 'model_name', 'start_time', 'end_time', 'created_on']
    search_columns = ['name', 'model_name', 'desc']

    spec_label_columns = {
        "name": _("实验名称"),
        "project": _("项目组"),
        "start_time": _("开始时间"),
        "end_time": _("结束时间"),
        "status": _("状态"),
        "model_name": _("模型名称"),
        "config": _("配置信息"),
        "desc": _("描述"),
    }


class Experiment_Api(Experiment_ModelView_base, MyappModelRestApi):
    route_base = '/experiment/api'


class ExperimentPipeline_ModelView_base():
    label_title = _('实验流水线关联')
    datamodel = SQLAInterface(ExperimentPipeline)
    base_permissions = ['can_add', 'can_show', 'can_edit', 'can_list', 'can_delete']
    base_order = ("id", "desc")
    order_columns = ['id']

    add_columns = ['experiment', 'pipeline']
    show_columns = ['id', 'experiment', 'pipeline']
    edit_columns = add_columns
    list_columns = ['id', 'experiment', 'pipeline']
    search_columns = ['experiment', 'pipeline']

    spec_label_columns = {
        "experiment": _("实验"),
        "pipeline": _("流水线"),
    }


class ExperimentPipeline_Api(ExperimentPipeline_ModelView_base, MyappModelRestApi):
    route_base = '/experiment_pipeline/api'


appbuilder.add_api(Experiment_Api)
appbuilder.add_api(ExperimentPipeline_Api)