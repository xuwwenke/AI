<template>
  <div class="paradigm-container">
    <div class="paradigm-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">任务范式</h1>
          <p class="page-subtitle">管理和配置训练任务范式</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="add-btn" size="small" @click="showAddDialog">
            <i class="el-icon-plus"></i> 添加任务模板
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">总范式数</div>
          <div class="stat-value">{{ totalParadigms }}</div>
          <div class="stat-trend up">
            <i class="el-icon-top"></i> {{ monthNewCount }} 本月新增
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">使用中</div>
          <div class="stat-value">{{ inUseCount }}</div>
          <div class="stat-trend">
            <i class="el-icon-check"></i> 全部正常
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">范式类型</div>
          <div class="stat-value">{{ paradigmTypesCount }} 种</div>
          <div class="stat-trend">
            涵盖多领域
          </div>
        </div>
      </div>

      <!-- 过滤条件区域 -->
      <div class="search-section">
        <div class="section-title">任务范式查询</div>
        <el-form :model="searchForm" :inline="true" class="search-form" label-position="top">
          <el-form-item label="功能分类">
            <el-select v-model="searchForm.projectId" placeholder="全部" clearable style="width: 280px;">
              <el-option v-for="item in jobTemplateTypes" :key="item.id" :label="item.name"
                :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="镜像">
            <el-select v-model="searchForm.images" placeholder="全部" clearable style="width: 280px;">
              <el-option v-for="item in imageOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="创建者">
            <el-input v-model="searchForm.creator" placeholder="创建者" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="修改者">
            <el-input v-model="searchForm.modifier" placeholder="修改者" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker v-model="searchForm.createTime" type="daterange" value-format="yyyy-MM-dd"
              start-placeholder="年/月/日" end-placeholder="年/月/日" style="width: 280px;"></el-date-picker>
          </el-form-item>
          <el-form-item label="修改时间">
            <el-date-picker v-model="searchForm.modified" type="daterange" value-format="yyyy-MM-dd"
              start-placeholder="年/月/日" end-placeholder="年/月/日" style="width: 280px;"></el-date-picker>
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="searchForm.name" placeholder="名称" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="版本">
            <el-input v-model="searchForm.version" placeholder="版本" clearable style="width: 280px;"></el-input>
          </el-form-item>
        </el-form>
        <div class="search-actions">
          <el-button size="small" @click="handleReset">重置</el-button>
          <el-button type="primary" size="small" @click="handleSearch">查询</el-button>
        </div>
      </div>

      <!-- 范式列表 -->
      <div class="table-section">
        <div class="table-header">
          <div class="header-left-group">
            <div class="section-title">范式列表</div>
            <el-radio-group v-model="searchForm.ifPublic" size="small" @change="handleSearch">
              <el-radio-button :label="true">公有</el-radio-button>
              <el-radio-button :label="false">私有</el-radio-button>
            </el-radio-group>
          </div>
          <div class="table-actions">
            <el-button size="small" @click="showBatchDialog">批量操作</el-button>
          </div>
        </div>
        <el-table :data="paradigmList" border style="width: 100%;" class="paradigm-table"
          @selection-change="handleSelectionChange" v-loading="loading">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="project" label="功能分类">
            <template slot-scope="scope">
              {{ getCategoryName(scope.row.project) }}
            </template>
          </el-table-column>
          <el-table-column label="名称">
            <template slot-scope="scope">
              <div class="name-cell">
                <div class="name-title">{{ scope.row.name }}</div>
                <div class="name-desc">{{ scope.row.describe }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本"></el-table-column>
          <el-table-column prop="creator" label="创建者"></el-table-column>
          <el-table-column prop="modified" label="修改时间"></el-table-column>
          <el-table-column label="操作" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="showDetailDialog(scope.row)">详情</el-button>
              <el-button type="text" size="mini" @click="showEditDialog(scope.row)">修改</el-button>
              <el-tooltip content="公有模板不可删除" placement="top" :disabled="!searchForm.ifPublic">
                <span style="margin-left: 10px;">
                  <el-button type="text" size="mini" :style="{ color: searchForm.ifPublic ? '#C0C4CC' : '#f56c6c' }"
                    :disabled="searchForm.ifPublic" @click="handleDelete(scope.row)">删除</el-button>
                </span>
              </el-tooltip> </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <div class="pagination-left">
            显示 {{ paginationStart }}-{{ paginationEnd }} 条，共 {{ total }} 条
          </div>
          <div class="pagination-right">
            <el-select v-model="pageSize" size="small" style="width: 100px; margin-right: 10px;"
              @change="handleSizeChange">
              <el-option :label="size + '条/页'" :value="size" v-for="size in [10, 20, 50, 100]" :key="size"></el-option>
            </el-select>
            <el-pagination :current-page="currentPage" :page-size="pageSize" layout="prev, pager, next" :total="total"
              @current-change="handlePageChange" background></el-pagination>
          </div>
        </div>
      </div>
    </div>

    <!-- 批量操作弹窗 -->
    <el-dialog title="批量操作" :visible.sync="batchDialogVisible" width="400px" :close-on-click-modal="false"
      class="batch-dialog">
      <div class="batch-content">
        <div class="batch-info">
          <i class="el-icon-info"></i>
          <span>已选择 {{ selectedRows.length }} 个任务模板</span>
        </div>
        <div class="batch-buttons">
          <el-button type="primary" size="medium" icon="el-icon-copy-document" @click="handleBatchCopy"
            :loading="batchCopyLoading">
            批量复制
          </el-button>

        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchDialogVisible = false">关闭</el-button>

      </span>
    </el-dialog>

    <!-- 添加任务模板弹框 -->
    <el-dialog title="添加任务模板" :visible.sync="addDialogVisible" :close-on-click-modal="false" class="add-paradigm-dialog"
      width="800px">
      <el-form :model="addForm" :rules="addRules" ref="addForm" label-width="120px">
        <el-form-item label="功能分类" prop="project">
          <el-select v-model="addForm.project" placeholder="请选择功能分类" style="width: 100%;">
            <el-option v-for="item in jobTemplateTypes" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="镜像" prop="images">
          <el-select v-model="addForm.images" placeholder="请选择镜像" style="width: 100%;" filterable allow-create>
            <el-option v-for="item in imageOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入名称"></el-input>
          <div class="form-tip">英文名(小写字母、数字、- 组成)，最长50个字符</div>
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-select v-model="addForm.version" placeholder="请选择版本" style="width: 100%;">
            <el-option label="Release" value="Release"></el-option>
            <el-option label="Alpha" value="Alpha"></el-option>
          </el-select>
          <div class="form-tip">job模板的版本，Release版本的模板才能被所有用户看到</div>
        </el-form-item>
        <el-form-item label="描述" prop="describe">
          <el-input v-model="addForm.describe" placeholder="请输入描述"></el-input>
          <div class="form-tip">模板的描述将直接显示在pipeline编排界面</div>
        </el-form-item>
        <el-form-item label="工作目录" prop="workdir">
          <el-input v-model="addForm.workdir" placeholder="请输入工作目录"></el-input>
          <div class="form-tip">工作目录，不填写将直接使用镜像默认的工作目录</div>
        </el-form-item>
        <el-form-item label="启动命令" prop="entrypoint">
          <el-input v-model="addForm.entrypoint" placeholder="请输入启动命令"></el-input>
          <div class="form-tip">镜像的入口命令，直接写成单行字符串，例如python xx.py，无需添加[]</div>
        </el-form-item>
        <el-form-item label="挂载目录" prop="volume_mount">
          <el-input v-model="addForm.volume_mount" placeholder="请输入挂载目录"></el-input>
          <div class="form-tip">使用该模板的task，会在添加时，自动添加该挂载。格式示例:
            $pvc_name1(pvc):/$container_path1,$hostpath1(hostpath):/$container_path2 注意pvc会自动挂载对应目录下的个人username子目录</div>
        </el-form-item>
        <el-form-item label="启动参数" prop="args" class="json-form-item">
          <div class="json-editor-header">
            <el-button type="primary" size="mini" class="format-btn"
              @click="formatJson(addForm, 'args')">格式化</el-button>
          </div>
          <el-input v-model="addForm.args" type="textarea" :rows="10" placeholder="请输入启动参数"></el-input>
          <div class="form-tip"><i class="el-icon-question"></i> 详情 json格式，此类task使用时需要填写的参数</div>
        </el-form-item>
        <el-form-item label="环境变量" prop="env">
          <el-input v-model="addForm.env" type="textarea" :rows="4" placeholder="请输入环境变量"></el-input>
          <div class="form-tip">使用模板的task自动添加的环境变量，支持模板变量。书写格式:每行一个环境变量env_key=env_value</div>
        </el-form-item>
        <el-form-item label="host" prop="host_aliases">
          <el-input v-model="addForm.host_aliases" type="textarea" :rows="4" placeholder="请输入host"></el-input>
          <div class="form-tip">添加到容器内的host映射。书写格式:每行一个dns解析记录，ip host1 host2，示例: 1.1.1.1 example1.oa.com
            example2.oa.com
          </div>
        </el-form-item>
        <el-form-item label="特权模式" prop="privileged">
          <el-radio-group v-model="addForm.privileged">
            <el-radio :label="true">yes</el-radio>
            <el-radio :label="false">no</el-radio>
          </el-radio-group>
          <div class="form-tip">是否启动超级权限</div>
        </el-form-item>
        <el-form-item label="k8s账号" prop="accounts">
          <el-input v-model="addForm.accounts" placeholder="请输入k8s账号"></el-input>
          <div class="form-tip">k8s的ServiceAccount，在此类任务运行时会自动挂载此账号，多用于任务操作k8s pod时使用</div>
        </el-form-item>
        <el-form-item label="扩展" prop="expand" class="json-form-item">
          <div class="json-editor-header">
            <el-button type="primary" size="mini" class="format-btn"
              @click="formatJson(addForm, 'expand')">格式化</el-button>
          </div>
          <el-input v-model="addForm.expand" type="textarea" :rows="5" placeholder="请输入扩展字段"></el-input>
          <div class="form-tip">json格式的扩展字段，支持 "index":"$模板展示顺序号", "help_url":"$帮助文档地址", "HostNetwork":true 启动主机端口监听
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSubmit">确定</el-button>
      </span>
    </el-dialog>

    <!-- 任务范式详情弹框 -->
    <el-dialog title="任务范式详情" :visible.sync="detailDialogVisible" :close-on-click-modal="false"
      class="detail-paradigm-dialog">
      <div class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">功能分类</div>
              <div class="detail-value">{{ getCategoryName(detailData.project) || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">镜像</div>
              <div class="detail-value">{{ getImageName(detailData.image) || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">名称</div>
              <div class="detail-value">{{ detailData.name || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">版本</div>
              <div class="detail-value">{{ detailData.version || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">描述</div>
              <div class="detail-value">{{ detailData.describe || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">工作目录</div>
              <div class="detail-value">{{ detailData.workdir || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">挂载目录</div>
              <div class="detail-value">{{ detailData.volume_mount || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">启动命令</div>
              <div class="detail-value">{{ detailData.entrypoint || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">启动参数</div>
              <div class="detail-value">{{ detailData.args || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">环境变量</div>
              <div class="detail-value">{{ detailData.env || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">Host信息</div>
              <div class="detail-value">{{ detailData.hostAliases || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">特权模式</div>
              <div class="detail-value">{{ detailData.privileged ? '是' : '否' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">K8s账号</div>
              <div class="detail-value">{{ detailData.accounts || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">扩展字段</div>
              <div class="detail-value">{{ detailData.expand || '--' }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 修改任务模板弹框 -->
    <el-dialog title="修改任务模板" :visible.sync="editDialogVisible" :close-on-click-modal="false"
      class="edit-paradigm-dialog" width="800px">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="120px">
        <el-form-item label="功能分类" prop="project">
          <el-select v-model="editForm.project" placeholder="请选择功能分类" style="width: 100%;">
            <el-option v-for="item in jobTemplateTypes" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="镜像" prop="images">
          <el-select v-model="editForm.images" placeholder="请选择镜像" style="width: 100%;" filterable allow-create>
            <el-option v-for="item in imageOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入名称"></el-input>
          <div class="form-tip">英文名(小写字母、数字、- 组成)，最长50个字符</div>
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-select v-model="editForm.version" placeholder="请选择版本" style="width: 100%;">
            <el-option label="Release" value="Release"></el-option>
            <el-option label="Alpha" value="Alpha"></el-option>
          </el-select>
          <div class="form-tip">job模板的版本，Release版本的模板才能被所有用户看到</div>
        </el-form-item>
        <el-form-item label="描述" prop="describe">
          <el-input v-model="editForm.describe" placeholder="请输入描述"></el-input>
          <div class="form-tip">模板的描述将直接显示在pipeline编排界面</div>
        </el-form-item>
        <el-form-item label="工作目录" prop="workdir">
          <el-input v-model="editForm.workdir" placeholder="请输入工作目录"></el-input>
          <div class="form-tip">工作目录，不填写将直接使用镜像默认的工作目录</div>
        </el-form-item>
        <el-form-item label="启动命令" prop="entrypoint">
          <el-input v-model="editForm.entrypoint" placeholder="请输入启动命令"></el-input>
          <div class="form-tip">镜像的入口命令，直接写成单行字符串，例如python xx.py，无需添加[]</div>
        </el-form-item>
        <el-form-item label="挂载目录" prop="volume_mount">
          <el-input v-model="editForm.volume_mount" placeholder="请输入挂载目录"></el-input>
          <div class="form-tip">使用该模板的task，会在添加时，自动添加该挂载。格式示例:
            $pvc_name1(pvc):/$container_path1,$hostpath1(hostpath):/$container_path2 注意pvc会自动挂载对应目录下的个人username子目录</div>
        </el-form-item>
        <el-form-item label="启动参数" prop="args" class="json-form-item">
          <div class="json-editor-header">
            <el-button type="primary" size="mini" class="format-btn"
              @click="formatJson(editForm, 'args')">格式化</el-button>
          </div>
          <el-input v-model="editForm.args" type="textarea" :rows="10" placeholder="请输入启动参数"></el-input>
          <div class="form-tip"><i class="el-icon-question"></i> 详情 json格式，此类task使用时需要填写的参数</div>
        </el-form-item>
        <el-form-item label="环境变量" prop="env">
          <el-input v-model="editForm.env" type="textarea" :rows="4" placeholder="请输入环境变量"></el-input>
          <div class="form-tip">使用模板的task自动添加的环境变量，支持模板变量。书写格式:每行一个环境变量env_key=env_value</div>
        </el-form-item>
        <el-form-item label="host" prop="host_aliases">
          <el-input v-model="editForm.host_aliases" type="textarea" :rows="4" placeholder="请输入host"></el-input>
          <div class="form-tip">添加到容器内的host映射。书写格式:每行一个dns解析记录，ip host1 host2，示例: 1.1.1.1 example1.oa.com
            example2.oa.com
          </div>
        </el-form-item>
        <el-form-item label="特权模式" prop="privileged">
          <el-radio-group v-model="editForm.privileged">
            <el-radio :label="true">yes</el-radio>
            <el-radio :label="false">no</el-radio>
          </el-radio-group>
          <div class="form-tip">是否启动超级权限</div>
        </el-form-item>
        <el-form-item label="k8s账号" prop="accounts">
          <el-input v-model="editForm.accounts" placeholder="请输入k8s账号"></el-input>
          <div class="form-tip">k8s的ServiceAccount，在此类任务运行时会自动挂载此账号，多用于任务操作k8s pod时使用</div>
        </el-form-item>
        <el-form-item label="扩展" prop="expand" class="json-form-item">
          <div class="json-editor-header">
            <el-button type="primary" size="mini" class="format-btn"
              @click="formatJson(editForm, 'expand')">格式化</el-button>
          </div>
          <el-input v-model="editForm.expand" type="textarea" :rows="5" placeholder="请输入扩展字段"></el-input>
          <div class="form-tip">json格式的扩展字段，支持 "index":"$模板展示顺序号", "help_url":"$帮助文档地址", "HostNetwork":true 启动主机端口监听
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getJobTemplateList,
  getJobTemplateDetail,
  createJobTemplate,
  updateJobTemplate,
  deleteJobTemplate,
  copyJobTemplate,
} from '@/api/llmops/paradigm'
import { getImagesNamesToSelect } from '@/api/llmops/image'
import { getJobTemplateType } from '@/api/llmops/project'

export default {
  name: 'Paradigm',
  data() {
    return {
      addDialogVisible: false,
      editDialogVisible: false,
      detailDialogVisible: false,
      batchDialogVisible: false,  // 批量操作弹窗
      batchCopyLoading: false,     // 批量复制loading
      loading: false,
      detailData: {
        project: '',
        images: '',
        name: '',
        version: '',
        describe: '',
        workdir: '',
        volume_mount: '',
        entrypoint: '',
        args: '',
        env: '',
        hostAliases: '',
        privileged: false,
        accounts: '',
        expand: ''
      },
      addForm: {
        project: '',
        images: '',
        name: '',
        version: '',
        describe: '',
        workdir: '',
        volume_mount: '',
        entrypoint: '',
        args: JSON.stringify({
          "参数分组1": {
            "--attr1": {
              "type": "str",
              "label": "参数1",
              "default": "value1",
              "describe": "这里是这个参数的描述和备注"
            }
          }
        }, null, 2),
        env: '',
        host_aliases: '',
        privileged: false,
        accounts: '',
        expand: JSON.stringify({
          "index": 0,
          "help_url": "https://github.com/data-infra/cube-studio/wiki"
        }, null, 2)
      },
      editForm: {
        id: '',
        project: '',
        images: '',
        name: '',
        version: '',
        describe: '',
        workdir: '',
        volume_mount: '',
        entrypoint: '',
        args: '',
        env: '',
        host_aliases: '',
        privileged: false,
        accounts: '',
        expand: ''
      },
      addRules: {
        project: [{ required: true, message: '请选择功能分类', trigger: 'change' }],
        images: [{ required: true, message: '请选择镜像', trigger: 'change' }],
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { pattern: /^[a-z0-9-]+$/, message: '英文名(小写字母、数字、- 组成)', trigger: 'blur' },
          { max: 50, message: '最长50个字符', trigger: 'blur' }
        ],
        version: [{ required: true, message: '请选择版本', trigger: 'change' }],
        describe: [{ required: true, message: '请输入描述', trigger: 'blur' }],
        entrypoint: [{ required: true, message: '请输入启动命令', trigger: 'blur' }],
        args: [
          { required: true, message: '请输入启动参数', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (!value) return callback();
              try {
                JSON.parse(value);
                callback();
              } catch (e) {
                callback(new Error('必须是有效的 JSON 格式'));
              }
            }, trigger: 'blur'
          }
        ],
        expand: [
          {
            validator: (rule, value, callback) => {
              if (!value) return callback();
              try {
                JSON.parse(value);
                callback();
              } catch (e) {
                callback(new Error('必须是有效的 JSON 格式'));
              }
            }, trigger: 'blur'
          }
        ]
      },
      editRules: {
        project: [{ required: true, message: '请选择功能分类', trigger: 'change' }],
        images: [{ required: true, message: '请选择镜像', trigger: 'change' }],
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { pattern: /^[a-z0-9-]+$/, message: '英文名(小写字母、数字、- 组成)', trigger: 'blur' },
          { max: 50, message: '最长50个字符', trigger: 'blur' }
        ],
        version: [{ required: true, message: '请选择版本', trigger: 'change' }],
        describe: [{ required: true, message: '请输入描述', trigger: 'blur' }],
        entrypoint: [{ required: true, message: '请输入启动命令', trigger: 'blur' }],
        args: [
          { required: true, message: '请输入启动参数', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (!value) return callback();
              try {
                JSON.parse(value);
                callback();
              } catch (e) {
                callback(new Error('必须是有效的 JSON 格式'));
              }
            }, trigger: 'blur'
          }
        ],
        expand: [
          {
            validator: (rule, value, callback) => {
              if (!value) return callback();
              try {
                JSON.parse(value);
                callback();
              } catch (e) {
                callback(new Error('必须是有效的 JSON 格式'));
              }
            }, trigger: 'blur'
          }
        ]
      },
      searchForm: {
        project: '',
        images: '',
        creator: '',
        modifier: '',
        createTime: null,
        modified: null,
        name: '',
        version: '',
        ifPublic: true
      },
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectedRows: [],
      paradigmList: [],
      imageOptions: [], // 镜像下拉选项
      jobTemplateTypes: [], // 功能分类下拉选项
    }
  },
  computed: {
    totalParadigms() {
      return this.total
    },
    inUseCount() {
      return this.paradigmList.length
    },
    paradigmTypesCount() {
      const categories = new Set(this.paradigmList.map(item => item.project))
      return categories.size
    },
    monthNewCount() {
      const now = new Date()
      const currentMonth = now.getMonth()
      const currentYear = now.getFullYear()
      const count = this.paradigmList.filter(item => {
        if (!item.createTime) return false
        const createDate = new Date(item.createTime)
        return createDate.getMonth() === currentMonth && createDate.getFullYear() === currentYear
      }).length
      return count
    },
    paginationStart() {
      return (this.currentPage - 1) * this.pageSize + 1
    },
    paginationEnd() {
      return Math.min(this.currentPage * this.pageSize, this.total)
    }
  },
  mounted() {
    this.fetchParadigmList()
    this.fetchImageOptions()
    this.fetchJobTemplateTypes()
  },
  methods: {
    // 获取功能分类下拉选项
    async fetchJobTemplateTypes() {
      try {
        const res = await getJobTemplateType()
        if (res) {
          this.jobTemplateTypes = res || []
        }
      } catch (error) {
        console.error('获取功能分类下拉选项失败:', error)
      }
    },
    // 获取镜像下拉选项
    // 格式化 JSON
    formatJson(form, field) {
      try {
        const val = form[field];
        if (!val) return;
        const obj = JSON.parse(val);
        form[field] = JSON.stringify(obj, null, 2);
      } catch (e) {
        this.$message.error('JSON 格式错误，无法格式化');
      }
    },
    async fetchImageOptions() {
      try {
        const res = await getImagesNamesToSelect()
        if (res) {
          this.imageOptions = res || []
        }
      } catch (error) {
        console.error('获取镜像下拉选项失败:', error)
      }
    },
    // 获取分类名称
    getCategoryName(value) {
      if (!value) return '--'
      const item = this.jobTemplateTypes.find(t => t.id === value)
      return item ? item.name : value
    },

    // 获取镜像名称
    getImageName(value) {
      return value || '未知镜像'
    },

    async fetchParadigmList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          projectId: this.searchForm.projectId || undefined,
          images: this.searchForm.images || undefined,
          creator: this.searchForm.creator || undefined,
          modifier: this.searchForm.modifier || undefined,
          name: this.searchForm.name || undefined,
          version: this.searchForm.version || undefined,
          ifPublic: this.searchForm.ifPublic
        }

        if (this.searchForm.createTime) {
          params.createTime = this.searchForm.createTime
        }
        if (this.searchForm.modified) {
          params.modified = this.searchForm.modified
        }

        const res = await getJobTemplateList(params)

        if (res.resultData) {
          this.paradigmList = res.resultData || []
          this.total = res.totalRecord || this.paradigmList.length
        } else {
          this.$message.error(res.message || '获取范式列表失败')
        }
      } catch (error) {
        console.error('获取范式列表失败:', error)
        this.$message.error('获取范式列表失败')
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      this.currentPage = 1
      this.fetchParadigmList()
    },

    handleReset() {
      const currentPublic = this.searchForm.ifPublic
      this.searchForm = {
        projectId: '',
        images: '',
        creator: '',
        modifier: '',
        createTime: null,
        modified: null,
        name: '',
        version: '',
        ifPublic: currentPublic
      }
      this.currentPage = 1
      this.fetchParadigmList()
    },

    showAddDialog() {
      this.addDialogVisible = true
      this.resetAddForm()
    },

    resetAddForm() {
      this.$nextTick(() => {
        if (this.$refs.addForm) {
          this.$refs.addForm.clearValidate()
        }
      })
      this.addForm = {
        project: '',
        images: '',
        name: '',
        version: '',
        describe: '',
        workdir: '',
        volume_mount: '',
        entrypoint: '',
        args: JSON.stringify({
          "参数分组1": {
            "--attr1": {
              "type": "str",
              "label": "参数1",
              "default": "value1",
              "describe": "这里是这个参数的描述和备注"
            }
          }
        }, null, 2),
        env: '',
        host_aliases: '',
        privileged: false,
        accounts: '',
        expand: JSON.stringify({
          "index": 0,
          "help_url": "https://github.com/data-infra/cube-studio/wiki"
        }, null, 2)
      }
    },

    async handleAddSubmit() {
      this.$refs.addForm.validate(async (valid) => {
        if (valid) {
          try {
            const res = await createJobTemplate(this.addForm)
            if (res) {
              this.$message.success('添加任务模板成功！')
              this.addDialogVisible = false
              this.fetchParadigmList()
            } else {
              this.$message.error(res.message || '添加失败')
            }
          } catch (error) {
            console.error('添加失败:', error)
            this.$message.error('添加失败')
          }
        }
      })
    },

    async showEditDialog(row) {
      this.editDialogVisible = true
      try {
        const res = await getJobTemplateDetail(row.id)
        if (res) {
          this.editForm = {
            ...res,
            images: res.imagesId,
            project: res.projectId
          }
        } else {
          this.$message.error(res.message || '获取详情失败')
        }
      } catch (error) {
        console.error('获取详情失败:', error)
        this.$message.error('获取详情失败')
      }
    },

    async handleEditSubmit() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            const res = await updateJobTemplate(this.editForm)
            if (res) {
              this.$message.success('修改任务模板成功！')
              this.editDialogVisible = false
              this.fetchParadigmList()
            } else {
              this.$message.error(res.message || '修改失败')
            }
          } catch (error) {
            console.error('修改失败:', error)
            this.$message.error('修改失败')
          }
        }
      })
    },

    async showDetailDialog(row) {
      this.detailDialogVisible = true
      try {
        const res = await getJobTemplateDetail(row.id)
        if (res) {
          this.detailData = {
            ...res
          }
        } else {
          this.$message.error(res.message || '获取详情失败')
        }
      } catch (error) {
        console.error('获取详情失败:', error)
        this.$message.error('获取详情失败')
      }
    },

    handleDelete(row) {
      this.$confirm('确定删除该任务模板吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteJobTemplate(row.id)
          if (res) {
            this.$message.success('删除成功')
            this.fetchParadigmList()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => { })
    },

    handlePageChange(page) {
      this.currentPage = page
      this.fetchParadigmList()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.fetchParadigmList()
    },

    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 显示批量操作弹窗
    showBatchDialog() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请先选择要操作的项')
        return
      }
      this.batchDialogVisible = true
    },

    // 批量复制
    async handleBatchCopy() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请先选择要复制的模板')
        return
      }

      this.batchCopyLoading = true
      const ids = this.selectedRows.map(row => row.id)
      console.log(ids)
      try {
        const res = await copyJobTemplate({ ids: ids })
        if (res) {
          this.$message.success(`成功复制 ${this.selectedRows.length} 个任务模板`)
          this.batchDialogVisible = false
          this.fetchParadigmList()
          // 清空选中
          this.$refs.table && this.$refs.table.clearSelection()
        } else {
          this.$message.error(res.message || '批量复制失败')
        }
      } catch (error) {
        console.error('批量复制失败:', error)
        this.$message.error('批量复制失败')
      } finally {
        this.batchCopyLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.add-paradigm-dialog,
.edit-paradigm-dialog,
.detail-paradigm-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px 24px 0;
    border-bottom: 1px solid #e5e7eb;
    margin-right: 0;
    padding-bottom: 16px;
  }

  ::v-deep .el-dialog__headerbtn {
    top: 24px;
  }

  ::v-deep .el-dialog__body {
    padding: 24px;
  }

  ::v-deep .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e5e7eb;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
    margin-top: 4px;

    i {
      color: #409eff;
      margin-right: 4px;
    }
  }

  .json-form-item {
    position: relative;

    .json-editor-header {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 8px;
      position: absolute;
      right: 0;
      top: -36px;
      z-index: 10;
    }
  }
}

// 批量操作弹窗样式
.batch-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px 24px 0;
    border-bottom: 1px solid #e5e7eb;
    margin-right: 0;
    padding-bottom: 16px;
  }

  ::v-deep .el-dialog__body {
    padding: 24px;
  }

  ::v-deep .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e5e7eb;
  }
}

.batch-content {
  text-align: center;

  .batch-info {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 24px;
    padding: 12px;
    background-color: #f0f9ff;
    border-radius: 8px;
    color: #0c4e6e;

    i {
      font-size: 20px;
      color: #0ea5e9;
    }

    span {
      font-size: 14px;
    }
  }

  .batch-buttons {
    display: flex;
    justify-content: center;
    gap: 16px;
  }
}

.detail-content {
  .detail-item {
    margin-bottom: 16px;
  }

  .detail-label {
    font-size: 13px;
    color: #6b7280;
    margin-bottom: 4px;
  }

  .detail-value {
    font-size: 14px;
    color: #1f2937;
    line-height: 1.5;
    word-break: break-all;
  }
}

.paradigm-container {
  min-height: calc(100vh - 60px);
}

.paradigm-content {
  // max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-trend.up {
  color: #10b981;
}

.search-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 12px;
}

.search-form .el-form-item {
  margin-bottom: 12px;
}

.search-form .el-form-item__label {
  font-size: 13px;
  color: #374151;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.table-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left-group {
      display: flex;
      align-items: center;
      gap: 20px;

      .section-title {
        margin-bottom: 0;
      }
    }
  }
}

.public-switch {
  ::v-deep {
    .el-radio-button__inner {
      border-radius: 4px;
      margin: 0 4px;
      border: 1px solid #dcdfe6 !important;
      box-shadow: none !important;
    }

    .el-radio-button__orig-radio:checked+.el-radio-button__inner {
      background-color: #f0f7ff;
      color: #409eff;
      border-color: #409eff !important;
    }
  }
}

.table-actions {
  display: flex;
  gap: 8px;
}

.paradigm-table {
  font-size: 13px;
}

.name-cell {
  display: flex;
  flex-direction: column;
}

.name-title {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.name-desc {
  font-size: 12px;
  color: #6b7280;
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.pagination-left {
  font-size: 13px;
  color: #6b7280;
}

.pagination-right {
  display: flex;
  align-items: center;
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .search-form .el-form-item {
    width: 100%;
  }

  .pagination-section {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>