<template>
  <div class="storage-container">
    <div class="storage-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">存储管理</h1>
          <p class="page-subtitle">管理存储卷、存储池和存储策略</p>
        </div>
        <div class="header-actions">
          <el-button class="export-btn" @click="exportCsv">
            <i class="el-icon-download"></i> 导出CSV
          </el-button>
          <el-button type="primary" class="create-btn" @click="showCreateDialog">
            <i class="el-icon-plus"></i> 创建存储卷
          </el-button>
        </div>
      </div>

      <div class="metric-grid">
        <div class="metric-card blue">
          <div class="metric-main">
            <span class="metric-label">总存储容量</span>
            <strong>{{ overview.totalCapacity }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-coin"></i></div>
          <div class="metric-progress">
            <span :style="{ width: overview.usedPercent + '%' }"></span>
          </div>
          <div class="metric-foot">已使用 {{ overview.usedCapacity }}</div>
        </div>
        <div class="metric-card green">
          <div class="metric-main">
            <span class="metric-label">存储卷数量</span>
            <strong>{{ overview.pvcNums }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-folder-opened"></i></div>
          <div class="metric-tags">
            <span>运行中 {{ overview.boundPvcNums }}</span>
            <span>待分配 {{ overview.pendingPvcNums }}</span>
          </div>
        </div>
        <div class="metric-card purple">
          <div class="metric-main">
            <span class="metric-label">存储池</span>
            <strong>{{ overview.poolCount || 0 }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-s-data"></i></div>
        </div>
        <div class="metric-card orange">
          <div class="metric-main">
            <span class="metric-label">数据快照</span>
            <strong>{{ overview.snapshotCount || 0 }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-camera-solid"></i></div>
          <!-- <div class="metric-foot strong">最近 7 天 · 自动备份</div> -->
        </div>
      </div>

      <div class="table-card" v-loading="listLoading" element-loading-text="加载中...">
        <div class="toolbar">
          <el-input v-model="keyword" class="search-input" clearable prefix-icon="el-icon-search"
            placeholder="搜索存储卷名称..." />
          <div class="status-tabs">
            <button v-for="item in statusOptions" :key="item.value" :class="{ active: statusFilter === item.value }"
              @click="statusFilter = item.value">{{ item.label }}</button>
          </div>
          <el-button plain @click="openFilterDialog">
            <i class="el-icon-s-operation"></i> 筛选
          </el-button>
          <el-button plain @click="toggleSort">
            <i class="el-icon-sort"></i> 排序
          </el-button>
          <div class="toolbar-spacer"></div>
          <el-checkbox v-model="allChecked" @change="toggleAll">全选</el-checkbox>
          <el-button type="text" class="danger-action" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            <i class="el-icon-delete-solid"></i> 批量删除
          </el-button>
        </div>

        <el-table ref="storageTable" :data="pagedVolumes"
          :header-cell-style="{ backgroundColor: '#f9fafb', color: '#4b5563' }" row-key="id" style="width: 100%"
          @selection-change="handleSelectionChange" @expand-change="handleExpandChange">
          <el-table-column type="selection" width="56"></el-table-column>
          <el-table-column type="expand" width="40">
            <template slot-scope="scope">
              <div class="snapshot-expand">
                <div class="snapshot-header">
                  <span class="snapshot-title">数据快照</span>
                  <el-button type="text" size="small" :loading="snapshotLoadingRows[scope.row.id]"
                    @click="refreshSnapshots(scope.row)">刷新</el-button>
                </div>
                <el-table :data="snapshotMap[scope.row.id] || []" v-loading="snapshotLoadingRows[scope.row.id]"
                  size="small" class="snapshot-table" border>
                  <el-table-column label="名称">
                    <template slot-scope="s">
                      <span>{{ scope.row.name }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="快照ID" prop="snapshot_id">
                    <template slot-scope="s">
                      <span class="snapshot-id">{{ s.row.snapshot_id }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="创建时间" prop="create_time">
                    <template slot-scope="s">{{ formatSnapshotTime(s.row.create_time) }}</template>
                  </el-table-column>
                  <el-table-column label="操作" fixed="right">
                    <template slot-scope="s">
                      <el-button type="text" size="small" @click="openRestoreDialog(scope.row, s.row)">恢复</el-button>
                      <el-button type="text" size="small" class="danger-action"
                        @click="handleDeleteSnapshot(scope.row, s.row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div
                  v-if="!snapshotLoadingRows[scope.row.id] && !(snapshotMap[scope.row.id] && snapshotMap[scope.row.id].length)"
                  class="snapshot-empty">暂无快照</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="名称" min-width="200">
            <template slot-scope="scope">
              <div class="volume-name">
                <span class="volume-icon"><i class="el-icon-folder"></i></span>
                <div>
                  <div class="name-text">{{ scope.row.name }}</div>
                  <div class="namespace-text">{{ scope.row.namespace }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="storageClass" label="存储类" min-width="150">
            <template slot-scope="scope">
              <span class="class-badge">{{ scope.row.storageClass }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="accessMode" label="访问模式" width="120"></el-table-column>
          <el-table-column prop="capacity" label="容量" width="110"></el-table-column>
          <el-table-column label="状态" width="130">
            <template slot-scope="scope">
              <span class="status-badge" :class="scope.row.status.toLowerCase()">
                <i></i>{{ scope.row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="190"></el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewDetail(scope.row)">详情</el-button>
              <el-button type="text" size="small" @click="openFileBrowser(scope.row)">文件浏览器</el-button>
              <el-button type="text" size="small" @click="backupVolume(scope.row)">备份</el-button>
              <el-button type="text" size="small" @click="cleanPod(scope.row)">清理</el-button>
              <el-button type="text" size="small" class="danger-action" @click="deleteVolume(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <span class="total-items">
            显示 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, filteredVolumes.length) }} 条，共
            {{
              filteredVolumes.length }} 条
          </span>
          <el-pagination background @size-change="handleSizeChange" @current-change="handlePageChange"
            :current-page="currentPage" :page-sizes="[10, 20, 50]" :page-size="pageSize"
            layout="total, sizes, prev, pager, next" :total="filteredVolumes.length" />
        </div>
      </div>
    </div>

    <el-dialog :visible.sync="createDialogVisible" width="720px" custom-class="pvc-dialog" :show-close="false">
      <div class="pvc-dialog-header">
        <h2>添加PVC</h2>
        <button class="pvc-close" @click="createDialogVisible = false">
          <i class="el-icon-close"></i>
        </button>
      </div>
      <el-form ref="volumeForm" :model="volumeForm" :rules="rules" class="pvc-form">
        <el-form-item prop="name">
          <template slot="label">名称</template>
          <el-input v-model="volumeForm.name" maxlength="50" show-word-limit placeholder="请输入小写字母、数字、中划线"></el-input>
        </el-form-item>
        <el-form-item prop="storageClass">
          <template slot="label">存储类</template>
          <el-select v-model="volumeForm.storageClass" style="width: 100%">
            <el-option label="local-path" value="local-path"></el-option>
            <el-option label="nfs-storage-quota" value="nfs-storage-quota"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="读写模式" prop="accessMode">
          <el-select v-model="volumeForm.accessMode" style="width: 100%">
            <el-option
              v-for="item in volumeAccessModeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="capacity">
          <template slot="label"> 容量</template>
          <el-input v-model="volumeForm.capacity" placeholder="输入容量">
            <span slot="suffix" class="capacity-example">例如: 10Gi</span>
          </el-input>
        </el-form-item>
      </el-form>
      <div class="pvc-dialog-footer">
        <el-button class="pvc-cancel" @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" class="pvc-confirm" :loading="createLoading" @click="createVolume">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="筛选" :visible.sync="filterDialogVisible" width="420px">
      <el-form label-width="90px">
        <el-form-item label="存储类">
          <el-select v-model="tempClassFilter" clearable placeholder="全部" style="width: 100%">
            <el-option label="local-path" value="local-path"></el-option>
            <el-option label="nfs-storage-quota" value="nfs-storage-quota"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="访问模式">
          <el-select v-model="tempAccessFilter" clearable placeholder="全部" style="width: 100%">
            <el-option label="只能被单节点读写" value="ReadWriteOnce"></el-option>
            <el-option label="可被多节点只读" value="ReadOnlyMany"></el-option>
            <el-option label="可被多节点读写" value="ReadWriteMany"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="applyFilters">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="detailDialogVisible" width="720px" custom-class="storage-detail-dialog"
      :show-close="false">
      <div class="detail-dialog-header">
        <h2>存储卷详情</h2>
        <button class="detail-close" @click="detailDialogVisible = false">
          <i class="el-icon-close"></i>
        </button>
      </div>
      <div v-if="currentVolume" class="storage-detail-body" v-loading="detailLoading" element-loading-text="加载中...">
        <div class="detail-summary">
          <div class="volume-name">
            <span class="volume-icon"><i class="el-icon-folder"></i></span>
            <div>
              <div class="name-text">{{ currentVolume.name }}</div>
              <div class="namespace-text">Namespace: {{ currentVolume.namespace }}</div>
            </div>
          </div>
          <span class="status-badge" :class="currentVolume.status.toLowerCase()">
            {{ currentVolume.status }}
          </span>
        </div>

        <div class="detail-grid">
          <div class="detail-field">
            <label>存储类</label>
            <div>{{ currentVolume.storageClass }}</div>
          </div>
          <div class="detail-field">
            <label>访问模式</label>
            <div>{{ getAccessModeText(currentVolume.accessMode) }}</div>
          </div>
          <div class="detail-field">
            <label>容量</label>
            <div>{{ currentVolume.capacity }}</div>
          </div>
          <div class="detail-field">
            <label>绑定卷</label>
            <div>{{ currentVolume.volumeName }}</div>
          </div>
        </div>

        <div class="detail-section">
          <h3>Annotations</h3>
          <pre class="annotations-code">{{ currentVolume.annotations }}</pre>
        </div>
      </div>
      <div v-if="currentVolume" class="storage-detail-footer">
        <span>创建时间: {{ currentVolume.createdAt }}</span>
        <div>
          <el-button class="detail-cancel" @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" class="detail-primary" @click="openFileBrowser(currentVolume)">打开文件浏览器</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="fileBrowserVisible" width="760px" custom-class="file-browser-dialog" :show-close="false">
      <div class="browser-window">
        <div class="browser-topbar">
          <div class="browser-nav">
            <button><i class="el-icon-back"></i></button>
            <button><i class="el-icon-right"></i></button>
            <span>{{ browserTitle }}</span>
          </div>
          <div class="browser-actions">
              <button title="刷新" @click="refreshFileBrowser"><i class="el-icon-refresh"></i></button>
              <button title="主页" @click="goHomeInBrowser"><i class="el-icon-s-home"></i></button>
              <button title="下载" :disabled="!selectedBrowserFile" @click="downloadBrowserFile"><i class="el-icon-download"></i></button>
              <button title="上传" @click="triggerBrowserUpload"><i class="el-icon-upload2"></i></button>
              <input ref="browserFileInput" type="file" style="display:none" @change="onBrowserFileSelected" />
              <button title="关闭" @click="fileBrowserVisible = false"><i class="el-icon-close"></i></button>
            </div>
          </div>

          <div v-if="browserUpload.uploading" class="browser-upload-bar">
            <i class="el-icon-loading"></i>
            <span class="browser-upload-name">{{ browserUpload.fileName }}</span>
            <el-progress :percentage="browserUpload.percent" :status="browserUpload.status" :stroke-width="10" style="flex:1;margin:0 12px"></el-progress>
            <span class="browser-upload-text">{{ browserUpload.statusText }}</span>
            <el-button size="mini" plain @click="cancelBrowserUpload">取消</el-button>
          </div>

        <div class="browser-main">
          <aside class="browser-sidebar">
            <div class="browser-user">
              <span><i class="el-icon-user-solid"></i></span>
              <div>
                <strong>My files</strong>
                <em>{{ browserUsage }} used</em>
              </div>
            </div>
            <button><i class="el-icon-folder-add"></i> New folder</button>
            <button><i class="el-icon-document-add"></i> New file</button>
            <div class="browser-divider"></div>
            <button class="active"><i class="el-icon-s-tools"></i> Settings</button>
            <button><i class="el-icon-question"></i> Help</button>
          </aside>

          <section class="browser-content">
            <el-input v-model="fileKeyword" class="browser-search" prefix-icon="el-icon-search" placeholder="Search..."
              clearable></el-input>

            <div class="browser-section-title">FOLDERS</div>
            <div class="file-grid">
              <div v-for="folder in filteredFolders" :key="folder.name" class="file-card folder-card">
                <div class="file-thumb"><i class="el-icon-folder"></i></div>
                <strong>{{ folder.name }}</strong>
                <span>{{ folder.modified }}</span>
              </div>
            </div>

            <div class="browser-section-title files-title">FILES</div>
            <div class="file-grid">
              <div v-for="file in filteredFiles" :key="file.name" class="file-card"
                :class="{ active: selectedBrowserFile && selectedBrowserFile.name === file.name }"
                @click="selectBrowserFile(file)">
                <div class="file-thumb file"><i class="el-icon-document"></i></div>
                <strong>{{ file.name }}</strong>
                <span>{{ file.size }}</span>
              </div>
            </div>
          </section>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="恢复快照" :visible.sync="restoreDialogVisible" width="420px" @close="restoreForm.sourcePvcName = ''">
      <el-form ref="restoreFormRef" :model="restoreForm" :rules="restoreRules" label-width="90px">
        <el-form-item label="快照ID">
          <span class="snapshot-id">{{ restoreForm.snapshotId }}</span>
        </el-form-item>
        <el-form-item label="目标PVC" prop="sourcePvcName">
          <el-input v-model.trim="restoreForm.sourcePvcName" placeholder="请输入目标PVC名称" clearable></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="restoreDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="restoreLoading" @click="confirmRestore">确定</el-button>
      </div>
    </el-dialog>

    <FileDialog ref="fileDialog" />
  </div>
</template>

<script>
import { getStorageList, createStorageVolume, deleteStorageVolume, getStorageOverview, getSnapshotList, deleteSnapshot, restoreSnapshot } from '@/api/llmops/storage'
import { createPod, deletePod, getPvcViewerOverview, backupPvc } from '@/api/app/k8s'
import {
  PART_SIZE,
  initProjectFileUpload,
  getProjectFilePresignedUrl,
  completeProjectFileUpload,
  notifyProjectFilePartComplete,
  abortProjectFileUpload,
  uploadProjectFilePartToS3,
  getProjectFileDownloadUrl,
  uploadProjectSmallFile
} from '@/api/llmops/storage-file'
import FileDialog from './fileDialog.vue'

const ACCESS_MODE_OPTIONS = [
  { label: '只能被单节点读写', value: 'ReadWriteOnce' },
  { label: '可被多节点只读', value: 'ReadOnlyMany' },
  { label: '可被多节点读写', value: 'ReadWriteMany' }
]

export default {
  name: 'StorageManagement',
  components: { FileDialog },
  data() {
    return {
      keyword: '',
      statusFilter: 'all',
      classFilter: '',
      accessFilter: '',
      tempClassFilter: '',
      tempAccessFilter: '',
      sortAsc: true,
      selectedIds: [],
      createDialogVisible: false,
      filterDialogVisible: false,
      detailDialogVisible: false,
      fileBrowserVisible: false,
      currentVolume: null,
      currentBrowserVolume: null,
      fileKeyword: '',
      overview: {
        totalCapacity: '--',
        usedCapacity: '--',
        usedPercent: 0,
        pvcNums: '--',
        boundPvcNums: '--',
        pendingPvcNums: '--',
        poolCount: 0,
        snapshotCount: 0
      },
      listLoading: false,
      createLoading: false,
      detailLoading: false,
      deleteLoading: false,
      volumeForm: {
        name: '',
        namespace: 'default',
        storageClass: 'local-path',
        accessMode: 'ReadWriteOnce',
        capacity: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { pattern: /^[a-z0-9-]+$/, message: '仅支持小写字母、数字、中划线', trigger: 'blur' }
        ],
        storageClass: [{ required: true, message: '请选择存储类', trigger: 'change' }],
        accessMode: [{ required: true, message: '请选择访问模式', trigger: 'change' }],
        capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
      },
      currentPage: 1,
      pageSize: 10,
      statusOptions: [
        { label: '全部', value: 'all' },
        { label: 'Bound', value: 'Bound' },
        { label: 'Pending', value: 'Pending' },
        { label: 'Failed', value: 'Failed' }
      ],
      volumes: [],
      snapshotMap: {},
      snapshotLoadingRows: {},
      restoreDialogVisible: false,
      restoreLoading: false,
      restoreForm: { pvcName: '', snapshotId: '', sourcePvcName: '' },
      restoreRules: {
        sourcePvcName: [{ required: true, message: '请输入目标PVC名称', trigger: 'blur' }]
      },
      browserFolders: [
        { name: '_cache', modified: '5 months ago' },
        { name: '.conda', modified: '5 months ago' },
        { name: '.ipynb_checkpoints', modified: '5 months ago' },
        { name: '.ipyhon', modified: '5 months ago' },
        { name: '.jupyter', modified: '5 months ago' },
        { name: 'keras', modified: '5 months ago' },
        { name: 'local', modified: '5 months ago' },
        { name: 'log', modified: '5 months ago' }
      ],
      browserFiles: [
        { name: 'model.h5', size: '54.98 MB' },
        { name: 'model.json', size: '126.84 KB' },
        { name: 'model_weights.h5', size: '54.5 MB' },
        { name: 'Untitled.ipynb', size: '38.3 KB' }
      ],
      selectedBrowserFile: null,
      browserUpload: {
        uploading: false,
        aborted: false,
        percent: 0,
        status: '',
        statusText: '',
        fileName: '',
        objectKey: '',
        uploadId: ''
      }
    }
  },
  computed: {
    storagePercent() {
      return 42
    },
    boundCount() {
      return this.volumes.filter(item => item.status === 'Bound').length
    },
    pendingCount() {
      return this.volumes.filter(item => item.status === 'Pending').length
    },
    browserTitle() {
      const name = this.currentBrowserVolume ? this.currentBrowserVolume.name : 'srv'
      return `${name} - Files - File Browser`
    },
    browserUsage() {
      if (!this.currentBrowserVolume) return '0 MB'
      return this.currentBrowserVolume.capacity === '20Gi' ? '215.08 MB' : '512.24 MB'
    },
    volumeAccessModeOptions() {
      if (this.volumeForm.storageClass === 'local-path') {
        return ACCESS_MODE_OPTIONS.filter(item => item.value === 'ReadWriteOnce')
      }
      return ACCESS_MODE_OPTIONS
    },
    filteredFolders() {
      const keyword = this.fileKeyword.trim().toLowerCase()
      if (!keyword) return this.browserFolders
      return this.browserFolders.filter(item => item.name.toLowerCase().includes(keyword))
    },
    filteredFiles() {
      const keyword = this.fileKeyword.trim().toLowerCase()
      if (!keyword) return this.browserFiles
      return this.browserFiles.filter(item => item.name.toLowerCase().includes(keyword))
    },
    allChecked: {
      get() {
        return this.pagedVolumes.length > 0 && this.pagedVolumes.every(item => this.selectedIds.includes(item.id))
      },
      set() { }
    },
    filteredVolumes() {
      const keyword = this.keyword.trim().toLowerCase()
      const list = this.volumes.filter(item => {
        const matchKeyword = !keyword || item.name.toLowerCase().includes(keyword)
        const matchStatus = this.statusFilter === 'all' || item.status === this.statusFilter
        const matchClass = !this.classFilter || item.storageClass === this.classFilter
        const matchAccess = !this.accessFilter || item.accessMode === this.accessFilter
        return matchKeyword && matchStatus && matchClass && matchAccess
      })
      return list.sort((a, b) => {
        const result = a.createdAt.localeCompare(b.createdAt)
        return this.sortAsc ? result : -result
      })
    },
    pagedVolumes() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredVolumes.slice(start, start + this.pageSize)
    }
  },
  watch: {
    keyword() { this.currentPage = 1 },
    statusFilter() { this.currentPage = 1 },
    classFilter() { this.currentPage = 1 },
    accessFilter() { this.currentPage = 1 },
    'volumeForm.storageClass'(value) {
      if (value === 'local-path') {
        this.volumeForm.accessMode = 'ReadWriteOnce'
      } else if (!this.volumeAccessModeOptions.some(item => item.value === this.volumeForm.accessMode)) {
        this.volumeForm.accessMode = this.volumeAccessModeOptions.length ? this.volumeAccessModeOptions[0].value : ''
      }
    }
  },
  mounted() {
    this.fetchList()
    this.fetchOverview()
    this.fetchPvcViewerOverview()
  },
  methods: {
    resolveCountValue(payload, fieldNames = []) {
      if (payload == null) return 0
      if (typeof payload === 'number') return payload
      if (typeof payload === 'string') {
        const value = Number(payload)
        return Number.isNaN(value) ? 0 : value
      }
      for (const field of fieldNames) {
        const value = payload[field]
        if (value != null) {
          const count = Number(value)
          return Number.isNaN(count) ? 0 : count
        }
      }
      return 0
    },
    async fetchOverview() {
      try {
        const res = await getStorageOverview()
        if (res) {
          this.overview.totalCapacity = res.storageLimit != null ? res.storageLimit + ' GB' : '--'
          this.overview.usedCapacity = res.storageUsed != null ? res.storageUsed + ' GB' : '--'
          this.overview.usedPercent = res.storageLimit > 0 ? Math.round(res.storageUsed / res.storageLimit * 100) : 0
          this.overview.pvcNums = res.pvcNums || '--'
          this.overview.boundPvcNums = res.boundPvcNums || '--'
          this.overview.pendingPvcNums = res.pendingPvcNums || '--'
        }
      } catch (e) {
        console.error('获取存储概览失败', e)
        console.error('get storage overview failed', e)
      }
    },
    async fetchPvcViewerOverview() {
      try {
        const res = await getPvcViewerOverview()
        this.overview.snapshotCount = this.resolveCountValue(res && res.snapshot, ['count'])
        this.overview.poolCount = this.resolveCountValue(res && res.storageclass, ['count'])
      } catch (e) {
        console.error('获取存储快照数失败', e)
        console.error('get pvcviewer overview failed', e)
      }
    },
    async fetchList() {
      this.listLoading = true
      try {
        const res = await getStorageList()
        const items = (res && res.items) || []
        this.volumes = items.map(item => {
          const meta = item.metadata || {}
          const spec = item.spec || {}
          const st = item.status || {}
          let status = st.phase || 'Pending'
          if (meta.deletionTimestamp) status = 'Terminating'
          return {
            id: meta.uid || meta.name,
            name: meta.name || '--',
            namespace: meta.namespace || '--',
            storageClass: spec.storageClassName || '--',
            accessMode: (spec.accessModes && spec.accessModes[0]) || '--',
            capacity: (spec.resources && spec.resources.requests && spec.resources.requests.storage) || '--',
            status,
            volumeName: spec.volumeName || '--',
            annotations: JSON.stringify(meta.annotations || {}, null, 2),
            createdAt: meta.creationTimestamp || '--'
          }
        })
      } catch (error) {
        console.error('获取存储卷列表失败:', error)
        this.$message.error('获取存储卷列表失败')
      } finally {
        this.listLoading = false
      }
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    toggleAll(checked) {
      if (!this.$refs.storageTable) return
      this.$refs.storageTable.clearSelection()
      if (checked) {
        this.pagedVolumes.forEach(item => {
          this.$refs.storageTable.toggleRowSelection(item, true)
        })
      }
    },
    handlePageChange(page) {
      this.currentPage = page
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    toggleSort() {
      this.sortAsc = !this.sortAsc
    },
    openFilterDialog() {
      this.tempClassFilter = this.classFilter
      this.tempAccessFilter = this.accessFilter
      this.filterDialogVisible = true
    },
    applyFilters() {
      this.classFilter = this.tempClassFilter
      this.accessFilter = this.tempAccessFilter
      this.currentPage = 1
      this.filterDialogVisible = false
    },
    resetFilters() {
      this.tempClassFilter = ''
      this.tempAccessFilter = ''
      this.classFilter = ''
      this.accessFilter = ''
      this.currentPage = 1
      this.filterDialogVisible = false
    },
    showCreateDialog() {
      this.volumeForm = {
        name: '',
        namespace: 'default',
        storageClass: 'local-path',
        accessMode: 'ReadWriteOnce',
        capacity: ''
      }
      this.createDialogVisible = true
      this.$nextTick(() => {
        this.$refs.volumeForm && this.$refs.volumeForm.clearValidate()
      })
    },
    createVolume() {
      this.$refs.volumeForm.validate(async valid => {
        if (!valid) return
        this.createLoading = true
        try {
          const rawCapacity = this.volumeForm.capacity.trim()
          const capacity = /[a-zA-Z]$/.test(rawCapacity) ? rawCapacity : rawCapacity + 'Gi'
          const body = {
            kind: 'PersistentVolumeClaim',
            apiVersion: 'v1',
            metadata: { name: this.volumeForm.name, annotations: {} },
            spec: {
              storageClassName: this.volumeForm.storageClass,
              accessModes: [this.volumeForm.accessMode],
              resources: { requests: { storage: capacity } }
            }
          }
          await createStorageVolume(body)
          this.createDialogVisible = false
          this.$message.success('存储卷已创建')
          this.fetchList()
        } catch (error) {
          console.error('创建存储卷失败:', error)
          this.$message.error(error.message || '创建存储卷失败')
        } finally {
          this.createLoading = false
        }
      })
    },
    viewDetail(row) {
      this.currentVolume = row
      this.detailDialogVisible = true
    },
    getAccessModeText(mode) {
      if (mode === 'RWO') return 'ReadWriteOnce'
      if (mode === 'RWX') return 'ReadWriteMany'
      return mode || '-'
    },
    async openFileBrowser(row) {
      const loading = this.$loading({ lock: true, text: '正在启动文件浏览器...', background: 'rgba(0,0,0,0.5)' })
      try {
        const res = await createPod({ namespace: row.namespace, pvc_name: row.name })
        this.$refs.fileDialog.init(res.pod.name)
      } catch (error) {
        this.$message.error('创建文件浏览器失败')
      } finally {
        loading.close()
      }
    },
    async backupVolume(row) {
      const loading = this.$loading({ lock: true, text: '正在备份，请稍候...', background: 'rgba(0,0,0,0.5)' })
      try {
        await backupPvc({ namespace: row.namespace, pvc_name: row.name })
        this.$message.success('备份成功')
        this.fetchPvcViewerOverview()
      } catch (error) {
        this.$message.error('备份失败')
      } finally {
        loading.close()
      }
    },
    async cleanPod(row) {
      try {
        await deletePod({ namespace: row.namespace, name: row.name })
        this.$message.success('清理成功')
      } catch (error) {
        this.$message.error('清理失败')
      }
    },
    deleteVolume(row) {
      this.$confirm(`确定删除存储卷 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.deleteLoading = true
        try {
          await deleteStorageVolume(row.name)
          this.$message.success('删除成功')
          this.fetchList()
        } catch (error) {
          console.error('删除存储卷失败:', error)
          this.$message.error(error.message || '删除失败')
        } finally {
          this.deleteLoading = false
        }
      }).catch(() => { })
    },
    handleBatchDelete() {
      if (!this.selectedIds.length) return
      this.$confirm(`确定删除选中的 ${this.selectedIds.length} 个存储卷吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.deleteLoading = true
        try {
          await Promise.all(this.selectedIds.map(id => deleteStorageVolume(id)))
          this.selectedIds = []
          this.$message.success('批量删除成功')
          this.fetchList()
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message.error('部分删除失败')
        } finally {
          this.deleteLoading = false
        }
      }).catch(() => { })
    },
    async handleExpandChange(row, expandedRows) {
      const isExpanded = expandedRows.some(r => r.id === row.id)
      if (!isExpanded) return
      // 关闭其他已展开的行
      this.pagedVolumes.forEach(item => {
        if (item.id !== row.id) {
          this.$refs.storageTable.toggleRowExpansion(item, false)
        }
      })
      if (!this.snapshotMap[row.id]) {
        await this.fetchSnapshots(row)
      }
    },
    async refreshSnapshots(row) {
      this.$set(this.snapshotMap, row.id, undefined)
      await this.fetchSnapshots(row)
    },
    async fetchSnapshots(row) {
      this.$set(this.snapshotLoadingRows, row.id, true)
      try {
        const res = await getSnapshotList(row.name)
        this.$set(this.snapshotMap, row.id, Array.isArray(res) ? res : [])
      } catch (e) {
        this.$message.error('获取快照列表失败')
        this.$set(this.snapshotMap, row.id, [])
      } finally {
        this.$set(this.snapshotLoadingRows, row.id, false)
      }
    },
    openRestoreDialog(row, snapshot) {
      this.restoreForm = { pvcName: row.name, snapshotId: snapshot.snapshot_id, sourcePvcName: '' }
      this.restoreDialogVisible = true
      this.$nextTick(() => {
        this.$refs.restoreFormRef && this.$refs.restoreFormRef.clearValidate()
      })
    },
    confirmRestore() {
      this.$refs.restoreFormRef.validate(async valid => {
        if (!valid) return
        this.restoreLoading = true
        try {
          await restoreSnapshot({
            pvc_name: this.restoreForm.sourcePvcName,
            snapshot_id: this.restoreForm.snapshotId,
            source_pvc_name: this.restoreForm.pvcName
          })
          this.$message.success('恢复成功')
          this.restoreDialogVisible = false
        } catch (e) {
          this.$message.error(e.message || '恢复失败')
        } finally {
          this.restoreLoading = false
        }
      })
    },
    handleDeleteSnapshot(row, snapshot) {
      this.$confirm(`确定删除快照 "${snapshot.snapshot_id.slice(0, 8)}..." 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteSnapshot({ pvc_name: row.name, snapshot_id: snapshot.snapshot_id })
          this.$message.success('删除成功')
          this.$set(this.snapshotMap, row.id, undefined)
          await this.fetchSnapshots(row)
        } catch (e) {
          this.$message.error(e.message || '删除快照失败')
        }
      }).catch(() => { })
    },
    formatSnapshotTime(val) {
      if (!val) return '--'
      return val.replace('T', ' ')
    },
    exportCsv() {
      const header = ['名称', '命名空间', '存储类', '访问模式', '容量', '状态', '创建时间']
      const rows = this.filteredVolumes.map(item => [
        item.name,
        item.namespace,
        item.storageClass,
        item.accessMode,
        item.capacity,
        item.status,
        item.createdAt
      ])
      const csv = [header, ...rows].map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')).join('\n')
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'storage-volumes.csv'
      link.click()
      URL.revokeObjectURL(url)
    },
    refreshFileBrowser() {
      this.$message.info('刷新文件列表（暂未对接列表接口）')
    },
    goHomeInBrowser() {
      this.fileKeyword = ''
    },
    triggerBrowserUpload() {
      if (this.browserUpload.uploading) {
        this.$message.warning('当前有文件正在上传，请稍候')
        return
      }
      this.$refs.browserFileInput && this.$refs.browserFileInput.click()
    },
    async onBrowserFileSelected(e) {
      const file = e.target.files && e.target.files[0]
      // 清空 input，允许下次选同一文件
      e.target.value = ''
      if (!file) return
      try {
        await this.doBrowserMultipartUpload(file)
      } catch (err) {
        this.browserUpload.status = 'exception'
        this.browserUpload.statusText = err.message || '上传失败'
        this.$message.error(this.browserUpload.statusText)
      }
    },
    cancelBrowserUpload() {
      this.browserUpload.aborted = true
      this.browserUpload.statusText = '正在取消...'
    },
    getCurrentProjectName() {
      const cp = this.$store && this.$store.state && this.$store.state.project && this.$store.state.project.currentProject
      return cp && cp.name ? String(cp.name) : 'default'
    },
    async doBrowserMultipartUpload(file) {
      const projectName = this.getCurrentProjectName()
      const safeName = (file.name || 'file').replace(/\s+/g, '-').replace(/[^a-zA-Z0-9.\-_]/g, '') || 'file'
      const CONCURRENCY = 3

      const bu = this.browserUpload
      bu.uploading = true
      bu.aborted = false
      bu.percent = 0
      bu.status = ''
      bu.statusText = '初始化上传...'
      bu.fileName = safeName
      bu.objectKey = ''
      bu.uploadId = ''
      
      // 如果文件小于分片大小 (5MB)，走小文件直接上传接口
      if (file.size < PART_SIZE) {
        bu.statusText = '正在上传...'
        try {
          const uploadData = new FormData()
          uploadData.append('file', new File([file], safeName))
          await uploadProjectSmallFile(uploadData, { 'X-Project-Name': projectName })
          bu.percent = 100
          bu.status = 'success'
          bu.statusText = '上传成功'
          this.$message.success(`${safeName} 上传成功`)
          this.browserFiles.unshift({ name: safeName, size: this.formatSize(file.size) })
        } catch (err) {
          bu.status = 'exception'
          bu.statusText = err.message || '上传失败'
          throw err
        } finally {
          bu.uploading = false
        }
        return
      }

      // 1. 初始化分片上传
      const initRes = await initProjectFileUpload({ fileName: safeName }, { 'X-Project-Name': projectName })
      if (!initRes || !initRes.uploadId) {
        bu.uploading = false
        throw new Error('初始化分片上传失败')
      }
      bu.objectKey = initRes.objectKey
      bu.uploadId = initRes.uploadId

      const totalParts = Math.ceil(file.size / PART_SIZE)
      const uploadedParts = []
      let completed = 0
      let nextPart = 1
      bu.statusText = `上传中 0 / ${totalParts}`

      const uploadOne = async () => {
        while (!bu.aborted) {
          const n = nextPart++
          if (n > totalParts) return

          // 2. 获取预签名 URL
          const presignedRes = await getProjectFilePresignedUrl({
            objectKey: bu.objectKey,
            uploadId: bu.uploadId,
            partNumber: n
          })
          if (!presignedRes || !presignedRes.url) {
            throw new Error(`获取预签名 URL 失败，分片 ${n}`)
          }
          const { url } = presignedRes
          const start = (n - 1) * PART_SIZE
          const blob = file.slice(start, Math.min(start + PART_SIZE, file.size))

          // 3. PUT 到 S3
          const putRes = await uploadProjectFilePartToS3(url, blob)
          const headers = putRes.headers || {}
          const etag = (headers.get ? (headers.get('etag') || headers.get('ETag')) : (headers['etag'] || headers['ETag']) || '').replace(/"/g, '')
          if (!etag) {
            throw new Error(`分片 ${n} 未返回 ETag，请检查 MinIO CORS 配置`)
          }

          // 4. 通知业务台账
          await notifyProjectFilePartComplete({
            uploadId: bu.uploadId,
            partNumber: n,
            etag,
            partSize: blob.size
          })

          uploadedParts.push({ partNumber: n, etag })
          completed++
          bu.percent = Math.round((completed / totalParts) * 100)
          bu.statusText = `上传中 ${completed} / ${totalParts}`
        }
      }

      try {
        const workers = []
        for (let i = 0; i < CONCURRENCY; i++) workers.push(uploadOne())
        await Promise.all(workers)

        if (bu.aborted) {
          // 取消：通知后端中止分片
          try {
            await abortProjectFileUpload({ objectKey: bu.objectKey, uploadId: bu.uploadId })
          } catch (e) { /* ignore */ }
          bu.status = 'exception'
          bu.statusText = '已取消'
          return
        }

        // 5. 合并分片
        bu.statusText = '合并分片...'
        uploadedParts.sort((a, b) => a.partNumber - b.partNumber)
        const completeRes = await completeProjectFileUpload({
          objectKey: bu.objectKey,
          uploadId: bu.uploadId,
          parts: uploadedParts
        })
        if (!completeRes) {
          throw new Error('合并分片失败')
        }
        bu.percent = 100
        bu.status = 'success'
        bu.statusText = '上传成功'
        this.$message.success(`${safeName} 上传成功`)
        // 简单地把刚上传的文件推进列表（仅前端展示）
        this.browserFiles.unshift({ name: safeName, size: this.formatSize(file.size) })
      } catch (err) {
        if (bu.aborted) {
          bu.status = 'exception'
          bu.statusText = '已取消'
        } else {
          bu.status = 'exception'
          bu.statusText = err.message || '上传失败'
          // 失败时尝试中止
          if (bu.objectKey && bu.uploadId) {
            try {
              await abortProjectFileUpload({ objectKey: bu.objectKey, uploadId: bu.uploadId })
            } catch (e) { /* ignore */ }
          }
        }
        throw err
      } finally {
        bu.uploading = false
      }
    },
    formatSize(bytes) {
      if (!bytes && bytes !== 0) return '--'
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
      if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(2) + ' MB'
      return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB'
    },
    async downloadBrowserFile() {
      if (!this.selectedBrowserFile) return
      const objectKey = this.selectedBrowserFile.objectKey
      if (!objectKey) {
        this.$message.warning('该文件为占位数据，没有可下载的远端地址')
        return
      }
      try {
        const res = await getProjectFileDownloadUrl({ objectKey })
        if (!res || !res.url) throw new Error('获取下载地址失败')
        const a = document.createElement('a')
        a.href = res.url
        a.download = this.selectedBrowserFile.name
        a.click()
      } catch (err) {
        this.$message.error(err.message || '下载失败')
      }
    },
    selectBrowserFile(file) {
      this.selectedBrowserFile = file
    }
  }
}
</script>

<style lang="scss" scoped>
.storage-container {
  min-height: calc(100vh - 84px);

  .storage-content {
    margin: 0 auto;
  }
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;

  .page-title {
    margin: 0 0 6px;
    font-size: 20px;
    color: #1f2937;
    font-weight: bold;
  }

  .page-subtitle {
    margin: 0;
    color: #5f6b7a;
    font-size: 14px;
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }

  .export-btn,
  .create-btn {
    font-size: 13px;
  }

  .export-btn {
    border: 0;
    background: #f3f4f6;
    color: #243447;
  }

  .create-btn {
    box-shadow: 0 8px 16px rgba(37, 99, 235, 0.26);
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}

.metric-card {
  position: relative;
  min-height: 116px;
  border-radius: 10px;
  padding: 18px 20px;
  color: #fff;
  overflow: hidden;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);

  &.blue {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
  }

  &.green {
    background: linear-gradient(135deg, #22c55e, #059669);
  }

  &.purple {
    background: linear-gradient(135deg, #a855f7, #7c3aed);
  }

  &.orange {
    background: linear-gradient(135deg, #f97316, #ea580c);
  }

  .metric-main {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .metric-label {
    font-size: 13px;
    font-weight: 500;
    opacity: 0.86;
  }

  strong {
    font-size: 24px;
    line-height: 1.1;
  }

  .metric-icon {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 42px;
    height: 42px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
  }

  .metric-progress {
    height: 6px;
    margin-top: 16px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.22);
    overflow: hidden;

    span {
      display: block;
      height: 100%;
      border-radius: inherit;
      background: #fff;
    }
  }

  .metric-foot {
    margin-top: 8px;
    font-size: 12px;
    opacity: 0.9;
  }

  .metric-foot.strong {
    margin-top: 16px;
  }

  .metric-tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    margin-top: 14px;

    span {
      padding: 3px 10px;
      font-size: 12px;
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.18);
    }
  }

  .metric-tags.soft span {
    border-radius: 6px;
  }
}

.table-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 20px;
  border-bottom: 1px solid #e5e7eb;

  .search-input {
    width: 260px;

    ::v-deep .el-input__inner {
      height: 32px;
      border-radius: 6px;
      font-size: 13px;
    }
  }

  .toolbar-spacer {
    flex: 1;
  }
}

.status-tabs {
  display: flex;
  gap: 4px;
  padding: 5px;
  background: #f0f2f5;
  border-radius: 8px;

  button {
    height: 26px;
    padding: 0 10px;
    border: 0;
    border-radius: 4px;
    background: transparent;
    color: #5f6b7a;
    font-size: 12px;
    cursor: pointer;
  }

  button.active {
    background: #fff;
    color: #111827;
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.08);
  }
}

.danger-action {
  color: #ef4444;
}

.volume-name {
  display: flex;
  align-items: center;
  gap: 14px;

  .volume-icon {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    background: #dbeafe;
    color: #2563eb;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    flex-shrink: 0;
  }

  .name-text {
    color: #1f2937;
    font-size: 13px;
    line-height: 18px;
    font-weight: 500;
  }

  .namespace-text {
    color: #9ca3af;
    font-size: 12px;
    margin-top: 1px;
  }
}

.class-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f3f4f6;
  color: #374151;
  font-size: 12px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;

  i {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &.bound {
    background: #dcfce7;
    color: #047857;

    i {
      background: #059669;
    }
  }

  &.pending {
    background: #fef3c7;
    color: #b45309;

    i {
      background: #d97706;
    }
  }

  &.failed {
    background: #fee2e2;
    color: #b91c1c;

    i {
      background: #dc2626;
    }
  }
}

.used-link {
  color: #2563eb;
  font-size: 13px;
}

.muted {
  color: #9ca3af;
  font-size: 13px;
}

.detail-list {
  display: grid;
  gap: 14px;

  div {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eef2f7;
  }

  span {
    color: #64748b;
  }

  strong {
    color: #111827;
    text-align: right;
    word-break: break-all;
  }
}

::v-deep .storage-detail-dialog {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.detail-dialog-header {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #e5e7eb;

  h2 {
    margin: 0;
    color: #1f2937;
    font-size: 15px;
    font-weight: 600;
  }
}

.detail-close {
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  color: #9ca3af;
  font-size: 16px;
  cursor: pointer;
}

.storage-detail-body {
  padding: 0 20px;
}

.detail-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 14px 0 12px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f9fafb;

  .status-badge {
    i {
      display: none;
    }
  }
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.detail-field {
  label {
    display: block;
    margin-bottom: 5px;
    color: #374151;
    font-size: 12px;
    font-weight: 600;
  }

  div {
    min-height: 32px;
    display: flex;
    align-items: center;
    padding: 0 12px;
    border-radius: 6px;
    background: #f3f4f6;
    color: #1f2937;
    font-size: 13px;
  }
}

.detail-section {
  margin-top: 14px;

  h3 {
    margin: 0 0 8px;
    color: #374151;
    font-size: 13px;
    font-weight: 600;
  }
}

.resource-row {
  min-height: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 12px;
  border-radius: 6px;
  background: #f9fafb;
  color: #1f2937;
  font-size: 13px;

  em {
    padding: 2px 8px;
    border-radius: 4px;
    background: #dbeafe;
    color: #2563eb;
    font-size: 12px;
    font-style: normal;
    font-weight: 500;
  }
}

.annotations-code {
  margin: 0;
  padding: 12px;
  border-radius: 6px;
  background: #1f2937;
  color: #e5e7eb;
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.storage-detail-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0 20px;
  padding: 12px 0 16px;
  border-top: 1px solid #e5e7eb;
  color: #9ca3af;
  font-size: 12px;

  >div {
    display: flex;
    gap: 10px;
  }

  .detail-primary {
    background: #2563eb;
    border-color: #2563eb;
  }
}

::v-deep .file-browser-dialog {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.browser-window {
  height: 560px;
  background: #fff;
  overflow: hidden;
}

.browser-topbar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e5e7eb;
}

.browser-nav,
.browser-actions {
  display: flex;
  align-items: center;
  gap: 8px;

  button {
    width: 24px;
    height: 24px;
    border: 0;
    background: transparent;
    color: #6b7280;
    font-size: 15px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.browser-nav span {
  color: #374151;
  font-size: 13px;
  font-weight: 500;
}

.browser-main {
  display: grid;
  grid-template-columns: 180px 1fr;
  height: calc(100% - 50px);
}

.browser-sidebar {
  padding: 12px 10px;
  border-right: 1px solid #e5e7eb;

  button {
    width: 100%;
    height: 32px;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0 10px;
    border: 0;
    border-radius: 6px;
    background: transparent;
    color: #374151;
    font-size: 13px;
    text-align: left;
    cursor: pointer;
  }

  button.active {
    background: #eff6ff;
    color: #2563eb;
  }
}

.browser-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  >span {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: #2563eb;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    flex-shrink: 0;
  }

  strong {
    display: block;
    color: #111827;
    font-size: 13px;
    font-weight: 600;
  }

  em {
    color: #9ca3af;
    font-size: 12px;
    font-style: normal;
  }
}

.browser-divider {
  height: 1px;
  margin: 8px 0 4px;
  background: #e5e7eb;
}

.browser-content {
  padding: 14px;
  overflow: auto;
}

.browser-search {
  margin-bottom: 12px;

  ::v-deep .el-input__inner {
    height: 32px;
    border-radius: 6px;
    font-size: 13px;
  }
}

.browser-section-title {
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.files-title {
  margin-top: 16px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.file-card {
  min-height: 90px;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;

  &.active,
  &:hover {
    border-color: #2563eb;
    box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
  }

  .file-thumb {
    height: 34px;
    border-radius: 6px;
    background: #dbeafe;
    color: #2563eb;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 17px;
    margin-bottom: 8px;
  }

  .file-thumb.file {
    background: #f3f4f6;
    color: #6b7280;
  }

  strong {
    display: block;
    color: #111827;
    font-size: 12px;
    line-height: 16px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  span {
    display: block;
    margin-top: 3px;
    color: #9ca3af;
    font-size: 11px;
  }
}

.browser-upload-bar {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-size: 12px;
  color: #374151;

  .browser-upload-name {
    max-width: 220px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #1f2937;
    font-weight: 500;
    margin-right: 8px;
  }

  .browser-upload-text {
    color: #6b7280;
    margin-right: 8px;
    white-space: nowrap;
  }
}

::v-deep .pvc-dialog {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }

  .el-form-item {
    margin-bottom: 16px;
  }

  .el-form-item__label {
    float: none;
    display: block;
    width: auto !important;
    padding: 0 0 5px;
    color: #374151;
    font-size: 13px;
    line-height: 18px;
    font-weight: 600;
    text-align: left;
  }

  .el-form-item__content {
    margin-left: 0 !important;
    line-height: normal;
  }

  .el-input__inner {
    height: 34px;
    border-radius: 6px;
    border-color: #d1d5db;
    color: #1f2937;
    font-size: 13px;
  }

  .el-input__inner::placeholder {
    color: #9ca3af;
  }

  .el-select .el-input .el-select__caret {
    color: #9ca3af;
    font-size: 14px;
  }

  .el-input__suffix {
    right: 10px;
    display: flex;
    align-items: center;
  }
}

.pvc-dialog-header {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #e5e7eb;

  h2 {
    margin: 0;
    color: #1f2937;
    font-size: 15px;
    font-weight: 600;
  }
}

.pvc-close {
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  color: #9ca3af;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pvc-form {
  padding: 20px 20px 4px;

  .required {
    color: #ef4444;
    margin-right: 4px;
  }

  .capacity-example {
    color: #9ca3af;
    font-size: 12px;
    line-height: 34px;
    white-space: nowrap;
  }
}

.pvc-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 0 20px;
  padding: 14px 0 18px;
  border-top: 1px solid #e5e7eb;
}

.snapshot-expand {
  padding: 0 20px;
}

.snapshot-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.snapshot-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.snapshot-table {
  max-width: 950px;
  border: 1px solid #e5e7eb !important;
  border-radius: 6px;
  overflow: hidden;

  ::v-deep .el-table__border-left-patch,
  ::v-deep td,
  ::v-deep th {
    border-color: #e5e7eb !important;
  }

  ::v-deep .el-table__body tr:hover>td {
    background: none !important;
  }
}

.snapshot-id {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: #374151;
}

.snapshot-empty {
  padding: 16px 0;
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
}

::v-deep .el-table th,
::v-deep .el-table td {
  padding: 10px 0;
}

::v-deep .el-table th {
  font-size: 13px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 14px 16px;
  border-top: 1px solid #e5e7eb;
}

.total-items {
  margin-right: auto;
  color: #6b7280;
  font-size: 13px;
}

@media (max-width: 1200px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar {
    flex-wrap: wrap;

    .search-input {
      width: 100%;
    }
  }
}

@media (max-width: 768px) {
  .storage-container .storage-content {
    padding: 18px 14px 32px;
  }

  .page-header {
    flex-direction: column;
  }

  .page-header .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
