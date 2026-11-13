<template>
  <el-dialog
    :title="(experimentName || '') + '实验-编排列表'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="1000px"
    class="experiment-detail-dialog"
  >
    <div v-loading="loading">
      <el-table :data="pipelineList" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="编排名称" min-width="120">
          <template slot-scope="scope">
            <div class="name-cell">
              <div class="name-title clickable" @click="handleOpenFlow(scope.row)">{{ scope.row.name }}</div>
             
            </div>
          </template>
        </el-table-column>
          <el-table-column prop="describe" label="编排描述" min-width="120"></el-table-column>
        <el-table-column prop="creator" label="创建者" width="150"></el-table-column>
        <el-table-column prop="changedOn" label="修改时间" width="180">
          <template slot-scope="scope">
            {{ scope.row.changedOn || scope.row.modified || '--' }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next"
          :total="totalCount"
        >
        </el-pagination>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">关闭</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getPipelinePageByExperimentId } from '@/api/llmops/orchestration'

export default {
  data() {
    return {
      visible: false,
      loading: false,
      experimentId: null,
      experimentName: '',
      pipelineList: [],
      currentPage: 1,
      pageSize: 10,
      totalCount: 0
    }
  },
  methods: {
    init(row) {
      this.visible = true
      this.experimentId = row.id
      this.experimentName = row.name
      this.currentPage = 1
      this.getDataList()
    },
    async getDataList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          aiExperimentId: this.experimentId
        }
        const res = await getPipelinePageByExperimentId(params)
        if (res && res.resultData) {
          this.pipelineList = res.resultData
          this.totalCount = res.totalRecord || 0
        } else {
          this.pipelineList = []
          this.totalCount = 0
        }
      } catch (error) {
        this.$message.error('获取编排列表失败')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.getDataList()
    },
    handlePageChange(val) {
      this.currentPage = val
      this.getDataList()
    },
    handleOpenFlow(row) {
      this.visible = false
      this.$router.push({
        path: '/llmops/training/flowBuilder',
        query: {
          id: row.id || '',
        }
      })
    }
  }
}
</script>

<style scoped>
.experiment-detail-dialog >>> .el-dialog__body {
  padding: 20px 24px;
}

.name-cell {
  display: flex;
  flex-direction: column;
}

.name-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.name-title.clickable {
  cursor: pointer;
  color: #409EFF;
}

.name-title.clickable:hover {
  text-decoration: underline;
}

.name-desc {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
