<template>
  <div class="datasets-container">
    <div class="datasets-content">
      <!-- 头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">数据集管理</h1>
          <p class="page-subtitle">管理数据集、版本控制和数据预处理</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="create-btn" @click="showCreateDialog">
            <i class="el-icon-plus"></i> 创建数据集
          </el-button>
        </div>
      </div>
      <!-- AI助手 -->
      <div class="section">
        <ai-assistant-card storage-key="llmops_datasets_chat_v1" assistant-name="数据集助手"
          welcome-message='你好！我是数据集管理AI助手，可以帮你完成数据集相关的操作。请问有什么可以帮助你的？例如："创建一个新的数据集"、"查询数据集"、"分析数据集的计算点"、"删除数据集"'
          :quick-suggestions="['列出数据集列表', '创建网络数据集', '上传本地数据集', '查看数据集【id或名称】详细信息']" />
      </div>
      <!-- 数据集列表 -->
      <!-- 数据集查询 -->
      <div class="section">
        <div class="search-card card-hover">
          <h2 class="section-title">数据集查询</h2>
          <div class="search-form">
            <!-- <div class="form-item"> -->
            <!-- <label class="form-label">类型</label> -->
            <!-- <el-select v-model="filters.type" placeholder="全部类型" size="small" clearable style="width: 100%"> -->
            <!-- <el-option v-for="item in jobTemplateList" :key="item.id" :label="item.name" -->
            <!-- :value="item.id"></el-option> -->

            <!-- </el-select> -->
            <!-- </div> -->
            <div class="form-item">
              <label class="form-label">责任人</label>
              <el-input v-model="filters.owner" placeholder="责任人名称" size="small" clearable></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">名称</label>
              <el-input v-model="filters.name" placeholder="数据集名称" size="small" clearable></el-input>
            </div>
            <div class="form-item">
              <label class="form-label">版本</label>
              <el-input v-model="filters.version" placeholder="版本号" size="small" clearable></el-input>
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
          <div class="list-header">
            <h2 class="section-title">数据集列表</h2>
            <el-radio-group v-model="filters.ifPublic" size="small" @change="handleSearch">
              <el-radio-button :label="true">公有</el-radio-button>
              <el-radio-button :label="false">私有</el-radio-button>
            </el-radio-group>
          </div>
          <el-table :data="datasets" style="width: 100%" stripe border
            :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }">
            <el-table-column label="预览图" width="100">
              <template slot-scope="scope">
                <div v-if="scope.row.icon">
                  <!-- 判断是否为 SVG 标签字符串 -->
                  <div v-if="isSvgString(scope.row.icon)" class="preview-icon-svg" v-html="scope.row.icon"></div>
                  <!-- 否则作为图片路径处理 -->
                  <img v-else class="preview-icon-img" :src="getIconUrl(scope.row.icon)" alt="预览图"
                    @error="handleImageError">
                </div>
                <!-- 没有 icon 时显示默认图标 -->
                <div v-else class="preview-icon" :class="getTypeClass(scope.row.fileType)">
                  <i :class="getTypeIcon(scope.row.fileType)"></i>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" min-width="150"></el-table-column>
            <el-table-column prop="version" label="版本" min-width="100"></el-table-column>
            <el-table-column prop="label" label="中文名称" min-width="150"></el-table-column>
            <el-table-column prop="describe" label="描述" min-width="150" show-overflow-tooltip></el-table-column>
            <el-table-column prop="owner" label="责任人" min-width="100"></el-table-column>
            <el-table-column prop="path" label="容器内路径" min-width="180" show-overflow-tooltip></el-table-column>
            <!-- <el-table-column label="下载链接" min-width="250"> -->
            <!-- <template slot-scope="scope"> -->
            <!-- <div class="download-links"> -->
            <!-- <template v-if="scope.row.downloadUrl"> -->
            <!-- 将字符串按换行分割成数组 -->
            <!-- <a v-for="(link, index) in scope.row.downloadUrl.split('\n')" :key="index" :href="link.trim()" -->
            <!-- target="_blank" class="download-link" :title="link.trim()" v-if="link.trim()"> -->
            <!-- {{ truncateLink(link.trim()) }} -->
            <!-- </a> -->
            <!-- </template> -->
            <!-- <span v-else>-</span> -->
            <!-- </div> -->
            <!-- </template> -->
            <!-- </el-table-column> -->
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleView(scope.row)">详情</el-button>
                <!-- <el-button type="text" size="small" @click="handleDownload(scope.row)">下载</el-button> -->
                <el-button v-if="!filters.ifPublic" type="text" size="small" @click="handleEdit(scope.row)">修改</el-button>
                <!-- <el-button v-if="!filters.ifPublic" type="text" size="small" @click="handleBackup(scope.row)">备份</el-button> -->
                <el-button v-if="!filters.ifPublic" type="text" size="small" @click="handleAnnotation(scope.row)">标注</el-button>
                <el-button v-if="!filters.ifPublic" type="text" size="small" :style="{ color: filters.ifPublic ? '#C0C4CC' : '#f56c6c' }" @click="handleDelete(scope.row)">删除</el-button>

              </template>
            </el-table-column>
          </el-table>
          <!-- 添加分页组件 -->
          <div class="pagination-container">
            <el-pagination @size-change="handlePageSizeChange" @current-change="handlePageChange"
              :current-page="currentPage" :page-sizes="pageSizes" :page-size="pageSize" :total="total"
              layout="total, sizes, prev, pager, next, jumper" background>
            </el-pagination>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建数据集弹框 -->
    <el-dialog title="创建数据集" :visible.sync="createDialogVisible" width="800px" :close-on-click-modal="false"
      class="create-dataset-dialog" :before-close="handleCreateDialogClose" @closed="resetDatasetForm">
      <el-form :model="datasetForm" label-width="100px" class="edit-form">
        <div class="form-row">
          <el-form-item label="数据集名称" class="form-item-half">
            <el-input v-model="datasetForm.name" placeholder="请输入数据集名称"></el-input>
          </el-form-item>
          <el-form-item label="版本" class="form-item-half">
            <el-input v-model="datasetForm.version" placeholder="请输入版本"></el-input>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="中文名称" class="form-item-half">
            <el-input v-model="datasetForm.label" placeholder="请输入中文名称"></el-input>
          </el-form-item>
          <el-form-item label="责任人" class="form-item-half">
            <el-input v-model="datasetForm.owner" placeholder="责任人名称"></el-input>
          </el-form-item>
          <!-- <el-form-item label="数据集类型" class="form-item-half"> -->
          <!-- <el-select v-model="datasetForm.type" placeholder="选择或输入类型" -->
          <!-- filterable allow-create style="width: 100%;"> -->
          <!-- <el-option v-for="item in jobTemplateList" :key="item.id" :label="item.name" -->
          <!-- :value="item.id"></el-option> -->
          <!-- </el-select> -->
          <!-- </el-form-item> -->
        </div>
        <el-form-item label="描述">
          <el-input v-model="datasetForm.describe" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>

        <el-form-item label="上传文件">
          <div class="upload-area" :class="{ 'is-uploading': fileUpload.uploading }"
            @click="!fileUpload.uploading && triggerFileUpload()">
            <i class="el-icon-upload upload-icon"></i>
            <div class="upload-text">拖拽文件到此处或点击上传</div>
            <div class="upload-hint">支持 CSV、JSON、Parquet 等格式</div>
            <el-button type="primary" size="small" style="margin-top: 12px;"
              :disabled="fileUpload.uploading">选择文件</el-button>
          </div>
          <div v-if="fileUpload.uploading || fileUpload.percent > 0" style="margin-top: 10px;">
            <el-progress :percentage="fileUpload.percent" :status="fileUpload.status"></el-progress>
            <div style="font-size: 12px; color: #606266; margin-top: 4px;">
              {{ fileUpload.statusText }}
              <el-button v-if="fileUpload.uploading" type="text" size="mini" style="color: #f56c6c; margin-left: 8px;"
                @click="abortUpload">取消上传</el-button>
            </div>
          </div>
          <div v-if="datasetForm.filePath" style="font-size: 12px; color: #67c23a; margin-top: 6px;">
            <i class="el-icon-check"></i> 已上传：{{ datasetForm.filePath }}
          </div>
          <input ref="fileInput" type="file" style="display: none" @change="handleFileChange" />
        </el-form-item>
        <el-form-item label="上传Logo">
          <div style="display: flex; align-items: center; gap: 12px;">
            <div class="logo-preview" v-if="datasetForm.iconPreview">
              <img :src="datasetForm.iconPreview" alt="Logo" />
            </div>
            <div class="logo-placeholder" v-else>
              <i class="el-icon-picture"></i>
            </div>
            <div>
              <el-button type="primary" size="small" @click="triggerLogoUpload">上传Logo</el-button>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">支持 png, jpg, jpeg, webp, svg 格式，不超过10MB
              </div>
            </div>
          </div>
          <input ref="logoInput" type="file" accept="image/png,image/jpeg,image/jpg,image/webp,image/svg+xml"
            style="display: none" @change="handleLogoChange" />
        </el-form-item>
        <el-form-item label="特征列">
          <el-input v-model="datasetForm.features" type="textarea" :rows="3" placeholder="请输入特征列"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateDataset" :disabled="fileUpload.uploading">创建</el-button>
      </span>
    </el-dialog>

    <!-- 数据集详情弹框 -->
    <el-dialog :visible.sync="detailDialogVisible" width="800px" :close-on-click-modal="false"
      class="dataset-detail-dialog" custom-class="dataset-detail-dialog">
      <div slot="title" class="dialog-title-custom">
        <div class="dataset-detail-header">
          <div class="dataset-detail-title">数据集详情 - {{ currentDataset ? currentDataset.name : '' }}</div>
        </div>
      </div>
      <div class="dataset-detail-content" v-if="currentDataset">
        <div class="detail-main">
          <div class="detail-left">
            <div class="preview-card">
              <div class="preview-icon-container">
                <div v-if="currentDataset.icon">
                  <!-- 判断是否为 SVG 标签字符串 -->
                  <div v-if="isSvgString(currentDataset.icon)" class="preview-icon-large-svg"
                    v-html="currentDataset.icon">
                  </div>
                  <!-- 否则作为图片路径处理 -->
                  <img v-else class="preview-icon-large-img" :src="getIconUrl(currentDataset.icon)" alt="预览图"
                    @error="handleImageError">
                </div>
                <div v-else class="preview-icon-large" :class="getTypeClass(currentDataset.fileType)">
                  <i :class="getTypeIcon(currentDataset.fileType)"></i>
                </div>
              </div>
              <div class="detail-info-item">
                <span class="detail-label-small">存储大小</span>
                <span class="detail-value-large">{{ currentDataset.storageSize || '-' }}</span>
              </div>
              <div class="detail-info-item">
                <span class="detail-label-small">文件类型</span>
                <span class="detail-value-large">{{ currentDataset.fileType || '-' }}</span>
              </div>
              <div class="detail-info-item">
                <span class="detail-label-small">状态</span>
                <span class="detail-value-large status-normal">{{ currentDataset.status || '正常' }}</span>
              </div>
              <div class="detail-info-item">
                <span class="detail-label-small">责任人</span>
                <span class="detail-value-large">{{ currentDataset.owner }}</span>
              </div>
            </div>
          </div>
          <div class="detail-right">
            <div class="detail-grid">
              <div class="detail-field">
                <span class="field-label">名称</span>
                <div class="field-value">{{ currentDataset.name }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">版本</span>
                <div class="field-value">{{ currentDataset.version || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">标签</span>
                <div class="field-value">{{ currentDataset.label || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">来源类型</span>
                <div class="field-value">{{ currentDataset.sourceType || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">数据来源</span>
                <div class="field-value">{{ currentDataset.source || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">行业</span>
                <div class="field-value">{{ currentDataset.industry || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">领域</span>
                <div class="field-value">{{ currentDataset.field || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">数据用途</span>
                <div class="field-value">{{ currentDataset.usage || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">存储类型</span>
                <div class="field-value">{{ currentDataset.storageClass || '-' }}</div>
              </div>
              <div class="detail-field">
                <span class="field-label">分区</span>
                <div class="field-value">{{ currentDataset.split || '-' }}</div>
              </div>
              <!-- <div class="detail-field"> -->
              <!-- <span class="field-label">相关网址</span> -->
              <!-- <div class="field-value">{{ currentDataset.url || '-' }}</div> -->
              <!-- </div> -->
              <!-- <div class="detail-field">
                <span class="field-label">价格</span>
                <div class="field-value">{{ currentDataset.price || '-' }}</div>
              </div> -->
              <div class="detail-field full-width">
                <span class="field-label">描述</span>
                <div class="field-value">{{ currentDataset.describe || '暂无描述' }}</div>
              </div>
              <div class="detail-field full-width">
                <span class="field-label">容器内路径</span>
                <div class="field-value">{{ currentDataset.path || '-' }}</div>
              </div>
              <!-- <div class="detail-field full-width"> -->
              <!-- <span class="field-label">下载地址</span> -->
              <!-- <div class="field-value download-link"> -->
              <!-- 将字符串按换行分割成数组 -->
              <!-- <template v-if="currentDataset.downloadUrl"> -->
              <!-- <a v-for="(link, index) in currentDataset.downloadUrl.split('\n')" :key="index" :href="link.trim()" -->
              <!-- target="_blank" class="download-link" :title="link.trim()" v-if="link.trim()"> -->
              <!-- {{ truncateLink(link.trim()) }} -->
              <!-- </a> -->
              <!-- </template> -->
              <!-- <span v-else>-</span> -->
              <!-- </div> -->
              <!-- </div> -->
              <div class="detail-field full-width">
                <span class="field-label">特征列</span>
                <div class="field-value">{{ currentDataset.features || '{}' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="!filters.ifPublic" type="primary" @click="handleDetailEdit">修改</el-button>
      </span>
    </el-dialog>

    <!-- 修改数据集弹框 -->
    <el-dialog title="修改数据集" :visible.sync="editDialogVisible" width="800px" :close-on-click-modal="false"
      class="edit-dataset-dialog" custom-class="edit-dataset-dialog">
      <el-form :model="editForm" label-width="100px" class="edit-form">
        <div class="form-row">
          <el-form-item label="名称" class="form-item-half">
            <el-input v-model="editForm.name" placeholder="请输入名称"></el-input>
          </el-form-item>
          <el-form-item label="版本" class="form-item-half">
            <el-input v-model="editForm.version" placeholder="请输入版本"></el-input>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="中文名称" class="form-item-half">
            <el-input v-model="editForm.tags" placeholder="请输入中文名称"></el-input>
          </el-form-item>
          <el-form-item label="责任人" class="form-item-half">
            <el-input v-model="editForm.owner" placeholder="责任人名称" clearable></el-input>
          </el-form-item>
        </div>
        <el-form-item label="描述">
          <el-input v-model="editForm.describe" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
        <div class="form-row">
          <el-form-item label="容器内路径" class="form-item-half">
            <el-input v-model="editForm.path" placeholder="请输入容器内路径"></el-input>
          </el-form-item>
        </div>
        <el-form-item label="预览图">
          <div style="display: flex; align-items: center; gap: 12px;">
            <div class="logo-preview" v-if="editForm.iconPreview">
              <img :src="editForm.iconPreview" alt="Logo" />
            </div>
            <div class="logo-placeholder" v-else>
              <i class="el-icon-picture"></i>
            </div>
            <div>
              <el-button type="primary" size="small" @click="triggerEditLogoUpload">更换Logo</el-button>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">支持 png, jpg, jpeg, webp, svg 格式，不超过10MB
              </div>
            </div>
          </div>
          <input ref="editLogoInput" type="file" accept="image/png,image/jpeg,image/jpg,image/webp,image/svg+xml"
            style="display: none" @change="handleEditLogoChange" />
        </el-form-item>
        <el-form-item label="特征列">
          <el-input v-model="editForm.features" type="textarea" :rows="3" placeholder="请输入特征列"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDatasetList, getDatasetDetail, createDataset, updateDataset, deleteDataset, initDatasetMultipartUpload, getDatasetUploadPresignedUrl, completeDatasetMultipartUpload, uploadDatasetPartToS3, notifyDatasetPartComplete, uploadIcon } from '@/api/llmops/dataset'
import AiAssistantCard from '@/components/llmops/AiAssistantCard.vue'

export default {
  name: 'Datasets',
  components: {
    AiAssistantCard
  },
  data() {
    return {
      loading: false,     // 页面加载状态
      jobTemplateList: [], //类型下拉框
      total: 0,           // 总记录数
      currentPage: 1,    // 当前页码
      pageSize: 10,      // 每页显示条数
      pageSizes: [10, 20, 50, 100], // 每页条数选项
      filters: {
        type: '',
        owner: '',
        name: '',
        version: '',
        describe: '',
        ifPublic: true
      },
      datasets: [],
      createDialogVisible: false,
      detailDialogVisible: false,
      editDialogVisible: false,
      currentDataset: null,
      datasetForm: {
        name: '',
        version: '',
        label: '',
        type: '',
        describe: '',
        owner: '',
        features: '{}',
        files: [],
        icon: '',
        iconPreview: '',
        filePath: ''
      },
      editForm: {
        name: '',
        version: '',
        tags: '',
        describe: '',
        website: '',
        path: '',
        downloadUrl: '',
        owner: '',
        features: '{}',
        icon: '',
        iconPreview: ''
      },
      fileUpload: {
        uploading: false,
        aborted: false,
        percent: 0,
        status: null,
        statusText: '',
        objectKey: '',
        uploadId: ''
      }
    }
  },
  computed: {},
  mounted() {
    this.loadDatasets()

  },
  methods: {
    async loadDatasets() {
      try {
        // 将筛选条件作为查询参数传给后端，便于服务端过滤
        const res = await getDatasetList({
          pageNum: this.currentPage || 1,
          pageSize: this.pageSize || 10,
          ...this.filters
        })

        if (res && res.resultData) {
          this.datasets = res.resultData || []
          this.total = res.totalRecord || 0
        } else {
          this.$message.error(res && res.message ? res.message : '加载数据集列表失败')
        }
      } catch (error) {
        console.error('加载数据集列表失败', error)
        this.$message.error('加载数据集列表失败')
      } finally {
      }
    },
    // 判断是否为 SVG 标签字符串
    isSvgString(str) {
      if (typeof str !== 'string') return false;
      const trimmed = str.trim();
      // 方法1：检查是否以 <svg 开头
      if (trimmed.startsWith('<svg')) return true;

      // 方法2：更严格的 SVG 标签正则
      const svgRegex = /^\s*<svg[\s\S]*<\/svg>\s*$/i;
      return svgRegex.test(trimmed);
    },

    // 图片加载失败时的处理
    handleImageError(event) {
      // 可以选择显示默认图标或隐藏
      event.target.style.display = 'none';
      // 或者替换为默认图标
      // event.target.src = '默认图片地址';
    },
    getTypeClass(type) {
      const map = {
        text: 'text-type',
        image: 'image-type',
        tabular: 'tabular-type',
        audio: 'audio-type'
      }
      return map[type] || 'tabular-type'
    },
    getTypeIcon(type) {
      const map = {
        text: 'el-icon-document',
        image: 'el-icon-picture',
        tabular: 'el-icon-s-grid',
        audio: 'el-icon-microphone'
      }
      return map[type] || 'el-icon-s-grid'
    },
    // 获取 Logo 完整 URL
    getIconUrl(icon) {
      if (!icon) return '';
      // 如果是完整的 http 地址，直接返回
      if (icon.startsWith('http://') || icon.startsWith('https://')) return icon;

      let iconPath = icon.replace(/^\/+/, '');
      if (iconPath.startsWith('minio/icon/')) {
        iconPath = iconPath.slice('minio/icon/'.length);
      } else if (iconPath.startsWith('icon/')) {
        iconPath = iconPath.slice('icon/'.length);
      }

      return `${window.location.origin}/minio/icon/${iconPath}`;
    },
    // 触发 Logo 上传
    triggerLogoUpload() {
      this.$refs.logoInput.click();
    },
    // 处理 Logo 上传
    async handleLogoChange(event) {
      const file = event.target.files[0];
      if (!file) return;

      // 增加格式校验
      const validTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp', 'image/svg+xml'];
      if (!validTypes.includes(file.type)) {
        this.$message.error('不支持的图片格式！请上传 png, jpg, jpeg, webp 或 svg 格式的图片');
        event.target.value = '';
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        this.$message.error('图片大小不能超过 10MB');
        event.target.value = '';
        return;
      }

      const formData = new FormData();
      formData.append('file', file);
      const projectName = (this.$store.state.project && this.$store.state.project.currentProject)
        ? String(this.$store.state.project.currentProject.name) : 'default';

      try {
        const res = await uploadIcon(formData, projectName);
        if (res) {
          this.datasetForm.icon = res.objectKey; // 接口返回的路径，如 ai-lab-dev/xxx.png
          this.datasetForm.iconPreview = this.getIconUrl(res.objectKey);
          this.$message.success('Logo 上传成功');
        }
      } catch (error) {
        console.error('Logo 上传失败', error);
        this.$message.error('Logo 上传失败');
      } finally {
        event.target.value = '';
      }
    },
    // 触发编辑 Logo 上传
    triggerEditLogoUpload() {
      this.$refs.editLogoInput.click();
    },
    // 处理编辑 Logo 上传
    async handleEditLogoChange(event) {
      const file = event.target.files[0];
      if (!file) return;

      // 增加格式校验
      const validTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp', 'image/svg+xml'];
      if (!validTypes.includes(file.type)) {
        this.$message.error('不支持的图片格式！请上传 png, jpg, jpeg, webp 或 svg 格式的图片');
        event.target.value = '';
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        this.$message.error('图片大小不能超过 10MB');
        event.target.value = '';
        return;
      }

      const formData = new FormData();
      formData.append('file', file);
      const projectName = (this.$store.state.project && this.$store.state.project.currentProject)
        ? String(this.$store.state.project.currentProject.name) : 'default';

      try {
        const res = await uploadIcon(formData, projectName);
        if (res) {
          this.editForm.icon = res.objectKey;
          this.editForm.iconPreview = this.getIconUrl(res.objectKey);
          this.$message.success('Logo 上传成功');
        }
      } catch (error) {
        console.error('Logo 上传失败', error);
        this.$message.error('Logo 上传失败');
      } finally {
        event.target.value = '';
      }
    },

    showCreateDialog() {
      this.createDialogVisible = true
      this.resetDatasetForm()
    },

    triggerFileUpload() {
      this.$refs.fileInput.click()
    },
    async handleFileChange(event) {
      const file = event.target.files && event.target.files[0]
      if (!file) return
      event.target.value = ''
      try {
        const objectKey = await this.doMultipartUpload(file)
        this.datasetForm.filePath = objectKey
        this.$message.success('文件上传成功')
      } catch (error) {
        this.fileUpload.status = 'exception'
        this.fileUpload.statusText = error.message || '上传失败'
        this.$message.error(error.message || '上传文件失败')
      }
    },
    async doMultipartUpload(file) {
      const PART_SIZE = 5 * 1024 * 1024
      const CONCURRENCY = 3
      const fileName = file.name.replace(/\s+/g, '-').replace(/[^a-zA-Z0-9.\-_]/g, '') || 'dataset.bin'

      const fu = this.fileUpload
      fu.uploading = true
      fu.aborted = false
      fu.percent = 0
      fu.status = null
      fu.statusText = '初始化上传...'
      fu.objectKey = ''
      fu.uploadId = ''

      const projectName = (this.$store.state.project && this.$store.state.project.currentProject)
        ? String(this.$store.state.project.currentProject.name) : 'default'
      const initRes = await initDatasetMultipartUpload({ fileName }, { 'X-Project-Name': projectName })
      if (!initRes || !initRes.uploadId) {
        fu.uploading = false
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

          const presignedRes = await getDatasetUploadPresignedUrl({
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

          const putRes = await uploadDatasetPartToS3(url, blob)
          const headers = putRes.headers || {}
          const etag = (headers.get ? (headers.get('etag') || headers.get('ETag')) : (headers['etag'] || headers['ETag']) || '').replace(/"/g, '')

          if (!etag) throw new Error(`分片 ${n} 未返回 ETag，请检查 MinIO CORS 配置`)

          await notifyDatasetPartComplete({
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

      fu.statusText = '合并分片...'
      uploadedParts.sort((a, b) => a.partNumber - b.partNumber)
      const completeRes = await completeDatasetMultipartUpload({
        objectKey: fu.objectKey,
        uploadId: fu.uploadId,
        parts: uploadedParts
      })

      if (!completeRes) {
        fu.uploading = false
        throw new Error('合并分片失败')
      }

      fu.percent = 100
      fu.status = 'success'
      fu.statusText = '上传完成'
      fu.uploading = false
      return (completeRes.objectKey) || fu.objectKey
    },
    async handleSearch() {
      this.currentPage = 1
      await this.loadDatasets()
    },
    handleReset() {
      const currentPublic = this.filters.ifPublic
      this.filters = {
        type: '',
        owner: '',
        name: '',
        version: '',
        describe: '',
        ifPublic: currentPublic
      }
      this.currentPage = 1
      this.loadDatasets()
    },
    async handleView(row) {
      try {
        // 调用详情接口
        const res = await getDatasetDetail(row.id)
        if (res) {
          this.currentDataset = res
          this.detailDialogVisible = true
        } else {
          this.$message.error(res && res.message ? res.message : '获取数据集详情失败')
        }
      } catch (error) {
        console.error('获取数据集详情失败', error)
        this.$message.error('获取数据集详情失败')
      }
    },
    // 创建数据集
    async handleCreateDataset() {
      // 表单验证
      if (!this.datasetForm.name.trim()) {
        this.$message.warning('请输入数据集名称')
        return
      }
      if (!this.datasetForm.label.trim()) {
        this.$message.warning('请输入中文名称')
        return
      }

      try {
        // 构建请求数据（只包含基本信息，不包含文件）
        const requestData = {
          name: this.datasetForm.name,
          version: this.datasetForm.version,
          label: this.datasetForm.label,
          type: this.datasetForm.type || '',
          describe: this.datasetForm.describe || '',
          owner: this.datasetForm.owner || '',
          features: this.datasetForm.features || '',
          path: this.datasetForm.filePath || '',
          icon: this.datasetForm.icon || '',
          ifPublic: false
        }

        // 调用创建接口
        const res = await createDataset(requestData)

        if (res) {
          this.$message.success('数据集创建成功')
          this.createDialogVisible = false
          this.resetDatasetForm()
          // 刷新列表
          await this.loadDatasets()
        } else {
          // 使用兼容写法，不使用可选链操作符
          const errorMsg = res && res.message ? res.message : '创建失败'
          this.$message.error(errorMsg)
        }
      } catch (error) {
        console.error('创建数据集失败', error)
        const errorMsg = error && error.message ? error.message : '创建数据集失败'
        this.$message.error(errorMsg)
      } finally {
      }
    },

    // 删除数据集
    async handleDelete(row) {
      // 确认删除
      try {
        await this.$confirm(`确定要删除数据集 "${row.name}" 吗？删除后不可恢复！`, '删除确认', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          distinguishCancelAndClose: true
        })
        // 调用删除接口
        const res = await deleteDataset(row.id)

        if (res) {
          this.$message.success('数据集删除成功')
          // 刷新列表
          await this.loadDatasets()
        } else {
          const errorMsg = res && res.message ? res.message : '删除失败'
          this.$message.error(errorMsg)
        }
      } catch (error) {
        if (error === 'cancel') {
          this.$message.info('已取消删除')
        } else {
          console.error('删除数据集失败', error)
          const errorMsg = error && error.message ? error.message : '删除数据集失败'
          this.$message.error(errorMsg)
        }
      } finally {
      }
    },

    // 修改数据集（保存编辑）
    async handleSaveEdit() {
      // 表单验证
      if (!this.editForm.name || !this.editForm.name.trim()) {
        this.$message.warning('请输入数据集名称')
        return
      }
      if (!this.editForm.version || !this.editForm.version.trim()) {
        this.$message.warning('请输入版本号')
        return
      }

      try {
        // 构建更新数据
        const updateData = {
          id: this.currentDataset ? this.currentDataset.id : null,
          name: this.editForm.name,
          version: this.editForm.version,
          label: this.editForm.tags,
          describe: this.editForm.describe,
          url: this.editForm.website,
          path: this.editForm.path,
          downloadUrl: this.editForm.downloadUrl,
          owner: this.editForm.owner,
          features: this.editForm.features,
          icon: this.editForm.icon || ''
        }

        // 调用更新接口
        const res = await updateDataset(updateData)

        if (res) {
          this.$message.success('数据集修改成功')
          this.editDialogVisible = false
          // 刷新列表
          await this.loadDatasets()
          // 如果详情弹框还开着，更新详情数据
          if (this.detailDialogVisible && this.currentDataset) {
            this.currentDataset = Object.assign({}, this.currentDataset, updateData)
          }
        } else {
          const errorMsg = res && res.message ? res.message : '修改失败'
          this.$message.error(errorMsg)
        }
      } catch (error) {
        console.error('修改数据集失败', error)
        const errorMsg = error && error.message ? error.message : '修改数据集失败'
        this.$message.error(errorMsg)
      } finally {
      }
    },

    // 重置创建表单
    resetDatasetForm() {
      this.datasetForm = {
        name: '',
        version: '',
        label: '',
        type: '',
        describe: '',
        owner: '',
        features: '{}',
        files: [],
        icon: '',
        iconPreview: '',
        filePath: ''
      }
      this.fileUpload = {
        uploading: false,
        aborted: false,
        percent: 0,
        status: null,
        statusText: '',
        objectKey: '',
        uploadId: ''
      }
      // 清空文件输入框
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = ''
      }
      if (this.$refs.logoInput) {
        this.$refs.logoInput.value = ''
      }
    },

    // 处理编辑时的数据初始化
    handleEdit(row) {
      this.currentDataset = row // 保存当前编辑的数据集
      this.editForm = {
        name: row.name || '',
        version: row.version || '',
        tags: row.label || '',
        describe: row.describe || '',
        website: row.url || '',
        path: row.path || '',
        downloadUrl: row.downloadUrl || '',
        owner: row.owner || '',
        features: row.features || '',
        icon: row.icon || '',
        iconPreview: this.getIconUrl(row.icon)
      }
      this.editDialogVisible = true
    },

    // 处理详情弹框中的编辑
    handleDetailEdit() {
      this.detailDialogVisible = false
      // 延迟一下，避免弹框冲突
      this.$nextTick(function () {
        this.handleEdit(this.currentDataset)
      }.bind(this))
    },
    handleBackup() {
      this.$message.info('备份功能正在开发中，敬请期待！')
    },

    handleAnnotation(row) {
      this.$router.push('/llmops/datasets/annotation')
    },

    // 下载数据集
    handleDownload(row) {
      if (row.downloadUrl) {
        // 如果有下载链接，打开链接
        var urls = row.downloadUrl.split('\n').filter(function (url) { return url.trim() })
        if (urls.length === 1) {
          window.open(urls[0], '_blank')
        } else if (urls.length > 1) {
          // 多个下载链接，弹出选择框
          this.$confirm('该数据集有多个下载链接，请选择要下载的文件', '下载选项', {
            distinguishCancelAndClose: true,
            confirmButtonText: '确定',
            cancelButtonText: '取消'
          }).then(function () {
            urls.forEach(function (url) { window.open(url, '_blank') })
          }).catch(function () { })
        }
      } else {
        this.$message.warning('该数据集暂无下载链接')
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadDatasets()
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1 // 切换每页大小时，重置到第一页
      this.loadDatasets()
    },
    truncateLink(link) {
      if (link.length <= 40) return link
      const start = link.substring(0, 25)
      const end = link.substring(link.length - 15)
      return `${start}...${end}`
    },
    handleCreateDialogClose(done) {
      if (this.fileUpload.uploading) {
        this.$confirm('文件正在上传中，关闭将取消上传，确定关闭？', '提示', {
          confirmButtonText: '确定关闭',
          cancelButtonText: '继续上传',
          type: 'warning'
        }).then(() => {
          this.fileUpload.aborted = true
          done()
        }).catch(() => { })
      } else {
        done()
      }
    },
    abortUpload() {
      this.fileUpload.aborted = true
      this.fileUpload.uploading = false
      this.fileUpload.statusText = '已取消'
      this.fileUpload.status = 'exception'
    },


  }
}
</script>

<style lang="scss" scoped>
.datasets-container {
  min-height: calc(100vh - 60px);
}

.datasets-content {
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

.list-header {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 16px;

  .section-title {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.search-card,
.table-card,
.ai-assistant-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
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

    .assistant-header {
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f3f4f6;
    }

    .chat-area {
      flex: 1;
      max-height: none;
      margin-bottom: 24px;
    }

    .chat-input-area {
      margin-bottom: 24px;
    }

    .quick-suggestions {
      margin-top: auto;
      padding-top: 24px;
    }
  }
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

.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
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

.pagination-container {
  margin-top: 20px;
}

/* 预览图标 */
.preview-icon-svg {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  ::v-deep svg {
    max-width: 100%;
    max-height: 100%;
    width: 40px !important;
    height: 40px !important;
  }
}

.preview-icon-img {
  width: 40px;
  height: 40px;
}

.preview-icon-large-img {
  width: 100px;
  height: 100px;
  object-fit: contain;
}

.preview-icon-large-svg {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: white;
  border-radius: 8px;
  padding: 10px;
  margin: 0 auto;

  ::v-deep svg {
    max-width: 100%;
    max-height: 100%;
    width: 80px !important;
    height: 80px !important;
  }
}

.preview-icon-large {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: white;
  margin: 0 auto;
}

.preview-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.text-type {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.image-type {
  background: linear-gradient(135deg, #10b981, #059669);
}

.tabular-type {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.audio-type {
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
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

  .suggestion-buttons {
    flex-direction: column;
    align-items: flex-start;
  }

  .suggestion-btn {
    width: 100%;
    justify-content: flex-start;
  }
}

/* 创建数据集弹框样式 */
.create-dataset-dialog {
  .upload-area {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    padding: 32px 24px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;

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

.logo-preview,
.logo-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.logo-preview {
  border: 1px solid #e5e7eb;
}

.logo-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-placeholder {
  background-color: #f5f7fa;
  border: 2px dashed #d9d9d9;
  color: #c0c4cc;
  font-size: 32px;
}

/* 下载链接样式 */
.download-links {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.download-link {
  display: inline-block;
  padding: 6px 10px;
  background-color: #f9fafb;
  color: #374151;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  text-decoration: none;
  transition: all 0.3s;
  border: 1px solid #e5e7eb;
  word-break: break-all;
  line-height: 1.4;
}

.download-link:hover {
  background-color: #eff6ff;
  color: #2563eb;
  border-color: #bfdbfe;
}

/* 数据集详情弹框样式 */
.dataset-detail-dialog {
  ::v-deep .el-dialog {
    border-radius: 16px;
  }

  ::v-deep .el-dialog__header {
    padding: 24px 24px 0;
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
}

.dialog-title-custom {
  margin: 0;
}

.dataset-detail-header {
  display: flex;
  flex-direction: column;
}

.dataset-detail-title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
}

.dataset-detail-content {
  display: flex;
  flex-direction: column;
}

.detail-main {
  display: flex;
  gap: 24px;
}

.detail-left {
  width: 220px;
  flex-shrink: 0;
}

.detail-right {
  flex: 1;
}

.preview-card {
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-icon-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

.preview-icon-large {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: white;
  margin: 0 auto;
}

.detail-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label-small {
  font-size: 14px;
  color: #6b7280;
}

.detail-value-large {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.status-normal {
  color: #10b981;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-field.full-width {
  grid-column: span 2;
}

.field-label {
  font-size: 15px;
  color: #374151;
  font-weight: 500;
}

.field-value {
  padding: 12px 16px;
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 15px;
  color: #1f2937;
  min-height: 24px;
}

.field-value.download-link a {
  color: #3b82f6;
  text-decoration: none;
  word-break: break-all;
}

.field-value.download-link a:hover {
  color: #2563eb;
  text-decoration: underline;
}

/* 修改数据集弹框样式 */
.edit-dataset-dialog {
  ::v-deep .el-dialog {
    border-radius: 16px;
  }

  ::v-deep .el-dialog__header {
    padding: 24px 24px 0;
    border-bottom: 1px solid #e5e7eb;
    margin-right: 0;
    padding-bottom: 16px;
  }

  ::v-deep .el-dialog__title {
    font-size: 22px;
    font-weight: bold;
    color: #1f2937;
  }

  ::v-deep .el-dialog__headerbtn {
    top: 24px;
  }

  ::v-deep .el-dialog__body {
    padding: 24px;
  }

  ::v-deep .el-dialog__footer {
    padding: 16px 24px 24px;
    border-top: 1px solid #e5e7eb;
  }
}

.edit-form {
  .form-row {
    display: flex;
    gap: 16px;
  }

  .form-item-half {
    flex: 1;
  }

  ::v-deep .el-form-item__label {
    color: #374151;
    font-weight: 500;
    font-size: 14px;
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-select .el-input__inner,
  ::v-deep .el-textarea__inner {
    border-radius: 10px;
    border: 1px solid #d1d5db;
    padding: 12px 16px;
    font-size: 15px;
    transition: all 0.3s;
  }

  ::v-deep .el-input__inner:focus,
  ::v-deep .el-select .el-input__inner:focus,
  ::v-deep .el-textarea__inner:focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }

  ::v-deep .el-button {
    border-radius: 10px;
  }

  ::v-deep .el-button--primary {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    border: none;
    transition: all 0.3s;
  }

  ::v-deep .el-button--primary:hover {
    background: linear-gradient(135deg, #2563eb, #1d4ed8);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  }
}

.preview-upload {
  display: flex;
  gap: 12px;
  align-items: center;
}

.preview-icon-upload {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  background-color: #dbeafe;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #3b82f6;
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
</style>