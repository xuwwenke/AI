<template>
  <div class="experiment-management-container">
     <!-- 头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">实验管理</h1>
          <p class="page-subtitle">管理和监控您的机器学习实验</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="create-btn" @click="addOrUpdateHandle()">
            <i class="el-icon-plus"></i> 创建实验
          </el-button>
        </div>
      </div>
    <!-- Top statistics cards -->
    <el-row :gutter="20" class="statistics-cards">
      <el-col :span="6">
        <el-card class="statistic-card">
          <div class="card-title">总实验数</div>
          <div class="card-value">{{ statistics.totalCount }}</div>
          <div class="card-footer">↑{{ statistics.weekNewCount }}本周新增</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistic-card running">
          <div class="card-title">运行中</div>
          <div class="card-value">{{ statistics.runningCount }}</div>
          <div class="card-footer">全部正常</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistic-card completed">
          <div class="card-title">已完成</div>
          <div class="card-value">{{ statistics.completedCount }}</div>
          <div class="card-footer">成功率{{ statistics.successRate }}%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistic-card failed">
          <div class="card-title">失败</div>
          <div class="card-value">{{ statistics.failedCount }}</div>
          <div class="card-footer">需要关注</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Experiment Query Section -->
    <el-card class="query-section">
      <div class="section-title">实验查询</div>
      <el-form :inline="true" :model="queryForm" class="query-form" label-position="top">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 280px">
            <el-option label="未开始" :value="0"></el-option>
            <el-option label="运行中" :value="1"></el-option>
            <el-option label="已完成" :value="2"></el-option>
            <el-option label="失败" :value="3"></el-option>
            <el-option label="已暂停" :value="4"></el-option>
            
          </el-select>
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model="queryForm.modelName" placeholder="模型名称" clearable  style="width: 280px"></el-input>
        </el-form-item>
        <el-form-item label="创建者">
          <el-input v-model="queryForm.creator" placeholder="创建者" clearable  style="width: 280px"></el-input>
        </el-form-item>
      </el-form>
      <div class="query-actions">
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="primary" @click="searchExperiments">查询</el-button>
      </div>
    </el-card>

    <!-- Experiment List Section -->
    <el-card class="list-section">
      <div class="section-title">实验列表</div>
    
      <el-table :data="experimentList" v-loading="dataListLoading" style="width: 100%">
        <el-table-column prop="name" label="实验名称"></el-table-column>
        <el-table-column prop="modelName" label="模型"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
             <el-tag :type="getStatusTagType(scope.row.status)">{{ statusMap[scope.row.status] || scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间">
            <template slot-scope="scope">
                {{ scope.row.startTime || '--' }}
            </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间">
            <template slot-scope="scope">
                {{ scope.row.endTime || '--' }}
            </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建者">
            <template slot-scope="scope">
                {{ scope.row.creator || '--' }}
            </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button type="text" size="small" @click="addOrUpdateHandle(scope.row.id)">编辑</el-button>
            <el-button type="text" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-dropdown v-if="getStatusActions(scope.row.status).length > 0" trigger="click" @command="(cmd) => handleStatusChange(scope.row, cmd)" style="margin-left:4px">
              <el-button type="text" size="small">
                状态<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  v-for="action in getStatusActions(scope.row.status)"
                  :key="action.key"
                  :command="action.key"
                >{{ action.label }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <span class="total-items">显示{{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, totalExperiments) }}条,共{{ totalExperiments }}条</span>
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next"
          :total="totalExperiments">
        </el-pagination>
      </div>
    </el-card>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="refreshAll"></add-or-update>
    <!-- 弹窗, 详情 -->
    <detail v-if="detailVisible" ref="detail"></detail>
  </div>
</template>

<script>
import { getExperimentList, getExperimentStatistics, deleteExperiment, startExperiment, pauseExperiment, finishExperiment, failExperiment } from '@/api/llmops/experiment'
import AddOrUpdate from './add-or-update'
import Detail from './detail'
export default {
  name: 'experimentIndex',
  components: {
    AddOrUpdate,
    Detail
  },
  data() {
    return {
      addOrUpdateVisible: false,
      detailVisible: false,
      statistics: {
        totalCount: 0,
        weekNewCount: 0,
        runningCount: 0,
        completedCount: 0,
        successRate: 0,
        failedCount: 0
      },
      queryForm: {
        project: '',
        status: '',
        modelName: '',
        creator: '',
       
      },
      statusMap: {
        0: '未开始',
        1: '运行中',
        2: '已完成',
        3: '失败',
        4: '已暂停'
      },
      experimentList: [],
      totalExperiments: 0,
      currentPage: 1,
      pageSize: 10,
      dataListLoading: false
    }
  },
  mounted() {
    this.fetchExperimentStatistics()
    this.fetchExperimentData()
  },
  methods: {
    resetQuery() {
      this.queryForm = {
        project: '',
        status: '',
        modelName: '',
        creator: '',
      }
      this.fetchExperimentData()
    },
    searchExperiments() {
      this.currentPage = 1
      this.fetchExperimentData()
    },
    handlePageChange(page) {
      this.currentPage = page

      this.fetchExperimentData()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.fetchExperimentData()
    },
    handleDetail(row) {
      this.detailVisible = true
      this.$nextTick(() => {
        this.$refs.detail.init(row)
      })
    },
    // 新增 / 修改
    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id)
      })
    },
    handleDelete(id) {
      this.$confirm('确定对该条数据进行 [删除] 操作?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteExperiment(id)
          this.$message({
            message: '操作成功',
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.refreshAll()
            }
          })
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    },
    getStatusActions(status) {
      const actions = []
      switch (status) {
        case 0: // 未开始
          actions.push({ key: 'start', label: '开始' })
          break
        case 1: // 运行中
          actions.push({ key: 'pause', label: '暂停' })
          actions.push({ key: 'finish', label: '结束' })
          actions.push({ key: 'fail', label: '失败' })
          break
        case 4: // 已暂停
          actions.push({ key: 'start', label: '开始' })
          break
      }
      return actions
    },
    handleStatusChange(row, action) {
      const actionMap = {
        start: { api: startExperiment, label: '开始' },
        pause: { api: pauseExperiment, label: '暂停' },
        finish: { api: finishExperiment, label: '结束' },
        fail: { api: failExperiment, label: '失败' }
      }
      const target = actionMap[action]
      if (!target) return
      this.$confirm(`确定对该实验进行 [${target.label}] 操作?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await target.api(row.id)
          this.$message({
            message: '状态变更成功',
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.refreshAll()
            }
          })
        } catch (error) {
          console.error('状态变更失败:', error)
        }
      }).catch(() => {})
    },
    refreshAll() {
      this.fetchExperimentData()
      this.fetchExperimentStatistics()
    },
    getStatusTagType(status) {
      switch (status) {
        case 0:
          return 'info'
        case 1:
          return ''
        case 2:
          return 'success'
        case 3:
          return 'danger'
        case 4:
          return 'warning'
        default:
          return 'info'
      }
    },
    async fetchExperimentData() {
      this.dataListLoading = true
      try {
        const res = await getExperimentList({...this.queryForm,  pageNum: this.currentPage,
          pageSize: this.pageSize})
        if (res && res.resultData) {
          this.experimentList = res.resultData || []
          this.totalExperiments = res.totalRecord || 0
        } else {
          this.experimentList = []
          this.totalExperiments = 0
        }
      } catch (error) {
        console.error('Error fetching experiment list:', error)
      } finally {
        this.dataListLoading = false
      }
    },
    async fetchExperimentStatistics() {
      try {
        const data = await getExperimentStatistics()
        this.statistics = data
         if (data.completedCount !== undefined && data.failedCount !== undefined) {
      const totalFinished = data.completedCount + data.failedCount
      if (totalFinished > 0) {
        this.statistics.successRate = (data.completedCount / totalFinished * 100).toFixed(1)
      } else {
        this.statistics.successRate = 0
      }
    }
      } catch (error) {
        console.error('Error fetching experiment statistics:', error)
      
      }
    }
  }
}
</script>

<style scoped>
.experiment-management-container {
  min-height: 100%;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}
.experiment-management-container h1 {
  font-size: 24px;
  margin-bottom: 5px;
  color: #303133;
}
.experiment-management-container p {
  color: #909399;
  margin-bottom: 20px;
  font-size: 14px;
}
.statistics-cards {
  margin-bottom: 20px;
}
.statistic-card {
  text-align: left;
}
.statistic-card .card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.statistic-card .card-value {
  font-size: 30px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}
.statistic-card .card-footer {
  font-size: 12px;
  color: #c0c4cc;
}
.statistic-card.running .card-value {
  color: #409eff;
}
.statistic-card.completed .card-value {
  color: #67c23a;
}
.statistic-card.failed .card-value {
  color: #f56c6c;
}
.query-section,
.list-section {
  margin-bottom: 20px;
}
.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
}
.query-form >>> .el-form-item {
  margin-right: 20px;
  margin-bottom: 15px;
}
.query-actions {
  text-align: right;
}
.create-experiment-button {
  float: right;
  margin-bottom: 15px;
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 20px;
}
.total-items {
  margin-right: auto;
  color: #606266;
  font-size: 14px;
}
.el-table {
  margin-top: 15px;
}
</style>
