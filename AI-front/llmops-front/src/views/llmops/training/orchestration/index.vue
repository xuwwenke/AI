<template>
  <div class="orchestration-container">
    <div class="orchestration-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">任务编排</h1>
          <p class="page-subtitle">编排和管理训练任务流程</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="add-btn" size="small" @click="showAddDialog">
            <i class="el-icon-plus"></i> 添加任务编排
          </el-button>
        </div>
      </div>

      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">总流程数</div>
          <div class="stat-value">{{ totalPipelines }}</div>
          <div class="stat-trend up">
            <i class="el-icon-top"></i> {{ weekNewCount }} 本周新增
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value">{{ runningCount }}</div>
          <div class="stat-trend">
            <i class="el-icon-check"></i> 全部正常
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ completedCount }}</div>
          <div class="stat-trend">
            成功率 {{ successRate }}%
          </div>
        </div>
      </div>

      <!-- 智能编排机器人 -->
      <ai-robot-section title="智能编排机器人" subtitle="通过问答形式快速编排流程"
        :welcome-message="{ role: 'assistant', content: '您好！我是智能编排机器人，可以帮助您：', features: ['快速创建任务编排', '管理编排流程', '监控编排状态', '优化编排策略'], question: '请问有什么可以帮助您的？' }"
        :quick-questions="['获取当前任务编排列表', '查看任务编排【id或名称】详细信息', '查看任务编排【id或名称】对应训练实例', '启动任务编排【id或名称】']"
        placeholder="请输入您的问题，例如：帮我创建一个文本分类训练编排" storage-key="llmops_training_orchestration_chat_v1" />
    </div>

    <div class="search-section">
      <div class="section-title">任务编排查询</div>
      <el-form :model="searchForm" :inline="true" class="search-form" label-position="top">
        <el-form-item label="创建者">
          <el-input v-model="searchForm.creator" placeholder="创建者" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="ID">
          <el-input v-model="searchForm.id" placeholder="ID" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="名称" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="searchForm.describe" placeholder="描述" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="调度类型">
          <el-select v-model="searchForm.scheduleType" placeholder="全部" clearable style="width: 280px;">
            <el-option label="定时调度" value="定时调度"></el-option>
            <el-option label="事件触发" value="事件触发"></el-option>
            <el-option label="手动触发" value="手动触发"></el-option>
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="项目组">
            <el-select v-model="searchForm.project" placeholder="全部" clearable style="width: 280px;">
              <el-option label="AI Lab" :value="1"></el-option>
              <el-option label="Data Science" :value="2"></el-option>
              <el-option label="Research" :value="3"></el-option>
            </el-select>
          </el-form-item> -->
      </el-form>
      <div class="search-actions">
        <el-button size="small" @click="handleReset">重置</el-button>
        <el-button type="primary" size="small" @click="handleSearch">查询</el-button>
      </div>
    </div>

    <div class="table-section">
      <div class="table-header">
        <div class="section-title">任务编排列表</div>
        <div class="table-actions">
          <el-button size="small" @click="showBatchDialog">批量操作</el-button>
        </div>
      </div>
      <el-table :data="pipelineList" border style="width: 100%;" class="orchestration-table"
        @selection-change="handleSelectionChange" v-loading="loading" ref="table">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="aiExperimentName" label="实验名称" width="120"></el-table-column>
        <el-table-column label="任务编排" min-width="250">
          <template slot-scope="scope">
            <div class="name-cell">
              <div class="name-title clickable" @click="handleOpenFlow(scope.row)">{{ scope.row.name }}</div>
              <div class="name-desc">{{ scope.row.describe }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建者" width="120"></el-table-column>
        <el-table-column prop="modified" label="修改时间" width="160"></el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="showDetailDialog(scope.row)">详情</el-button>
            <el-button type="text" size="mini" @click="showEditDialog(scope.row)">修改</el-button>
            <el-button type="text" size="mini" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
            <el-button type="text" size="mini" @click="showTaskDialog(scope.row)">任务</el-button>
          </template>
        </el-table-column>
      </el-table>

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


    <!-- 批量操作弹窗 -->
    <el-dialog title="批量操作" :visible.sync="batchDialogVisible" width="400px" :close-on-click-modal="false"
      class="batch-dialog">
      <div class="batch-content">
        <div class="batch-info">
          <i class="el-icon-info"></i>
          <span>已选择 {{ selectedRows.length }} 个任务编排</span>
        </div>
        <div class="batch-buttons">
          <el-button type="primary" size="medium" icon="el-icon-copy-document" @click="handleBatchCopy"
            :loading="batchCopyLoading">
            批量复制
          </el-button>
          <el-button type="danger" size="medium" icon="el-icon-delete" @click="handleBatchDelete"
            :loading="batchDeleteLoading">
            批量删除
          </el-button>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 添加任务编排弹框 -->
    <el-dialog title="添加任务编排" :visible.sync="addDialogVisible" width="800px" :close-on-click-modal="false"
      class="add-orchestration-dialog">
      <el-form :model="addForm" :rules="addRules" ref="addForm" label-width="80px">
        <el-form-item label="所属实验" prop="ai_experiment_id">
          <el-select v-model="addForm.ai_experiment_id" placeholder="请选择" style="width: 100%;">
            <el-option v-for="item in experimentList" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="addForm.name" placeholder="英文名(小写字母、数字、- 组成)，最长50个字符"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="describe">
          <el-input v-model="addForm.describe" placeholder="描述"></el-input>
        </el-form-item>
        <el-form-item label="调度类型" prop="scheduleType">
          <el-select v-model="addForm.scheduleType" placeholder="请选择调度类型" style="width: 100%;">
            <el-option label="once" value="once"></el-option>
            <el-option label="crontab" value="crontab"></el-option>
          </el-select>
          <div class="form-tip">调度类型，once仅运行一次，crontab周期运行，crontab配置保存一个小时后才生效</div>
        </el-form-item>
        <el-form-item v-if="addForm.scheduleType === 'crontab'" label="调度周期" prop="cronTime">
          <el-input v-model="addForm.cronTime" placeholder="请选择调度周期"></el-input>
          <div class="form-tip">周期任务的时间设定 * * * * * 表示为 minute hour day month week</div>
        </el-form-item>
        <el-form-item label="过往依赖" prop="dependsOnPast">
          <el-radio-group v-model="addForm.dependsOnPast">
            <el-radio :label="true">yes</el-radio>
            <el-radio :label="false">no</el-radio>
          </el-radio-group>
          <div class="form-tip">任务运行是否依赖上一次的示例状态</div>
        </el-form-item>
        <el-form-item label="最大激活运行数" prop="maxActiveRuns">
          <el-input-number v-model="addForm.maxActiveRuns" :min="1" style="width: 100%;"></el-input-number>
          <div class="form-tip">当前pipeline可同时运行的任务流实例数目</div>
        </el-form-item>
        <el-form-item label="有效实例数目" prop="expiredLimit">
          <el-input-number v-model="addForm.expiredLimit" :min="0" style="width: 100%;"></el-input-number>
          <div class="form-tip">定时调度最新实例限制数目，0表示不限制</div>
        </el-form-item>
        <el-form-item label="任务并行数" prop="parallelism">
          <el-input-number v-model="addForm.parallelism" :min="1" style="width: 100%;"></el-input-number>
          <div class="form-tip">一个任务流实例中可同时运行的task数目</div>
        </el-form-item>
        <el-form-item label="流向图" prop="dagJson">
          <div class="json-editor-container">
            <el-input v-model="addForm.dagJson" type="textarea" :rows="8" placeholder="任务的上下游关系，目前不需要手动修改"
              class="json-textarea"></el-input>
            <el-button type="text" class="format-btn" @click="formatJsonField('addForm', 'dagJson')">格式化</el-button>
          </div>
        </el-form-item>
        <el-form-item label="全局环境变量" prop="globalEnv">
          <el-input v-model="addForm.globalEnv" type="textarea" :rows="4" placeholder="请选择全局环境变量"></el-input>
          <div class="form-tip">公共环境变量会以环境变量的形式传递给每个task，可以配置多个公共环境变量，每行一个，支持datetime/creator/runner/uuid/pipeline_id等变量
            例如：USERNAME={creator}</div>
        </el-form-item>
        <el-form-item label="监控状态" prop="alertStatus">
          <el-select v-model="addForm.alertStatus" multiple placeholder="选择通知状态" style="width: 100%;">
            <el-option label="Pending" value="Pending"></el-option>
            <el-option label="Running" value="Running"></el-option>
            <el-option label="Succeeded" value="Succeeded"></el-option>
            <el-option label="Failed" value="Failed"></el-option>
            <el-option label="Terminated" value="Terminated"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知用户" prop="alertUser">
          <el-input v-model="addForm.alertUser" placeholder="选择通知用户，每个用户使用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="前端扩展" prop="expand">
          <div class="json-editor-container">
            <el-input v-model="addForm.expand" type="textarea" :rows="8" placeholder="前端扩展参数，前端用于记录任务流节点位置和连线关系"
              class="json-textarea"></el-input>
            <el-button type="text" class="format-btn" @click="formatJsonField('addForm', 'expand')">格式化</el-button>
          </div>
        </el-form-item>
        <el-form-item label="后端扩展" prop="parameter">
          <div class="json-editor-container">
            <el-input v-model="addForm.parameter" type="textarea" :rows="2" placeholder="后端扩展参数，用于配置是否为demo或固化任务流"
              class="json-textarea"></el-input>
            <el-button type="text" class="format-btn" @click="formatJsonField('addForm', 'parameter')">格式化</el-button>
          </div>
        </el-form-item>
        <el-form-item label="补录起点" prop="cronjobStartTime">
          <el-input v-model="addForm.cronjobStartTime" placeholder="请选择补录起点"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSubmit">确定</el-button>
      </span>
    </el-dialog>

    <!-- 任务编排详情弹框 -->
    <el-dialog title="任务编排详情" :visible.sync="detailDialogVisible" width="800px" :close-on-click-modal="false"
      class="detail-orchestration-dialog">
      <div class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">ID</div>
              <div class="detail-value">{{ detailData.id }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">实验名称</div>
              <div class="detail-value">{{ detailData.aiExperimentName || '--' }}</div>
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
              <div class="detail-label">描述</div>
              <div class="detail-value">{{ detailData.describe || detailData.desc || '--' }}</div>
            </div>
          </el-col>

        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">调度类型</div>
              <div class="detail-value">{{ detailData.scheduleType || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">调度周期</div>
              <div class="detail-value">{{ detailData.cronTime || '--' }}</div>
            </div>
          </el-col>

        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">过往依赖</div>
              <div class="detail-value">{{ detailData.dependsOnPast || '--' }}</div>
            </div>
          </el-col>


        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="detail-item">
              <div class="detail-label">最大激活运行数</div>
              <div class="detail-value">{{ detailData.maxActiveRuns }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="detail-item">
              <div class="detail-label">有效实例数目</div>
              <div class="detail-value">{{ detailData.expiredLimit }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="detail-item">
              <div class="detail-label">任务并行数</div>
              <div class="detail-value">{{ detailData.parallelism }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">全局环境变量</div>
              <div class="detail-value" style="white-space: pre-wrap;">{{ detailData.globalEnv || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">流向图</div>
              <div class="detail-value">
                <pre class="json-display">{{ detailData.dagJson }}</pre>
              </div>
            </div>
          </el-col>
        </el-row>


        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">任务流ID</div>
              <div class="detail-value">{{ detailData.pipelineArgoId || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">运行ID</div>
              <div class="detail-value">{{ detailData.runId || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">创建者</div>
              <div class="detail-value">{{ detailData.creator || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">修改者</div>
              <div class="detail-value">{{ detailData.changedBy || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">创建时间</div>
              <div class="detail-value">{{ detailData.createdOn }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">修改时间</div>
              <div class="detail-value">{{ detailData.changedOn }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">前端扩展</div>
              <div class="detail-value">
                <pre class="json-display">{{ detailData.expand || '--' }}</pre>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="detail-item">
              <div class="detail-label">后端扩展</div>
              <div class="detail-value">
                <pre class="json-display">{{ detailData.parameter || '--' }}</pre>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">监控状态</div>
              <div class="detail-value">{{ detailData.alertStatus || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">通知用户</div>
              <div class="detail-value">{{ detailData.alertUser || '--' }}</div>
            </div>
          </el-col> <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">补录起点</div>
              <div class="detail-value">{{ detailData.cronjobStartTime || '--' }}</div>
            </div>
          </el-col>

        </el-row>

        <!-- <div class="task-list-section">
          <div class="section-title">任务列表</div>
          <div class="task-list">
            <div class="task-item" v-for="(task, index) in taskDetailList" :key="index">
              <div class="task-left">
                <span class="task-dot" :class="getTaskStatusClass(task.status)"></span>
                <div class="task-info">
                  <div class="task-name">{{ task.label }}</div>
                  <div class="task-desc">{{ task.name }}</div>
                </div>
              </div>
              <div class="task-status" :class="getTaskStatusTextClass(task.status)">
                {{ getTaskStatusText(task.status) }}
              </div>
            </div>
          </div>
        </div> -->

      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 修改任务编排弹框 -->
    <el-dialog title="修改任务编排" :visible.sync="editDialogVisible" width="800px" :close-on-click-modal="false"
      class="edit-orchestration-dialog">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="120px">
        <el-form-item label="所属实验" prop="ai_experiment_id">
          <el-select v-model="editForm.ai_experiment_id" placeholder="暂无实验" style="width: 100%;" disabled>
            <el-option v-for="item in experimentList" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" placeholder="英文名(小写字母、数字、- 组成)，最长50个字符"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="describe">
          <el-input v-model="editForm.describe" placeholder="描述"></el-input>
        </el-form-item>
        <el-form-item label="调度类型" prop="scheduleType">
          <el-select v-model="editForm.scheduleType" placeholder="请选择调度类型" style="width: 100%;">
            <el-option label="once" value="once"></el-option>
            <el-option label="crontab" value="crontab"></el-option>
          </el-select>
          <div class="form-tip">调度类型，once仅运行一次，crontab周期运行，crontab配置保存一个小时后才生效</div>
        </el-form-item>
        <el-form-item label="调度周期" prop="cronTime">
          <el-input v-model="editForm.cronTime" placeholder="请选择调度周期"></el-input>
          <div class="form-tip">周期任务的时间设定 * * * * * 表示为 minute hour day month week</div>
        </el-form-item>
        <el-form-item label="过往依赖" prop="dependsOnPast">
          <el-radio-group v-model="editForm.dependsOnPast">
            <el-radio :label="true">yes</el-radio>
            <el-radio :label="false">no</el-radio>
          </el-radio-group>
          <div class="form-tip">任务运行是否依赖上一次的示例状态</div>
        </el-form-item>
        <el-form-item label="最大激活运行数" prop="maxActiveRuns">
          <el-input-number v-model="editForm.maxActiveRuns" :min="1" style="width: 100%;"></el-input-number>
          <div class="form-tip">当前pipeline可同时运行的任务流实例数目</div>
        </el-form-item>
        <el-form-item label="有效实例数目" prop="expiredLimit">
          <el-input-number v-model="editForm.expiredLimit" :min="0" style="width: 100%;"></el-input-number>
          <div class="form-tip">定时调度最新实例限制数目，0表示不限制</div>
        </el-form-item>
        <el-form-item label="任务并行数" prop="parallelism">
          <el-input-number v-model="editForm.parallelism" :min="1" style="width: 100%;"></el-input-number>
          <div class="form-tip">一个任务流实例中可同时运行的task数目</div>
        </el-form-item>
        <el-form-item label="流向图" prop="dagJson">
          <div class="json-editor-container">
            <el-input v-model="editForm.dagJson" type="textarea" :rows="8" placeholder="任务的上下游关系，目前不需要手动修改"
              class="json-textarea"></el-input>
            <el-button type="text" class="format-btn" @click="formatJsonField('editForm', 'dagJson')">格式化</el-button>
          </div>
        </el-form-item>
        <el-form-item label="全局环境变量" prop="globalEnv">
          <el-input v-model="editForm.globalEnv" type="textarea" :rows="4" placeholder="请选择全局环境变量"></el-input>
          <div class="form-tip">公共环境变量会以环境变量的形式传递给每个task，可以配置多个公共环境变量，每行一个，支持datetime/creator/runner/uuid/pipeline_id等变量
            例如：USERNAME={creator}</div>
        </el-form-item>
        <el-form-item label="监控状态" prop="alertStatus">
          <el-select v-model="editForm.alertStatus" multiple placeholder="选择通知状态" style="width: 100%;">
            <el-option label="Pending" value="Pending"></el-option>
            <el-option label="Running" value="Running"></el-option>
            <el-option label="Succeeded" value="Succeeded"></el-option>
            <el-option label="Failed" value="Failed"></el-option>
            <el-option label="Terminated" value="Terminated"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知用户" prop="alertUser">
          <el-input v-model="editForm.alertUser" placeholder="选择通知用户，每个用户使用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="前端扩展" prop="expand">
          <div class="json-editor-container">
            <el-input v-model="editForm.expand" type="textarea" :rows="8" placeholder="前端扩展参数，前端用于记录任务流节点位置和连线关系"
              class="json-textarea"></el-input>
            <el-button type="text" class="format-btn" @click="formatJsonField('editForm', 'expand')">格式化</el-button>
          </div>
        </el-form-item>
        <el-form-item label="后端扩展" prop="parameter">
          <div class="json-editor-container">
            <el-input v-model="editForm.parameter" type="textarea" :rows="2" placeholder="后端扩展参数，用于配置是否为demo或固化任务流"
              class="json-textarea"></el-input>
            <el-button type="text" class="format-btn" @click="formatJsonField('editForm', 'parameter')">格式化</el-button>
          </div>
        </el-form-item>
        <el-form-item label="补录起点" prop="cronjobStartTime">
          <el-input v-model="editForm.cronjobStartTime" placeholder="请选择补录起点"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </span>
    </el-dialog>

    <!-- 任务列表弹框 -->
    <el-dialog title="任务列表" :visible.sync="taskDialogVisible" width="700px" :close-on-click-modal="false"
      class="task-orchestration-dialog">
      <div class="task-list-content">
        <div class="task-item" v-for="(task, index) in taskList" :key="index">
          <div class="task-left">
            <span class="task-dot" :class="getTaskStatusClass(task.status)"></span>
            <div class="task-info">
              <div class="task-name">{{ task.label }}</div>
              <div class="task-desc">{{ task.name }}</div>
            </div>
          </div>
          <div class="task-right">
            <span class="task-status" :class="getTaskStatusTextClass(task.status)">
              {{ getTaskStatusText(task.status) }}
            </span>
            <el-button type="text" size="mini" class="task-detail-btn" @click="showTaskDetail(task)">详情</el-button>
            <el-button type="text" size="mini" class="task-pause-btn" v-if="task.status === 'running'"
              @click="handlePauseTask(task)">暂停</el-button>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="taskDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getPipelineList,
  getPipelineDetail,
  createPipeline,
  updatePipeline,
  deletePipeline,
  mulDeletePipeline,
  mulCopyPipeline,
  getTaskList
} from '@/api/llmops/orchestration'
import { getExperimentOrchestrationList } from '@/api/llmops/experiment'
import { mapState } from 'vuex'
import AiRobotSection from '@/components/llmops/AiRobotSection'

export default {
  name: 'Orchestration',
  components: { AiRobotSection },
  data() {
    return {
      addDialogVisible: false,
      editDialogVisible: false,
      detailDialogVisible: false,
      taskDialogVisible: false,
      batchDialogVisible: false,
      batchCopyLoading: false,
      batchDeleteLoading: false,
      loading: false,
      taskList: [], // 任务列表弹框中的数据
      taskDetailList: [], // 详情弹框中的任务列表数据
      detailData: {
        id: '',
        aiExperimentName: '',
        name: '',
        creator: '',
        describe: '',
        createdOn: '',
        changedOn: '',
        projectId: '',
        dagJson: '',
        globalEnv: '',
        scheduleType: '',
        cronTime: '',
        parallelism: 0,
        alertStatus: '',
        alertUser: '',
        expand: '',
        dependsOnPast: false,
        maxActiveRuns: 0,
        parameter: '',
        expiredLimit: 0,
        cronjobStartTime: '',
        runId: '',
        workflowYaml: '',
        changedBy: '',
        namespace: '',
        pipelineFile: '',
        pipelineArgoId: '',
        versionId: '',
        nodeSelector: '',
        imagePullPolicy: '',
        priority: '',
        type: '',
        version: '',
        tasks: []
      },
      addForm: {
        ai_experiment_id: '',
        name: '',
        describe: '',
        projectId: '',
        scheduleType: 'once',
        cronTime: '',
        dependsOnPast: false,
        maxActiveRuns: 3,
        expiredLimit: 0,
        parallelism: 1,
        dagJson: '{}',
        globalEnv: '',
        alertStatus: [],
        alertUser: '',
        expand: '[]',
        parameter: '{}',
        cronjobStartTime: ''
      },
      addRules: {
        ai_experiment_id: [{ required: true, message: '请选择所属实验', trigger: 'change' }],
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        describe: [{ required: true, message: '请输入描述', trigger: 'blur' }],
        scheduleType: [{ required: true, message: '请选择调度类型', trigger: 'change' }],
        maxActiveRuns: [{ required: true, message: '请输入最大激活运行数', trigger: 'blur' }],
        expiredLimit: [{ required: true, message: '请输入有效实例数目', trigger: 'blur' }],
        parallelism: [{ required: true, message: '请输入任务并行数', trigger: 'blur' }],
        dagJson: [{ required: true, message: '请输入流向图', trigger: 'blur' }]
      },
      editForm: {
        id: '',
        aiExperimentId: '',
        name: '',
        describe: '',
        projectId: '',
        scheduleType: 'once',
        cronTime: '',
        dependsOnPast: false,
        maxActiveRuns: 3,
        expiredLimit: 0,
        parallelism: 1,
        dagJson: '{}',
        globalEnv: '',
        alertStatus: [],
        alertUser: '',
        expand: '[]',
        parameter: '{}',
        cronjobStartTime: ''
      },
      editRules: {
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        describe: [{ required: true, message: '请输入描述', trigger: 'blur' }],
        scheduleType: [{ required: true, message: '请选择调度类型', trigger: 'change' }],
        maxActiveRuns: [{ required: true, message: '请输入最大激活运行数', trigger: 'blur' }],
        expiredLimit: [{ required: true, message: '请输入有效实例数目', trigger: 'blur' }],
        parallelism: [{ required: true, message: '请输入任务并行数', trigger: 'blur' }],
        dagJson: [{ required: true, message: '请输入流向图', trigger: 'blur' }]
      },
      searchForm: {
        creator: '',
        id: '',
        name: '',
        describe: '',
        scheduleType: '',
      },
      experimentList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectedRows: [],
      pipelineList: []
    }
  },
  computed: {
    ...mapState('project', ['currentProject']),
    totalPipelines() {
      return this.total
    },
    runningCount() {
      // 根据实际业务逻辑计算运行中的编排数量
      return this.pipelineList.filter(item => item.status === 'running').length
    },
    completedCount() {
      return this.pipelineList.filter(item => item.status === 'completed').length
    },
    successRate() {
      if (this.completedCount === 0) return 0
      return Math.round((this.completedCount / (this.completedCount + (this.pipelineList.filter(item => item.status === 'failed').length || 1))) * 100)
    },
    weekNewCount() {
      const now = new Date()
      const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      return this.pipelineList.filter(item => {
        if (!item.createdOn) return false
        const createDate = new Date(item.createdOn)
        return createDate >= oneWeekAgo
      }).length
    },
    paginationStart() {
      return (this.currentPage - 1) * this.pageSize + 1
    },
    paginationEnd() {
      return Math.min(this.currentPage * this.pageSize, this.total)
    }
  },
  mounted() {
    this.fetchExperimentList()
    this.fetchPipelineList()
  },
  methods: {
    // 获取实验列表
    async fetchExperimentList() {
      try {
        const res = await getExperimentOrchestrationList({
          projectId: this.currentProject.id
        })
        if (res) {
          this.experimentList = res || []
        }
      } catch (error) {
        console.error('获取实验列表失败:', error)
      }
    },
    formatJsonField(formName, fieldName) {
      const val = this[formName][fieldName];
      if (!val) return;
      try {
        const obj = JSON.parse(val);
        this[formName][fieldName] = JSON.stringify(obj, null, 2);
        this.$message.success('格式化成功');
      } catch (e) {
        this.$message.error('JSON 格式错误，无法格式化');
      }
    },
    // 获取任务编排列表
    async fetchPipelineList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          creator: this.searchForm.creator || undefined,
          id: this.searchForm.id || undefined,
          name: this.searchForm.name || undefined,
          describe: this.searchForm.describe || undefined,
          // project: this.searchForm.project || undefined
        }

        const res = await getPipelineList(params)

        if (res.resultData) {
          this.pipelineList = res.resultData || []
          this.total = res.totalRecord || this.pipelineList.length
        }
        else {
          this.$message.error(res.message || '获取任务编排列表失败')
        }
      } catch (error) {
        console.error('获取任务编排列表失败:', error)
        this.$message.error('获取任务编排列表失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索
    handleSearch() {
      this.currentPage = 1
      this.fetchPipelineList()
    },

    // 重置搜索
    handleReset() {
      this.searchForm = {
        creator: '',
        id: '',
        name: '',
        describe: '',
        scheduleType: '',
        // project: ''
      }
      this.currentPage = 1
      this.fetchPipelineList()
    },

    // 显示添加弹框
    showAddDialog() {
      this.addDialogVisible = true
      this.resetAddForm()
    },

    // 重置添加表单
    resetAddForm() {
      this.$nextTick(() => {
        if (this.$refs.addForm) {
          this.$refs.addForm.clearValidate()
        }
      })
      this.addForm = {
        ai_experiment_id: '',
        name: '',
        describe: '',
        projectId: this.currentProject.id || '',
        scheduleType: 'once',
        cronTime: '',
        dependsOnPast: false,
        maxActiveRuns: 3,
        expiredLimit: 0,
        parallelism: 1,
        dagJson: '{}',
        globalEnv: '',
        alertStatus: [],
        alertUser: '',
        expand: '[]',
        parameter: '{}',
        cronjobStartTime: ''
      }
    },

    // 提交添加
    async handleAddSubmit() {
      this.$refs.addForm.validate(async (valid) => {
        if (valid) {
          try {
            const submitData = {
              ...this.addForm,
              id: 0,
              ai_experiment_id: this.addForm.ai_experiment_id,
              alertStatus: Array.isArray(this.addForm.alertStatus) ? this.addForm.alertStatus.join(',') : this.addForm.alertStatus
            }
            // 移除旧字段名
            delete submitData.aiExperimentId;
            const res = await createPipeline(submitData)
            if (res) {
              this.$message.success('添加任务编排成功！')
              this.addDialogVisible = false
              this.fetchPipelineList()
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

    // 显示详情弹框
    async showDetailDialog(row) {
      this.detailDialogVisible = true
      try {
        const res = await getPipelineDetail(row.id)
        if (res) {
          const data = res
          this.detailData = {
            id: data.id,
            aiExperimentName: data.aiExperimentName,
            name: data.name,
            creator: data.createdBy,
            describe: data.describe,
            desc: data.desc,
            createdOn: data.createdOn,
            changedOn: data.changedOn,
            projectId: data.projectId,
            dagJson: data.dagJson,
            globalEnv: data.globalEnv,
            scheduleType: data.scheduleType,
            cronTime: data.cronTime,
            parallelism: data.parallelism,
            alertStatus: data.alertStatus,
            alertUser: data.alertUser,
            expand: data.expand,
            dependsOnPast: data.dependsOnPast,
            maxActiveRuns: data.maxActiveRuns,
            parameter: data.parameter,
            expiredLimit: data.expiredLimit,
            cronjobStartTime: data.cronjobStartTime,
            runId: data.runId || '--',
            workflowYaml: data.workflowYaml || '--',
            changedBy: data.changedBy || '--',
            namespace: data.namespace || '--',
            pipelineFile: data.pipelineFile || '--',
            pipelineArgoId: data.pipelineArgoId || '--',
            versionId: data.versionId || '--',
            nodeSelector: data.nodeSelector || '--',
            imagePullPolicy: data.imagePullPolicy || '--',
            priority: data.priority || '--',
            type: data.type || '--',
            version: data.version || '--',
            tasks: data.tasks || []
          }
          // 获取详情弹框中的任务列表
          const taskRes = await getTaskList({ pipelineId: row.id });
          if (taskRes && taskRes.resultData) {
            this.taskDetailList = taskRes.resultData;
          } else {
            this.taskDetailList = [];
          }
        } else {
          this.$message.error(res.message || '获取详情失败')
        }
      } catch (error) {
        console.error('获取详情失败:', error)
        this.$message.error('获取详情失败')
      }
    },

    // 显示编辑弹框
    async showEditDialog(row) {
      this.editDialogVisible = true
      try {
        const res = await getPipelineDetail(row.id)
        if (res) {
          const data = res
          this.editForm = {
            id: data.id,
            ai_experiment_id: data.aiExperimentId || data.ai_experiment_id,
            name: data.name,
            describe: data.describe || data.desc,
            projectId: data.projectId || '',
            scheduleType: data.scheduleType || 'once',
            cronTime: data.cronTime || '',
            dependsOnPast: data.dependsOnPast !== undefined ? data.dependsOnPast : false,
            maxActiveRuns: data.maxActiveRuns || 3,
            expiredLimit: data.expiredLimit || 0,
            parallelism: data.parallelism || 1,
            dagJson: data.dagJson || '{}',
            globalEnv: data.globalEnv || '',
            alertStatus: data.alertStatus ? data.alertStatus.split(',') : [],
            alertUser: data.alertUser || '',
            expand: data.expand || '[]',
            parameter: data.parameter || '{}',
            cronjobStartTime: data.cronjobStartTime || ''
          }
        } else {
          this.$message.error(res.message || '获取详情失败')
        }
      } catch (error) {
        console.error('获取详情失败:', error)
        this.$message.error('获取详情失败')
      }
    },

    // 提交编辑
    async handleEditSubmit() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            const submitData = {
              id: this.editForm.id,
              project: this.currentProject.id,
              name: this.editForm.name,
              describe: this.editForm.describe,
              schedule_type: this.editForm.scheduleType,
              cron_time: this.editForm.cronTime,
              depends_on_past: this.editForm.dependsOnPast,
              max_active_runs: this.editForm.maxActiveRuns,
              expired_limit: this.editForm.expiredLimit,
              parallelism: this.editForm.parallelism,
              dag_json: this.editForm.dagJson,
              global_env: this.editForm.globalEnv,
              alert_status: Array.isArray(this.editForm.alertStatus) ? this.editForm.alertStatus.join(',') : this.editForm.alertStatus,
              alert_user: this.editForm.alertUser,
              expand: this.editForm.expand,
              parameter: this.editForm.parameter,
              cronjob_start_time: this.editForm.cronjobStartTime,
              ai_experiment_id: this.editForm.ai_experiment_id
            }
            const res = await updatePipeline(submitData)
            if (res) {
              this.$message.success('修改任务编排成功！')
              this.editDialogVisible = false
              this.fetchPipelineList()
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

    // 删除
    handleDelete(row) {
      this.$confirm('确定删除该任务编排吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deletePipeline(row.id)
          if (res) {
            this.$message.success('删除成功')
            this.fetchPipelineList()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => { })
    },

    // 跳转到任务编排 flowBuilder
    handleOpenFlow(row) {
      this.$router.push({
        path: '/llmops/training/flowBuilder',
        query: {
          id: row.id || '',
        }
      })
    },

    // 跳转到任务列表页面
    showTaskDialog(row) {
      this.$router.push({
        path: '/llmops/training/task',
        query: {
          pipelineId: row.id
        }
      })
    },

    // 显示任务详情
    showTaskDetail(task) {
      this.$message.info(`查看任务详情: ${task.name}`)
    },

    // 暂停任务
    handlePauseTask(task) {
      this.$confirm(`确定暂停任务"${task.name}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success(`任务"${task.name}"已暂停`)
      }).catch(() => { })
    },

    // 分页
    handlePageChange(page) {
      this.currentPage = page
      this.fetchPipelineList()
    },

    // 每页条数变化
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.fetchPipelineList()
    },

    // 选中行变化
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
        this.$message.warning('请先选择要复制的编排')
        return
      }

      this.batchCopyLoading = true
      const ids = this.selectedRows.map(row => row.id)

      try {
        const res = await mulCopyPipeline({ ids: ids })
        if (res) {
          this.$message.success(`成功复制 ${this.selectedRows.length} 个任务编排`)
          this.batchDialogVisible = false
          this.fetchPipelineList()
          // 清空选中
          if (this.$refs.table) {
            this.$refs.table.clearSelection()
          }
        } else {
          this.$message.error(res.message || '批量复制失败')
        }
      } catch (error) {
        console.error('批量复制失败:', error)
        this.$message.error('批量复制失败')
      } finally {
        this.batchCopyLoading = false
      }
    },

    // 批量删除
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请先选择要删除的编排')
        return
      }
      try {
        await this.$confirm(`确定批量删除 ${this.selectedRows.length} 个任务编排吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        this.batchDeleteLoading = true
        const ids = this.selectedRows.map(row => row.id)
        const res = await mulDeletePipeline({ ids: ids })
        if (res) {
          this.$message.success(`成功删除 ${this.selectedRows.length} 个任务编排`)
          this.batchDialogVisible = false
          this.fetchPipelineList()
          if (this.$refs.table) {
            this.$refs.table.clearSelection()
          }
        } else {
          this.$message.error(res.message || '批量删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      } finally {
        this.batchDeleteLoading = false
      }
    },

    // 获取任务状态样式类
    getTaskStatusClass(status) {
      const map = {
        'pending': 'task-dot-pending',
        'running': 'task-dot-running',
        'completed': 'task-dot-completed',
        'failed': 'task-dot-failed'
      }
      return map[status] || 'task-dot-pending'
    },

    // 获取任务状态文本样式类
    getTaskStatusTextClass(status) {
      const map = {
        'pending': 'task-status-pending',
        'running': 'task-status-running',
        'completed': 'task-status-completed',
        'failed': 'task-status-failed'
      }
      return map[status] || 'task-status-pending'
    },

    // 获取任务状态文本
    getTaskStatusText(status) {
      const map = {
        'pending': '排队中',
        'running': '运行中',
        'completed': '已完成',
        'failed': '失败'
      }
      return map[status] || status
    }
  }
}
</script>

<style lang="scss" scoped>
// 添加批量操作弹窗样式
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

// 以下样式保持原有不变
.add-orchestration-dialog,
.edit-orchestration-dialog,
.detail-orchestration-dialog,
.task-orchestration-dialog {
  ::v-deep .el-dialog {
    max-height: 90vh;
    display: flex;
    flex-direction: column;
  }

  ::v-deep .el-dialog__header {
    padding: 20px 24px 0;
    border-bottom: 1px solid #e5e7eb;
    margin-right: 0;
    padding-bottom: 16px;
    flex-shrink: 0;
  }

  ::v-deep .el-dialog__headerbtn {
    top: 24px;
  }

  ::v-deep .el-dialog__body {
    padding: 24px;
    overflow-y: auto;
    flex: 1;
  }

  ::v-deep .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e5e7eb;
    flex-shrink: 0;
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

.task-list-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;

  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }
}

.task-list {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 12px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
  background-color: white;
  border-radius: 6px;
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.task-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.task-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.task-dot-pending {
  background-color: #f59e0b;
}

.task-dot-running {
  background-color: #3b82f6;
}

.task-dot-completed {
  background-color: #10b981;
}

.task-dot-failed {
  background-color: #ef4444;
}

.task-info {
  flex: 1;
}

.task-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.task-desc {
  font-size: 12px;
  color: #6b7280;
}

.task-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.task-status-pending {
  color: #f59e0b;
  background-color: #fef3c7;
}

.task-status-running {
  color: #3b82f6;
  background-color: #dbeafe;
}

.task-status-completed {
  color: #10b981;
  background-color: #d1fae5;
}

.task-status-failed {
  color: #ef4444;
  background-color: #fee2e2;
}

.task-list-content {
  .task-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #e5e7eb;

    &:last-child {
      border-bottom: none;
    }
  }

  .task-left {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    flex: 1;
  }

  .task-dot {
    display: inline-block;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-top: 4px;
    flex-shrink: 0;
  }

  .task-info {
    flex: 1;
  }

  .task-name {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .task-desc {
    font-size: 13px;
    color: #6b7280;
  }

  .task-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .task-status {
    font-size: 13px;
    padding: 4px 10px;
    border-radius: 4px;
    white-space: nowrap;
  }

  .task-detail-btn {
    color: #374151;
    border: 1px solid #d1d5db;
    background-color: white;
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;

    &:hover {
      background-color: #f3f4f6;
    }
  }

  .task-pause-btn {
    color: #f59e0b;
    border: 1px solid #fbbf24;
    background-color: white;
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;

    &:hover {
      background-color: #fef3c7;
    }
  }
}

.orchestration-container {
  min-height: calc(100vh - 60px);
}

.orchestration-content {
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
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.orchestration-table {
  font-size: 13px;
}

.name-cell {
  display: flex;
  flex-direction: column;
}

.name-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;

  &.clickable {
    color: #3b82f6;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
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

.json-editor-container {
  position: relative;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;

  .json-textarea ::v-deep textarea {
    font-family: 'Fira Code', 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace;
    font-size: 13px;
    line-height: 1.5;
    background-color: #f9fafb;
    color: #374151;
    border: none;
    padding: 12px;
    resize: vertical;

    &::placeholder {
      color: #9ca3af;
    }
  }

  .format-btn {
    position: absolute;
    top: 5px;
    right: 10px;
    z-index: 10;
    background: #4f46e5;
    color: #fff;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;

    &:hover {
      background: #4338ca;
      color: #fff;
    }
  }
}

.form-tip {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.5;
  margin-top: 4px;
}

.json-display {
  background-color: #f9fafb;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  font-family: monospace;
  font-size: 13px;
  color: #374151;
  max-height: 300px;
  overflow-y: auto;
  margin: 0;
}

.detail-item {
  margin-bottom: 20px;

  .detail-label {
    font-size: 13px;
    color: #6b7280;
    margin-bottom: 8px;
  }

  .detail-value {
    font-size: 14px;
    color: #111827;
    font-weight: 500;
  }
}
</style>