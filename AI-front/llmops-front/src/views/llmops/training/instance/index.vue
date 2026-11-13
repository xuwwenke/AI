<template>
  <div class="instance-container">
    <div class="instance-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">训练实例</h1>
          <p class="page-subtitle">管理和监控训练实例</p>
        </div>
        <!-- <div class="header-right">
          <el-button type="primary" class="add-btn" size="small" @click="showAddDialog">
            <i class="el-icon-plus"></i> 新建实例
          </el-button>
        </div> -->
      </div>

      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">总实例数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
          <div class="stat-trend up">
            <i class="el-icon-top"></i> +{{ stats.weekNewCount }} 本周新增
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value">{{ stats.runningCount }}</div>
          <div class="stat-trend">
            <i class="el-icon-check"></i> 全部正常
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ stats.completedCount }}</div>
          <div class="stat-trend">
            成功率 {{ stats.totalCount ? Math.round((stats.completedCount / stats.totalCount) * 100) : 0 }}%
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">失败</div>
          <div class="stat-value">{{ stats.failedCount }}</div>
          <div class="stat-trend down">
            需要关注
          </div>
        </div>
      </div>

      <!-- 智能训练机器人 -->
      <ai-robot-section title="智能训练机器人" subtitle="通过问答形式快速创建训练实例流程" :welcome-message="{
        role: 'assistant',
        content: '您好！我是智能训练机器人，可以帮助您：',
        features: [
          '快速创建训练实例',
          '配置训练任务参数',
          '查看训练日志',
          '管理训练实例'
        ],
        question: '请问有什么可以帮助您的？'
      }" :quick-questions="[
        '列出当前训练实例列表',
        '查看训练实例【id或名称】日志信息',
        '停止训练实例【id或名称】',
        '删除训练实例【id或名称】'
      ]" placeholder="请输入您的问题，例如：帮我创建一个文本分类训练实例" storage-key="llmops_training_instance_chat_v1" />

      <!-- 过滤条件区域 -->
      <div class="search-section">
        <div class="section-title">训练实例查询</div>
        <el-form :model="searchForm" :inline="true" class="search-form" label-position="top">
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 280px;">
              <el-option label="运行中" value="running"></el-option>
              <el-option label="已完成" value="completed"></el-option>
              <el-option label="失败" value="failed"></el-option>
              <el-option label="排队中" value="pending"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="searchForm.label" placeholder="标签" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="searchForm.name" placeholder="名称" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="集群">
            <el-input v-model="searchForm.cluster" placeholder="集群" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="注释">
            <el-input v-model="searchForm.annotation" placeholder="注释" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="属性">
            <el-input v-model="searchForm.property" placeholder="属性" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="状态详情">
            <el-input v-model="searchForm.statusDetail" placeholder="状态详情" clearable style="width: 280px;"></el-input>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width: 280px;"></el-input>
          </el-form-item>
        </el-form>
        <div class="search-actions">
          <el-button size="small" @click="handleReset">重置</el-button>
          <el-button type="primary" size="small" @click="handleSearch">查询</el-button>
        </div>
      </div>

      <!-- 实例查询列表 -->
      <div class="table-section" v-loading="loading">
        <div class="section-title">实例查询列表</div>
        <el-table :data="instanceList" border style="width: 100%;" class="instance-table">
          <!-- <el-table-column prop="project" label="项目组" width="120"></el-table-column> -->
          <el-table-column prop="name" label="任务编排" min-width="180">
            <template slot-scope="scope">
              <div class="pipeline-link" @click="handleOpenFlow(scope.row)"
                v-html="formatModelUrl(getPipelineName(scope.row))"></div>
            </template>
          </el-table-column>
          <el-table-column prop="cluster" label="集群" min-width="100"></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="150"></el-table-column>
          <el-table-column prop="changeTime" label="更新时间" min-width="150"></el-table-column>
          <el-table-column prop="runningHours" label="耗时" min-width="100"></el-table-column>
          <el-table-column prop="phase" label="删除前状态" min-width="120">
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <!-- <template slot-scope="scope">
              <span class="status-dot" :class="getStatusClass(scope.row.status)"></span>
              <span :class="getStatusTextClass(scope.row.status)">{{ getStatusText(scope.row.status) }}</span>
            </template> -->
          </el-table-column>
          <el-table-column prop="username" label="用户名" min-width="100"></el-table-column>
          <el-table-column label="日志" min-width="80">
            <template slot-scope="scope">
              <el-button type="text" class="log-btn" @click="handleShowLog(scope.row)">查看</el-button>
            </template>
          </el-table-column>
          <el-table-column label="停止" width="80">
            <template slot-scope="scope">
              <el-button type="text" @click="handleStop(scope.row)">停止</el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="showDetailDialog(scope.row)">详情</el-button>
              <!-- <el-button type="text" size="mini" @click="showEditDialog(scope.row)">修改</el-button> -->
              <el-button type="text" size="mini" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <div class="pagination-left">
            显示 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, total) }} 条，共 {{ total }} 条
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

    <!-- 训练实例详情弹框 -->
    <el-dialog title="训练实例详情" :visible.sync="detailDialogVisible" width="700px" :close-on-click-modal="false"
      class="detail-instance-dialog">
      <div class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">项目组</div>
              <div class="detail-value">{{ detailData.namespace || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">任务编排</div>

              <div class="detail-value"> {{ (detailData.labels ? (JSON.parse(detailData.labels)['pipeline-name'] || '-')
                :
                '-') }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">集群</div>
              <div class="detail-value">{{ detailData.cluster || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">创建时间</div>
              <div class="detail-value">{{ detailData.createTime || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">更新时间</div>
              <div class="detail-value">{{ detailData.changeTime || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">耗时</div>
              <div class="detail-value">{{ detailData.duration || '--' }}</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">删除前状态</div>
              <div class="detail-value">{{ detailData.phase || '--' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">状态</div>
              <div class="detail-value">
                <span class="status-dot" :class="getDetailStatusClass(detailData.status)"></span>
                <span :class="getDetailStatusTextClass(detailData.status)">{{ getDetailStatusText(detailData.status)
                }}</span>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">用户名</div>
              <div class="detail-value">{{ detailData.username }}</div>
            </div>
          </el-col>
          <!-- <el-col :span="12">
            <div class="detail-item">
              <div class="detail-label">日志</div>
              <div class="detail-value">
                <el-button type="text" size="small" class="detail-log-btn">查看</el-button>
              </div>
            </div>
          </el-col> -->
        </el-row>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 修改训练实例弹框 -->
    <el-dialog title="修改训练实例" :visible.sync="editDialogVisible" width="700px" :close-on-click-modal="false"
      class="edit-instance-dialog">
      <el-form :model="editForm" ref="editForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目组">
              <el-select v-model="editForm.project" placeholder="请选择" style="width: 100%;">
                <el-option label="AI Lab" value="AI Lab"></el-option>
                <el-option label="Data Science" value="Data Science"></el-option>
                <el-option label="Research" value="Research"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称">
              <el-input v-model="editForm.name" placeholder="名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本">
              <el-input v-model="editForm.version" placeholder="版本"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="描述">
              <el-input v-model="editForm.desc" placeholder="描述"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模型文件">
              <div class="file-input-wrapper">
                <el-input v-model="editForm.modelFile" placeholder="模型文件" style="width: calc(100% - 70px);"></el-input>
                <el-button size="small" style="margin-left: 10px;">导入</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练框架">
              <el-input v-model="editForm.framework" placeholder="训练框架"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="运行ID">
              <el-input v-model="editForm.runId" placeholder="运行ID"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运行时间">
              <el-input v-model="editForm.runTime" placeholder="运行时间"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="指标">
              <el-input v-model="editForm.metrics" placeholder="指标"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="MD5">
              <el-input v-model="editForm.md5" placeholder="MD5"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="推理框架">
              <el-input v-model="editForm.inferenceFramework" placeholder="推理框架"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务编排ID">
              <el-input v-model="editForm.orchestrationId" placeholder="任务编排ID"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getWorkflowList, getWorkflowDetail, createWorkflow, updateWorkflow, deleteWorkflow, stopWorkflow, getWorkflowStats } from '@/api/llmops/instance'
import AiRobotSection from '@/components/llmops/AiRobotSection.vue'

export default {
  name: 'TrainingInstance',
  components: {
    AiRobotSection
  },
  data() {
    return {
      loading: false,
      detailDialogVisible: false,
      detailData: {
        project: '',
        orchestration: '',
        cluster: '',
        createTime: '',
        updateTime: '',
        duration: '',
        phase: '',
        status: '',
        username: ''
      },
      stats: {
        totalCount: 0,
        weekNewCount: 0,
        runningCount: 0,
        completedCount: 0,
        failedCount: 0
      },
      editDialogVisible: false,
      editForm: {
        project: '',
        name: '',
        version: '',
        desc: '',
        modelFile: '',
        framework: '',
        runId: '',
        runTime: '',
        metrics: '',
        md5: '',
        inferenceFramework: '',
        orchestrationId: ''
      },
      searchForm: {
        status: '',
        label: '',
        name: '',
        cluster: '',
        annotation: '',
        property: '',
        statusDetail: '',
        username: ''
      },
      currentPage: 1,
      pageSize: 10,
      total: 0,
      instanceList: []
    }
  },
  created() {
    this.fetchList()
    this.fetchStats()
  },
  methods: {
    async fetchStats() {
      try {
        const res = await getWorkflowStats()
        if (res) {
          this.stats = {
            totalCount: res.totalCount || 0,
            weekNewCount: res.weekNewCount || 0,
            runningCount: res.runningCount || 0,
            completedCount: res.completedCount || 0,
            failedCount: res.failedCount || 0
          }
        }
      } catch (e) {
        console.error('获取统计数据失败：', e)
      }
    },
    async fetchList() {
      this.loading = true
      try {
        // const params = {
        //   page: this.currentPage - 1,
        //   page_size: this.pageSize,
        //   filters: [],
        //   str_related: 1,
        // }
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          ...this.searchForm
        }
        const { resultData, totalRecord } = await getWorkflowList(params)
        this.instanceList = resultData || []
        this.total = totalRecord || 0
      } catch (e) {
        this.$message.error('获取列表失败：' + (e.message || e))
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchList()
    },
    handleReset() {
      this.searchForm = {
        status: '',
        label: '',
        name: '',
        cluster: '',
        annotation: '',
        property: '',
        statusDetail: '',
        username: ''
      }
      this.currentPage = 1
      this.fetchList()
    },
    async showEditDialog(row) {
      this.editDialogVisible = true
      try {
        const res = await getWorkflowDetail(row.id)
        this.editForm = res || {}
      } catch (e) {
        this.$message.error('获取详情失败：' + (e.message || e))
      }
    },
    async handleEditSubmit() {
      try {
        if (this.editForm.id) {
          await updateWorkflow(this.editForm)
          this.$message.success('修改训练实例成功！')
        } else {
          await createWorkflow(this.editForm)
          this.$message.success('新增训练实例成功！')
        }
        this.editDialogVisible = false
        this.fetchList()
      } catch (e) {
        this.$message.error('保存失败：' + (e.message || e))
      }
    },
    async showDetailDialog(row) {
      this.detailDialogVisible = true
      try {
        const res = await getWorkflowDetail(row.id)
        this.detailData = res || {}
      } catch (e) {
        this.$message.error('获取详情失败：' + (e.message || e))
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该训练实例吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteWorkflow(row.id)
        this.$message.success('删除成功')
        this.fetchList()
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error('删除失败：' + (e.message || e))
        }
      }
    },
    async handleStop(row) {
      try {
        await this.$confirm('确认停止该训练实例吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await stopWorkflow(row.id)
        this.$message.success('已发送停止指令')
        this.fetchList()
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error('操作失败：' + (e.message || e))
        }
      }
    },
    getDetailStatusClass(status) {
      const map = {
        'running': 'status-dot-running',
        'completed': 'status-dot-completed',
        'failed': 'status-dot-failed',
        'pending': 'status-dot-pending'
      }
      return map[status] || 'status-dot-pending'
    },
    getDetailStatusTextClass(status) {
      const map = {
        'running': 'status-text-running',
        'completed': 'status-text-completed',
        'failed': 'status-text-failed',
        'pending': 'status-text-pending'
      }
      return map[status] || 'status-text-pending'
    },
    getDetailStatusText(status) {
      const map = {
        'running': '运行中',
        'completed': '已完成',
        'failed': '失败',
        'pending': '排队中'
      }
      return map[status] || status
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.fetchList()
    },
    showAddDialog() {
      this.editForm = {
        project: '',
        name: '',
        version: '',
        desc: '',
        modelFile: '',
        framework: '',
        runId: '',
        runTime: '',
        metrics: '',
        md5: '',
        inferenceFramework: '',
        orchestrationId: ''
      }
      this.editDialogVisible = true
    },

    handleShowLog(row) {
      this.$router.push({
        path: '/llmops/training/log',
        query: {
          id: row.id,
          cluster: row.cluster
        }
      })
    },
    handleOpenFlow(row) {
      let pipelineId = row.id;
      if (row.labels) {
        try {
          const labels = typeof row.labels === 'string' ? JSON.parse(row.labels) : row.labels;
          if (labels['pipeline-id']) {
            pipelineId = labels['pipeline-id'];
          }
        } catch (e) {
          console.error('解析 labels 失败:', e);
        }
      }
      this.$router.push({
        path: '/llmops/training/flowBuilder',
        query: {
          id: pipelineId || '',
        }
      })
    },
    getPipelineName(row) {
      if (row.labels) {
        try {
          const labels = typeof row.labels === 'string' ? JSON.parse(row.labels) : row.labels;
          return labels['pipeline-name'] || row.name;
        } catch (e) {
          console.error('解析 labels 中的 pipeline-name 失败:', e);
        }
      }
      return row.name;
    },

    getStatusClass(status) {
      const map = {
        'running': 'status-dot-running',
        'completed': 'status-dot-completed',
        'failed': 'status-dot-failed',
        'pending': 'status-dot-pending'
      }
      return map[status] || 'status-dot-pending'
    },
    getStatusTextClass(status) {
      const map = {
        'Running': 'status-text-running',
        'Succeeded': 'status-text-completed',
        'Deleted': 'status-text-failed',
        'pending': 'status-text-pending'
      }
      return map[status] || 'status-text-pending'
    },
    getStatusText(status) {
      const map = {
        'running': '运行中',
        'completed': '已完成',
        'failed': '失败',
        'pending': '排队中'
      }
      return map[status] || status
    },
    formatModelUrl(urlHtml) {
      if (!urlHtml) return '--';
      const baseUrl = 'http://192.168.224.251:747';
      // 使用正则替换 href 中的相对路径，拼接上 IP 前缀
      return urlHtml.replace(/href=(['"])(.*?)\1/g, (match, quote, path) => {
        if (path.startsWith('http')) return match;
        return `href=${quote}${baseUrl}${path}${quote}`;
      });
    },
  }
}
</script>

<style lang="scss" scoped>
.robot-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;

  &.is-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 2000;
    margin: 0;
    border-radius: 0;
    display: flex;
    flex-direction: column;
    padding: 32px;
    background-color: white;

    .robot-header {
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f3f4f6;
    }

    .robot-content {
      flex: 1;
      max-height: none;
      margin-bottom: 24px;
    }

    .robot-input-section {
      margin-bottom: 24px;
    }

    .quick-questions {
      margin-top: auto;
      padding-top: 24px;
    }
  }
}

.robot-status-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.assistant-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fullscreen-btn {
  font-size: 18px;
  color: #6b7280;
  padding: 4px;
}

.fullscreen-btn:hover {
  color: #2563eb;
}

.stop-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  border: none !important;
  color: white !important;
}

.stop-btn:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c) !important;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3) !important;
}

.robot-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.robot-title-section {
  flex: 1;
}

.robot-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.robot-subtitle {
  font-size: 13px;
  color: #6b7280;
}

.robot-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background-color: #d1fae5;
  border-radius: 12px;
}

.status-dot-online {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #10b981;
}

.status-text-online {
  font-size: 12px;
  color: #059669;
  font-weight: 500;
}

.robot-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 16px;

  .chat-message {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    &.user-message {
      flex-direction: row-reverse;
    }
  }

  .robot-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
    flex-shrink: 0;

    &.user-avatar {
      background: linear-gradient(135deg, #10b981, #059669);
    }
  }

  .robot-bubble {
    background: #f3f4f6;
    padding: 12px 16px;
    border-radius: 12px;
    border-top-left-radius: 0;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    max-width: 80%;
    width: fit-content;

    &.user-bubble {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: white;
      border-top-left-radius: 12px;
      border-top-right-radius: 0;
    }
  }

  .message-text {
    font-size: 14px;
    line-height: 1.6;
    word-break: break-word;
  }

  .message-loading {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #6b7280;
  }

  .robot-features {
    margin: 10px 0 0 0;
    padding-left: 20px;

    li {
      font-size: 13px;
      color: #4b5563;
      margin-bottom: 6px;
      list-style-type: disc;
    }
  }

  .user-bubble .robot-features li {
    color: rgba(255, 255, 255, 0.9);
  }

  .robot-question {
    font-size: 14px;
    color: #1f2937;
    font-weight: 500;
    margin-top: 10px;
  }

  .user-bubble .robot-question {
    color: white;
  }
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.quick-question-btn {
  background-color: #f3f4f6 !important;
  border: none !important;
  color: #374151;
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 16px;

  &:hover {
    background-color: #e5e7eb;
    border-color: #d1d5db;
    color: #1f2937;
  }
}

.robot-input-section {
  display: flex;
  gap: 12px;
  align-items: center;

  .robot-input {
    flex: 1;

    ::v-deep .el-input__inner {
      border-radius: 8px;
    }
  }

  .send-btn {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    border: none;
    color: white;
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    border-radius: 8px;
    flex-shrink: 0;

    &:hover {
      opacity: 0.9;
      color: white;
    }
  }
}

.detail-instance-dialog,
.edit-instance-dialog {
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
}

.file-input-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
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

  .detail-log-btn {
    color: #3b82f6;
    padding: 0;
  }
}

.log-view-content {
  border-radius: 8px;
  min-height: calc(100vh - 100px);
}

.instance-container {
  min-height: calc(100vh - 60px);
}

.instance-content {
  // max-width: 1800px;
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
  grid-template-columns: repeat(4, 1fr);
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

.stat-trend.down {
  color: #ef4444;
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

.instance-table {
  font-size: 13px;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-dot-running {
  background-color: #10b981;
}

.status-dot-completed {
  background-color: #059669;
}

.status-dot-failed {
  background-color: #ef4444;
}

.status-dot-pending {
  background-color: #f59e0b;
}

.status-text-running {
  color: #10b981;
}

.status-text-completed {
  color: #059669;
}

.status-text-failed {
  color: #ef4444;
}

.status-text-pending {
  color: #f59e0b;
}

.log-btn {
  color: #3b82f6;
}

.pipeline-link {
  color: #3b82f6;
  cursor: pointer;
  word-break: break-all;
  line-height: 1.5;

  &:hover {
    color: #2563eb;
    text-decoration: underline;
  }

  ::v-deep a {
    color: inherit;
    pointer-events: none;
    text-decoration: none;
  }
}

.stop-btn {
  color: #ef4444;
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

<!-- 非 scoped 样式，用于 Markdown 渲染 -->
<style>
/* Markdown 全局样式 */
.markdown-body {
  word-wrap: break-word !important;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin-top: 16px !important;
  margin-bottom: 8px !important;
  font-weight: 600 !important;
  line-height: 1.25 !important;
  color: #1f2937 !important;
  display: block !important;
}

.markdown-body h1 {
  font-size: 20px !important;
  border-bottom: 1px solid #e5e7eb !important;
  padding-bottom: 8px !important;
}

.markdown-body h2 {
  font-size: 18px !important;
}

.markdown-body h3 {
  font-size: 16px !important;
  color: #374151 !important;
}

.markdown-body h4 {
  font-size: 15px !important;
}

.markdown-body p {
  margin: 8px 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-body li {
  margin: 4px 0;
}

.markdown-body code {
  background-color: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  color: #e11d48;
}

.markdown-body pre {
  background-color: #1f2937;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-body pre code {
  background-color: transparent;
  padding: 0;
  color: #e5e7eb;
}

.markdown-body a {
  color: #2563eb;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

/* ===== AI 助手统一增强样式 ===== */
.ai-temp-control {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 12px;
  color: #374151;
}

.ai-temp-control .ai-temp-label {
  color: #6b7280;
}

.ai-temp-control .ai-temp-range {
  width: 90px;
  accent-color: #3b82f6;
}

.ai-temp-control .ai-temp-value {
  min-width: 22px;
  text-align: right;
  color: #2563eb;
  font-weight: 600;
}

.ai-temp-presets {
  display: inline-flex;
  gap: 4px;
  margin-left: 2px;
}

.ai-temp-preset-btn {
  border: 1px solid #d1d5db;
  background: #fff;
  color: #4b5563;
  border-radius: 10px;
  font-size: 11px;
  line-height: 1;
  padding: 3px 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-temp-preset-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.ai-temp-preset-btn.active {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-color: #2563eb;
  color: #fff;
}

.ai-status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #3b82f6;
  margin-right: 4px;
  animation: aiDotPulse 1s ease-in-out infinite;
}

.ai-status-dot.generating {
  background: #22c55e;
}

.ai-status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  padding: 2px 8px;
  font-size: 11px;
  color: #16a34a;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  line-height: 1.4;
}

.ai-error-card {
  margin-top: 8px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  word-break: break-word;
}

.ai-error-text {
  flex: 1;
}

.ai-retry-btn {
  flex-shrink: 0;
}

.ai-message-time {
  margin-top: 6px;
  font-size: 11px;
  color: #9ca3af;
}

.ai-message-time.user-time {
  text-align: right;
}

@keyframes aiDotPulse {
  0% {
    transform: scale(0.85);
    opacity: 0.65;
  }

  50% {
    transform: scale(1.15);
    opacity: 1;
  }

  100% {
    transform: scale(0.85);
    opacity: 0.65;
  }
}
</style>
