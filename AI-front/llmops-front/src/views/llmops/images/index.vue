<template>
  <div class="images-container">
    <div class="images-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">镜像管理</h1>
          <p class="page-subtitle">管理和发布镜像</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="create-btn" size="small" @click="showCreateDialog">
            <i class="el-icon-plus"></i> 添加镜像
          </el-button>
        </div>
      </div>

      <div class="section">
        <div class="search-card card-hover">
          <h2 class="section-title">镜像查询</h2>
          <div class="search-form">
            <div class="form-item">
              <label class="form-label">名称</label>
              <el-input v-model="filters.name" placeholder="镜像名称" size="small" clearable></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">创建者</label>
              <el-input v-model="filters.creator" placeholder="创建者名称" size="small" clearable></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">功能分类</label>
              <el-select v-model="filters.projectId" placeholder="全部分类" size="small" clearable style="width: 100%">
                <el-option v-for="item in jobTemplateOptions" :key="item.id" :label="item.name"
                  :value="item.id"></el-option>
              </el-select>
            </div>
            <div class="form-item">
              <label class="form-label">描述</label>
              <el-input v-model="filters.describe" placeholder="描述关键词" size="small" clearable></el-input>
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
          <el-table :data="images" style="width: 100%" stripe border
            :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }">
            <el-table-column label="镜像">
              <template slot-scope="scope">
                <span class="category-tag" :class="getCategoryClass(scope.row.name)">
                  {{ scope.row.name }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="描述">
              <template slot-scope="scope">
                {{ scope.row.describe }}
              </template>
            </el-table-column>


            <el-table-column prop="creatorName" label="创建者"></el-table-column>
            <el-table-column prop="changedOn" label="修改时间"></el-table-column>
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
              <span class="pagination-info">共 {{ total }} 个镜像</span>
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

    <!-- 添加镜像弹框 -->
    <el-dialog title="添加镜像" :visible.sync="createDialogVisible" width="600px" :close-on-click-modal="false"
      class="create-image-dialog">
      <el-form :model="imageForm" label-width="100px">
        <el-form-item label="功能分类">
          <el-select v-model="imageForm.projectId" placeholder="请选择功能分类" style="width: 100%;">
            <el-option v-for="item in jobTemplateOptions" :key="item.id" :label="item.name"
              :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="imageForm.repository" placeholder="请选择仓库" style="width: 100%;">
            <el-option v-for="item in repositoryOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="imageForm.name" placeholder="镜像名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="imageForm.describe" type="textarea" :rows="3" placeholder="镜像描述"></el-input>
        </el-form-item>
        <el-form-item label="Dockerfile">
          <el-input v-model="imageForm.dockerfile" type="textarea" :rows="8" placeholder="编写Dockerfile内容"></el-input>
        </el-form-item>
        <el-form-item label="git地址">
          <el-input v-model="imageForm.gitpath" placeholder="git地址"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateImage">创建</el-button>
      </span>
    </el-dialog>

    <!-- 修改镜像弹框 -->
    <el-dialog title="修改镜像" :visible.sync="editDialogVisible" width="500px" :close-on-click-modal="false"
      class="edit-image-dialog">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" placeholder="镜像名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.describe" placeholder="描述"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveImage">保存</el-button>
      </span>
    </el-dialog>

    <!-- 镜像详情弹窗 -->
    <el-dialog title="镜像 详情" :visible.sync="detailDialogVisible" width="600px" class="detail-dialog">
      <div class="detail-content">
        <div class="detail-item">
          <label>功能分类：</label>
          <span>{{ getProjectName(currentDetail.projectId) || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>仓库：</label>
          <span>{{ getRepositoryName(currentDetail.repository) || '-' }}</span>
        </div>
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
          <label>描述：</label>
          <span>{{ currentDetail.describe || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>启动命令：</label>
          <span>{{ currentDetail.command || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>Dockerfile：</label>
          <pre class="config-pre">{{ currentDetail.dockerfile || '-' }}</pre>
        </div>
        <div class="detail-item">
          <label>git地址：</label>
          <span>{{ currentDetail.gitpath || '-' }}</span>
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
import { getImageList, getImageDetail, createImage, updateImage, deleteImage } from '@/api/llmops/image'
import { getJobTemplateType } from '@/api/llmops/project'
import { getRepositoryNames } from '@/api/llmops/repository'

export default {
  name: 'Images',
  data() {
    return {
      filters: {
        name: '',
        creator: '',
        projectId: '',
        describe: '',
        repository: 1
      },
      images: [],
      total: 0,           // 总记录数
      currentPage: 1,    // 当前页码
      pageSize: 10,      // 每页显示条数
      createDialogVisible: false,
      editDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: {},
      imageForm: {
        projectId: '',
        repository: '',
        name: '',
        describe: '',
        dockerfile: '',
        gitpath: ''
      },
      editingImage: {},
      editForm: {
        describe: '',
        name: ''
      },
      jobTemplateOptions: [], // 功能分类下拉选项
      repositoryOptions: [] // 仓库下拉选项
    }
  },
  mounted() {
    this.loadImages()
    this.loadJobTemplateOptions()
    this.loadRepositoryOptions()
  },
  methods: {
    async loadJobTemplateOptions() {
      try {
        const res = await getJobTemplateType()
        if (res) {
          this.jobTemplateOptions = res || []
        }
      } catch (error) {
        console.error('加载功能分类选项失败', error)
      }
    },
    async loadRepositoryOptions() {
      try {
        const res = await getRepositoryNames()
        if (res) {
          this.repositoryOptions = res || []
        }
      } catch (error) {
        console.error('加载仓库选项失败', error)
      }
    },
    async loadImages() {
      try {
        // 构建查询参数
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }

        // 添加筛选条件
        if (this.filters.name) params.name = this.filters.name
        if (this.filters.creator) params.creator = this.filters.creator
        if (this.filters.projectId) params.projectId = this.filters.projectId
        if (this.filters.describe) params.describe = this.filters.describe

        const res = await getImageList(params)

        if (res && res.resultData) {
          this.images = res.resultData || []
          this.total = res.totalRecord || 0
        }
      } catch (error) {
        console.error('加载镜像列表失败', error)
        this.$message.error('加载镜像列表失败')
      }
    },
    showCreateDialog() {
      this.createDialogVisible = true
      this.imageForm = {
        projectId: '',
        repository: '',
        name: '',
        describe: '',
        dockerfile: '',
        gitpath: ''
      }
    },
    async handleCreateImage() {
      if (!this.imageForm.name.trim()) {
        this.$message.warning('请输入镜像名称')
        return
      }
      // if (!this.imageForm.describe) {
      //   this.$message.warning('请选择功能分类')
      //   return
      // }
      try {
        const res = await createImage({
          projectId: this.imageForm.projectId,
          name: this.imageForm.name,
          describe: this.imageForm.describe,
          dockerfile: this.imageForm.dockerfile,
          gitpath: this.imageForm.gitpath,
          repository: this.imageForm.repository
        })
        if (res) {
          this.$message.success('镜像创建成功')
          this.createDialogVisible = false
          await this.loadImages()
        }
      } catch (error) {
        console.error('创建镜像失败', error)
        this.$message.error('创建镜像失败')
      }
    },
    handleSearch() {
      this.currentPage = 1  // 重置到第一页
      this.loadImages()
    },
    async handleDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
      try {
        const res = await getImageDetail(row.id)
        if (res) {
          this.currentDetail = res
        } else if (res && res.data) {
          this.currentDetail = res.data
        }
      } catch (error) {
        console.error('获取镜像详情失败', error)
      }
    },
    handleModify(row) {
      this.editingImage = row
      this.editForm = {
        describe: row.describe || '',
        name: row.name || ''
      }
      this.editDialogVisible = true
    },
    async handleSaveImage() {
      if (!this.editForm.name.trim()) {
        this.$message.warning('请输入镜像名称')
        return
      }
      try {
        const res = await updateImage({
          id: this.editingImage.id,
          name: this.editForm.name,
          describe: this.editForm.describe,
          dockerfile: this.editForm.dockerfile,
          gitpath: this.editForm.gitpath,
          repository: this.editForm.repository

        })
        if (res) {
          this.$message.success('镜像修改成功')
          this.editDialogVisible = false
          await this.loadImages()
        }
      } catch (error) {
        console.error('修改镜像失败', error)
        this.$message.error('修改镜像失败')
      }
    },
    async handleDelete(row) {
      const name = row.project && row.project.name || row.name
      this.$confirm(`确定要删除镜像"${name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteImage(row.id)
          if (res) {
            this.$message.success('删除成功')
            await this.loadImages()
          }
        } catch (error) {
          console.error('删除镜像失败', error)
          this.$message.error('删除镜像失败')
        }
      }).catch(() => { })
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadImages()
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadImages()
    },
    getProjectName(projectId) {
      if (!projectId) return ''
      const item = this.jobTemplateOptions.find(opt => opt.id === projectId)
      return item ? item.name : ''
    },
    getRepositoryName(repositoryId) {
      if (!repositoryId) return ''
      const item = this.repositoryOptions.find(opt => opt.id === repositoryId)
      return item ? item.name : repositoryId
    },
    getCategoryClass(category) {
      const map = {
        '文本处理': 'category-dev',
        '模型服务化': 'category-inference',
        '机器学习算法': 'category-dev',
        '基础命令': 'category-dev',
        '大模型': 'category-inference',
        '数据预处理': 'category-dev',
        '开发环境': 'category-dev'
      }
      return map[category] || 'category-dev'
    }
  }
}
</script>

<style lang="scss" scoped>
.images-container {
  min-height: calc(100vh - 60px);
}

.images-content {
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

    .config-pre {
      margin: 0;
      white-space: pre-wrap;
      word-break: break-all;
      font-family: monospace;
      background: #f8f9fa;
      padding: 10px;
      border-radius: 4px;
      border: 1px solid #e9ecef;
      flex: 1;
    }

    &:last-child {
      border-bottom: none;
    }
  }
}

.button-item {
  display: flex;
  justify-content: flex-start;
}

.category-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.category-dev {
  background-color: #dbeafe;
  color: #2563eb;
}

.category-inference {
  background-color: #fef3c7;
  color: #d97706;
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