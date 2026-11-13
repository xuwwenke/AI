<template>
  <div class="task-container">
    <div class="task-content">
      <div class="page-header">
        <div class="header-left">
          <div class="title-row">
            <el-button size="mini" icon="el-icon-back" circle @click="$router.go(-1)" class="back-btn"></el-button>
            <h1 class="page-title">任务列表</h1>
          </div>
          <p class="page-subtitle">查看和管理任务流中的具体任务节点</p>
        </div>
        <div class="header-right">
          <!-- 移除添加任务按钮 -->
        </div>
      </div>

      <div class="section">
        <div class="search-card card-hover">
          <div class="search-form">
            <div class="form-item">
              <label class="form-label">名称</label>
              <el-input v-model="filters.name" placeholder="任务名称" size="small" clearable @keyup.enter.native="handleSearch"></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">标签</label>
              <el-input v-model="filters.label" placeholder="标签名称" size="small" clearable @keyup.enter.native="handleSearch"></el-input>
            </div>
            <div class="form-item button-item">
              <el-button type="primary" size="small" @click="handleSearch">
                <i class="el-icon-search"></i> 查询
              </el-button>
              <el-button size="small" @click="handleReset">重置</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="table-card card-hover">
          <div class="table-header-row">
            <div class="section-title">任务列表</div>
          </div>
          <el-table :data="taskList" style="width: 100%" stripe border v-loading="loading"
            :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="label" label="标签" min-width="150" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="pipelineId" label="任务流" min-width="150" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="jobTemplateId" label="任务模板" min-width="120" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="volumeMount" label="挂载目录" min-width="150" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="nodeSelector" label="调度机器" min-width="150" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="command" label="启动命令" min-width="150" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="overwriteEntrypoint" label="覆盖原始启动命令" min-width="140">
              <template slot-scope="scope">
                <el-tag :type="scope.row.overwriteEntrypoint ? 'success' : 'info'" size="mini">
                  {{ scope.row.overwriteEntrypoint ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="workingDir" label="启动目录" min-width="150" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="args" label="启动参数" min-width="200" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="resourceMemory" label="内存申请" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="resourceCpu" label="CPU申请" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="resourceGpu" label="GPU申请" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="resourceRdma" label="RDMA申请" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="timeout" label="超时中断" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="retry" label="重试次数" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="creator" label="创建者" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="modifier" label="修改者" min-width="100" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="createdOn" label="创建时间" min-width="160" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="changedOn" label="修改时间" min-width="160" :formatter="formatTableValue"></el-table-column>
            <el-table-column prop="monitoring" label="监控" min-width="100">
            </el-table-column>
            <el-table-column prop="expand" label="扩展" min-width="100" show-overflow-tooltip :formatter="formatTableValue"></el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleDetail(scope.row)">详情</el-button>
                <el-button type="text" size="small" @click="handleEdit(scope.row)">修改</el-button>
                <el-button type="text" size="small" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-area">
            <div class="pagination-left">
              <span class="pagination-info">共 {{ totalCount }} 条任务</span>
            </div>
            <div class="pagination-right">
              <el-pagination background layout="prev, pager, next, sizes, jumper" :total="totalCount"
                :page-size="pageSize" :current-page="currentPage" :page-sizes="[10, 20, 50, 100]"
                @current-change="handlePageChange" @size-change="handlePageSizeChange">
              </el-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 任务详情对话框 -->
    <el-dialog title="任务详情" :visible.sync="detailVisible" width="700px">
      <div class="detail-content" v-if="currentTask">
        <div class="detail-row">
          <div class="detail-item"><span class="label">名称:</span><span class="value">{{ currentTask.name || '--' }}</span></div>
          <div class="detail-item"><span class="label">标签:</span><span class="value">{{ currentTask.label || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item"><span class="label">任务流:</span><span class="value">{{ currentTask.pipelineId || '--' }}</span></div>
          <div class="detail-item"><span class="label">任务模板:</span><span class="value">{{ currentTask.jobTemplateId || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item full"><span class="label">挂载目录:</span><span class="value">{{ currentTask.volumeMount || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item full"><span class="label">调度机器:</span><span class="value">{{ currentTask.nodeSelector || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item full"><span class="label">启动命令:</span><span class="value">{{ currentTask.command || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item"><span class="label">覆盖启动命令:</span><span class="value">{{ currentTask.overwriteEntrypoint ? '是' : '否' }}</span></div>
          <div class="detail-item"><span class="label">启动目录:</span><span class="value">{{ currentTask.workingDir || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item full"><span class="label">启动参数:</span><pre class="json-value">{{ formatJson(currentTask.args) }}</pre></div>
        </div>
        <div class="detail-row">
          <div class="detail-item"><span class="label">内存:</span><span class="value">{{ currentTask.resourceMemory || '--' }}</span></div>
          <div class="detail-item"><span class="label">CPU:</span><span class="value">{{ currentTask.resourceCpu || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item"><span class="label">GPU:</span><span class="value">{{ currentTask.resourceGpu || '--' }}</span></div>
          <div class="detail-item"><span class="label">RDMA:</span><span class="value">{{ currentTask.resourceRdma || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item"><span class="label">超时:</span><span class="value">{{ (currentTask.timeout !== null && currentTask.timeout !== undefined) ? currentTask.timeout : '--' }}</span></div>
          <div class="detail-item"><span class="label">重试:</span><span class="value">{{ (currentTask.retry !== null && currentTask.retry !== undefined) ? currentTask.retry : '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item"><span class="label">创建者:</span><span class="value">{{ currentTask.creator || '--' }}</span></div>
          <div class="detail-item"><span class="label">修改者:</span><span class="value">{{ currentTask.modifier || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item"><span class="label">创建时间:</span><span class="value">{{ currentTask.createdOn || '--' }}</span></div>
          <div class="detail-item"><span class="label">修改时间:</span><span class="value">{{ currentTask.changedOn || '--' }}</span></div>
        </div>
        <div class="detail-row">
          <div class="detail-item full"><span class="label">扩展:</span><pre class="json-value">{{ formatJson(currentTask.expand) }}</pre></div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 任务修改对话框 -->
    <el-dialog title="修改任务" :visible.sync="editVisible" width="800px" :close-on-click-modal="false" class="edit-task-dialog">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" placeholder="任务名称"></el-input>
          <div class="form-tip">英文名(小写字母、数字、- 组成)，最长50个字符</div>
        </el-form-item>
        <el-form-item label="标签" prop="label">
          <el-input v-model="editForm.label" placeholder="标签"></el-input>
          <div class="form-tip">中文名</div>
        </el-form-item>
        <el-form-item label="启动目录" prop="workingDir">
          <el-input v-model="editForm.workingDir" placeholder="请输入启动目录"></el-input>
          <div class="form-tip">工作目录，容器启动的初始所在目录，不填默认使用Dockerfile内定义的工作目录</div>
        </el-form-item>
        <el-form-item label="启动命令" prop="command">
          <el-input v-model="editForm.command" placeholder="请输入启动命令"></el-input>
          <div class="form-tip">启动命令</div>
        </el-form-item>
        <el-form-item label="启动参数" prop="args" class="json-form-item">
          <div class="json-editor-header">
            <el-button type="primary" size="mini" class="format-btn" @click="formatJsonField('editForm', 'args')">格式化</el-button>
          </div>
          <el-input v-model="editForm.args" type="textarea" :rows="8" placeholder="请输入启动参数"></el-input>
        </el-form-item>
        <el-form-item label="挂载目录" prop="volumeMount">
          <el-input v-model="editForm.volumeMount" placeholder="请输入挂载目录"></el-input>
          <div class="form-tip">外部挂载，格式：$pvc_name1(pvc):/$container_path1,$hostpath1(hostpath):/$container_path2 注意pvc会自动挂载对应目录下的个人username子目录</div>
        </el-form-item>
        <el-form-item label="调度机器" prop="nodeSelector">
          <el-input v-model="editForm.nodeSelector" placeholder="请输入调度机器"></el-input>
          <div class="form-tip">运行当前task所在的机器</div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="内存申请" prop="resourceMemory">
              <el-input v-model="editForm.resourceMemory" placeholder="例如: 2G"></el-input>
              <div class="form-tip">内存的资源使用配置，示例1G, 10G，最大100G，如需更多联系管理员</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="cpu申请" prop="resourceCpu">
              <el-input v-model="editForm.resourceCpu" placeholder="例如: 2"></el-input>
              <div class="form-tip">cpu的资源使用配置(单位核)，示例 0.4, 10, 最大50核，如需更多联系管理员</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="gpu申请" prop="resourceGpu">
              <el-input v-model="editForm.resourceGpu" placeholder="例如: 0"></el-input>
              <div class="form-tip">gpu的资源使用配置(单位卡)，示例:1, 2, 训练任务每个容器独占整卡。申请具体的卡型号，可以类似 1(V100)</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="rdma申请" prop="resourceRdma">
              <el-input v-model="editForm.resourceRdma" placeholder="例如: 0"></el-input>
              <div class="form-tip">RDMA的资源使用配置，示例 0, 1, 10, 填写方式咨询管理员</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="超时中断" prop="timeout">
              <el-input v-model="editForm.timeout" placeholder="例如: 0"></el-input>
              <div class="form-tip">task运行时长限制，为0表示不限制(单位s)</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重试次数" prop="retry">
              <el-input v-model="editForm.retry" placeholder="例如: 0"></el-input>
              <div class="form-tip">task重试次数</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="跳过" prop="skip">
          <el-radio-group v-model="editForm.skip">
            <el-radio :label="true">yes</el-radio>
            <el-radio :label="false">no</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="扩展" prop="expand" class="json-form-item">
          <div class="json-editor-header">
            <el-button type="primary" size="mini" class="format-btn" @click="formatJsonField('editForm', 'expand')">格式化</el-button>
          </div>
          <el-input v-model="editForm.expand" type="textarea" :rows="4" placeholder="请输入扩展"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { searchTasks, getTaskDetail, updateTask, deleteTask } from '@/api/llmops/task'

export default {
  name: 'TaskList',
  data() {
    return {
      pipelineId: '',
      loading: false,
      taskList: [],
      totalCount: 0,
      currentPage: 1,
      pageSize: 10,
      filters: {
        name: '',
        label: ''
      },
      detailVisible: false,
      editVisible: false,
      currentTask: null,
      editForm: {
        id: '',
        name: '',
        label: '',
        workingDir: '',
        command: '',
        args: '',
        volumeMount: '',
        nodeSelector: '',
        resourceMemory: '',
        resourceCpu: '',
        resourceGpu: '',
        resourceRdma: '',
        timeout: 0,
        retry: 0,
        skip: false,
        expand: ''
      },
      editRules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { pattern: /^[a-z0-9-]+$/, message: '英文名(小写字母、数字、- 组成)', trigger: 'blur' },
          { max: 50, message: '最长50个字符', trigger: 'blur' }
        ],
        label: [{ required: true, message: '请输入标签', trigger: 'blur' }],
        resourceMemory: [{ required: true, message: '请输入内存申请', trigger: 'blur' }],
        resourceCpu: [{ required: true, message: '请输入cpu申请', trigger: 'blur' }],
        resourceGpu: [{ required: true, message: '请输入gpu申请', trigger: 'blur' }],
        timeout: [{ required: true, message: '请输入超时中断', trigger: 'blur' }],
        retry: [{ required: true, message: '请输入重试次数', trigger: 'blur' }],
        args: [
          { validator: (rule, value, callback) => {
            if (!value) return callback();
            try {
              JSON.parse(value);
              callback();
            } catch (e) {
              callback(new Error('必须是有效的 JSON 格式'));
            }
          }, trigger: 'blur' }
        ],
        expand: [
          { validator: (rule, value, callback) => {
            if (!value) return callback();
            try {
              JSON.parse(value);
              callback();
            } catch (e) {
              callback(new Error('必须是有效的 JSON 格式'));
            }
          }, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.pipelineId = this.$route.query.pipelineId || this.$route.params.pipelineId
    if (this.pipelineId) {
      this.loadTaskList()
    } else {
      this.$message.warning('未提供任务流 ID')
    }
  },
  methods: {
    async loadTaskList() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          name: this.filters.name,
          label: this.filters.label
        }
        const res = await searchTasks(this.pipelineId, params)
        if (res && res.resultData) {
          this.taskList = res.resultData || []
          this.totalCount = res.totalRecord || 0
        }
      } catch (error) {
        console.error('加载任务列表失败', error)
        this.$message.error('加载任务列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.loadTaskList()
    },
    handleReset() {
      this.filters = {
        name: '',
        label: ''
      }
      this.handleSearch()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadTaskList()
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadTaskList()
    },
    showAddDialog() {
      // 此处逻辑可根据实际业务跳转到添加任务页面或打开弹窗
      this.$message.info('跳转到添加任务功能待实现')
    },
    handleDetail(row) {
      this.currentTask = row
      this.detailVisible = true
    },
    async handleEdit(row) {
      this.editVisible = true
       this.editForm = row
      // this.loading = true
      // try {
      //   const res = await getTaskDetail(row.id)
      //   console.log(res)
      //   if (res.resultData) {
      //     this.editForm = {
      //       ...res.resultData,
      //       args: typeof res.resultData.args === 'object' ? JSON.stringify(res.resultData.args, null, 2) : res.resultData.args,
      //       expand: typeof res.resultData.expand === 'object' ? JSON.stringify(res.resultData.expand, null, 2) : res.resultData.expand
      //     }
      //   }
      // } catch (error) {
      //   console.error('获取任务详情失败', error)
      // } finally {
      //   this.loading = false
      // }
    },
    async handleEditSubmit() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            const res = await updateTask(this.editForm)
            if (res) {
              this.$message.success('修改成功')
              this.editVisible = false
              this.loadTaskList()
            }
          } catch (error) {
            console.error('修改任务失败', error)
            this.$message.error('修改任务失败')
          }
        }
      })
    },
    handleDelete(row) {
      this.$confirm(`确定要删除任务 "${row.name}" 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteTask(row.id)
          if (res) {
            this.$message.success('删除成功')
            this.loadTaskList()
          }
        } catch (error) {
          console.error('删除任务失败', error)
          this.$message.error('删除任务失败')
        }
      }).catch(() => {})
    },
    handleMonitor(row) {
      this.$message.info(`查看任务 "${row.name}" 的监控数据`)
    },
    formatTableValue(row, column, cellValue) {
      if (cellValue === '' || cellValue === null || cellValue === undefined) {
        return '--'
      }
      return cellValue
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
    formatJson(str) {
      if (!str) return '-'
      try {
        const obj = typeof str === 'string' ? JSON.parse(str) : str
        return JSON.stringify(obj, null, 2)
      } catch (e) {
        return str
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.task-container {
  min-height: calc(100vh - 60px);
}

.task-content {
  max-width: 100%;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.back-btn {
  background-color: white;
  border: 1px solid #e5e7eb;
  color: #374151;
  &:hover {
    background-color: #f9fafb;
    border-color: #d1d5db;
  }
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.section {
  margin-bottom: 24px;
}

.search-card, .table-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.card-hover {
  transition: all 0.3s ease;
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.search-form {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  align-items: flex-end;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.button-item {
  flex-direction: row;
  justify-content: flex-start;
  gap: 12px;
}

.table-header-row {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.pagination-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

.detail-content {
  padding: 10px;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
  gap: 20px;
}

.detail-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.full {
    flex: none;
    width: 100%;
  }

  .label {
    font-size: 13px;
    color: #909399;
    font-weight: 500;
  }

  .value {
    font-size: 14px;
    color: #303133;
  }

  .json-value {
      background-color: #f8f9fa;
      padding: 12px;
      border-radius: 4px;
      font-family: monospace;
      font-size: 12px;
      max-height: 200px;
      overflow-y: auto;
      border: 1px solid #ebeef5;
      margin: 4px 0;
    }
  }

.edit-task-dialog {
  ::v-deep .el-dialog__body {
    padding: 20px 40px;
    max-height: 70vh;
    overflow-y: auto;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
    margin-top: 4px;
  }

  .json-form-item {
    position: relative;
    .json-editor-header {
      position: absolute;
      right: 0;
      top: -32px;
      z-index: 1;
    }
  }
}
</style>
