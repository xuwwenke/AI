<template>
  <div class="notebook-container">
    <div class="notebook-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">开发环境</h1>
          <p class="page-subtitle">AI对话模式 - 只需提出需求，自动完成开发</p>
        </div>
        <div class="header-right">
          <el-button v-if="!showIdeNotice" type="text" class="restore-notice-btn" @click="restoreIdeNotice">
            <i class="el-icon-refresh-left"></i> 重新显示提示
          </el-button>
          <el-button class="guide-btn" @click="guideDrawerVisible = true">
            <i class="el-icon-document"></i> 操作指南
          </el-button>
        </div>
      </div>
      <div v-if="showIdeNotice" class="ide-notice">
        <i class="el-icon-info"></i>
        <span class="ide-notice-text">如遇 IDE 加载不出来，请点击右上角
          <el-button type="text" class="notice-link" @click="guideDrawerVisible = true">操作指南</el-button>
          ，按步骤导入自签名证书后刷新页面。
        </span>
        <el-button type="text" class="notice-close" @click="hideIdeNotice">
          <i class="el-icon-close"></i>
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="main-tabs">
        <el-tab-pane label="AI智能IDE" name="ide">
          <div class="ide-iframe-container" v-loading="iframeLoading" element-loading-text="正在加载开发环境，请稍候...">
            <iframe :src="iframeSrc" class="ide-iframe" frameborder="0" @load="onIframeLoad"
              v-show="iframeSrc && !iframeLoading"></iframe>
            <div v-if="!iframeSrc && !iframeLoading" class="no-environment-placeholder">
              <template v-if="ideDeploying">
                <div class="deploy-spin"><i class="el-icon-loading"></i></div>
                <p class="deploy-msg">开发环境正在部署中，请稍后…</p>
                <p class="deploy-sub">通常需要几分钟，部署完成后将自动展示</p>
                <el-button size="small" :loading="ideRefreshing" @click="fetchIdeUrl">手动刷新</el-button>
              </template>
              <template v-else>
                <i class="el-icon-monitor"></i>
                <p>请从下方环境列表中选择并启动一个环境</p>
              </template>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- <div class="section"> -->
      <!-- <h2 class="section-title">查询环境</h2> -->
      <!-- <div class="search-card card-hover"> -->
      <!-- <div class="search-form"> -->
      <!-- <div class="form-item"> -->
      <!-- <label class="form-label">环境名称</label> -->
      <!-- <el-input v-model="filters.name" placeholder="环境名称" size="small" clearable></el-input> -->
      <!-- </div> -->
      <!-- <div class="form-item"> -->
      <!-- <label class="form-label">创建者</label> -->
      <!-- <el-input v-model="filters.creator" placeholder="创建者名称" size="small" clearable></el-input> -->
      <!-- </div> -->
      <!-- <div class="form-item"> -->
      <!-- <label class="form-label">状态</label> -->
      <!-- <el-select v-model="filters.status" placeholder="全部状态" size="small" clearable style="width: 100%"> -->
      <!-- <el-option label="全部状态" value=""></el-option> -->
      <!-- <el-option label="运行中" value="running"></el-option> -->
      <!-- <el-option label="已停止" value="stopped"></el-option> -->
      <!-- </el-select> -->
      <!-- </div> -->
      <!-- <div class="form-item button-item"> -->
      <!-- <el-button type="primary" size="small" @click="handleSearch"> -->
      <!-- <i class="el-icon-search"></i> 查询 -->
      <!-- </el-button> -->
      <!-- </div> -->
      <!-- </div> -->
      <!-- </div> -->
      <!-- </div> -->

      <!-- <div class="section"> -->
      <!-- <div class="table-card card-hover"> -->
      <!-- <el-table :data="environments" style="width: 100%" stripe border -->
      <!-- :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }"> -->
      <!-- <el-table-column prop="name" label="名称" min-width="180"> -->
      <!-- <template slot-scope="scope"> -->
      <!-- <span class="name-link" @click="handleOpenConsole(scope.row)">{{ scope.row.name }}</span> -->
      <!-- </template> -->
      <!-- </el-table-column> -->
      <!-- <el-table-column prop="status" label="状态" width="100"> -->
      <!-- <template slot-scope="scope"> -->
      <!-- <span class="status-tag" -->
      <!-- :class="getStatusClass(scope.row.expand ? scope.row.expand.status : 'stopped')"> -->
      <!-- {{ (scope.row.expand && scope.row.expand.status === 'online') ? '运行中' : '已停止' }} -->
      <!-- </span> -->
      <!-- </template> -->
      <!-- </el-table-column> -->
      <!-- <el-table-column prop="describe" label="描述" min-width="200" show-overflow-tooltip></el-table-column> -->
      <!-- <el-table-column label="重置" width="80"> -->
      <!-- <template slot-scope="scope"> -->
      <!-- <el-button size="small" @click="handleRestart(scope.row)"> -->
      <!-- reset -->
      <!-- </el-button> -->
      <!-- </template> -->
      <!-- </el-table-column> -->
      <!-- <el-table-column prop="resource" label="资源" width="200"> -->
      <!-- <template slot-scope="scope"> -->
      <!-- <div class="resource-text"> -->
      <!-- {{ formatResource(scope.row) }} -->
      <!-- </div> -->
      <!-- </template> -->
      <!-- </el-table-column> -->

      <!-- <el-table-column prop="thread" label="续租" width="80"> -->
      <!-- <template slot-scope="scope"> -->
      <!-- <el-button size="small" @click="handleRenew(scope.row)"> -->
      <!-- 续租 -->
      <!-- </el-button> -->
      <!-- </template> -->
      <!-- </el-table-column> -->
      <!-- <el-table-column label="保存" width="80"> -->
      <!-- <template slot-scope="scope"> -->
      <!-- <el-button size="small" @click="handleSave(scope.row)"> -->
      <!-- 保存 -->
      <!-- </el-button> -->
      <!-- </template> -->
      <!-- </el-table-column> -->
      <!-- <el-table-column label="操作" width="180" fixed="right"> -->
      <!-- <template slot-scope="scope"> -->
      <!-- <el-button type="text" size="small" @click="handleOpen(scope.row)">详情</el-button> -->
      <!-- <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button> -->
      <!-- <el-button type="text" size="small" @click="handleStop(scope.row)" -->
      <!-- v-if="scope.row.expand && scope.row.expand.status === 'online'">停止</el-button> -->
      <!-- <el-button type="text" size="small" @click="handleDelete(scope.row)">删除</el-button> -->
      <!-- </template> -->
      <!-- </el-table-column> -->
      <!-- </el-table> -->
      <!-- <div class="pagination-right"> -->
      <!-- <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total" -->
      <!-- :page-size="pageSize" :page-sizes="[10, 20, 50, 100]" :current-page="currentPage" -->
      <!-- @current-change="handlePageChange" @size-change="handlePageSizeChange"> -->
      <!-- </el-pagination> -->
      <!-- </div> -->
      <!-- </div> -->
      <!-- </div> -->
    </div>

    <!-- 创建环境弹框 -->
    <el-dialog title="创建环境" :visible.sync="createDialogVisible" width="600px" :close-on-click-modal="false"
      class="create-environment-dialog">
      <el-form :model="environmentForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="environmentForm.name" placeholder="环境名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="environmentForm.describe" type="textarea" :rows="3" placeholder="环境描述"
            clearable></el-input>
        </el-form-item>
        <el-form-item label="镜像">
          <el-select v-model="environmentForm.images" filterable allow-create default-first-option
            placeholder="请选择或输入镜像" clearable style="width: 100%;">

            <el-option v-for="item in imageOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>

          </el-select>
        </el-form-item>
        <el-form-item label="资源配置">
          <div style="display: flex; gap: 12px;">
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">内存申请</div>
              <el-input v-model="environmentForm.resource_memory" placeholder="内存" clearable></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">CPU申请</div>
              <el-input v-model="environmentForm.resource_cpu" placeholder="CPU" clearable></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">GPU申请</div>
              <el-input v-model="environmentForm.resource_gpu" placeholder="GPU" clearable></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="挂载目录">
          <el-input v-model="environmentForm.volume_mount" placeholder="挂载目录" clearable></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateEnvironment">创建</el-button>
      </span>
    </el-dialog>

    <!-- 修改环境弹框 -->
    <el-dialog :title="`修改环境 - ${editingEnvironment.name}`" :visible.sync="editDialogVisible" width="600px"
      :close-on-click-modal="false" class="edit-environment-dialog">
      <el-form :model="editForm" label-width="100px">

        <el-form-item label="名称">
          <el-input v-model="editForm.name" placeholder="环境名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.describe" type="textarea" :rows="3" placeholder="环境描述" clearable></el-input>
        </el-form-item>
        <el-form-item label="镜像">
          <el-select v-model="editForm.images" filterable allow-create default-first-option placeholder="请选择或输入镜像"
            clearable style="width: 100%;">
            <el-option v-for="item in imageOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资源配置">
          <div style="display: flex; gap: 12px;">
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">内存申请</div>
              <el-input v-model="editForm.resource_memory" placeholder="如 64G" clearable></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">CPU申请</div>
              <el-input v-model="editForm.resource_cpu" placeholder="如 8核" clearable></el-input>
            </div>
            <div style="flex: 1;">
              <div style="font-size: 12px; color: #606266; margin-bottom: 4px;">GPU申请</div>
              <el-input v-model="editForm.resource_gpu" placeholder="如 1卡T4" clearable></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="挂载目录">
          <el-input v-model="editForm.volume_mount" placeholder="如 /data" clearable></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEnvironment">保存</el-button>
      </span>
    </el-dialog>

    <!-- 详情弹框 -->
    <el-dialog title="notebook 详情" :visible.sync="detailDialogVisible" width="700px"
      custom-class="notebook-detail-dialog">
      <div class="detail-content" v-loading="detailLoading">
        <div class="detail-item">
          <div class="detail-label">项目组：</div>
          <div class="detail-value">{{ notebookDetail.projectName || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">名称：</div>
          <div class="detail-value">{{ notebookDetail.name || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">命名空间：</div>
          <div class="detail-value">{{ notebookDetail.namespace || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">描述：</div>
          <div class="detail-value">{{ notebookDetail.describe || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">镜像：</div>
          <div class="detail-value">{{ notebookDetail.images || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">启动目录：</div>
          <div class="detail-value">{{ notebookDetail.workingDir || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">环境变量：</div>
          <div class="detail-value">{{ notebookDetail.env || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">挂载目录：</div>
          <div class="detail-value">{{ notebookDetail.volumeMount || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">内存申请：</div>
          <div class="detail-value">{{ notebookDetail.resource_memory || notebookDetail.resourceMemory || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">cpu申请：</div>
          <div class="detail-value">{{ notebookDetail.resource_cpu || notebookDetail.resourceCpu || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">gpu申请：</div>
          <div class="detail-value">{{ notebookDetail.resource_gpu || notebookDetail.resourceGpu || '-' }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">扩展：</div>
          <div class="detail-value expand-json">
            <code>{{ notebookDetail.expand ? JSON.stringify(notebookDetail.expand, null, 2) : '{}' }}</code>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-label">id：</div>
          <div class="detail-value">{{ notebookDetail.id || '-' }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 自签名证书操作指南抽屉 -->
    <el-drawer :visible.sync="guideDrawerVisible" :size="guideFullscreen ? '100%' : '540px'" direction="rtl"
      :with-header="false" custom-class="cert-guide-drawer">
      <div class="guide-wrapper" :class="{ 'guide-fullscreen': guideFullscreen }">
        <div class="guide-header">
          <h2><i class="el-icon-document"></i> 操作指南</h2>
          <div class="guide-header-actions">
            <el-tooltip :content="guideFullscreen ? '退出全屏' : '全屏查看'" placement="bottom">
              <el-button class="guide-header-btn" @click="guideFullscreen = !guideFullscreen">
                <i :class="guideFullscreen ? 'el-icon-full-screen' : 'el-icon-full-screen'"></i>
              </el-button>
            </el-tooltip>
            <el-button class="guide-header-btn" @click="guideDrawerVisible = false">
              <i class="el-icon-close"></i>
            </el-button>
          </div>
        </div>

        <div class="guide-body">
          <div class="guide-intro">
            <i class="el-icon-warning-outline guide-intro-icon"></i>
            <div>
              <strong>为什么 IDE 无法加载？</strong>
              <p>开发环境（code-server）使用自签名 HTTPS 证书。浏览器未信任该证书时，会提示"您的连接不是私密连接"或"网页可能暂时无法连接"，这是正常现象，并非服务故障。</p>
              <p>将证书导入系统受信任列表后，操作一次永久生效，无需重复。</p>
            </div>
          </div>

          <div class="guide-step">
            <div class="step-number">1</div>
            <div class="step-content">
              <h3>下载证书文件</h3>
              <p>点击下方按钮下载证书文件 <code>code-server.csr</code>：</p>
              <el-button type="primary" class="download-btn" @click="downloadCsr">
                <i class="el-icon-download"></i> 下载证书文件
              </el-button>
              <div class="step-tip info">
                <span>✅ 该文件可直接导入，无需修改文件名或格式</span>
              </div>
            </div>
          </div>

          <div class="guide-step">
            <div class="step-number">2</div>
            <div class="step-content">
              <h3>Chrome / Edge（Windows）导入证书</h3>
              <ul>
                <li>打开 Chrome/Edge → 右上角菜单 <strong>⋮</strong> → <strong>"设置"</strong></li>
                <li>左侧选择 <strong>"隐私与安全"</strong> → 右侧点击 <strong>"管理证书"</strong></li>
                <img src="@/assets/images/cert-guide/step1-windows.png" alt="Windows证书导入" class="guide-img">
                <li>在弹出的证书窗中选择 <strong>"本地证书"</strong> 选项卡</li>
                <li>点击 <strong>"管理从 Windows 导入的证书"</strong><strong> → 在"受信任的根证书颁发机构"</strong>选项卡中点击<strong>"导入"</strong>
                </li>
                <img src="@/assets/images/cert-guide/step2-windows.png" alt="Windows证书导入" class="guide-img">
                <li>按照证书导入向导，选择下载的 <code>code-server.csr</code> 文件 → 完成导入</li>
                <li><strong>完全退出并重启浏览器</strong>（Ctrl+Shift+Q 完全退出，不是关闭窗口）</li>
              </ul>
              <div class="step-tip info">
                <i class="el-icon-info"></i>
                <span>重启后回到本页刷新，IDE 即可正常加载。地址栏出现 🔒 锁图标即表示成功。</span>
              </div>
            </div>
          </div>

          <div class="guide-step">
            <div class="step-number">3</div>
            <div class="step-content">
              <h3>Firefox 导入证书</h3>
              <p>Firefox 使用独立证书库，需单独导入：</p>
              <ul>
                <li>右上角菜单 <strong>☰</strong> → <strong>"设置"</strong> → 左侧 <strong>"隐私与安全"</strong></li>
                <li>滚动到底部 <strong>"证书"</strong> 区块 → 点击 <strong>"查看证书"</strong></li>
                <img src="@/assets/images/cert-guide/step3-windows.png" alt="Windows证书导入" class="guide-img">
                <li>切换到 <strong>"证书颁发机构"</strong> 选项卡 → 点击 <strong>"导入"</strong></li>
                <img src="@/assets/images/cert-guide/step4-windows.png" alt="Windows证书导入" class="guide-img">
                <li>选择 <code>code-server.csr</code> 文件 → 勾选 <strong>"信任由此 CA 标识的网站"</strong> → 确定</li>
                <li>重启 Firefox</li>
              </ul>
            </div>
          </div>

          <div class="guide-step">
            <div class="step-number">4</div>
            <div class="step-content">
              <h3>Mac 系统（Safari / Chrome / Edge）导入证书</h3>
              <ul>
                <li>双击下载的 <code>code-server.csr</code> 文件，在"钥匙串访问"弹窗中选择 <strong>"系统"</strong> 钥匙串 → 点击"添加"（需输入系统密码）</li>
                <li>打开"钥匙串访问"应用，搜索证书域名，双击找到该证书</li>
                <li>展开 <strong>"信任"</strong> 区块，将 <strong>"使用此证书时"</strong> 改为 <strong>"始终信任"</strong></li>
                <li>关闭窗口，输入系统密码确认</li>
                <li>重启浏览器</li>
              </ul>
              <!-- <img src="/img/cert-guide/step5-mac.png" alt="Mac钥匙串" class="guide-img"> -->

            </div>
          </div>

          <div class="guide-done">
            <i class="el-icon-success"></i>
            <div>
              <strong>完成！</strong>
              <span>重启浏览器后回到本页刷新，IDE 即可正常加载，扩展插件可正常使用。若仍显示异常，请确认已完全退出浏览器，或联系运维人员。</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getNotebookList, createNotebook, updateNotebook, deleteNotebook, getNotebookDetail, stopNotebook, saveNotebook, resetNotebook, renewNotebook, getImagesOpt, getIdeServiceTask } from '@/api/llmops/notebook'
import { mapState } from 'vuex'

const IDE_NOTICE_STORAGE_KEY = 'llmops_notebook_ide_notice_hidden'

export default {
  name: 'Notebook',
  data() {
    return {
      iframeLoading: true,
      iframeSrc: '',
      ideDeploying: false,
      ideRefreshing: false,
      activeTab: 'ide',
      apiBase: "/api",
      previewSending: false,
      chatInput: '',
      chatPreviewMessages: [
        {
          role: "assistant",
          content:
            '你好！我是AI开发机器人，可以帮你完成各种环境操作。请问有什么可以帮助你的？例如："创建一个PyTorch环境"、"重启我的开发环境"、"查看环境资源使用情况"、"保存我的环境镜像"'
        }
      ],
      ideAssistantInput: '',
      ideAssistantSending: false,
      ideAssistantMessages: [
        {
          role: 'assistant',
          content: '我看到你正在创建一个简单的神经网络模型。需要我帮你完善代码吗？例如添加数据加载、训练循环或模型评估部分。'
        }
      ],
      filters: {
        name: '',
        creator: '',
        project: '',
        status: ''
      },
      environments: [],
      total: 0,           // 添加总记录数
      currentPage: 1,    // 当前页码
      pageSize: 10,      // 每页显示条数

      detailDialogVisible: false,
      detailLoading: false,
      notebookDetail: {},

      createDialogVisible: false,
      environmentForm: {
        project: '',
        name: '',
        describe: '',
        images: '',
        resource_memory: '',
        resource_cpu: '',
        resource_gpu: '',
        volume_mount: ''
      },
      editDialogVisible: false,
      editingEnvironment: {},
      editForm: {
        project: '',
        name: '',
        describe: '',
        images: '',
        resource_memory: '',
        resource_cpu: '',
        resource_gpu: '',
        volume_mount: ''
      },
      imageOptions: [],
      showIdeNotice: true,
      guideDrawerVisible: false,
      guideFullscreen: false,
      downloadBase: window.location.origin
    }
  },
  computed: {
    ...mapState('project', ['currentProject']),
  },
  mounted() {
    this.loadIdeNoticePreference()
    this.fetchIdeUrl()
    this.loadEnvironments()
    this.fetchImageOptions()
  },
  beforeDestroy() {
    if (this._ideTimer) clearInterval(this._ideTimer)
  },
  methods: {
    loadIdeNoticePreference() {
      try {
        this.showIdeNotice = window.localStorage.getItem(IDE_NOTICE_STORAGE_KEY) !== '1'
      } catch (error) {
        this.showIdeNotice = true
      }
    },
    hideIdeNotice() {
      this.showIdeNotice = false
      try {
        window.localStorage.setItem(IDE_NOTICE_STORAGE_KEY, '1')
      } catch (error) {
        // Ignore storage write failures and keep runtime behavior.
      }
    },
    restoreIdeNotice() {
      this.showIdeNotice = true
      try {
        window.localStorage.removeItem(IDE_NOTICE_STORAGE_KEY)
      } catch (error) {
        // Ignore storage write failures and keep runtime behavior.
      }
    },
    downloadCsr() {
      const a = document.createElement('a')
      a.href = '/video/certs/code-server.csr'
      a.download = this.isMac ? 'code-server.crt' : 'code-server.csr'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    },
    async fetchIdeUrl() {
      this.ideRefreshing = true
      try {
        const res = await getIdeServiceTask()
        const url = res && res.userIdeLink
        if (url) {
          this.iframeSrc = url
          this.ideDeploying = false
          if (this._ideTimer) { clearInterval(this._ideTimer); this._ideTimer = null }
        } else {
          this.iframeSrc = ''
          this.iframeLoading = false
          this.ideDeploying = true
          this.startIdePolling()
        }
      } catch {
        this.iframeSrc = ''
        this.iframeLoading = false
        this.ideDeploying = true
        this.startIdePolling()
      } finally {
        this.ideRefreshing = false
      }
    },
    startIdePolling() {
      if (this._ideTimer) return
      this._ideTimer = setInterval(() => {
        if (!this.iframeSrc) this.fetchIdeUrl()
      }, 15000)
    },
    onIframeLoad() {
      this.iframeLoading = false
    },
    // 格式化资源显示
    formatResource(row) {
      const cpu = row.resourceCpu || '0'
      const memory = row.resourceMemory || '0'
      const gpu = row.resourceGpu || '0'
      return `${cpu}(cpu)/${memory}(memory)/${gpu}(gpu)`
    },
    async loadEnvironments() {
      if (!this.currentProject || !this.currentProject.name) {
        console.warn('当前项目未选择')
        return
      }
      try {
        const res = await getNotebookList({
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          namespace: this.currentProject.name,
          ...this.filters  // 传递筛选条件
        })
        if (res && res.resultData) {
          this.environments = (res.resultData || []).map(item => {
            if (item.expand && typeof item.expand === 'string') {
              try {
                item.expand = JSON.parse(item.expand)
              } catch (e) {
                console.warn('解析expand字段失败', e)
              }
            }
            return item
          })
          this.total = res.totalRecord || 0
        }
      } catch (error) {
        console.error('加载环境列表失败', error)
        this.$message.error('加载环境列表失败')
      }
    },
    async fetchImageOptions() {

      try {
        const res = await getImagesOpt()
        if (res) {
          this.imageOptions = res || []
        }
      } catch (error) {
        console.error('获取镜像列表失败', error)
      }
    },
    async sendMessage() {
      const text = (this.chatInput || "").trim();
      if (!text || this.previewSending) return;

      this.chatPreviewMessages.push({ role: "user", content: text });
      this.chatInput = "";
      this.previewSending = true;
      this.$nextTick(() => this.scrollPreviewBottom());

      const history = this.chatPreviewMessages.slice(0, -1);
      const aiMsg = { role: "assistant", content: "", loading: true };
      this.chatPreviewMessages.push(aiMsg);
      this.$nextTick(() => this.scrollPreviewBottom());

      try {
        await this.streamChat(text, history, token => {
          aiMsg.content += token;
          this.$nextTick(() => this.scrollPreviewBottom());
        });

        if (!aiMsg.content.trim()) aiMsg.content = "（模型未返回内容）";
      } catch (e) {
        aiMsg.content = `请求失败：${e.message || e}`;
      } finally {
        aiMsg.loading = false;
        this.previewSending = false;
        this.$nextTick(() => this.scrollPreviewBottom());
      }
    },
    scrollPreviewBottom() {
      const el = this.$refs.chatPreviewArea;
      if (el && el.scrollHeight !== undefined) {
        el.scrollTop = el.scrollHeight;
      }
    },
    async streamChat(message, history, onToken) {
      const resp = await fetch(`${this.apiBase}/chat/stream`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          message,
          history
        })
      });

      if (!resp.ok || !resp.body) {
        const t = await resp.text().catch(() => "");
        throw new Error(`HTTP ${resp.status} ${t}`);
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder("utf-8");
      let buffer = "";
      let done = false;

      while (!done) {
        const { value, done: readDone } = await reader.read();
        done = readDone;
        buffer += decoder.decode(value || new Uint8Array(), { stream: !done });

        const blocks = buffer.split("\n\n");
        buffer = blocks.pop() || "";

        for (const block of blocks) {
          const lines = block.split("\n");
          let event = "message";
          let data = "";

          for (const line of lines) {
            if (line.startsWith("event:")) event = line.slice(6).trim();
            if (line.startsWith("data:")) data += line.slice(5).trim();
          }

          if (!data) continue;

          let payload = {};
          try {
            payload = JSON.parse(data);
          } catch (e) {
            continue;
          }

          if (event === "token") {
            onToken(payload.token || "");
          } else if (event === "error") {
            throw new Error(payload.message || "流式请求异常");
          } else if (event === "done") {
            done = true;
            break;
          }
        }
      }
    },
    quickSuggestion(text) {
      this.chatInput = text
    },
    async sendIdeAssistantMessage() {
      const text = (this.ideAssistantInput || '').trim()
      if (!text || this.ideAssistantSending) return

      this.ideAssistantMessages.push({ role: 'user', content: text })
      this.ideAssistantInput = ''
      this.ideAssistantSending = true
      this.$nextTick(() => this.scrollIdeAssistantBottom())

      const history = this.ideAssistantMessages
        .slice(0, -1)
        .map(item => ({ role: item.role, content: item.content || '' }))
      const aiMsg = { role: 'assistant', content: '', loading: true }
      this.ideAssistantMessages.push(aiMsg)
      this.$nextTick(() => this.scrollIdeAssistantBottom())

      try {
        await this.streamChat(text, history, token => {
          aiMsg.content += token
          this.$nextTick(() => this.scrollIdeAssistantBottom())
        })
        if (!aiMsg.content.trim()) aiMsg.content = '（模型未返回内容）'
      } catch (e) {
        aiMsg.content = `请求失败：${e.message || e}`
      } finally {
        aiMsg.loading = false
        this.ideAssistantSending = false
        this.$nextTick(() => this.scrollIdeAssistantBottom())
      }
    },
    scrollIdeAssistantBottom() {
      const el = this.$refs.ideAssistantArea
      if (el && el.scrollHeight !== undefined) {
        el.scrollTop = el.scrollHeight
      }
    },
    showCreateDialog() {
      this.createDialogVisible = true
      this.environmentForm = {
        project: this.currentProject ? this.currentProject.id : '',
        name: '',
        describe: '',
        images: '',
        resource_memory: '',
        resource_cpu: '',
        resource_gpu: '',
        volume_mount: ''
      }
    },
    async handleCreateEnvironment() {
      if (!this.environmentForm.name.trim()) {
        this.$message.warning('请输入环境名称')
        return
      }
      if (!this.environmentForm.images) {
        this.$message.warning('请选择镜像')
        return
      }

      // 确保 resource_memory 以 G 结尾
      let memory = this.environmentForm.resource_memory.trim()
      if (memory && !memory.toUpperCase().endsWith('G')) {
        this.environmentForm.resource_memory = memory + 'G'
      }

      try {
        const res = await createNotebook(this.environmentForm)
        if (res.status === 0) {
          this.$message.success('环境创建成功')
          this.createDialogVisible = false
          await this.loadEnvironments()
        }
      } catch (error) {
        console.error('创建环境失败', error)
        this.$message.error('创建环境失败')
      }
    },
    handleSearch() {
      this.currentPage = 1  // 重置到第一页
      this.loadEnvironments()
    },
    async handleRestart(row) {
      this.$confirm(`确定要重置环境"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await resetNotebook(row.id)
          if (res.status === 0) {
            this.$message.success(`环境 ${row.name} 重置成功`)
            await this.loadEnvironments()
          } else {
            this.$message.error(res.message || '重置环境失败')
          }
        } catch (error) {
          this.$message.error('重置环境失败')
        }
      }).catch(() => { })
    },
    handleSave(row) {
      this.$confirm(`确定要保存环境"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await saveNotebook(row.id)
          if (res.status === 0) {
            this.$message.success('环境保存成功')
          } else {
            this.$message.error(res.message || '保存环境失败')
          }
        } catch (error) {
          this.$message.error('保存环境失败')
        }
      }).catch(() => { })
    },
    async handleRenew(row) {
      this.$confirm(`确定要续租环境"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await renewNotebook(row.id)
          if (res.status === 0) {
            this.$message.success('环境续租成功')
            await this.loadEnvironments()
          } else {
            this.$message.error(res.message || '续租环境失败')
          }
        } catch (error) {
          this.$message.error('续租环境失败')
        }
      }).catch(() => { })
    },
    handleEdit(row) {
      this.editingEnvironment = row
      this.editForm = {
        project: this.currentProject ? this.currentProject.id : '',
        name: row.name,
        describe: row.describe,
        images: row.images || 'PyTorch 2.0',
        resource_memory: row.resourceMemory || '',
        resource_cpu: row.resourceCpu || '',
        resource_gpu: row.resourceGpu || '',
        volume_mount: row.volumeMount || ''
      }
      this.editDialogVisible = true
    },
    async handleSaveEnvironment() {
      if (!this.editForm.name.trim()) {
        this.$message.warning('请输入环境名称')
        return
      }

      // 确保 resource_memory 以 G 结尾
      let memory = this.editForm.resource_memory.trim()
      if (memory && !memory.toUpperCase().endsWith('G')) {
        this.editForm.resource_memory = memory + 'G'
      }

      try {
        const res = await updateNotebook({
          id: this.editingEnvironment.id,
          ...this.editForm
        })
        if (res.status === 0) {
          this.$message.success('环境修改成功')
          this.editDialogVisible = false
          await this.loadEnvironments()
        }
      } catch (error) {
        console.error('修改环境失败', error)
        this.$message.error('修改环境失败')
      }
    },
    async handleStop(row) {
      this.$confirm(`确定要停止环境"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await stopNotebook(row.id)
          if (res.status === 0) {
            this.$message.success('停止指令已发送')
            await this.loadEnvironments()
          } else {
            this.$message.error(res.message || '停止环境失败')
          }
        } catch (error) {
          console.error('停止环境失败', error)
          this.$message.error('停止环境失败')
        }
      }).catch(() => { })
    },
    async handleDelete(row) {
      this.$confirm(`确定要删除环境"${row.name}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteNotebook(row.id)
          if (res.status === 0) {
            this.$message.success('删除成功')
            await this.loadEnvironments()
          }
        } catch (error) {
          console.error('删除环境失败', error)
          this.$message.error('删除环境失败')
        }
      }).catch(() => { })
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadEnvironments()
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1  // 重置到第一页
      this.loadEnvironments()
    },
    getIdeTagClass(type) {
      const map = {
        'VSCode': 'vscode-tag',
        'Jupyter': 'jupyter-tag'
      }
      return map[type] || 'vscode-tag'
    },
    getStatusClass(status) {
      return status === 'online' ? 'status-running' : 'status-stopped'
    },
    async handleOpen(row) {
      this.detailDialogVisible = true
      this.detailLoading = true
      try {
        const res = await getNotebookDetail(row.id)
        this.notebookDetail = res || {}
      } catch (error) {
        this.$message.error('获取详情失败')
        console.error(error)
      } finally {
        this.detailLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.notebook-detail-dialog {
  .detail-content {
    padding: 10px 20px;

    .detail-item {
      display: flex;
      margin-bottom: 15px;
      line-height: 1.5;

      .detail-label {
        width: 100px;
        font-weight: bold;
        color: #374151;
        text-align: right;
        flex-shrink: 0;
      }

      .detail-value {
        color: #4b5563;
        word-break: break-all;

        &.expand-json {
          background-color: #f9fafb;
          padding: 10px;
          border-radius: 4px;
          border: 1px solid #e5e7eb;
          width: 100%;

          pre {
            margin: 0;
            white-space: pre-wrap;
          }
        }
      }
    }
  }
}

.notebook-container {
  min-height: calc(100vh - 60px);
}

.notebook-content {
  // max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
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
.table-card,
.ai-assistant-card,
.info-card,
.ide-container {
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

/* 标签页 */
.main-tabs {
  margin-bottom: 24px;
  background-color: #fff;
  padding: 10px 20px;
  border-radius: 12px;
}

.main-tabs ::v-deep .el-tabs__header {
  margin: 0 0 20px 0;
}

.main-tabs ::v-deep .el-tabs__nav-wrap::after {
  display: none;
}

.main-tabs ::v-deep .el-tabs__item {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  padding: 0 24px;
  height: 44px;
  line-height: 44px;
}

.main-tabs ::v-deep .el-tabs__item.is-active {
  color: #2563eb;
  font-weight: 600;
}

.main-tabs ::v-deep .el-tabs__active-bar {
  background-color: #2563eb;
  height: 3px;
}

/* 搜索表单 */
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

/* 信息卡片 */
.info-card {
  background-color: #f9fafb;
  border: 1px dashed #d1d5db;
}

.info-text {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

/* AI助手 */
.assistant-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.assistant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.assistant-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assistant-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.assistant-status {
  font-size: 12px;
  padding: 2px 8px;
  background-color: #dcfce7;
  color: #16a34a;
  border-radius: 12px;
}

.chat-area {
  min-height: 80px;
  max-height: 220px;
  overflow-y: auto;
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.chat-message {
  display: flex;
  gap: 12px;
}

.user-message {
  justify-content: flex-end;
}

.chat-area .chat-message+.chat-message {
  margin-top: 12px;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #10b981, #059669);
}

.message-content {
  background-color: white;
  padding: 12px 16px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  font-size: 14px;
  color: #1f2937;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  white-space: pre-wrap;
  word-break: break-word;
}

.message-loading {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  line-height: 1;
  padding: 0;
  font-size: 0;
}

.message-loading i.el-icon-loading {
  font-size: 13px;
  line-height: 1;
  font-style: normal;
  display: inline-block;
  margin: 0 !important;
  padding: 0 !important;
  white-space: nowrap;
}

.message-loading-text {
  font-size: 13px;
  line-height: 1;
  white-space: nowrap;
}

.user-preview-content {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.chat-input-area {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.chat-input {
  flex: 1;
}

.send-btn {
  white-space: nowrap;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
}

.quick-suggestions {
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.suggestion-label {
  font-size: 12px;
  color: #6b7280;
  display: block;
  margin-bottom: 8px;
}

.suggestion-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.suggestion-btn {
  background-color: #f3f4f6 !important;
  color: #374151;
  border: none !important;
}

.suggestion-btn:hover {
  background-color: #e5e7eb;
  color: #1f2937;
}

/* IDE容器 */
.ide-container {
  display: flex;
  gap: 16px;
  min-height: 500px;
  padding: 0;
  overflow: hidden;
}

.ide-left {
  width: 220px;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.file-tree-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.file-tree {
  padding: 12px 16px;
  flex: 1;
  overflow-y: auto;
}

.tree-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 4px;
  font-size: 13px;
  color: #4b5563;
  cursor: pointer;
}

.tree-item:hover {
  background-color: #f3f4f6;
}

.tree-item-indent {
  padding-left: 28px;
}

.active-file {
  color: #2563eb;
  font-weight: 500;
}

.ide-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.code-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-bottom: 1px solid #e5e7eb;
}

.editor-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  color: #374151;
  background-color: #f9fafb;
}

.file-name {
  font-weight: 500;
}

.code-content {
  flex: 1;
  background-color: #1e1e1e;
  padding: 20px;
  overflow: auto;
}

.code-content pre {
  margin: 0;
}

.code-content code {
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.ai-assistant-ide {
  padding: 16px;
  background-color: white;
  border-top: 1px solid #e5e7eb;
}

.assistant-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.assistant-name-small {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.ide-chat-area {
  max-height: 250px;
  background-color: #f9fafb;
  margin-bottom: 12px;
  padding: 12px;
}

.robot-features {
  margin: 10px 0 0 0;
  padding-left: 20px;

  li {
    font-size: 13px;
    color: #4b5563;
    margin-bottom: 6px;
    list-style-type: disc;
  }
}

.robot-question {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e5e7eb;
}

/* 表格标签 */
.ide-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.vscode-tag {
  background-color: #dbeafe;
  color: #2563eb;
}

.jupyter-tag {
  background-color: #fef3c7;
  color: #d97706;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-running {
  background-color: #dcfce7;
  color: #16a34a;
}

.status-stopped {
  background-color: #f3f4f6;
  color: #6b7280;
}

.resource-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.resource-tag {
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
  background-color: #f3f4f6;
  color: #6b7280;
}

/* 分页 */
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

@media (max-width: 1200px) {
  .search-form {
    grid-template-columns: repeat(3, 1fr);
  }

  .ide-container {
    flex-direction: column;
  }

  .ide-left {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
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

  .pagination-area {
    flex-direction: column;
    gap: 12px;
  }
}

.name-link {
  color: #409EFF;
  cursor: pointer;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.ide-iframe {
  width: 100%;
  height: 600px;
}

.no-environment-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 340px;
  color: #909399;
  gap: 12px;

  i.el-icon-monitor {
    font-size: 52px;
    color: #c0c4cc;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

.deploy-spin {
  font-size: 44px;
  color: #409eff;
  animation: none;

  i {
    font-size: 44px;
    animation: rotating 1.5s linear infinite;
  }
}

.deploy-msg {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.deploy-sub {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* IDE 提示条 */
.ide-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 10px 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #92400e;
  line-height: 1.5;
}

.ide-notice i {
  font-size: 15px;
  flex-shrink: 0;
}

.ide-notice-text {
  flex: 1;
}

.notice-link {
  font-size: 13px;
  padding: 0;
  color: #d97706;
  font-weight: 600;
  text-decoration: underline;
}

.notice-close {
  flex-shrink: 0;
  padding: 0;
  color: #92400e;
}

.notice-close i {
  font-size: 14px;
}

.restore-notice-btn {
  padding: 0;
  color: #d97706;
}

/* ===== 操作指南抽屉 ===== */
.guide-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.cert-guide-drawer {
  overflow: auto;
}

.guide-wrapper {
  padding: 24px 28px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.guide-wrapper.guide-fullscreen {
  max-width: 800px;
  margin: 0 auto;
}

.guide-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.guide-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.guide-header-actions {
  display: flex;
  gap: 8px;
}

.guide-header-btn {
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 16px;
  color: #6b7280;
}

.guide-header-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.guide-body {
  flex: 1;
  overflow-y: auto;
}

.guide-intro {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 28px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.guide-intro-icon {
  font-size: 22px;
  color: #2563eb;
  margin-top: 2px;
  flex-shrink: 0;
}

.guide-intro strong {
  font-size: 14px;
  color: #1e40af;
}

.guide-intro p {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #1e40af;
  line-height: 1.6;
}

.guide-intro p:last-child {
  margin-bottom: 0;
}

.code-block {
  background: #1e293b;
  color: #7dd3fc;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  padding: 10px 16px;
  border-radius: 8px;
  margin: 8px 0;
  word-break: break-all;
}

.guide-step {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
}

.step-number {
  width: 32px;
  height: 32px;
  min-width: 32px;
  border-radius: 50%;
  background: #2563eb;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
}

.step-content {
  flex: 1;
}

.step-content h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.step-content p {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
  margin: 0 0 8px 0;
}

.step-content ul {
  margin: 8px 0;
  padding-left: 20px;
  list-style: disc;
}

.step-content ul li {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.8;
}

.step-content code {
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #d97706;
}

.img-placeholder {
  background: #f9fafb;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  margin-top: 8px;
}

.guide-img {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-top: 8px;
}

.step-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.5;
}

.step-tip i {
  margin-top: 2px;
  font-size: 14px;
  flex-shrink: 0;
}

.step-tip.warn {
  background: #fffbeb;
  border: 1px solid #fde68a;
  color: #92400e;
}

.step-tip.info {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  color: #075985;
}

.guide-divider {
  text-align: center;
  margin: 32px 0;
  color: #9ca3af;
  font-size: 13px;
}

.guide-divider span {
  background: #fff;
  padding: 0 16px;
}

.download-link {
  display: inline-block;
  margin: 8px 0;
  text-decoration: none;
}

.download-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 12px 24px;
  font-size: 14px;
}

.guide-done {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 16px 20px;
  margin-top: 8px;
  font-size: 14px;
  color: #166534;
}

.guide-done i {
  font-size: 20px;
  color: #16a34a;
  flex-shrink: 0;
  margin-top: 2px;
}

.guide-done strong {
  display: block;
  margin-bottom: 4px;
}
</style>
