<template>
  <div v-loading="loading" class="knowledge-detail-page">
    <header class="detail-header">
      <h1>文档</h1>
      <p>知识库的所有文件都在这里显示，整个知识库都可以链接到应用引用或通过 Chat 插件进行索引。<a href="javascript:;" @click.prevent="showMoreInfo">了解更多 <i class="el-icon-top-right"></i></a></p>
    </header>

    <div class="toolbar">
      <el-select v-model="status" clearable placeholder="全部" size="small" class="status-select">
        <el-option label="全部" value="all" />
        <el-option label="可用" value="available" />
        <el-option label="处理中" value="indexing" />
        <el-option label="失败" value="error" />
        <el-option label="已停用" value="disabled" />
        <el-option label="已归档" value="archived" />
      </el-select>
      <el-input v-model.trim="keyword" clearable prefix-icon="el-icon-search" placeholder="搜索" size="small" class="search-input" />
      <el-dropdown trigger="click" @command="sortBy = $event">
        <el-button class="sort-button" size="small">排序：{{ sortLabel }} <i class="el-icon-arrow-down"></i></el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="created_at">上传时间</el-dropdown-item>
          <el-dropdown-item command="name">文件名称</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button class="sort-direction" size="small" icon="el-icon-sort" aria-label="切换排序方向" @click="descending = !descending" />
      <div class="header-actions">
        <el-button size="small" icon="el-icon-setting" @click="goToSettings">设置</el-button>
        <el-button v-if="documents.length" size="small" icon="el-icon-aim" @click="goToRetrieve">召回测试</el-button>
        <el-button size="small" icon="el-icon-edit-outline" @click="showMetadata">元数据</el-button>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="addFile">添加文件</el-button>
      </div>
    </div>

    <div v-if="!loading && loadError" class="document-empty-stage">
      <div class="empty-documents">
        <span class="empty-icon"><i class="el-icon-warning-outline"></i></span>
        <h2>文档加载失败</h2>
        <p>{{ loadError }}</p>
        <el-button size="small" icon="el-icon-refresh" @click="loadDocuments">重新加载</el-button>
      </div>
    </div>

    <div v-else-if="!loading && filteredDocuments.length" class="table-wrap">
      <el-table :data="filteredDocuments" class="document-table" size="small" :row-key="row => row.id || row.name" @selection-change="selectedDocuments = $event">
        <el-table-column type="selection" width="38" />
        <el-table-column type="index" label="#" width="34" />
        <el-table-column label="名称" min-width="230">
          <template slot-scope="scope"><button type="button" class="document-name" @click="openDocument(scope.row)"><i class="el-icon-document"></i>{{ documentName(scope.row) }}</button></template>
        </el-table-column>
        <el-table-column label="分段模式" min-width="120">
          <template slot-scope="scope"><span class="mode-badge"><i class="el-icon-s-unfold"></i>{{ segmentMode(scope.row) }}</span></template>
        </el-table-column>
        <el-table-column label="字符数" min-width="88">
          <template slot-scope="scope">{{ formatCount(scope.row.word_count || scope.row.character_count || scope.row.char_count) }}</template>
        </el-table-column>
        <el-table-column label="召回次数" min-width="100">
          <template slot-scope="scope">{{ scope.row.hit_count || scope.row.retrieval_count || 0 }}</template>
        </el-table-column>
        <el-table-column label="上传时间" min-width="155">
          <template slot-scope="scope">{{ formatTime(scope.row.created_at || scope.row.uploaded_at) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="95">
          <template slot-scope="scope"><span class="status-label" :class="statusClass(scope.row)"><i></i>{{ statusLabel(scope.row) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="118" align="center">
          <template slot-scope="scope">
            <el-tooltip :content="isArchived(scope.row) ? '请先取消归档' : (isDocumentEnabled(scope.row) ? '停用文档' : '启用文档')" placement="top"><span><el-switch :value="isDocumentEnabled(scope.row)" :disabled="!!rowBusy[scope.row.id] || isArchived(scope.row) || !canChangeStatus(scope.row)" active-color="#1649f5" @change="toggleDocumentEnabled(scope.row, $event)" /></span></el-tooltip>
            <span class="action-divider"></span>
            <el-dropdown trigger="click" :disabled="!!rowBusy[scope.row.id]" @command="handleDocumentCommand($event, scope.row)">
              <button type="button" class="action-more" :disabled="!!rowBusy[scope.row.id]" :aria-label="'管理文档 ' + documentName(scope.row)"><i :class="rowBusy[scope.row.id] ? 'el-icon-loading' : 'el-icon-more'"></i></button>
              <el-dropdown-menu slot="dropdown" class="knowledge-document-menu">
                <el-dropdown-item command="rename" icon="el-icon-edit" disabled title="缺少文档重命名接口">重命名 <small>待接口</small></el-dropdown-item>
                <el-dropdown-item command="summary" icon="el-icon-document-add" disabled title="缺少文档摘要生成及进度查询接口">生成摘要 <small>待接口</small></el-dropdown-item>
                <el-dropdown-item command="segmentation" icon="el-icon-set-up">分段设置 <small>仅查看</small></el-dropdown-item>
                <el-dropdown-item command="download" icon="el-icon-download">下载</el-dropdown-item>
                <el-dropdown-item :command="isArchived(scope.row) ? 'un_archive' : 'archive'" icon="el-icon-box" divided :disabled="!canChangeStatus(scope.row)">{{ isArchived(scope.row) ? '取消归档' : '归档' }}</el-dropdown-item>
                <el-dropdown-item command="delete" icon="el-icon-delete" class="document-delete-menu-item">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="selectedDocuments.length" class="selection-count">已选择 {{ selectedDocuments.length }} 个文档</div>
    </div>

    <div v-else-if="!loading && !documents.length" class="document-empty-stage">
      <div class="empty-documents">
        <span class="empty-icon"><i class="el-icon-folder-add"></i></span>
        <h2>还没有文档</h2>
        <p>您可以上传文件，从网站同步，或者从网络应用程序（如 Notion、GitHub 等）同步。</p>
        <el-button size="small" icon="el-icon-plus" @click="addFile">添加文件</el-button>
      </div>
    </div>
    <div v-else-if="!loading" class="document-empty-stage">
      <div class="empty-documents">
        <span class="empty-icon"><i class="el-icon-search"></i></span>
        <h2>没有找到匹配的文档</h2>
        <p>请调整搜索关键词或文档状态后重试。</p>
        <el-button size="small" @click="clearFilters">清除筛选</el-button>
      </div>
    </div>
    <el-dialog title="文档信息" :visible.sync="infoVisible" width="480px">
      <div v-if="activeDocument" class="document-info-dialog">
        <p><span>名称</span>{{ documentName(activeDocument) }}</p>
        <p><span>状态</span>{{ statusLabel(activeDocument) }}</p>
        <p><span>字符数</span>{{ formatCount(activeDocument.word_count || activeDocument.character_count || activeDocument.char_count) }}</p>
        <p><span>上传时间</span>{{ formatTime(activeDocument.created_at || activeDocument.uploaded_at) }}</p>
      </div>
    </el-dialog>
    <el-dialog :title="actionTitle" :visible.sync="actionDialogVisible" width="500px" custom-class="document-action-dialog" :close-on-click-modal="false" :close-on-press-escape="!performingAction" :show-close="!performingAction" @closed="actionTarget = null">
      <p class="action-description">{{ actionDescription }}</p>
      <span slot="footer"><el-button :disabled="performingAction" @click="actionDialogVisible = false">取消</el-button><el-button :type="pendingAction === 'delete' ? 'danger' : 'primary'" :loading="performingAction" @click="confirmDocumentAction">{{ pendingAction === 'delete' ? '删除' : '确定' }}</el-button></span>
    </el-dialog>
    <el-dialog title="分段设置" :visible.sync="segmentationVisible" width="560px" custom-class="document-action-dialog">
      <div v-loading="loadingSegmentation" class="segmentation-detail">
        <p class="segmentation-document-name">{{ ruleDocument ? documentName(ruleDocument) : '' }}</p>
        <div v-if="segmentationError" class="segmentation-error">{{ segmentationError }} <el-button type="text" @click="loadSegmentation">重试</el-button></div>
        <template v-else-if="!loadingSegmentation && ruleDocument">
          <dl class="segmentation-values"><dt>分段模式</dt><dd>{{ segmentMode(ruleDocument) }}</dd><dt>处理方式</dt><dd>{{ processModeLabel }}</dd><dt>分段标识符</dt><dd><code>{{ separatorLabel }}</code></dd><dt>分段最大长度</dt><dd>{{ segmentationRule.max_tokens == null ? '—' : segmentationRule.max_tokens }}</dd><dt>分段重叠长度</dt><dd>{{ segmentationRule.chunk_overlap == null ? '—' : segmentationRule.chunk_overlap }}</dd></dl>
          <div class="preprocessing-rules"><strong>文本清洗规则</strong><el-checkbox v-for="rule in preprocessingRules" :key="rule.id" :value="!!rule.enabled" disabled>{{ preprocessingLabel(rule.id) }}</el-checkbox><p v-if="!preprocessingRules.length">接口未返回文本清洗规则</p></div>
          <el-alert title="当前仅支持查看分段配置" description="缺少不重新上传文件即可更新分段规则并重建索引的接口；现有 PUT 文档接口要求上传文件。" type="info" :closable="false" show-icon />
        </template>
      </div>
      <span slot="footer"><el-button @click="segmentationVisible = false">关闭</el-button><el-button type="primary" disabled>保存（待接口）</el-button></span>
    </el-dialog>
    <knowledge-metadata-manager :visible.sync="metadataVisible" :knowledge-base-id="$route.params.id" />
  </div>
</template>

<script>
import { getKnowledgeDocumentList, getKnowledgeDocument, getKnowledgeDocumentDownload, updateKnowledgeDocumentStatus, deleteKnowledgeDocument } from '@/api/llmops/knowledge'
import KnowledgeMetadataManager from './metadata-manager.vue'

export default {
  name: 'KnowledgeDetail',
  components: { KnowledgeMetadataManager },
  data() {
    return {
      loading: false,
      metadataVisible: false,
      loadError: '',
      documents: [],
      keyword: '',
      status: 'all',
      sortBy: 'created_at',
      descending: true,
      selectedDocuments: [],
      activeDocument: null,
      infoVisible: false,
      rowBusy: {}, actionDialogVisible: false, actionTarget: null, actionBaseId: '', pendingAction: '', performingAction: false,
      segmentationVisible: false, loadingSegmentation: false, segmentationError: '', ruleDocument: null, ruleTargetId: '', ruleRequestVersion: 0, listRequestVersion: 0
    }
  },
  computed: {
    actionTitle() { return { delete: '删除文档', archive: '归档文档', un_archive: '取消归档' }[this.pendingAction] || '文档操作' },
    actionDescription() {
      const name = this.actionTarget ? this.documentName(this.actionTarget) : ''
      if (this.pendingAction === 'delete') return `确定删除文档“${name}”吗？文档及其分段和索引将从平台和后端删除，此操作不可恢复。`
      if (this.pendingAction === 'archive') return `确定归档文档“${name}”吗？归档后文档将不再参与检索，文档内容会保留，可取消归档。`
      return `确定取消“${name}”的归档状态吗？恢复后文档将按启用状态重新参与检索。`
    },
    documentProcessRule() { return this.ruleDocument && (this.ruleDocument.document_process_rule || this.ruleDocument.dataset_process_rule || this.ruleDocument.process_rule) || {} },
    segmentationRule() { return this.documentProcessRule.rules && this.documentProcessRule.rules.segmentation || {} },
    preprocessingRules() { const rules = this.documentProcessRule.rules && this.documentProcessRule.rules.pre_processing_rules; return Array.isArray(rules) ? rules : [] },
    processModeLabel() { const mode = this.documentProcessRule.mode; return { automatic: '自动', custom: '自定义', hierarchical: '父子分段' }[mode] || mode || '—' },
    separatorLabel() { const rule = this.segmentationRule; const separator = rule.separator !== undefined ? rule.separator : rule.delimiter; return separator == null ? '—' : JSON.stringify(separator) },
    sortLabel() { return this.sortBy === 'name' ? '文件名称' : '上传时间' },
    filteredDocuments() {
      const keyword = this.keyword.toLowerCase()
      const list = this.documents.filter(document => {
        const name = this.documentName(document)
        const documentStatus = this.documentStatus(document)
        return (!keyword || name.toLowerCase().includes(keyword)) &&
          (!this.status || this.status === 'all' || documentStatus === this.status || (this.status === 'available' && documentStatus === 'completed'))
      })
      return list.sort((a, b) => {
        const first = this.sortBy === 'name' ? this.documentName(a) : (a.created_at || a.uploaded_at || '')
        const second = this.sortBy === 'name' ? this.documentName(b) : (b.created_at || b.uploaded_at || '')
        return String(first).localeCompare(String(second)) * (this.descending ? -1 : 1)
      })
    }
  },
  created() { this.loadDocuments() },
  activated() { if (!this.loading && this.$route.name === 'knowledge-detail') this.loadDocuments() },
  watch: { '$route.params.id'() { this.metadataVisible = false; if (this.$route.name === 'knowledge-detail') { this.segmentationVisible = false; this.actionDialogVisible = false; this.loadDocuments() } } },
  methods: {
    unwrap(result) { return result && result.data !== undefined ? result.data : result },
    async loadDocuments() {
      const id = this.$route.params.id
      const version = ++this.listRequestVersion
      this.loading = true
      this.loadError = ''
      try {
        const result = await getKnowledgeDocumentList(id, { page: 1, limit: 100 })
        const payload = result && result.data !== undefined ? result.data : result
        const list = Array.isArray(payload) ? payload : payload && (payload.data || payload.items || payload.results)
        if (!Array.isArray(list)) throw new Error('文档列表响应格式异常，请稍后重试')
        if (id !== this.$route.params.id || version !== this.listRequestVersion) return false
        this.documents = list
        this.selectedDocuments = []
        return true
      } catch (e) {
        if (id !== this.$route.params.id || version !== this.listRequestVersion) return false
        this.documents = []
        this.loadError = e.message || '暂时无法获取文档，请稍后重试'
        return false
      } finally {
        if (version === this.listRequestVersion) this.loading = false
      }
    },
    handleDocumentCommand(command, document) {
      if (!document.id || this.rowBusy[document.id]) return
      if (command === 'download') this.downloadDocument(document)
      else if (command === 'segmentation') this.openSegmentation(document)
      else if (['delete', 'archive', 'un_archive'].includes(command)) {
        this.actionTarget = { ...document }
        this.actionBaseId = this.$route.params.id
        this.pendingAction = command
        this.actionDialogVisible = true
      }
    },
    async downloadDocument(document) {
      if (!document.id || this.rowBusy[document.id]) return
      const id = document.id
      this.$set(this.rowBusy, id, true)
      try {
        const result = this.unwrap(await getKnowledgeDocumentDownload(this.$route.params.id, id))
        if (!result || typeof result.url !== 'string' || !result.url.trim()) throw new Error('下载接口未返回有效的文件地址')
        const url = new URL(result.url, window.location.origin)
        if (!['http:', 'https:'].includes(url.protocol)) throw new Error('下载地址协议不受支持')
        const link = window.document.createElement('a')
        link.href = url.href
        link.download = this.documentName(document)
        link.target = '_blank'
        link.rel = 'noopener noreferrer'
        window.document.body.appendChild(link)
        try { link.click() } finally { link.remove() }
      } catch (e) { this.$message.error(e.message || '下载地址获取失败，请稍后重试') }
      finally { this.$delete(this.rowBusy, id) }
    },
    async changeDocumentStatus(document, action) {
      if (!document.id || this.rowBusy[document.id]) return false
      const id = document.id
      const baseId = this.$route.params.id
      this.$set(this.rowBusy, id, true)
      try {
        await updateKnowledgeDocumentStatus(baseId, action, [id])
        const fresh = this.unwrap(await getKnowledgeDocument(baseId, id))
        if (baseId !== this.$route.params.id) return false
        if (!fresh || !fresh.id) throw new Error('操作已提交，但无法确认文档最新状态，请刷新列表')
        this.documents = this.documents.map(item => item.id === id ? { ...item, ...fresh, id } : item)
        const applied = action === 'archive' ? fresh.archived === true : action === 'un_archive' ? fresh.archived === false : fresh.enabled === (action === 'enable')
        if (!applied) throw new Error('接口已响应，但文档状态尚未生效，请稍后刷新确认')
        return true
      } catch (e) {
        const data = e.response && e.response.data
        const message = data && data.message || e.message || '文档状态更新失败'
        this.$message.error(`${message}；若刚进行过状态操作，请等待索引同步后重试`)
        return false
      } finally { this.$delete(this.rowBusy, id) }
    },
    async toggleDocumentEnabled(document, enabled) {
      if (!this.canChangeStatus(document) || this.isArchived(document)) return
      if (await this.changeDocumentStatus(document, enabled ? 'enable' : 'disable')) this.$message.success(enabled ? '文档已启用' : '文档已停用')
    },
    async confirmDocumentAction() {
      if (!this.actionTarget || this.performingAction || this.rowBusy[this.actionTarget.id]) return
      const target = this.actionTarget
      const action = this.pendingAction
      const baseId = this.actionBaseId
      if (baseId !== this.$route.params.id) { this.actionDialogVisible = false; return }
      this.performingAction = true
      try {
        if (action === 'delete') {
          this.$set(this.rowBusy, target.id, true)
          await deleteKnowledgeDocument(baseId, target.id)
          if (baseId !== this.$route.params.id) return
          this.documents = this.documents.filter(item => item.id !== target.id)
          this.actionDialogVisible = false
          const refreshed = await this.loadDocuments()
          if (!refreshed) this.$message.warning('删除请求已完成，但列表刷新失败，请重新加载确认')
          else if (this.documents.some(item => item.id === target.id)) this.$message.warning('删除接口已响应，但列表仍返回该文档，请联系后端确认')
          else this.$message.success('文档已删除')
        } else if (await this.changeDocumentStatus(target, action)) {
          this.actionDialogVisible = false
          this.$message.success(action === 'archive' ? '文档已归档' : '已取消归档')
        }
      } catch (e) { this.$message.error(e.message || '文档操作失败，请重试') }
      finally { this.performingAction = false; this.$delete(this.rowBusy, target.id) }
    },
    openSegmentation(document) { this.ruleTargetId = document.id; this.ruleDocument = { ...document }; this.segmentationVisible = true; this.loadSegmentation() },
    async loadSegmentation() {
      const id = this.ruleTargetId
      const baseId = this.$route.params.id
      const version = ++this.ruleRequestVersion
      this.loadingSegmentation = true
      this.segmentationError = ''
      try {
        const detail = this.unwrap(await getKnowledgeDocument(baseId, id))
        if (version !== this.ruleRequestVersion || baseId !== this.$route.params.id) return
        if (!detail || !detail.id) throw new Error('文档配置响应格式异常')
        this.ruleDocument = detail
      } catch (e) { if (version === this.ruleRequestVersion) this.segmentationError = e.message || '分段设置加载失败' }
      finally { if (version === this.ruleRequestVersion) this.loadingSegmentation = false }
    },
    preprocessingLabel(id) { return { remove_extra_spaces: '替换连续的空格、换行符和制表符', remove_urls_emails: '删除 URL 和电子邮件地址' }[id] || id },
    goBack() { this.$router.push({ name: 'knowledge', query: this.$route.query }) },
    goToRetrieve() { this.$router.push({ name: 'knowledge-retrieve', params: { id: this.$route.params.id }, query: this.$route.query }) },
    goToSettings() { this.$router.push({ name: 'knowledge-settings', params: { id: this.$route.params.id }, query: this.$route.query }) },
    addFile() {
      this.$router.push({
        name: 'knowledge-document-create',
        params: { id: this.$route.params.id },
        query: this.$route.query
      })
    },
    clearFilters() { this.keyword = ''; this.status = 'all' },
    openDocument(document) {
      if (!document.id) return this.$message.warning('该文档缺少 ID，无法查看详情')
      this.$router.push({
        name: 'knowledge-document-detail',
        params: { id: this.$route.params.id, documentId: document.id },
        query: { ...this.$route.query, documentName: this.documentName(document) }
      })
    },
    showMetadata() { this.metadataVisible = true },
    showMoreInfo() { this.$message.info('文档可在知识库中用于检索和引用') },
    showDocumentInfo(document) { this.activeDocument = document; this.infoVisible = true },
    documentName(document) { return document.name || document.file_name || document.filename || '未命名文档' },
    segmentMode(document) { return { qa_model: '问答', hierarchical_model: '父子' }[document.doc_form] || '通用' },
    formatCount(value) {
      const count = Number(value) || 0
      return count >= 1000 ? `${(count / 1000).toFixed(1)}k` : String(count)
    },
    isArchived(document) { return document.archived === true || document.display_status === 'archived' },
    isDocumentEnabled(document) { return !this.isArchived(document) && document.enabled !== false && document.display_status !== 'disabled' },
    canChangeStatus(document) { return !!document.id && ['completed', 'available', 'disabled', 'archived'].includes(document.indexing_status || document.display_status || document.status) },
    documentStatus(document) { return this.isArchived(document) ? 'archived' : document.enabled === false ? 'disabled' : document.display_status || document.indexing_status || document.status || 'available' },
    isAvailable(document) { return ['available', 'completed'].includes(this.documentStatus(document)) },
    statusClass(document) { return ['archived', 'disabled'].includes(this.documentStatus(document)) ? 'is-muted' : this.isAvailable(document) ? 'is-available' : (this.statusType(document) === 'danger' ? 'is-error' : 'is-pending') },
    statusLabel(document) {
      const status = this.documentStatus(document)
      return { available: '可用', completed: '可用', indexing: '处理中', parsing: '解析中', cleaning: '清洗中', splitting: '分段中', waiting: '等待中', queuing: '排队中', paused: '已暂停', error: '失败', disabled: '已停用', archived: '已归档' }[status] || status || '可用'
    },
    statusType(document) {
      const status = this.documentStatus(document)
      if (status === 'error') return 'danger'
      if (status === 'indexing' || status === 'waiting') return 'warning'
      return 'success'
    },
    formatTime(value) {
      if (!value) return '—'
      const date = new Date(typeof value === 'number' && value < 100000000000 ? value * 1000 : value)
      if (Number.isNaN(date.getTime())) return '—'
      const pad = number => String(number).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    }
  }
}
</script>

<style scoped>
.knowledge-detail-page { position: relative; display: flex; flex-direction: column; min-height: max(620px, calc(100vh - 112px)); padding: 20px 24px 40px; color: #17233d; background: #fff; box-sizing: border-box; }
.detail-header h1 { margin: 0; color: #101828; font-size: 20px; font-weight: 600; }
.detail-header p { margin: 8px 0 0; color: #667085; font-size: 14px; line-height: 1.5; }
.detail-header a { margin-left: 5px; color: #1649f5; text-decoration: none; }.detail-header a i { font-size: 11px; }
.toolbar { display: flex; align-items: center; gap: 10px; margin: 20px 0 25px; }
.status-select { width: 180px; }.search-input { width: 225px; }.sort-button { border-radius: 8px 0 0 8px; }.sort-direction { margin-left: -11px !important; border-radius: 0 8px 8px 0; }
.header-actions { display: flex; gap: 10px; margin-left: auto; }.header-actions .el-button { margin: 0; border-radius: 8px; }.header-actions .el-button--primary { background: #1649f5; border-color: #1649f5; }
.table-wrap { width: 100%; overflow-x: auto; }.document-table { min-width: 950px; color: #344054; font-size: 13px; }
.document-name { display: inline-flex; align-items: center; gap: 9px; padding: 0; color: #344054; background: none; border: 0; cursor: pointer; font: inherit; text-align: left; }.document-name:hover { color: #1649f5; }.document-name i { color: #409eff; font-size: 16px; }
.mode-badge { display: inline-flex; align-items: center; gap: 3px; padding: 2px 5px; border: 1px solid #d9e0ea; border-radius: 4px; color: #667085; font-size: 11px; }
.status-label { display: inline-flex; align-items: center; gap: 8px; }.status-label i { width: 8px; height: 8px; border-radius: 50%; background: #f5a623; }
.status-label.is-available { color: #08a66c; }.status-label.is-available i { background: #31c68a; box-shadow: 0 1px 5px #bcefd9; }
.status-label.is-error { color: #e5484d; }.status-label.is-error i { background: #e5484d; }
.status-label.is-muted { color: #98a2b3; }.status-label.is-muted i { background: #98a2b3; }
.action-divider { display: inline-block; height: 15px; margin: 0 8px; border-left: 1px solid #e5e9ef; vertical-align: middle; }.action-more { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; padding: 0; color: #667085; background: transparent; border: 0; border-radius: 6px; cursor: pointer; font-size: 16px; vertical-align: middle; }.action-more:hover, .action-more:focus { color: #1649f5; background: #edf2ff; outline: none; }.action-more:disabled { cursor: default; color: #98a2b3; }
.action-description { margin: 0; color: #667085; font-size: 14px; line-height: 1.8; overflow-wrap: anywhere; }.segmentation-detail { min-height: 180px; }.segmentation-document-name { margin: 0 0 18px; color: #344054; overflow-wrap: anywhere; }.segmentation-values { display: grid; grid-template-columns: 120px minmax(0, 1fr); gap: 15px; margin: 0 0 22px; font-size: 13px; }.segmentation-values dt { color: #98a2b3; }.segmentation-values dd { margin: 0; color: #344054; overflow-wrap: anywhere; }.segmentation-values code { white-space: pre-wrap; }.preprocessing-rules { display: grid; gap: 12px; margin-bottom: 22px; }.preprocessing-rules strong { color: #344054; font-size: 13px; }.preprocessing-rules p { color: #98a2b3; font-size: 12px; }.segmentation-error { padding: 12px; color: #b54708; background: #fffaeb; border-radius: 6px; }
.selection-count { margin-top: 10px; color: #667085; font-size: 12px; }.document-info-dialog p { display: flex; gap: 18px; }.document-info-dialog p span { width: 72px; color: #98a2b3; flex: none; }
.document-empty-stage { display: flex; flex: 1; align-items: center; justify-content: center; min-height: 380px; padding: 48px 0; box-sizing: border-box; }
.empty-documents { width: min(590px, 100%); text-align: left; }.empty-icon { display: flex; align-items: center; justify-content: center; width: 48px; height: 48px; margin-bottom: 12px; color: #60708b; border: 1px solid #dfe4ec; border-radius: 9px; font-size: 21px; }.empty-documents h2 { margin: 0 0 9px; font-size: 18px; }.empty-documents p { margin: 0 0 18px; color: #667085; font-size: 14px; line-height: 1.6; }.empty-documents .el-button { height: 36px; color: #1649f5; border-radius: 8px; }
.knowledge-detail-page /deep/ .search-input .el-input__inner { background: #f3f5f8; border-color: #f3f5f8; border-radius: 8px; }
.knowledge-detail-page /deep/ .search-input .el-input__inner:focus { background: #fff; border-color: #3157ef; }
.knowledge-detail-page /deep/ .document-table th { color: #667085; background: #fff; font-weight: 500; }.knowledge-detail-page /deep/ .document-table td { padding: 5px 0; }.knowledge-detail-page /deep/ .document-table::before { background: #edf0f4; }
.knowledge-detail-page /deep/ .document-table .el-switch.is-disabled { opacity: 1; }.knowledge-detail-page /deep/ .document-table .el-switch.is-disabled .el-switch__core { cursor: default; }
@media (max-width: 760px) { .knowledge-detail-page { padding: 18px 16px 32px; }.toolbar { flex-wrap: wrap; }.status-select, .search-input { width: calc(50% - 5px); }.header-actions { width: 100%; justify-content: flex-end; }.detail-header p { line-height: 1.6; }.document-empty-stage { min-height: 320px; } }
</style>

<style>
.knowledge-document-menu { min-width: 205px; padding: 6px 0; border: 1px solid #edf0f4; border-radius: 12px; box-shadow: 0 6px 20px rgba(16, 24, 40, .1); }.knowledge-document-menu .el-dropdown-menu__item { padding: 0 17px; color: #526079; line-height: 40px; }.knowledge-document-menu .el-dropdown-menu__item i { margin-right: 10px; }.knowledge-document-menu .el-dropdown-menu__item small { float: right; margin-left: 22px; color: #98a2b3; font-size: 11px; }.knowledge-document-menu .el-dropdown-menu__item.is-disabled { color: #b1b8c5; }.knowledge-document-menu .document-delete-menu-item:not(.is-disabled) { color: #e5484d; }.document-action-dialog { max-width: calc(100% - 32px); border-radius: 12px; }
</style>
