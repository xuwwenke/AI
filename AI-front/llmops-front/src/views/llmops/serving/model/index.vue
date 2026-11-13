<template>
  <div class="model-container">
    <div class="model-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">模型管理</h1>
          <p class="page-subtitle">管理和维护模型资源</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="add-btn" size="small" @click="showUploadDialog">
            <i class="el-icon-plus"></i> 上传模型
          </el-button>
        </div>
      </div>

      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">总模型数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
          <div class="stat-trend up">
            <i class="el-icon-top"></i> 5 本周新增
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">在线模型</div>
          <div class="stat-value">{{ stats.onlineCount }}</div>
          <div class="stat-trend">
            <i class="el-icon-check"></i> 全部正常
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">模型类型</div>
          <div class="stat-value">{{ stats.typeCount }} 种</div>
          <div class="stat-trend">
            涵盖多领域
          </div>
        </div>
      </div>

      <ai-robot-section title="模型管理助手" subtitle="通过问答形式完成模型管理操作"
        :welcome-message="{ role: 'assistant', content: '您好！我是模型管理助手，可以帮助您：', features: ['上传和管理模型', '查询和浏览模型', '查看和发布模型', '监控模型状态'], question: '请问有什么可以帮助您的？' }"
        :quick-questions="['查看当前的模型列表', '注册最新训练版本yolov8模型', '发布模型【id或名称】', '删除模型【id或名称】']"
        placeholder="请输入您的问题，例如：帮我上传一个文本分类模型" storage-key="llmops_serving_model_chat_v1" />

      <div class="search-section">
        <div class="section-title">模型查询</div>
        <el-form :model="searchForm" :inline="true" class="search-form" label-position="top">
          <!-- <el-form-item label="项目组">
            <el-select v-model="searchForm.projectId" placeholder="全部" clearable style="width: 280px;"
              @change="handleSearch">
              <el-option label="AI Lab" value="AI Lab"></el-option>
              <el-option label="Data Science" value="Data Science"></el-option>
              <el-option label="Research" value="Research"></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="名称">
            <el-input v-model="searchForm.name" placeholder="模型名称" clearable style="width: 280px;"
              @clear="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="版本">
            <el-input v-model="searchForm.version" placeholder="版本号" clearable style="width: 280px;"
              @clear="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="训练框架">
            <el-select v-model="searchForm.framework" placeholder="全部" clearable style="width: 280px;"
              @change="handleSearch">
              <el-option v-for="item in trainingFrameworkOptions" :key="item.value" :label="item.label"
                :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <!-- <el-form-item label="数据来源">
            <el-select v-model="searchForm.source" placeholder="全部" clearable style="width: 280px;"
              @change="handleSearch">
              <el-option label="本地" value="本地"></el-option>
              <el-option label="ModelScope" value="ModelScope"></el-option>
              <el-option label="Hugging Face" value="Hugging Face"></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="任务编排ID">
            <el-input v-model="searchForm.orchestrationId" placeholder="编排ID" clearable style="width: 280px;"
              @clear="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="模型文件">
            <el-input v-model="searchForm.path" placeholder="文件路径" clearable style="width: 280px;"
              @clear="handleSearch"></el-input>
          </el-form-item>
        </el-form>
        <div class="search-actions">
          <el-button type="primary" size="small" @click="handleSearch">查询</el-button>
          <el-button size="small" @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="table-section">
        <div class="table-header">
          <div class="header-left-group">
            <div class="section-title" style='margin: 0'>模型列表</div>
            <el-radio-group v-model="searchForm.ifPublic" size="small" @change="handleSearch" class="public-switch">
              <el-radio-button :label="true">公有</el-radio-button>
              <el-radio-button :label="false">私有</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <el-table :data="modelList" border style="width: 100%;" class="model-table" v-loading="loading">
          <!-- <el-table-column prop="projectId" label="项目组" width="120"></el-table-column> -->
          <el-table-column prop="name" label="名称" min-width="150"></el-table-column>
          <el-table-column prop="version" label="版本" min-width="150"></el-table-column>
          <el-table-column prop="describe" label="描述" min-width="150"></el-table-column>
          <el-table-column prop="path" label="模型文件" min-width="200"></el-table-column>
          <el-table-column prop="framework" label="训练框架" min-width="120"></el-table-column>
          <el-table-column prop="metrics" label="指标" min-width="150"></el-table-column>
          <el-table-column prop="apiType" label="推理框架" min-width="120"></el-table-column>
          <el-table-column label="发布" min-width="150">
            <template slot-scope="scope">
              <el-button size="mini" @click="handleDownload(scope.row)">下载</el-button>
              <el-button type="primary" size="mini" @click="handleDeploy(scope.row)">发布</el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">

              <el-button type="text" size="mini" @click="handleDetail(scope.row)">详情</el-button>
              <el-button type="text" size="mini" @click="showEditDialog(scope.row)">修改</el-button>
              <el-button type="text" size="mini" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-section">
          <div class="pagination-left">
            显示 {{ totalCount ? (currentPage - 1) * pageSize + 1 : 0 }} - {{ Math.min(currentPage * pageSize, totalCount)
            }} 条，共
            {{
              totalCount
            }} 条
          </div>
          <div class="pagination-right">
            <el-select v-model="pageSize" size="small" style="width: 100px; margin-right: 10px;"
              @change="handlePageSizeChange">
              <el-option :label="size + '条/页'" :value="size" v-for="size in [10, 20, 50, 100]" :key="size"></el-option>
            </el-select>
            <el-pagination :current-page="currentPage" :page-size="pageSize" layout="prev, pager, next, jumper"
              :total="totalCount" @current-change="handlePageChange" background></el-pagination>
          </div>
        </div>
      </div>
    </div>

    <!-- 上传模型弹框 -->
    <el-dialog title="上传模型" :visible.sync="uploadDialogVisible" width="700px" :close-on-click-modal="false"
      :before-close="handleUploadDialogClose" class="upload-model-dialog">
      <el-form :model="uploadForm" :rules="rules" ref="uploadForm" label-width="100px">
        <el-row :gutter="20">
          <!-- <el-col :span="12">
            <el-form-item label="项目组">
              <el-select v-model="uploadForm.project" placeholder="请选择" style="width: 100%;">
                <el-option label="AI Lab" value="AI Lab"></el-option>
                <el-option label="Data Science" value="Data Science"></el-option>
                <el-option label="Research" value="Research"></el-option>
              </el-select>
            </el-form-item>
          </el-col> -->
          <el-col :span="24">
            <el-form-item label="名称" prop="name">
              <el-input v-model="uploadForm.name" placeholder="模型名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本" prop="version">
              <el-input v-model="uploadForm.version" placeholder="版本号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="训练框架" prop="framework">
              <el-select v-model="uploadForm.framework" placeholder="请选择" style="width: 100%;">
                <el-option v-for="item in trainingFrameworkOptions" :key="item.value" :label="item.label"
                  :value="item.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="推理框架" prop="apiType">
              <el-select v-model="uploadForm.apiType" placeholder="请选择" style="width: 100%;">
                <el-option v-for="item in inferenceFrameworkOptions" :key="item.value" :label="item.label"
                  :value="item.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指标">
              <el-input v-model="uploadForm.metrics" placeholder="例如：Accuracy: 92%"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="运行ID">
              <el-input v-model="uploadForm.runId" placeholder="pipeline 训练的 run id"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运行时间">
              <el-date-picker v-model="uploadForm.runTime" type="datetime" placeholder="模型的保存时间"
                style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="MD5">
              <el-input v-model="uploadForm.md5" placeholder="请输入md5"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务流ID">
              <el-input v-model="uploadForm.pipelineId" placeholder="任务流的id"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="描述" prop="describe">
              <el-input v-model="uploadForm.describe" type="textarea" :rows="3" placeholder="模型描述"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="模型文件" prop="path">
              <div class="upload-area" :class="{ 'is-uploading': fileUpload.uploading }" @click="!fileUpload.uploading && triggerFileInput('uploadFileInput')">
                <i class="el-icon-upload upload-icon"></i>
                <div class="upload-text">拖拽文件到此处或点击上传</div>
                <div class="upload-hint">支持常见模型格式</div>
                <el-button type="primary" size="small" style="margin-top: 12px;" :disabled="fileUpload.uploading">选择文件</el-button>
                <input ref="uploadFileInput" type="file" style="display:none" @change="handleFileSelected($event, 'upload')" />
              </div>
              <div v-if="fileUpload.uploading || fileUpload.percent > 0" style="margin-top: 10px;">
                <el-progress :percentage="fileUpload.percent" :status="fileUpload.status"></el-progress>
                <div style="font-size: 12px; color: #606266; margin-top: 4px;">
                  {{ fileUpload.statusText }}
                  <el-button v-if="fileUpload.uploading" type="text" size="mini" style="color: #f56c6c; margin-left: 8px;" @click="abortModelUpload">取消上传</el-button>
                </div>
              </div>
              <div v-if="uploadForm.path" style="font-size: 12px; color: #67c23a; margin-top: 6px;">
                <i class="el-icon-check"></i> 已上传：{{ uploadForm.path }}
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelUploadDialog">取消</el-button>
        <el-button type="primary" @click="handleUploadSubmit" :disabled="fileUpload.uploading">上传</el-button>
      </span>
    </el-dialog>

    <!-- 修改模型弹框 -->
    <el-dialog title="修改模型" :visible.sync="editDialogVisible" width="700px" :close-on-click-modal="false"
      :before-close="handleEditDialogClose" class="edit-model-dialog">
      <div class="dialog-content">
        <el-form :model="editForm" :rules="rules" ref="editForm" label-width="100px">
          <el-row :gutter="20">
            <!-- <el-col :span="12">
              <el-form-item label="项目组">
                <el-select v-model="editForm.project" placeholder="请选择" style="width: 100%;">
                  <el-option label="AI Lab" value="AI Lab"></el-option>
                  <el-option label="Data Science" value="Data Science"></el-option>
                  <el-option label="Research" value="Research"></el-option>
                </el-select>
              </el-form-item>
            </el-col> -->
            <el-col :span="24">
              <el-form-item label="名称" prop="name">
                <el-input v-model="editForm.name" placeholder="模型名称"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="版本" prop="version">
                <el-input v-model="editForm.version" placeholder="模型版本"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="训练框架" prop="framework">
                <el-select v-model="editForm.framework" placeholder="请选择" style="width: 100%;">
                  <el-option v-for="item in trainingFrameworkOptions" :key="item.value" :label="item.label"
                    :value="item.value"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="描述" prop="describe">
                <el-input v-model="editForm.describe" type="textarea" :rows="3" placeholder="模型描述"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="模型文件" prop="path">
                <div class="upload-area" :class="{ 'is-uploading': fileUpload.uploading }" @click="!fileUpload.uploading && triggerFileInput('editFileInput')">
                  <i class="el-icon-upload upload-icon"></i>
                  <div class="upload-text">拖拽文件到此处或点击上传</div>
                  <div class="upload-hint">支持常见模型格式</div>
                  <el-button type="primary" size="small" style="margin-top: 12px;" :disabled="fileUpload.uploading">选择文件</el-button>
                  <input ref="editFileInput" type="file" style="display:none" @change="handleFileSelected($event, 'edit')" />
                </div>
                <div v-if="fileUpload.uploading || fileUpload.percent > 0" style="margin-top: 10px;">
                  <el-progress :percentage="fileUpload.percent" :status="fileUpload.status"></el-progress>
                  <div style="font-size: 12px; color: #606266; margin-top: 4px;">
                    {{ fileUpload.statusText }}
                    <el-button v-if="fileUpload.uploading" type="text" size="mini" style="color: #f56c6c; margin-left: 8px;" @click="abortModelUpload">取消上传</el-button>
                  </div>
                </div>
                <div v-if="editForm.path" style="font-size: 12px; color: #67c23a; margin-top: 6px;">
                  <i class="el-icon-check"></i> 已上传：{{ editForm.path }}
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="运行ID">
                <el-input v-model="editForm.runId" placeholder="pipeline 训练的 run id"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="运行时间">
                <el-date-picker v-model="editForm.runTime" type="datetime" placeholder="模型的保存时间"
                  style="width: 100%;"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="指标">
                <el-input v-model="editForm.metrics" type="textarea" :rows="4"
                  placeholder='例如: {"mAP50": 0.666}'></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="MD5">
                <el-input v-model="editForm.md5" placeholder="请选择md5"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="推理框架" prop="apiType">
                <el-select v-model="editForm.apiType" placeholder="请选择" style="width: 100%;">
                  <el-option v-for="item in inferenceFrameworkOptions" :key="item.value" :label="item.label"
                    :value="item.value"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="任务流ID">
                <el-input v-model="editForm.pipelineId" placeholder="任务流的id, 0表示非任务流产生模型"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelEditDialog">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :disabled="fileUpload.uploading">确定</el-button>
      </span>
    </el-dialog>

    <!-- 模型详情弹框 -->
    <el-dialog title="模型详情" :visible.sync="detailDialogVisible" width="600px" :close-on-click-modal="false"
      class="model-detail-dialog">
      <div class="detail-content">
        <!-- <div class="detail-item">
          <div class="detail-label">项目组</div>
          <div class="detail-value">{{ detailForm.project || '-' }}</div>
        </div> -->
        <div class="detail-item">
          <div class="detail-label">名称</div>
          <div class="detail-value">{{ detailForm.name || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">版本</div>
          <div class="detail-value">{{ detailForm.version || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">描述</div>
          <div class="detail-value">{{ detailForm.describe || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">模型文件</div>
          <div class="detail-value">{{ detailForm.path || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">训练框架</div>
          <div class="detail-value">{{ detailForm.framework || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">运行ID</div>
          <div class="detail-value">{{ detailForm.runId || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">运行时间</div>
          <div class="detail-value">{{ detailForm.runTime || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">指标</div>
          <div class="detail-value metrics-value">
            <pre>{{ formatMetrics(detailForm.metrics) }}</pre>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-label">md5</div>
          <div class="detail-value">{{ detailForm.md5 || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">推理框架</div>
          <div class="detail-value">{{ detailForm.apiType || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">任务流ID</div>
          <div class="detail-value">{{ detailForm.pipelineId }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">id</div>
          <div class="detail-value">{{ detailForm.id || '-' }}</div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getModelList,
  getModelDetail,
  createModel,
  updateModel,
  deleteModel,
  initMultipartUpload,
  getUploadPresignedUrl,
  completeMultipartUpload,
  uploadPartToS3,
  notifyPartComplete,
  getModelDownloadUrl,
  getModelStats,
  deployModel
} from '@/api/llmops/model'
import { mapState } from 'vuex'
import AiRobotSection from '@/components/llmops/AiRobotSection'

export default {
  name: 'ModelManagement',
  components: { AiRobotSection },
  computed: {
    ...mapState('project', ['currentProject'])
  },
  data() {
    return {
      loading: false,
      modelList: [],
      totalCount: 0,
      currentPage: 1,
      pageSize: 10,
      uploadDialogVisible: false,
      uploadForm: {
        project: '',
        name: '',
        version: '',
        framework: '',
        apiType: '',
        metrics: '',
        describe: '',
        path: '',
        runId: '',
        runTime: '',
        md5: '',
        pipelineId: ''
      },
      editDialogVisible: false,
      editingModelId: null,
      editForm: {
        project: '',
        name: '',
        version: '',
        framework: '',
        describe: '',
        path: '',
        runId: '',
        runTime: '',
        metrics: '',
        md5: '',
        apiType: '',
        pipelineId: ''
      },
      rules: {
        name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
        version: [{ required: true, message: '请输入模型版本', trigger: 'blur' }],
        path: [{ required: true, message: '请导入模型文件', trigger: 'change' }],
        framework: [{ required: true, message: '请选择训练框架', trigger: 'change' }],
        apiType: [{ required: true, message: '请选择推理框架', trigger: 'change' }],
        describe: [{ required: true, message: '请输入模型描述', trigger: 'blur' }]
      },
      detailDialogVisible: false,
      detailForm: {
        project: '',
        name: '',
        version: '',
        framework: '',
        describe: '',
        path: '',
        runId: '',
        runTime: '',
        metrics: '',
        md5: '',
        apiType: '',
        pipelineId: '',
        id: ''
      },
      stats: {
        totalCount: 0,
        typeCount: 0,
        onlineCount: 0
      },
      searchForm: {
        project: '',
        name: '',
        version: '',
        framework: '',
        source: '',
        orchestrationId: '',
        path: '',
        ifPublic: true
      },
      fileUpload: {
        uploading: false,
        percent: 0,
        status: null,
        statusText: '',
        objectKey: '',
        uploadId: '',
        aborted: false
      },
      trainingFrameworkOptions: [
        { label: 'sklearn', value: 'sklearn' },
        { label: 'xgb', value: 'xgb' },
        { label: 'tf', value: 'tf' },
        { label: 'pytorch', value: 'pytorch' },
        { label: 'onnx', value: 'onnx' },
        { label: 'tensorrt', value: 'tensorrt' },
        { label: 'aihub', value: 'aihub' }
      ],
      inferenceFrameworkOptions: [
        { label: 'serving', value: 'serving' },
        { label: 'ml-server', value: 'ml-server' },
        { label: 'tfserving', value: 'tfserving' },
        { label: 'torch-server', value: 'torch-server' },
        { label: 'onnxruntime', value: 'onnxruntime' },
        { label: 'triton-server', value: 'triton-server' }
      ]
    }
  },
  mounted() {
    this.loadModelList()
    this.loadModelStats()
  },
  methods: {
    async loadModelStats() {
      try {
        const res = await getModelStats()
        if (res) {
          this.stats = {
            totalCount: res.totalCount || 0,
            typeCount: res.typeCount || 0,
            onlineCount: res.onlineCount || 0
          }
        }
      } catch (error) {
        console.error('加载模型统计失败', error)
      }
    },
    async loadModelList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }

        if (this.searchForm.projectId) params.projectId = this.searchForm.projectId
        if (this.searchForm.name) params.name = this.searchForm.name
        if (this.searchForm.version) params.version = this.searchForm.version
        if (this.searchForm.framework) params.framework = this.searchForm.framework
        if (this.searchForm.source) params.source = this.searchForm.source
        if (this.searchForm.orchestrationId) params.orchestrationId = this.searchForm.orchestrationId
        if (this.searchForm.path) params.path = this.searchForm.path
        if (this.searchForm.ifPublic !== undefined) params.ifPublic = this.searchForm.ifPublic

        const res = await getModelList(params)
        if (res && res.resultData) {
          this.modelList = res.resultData || []
          this.totalCount = res.totalRecord || 0
        }

      } catch (error) {
        console.error('加载模型列表失败', error)
        this.$message.error('加载模型列表失败')
        this.modelList = []
        this.totalCount = 0
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.loadModelList()
    },
    handleReset() {
      const currentPublic = this.searchForm.ifPublic
      this.searchForm = {
        project: '',
        name: '',
        version: '',
        framework: '',
        source: '',
        orchestrationId: '',
        path: '',
        ifPublic: currentPublic
      }
      this.currentPage = 1
      this.loadModelList()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadModelList()
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadModelList()
    },
    async handleDeploy(row) {
      try {
        await this.$confirm(`确定发布模型「${row.name}」？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deployModel(row.id)
        this.$message.success('发布成功')
        this.loadModelList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('发布模型失败', error)
          this.$message.error('发布失败')
        }
      }
    },
    async handleDownload(row) {
      try {
        let url = ''
        // 如果 path 本身就是 http/https 开头的完整 URL，直接使用
        if (row.path && (row.path.startsWith('http://') || row.path.startsWith('https://'))) {
          url = row.path
        } else {
          // 否则走接口获取预签名下载地址
          const res = await getModelDownloadUrl({ objectKey: row.path })
          url = (res ? res.url : '')
        }

        if (url) {
          // 使用 a 标签触发下载
          const link = document.createElement('a')
          link.href = url
          link.download = row.name || 'model'
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
        } else {
          this.$message.error('获取下载地址失败')
        }
      } catch (error) {
        console.error('获取模型下载地址失败', error)
        this.$message.error('获取模型下载地址失败')
      }
    },
    async handleDetail(row) {
      this.detailDialogVisible = true
      try {
        const res = await getModelDetail(row.id)
        if (res) {

          this.detailForm = { ...res }
        }
      } catch (error) {
        console.error('获取模型详情失败', error)
        this.$message.error('获取模型详情失败')
      }
    },
    formatMetrics(metrics) {
      if (!metrics) return '-'
      try {
        if (typeof metrics === 'string') {
          const obj = JSON.parse(metrics)
          return JSON.stringify(obj, null, 2)
        }
        return JSON.stringify(metrics, null, 2)
      } catch (e) {
        return metrics
      }
    },
    async showEditDialog(row) {
      this.editingModelId = row.id
      this.editDialogVisible = true

      let detail = row
      try {
        const res = await getModelDetail(row.id)
        if (res) {
          detail = res
        }
      } catch (error) {
        console.error('获取模型详情失败，使用列表数据', error)
      }

      this.editForm = { ...detail }
    },
    editNextStep() {
      // 移除分步逻辑，此方法不再需要
    },
    editPrevStep() {
      // 移除分步逻辑，此方法不再需要
    },
    buildModelPayload(editForm) {
      return {
        project: this.currentProject ? this.currentProject.id : 0,
        describe: editForm.describe || '',
        metrics: editForm.metrics || '',
        api_type: editForm.apiType || '',
        name: editForm.name || '',
        version: editForm.version || '',
        path: editForm.path || '',
        framework: editForm.framework || '',
        run_id: editForm.runId || '',
        run_time: editForm.runTime || '',
        md5: editForm.md5 || '',
        pipeline_id: editForm.pipelineId || ''
      }
    },

    async handleEditSubmit() {
      try {
        const valid = await this.$refs.editForm.validate().catch(() => false)
        if (!valid) {
          // this.$message.error('表单校验失败，请检查输入')
          return
        }

        const submitData = {
          id: this.editingModelId,
          ...this.buildModelPayload(this.editForm)
        }
        const res = await updateModel(submitData)
        if (res && (res.code === 200 || res.status === 0)) {
          this.$message.success('模型修改成功！')
          this.editDialogVisible = false
          await this.loadModelList()
        } else {
          this.$message.error(res.message || '修改失败')
        }
      } catch (error) {
        console.error('修改模型失败', error)
        this.$message.error('修改模型失败')
      }
    },
    showUploadDialog() {
      this.uploadDialogVisible = true
    },
    handleUploadDialogClose(done) {
      if (this.fileUpload.uploading) {
        this.$confirm('文件正在上传中，关闭将取消上传，确定关闭？', '提示', {
          confirmButtonText: '确定关闭',
          cancelButtonText: '继续上传',
          type: 'warning'
        }).then(() => {
          this.fileUpload.aborted = true
          this.resetUploadForm()
          if (typeof done === 'function') done()
          else this.uploadDialogVisible = false
        }).catch(() => {})
      } else {
        this.resetUploadForm()
        if (typeof done === 'function') done()
        else this.uploadDialogVisible = false
      }
    },
    cancelUploadDialog() {
      this.handleUploadDialogClose(null)
    },
    handleEditDialogClose(done) {
      if (this.fileUpload.uploading) {
        this.$confirm('文件正在上传中，关闭将取消上传，确定关闭？', '提示', {
          confirmButtonText: '确定关闭',
          cancelButtonText: '继续上传',
          type: 'warning'
        }).then(() => {
          this.fileUpload.aborted = true
          this.resetUploadForm()
          if (typeof done === 'function') done()
          else this.editDialogVisible = false
        }).catch(() => {})
      } else {
        this.resetUploadForm()
        if (typeof done === 'function') done()
        else this.editDialogVisible = false
      }
    },
    cancelEditDialog() {
      this.handleEditDialogClose(null)
    },
    abortModelUpload() {
      this.fileUpload.aborted = true
      this.fileUpload.uploading = false
      this.fileUpload.statusText = '已取消'
      this.fileUpload.status = 'exception'
    },
    triggerFileInput(refName) {
      this.$refs[refName].value = ''
      this.$refs[refName].click()
    },
    async handleFileSelected(e, target) {
      const file = e.target.files && e.target.files[0]
      if (!file) return
      
      const projectName = this.currentProject ? String(this.currentProject.name) : 'default'
      try {
        const objectKey = await this.doMultipartUpload(file, projectName)
        if (target === 'upload') {
          this.uploadForm.path = objectKey
        } else if (target === 'edit') {
          this.editForm.path = objectKey
        }
      } catch (error) {
        this.fileUpload.status = 'exception'
        this.fileUpload.statusText = error.message || '上传失败'
        this.$message.error(error.message || '上传模型文件失败')
      }
    },
    async doMultipartUpload(file, projectName) {
      const PART_SIZE = 5 * 1024 * 1024
      const CONCURRENCY = 3
      const fileName = file.name.replace(/\s+/g, '-').replace(/[^a-zA-Z0-9.\-_]/g, '') || 'model.bin'

      const fu = this.fileUpload
      fu.uploading = true
      fu.aborted = false
      fu.percent = 0
      fu.status = null
      fu.statusText = '初始化上传...'
      fu.objectKey = ''
      fu.uploadId = ''

      // 1. 初始化
      const initRes = await initMultipartUpload({ fileName }, { 'X-Project-Name': projectName })
      if (!initRes || !initRes.uploadId) {
        throw new Error('初始化分片上传失败')
      }

      const { objectKey, uploadId } = initRes
      fu.objectKey = objectKey
      fu.uploadId = uploadId

      const totalParts = Math.ceil(file.size / PART_SIZE)
      const uploadedParts = []
      let completed = 0
      let nextPart = 1

      fu.statusText = `上传中 0 / ${totalParts}`

      const uploadOne = async () => {
        while (!fu.aborted) {
          const n = nextPart++
          if (n > totalParts) return

          // 2. 获取预签名 URL
          const presignedRes = await getUploadPresignedUrl({
            objectKey: fu.objectKey,
            uploadId: fu.uploadId,
            partNumber: n
          })

          if (!presignedRes || !presignedRes.url) {
            throw new Error(`获取预签名 URL 失败，分片 ${n}`)
          }

          const { url } = presignedRes
          const start = (n - 1) * PART_SIZE
          const blob = file.slice(start, Math.min(start + PART_SIZE, file.size))

          // 3. 上传到 S3
          const putRes = await uploadPartToS3(url, blob)
          // axios 返回的是完整的 response 对象 { data, headers, ... }
          const headers = putRes.headers || {}
          const etag = (headers.get ? (headers.get('etag') || headers.get('ETag')) : (headers['etag'] || headers['ETag']) || '').replace(/"/g, '')

          if (!etag) throw new Error(`分片 ${n} 未返回 ETag，请检查 MinIO CORS 配置`)

          await notifyPartComplete({
            uploadId: fu.uploadId,
            partNumber: n,
            etag,
            partSize: blob.size
          })

          uploadedParts.push({ partNumber: n, etag })
          completed++
          fu.percent = Math.round((completed / totalParts) * 100)
          fu.statusText = `上传中 ${completed} / ${totalParts}`
        }
      }

      const workers = []
      for (let i = 0; i < CONCURRENCY; i++) workers.push(uploadOne())
      await Promise.all(workers)

      if (fu.aborted) throw new Error('上传已取消')

      // 4. 合并分片
      fu.statusText = '合并分片...'
      uploadedParts.sort((a, b) => a.partNumber - b.partNumber)
      const completeRes = await completeMultipartUpload({
        objectKey: fu.objectKey,
        uploadId: fu.uploadId,
        parts: uploadedParts
      })

      if (!completeRes) {
        throw new Error('合并分片失败')
      }

      fu.percent = 100
      fu.status = 'success'
      fu.statusText = '上传完成'
      fu.uploading = false
      // 返回 completeRes 中的 objectKey，如果没有则回退到 fu.objectKey
      return (completeRes.objectKey) || fu.objectKey
    },
    async handleUploadSubmit() {
      if (this.fileUpload.uploading) {
        this.$message.warning('文件正在上传中，请等待完成后再提交')
        return
      }
      const valid = await this.$refs.uploadForm.validate().catch(() => false)
      if (!valid) {
        this.$message.error('表单校验失败，请检查输入')
        return
      }
      try {
        const filePath = this.uploadForm.path
        const submitData = {
          project: this.currentProject ? this.currentProject.id : 0,
          name: this.uploadForm.name,
          version: this.uploadForm.version,
          framework: this.uploadForm.framework,
          describe: this.uploadForm.describe,
          path: filePath,
          metrics: this.uploadForm.metrics,
          api_type: this.uploadForm.apiType,
          run_id: this.uploadForm.runId,
          run_time: this.uploadForm.runTime,
          md5: this.uploadForm.md5,
          pipeline_id: this.uploadForm.pipelineId
        }
        const res = await createModel(submitData)
        if (res && (res.code === 200 || res.status === 0)) {
          this.$message.success('模型上传成功！')
          this.uploadDialogVisible = false
          this.resetUploadForm()
          await this.loadModelList()
        } else {
          this.$message.error(res.message || '上传失败')
        }
      } catch (error) {
        this.fileUpload.uploading = false
        this.fileUpload.status = 'exception'
        this.fileUpload.statusText = error.message || '上传失败'
        this.$message.error(error.message || '上传模型失败')
      }
    },
    resetUploadForm() {
      this._pendingUploadFile = null
      this.uploadForm = {
        project: '',
        name: '',
        version: '',
        framework: '',
        apiType: '',
        metrics: '',
        describe: '',
        path: '',
        runId: '',
        runTime: '',
        md5: '',
        pipelineId: ''
      }
      this.fileUpload = {
        uploading: false,
        percent: 0,
        status: null,
        statusText: '',
        objectKey: '',
        uploadId: '',
        aborted: false
      }
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除模型"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteModel(row.id)
          if (res.status === 0) {
            this.$message.success('删除成功')
            await this.loadModelList()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        } catch (error) {
          console.error('删除模型失败', error)
          this.$message.error('删除模型失败')
        }
      }).catch(() => { })
    }
  }
}
</script>

<style lang="scss" scoped>
.model-container {
  min-height: calc(100vh - 60px);
}

.model-content {
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

::v-deep .el-button--primary:not(.is-text) {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;

  &:hover,
  &:focus {
    opacity: 0.9;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
  }
}

/* 统计卡片 */
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

/* 搜索区域 */
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

// .search-form {
// margin-bottom: 12px;
// }

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
  margin-top: 8px;
}

/* 表格区域 */
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

.header-left-group {
  display: flex;
  align-items: center;
  gap: 20px;
}

.public-switch {
  display: flex;
}

.public-switch ::v-deep .el-radio-button__inner {
  padding: 8px 20px;
  font-size: 14px;
}

.model-table {
  font-size: 13px;
}

/* 分页区域 */
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.pagination-left {
  font-size: 13px;
  color: #6b7280;
}

.pagination-right {
  display: flex;
  align-items: center;
}

/* 上传模型弹框 */
.upload-model-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px 20px 15px;
    border-bottom: 1px solid #e5e7eb;
  }

  ::v-deep .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }

  ::v-deep .el-dialog__body {
    padding: 20px;
  }

  ::v-deep .el-dialog__footer {
    padding: 15px 20px;
    border-top: 1px solid #e5e7eb;
  }

  .upload-area {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    padding: 32px 24px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    width: 100%;
    box-sizing: border-box;

    &:hover {
      border-color: #409eff;
      background-color: #f5f7fa;
    }

    &.is-uploading {
      cursor: not-allowed;
      opacity: 0.6;

      &:hover {
        border-color: #d9d9d9;
        background-color: transparent;
      }
    }
  }

  .upload-icon {
    font-size: 48px;
    color: #c0c4cc;
    margin-bottom: 12px;
    display: block;
  }

  .upload-text {
    font-size: 14px;
    color: #606266;
    margin-bottom: 4px;
  }

  .upload-hint {
    font-size: 12px;
    color: #909399;
  }
}

/* 修改模型弹框 */
.edit-model-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px 20px 15px;
    border-bottom: 1px solid #e5e7eb;
  }

  ::v-deep .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }

  ::v-deep .el-dialog__body {
    padding: 20px;
  }

  .dialog-content {
    .step-title {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 16px;
      padding-top: 20px;
    }

    .dialog-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid #e5e7eb;
    }

    .footer-left,
    .footer-right {
      display: flex;
      gap: 8px;
    }
  }

  .upload-area {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    padding: 32px 24px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    width: 100%;
    box-sizing: border-box;

    &:hover {
      border-color: #409eff;
      background-color: #f5f7fa;
    }

    &.is-uploading {
      cursor: not-allowed;
      opacity: 0.6;

      &:hover {
        border-color: #d9d9d9;
        background-color: transparent;
      }
    }
  }

  .upload-icon {
    font-size: 48px;
    color: #c0c4cc;
    margin-bottom: 12px;
    display: block;
  }

  .upload-text {
    font-size: 14px;
    color: #606266;
    margin-bottom: 4px;
  }

  .upload-hint {
    font-size: 12px;
    color: #909399;
  }
}

/* 模型详情弹框 */
.model-detail-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px 20px 15px;
    border-bottom: 1px solid #e5e7eb;
  }

  ::v-deep .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }

  ::v-deep .el-dialog__body {
    padding: 20px 30px;
  }

  .detail-content {
    .detail-item {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      .detail-label {
        font-size: 13px;
        color: #6b7280;
        margin-bottom: 6px;
        font-weight: 500;
      }

      .detail-value {
        font-size: 14px;
        color: #1f2937;
        line-height: 1.5;
        word-break: break-all;

        &.metrics-value {
          background-color: #f9fafb;
          padding: 10px;
          border-radius: 4px;
          border: 1px solid #e5e7eb;

          pre {
            margin: 0;
            font-family: monospace;
            white-space: pre-wrap;
          }
        }
      }
    }
  }

  ::v-deep .el-dialog__footer {
    padding: 15px 20px;
    border-top: 1px solid #e5e7eb;
  }
}

/* 响应式布局 */
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