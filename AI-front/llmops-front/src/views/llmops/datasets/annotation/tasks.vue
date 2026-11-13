<template>
  <div class="tasks-container">
    <div class="tasks-content">
      <!-- 头部 -->
      <!-- <div class="page-header">
        <div class="header-left">
          <div class="back-btn" @click="handleBack">
            <i class="el-icon-arrow-left"></i>
          </div>
          <div class="breadcrumb">
            <span class="breadcrumb-item" @click="handleBack">Projects</span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item">{{ projectName }}</span>
            <template v-if="selectedTask">
              <span class="breadcrumb-separator">/</span>
              <span class="breadcrumb-item active">Labeling</span>
            </template>
            <template v-else>
              <span class="breadcrumb-separator">/</span>
              <span class="breadcrumb-item active">{{ projectName }}</span>
            </template>
          </div>
        </div>
        <div class="header-actions">
          <el-button class="header-btn" @click="goSettings">
            <i class="el-icon-setting"></i> Settings
          </el-button>
          <el-button class="header-btn icon-btn">
            <i class="el-icon-document-copy"></i>
          </el-button>
          <div class="header-status">
            <span class="status-dot"></span>
            <span class="status-text">Auto</span>
          </div>
          <div class="header-avatar">WK</div>
        </div>
      </div> -->

      <div class="main-layout" :class="{ 'split-mode': selectedTask }">
        <!-- 左侧任务列表 -->
        <div class="left-panel" :class="{ collapsed: selectedTask }">
          <!-- 标签页栏 -->
          <div class="tabs-bar">
            <div class="tab-item active">
              <span>Default</span>
            </div>
            <div class="tab-add" @click="addTab">
              <i class="el-icon-plus"></i>
            </div>
          </div>

          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-dropdown trigger="click" @command="handleActions">
                <el-button class="toolbar-btn">
                  Actions <i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="delete">删除选中</el-dropdown-item>
                  <el-dropdown-item command="export">导出选中</el-dropdown-item>
                  <el-dropdown-item command="cancel">取消标注</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-dropdown trigger="click">
                <el-button class="toolbar-btn">
                  Columns <i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item v-for="col in columnOptions" :key="col.key">
                    <el-checkbox :value="visibleColumns.includes(col.key)" @change="toggleColumn(col.key)">
                      {{ col.label }}
                    </el-checkbox>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-select v-model="status" size="small" class="status-select" @change="handleStatusChange">
                <el-option
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"></el-option>
              </el-select>
              <el-button type="primary" class="settings-btn" @click="goSettings">
                <i class="el-icon-setting"></i> Settings
              </el-button>
            </div>
            <div class="toolbar-right">
              <el-button class="toolbar-btn icon-btn" @click="loadTasks">
                <i class="el-icon-refresh"></i>
              </el-button>
              <el-button class="toolbar-btn" @click="triggerFileUpload">Import</el-button>
              <el-button class="toolbar-btn" @click="handleExport">Export</el-button>
            </div>
          </div>

          <!-- 任务表格 -->
          <div class="table-card" v-loading="loading" element-loading-text="加载中...">
            <!-- 空状态：项目无数据 -->
            <div v-if="!loading && total === 0" class="empty-state">
              <div class="empty-icon-circle">
                <i class="el-icon-upload2"></i>
              </div>
              <h2 class="empty-title">Import data to get your project started</h2>
              <p class="empty-subtitle">Connect your cloud storage or upload files from your computer</p>
              <div class="cloud-icons">
                <div class="cloud-icon" title="S3 Storage">
                  <i class="el-icon-box"></i>
                </div>
                <div class="cloud-icon" title="Google Cloud Storage">
                  <i class="el-icon-cloudy"></i>
                </div>
                <div class="cloud-icon" title="Azure Storage">
                  <i class="el-icon-files"></i>
                </div>
                <div class="cloud-icon" title="Local Storage">
                  <i class="el-icon-folder-opened"></i>
                </div>
              </div>
              <div class="empty-actions">
                <el-button type="primary" class="empty-action-btn" :loading="uploadLoading" @click="triggerFileUpload">
                  <i class="el-icon-upload2"></i> 连接云存储
                </el-button>
                <el-button class="empty-action-btn" @click="triggerFileUpload">
                  <i class="el-icon-folder-opened"></i> 导入文件
                </el-button>
              </div>
              <a class="empty-docs-link" href="https://labelstud.io/guide/tasks.html" target="_blank" rel="noopener">
                See docs on importing data <i class="el-icon-link"></i>
              </a>
            </div>

            <template v-else>
            <el-table
              ref="taskTable"
              :data="tasks"
              style="width: 100%"
              stripe
              height="100%"
              @row-click="handleRowClick"
              :row-class-name="rowClassName">
              <el-table-column label="ID" width="80" prop="id" align="center">
                <template slot-scope="scope">
                  <span class="task-id">{{ scope.row.id }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Completed" width="150" align="center">
                <template slot-scope="scope">
                  <span class="completed-time">{{ formatCompletedAt(scope.row.completed_at) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="" width="60" align="center">
                <template slot="header">
                  <i class="el-icon-document-copy stat-icon annotation"></i>
                </template>
                <template slot-scope="scope">
                  <span class="stat-cell">
                    <i class="el-icon-document-copy stat-icon annotation"></i>
                    <span class="stat-value">{{ scope.row.completed }}</span>
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="" width="60" align="center">
                <template slot="header">
                  <i class="el-icon-circle-close stat-icon cancelled"></i>
                </template>
                <template slot-scope="scope">
                  <span class="stat-cell">
                    <i class="el-icon-circle-close stat-icon cancelled"></i>
                    <span class="stat-value">{{ scope.row.cancelled }}</span>
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="" width="60" align="center">
                <template slot="header">
                  <i class="el-icon-magic-stick stat-icon predicted"></i>
                </template>
                <template slot-scope="scope">
                  <span class="stat-cell">
                    <i class="el-icon-magic-stick stat-icon predicted"></i>
                    <span class="stat-value">{{ scope.row.predicted }}</span>
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="Annotated by" width="140" align="center">
                <template slot-scope="scope">
                  <div class="annotated-by-cell">
                    <template v-if="scope.row.annotatedBy && scope.row.annotatedBy.length">
                      <el-tooltip
                        v-for="(u, idx) in scope.row.annotatedBy"
                        :key="u.user_id || u.id"
                        :content="u.first_name + ' ' + u.last_name || u.username || u.email"
                        placement="top">
                        <span class="user-avatar" :style="{ backgroundColor: getAvatarColor(u) }">
                          {{ getInitials(u) }}
                        </span>
                      </el-tooltip>
                    </template>
                    <span v-else class="annotated-empty"></span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                v-for="field in dataFields"
                :key="field.key"
                :label="field.label"
                min-width="120"
                show-overflow-tooltip>
                <template slot="header">
                  <span>{{ field.label }}</span>
                  <el-tag size="mini" type="info" class="type-tag">{{ field.type }}</el-tag>
                </template>
                <template slot-scope="scope">
                  <span class="data-text">{{ scope.row.data[field.key] }}</span>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-wrapper" v-if="total > 0">
              <el-pagination
                background
                layout="total, prev, pager, next, jumper"
                :total="total"
                :current-page.sync="page"
                :page-size="pageSize"
                @current-change="loadTasks">
              </el-pagination>
            </div>
            </template>
          </div>

          <input
            ref="fileInput"
            type="file"
            multiple
            style="display: none"
            @change="handleFileChange" />

          <!-- 底部统计栏 -->
          <div class="stats-footer">
            <span class="stats-item">Tasks: {{ tasks.length }} / {{ total }}</span>
            <span class="stats-item">Submitted annotations: {{ totalAnnotations }}</span>
            <span class="stats-item">Predictions: {{ totalPredictions }}</span>
          </div>
        </div>

        <!-- 右侧标注面板 -->
        <div class="right-panel" v-if="selectedTask">
          <!-- 标注区顶部 -->
          <div class="annotation-top">
            <div class="compare-bar">
              <el-button class="compare-btn">
                <i class="el-icon-s-grid"></i> Compare All
              </el-button>
              <el-button class="compare-btn icon-btn" @click="addCurrentAnnotationTab">
                <i class="el-icon-plus"></i>
              </el-button>
              <div
                v-for="item in annotators"
                :key="item.id"
                class="annotator-chip"
                :class="{ active: activeAnnotator === item.id, 'is-prediction': item.type === 'prediction' }"
                @click="loadAnnotatorResult(item.id)">
                <div class="annotator-avatar" :class="avatarClass(item)">{{ item.initial }}</div>
                <div class="annotator-meta">
                  <span class="annotator-name">{{ item.name }}</span>
                  <span class="annotator-time">
                    {{ item.time }}<template v-if="item.score"> · {{ item.score }}%</template>
                  </span>
                </div>
                <span class="annotator-action-wrap" @click.stop>
                  <el-dropdown
                    trigger="click"
                    @command="(cmd) => handleAnnotationCommand(cmd, item)"
                    placement="bottom-start">
                    <i class="el-icon-s-operation annotator-action"></i>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item command="copyId" v-if="item.type === 'annotation'">
                        <i class="el-icon-document-copy"></i> Copy Annotation ID
                      </el-dropdown-item>
                      <el-dropdown-item command="duplicate" v-if="item.type === 'annotation'">
                        <i class="el-icon-document"></i> Duplicate Annotation
                      </el-dropdown-item>
                      <el-dropdown-item command="copyLink" v-if="item.type === 'annotation'">
                        <i class="el-icon-link"></i> Copy Annotation Link
                      </el-dropdown-item>
                      <el-dropdown-item command="showOthers" v-if="item.type === 'annotation'">
                        <i class="el-icon-view"></i> Show Other Annotations
                      </el-dropdown-item>
                      <el-dropdown-item
                        command="delete"
                        divided
                        :style="{ color: '#f56c6c' }">
                        <i class="el-icon-delete"></i> Delete Annotation
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </div>
            </div>
          </div>

          <!-- 数据内容区 -->
          <div class="annotation-body">
            <div class="data-content">
              <template v-for="field in labelConfig.textFields">
                <div :key="field.name" class="data-field">
                  <div class="field-label">{{ field.name }}</div>
                  <div class="field-value">{{ selectedTask.data[field.value.replace('$', '')] }}</div>
                </div>
              </template>
            </div>

            <!-- 标注卡片 -->
            <div class="annotation-card" v-for="(section, idx) in labelConfig.sections" :key="idx">
              <div class="annotation-header" v-if="section.header">{{ section.header }}</div>
              <div class="annotation-choices">
                <label
                  class="choice-item"
                  :class="{ 'is-readonly': isReadonlyView }"
                  v-for="choice in section.choices"
                  :key="choice.value">
                  <input
                    type="checkbox"
                    :checked="isChoiceChecked(section, choice)"
                    :disabled="isReadonlyView"
                    @change="toggleChoice(section, choice)">
                  <span class="choice-text">{{ choice.value }}</span>
                  <span class="choice-hotkey" v-if="!isReadonlyView">{{ choice.hotkey }}</span>
                </label>
              </div>
            </div>

            <!-- AI 预测加载动画 -->
            <div class="ai-loading-wrap" v-if="aiLoading">
              <div class="ai-loading-bar"></div>
              <div class="ai-loading-text">AI 预测生成中...</div>
            </div>
          </div>

          <!-- 底部操作栏 -->
          <div class="annotation-footer">
            <div class="footer-left">
              <span class="task-id-badge">#{{ selectedTask.id }}</span>
              <el-button class="footer-btn icon-btn" @click="skipTask">
                <i class="el-icon-d-arrow-right"></i>
              </el-button>
              <el-button class="footer-btn icon-btn">
                <i class="el-icon-refresh-left"></i>
              </el-button>
              <el-button class="footer-btn icon-btn">
                <i class="el-icon-refresh-right"></i>
              </el-button>
              <el-button class="footer-btn icon-btn" v-if="!isReadonlyView">
                <i class="el-icon-magic-stick"></i>
              </el-button>
            </div>
            <div class="footer-right" v-if="!isReadonlyView">
              <el-button
                v-if="activeAnnotatorItem && activeAnnotatorItem.type === 'current'"
                type="primary"
                class="submit-btn"
                @click="submitAnnotation">
                Submit
              </el-button>
              <el-button
                v-else-if="isOwnAnnotation"
                type="primary"
                class="submit-btn"
                :disabled="!canUpdate"
                @click="updateAnnotationResult">
                Update
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 导出数据弹窗 -->
    <el-dialog
      title="Export data"
      :visible.sync="exportDialogVisible"
      width="520px"
      :close-on-click-modal="false"
      append-to-body
      custom-class="export-data-dialog">
      <div class="export-data-content">
        <p class="export-data-desc">
          You can export dataset in one of the following formats:
        </p>
        <div class="export-format-list" v-loading="exportFormatsLoading">
          <div
            v-for="fmt in exportFormats"
            :key="fmt.name"
            class="export-format-item"
            :class="{ disabled: fmt.disabled, selected: selectedExportFormat === fmt.name }"
            @click="!fmt.disabled && (selectedExportFormat = fmt.name)">
            <div class="export-format-radio">
              <el-radio
                v-model="selectedExportFormat"
                :label="fmt.name"
                :disabled="fmt.disabled"
                @click.native.stop></el-radio>
            </div>
            <div class="export-format-info">
              <div class="export-format-title">{{ fmt.title }}</div>
              <div class="export-format-desc">{{ fmt.description }}</div>
              <div v-if="fmt.tags && fmt.tags.length" class="export-format-tags">
                <span v-for="tag in fmt.tags" :key="tag" class="export-format-tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>
        <p class="export-data-footer">
          Can't find an export format?
          <a href="https://labelstud.io/guide/export.html" target="_blank" rel="noopener">Let us know in Slack or submit an issue to the Repository</a>
        </p>
        <p class="export-data-tip">
          Having a timeout or trouble exporting large projects?
        </p>
      </div>
      <div slot="footer">
        <el-button @click="exportDialogVisible = false">Cancel</el-button>
        <el-button
          type="primary"
          :disabled="!selectedExportFormat || exportFormatsLoading"
          :loading="exportLoading"
          @click="confirmExport">Export</el-button>
      </div>
    </el-dialog>

    <!-- 未配置 Labeling Interface 提示弹窗 -->
    <el-dialog
      :visible.sync="noLabelConfigDialogVisible"
      width="480px"
      :show-close="false"
      append-to-body
      custom-class="no-label-config-dialog">
      <div class="no-label-config-content">
        <h2 class="no-label-config-title">You're almost there!</h2>
        <p class="no-label-config-desc">Before you can annotate the data, set up labeling configuration</p>
      </div>
      <div slot="footer" class="no-label-config-footer">
        <el-button @click="noLabelConfigDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="goToLabelingSetup">Go to setup</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProject, getProjectTasks, getProjectViews, createProjectView, getProjectUsers, getTaskAnnotations, getTaskPredictions, loginLabelStudio, registerLabelStudio, createTaskAnnotation, deleteAnnotation, deletePrediction, updateAnnotation, uploadDatasetFiles, getProjectExportFormats, exportProjectData, getProjectModels, aiAnnotation } from '@/api/llmops/labelstudio'

export default {
  name: 'AnnotationTasks',
  data() {
    return {
      projectId: this.$route.params.id,
      projectName: '',
      loading: false,
      page: 1,
      pageSize: 20,
      total: 0,
      status: 'all',
      statusOptions: [
        { value: 'all', label: '全部' },
        { value: 'annotated', label: '已标注' },
        { value: 'unannotated', label: '未标注' }
      ],
      tasks: [],
      selectedTasks: [],
      dataFields: [],
      visibleColumns: ['id', 'completed'],
      selectedTask: null,
      selectedTaskIndex: -1,
      hasLabelConfig: false,
      noLabelConfigDialogVisible: false,
      labelConfig: {
        textFields: [],
        sections: []
      },
      annotationResult: {},
      originalAnnotationResult: {},
      annotators: [],
      activeAnnotator: null,
      annotations: [],
      predictions: [],
      projectModels: [],
      currentUser: localStorage.getItem('userName') || 'admin',
      lsDefaultPassword: 'Llops@2024',
      isDragover: false,
      uploadLoading: false,
      selectedFiles: [],
      fileTypes: [
        { label: '文本', exts: '.txt, .json, .csv, .tsv' },
        { label: '图像', exts: '.jpg, .jpeg, .png, .gif, .bmp, .webp' },
        { label: '音频', exts: '.wav, .mp3, .flac, .ogg' },
        { label: '视频', exts: '.mp4, .avi, .mov, .mkv' },
        { label: '其他', exts: '.html, .pdf, .zip' }
      ],
      exportDialogVisible: false,
      exportFormats: [],
      exportFormatsLoading: false,
      selectedExportFormat: '',
      exportLoading: false,
      aiLoading: false
    }
  },
  computed: {
    columnOptions() {
      const fixed = [
        { key: 'id', label: 'ID' },
        { key: 'completed', label: 'Completed' }
      ]
      const dataCols = this.dataFields.map(f => ({ key: f.key, label: f.label }))
      return [...fixed, ...dataCols]
    },
    selectedTaskId() {
      return this.selectedTask ? this.selectedTask.id : null
    },
    activeAnnotatorItem() {
      return this.annotators.find(a => a.id === this.activeAnnotator) || null
    },
    isViewingPrediction() {
      return this.activeAnnotatorItem && this.activeAnnotatorItem.type === 'prediction'
    },
    isOwnAnnotation() {
      const item = this.activeAnnotatorItem
      if (!item || item.type !== 'annotation' || !item.source) return false
      return this.isAnnotationOwnedByCurrentUser(item.source)
    },
    isAnnotationDirty() {
      return JSON.stringify(this.annotationResult) !== JSON.stringify(this.originalAnnotationResult)
    },
    canUpdate() {
      return this.isOwnAnnotation && this.isAnnotationDirty
    },
    isReadonlyView() {
      const item = this.activeAnnotatorItem
      if (!item) return true
      if (item.type === 'current') return false
      if (item.type === 'prediction') return true
      return !this.isOwnAnnotation
    },
    totalAnnotations() {
      return this.tasks.reduce((sum, t) => sum + (t.completed || 0), 0)
    },
    totalPredictions() {
      return this.tasks.reduce((sum, t) => sum + (t.predicted || 0), 0)
    }
  },
  created() {
    this.loadProject()
    this.loadTasks()
  },
  methods: {
    async loadProject() {
      try {
        const data = await getProject(this.projectId)
        if (data) {
          this.projectName = data.title || data.name || `项目 #${this.projectId}`
          const trimmed = (data.label_config || '').trim()
          this.hasLabelConfig = !!trimmed && trimmed !== '<View></View>'
          if (this.hasLabelConfig) {
            this.labelConfig = this.parseLabelConfig(data.label_config)
          }
        }
      } catch (e) {
        this.projectName = `项目 #${this.projectId}`
      }
      this.loadProjectModels()
    },
    async loadProjectModels() {
      try {
        const res = await getProjectModels(this.projectId)
        const list = (res && res.items) || (res && res.results) || (Array.isArray(res) ? res : [])
        this.projectModels = list.filter(m => m && m.id)
      } catch (e) {
        console.error('加载项目模型失败:', e)
        this.projectModels = []
      }
    },
    parseLabelConfig(xml) {
      const config = { textFields: [], sections: [] }
      const parser = new DOMParser()
      const doc = parser.parseFromString(xml, 'application/xml')

      const textNodes = doc.getElementsByTagName('Text')
      for (let i = 0; i < textNodes.length; i++) {
        const n = textNodes[i]
        config.textFields.push({
          name: n.getAttribute('name') || 'text',
          value: n.getAttribute('value') || '$text'
        })
      }

      const choiceNodes = doc.getElementsByTagName('Choices')
      for (let i = 0; i < choiceNodes.length; i++) {
        const c = choiceNodes[i]
        const section = {
          name: c.getAttribute('name') || `choices_${i}`,
          choice: c.getAttribute('choice') || 'single',
          showInLine: c.getAttribute('showInLine') === 'true',
          header: '',
          choices: []
        }
        const view = c.parentElement
        if (view && view.tagName === 'View') {
          const header = view.querySelector('Header')
          if (header) section.header = header.getAttribute('value') || ''
        }
        const choiceItems = c.getElementsByTagName('Choice')
        for (let j = 0; j < choiceItems.length; j++) {
          const ci = choiceItems[j]
          section.choices.push({
            value: ci.getAttribute('value') || '',
            hotkey: ci.getAttribute('hotkey') || ''
          })
        }
        config.sections.push(section)
      }

      return config
    },
    async loadTasks() {
      this.loading = true
      try {
        const [viewsRes, usersRes] = await Promise.all([
          getProjectViews(this.projectId).catch(() => null),
          getProjectUsers(this.projectId).catch(() => null)
        ])
        let views = (viewsRes && viewsRes.results) || viewsRes || []
        if (views.length === 0) {
          try {
            await createProjectView(this.projectId, {
              data: {
                title: 'Default',
                type: 'list',
                target: 'tasks',
                hiddenColumns: {
                  explore: [
                    'tasks:inner_id',
                    'tasks:annotations_results',
                    'tasks:annotations_ids',
                    'tasks:predictions_score',
                    'tasks:predictions_model_versions',
                    'tasks:predictions_results',
                    'tasks:file_upload',
                    'tasks:storage_filename',
                    'tasks:created_at',
                    'tasks:updated_at',
                    'tasks:updated_by',
                    'tasks:avg_lead_time',
                    'tasks:draft_exists'
                  ],
                  labeling: [
                    'tasks:data.age',
                    'tasks:data.pdays',
                    'tasks:data.campaign',
                    'tasks:data.duration',
                    'tasks:data.previous',
                    'tasks:data.euribor3m',
                    'tasks:data.nr_employed',
                    'tasks:data.emp_var_rate',
                    'tasks:data.cons_conf_idx',
                    'tasks:data.cons_price_idx',
                    'tasks:id',
                    'tasks:inner_id',
                    'tasks:completed_at',
                    'tasks:cancelled_annotations',
                    'tasks:total_predictions',
                    'tasks:annotators',
                    'tasks:annotations_results',
                    'tasks:annotations_ids',
                    'tasks:predictions_score',
                    'tasks:predictions_model_versions',
                    'tasks:predictions_results',
                    'tasks:file_upload',
                    'tasks:storage_filename',
                    'tasks:created_at',
                    'tasks:updated_at',
                    'tasks:updated_by',
                    'tasks:avg_lead_time',
                    'tasks:draft_exists'
                  ]
                },
                columnsWidth: {},
                columnsDisplayType: {},
                gridWidth: 4,
                gridFitImagesToWidth: false,
                semantic_search: [],
                agreement_selected: {},
                filters: {
                  conjunction: 'and',
                  items: []
                }
              }
            })
            const newViewsRes = await getProjectViews(this.projectId).catch(() => null)
            views = (newViewsRes && newViewsRes.results) || newViewsRes || []
          } catch (e) {
            console.error('创建默认视图失败:', e)
          }
        }
        const viewId = views.length > 0 ? views[0].id : null
        const userMap = {}
        if (usersRes) {
          const userList = (usersRes && usersRes.results) || usersRes || []
          userList.forEach(u => {
            userMap[u.id] = u
          })
        }
        const params = {
          page: this.page,
          page_size: this.pageSize
        }
        if (viewId) {
          params.view = viewId
        }
        const res = await getProjectTasks(this.projectId, params)
        const results = (res && res.tasks) || []
        this.total = (res && res.total) || 0
        this.tasks = results.map(t => this.mapTask(t, userMap))
        this.inferDataFields(results)
      } catch (err) {
        console.error('获取任务列表失败:', err)
        this.tasks = []
        this.$message.error('获取任务列表失败')
      } finally {
        this.loading = false
      }
    },
    triggerFileUpload() {
      this.$refs.fileInput && this.$refs.fileInput.click()
    },
    handleFileChange(e) {
      const files = e.target.files
      if (files && files.length) {
        this.handleUploadFiles(Array.from(files))
      }
      e.target.value = ''
    },
    async handleUploadFiles(fileList) {
      if (!fileList || !fileList.length) return
      this.uploadLoading = true
      try {
        const uploadRes = await uploadDatasetFiles(this.projectId, fileList)
        console.log('[标注任务] 文件上传响应:', uploadRes)
        const taskCount = (uploadRes && uploadRes.task_count) || (uploadRes && uploadRes.uploaded) || fileList.length
        this.$message.success(`成功导入 ${taskCount} 条数据`)
        this.selectedFiles = []
        await this.loadTasks()
      } catch (err) {
        console.error('[标注任务] 文件上传失败:', err)
        this.$message.error('数据导入失败，请稍后重试')
      } finally {
        this.uploadLoading = false
      }
    },
    formatFileSize(bytes) {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    },
    getInitials(u) {
      if (u.initials) return u.initials
      const first = (u.first_name || '').trim()
      const last = (u.last_name || '').trim()
      if (first || last) {
        const fParts = first.split(/\s+/).filter(Boolean)
        const lParts = last.split(/\s+/).filter(Boolean)
        const allParts = [...fParts, ...lParts]
        if (allParts.length >= 2) return (allParts[0][0] + allParts[allParts.length - 1][0]).toUpperCase()
        return allParts[0].substring(0, 2).toUpperCase()
      }
      if (u.username) return u.username.substring(0, 2).toUpperCase()
      if (u.email) {
        const name = u.email.split('@')[0]
        return name.substring(0, 2).toUpperCase()
      }
      return String(u.user_id || u.id || '?').substring(0, 2)
    },
    getAvatarColor(u) {
      const name = u.username || u.email || u.first_name || u.last_name || '?'
      let hash = 0
      for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash)
      }
      const hue = Math.abs(hash) % 360
      return `hsl(${hue}, 60%, 55%)`
    },
    formatCompletedAt(iso) {
      if (!iso) return '0'
      try {
        const d = new Date(iso)
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
        const mon = months[d.getMonth()]
        const day = d.getDate()
        const year = d.getFullYear()
        const hh = String(d.getHours()).padStart(2, '0')
        const mm = String(d.getMinutes()).padStart(2, '0')
        const ss = String(d.getSeconds()).padStart(2, '0')
        return `${mon} ${day} ${year}, ${hh}:${mm}:${ss}`
      } catch (e) {
        return iso
      }
    },
    mapTask(t, userMap) {
      const annotatorIds = Array.isArray(t.annotators) ? t.annotators : []
      const annotators = annotatorIds
        .map(uid => {
          const u = userMap[uid]
          if (u) {
            return {
              user_id: uid,
              id: uid,
              first_name: u.first_name || '',
              last_name: u.last_name || '',
              username: u.username || u.first_name || `user${uid}`,
              email: u.email || '',
              initials: u.initials || ''
            }
          }
          return {
            user_id: uid,
            id: uid,
            first_name: '',
            last_name: '',
            username: `user${uid}`,
            email: ''
          }
        })
        .filter(Boolean)
      const totalAnnotations = t.total_annotations || 0
      const cancelled = t.cancelled_annotations || 0
      const completed = Math.max(0, totalAnnotations - cancelled)
      return {
        id: t.id,
        inner_id: t.inner_id,
        data: t.data || {},
        completed: completed,
        skipped: cancelled,
        cancelled: cancelled,
        predicted: t.total_predictions || 0,
        is_labeled: !!t.is_labeled,
        annotatedBy: annotators,
        completed_at: t.completed_at || null,
        created_at: t.created_at
      }
    },
    inferDataFields(tasks) {
      const fieldMap = {}
      tasks.forEach(t => {
        const data = t.data || {}
        Object.keys(data).forEach(key => {
          if (!fieldMap[key]) {
            const val = data[key]
            let type = 'str'
            if (Array.isArray(val)) type = 'list'
            else if (typeof val === 'number') type = 'int'
            else if (typeof val === 'boolean') type = 'bool'
            fieldMap[key] = { key, label: key, type }
          }
        })
      })
      this.dataFields = Object.values(fieldMap)
      const fieldKeys = this.dataFields.map(f => f.key)
      const existingVisible = new Set(this.visibleColumns)
      fieldKeys.forEach(k => {
        if (!existingVisible.has(k)) {
          this.visibleColumns.push(k)
        }
      })
    },
    rowClassName({ row }) {
      return row.id === this.selectedTaskId ? 'current-row' : ''
    },
    handleRowClick(row) {
      if (!this.hasLabelConfig) {
        this.noLabelConfigDialogVisible = true
        return
      }
      if (this.selectedTask && this.selectedTask.id === row.id) {
        this.selectedTask = null
        this.selectedTaskIndex = -1
        this.annotationResult = {}
        this.annotators = []
        this.activeAnnotator = null
        return
      }
      const idx = this.tasks.findIndex(t => t.id === row.id)
      this.selectedTask = row
      this.selectedTaskIndex = idx
      this.annotationResult = {}
      this.annotators = []
      this.activeAnnotator = null
      this.loadTaskAnnotators(row.id)
    },
    async loadTaskAnnotators(taskId) {
      try {
        const [annotations, predictions] = await Promise.all([
          getTaskAnnotations(taskId).catch(() => []),
          getTaskPredictions(taskId).catch(() => [])
        ])
        this.annotations = this.extractItems(annotations)
        this.predictions = this.extractItems(predictions)
        this.buildAnnotators()
        await this.ensureAiPredictions(taskId)
      } catch (e) {
        console.error('加载标注人员失败:', e)
        this.annotators = []
      }
    },
    async ensureAiPredictions(taskId) {
      if (!this.projectModels || this.projectModels.length === 0) return
      const existingVersions = new Set(
        this.predictions
          .map(p => p && p.model_version)
          .filter(Boolean)
      )
      const missingModels = this.projectModels.filter(m => {
        const mv = m.model_version
        if (!mv) return false
        return !existingVersions.has(mv)
      })
      if (missingModels.length === 0) return
      this.aiLoading = true
      const promises = missingModels.map(m =>
        aiAnnotation(taskId, {
          model_id: m.id,
          model_version: m.model_version,
          overwrite: true,
          save: true
        }).catch(err => {
          console.error(`AI 标注失败 (model: ${m.model_version}):`, err)
          return null
        })
      )
      try {
        await Promise.all(promises)
      } catch (e) {
        console.error('AI 标注过程出错:', e)
      } finally {
        this.aiLoading = false
      }
      try {
        const refreshed = await getTaskPredictions(taskId).catch(() => [])
        this.predictions = this.extractItems(refreshed)
        this.buildAnnotators()
        const task = this.tasks.find(t => t.id === taskId)
        if (task) {
          task.predicted = this.predictions.length
        }
      } catch (e) {
        console.error('刷新预测结果失败:', e)
      }
    },
    extractItems(resp) {
      if (Array.isArray(resp)) return resp
      if (resp && Array.isArray(resp.items)) return resp.items
      if (resp && Array.isArray(resp.results)) return resp.results
      if (resp && Array.isArray(resp.predictions)) return resp.predictions
      if (resp && Array.isArray(resp.annotations)) return resp.annotations
      return []
    },
    buildAnnotators() {
      const list = []
      this.annotations.forEach((ann, i) => {
        const userName = this.parseAnnotationUser(ann)
        const initial = (userName || 'u').charAt(0).toUpperCase()
        list.push({
          id: `ann-${ann.id || i}`,
          type: 'annotation',
          source: ann,
          name: userName,
          initial,
          time: ann.created_at ? this.formatTime(ann.created_at) : '',
          score: null
        })
      })
      this.predictions.forEach((pred, i) => {
        const modelName = pred.model_version || `prediction-${i}`
        list.push({
          id: `pred-${pred.id || i}`,
          type: 'prediction',
          source: pred,
          name: modelName,
          initial: modelName.charAt(0).toUpperCase(),
          time: pred.created_at ? this.formatTime(pred.created_at) : '',
          score: typeof pred.score === 'number' ? (pred.score * 100).toFixed(2) : null
        })
      })

      const hasAnnotation = this.annotations.length > 0
      const hasPrediction = this.predictions.length > 0

      if (list.length === 0) {
        list.push({
          id: 'current-user',
          type: 'current',
          name: this.currentUser,
          initial: this.currentUser.charAt(0).toUpperCase(),
          time: '',
          score: null
        })
        this.annotators = list
        this.activeAnnotator = 'current-user'
        this.annotationResult = {}
        return
      }

      if (!hasAnnotation && hasPrediction) {
        list.unshift({
          id: 'current-user',
          type: 'current',
          name: this.currentUser,
          initial: this.currentUser.charAt(0).toUpperCase(),
          time: '',
          score: null
        })
      }

      this.annotators = list

      const currentUserAnn = list.find(a => a.type === 'annotation' && a.name === this.currentUser)
      if (currentUserAnn) {
        this.activeAnnotator = currentUserAnn.id
      } else {
        const currentUserTab = list.find(a => a.type === 'current')
        if (currentUserTab) {
          this.activeAnnotator = currentUserTab.id
        } else if (hasPrediction) {
          const firstPred = list.find(a => a.type === 'prediction')
          this.activeAnnotator = firstPred.id
        } else {
          this.activeAnnotator = list[0].id
        }
      }
      this.loadAnnotatorResult(this.activeAnnotator)
    },
    parseAnnotationUser(ann) {
      const cu = ann.created_by
      if (cu && typeof cu === 'object') {
        return cu.first_name || cu.last_name || cu.username || (cu.email ? cu.email.split('@')[0] : '') || 'user'
      }
      if (typeof ann.created_username === 'string') {
        const trimmed = ann.created_username.trim()
        const parts = trimmed.split(',').map(p => p.trim()).filter(Boolean)
        const first = parts[0] || ''
        if (first.includes('@')) {
          return first.split('@')[0]
        }
        return first || 'user'
      }
      return `user-${ann.completed_by || 'unknown'}`
    },
    loadAnnotatorResult(annotatorId) {
      const item = this.annotators.find(a => a.id === annotatorId)
      if (!item) return
      this.activeAnnotator = annotatorId
      const result = {}
      if (item.type === 'current') {
        const firstPred = this.predictions[0]
        if (firstPred && Array.isArray(firstPred.result)) {
          firstPred.result.forEach(r => {
            const fromName = r.from_name
            const val = r.value
            if (!val) return
            if (val.choices) {
              const section = this.labelConfig.sections.find(s => s.name === fromName)
              if (section && section.choice === 'single') {
                result[fromName] = val.choices[0]
              } else {
                result[fromName] = val.choices
              }
            }
          })
        }
        this.annotationResult = result
        this.originalAnnotationResult = {}
        return
      }
      const sourceResult = item.source && item.source.result
      if (Array.isArray(sourceResult)) {
        sourceResult.forEach(r => {
          const fromName = r.from_name
          const val = r.value
          if (!val) return
          if (val.choices) {
            const section = this.labelConfig.sections.find(s => s.name === fromName)
            if (section && section.choice === 'single') {
              result[fromName] = val.choices[0]
            } else {
              result[fromName] = val.choices
            }
          }
        })
      }
      this.annotationResult = result
      this.originalAnnotationResult = JSON.parse(JSON.stringify(result))
    },
    formatTime(dateStr) {
      try {
        const d = new Date(dateStr)
        const now = new Date()
        const diff = now - d
        const min = Math.floor(diff / 60000)
        if (min < 1) return 'just now'
        if (min < 60) return `${min} minute${min > 1 ? 's' : ''} ago`
        const hour = Math.floor(min / 60)
        if (hour < 24) return `${hour} hour${hour > 1 ? 's' : ''} ago`
        const day = Math.floor(hour / 24)
        if (day < 30) return `${day} day${day > 1 ? 's' : ''} ago`
        return d.toLocaleDateString()
      } catch (e) {
        return dateStr
      }
    },
    avatarClass(item) {
      if (item.type === 'prediction') return 'avatar-prediction'
      if (item.type === 'current') return 'avatar-current'
      return 'avatar-user'
    },
    isChoiceChecked(section, choice) {
      const val = this.annotationResult[section.name]
      if (section.choice === 'single') {
        return val === choice.value
      }
      return Array.isArray(val) && val.includes(choice.value)
    },
    toggleChoice(section, choice) {
      if (section.choice === 'single') {
        if (this.annotationResult[section.name] === choice.value) {
          this.$delete(this.annotationResult, section.name)
        } else {
          this.$set(this.annotationResult, section.name, choice.value)
        }
      } else {
        const arr = this.annotationResult[section.name] || []
        const i = arr.indexOf(choice.value)
        if (i > -1) {
          arr.splice(i, 1)
        } else {
          arr.push(choice.value)
        }
        this.$set(this.annotationResult, section.name, [...arr])
      }
    },
    skipTask() {
      if (this.selectedTaskIndex < this.tasks.length - 1) {
        this.handleRowClick(this.tasks[this.selectedTaskIndex + 1])
      } else {
        this.$message.info('已是最后一条任务')
      }
    },
    getCurrentUserEmail() {
      const userInfo = (window.globalCache && window.globalCache.userInfo) || {}
      if (userInfo.email) return userInfo.email
      const userName = userInfo.userName || this.currentUser
      return `${userName}@llmops.local`
    },
    isAnnotationOwnedByCurrentUser(ann) {
      if (!ann) return false
      const email = this.getCurrentUserEmail()
      if (ann.created_by && typeof ann.created_by === 'object' && ann.created_by.email) {
        return ann.created_by.email === email
      }
      if (typeof ann.created_username === 'string') {
        const parts = ann.created_username.split(',').map(p => p.trim()).filter(Boolean)
        return parts[0] === email
      }
      return false
    },
    async getLabelStudioToken() {
      const email = this.getCurrentUserEmail()
      const cacheKey = `ls_token_${email}`
      const cached = localStorage.getItem(cacheKey)
      if (cached) return cached
      try {
        const res = await loginLabelStudio({ email, password: this.lsDefaultPassword })
        const token = res && res.token
        if (token) {
          localStorage.setItem(cacheKey, token)
          return token
        }
        throw new Error('登录返回数据异常')
      } catch (e) {
        try {
          await registerLabelStudio({ email, password: this.lsDefaultPassword })
        } catch (regErr) {
          // 注册失败可能是用户已存在，忽略后继续尝试登录
        }
        const res = await loginLabelStudio({ email, password: this.lsDefaultPassword })
        const token = res && res.token
        if (token) {
          localStorage.setItem(cacheKey, token)
          return token
        }
        throw new Error('Label Studio 登录失败')
      }
    },
    buildAnnotationPayload() {
      const result = []
      const toName = this.labelConfig.textFields.length ? this.labelConfig.textFields[0].name : 'text'
      this.labelConfig.sections.forEach(section => {
        const val = this.annotationResult[section.name]
        if (val === undefined || val === null || val === '') return
        let choices
        if (section.choice === 'single') {
          choices = [val]
        } else {
          choices = Array.isArray(val) ? val : [val]
        }
        if (choices.length === 0) return
        result.push({
          from_name: section.name,
          to_name: toName,
          type: 'choices',
          value: { choices }
        })
      })
      return {
        result,
        lead_time: 0,
        was_cancelled: false,
        ground_truth: false
      }
    },
    async submitAnnotation() {
      if (Object.keys(this.annotationResult).length === 0) {
        this.$message.warning('请先选择标注结果')
        return
      }
      try {
        const token = await this.getLabelStudioToken()
        if (!token) {
          this.$message.error('获取 Label Studio 用户凭证失败')
          return
        }
        const payload = this.buildAnnotationPayload()
        if (payload.result.length === 0) {
          this.$message.warning('请先选择标注结果')
          return
        }
        const currentTaskId = this.selectedTask.id
        await createTaskAnnotation(currentTaskId, payload, token)
        this.$message.success('标注提交成功')
        await this.loadTasks()
        const currentIndex = this.tasks.findIndex(t => t.id === currentTaskId)
        if (currentIndex > -1 && currentIndex < this.tasks.length - 1) {
          this.handleRowClick(this.tasks[currentIndex + 1])
        } else if (currentIndex === this.tasks.length - 1) {
          this.$message.info('已是最后一条任务')
        }
      } catch (e) {
        console.error('提交标注失败:', e)
        const email = this.getCurrentUserEmail()
        localStorage.removeItem(`ls_token_${email}`)
        this.$message.error('标注提交失败，请重试')
      }
    },
    async updateAnnotationResult() {
      const item = this.activeAnnotatorItem
      if (!item || !item.source) return
      const annId = item.source.id
      if (!annId) {
        this.$message.warning('未找到标注记录 ID')
        return
      }
      const payload = this.buildAnnotationPayload()
      if (payload.result.length === 0) {
        this.$message.warning('请先选择标注结果')
        return
      }
      try {
        const token = await this.getLabelStudioToken()
        if (!token) {
          this.$message.error('获取 Label Studio 用户凭证失败')
          return
        }
        await updateAnnotation(annId, payload, token)
        item.source.result = payload.result
        this.originalAnnotationResult = JSON.parse(JSON.stringify(this.annotationResult))
        this.$message.success('标注更新成功')
      } catch (e) {
        console.error('更新标注失败:', e)
        const email = this.getCurrentUserEmail()
        localStorage.removeItem(`ls_token_${email}`)
        this.$message.error('标注更新失败，请重试')
      }
    },
    handleSelectionChange(rows) {
      this.selectedTasks = rows
    },
    toggleColumn(key) {
      const idx = this.visibleColumns.indexOf(key)
      if (idx > -1) {
        this.visibleColumns.splice(idx, 1)
      } else {
        this.visibleColumns.push(key)
      }
    },
    handleActions(cmd) {
      if (!this.selectedTasks.length) {
        this.$message.warning('请先选择任务')
        return
      }
      this.$message.info(`${cmd}: ${this.selectedTasks.length} 个任务`)
    },
    handleStatusChange() {
      this.page = 1
      this.loadTasks()
    },
    addTab() {
      this.$message.info('新建标签页')
    },
    addCurrentAnnotationTab() {
      if (!this.selectedTask) return
      const userName = this.currentUser
      const newTab = {
        id: `current-user-${Date.now()}`,
        type: 'current',
        name: userName,
        initial: (userName || 'u').charAt(0).toUpperCase(),
        time: '',
        score: null
      }
      this.annotators.push(newTab)
      this.activeAnnotator = newTab.id
      this.annotationResult = {}
      this.originalAnnotationResult = {}
    },
    handleAnnotationCommand(command, item) {
      switch (command) {
        case 'copyId':
          this.copyAnnotationId(item)
          break
        case 'duplicate':
          this.duplicateAnnotation(item)
          break
        case 'copyLink':
          this.copyAnnotationLink(item)
          break
        case 'showOthers':
          this.showOtherAnnotations(item)
          break
        case 'delete':
          this.deleteAnnotationItem(item)
          break
      }
    },
    copyAnnotationId(item) {
      const id = item.source && item.source.id
      if (!id) return
      this.copyToClipboard(String(id))
      this.$message.success('Annotation ID 已复制到剪贴板')
    },
    copyAnnotationLink(item) {
      const id = item.source && item.source.id
      if (!id) return
      const url = `${window.location.origin}/labelstudio/annotations/${id}`
      this.copyToClipboard(url)
      this.$message.success('Annotation Link 已复制到剪贴板')
    },
    copyToClipboard(text) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).catch(() => this.fallbackCopy(text))
      } else {
        this.fallbackCopy(text)
      }
    },
    fallbackCopy(text) {
      const ta = document.createElement('textarea')
      ta.value = text
      ta.style.position = 'fixed'
      ta.style.left = '-9999px'
      document.body.appendChild(ta)
      ta.select()
      try { document.execCommand('copy') } catch (e) {}
      document.body.removeChild(ta)
    },
    duplicateAnnotation(item) {
      if (item.type !== 'annotation' || !item.source) return
      const userName = this.currentUser
      const newTab = {
        id: `current-user-${Date.now()}`,
        type: 'current',
        name: userName,
        initial: (userName || 'u').charAt(0).toUpperCase(),
        time: '',
        score: null
      }
      this.annotators.push(newTab)
      this.activeAnnotator = newTab.id
      const result = {}
      const sourceResult = item.source.result
      if (Array.isArray(sourceResult)) {
        sourceResult.forEach(r => {
          const fromName = r.from_name
          const val = r.value
          if (!val || !val.choices) return
          const section = this.labelConfig.sections.find(s => s.name === fromName)
          if (section && section.choice === 'single') {
            result[fromName] = val.choices[0]
          } else {
            result[fromName] = val.choices
          }
        })
      }
      this.annotationResult = result
      this.originalAnnotationResult = {}
      this.$message.success('已复制标注内容到新的标注面板')
    },
    showOtherAnnotations() {
      this.$message.info('Show Other Annotations 功能开发中')
    },
    async deleteAnnotationItem(item) {
      if (item.type === 'current') {
        this.annotators = this.annotators.filter(a => a.id !== item.id)
        if (this.activeAnnotator === item.id) {
          const first = this.annotators[0]
          if (first) {
            this.activeAnnotator = first.id
            this.loadAnnotatorResult(first.id)
          } else {
            this.activeAnnotator = null
            this.annotationResult = {}
          }
        }
        return
      }
      const annId = item.source && item.source.id
      if (!annId) {
        this.$message.warning('未找到标注记录 ID')
        return
      }
      const isPrediction = item.type === 'prediction'
      const confirmText = isPrediction
        ? '确定要删除该 AI 预测结果吗？'
        : '确定要删除该标注记录吗？'
      try {
        await this.$confirm(confirmText, '提示', {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch (e) {
        return
      }
      try {
        if (isPrediction) {
          await deletePrediction(annId)
          this.predictions = this.predictions.filter(p => p.id !== annId)
        } else {
          await deleteAnnotation(annId)
          this.annotations = this.annotations.filter(a => a.id !== annId)
        }
        this.annotators = this.annotators.filter(a => a.id !== item.id)
        const task = this.tasks.find(t => t.id === this.selectedTask.id)
        if (task) {
          if (isPrediction) {
            task.predicted = Math.max((task.predicted || 0) - 1, 0)
          } else if (task.completed) {
            task.completed = Math.max(task.completed - 1, 0)
          }
        }
        if (this.activeAnnotator === item.id) {
          const remaining = this.annotators
          if (remaining.length > 0) {
            const firstPred = remaining.find(a => a.type === 'prediction')
            const target = remaining.find(a => a.type === 'annotation') || firstPred || remaining[0]
            this.activeAnnotator = target.id
            this.loadAnnotatorResult(target.id)
          } else {
            this.activeAnnotator = null
            this.annotationResult = {}
          }
        }
        this.$message.success(isPrediction ? 'AI 预测结果已删除' : '标注记录已删除')
      } catch (e) {
        console.error('删除失败:', e)
        this.$message.error(isPrediction ? '删除 AI 预测结果失败，请重试' : '删除标注记录失败，请重试')
      }
    },
    goSettings() {
      this.$router.push({
        path: `/llmops/datasets/annotation/projects/${this.projectId}/settings`
      })
    },
    goToLabelingSetup() {
      this.noLabelConfigDialogVisible = false
      this.$router.push({
        path: `/llmops/datasets/annotation/projects/${this.projectId}/settings`,
        query: { tab: 'labeling' }
      })
    },
    handleBack() {
      this.$router.push('/llmops/datasets/annotation')
    },
    async handleExport() {
      this.exportDialogVisible = true
      this.exportFormats = []
      this.selectedExportFormat = ''
      this.exportFormatsLoading = true
      try {
        const res = await getProjectExportFormats(this.projectId)
        this.exportFormats = res.formats || []
        const firstAvailable = this.exportFormats.find(f => !f.disabled)
        if (firstAvailable) {
          this.selectedExportFormat = firstAvailable.name
        }
      } catch (e) {
        console.error('[任务列表] 获取导出格式列表失败:', e)
        this.$message.error('获取导出格式列表失败')
      } finally {
        this.exportFormatsLoading = false
      }
    },
    async confirmExport() {
      if (!this.selectedExportFormat) return
      this.exportLoading = true
      try {
        const res = await exportProjectData(this.projectId, this.selectedExportFormat)
        const blob = res.data
        const disposition = res.headers['content-disposition'] || ''
        let fileName = ''
        const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
        const plainMatch = disposition.match(/filename="?([^";]+)"?/i)
        if (utf8Match) {
          fileName = decodeURIComponent(utf8Match[1])
        } else if (plainMatch) {
          fileName = plainMatch[1]
        } else {
          const ext = (this.selectedExportFormat || 'json').toLowerCase().replace(/_/g, '-')
          fileName = `${this.projectName || 'project'}-${ext}`
        }
        const fmt = this.exportFormats.find(f => f.name === this.selectedExportFormat)
        const url = window.URL.createObjectURL(new Blob([blob]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success(`已导出为 ${fmt ? fmt.title : this.selectedExportFormat} 格式`)
        this.exportDialogVisible = false
      } catch (e) {
        console.error('[任务列表] 导出数据失败:', e)
        this.$message.error('导出数据失败，请稍后重试')
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.tasks-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.tasks-content {
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #374151;
  transition: all 0.2s ease;

  &:hover {
    color: #2563eb;
    border-color: #bfdbfe;
    background: #eff6ff;
  }

  i {
    font-size: 16px;
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;

  .breadcrumb-item {
    cursor: pointer;
    &:hover {
      color: #2563eb;
    }
    &.active {
      color: #1f2937;
      font-weight: 600;
      cursor: default;
    }
  }

  .breadcrumb-separator {
    color: #d1d5db;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #374151;
  border: 1px solid #e5e7eb;
  background: #fff;

  &.icon-btn {
    padding: 8px 10px;
  }

  &:hover {
    color: #2563eb;
    border-color: #bfdbfe;
  }
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #f3f4f6;
  border-radius: 16px;
  font-size: 13px;
  color: #374151;

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #10b981;
  }
}

.header-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #8b5cf6;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.main-layout {
  display: flex;
  gap: 0;
  flex: 1;
  min-height: 0;
}

.left-panel {
  width: 100%;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  min-height: 0;

  &.collapsed {
    width: 38%;
    min-width: 320px;
  }
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-left: none;
  border-radius: 0 8px 8px 0;
  overflow: hidden;
  min-height: 0;
}

/* 标签页栏 */
.tabs-bar {
  display: flex;
  align-items: center;
  gap: 2px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-bottom: none;
  border-radius: 8px 8px 0 0;
  padding: 6px 8px 0;

  .tab-item {
    padding: 8px 20px;
    background: #fff;
    border: 1px solid #e5e7eb;
    border-bottom: none;
    border-radius: 6px 6px 0 0;
    font-size: 13px;
    color: #6b7280;
    cursor: pointer;

    &.active {
      color: #1f2937;
      font-weight: 600;
    }
  }

  .tab-add {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    cursor: pointer;
    color: #6b7280;

    &:hover {
      background: #e5e7eb;
      color: #2563eb;
    }
  }
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-top: none;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #374151;
  border: 1px solid #e5e7eb;
  background: #fff;
  font-size: 13px;

  &.icon-btn {
    padding: 8px 10px;
  }

  &:hover {
    color: #2563eb;
    border-color: #bfdbfe;
  }
}

.settings-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-select {
  width: 130px;
}

/* 表格 */
.table-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-top: none;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.task-id {
  font-family: 'SF Mono', Monaco, monospace;
  color: #374151;
}

.stat-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 14px;

  &.completed {
    color: #10b981;
  }

  &.annotation {
    color: #3b82f6;
  }

  &.cancelled {
    color: #ef4444;
  }

  &.predicted {
    color: #8b5cf6;
  }
}

.stat-value {
  font-size: 13px;
  color: #374151;
}

.completed-time {
  font-size: 13px;
  color: #374151;
  white-space: nowrap;
}

.annotated-by {
  font-size: 13px;
  color: #6b7280;
}

.type-tag {
  margin-left: 6px;
  font-weight: normal;
}

.data-text {
  font-size: 13px;
  color: #374151;
}

.pagination-wrapper {
  padding: 16px;
  display: flex;
  justify-content: flex-end;
}

::v-deep .current-row {
  background-color: #dbeafe !important;
}

/* 标注面板 */
.annotation-top {
  padding: 8px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #fafafa;
}

.compare-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  white-space: nowrap;
  padding: 2px 0;
}

.compare-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #374151;
  border: 1px solid #e5e7eb;
  background: #fff;
  font-size: 13px;

  &.icon-btn {
    padding: 8px 10px;
  }

  &:hover {
    color: #2563eb;
    border-color: #bfdbfe;
  }
}

.annotator-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;

  &.is-prediction {
    border-style: dashed;
    border-color: #c7d2fe;
  }

  &.active {
    border-color: #2563eb;
    background: #eff6ff;
  }

  &:hover {
    border-color: #bfdbfe;
  }
}

.annotator-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;

  &.avatar-user {
    background: #10b981;
  }

  &.avatar-current {
    background: #2563eb;
  }

  &.avatar-prediction {
    background: #8b5cf6;
  }
}

.annotator-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.annotator-name {
  font-size: 12px;
  font-weight: 600;
  color: #1f2937;
}

.annotator-time {
  font-size: 11px;
  color: #9ca3af;
}

.annotator-action {
  color: #6366f1;
  cursor: pointer;
}

/* 数据内容区 */
.annotation-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #fff;
}

.data-content {
  margin-bottom: 24px;
}

.data-field {
  margin-bottom: 16px;
}

.field-label {
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.field-value {
  font-size: 16px;
  color: #1f2937;
  line-height: 1.6;
}

/* 标注卡片 */
.annotation-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-top: 16px;
  background: #fff;
}

/* AI 预测加载动画 */
.ai-loading-wrap {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.ai-loading-bar {
  width: 160px;
  height: 10px;
  border-radius: 5px;
  background: repeating-linear-gradient(
    45deg,
    #3b82f6,
    #3b82f6 8px,
    #dbeafe 8px,
    #dbeafe 16px
  );
  background-size: 200% 100%;
  animation: ai-loading-stripes 1s linear infinite;
}

.ai-loading-text {
  margin-top: 14px;
  font-size: 13px;
  color: #6b7280;
}

@keyframes ai-loading-stripes {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: -32px 0;
  }
}

.annotation-header {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 20px;
}

.annotation-choices {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}

.choice-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  user-select: none;

  input[type='checkbox'] {
    width: 16px;
    height: 16px;
    cursor: pointer;
  }

  .choice-text {
    font-weight: 500;
  }

  .choice-hotkey {
    font-size: 11px;
    color: #9ca3af;
    border: 1px solid #e5e7eb;
    border-radius: 3px;
    padding: 0 4px;
    line-height: 1.4;
  }

  &.is-readonly {
    cursor: default;

    input[type='checkbox'] {
      cursor: not-allowed;
    }
  }
}

/* 底部操作栏 */
.annotation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-id-badge {
  font-size: 13px;
  color: #6b7280;
  font-family: 'SF Mono', Monaco, monospace;
  margin-right: 8px;
}

.footer-btn {
  color: #6b7280;
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 8px 10px;

  &:hover {
    color: #2563eb;
    border-color: #bfdbfe;
  }
}

.submit-btn {
  padding: 10px 32px;
  font-size: 14px;
  font-weight: 600;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  text-align: center;
  flex: 1;
}

.empty-icon-circle {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;

  i {
    font-size: 36px;
    color: #2563eb;
  }
}

.empty-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.empty-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 28px 0;
  line-height: 1.6;
}

.cloud-icons {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
}

.cloud-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 36px;
    color: #9ca3af;
  }
}

.empty-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.empty-action-btn {
  min-width: 140px;
  height: 40px;
  font-size: 14px;
}

.empty-docs-link {
  font-size: 13px;
  color: #2563eb;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &:hover {
    text-decoration: underline;
  }

  i {
    font-size: 12px;
  }
}

/* 底部统计栏 */
.stats-footer {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-top: none;
  border-radius: 0 0 8px 8px;
  font-size: 12px;
  color: #6b7280;
}

.stats-item {
  white-space: nowrap;
}

.export-data-content {
  padding: 8px 4px;
}

.export-data-desc {
  font-size: 14px;
  color: #374151;
  margin: 0 0 16px;
}

.export-format-list {
  max-height: 480px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.export-format-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #3b82f6;
    background-color: #f9fafb;
  }

  &.selected {
    border-color: #3b82f6;
    background-color: #eff6ff;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;

    &:hover {
      border-color: #e5e7eb;
      background-color: #fff;
    }
  }
}

.export-format-radio {
  margin-right: 12px;
  padding-top: 2px;
}

.export-format-info {
  flex: 1;
  min-width: 0;
}

.export-format-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.export-format-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 6px;
}

.export-format-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.export-format-tag {
  display: inline-block;
  font-size: 11px;
  color: #3b82f6;
  background-color: #eff6ff;
  padding: 2px 8px;
  border-radius: 10px;
}

.export-data-footer {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 8px;

  a {
    color: #3b82f6;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.export-data-tip {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.no-label-config-content {
  padding: 8px 4px 4px;
}

.no-label-config-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px 0;
}

.no-label-config-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

.no-label-config-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.annotated-by-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
}

.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  cursor: default;
  flex-shrink: 0;
}

.annotated-empty {
  display: inline-block;
  width: 0;
  height: 0;
}
</style>