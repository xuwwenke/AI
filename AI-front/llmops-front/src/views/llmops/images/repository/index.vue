<template>
  <div class="repository-container">
    <div class="repository-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">镜像仓库</h1>
          <p class="page-subtitle">管理镜像仓库配置</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="create-btn" size="small" @click="showCreateDialog">
            <i class="el-icon-plus"></i> 添加仓库
          </el-button>
        </div>
      </div>

      <div class="section">
        <div class="search-card card-hover">
          <h2 class="section-title">仓库查询</h2>
          <div class="search-form">
            <div class="form-item">
              <label class="form-label">名称</label>
              <el-input v-model="filters.name" placeholder="仓库名称" size="small" clearable
                @clear="handleSearch"></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">仓库地址</label>
              <el-input v-model="filters.server" placeholder="仓库地址" size="small" clearable
                @clear="handleSearch"></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">k8s hubsecret</label>
              <el-input v-model="filters.hubsecret" placeholder="hubsecret" size="small" clearable
                @clear="handleSearch"></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">用户名</label>
              <el-input v-model="filters.user" placeholder="用户名" size="small" clearable
                @clear="handleSearch"></el-input>
            </div>
            <div class="form-item button-item">
              <el-button type="primary" size="small" @click="handleSearch">
                <i class="el-icon-search"></i> 查询
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="table-card card-hover">
          <el-table :data="repositories" style="width: 100%" stripe border
            :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }">
            <el-table-column prop="name" label="名称" min-width="150">
            </el-table-column>
            <el-table-column prop="server" label="仓库地址" min-width="200" show-overflow-tooltip></el-table-column>
            <el-table-column prop="hubsecret" label="k8s hubsecret" min-width="150"></el-table-column>
            <el-table-column prop="user" label="创建者" width="100"></el-table-column>
            <el-table-column prop="changedOn" label="更新时间" width="160"></el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleDetail(scope.row)">详情</el-button>
                <el-button type="text" size="small" @click="handleModify(scope.row)">修改</el-button>
                <el-button type="text" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-area">
            <div class="pagination-left">
              <span class="pagination-info">共 {{ total }} 个仓库</span>
            </div>
            <div class="pagination-right">
              <span class="page-size-label">每页显示：</span>
              <el-select v-model="pageSize" size="small" @change="handlePageSizeChange">
                <el-option :value="10" label="10"></el-option>
                <el-option :value="20" label="20"></el-option>
                <el-option :value="50" label="50"></el-option>
                <el-option :value="100" label="100"></el-option>
              </el-select>
              <el-pagination background layout="prev, pager, next, jumper" :total="total" :page-size="pageSize"
                :current-page="currentPage" @current-change="handlePageChange">
              </el-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加仓库弹框 -->
    <el-dialog title="添加仓库" :visible.sync="createDialogVisible" width="500px" :close-on-click-modal="false"
      class="create-repository-dialog">
      <el-form :model="repositoryForm" label-width="120px">
        <el-form-item label="名称">
          <el-input v-model="repositoryForm.name" placeholder="仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库地址">
          <el-input v-model="repositoryForm.server" placeholder="仓库地址"></el-input>
        </el-form-item>
        <el-form-item label="k8s hubsecret">
          <el-input v-model="repositoryForm.hubsecret" placeholder="hubsecret"></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="repositoryForm.user" placeholder="镜像仓库的用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="repositoryForm.password" type="password" placeholder="镜像仓库的链接密码"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateRepository">创建</el-button>
      </span>
    </el-dialog>

    <!-- 修改仓库弹框 -->
    <el-dialog title="修改仓库" :visible.sync="editDialogVisible" width="500px" :close-on-click-modal="false"
      class="edit-repository-dialog">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" placeholder="仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库地址">
          <el-input v-model="editForm.server" placeholder="仓库地址"></el-input>
        </el-form-item>
        <el-form-item label="k8s hubsecret">
          <el-input v-model="editForm.hubsecret" placeholder="hubsecret"></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.user" placeholder="镜像仓库的用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="editForm.password" type="password" placeholder="镜像仓库的链接密码"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRepository">保存</el-button>
      </span>
    </el-dialog>

    <!-- 仓库详情弹框 -->
    <el-dialog title="仓库 详情" :visible.sync="detailDialogVisible" width="600px" class="detail-dialog">
      <div class="detail-content">
        <div class="detail-item">
          <label>创建者：</label>
          <span>{{ currentDetail.creator || currentDetail.createdByFk || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>修改者：</label>
          <span>{{ currentDetail.modifier || currentDetail.changedByFk || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>创建时间：</label>
          <span>{{ currentDetail.createdOn || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>修改时间：</label>
          <span>{{ currentDetail.changedOn || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>名称：</label>
          <span>{{ currentDetail.name || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>仓库：</label>
          <span>{{ currentDetail.server || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>用户名：</label>
          <span>{{ currentDetail.user || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>k8s hubsecret：</label>
          <span>{{ currentDetail.hubsecret || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>id：</label>
          <span>{{ currentDetail.id || '-' }}</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getRepositoryList, createRepository, updateRepository, deleteRepository,getRepositoryDetail } from '@/api/llmops/repository'

export default {
  name: 'ImageRepository',
  data() {
    return {
      filters: {
        name: '',
        server: '',
        hubsecret: '',
        user: ''
      },
      repositories: [],
      total: 0,           // 总记录数
      currentPage: 1,    // 当前页码
      pageSize: 10,      // 每页显示条数
      createDialogVisible: false,
      editDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: {},
      repositoryForm: {
        name: '',
        server: '',
        secret: '',
        user: '',
        password: ''
      },
      editingRepository: {},
      editForm: {
        name: '',
        server: '',
        hubsecret: '',
        user: '',
        password: ''
      }
    }
  },
  mounted() {
    this.loadRepositories()
  },
  methods: {
    async loadRepositories() {
      try {
        // 构建查询参数
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }

        // 添加筛选条件
        if (this.filters.name) params.name = this.filters.name
        if (this.filters.server) params.server = this.filters.server
        if (this.filters.hubsecret) params.hubsecret = this.filters.hubsecret
        if (this.filters.user) params.user = this.filters.user

        const res = await getRepositoryList(params)
        if (res && res.resultData) {
          this.repositories = res.resultData
          this.total = res.totalRecord || 0
        } else if (res && res.data) {
          // 兼容不同的返回格式
          this.repositories = res.data
          this.total = res.total || 0
        }
      } catch (error) {
        console.error('加载仓库列表失败', error)
        this.$message.error('加载仓库列表失败')
      }
    },
    showCreateDialog() {
      this.createDialogVisible = true
      this.repositoryForm = {
        name: '',
        server: '',
        hubsecret: '',
        user: '',
        password: ''
      }
    },
    async handleCreateRepository() {
      if (!this.repositoryForm.name.trim()) {
        this.$message.warning('请输入仓库名称')
        return
      }
      if (!this.repositoryForm.server.trim()) {
        this.$message.warning('请输入仓库地址')
        return
      }
      if (!this.repositoryForm.hubsecret.trim()) {
        this.$message.warning('请输入k8s hubsecret')
        return
      }
      try {
        const res = await createRepository({
          name: this.repositoryForm.name,
          server: this.repositoryForm.server,
          hubsecret: this.repositoryForm.hubsecret,
          user: this.repositoryForm.user,
          password: this.repositoryForm.password,
        })
        if (res) {
          this.$message.success('仓库创建成功')
          this.createDialogVisible = false
          await this.loadRepositories()
        }
      } catch (error) {
        console.error('创建仓库失败', error)
        this.$message.error('创建仓库失败')
      }
    },
    handleSearch() {
      this.currentPage = 1  // 重置到第一页
      this.loadRepositories()
    },
    async handleDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
      try {
        const res = await getRepositoryDetail(row.id)
        if (res) {
          this.currentDetail = res
        } else if (res && res.data) {
          this.currentDetail = res.data
        }
      } catch (error) {
        console.error('获取仓库详情失败', error)
      }
    },
    handleModify(row) {
      this.editingRepository = row
      this.editForm = {
        name: row.name || '',
        server: row.server || '',
        hubsecret: row.hubsecret || '',
        user: row.user || '',
        password: row.password || '',
      }
      this.editDialogVisible = true
    },
    async handleSaveRepository() {
      if (!this.editForm.name.trim()) {
        this.$message.warning('请输入仓库名称')
        return
      }
      try {
        const res = await updateRepository({
          id: this.editingRepository.id,
          name: this.editForm.name,
          server: this.editForm.server,
          hubsecret: this.editForm.hubsecret,
          user: this.editForm.user,
          password: this.editForm.password,
        })
        if (res) {
          this.$message.success('仓库修改成功')
          this.editDialogVisible = false
          await this.loadRepositories()
        }
      } catch (error) {
        console.error('修改仓库失败', error)
        this.$message.error('修改仓库失败')
      }
    },
    async handleDelete(row) {
      const name = row.project && row.project.name || row.name
      this.$confirm(`确定要删除仓库"${name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteRepository(row.id)
          if (res) {
            this.$message.success('删除成功')
            await this.loadRepositories()
          }
        } catch (error) {
          console.error('删除仓库失败', error)
          this.$message.error('删除仓库失败')
        }
      }).catch(() => { })
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadRepositories()
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadRepositories()
    }
  }
}
</script>

<style lang="scss" scoped>
.repository-container {
  min-height: calc(100vh - 60px);
}

.repository-content {
  // max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
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

.search-card,
.table-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  align-items: end;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.button-item {
  display: flex;
  justify-content: flex-start;
}

.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

.pagination-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-size-label {
  font-size: 14px;
  color: #6b7280;
}

/* 分页组件样式 */
.pagination-right {
}

.detail-dialog {
  .detail-content {
    padding: 10px 20px;
    max-height: 500px;
    overflow-y: auto;
  }
  
  .detail-item {
    display: flex;
    margin-bottom: 12px;
    line-height: 1.6;
    // border-bottom: 1px solid #f0f2f5;
    padding-bottom: 8px;

    label {
      width: 140px;
      color: #303133;
      font-weight: 600;
      flex-shrink: 0;
      text-align: right;
      padding-right: 10px;
    }

    span {
      color: #303133;
      word-break: break-all;
    }
    
    &:last-child {
      border-bottom: none;
    }
  }
}

@media (max-width: 1200px) {
  .search-form {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .search-form {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .pagination-area {
    flex-direction: column;
    gap: 12px;
  }

  .pagination-right {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>