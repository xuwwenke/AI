<template>
  <div class="build-container">
    <div class="build-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">镜像构建</h1>
          <p class="page-subtitle">构建和管理容器镜像</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="create-btn" size="small" @click="showCreateDialog">
            <i class="el-icon-plus"></i> 添加容器
          </el-button>
        </div>
      </div>

      <div class="section">
        <div class="search-card card-hover">
          <h2 class="section-title">容器查询</h2>
          <div class="search-form">
            <div class="form-item">
              <label class="form-label">创建者</label>
              <el-input v-model="filters.creatorName" placeholder="创建者名称" size="small" clearable></el-input>
          
            </div>
            <!-- <div class="form-item">
              <label class="form-label">项目组</label>
              <el-select v-model="filters.projectId" placeholder="全部项目组" size="small" clearable style="width: 100%">
                <el-option label="AI Lab" :value="1"></el-option>
                <el-option label="风控部" :value="2"></el-option>
              </el-select>
            </div> -->
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
          <el-table :data="builds" style="width: 100%" stripe border v-loading="loading"
            :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }">
            <el-table-column prop="projectName" label="项目组" width="120"></el-table-column>
            <el-table-column prop="describe" label="描述" min-width="150" show-overflow-tooltip></el-table-column>
            <el-table-column prop="consecutiveBuild" label="连续构建" min-min-width="100">
              <template slot-scope="scope">
                <span :class="scope.row.consecutiveBuild ? 'tag-green' : 'tag-gray'">
                  {{ scope.row.consecutiveBuild ? 'true' : 'false' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="buildHistory" label="镜像历史" min-width="180">
              <template slot-scope="scope">
                <div class="image-info">
                  <div v-if="scope.row.baseImage"><span class="label">基础镜像：</span>{{ scope.row.baseImage }}</div>
                  <div v-if="scope.row.targetImage"><span class="label">目标镜像：</span>{{ scope.row.targetImage }}</div>
                  <div v-if="scope.row.currentImage"><span class="label">当前镜像：</span>{{ scope.row.currentImage }}</div>
                </div>
              </template>

            </el-table-column>
            <el-table-column prop="test" label="调试" min-width="120">
              <template slot-scope="scope">
                <div class="action-buttons">

                  <el-button size="small" @click="handleDebug(scope.row)">
                    调试
                  </el-button>
                  <el-button size="small" @click="handleTest(scope.row)">
                    清理
                  </el-button>
                  <el-button size="small" @click="handleSave(scope.row)">
                    保存
                  </el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleDetail(scope.row)">详情</el-button>
                <el-button type="text" size="small" @click="handleModify(scope.row)">修改</el-button>
                <el-button type="text" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-area">
            <div class="pagination-left">
              <span class="pagination-info">共 {{ totalCount }} 个容器</span>
            </div>
            <div class="pagination-right">
              <span class="page-size-label">每页显示：</span>
              <el-select v-model="pageSize" size="small" @change="handlePageSizeChange">
                <el-option :value="10" label="10"></el-option>
                <el-option :value="20" label="20"></el-option>
                <el-option :value="50" label="50"></el-option>
              </el-select>
              <el-pagination background layout="prev, pager, next" :total="totalCount" :page-size="pageSize"
                :current-page="currentPage" @current-change="handlePageChange"></el-pagination>
              <el-button type="primary" size="small" @click="handleNextPage"
                :disabled="currentPage >= totalPages">下一页</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加容器弹框 -->
    <el-dialog title="添加容器" :visible.sync="createDialogVisible" width="500px" :close-on-click-modal="false"
      class="create-container-dialog">
      <el-form :model="containerForm" label-width="100px">
        <!-- <el-form-item label="项目组">
          <el-select v-model="containerForm.project" placeholder="请选择项目组" style="width: 100%;">
            <el-option label="AI Lab" :value="1"></el-option>
            <el-option label="数据科学部" :value="2"></el-option>
            <el-option label="研发部" :value="3"></el-option>
            <el-option label="风控部" :value="4"></el-option>
          </el-select>
        </el-form-item> -->
        <el-form-item label="描述">
          <el-input v-model="containerForm.describe" type="textarea" :rows="3" placeholder="容器描述"></el-input>
        </el-form-item>
        <el-form-item label="基础镜像">
          <el-input v-model="containerForm.baseImage" placeholder="基础镜像"></el-input>
        </el-form-item>
        <el-form-item label="目标镜像">
          <el-input v-model="containerForm.targetImage" placeholder="目标镜像"></el-input>
        </el-form-item>
        <el-form-item label="连续构建">
          <el-select v-model="containerForm.consecutiveBuild" placeholder="请选择" style="width: 100%;">
            <el-option label="yes" value="yes"></el-option>
            <el-option label="no" value="no"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资源配置">
          <div style="display: flex; gap: 12px;">
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">内存</div>
              <el-input v-model="containerForm.resourceMemory" placeholder="内存"></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">CPU</div>
              <el-input v-model="containerForm.resourceCpu" placeholder="CPU"></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">GPU</div>
              <el-input v-model="containerForm.resourceGpu" placeholder="GPU"></el-input>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateContainer">创建</el-button>
      </span>
    </el-dialog>

    <!-- 修改容器弹框 -->
    <el-dialog title="修改容器" :visible.sync="editDialogVisible" width="500px" :close-on-click-modal="false"
      class="edit-container-dialog">
      <el-form :model="editForm" label-width="100px">
        <!-- <el-form-item label="项目组">
          <el-select v-model="editForm.project" placeholder="请选择项目组" style="width: 100%;">
            <el-option label="AI Lab" :value="1"></el-option>
            <el-option label="数据科学部" :value="2"></el-option>
            <el-option label="研发部" :value="3"></el-option>
            <el-option label="风控部" :value="4"></el-option>
          </el-select>
        </el-form-item> -->
        <el-form-item label="描述">
          <el-input v-model="editForm.describe" type="textarea" :rows="3" placeholder="容器描述"></el-input>
        </el-form-item>
        <el-form-item label="基础镜像">
          <el-input v-model="editForm.baseImage" placeholder="基础镜像"></el-input>
        </el-form-item>
        <el-form-item label="目标镜像">
          <el-input v-model="editForm.targetImage" placeholder="目标镜像"></el-input>
        </el-form-item>
        <el-form-item label="连续构建">
          <el-select v-model="editForm.consecutiveBuild" placeholder="请选择" style="width: 100%;">
            <el-option label="yes" value="yes"></el-option>
            <el-option label="no" value="no"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资源配置">
          <div style="display: flex; gap: 12px;">
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">内存</div>
              <el-input v-model="editForm.resourceMemory" placeholder="内存"></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">CPU</div>
              <el-input v-model="editForm.resourceCpu" placeholder="CPU"></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">GPU</div>
              <el-input v-model="editForm.resourceGpu" placeholder="GPU"></el-input>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </span>
    </el-dialog>

    <!-- 容器详情弹窗 -->
    <el-dialog title="容器 详情" :visible.sync="detailDialogVisible" width="700px" class="detail-dialog">
      <div class="detail-content">
        <div class="detail-item">
          <label>项目组：</label>
          <span>{{ currentDetail.projectName || currentDetail.project || '-' }}</span>
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
          <label>描述：</label>
          <span>{{ currentDetail.describe || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>命名空间：</label>
          <span>{{ currentDetail.namespace || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>基础镜像：</label>
          <span>{{ currentDetail.baseImage || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>目标镜像：</label>
          <span>{{ currentDetail.targetImage || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>最新镜像：</label>
          <span>{{ currentDetail.currentImage || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>需要gpu：</label>
          <span>{{ getExpandResource(currentDetail.expand, 'resource_gpu') && getExpandResource(currentDetail.expand, 'resource_gpu') != '0' ? 'true' : 'false' }}</span>
        </div>
        <div class="detail-item">
          <label>连续构建：</label>
          <span>{{ currentDetail.consecutiveBuild ? 'true' : 'false' }}</span>
        </div>
        <div class="detail-item">
          <label>扩展：</label>
          <pre class="config-pre">{{ currentDetail.expand || '{}' }}</pre>
        </div>
        <div class="detail-item">
          <label>id：</label>
          <span>{{ currentDetail.id || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>内存：</label>
          <span>{{ getExpandResource(currentDetail.expand, 'resource_memory') || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>cpu：</label>
          <span>{{ getExpandResource(currentDetail.expand, 'resource_cpu') || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>gpu：</label>
          <span>{{ getExpandResource(currentDetail.expand, 'resource_gpu') || '-' }}</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDockerList, getDockerDetail, createDocker, updateDocker, deleteDocker, deleteDockerPod, saveDocker, debugDocker } from '@/api/llmops/docker'
import * as k8s_api from '@/api/app/k8s'

export default {
  name: 'ImageBuild',
  data() {
    return {
      filters: {
        creator: '',
        
      },
      builds: [],
      totalCount: 0,
      currentPage: 1,
      pageSize: 20,
      createDialogVisible: false,
      editDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: {},
      loading: false,
      containerForm: {
        project: '',
        describe: '',
        baseImage: '',
        targetImage: '',
        consecutiveBuild: 'yes',
        resourceMemory: '',
        resourceCpu: '',
        resourceGpu: ''
      },
      editForm: {
        id: null,
        project: '',
        describe: '',
        baseImage: '',
        targetImage: '',
        consecutiveBuild: 'yes',
        resourceMemory: '',
        resourceCpu: '',
        resourceGpu: ''
      }
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.totalCount / this.pageSize)
    }
  },
  mounted() {
    this.loadBuilds()
  },
  methods: {
    // 加载容器列表
    async loadBuilds() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }

        if (this.filters.creatorName) params.creator = this.filters.creatorName
        if (this.filters.projectId) params.project = this.filters.projectId

        const res = await getDockerList(params)

        if (res.resultData) {
          this.builds = res.resultData
          this.totalCount = res.totalRecord || 0

        } else {
          this.$message.error((res && res.message) || '加载容器列表失败')
        }
      } catch (error) {
        console.error('加载容器列表失败', error)
        this.$message.error('加载容器列表失败')
      } finally {
        this.loading = false
      }
    },

    // 显示创建弹框
    showCreateDialog() {
      this.createDialogVisible = true
      this.containerForm = {
        project: '',
        describe: '',
        baseImage: '',
        targetImage: '',
        consecutiveBuild: 'yes',
        resourceMemory: '',
        resourceCpu: '',
        resourceGpu: ''
      }
    },

    // 创建容器
    async handleCreateContainer() {
      if (!this.containerForm.describe.trim()) {
        this.$message.warning('请输入描述')
        return
      }
      if (!this.containerForm.baseImage.trim()) {
        this.$message.warning('请输入基础镜像')
        return
      }
      if (!this.containerForm.targetImage.trim()) {
        this.$message.warning('请输入目标镜像')
        return
      }

      try {
        const res = await createDocker({
          project: this.containerForm.project,
          describe: this.containerForm.describe,
          base_image: this.containerForm.baseImage,
          target_image: this.containerForm.targetImage,
          consecutive_build: this.containerForm.consecutiveBuild === 'yes',
          resource_memory: this.containerForm.resourceMemory,
          resource_cpu: this.containerForm.resourceCpu,
          resource_Gpu: this.containerForm.resourceGpu

        })

        if (res) {
          this.$message.success('容器创建成功')
          this.createDialogVisible = false
          await this.loadBuilds()
        } else {
          this.$message.error((res && res.message) || '创建失败')
        }
      } catch (error) {
        console.error('创建容器失败', error)
        this.$message.error('创建容器失败')
      }
    },

    // 查询
    handleSearch() {
      this.currentPage = 1
      this.loadBuilds()
    },

    // 测试
    async handleTest(row) {
      try {
        await this.$confirm(`确定要清理容器"${row.describe}"吗?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await deleteDockerPod(row.id)
        if (res && res.status === 0) {
          this.$message.success('清理成功')
          await this.loadBuilds()
        } else {
          this.$message.error((res && res.message) || '清理失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清理容器失败', error)
          this.$message.error('清理容器失败')
        }
      }
    },

    // 调试
    async handleDebug(row) {
      if (!row || !row.id) return;

      const loading = this.$loading({
        target: 'body',
        text: '正在获取调试信息...',
        background: 'rgba(0, 0, 0, 0.3)'
      });

      try {
        const res = await debugDocker(row.id);
        if (res && res.status !== 0) {
          this.$message.error(res.message || '调试启动失败');
          loading.close();
          return;
        }

        const namespace = row.namespace || row.projectName || (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || 'public';
        const clusterName = row.clusterName || (this.$store.state.project.currentCluster && this.$store.state.project.currentCluster.name);

        // 使用用户指定的标签选择器格式
        // pod-name=docker-登录名-id, pod-type=docker
        const userName = localStorage.getItem('userName') || 'admin';
        const podNameLabel = `docker-${userName}-${row.id}`;
        const labelSelector = `pod-name=${podNameLabel},pod-type=docker`;

        // 尝试获取对应的 Pod
        const podRes = await k8s_api.getContainerGroup(namespace, clusterName, labelSelector);
        const items = (podRes && podRes.items) || [];
        const runningPods = items.filter(i => i.status && i.status.phase === 'Running');
        const pod = runningPods[0] || items[0];

        loading.close();

        if (!pod) {
          this.$message.warning('调试容器创建中，请稍后重试');
          return;
        }

        // 跳转到 WebSSH 终端
        const { href } = this.$router.resolve({
          path: `/webssh/console/${namespace}/${pod.metadata.name}`,
          query: {
            proName: namespace,
            clusterName: clusterName
          }
        });
        window.open(href, '_blank');
      } catch (error) {
        loading.close();
        console.error('调试容器失败:', error);
        this.$message.error('调试失败');
      }
    },

    // 保存容器配置
    async handleSave(row) {
      try {
        await this.$confirm(`确定要保存容器"${row.describe}"的配置吗?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })

        const loading = this.$loading({
          lock: true,
          text: '正在保存配置...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })

        try {
          // 调用保存接口
          const res = await saveDocker(row.id)

          if (res && res.status === 0) {
            this.$message.success('容器保存成功')
            // 刷新列表以获取最新数据
            await this.loadBuilds()
          } else {
            this.$message.error((res && res.message) || '保存失败')
          }
        } finally {
          loading.close()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('保存容器配置失败', error)
          this.$message.error('保存容器配置失败')
        }
      }
    },

    // 详情
    async handleDetail(row) {
      this.currentDetail = row
      this.detailDialogVisible = true
      try {
        const res = await getDockerDetail(row.id)
        if (res) {
          this.currentDetail = res
        } else if (res && res.data) {
          this.currentDetail = res.data
        }
      } catch (error) {
        console.error('获取容器详情失败', error)
      }
    },

    // 修改 - 打开编辑弹框
    handleModify(row) {
      this.editForm = {
        id: row.id,
        project: row.project || '',
        describe: row.describe || '',
        baseImage: row.baseImage || '',
        targetImage: row.targetImage || '',
        consecutiveBuild: row.consecutiveBuild ? 'yes' : 'no',
        resourceMemory: JSON.parse(row.expand).resource_memory || '',
        resourceCpu: JSON.parse(row.expand).resource_cpu || '',
        resourceGpu: JSON.parse(row.expand).resource_gpu || ''
      }
      this.editDialogVisible = true
    },

    // 保存修改
    async handleSaveEdit() {
      if (!this.editForm.describe.trim()) {
        this.$message.warning('请输入描述')
        return
      }
      if (!this.editForm.baseImage.trim()) {
        this.$message.warning('请输入基础镜像')
        return
      }
      if (!this.editForm.targetImage.trim()) {
        this.$message.warning('请输入目标镜像')
        return
      }

      try {
        const res = await updateDocker({
          id: this.editForm.id,
          project: this.editForm.project,
          describe: this.editForm.describe,
          base_image: this.editForm.baseImage,
          target_image: this.editForm.targetImage,
          consecutive_build: this.editForm.consecutiveBuild === 'yes',
          resource_memory: this.editForm.resourceMemory,
          resource_cpu: this.editForm.resourceCpu,
          resource_gpu: this.editForm.resourceGpu

        })

        if (res) {
          this.$message.success('容器修改成功')
          this.editDialogVisible = false
          await this.loadBuilds()
        } else {
          this.$message.error((res && res.message) || '修改失败')
        }
      } catch (error) {
        console.error('修改容器失败', error)
        this.$message.error('修改容器失败')
      }
    },

    // 删除
    async handleDelete(row) {
      const name = row.project || row.name
      try {
        await this.$confirm(`确定要删除容器"${name}"吗?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const res = await deleteDocker(row.id)

        if (res && res.status === 0) {
          this.$message.success('删除成功')
          if (this.builds.length === 1 && this.currentPage > 1) {
            this.currentPage--
          }
          await this.loadBuilds()
        } else {
          this.$message.error((res && res.message) || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除容器失败', error)
          this.$message.error('删除容器失败')
        }
      }
    },

    // 分页切换
    handlePageChange(page) {
      this.currentPage = page
      this.loadBuilds()
    },

    // 每页条数切换
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadBuilds()
    },

    // 下一页
    handleNextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++
        this.loadBuilds()
      }
    },

    // 从 expand JSON 中获取资源信息
    getExpandResource(expand, key) {
      if (!expand) return null
      try {
        const expandObj = typeof expand === 'string' ? JSON.parse(expand) : expand
        return expandObj[key] || null
      } catch (e) {
        console.error('解析 expand 字段失败:', e)
        return null
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.build-container {
  min-height: calc(100vh - 60px);
}

.build-content {
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

.search-form {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.tag-green {
  background-color: #dcfce7;
  color: #16a34a;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.tag-gray {
  background-color: #f3f4f6;
  color: #6b7280;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.history-tag {
  background-color: #f3f4f6;
  color: #374151;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.test-btn {
  background-color: #dcfce7;
  color: #16a34a;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.debug-btn {
  background-color: #dbeafe;
  color: #2563eb;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.save-btn {
  background-color: #fef3c7;
  color: #d97706;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
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

@media (max-width: 1200px) {
  .search-form {
    grid-template-columns: repeat(2, 1fr);
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