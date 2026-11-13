<template>
  <div class="document-detail-page">
    <aside class="document-sidebar">
      <div class="sidebar-breadcrumb"><button type="button" @click="goToKnowledgeList"><i class="el-icon-arrow-left"></i> 知识库</button></div>
      <div class="base-summary"><span class="base-icon"><i class="el-icon-collection"></i></span><div><strong>{{ knowledgeName }}</strong><small>知识库文档</small></div></div>
      <nav class="base-nav" aria-label="知识库导航">
        <button type="button" class="is-active" @click="goToDocuments"><i class="el-icon-document"></i> 文档</button>
        <button type="button" disabled><i class="el-icon-share"></i> 流水线</button>
        <button type="button" @click="goToRetrieve"><i class="el-icon-aim"></i> 召回测试</button>
        <button type="button" @click="goToSettings"><i class="el-icon-setting"></i> 设置</button>
      </nav>
      <div class="sidebar-bottom"><span>{{ documentCount }} 文档</span><span>访问 API <i class="el-icon-success"></i></span></div>
    </aside>

    <main class="document-main">
      <header class="document-header">
        <button type="button" class="back-button" aria-label="返回文档列表" @click="goToDocuments"><i class="el-icon-back"></i></button>
        <span class="file-icon"><i class="el-icon-document"></i></span>
        <div class="document-heading"><strong>{{ documentName }}</strong><small><i class="el-icon-s-unfold"></i> {{ segmentMode }}</small></div>
        <span class="header-spacer"></span>
        <el-tag size="mini" :type="documentStatusType">{{ documentStatus }}</el-tag>
        <el-dropdown split-button type="primary" size="small" :disabled="loadingDocument || !documentInfo.id || mutationsBusy" @click="openAdd('single')" @command="openAdd">
          <i class="el-icon-plus"></i> 添加分段
          <el-dropdown-menu slot="dropdown"><el-dropdown-item command="batch" icon="el-icon-upload2">批量添加</el-dropdown-item></el-dropdown-menu>
        </el-dropdown>
      </header>

      <div class="document-body">
        <section class="segments-panel" v-loading="loadingSegments">
          <div class="segments-toolbar">
            <strong><el-checkbox v-model="selectAll" :indeterminate="selectedIds.length > 0 && !selectAll" :disabled="!segments.length || mutationsBusy || loadingSegments" @change="toggleAll" /> {{ total }} 分段</strong>
            <div class="segment-filters">
              <el-select v-model="statusFilter" size="small" class="status-filter" :disabled="mutationsBusy" @change="resetAndLoad">
                <el-option label="全部" value="" /><el-option label="已完成" value="completed" /><el-option label="处理中" value="indexing" /><el-option label="失败" value="error" />
              </el-select>
              <el-input v-model.trim="keyword" size="small" clearable prefix-icon="el-icon-search" placeholder="搜索" class="segment-search" :disabled="mutationsBusy" @keyup.enter.native="resetAndLoad" @clear="resetAndLoad" />
              <el-button size="small" icon="el-icon-search" aria-label="搜索分段" :disabled="mutationsBusy" @click="resetAndLoad" />
            </div>
          </div>

          <el-alert v-if="segmentsError" :title="segmentsError" type="error" :closable="false" show-icon /><el-button v-if="segmentsError" type="text" @click="loadSegments">重新加载分段</el-button>
          <el-alert v-if="segmentOperationError" class="operation-alert" :title="segmentOperationError" type="warning" :closable="false" show-icon />
          <div v-if="!loadingSegments && segments.length" class="segment-list">
            <article v-for="(segment, index) in segments" :key="segment.id || index" class="segment-item" :class="{ 'is-selected': activeSegmentId === segment.id, 'is-checked': selectedIds.includes(segment.id) }">
              <el-checkbox :value="selectedIds.includes(segment.id)" :disabled="!segment.id || mutationsBusy" :aria-label="`选择分段 ${segment.position || index + 1}`" @change="toggleSegment(segment.id)" />
              <div class="segment-content">
                <div class="segment-meta"><span><i class="el-icon-menu"></i> 分段-{{ segment.position || segment.index || (page - 1) * pageSize + index + 1 }} <span class="meta-dot">·</span> {{ segmentLength(segment) }} 字符 <span class="meta-dot">·</span> {{ segment.hit_count || 0 }} 召回次数</span><span class="segment-status" :class="{ 'is-off': !segmentEnabled(segment) }">{{ segmentEnabled(segment) ? '已启用' : '未启用' }} <i></i></span></div>
                <div class="segment-tools" @click.stop>
                  <el-button type="text" icon="el-icon-edit" :disabled="!segment.id || mutationsBusy" title="编辑分段" aria-label="编辑分段" @click="openSegment(segment, index)" />
                  <el-button type="text" icon="el-icon-delete" :disabled="!segment.id || mutationsBusy" title="删除分段" aria-label="删除分段" @click="deleteSegment(segment)" />
                  <span class="tool-divider"></span>
                  <el-switch :value="segmentEnabled(segment)" :disabled="!segment.id || mutationsBusy" active-color="#1649f5" :aria-label="segmentEnabled(segment) ? '禁用分段' : '启用分段'" @change="setSegmentEnabled(segment, $event)" />
                </div>
                <p class="segment-text">{{ segment.content || segment.text || '暂无内容' }}</p>
                <knowledge-segment-image-gallery v-if="Array.isArray(segment.attachments) && segment.attachments.length" :attachments="segment.attachments" @retry="loadSegments" />
                <div v-if="segment.keywords && segment.keywords.length" class="keywords"><span v-for="(word, wordIndex) in segment.keywords" :key="wordIndex"># {{ word }}</span></div>
              </div>
            </article>
          </div>
          <div v-else-if="!loadingSegments" class="segment-empty">{{ keyword || statusFilter ? '没有匹配的分段' : '这个文档暂无分段' }}</div>
          <div v-if="selectedIds.length" class="segment-batch-bar" role="toolbar" aria-label="批量操作分段"><strong>{{ selectedIds.length }}</strong><span>已选择</span><span class="tool-divider"></span><el-button type="text" icon="el-icon-circle-check" :disabled="mutationsBusy || loadingSegments" @click="runSegmentAction('enable', selectedIds, true)">启用</el-button><el-button type="text" icon="el-icon-circle-close" :disabled="mutationsBusy || loadingSegments" @click="runSegmentAction('disable', selectedIds, true)">禁用</el-button><el-button type="text" class="batch-delete" icon="el-icon-delete" :disabled="mutationsBusy || loadingSegments" @click="runSegmentAction('delete', selectedIds, true)">删除</el-button><span class="tool-divider"></span><el-button type="text" :disabled="mutationsBusy" @click="clearSelection">取消</el-button><i v-if="segmentActionBusy" class="el-icon-loading"></i></div>
          <footer v-if="total" class="pagination"><el-pagination small background layout="prev, pager, next, sizes" :disabled="mutationsBusy" :current-page="page" :page-size="pageSize" :page-sizes="[10, 25, 50]" :total="total" @current-change="changePage" @size-change="changePageSize" /></footer>
        </section>

        <aside class="info-panel" v-loading="loadingDocument">
          <el-alert v-if="documentError" :title="documentError" type="error" :closable="false" /><el-button v-if="documentError" type="text" @click="loadDocument">重新加载文档</el-button>
          <knowledge-document-metadata v-if="documentInfo.id" :knowledge-base-id="$route.params.id" :document-id="$route.params.documentId" :document-info="documentInfo" @changed="metadataChanged" @updated="metadataUpdated" />
          <h2>文档信息</h2>
          <dl><dt>原始文件名称</dt><dd>{{ documentName }}</dd><dt>原始文件大小</dt><dd>{{ formatSize(documentInfo.data_source_info && documentInfo.data_source_info.upload_file_size || documentInfo.file_size) }}</dd><dt>上传日期</dt><dd>{{ formatTime(documentInfo.created_at) }}</dd><dt>最后更新日期</dt><dd>{{ formatTime(documentInfo.updated_at) }}</dd><dt>来源</dt><dd>{{ documentInfo.data_source_type === 'upload_file' || !documentInfo.data_source_type ? '文件上传' : documentInfo.data_source_type }}</dd></dl>
          <h2>技术参数</h2>
          <dl><dt>分段规则</dt><dd>{{ segmentMode }}</dd><dt>段落长度</dt><dd>{{ documentInfo.segment_length || documentInfo.max_tokens || '—' }}</dd><dt>平均段落长度</dt><dd>{{ averageLength }}</dd><dt>段落数量</dt><dd>{{ total }}</dd><dt>召回次数</dt><dd>{{ documentInfo.hit_count || 0 }}</dd><dt>嵌入时间</dt><dd>{{ documentInfo.indexing_latency || '—' }}</dd><dt>嵌入花费</dt><dd>{{ documentInfo.tokens || 0 }} tokens</dd></dl>
        </aside>
      </div>
    </main>

    <aside v-if="editorOpen" class="segment-editor" :class="{ 'is-fullscreen': editorFullscreen }" v-loading="loadingEditor" role="dialog" aria-label="编辑分段">
      <div class="editor-header"><div><h2>编辑分段</h2><small>分段-{{ activeSegmentPosition }} <span>·</span> {{ editor.content.length }} 字符</small></div><div><button type="button" aria-label="切换全屏编辑" @click="editorFullscreen = !editorFullscreen"><i :class="editorFullscreen ? 'el-icon-copy-document' : 'el-icon-full-screen'"></i></button><button type="button" aria-label="关闭编辑面板" :disabled="savingEditor" @click="closeEditor"><i class="el-icon-close"></i></button></div></div>
      <div class="editor-body">
        <el-input v-model="editor.content" :disabled="savingEditor" type="textarea" resize="none" class="content-editor" placeholder="请输入分段内容" />
        <template v-if="documentInfo.doc_form === 'qa_model'"><label class="editor-label">答案</label><el-input v-model="editor.answer" type="textarea" :rows="4" :disabled="savingEditor" placeholder="请输入答案" /></template>
        <knowledge-segment-image-upload :key="editorRevision" v-model="editorImages" :knowledge-base-id="$route.params.id" :document-id="$route.params.documentId" :disabled="savingEditor || loadingEditor || !editorImagesKnown" @input="editorImagesDirty = true" @blocked-change="editorImagesBlocked = $event" />
        <p v-if="!editorImagesKnown" class="attachment-warning">未获取完整附件信息，暂不能修改图片；保存文字会保留已有图片。请重新打开分段重试。</p>
        <label class="editor-label" for="segment-summary">摘要</label>
        <el-input id="segment-summary" v-model.trim="editor.summary" :disabled="savingEditor" type="textarea" :rows="2" resize="none" placeholder="写一个简短的摘要，以便更好地检索..." />
        <label class="editor-label" for="segment-keyword">关键词</label>
        <div class="editor-keywords"><el-tag v-for="(word, index) in editor.keywords" :key="word + index" :closable="!savingEditor" size="small" @close="removeKeyword(index)">{{ word }}</el-tag><el-input id="segment-keyword" v-model.trim="keywordInput" :disabled="savingEditor" size="small" placeholder="+ 添加关键词" @keyup.enter.native="addKeyword" @blur="addKeyword" /></div>
      </div>
      <div class="editor-footer"><el-button size="small" :disabled="savingEditor" @click="closeEditor">取消</el-button><el-button type="primary" size="small" :loading="savingEditor" :disabled="loadingEditor || editorImagesBlocked || segmentActionBusy" @click="saveSegment">保存 <span class="save-shortcut">Ctrl S</span></el-button></div>
    </aside>
    <knowledge-document-add-segments :mode.sync="addMode" :knowledge-base-id="$route.params.id" :document-id="$route.params.documentId" :qa="documentInfo.doc_form === 'qa_model'" @changed="segmentsChanged" />
  </div>
</template>

<script>
import { getKnowledgeDocument, getKnowledgeDocumentSegments, getKnowledgeDocumentSegment, updateKnowledgeDocumentSegment, deleteKnowledgeDocumentSegment, batchKnowledgeDocumentSegments } from '@/api/llmops/knowledge'
import { parseSegmentBatchResult } from '@/utils/knowledgeSegmentActions'
import KnowledgeDocumentMetadata from './document-metadata.vue'
import KnowledgeDocumentAddSegments from './document-add-segments.vue'
import { knowledgeRetrievalError } from '@/utils/knowledgeRetrieval'
import { readSegmentImages, segmentImageIds } from '@/utils/knowledgeSegmentImages'
import KnowledgeSegmentImageUpload from './segment-image-upload.vue'
import KnowledgeSegmentImageGallery from './segment-image-gallery.vue'

export default {
  name: 'KnowledgeDocumentDetail',
  components: { KnowledgeDocumentMetadata, KnowledgeDocumentAddSegments, KnowledgeSegmentImageUpload, KnowledgeSegmentImageGallery },
  data() {
    return { documentInfo: {}, segments: [], total: 0, page: 1, pageSize: 10, keyword: '', statusFilter: '', loadingDocument: false, loadingSegments: false, selectedIds: [], selectAll: false, editorOpen: false, loadingEditor: false, savingEditor: false, activeSegmentId: '', activeSegmentPosition: 1, editor: { content: '', answer: '', summary: '', keywords: [] }, keywordInput: '', addMode: '', documentError: '', segmentsError: '', documentRevision: 0, segmentsRevision: 0, editorRevision: 0, editorImages: [], editorImagesKnown: false, editorImagesDirty: false, editorImagesBlocked: false, editorFullscreen: false, segmentActionBusy: false, segmentActionRevision: 0, segmentOperationError: '' }
  },
  computed: {
    mutationsBusy() { return this.savingEditor || this.segmentActionBusy },
    documentContext() { return `${this.$route.params.id}/${this.$route.params.documentId}` },
    knowledgeName() { return this.$route.query.name || '知识库' },
    documentName() { return this.documentInfo.name || this.documentInfo.file_name || this.$route.query.documentName || '文档' },
    documentCount() { return this.$route.query.documentCount || '—' },
    segmentMode() { return this.documentInfo.doc_form === 'qa_model' ? '问答' : '通用' },
    documentStatus() {
      const status = this.documentInfo.display_status || this.documentInfo.indexing_status || this.documentInfo.status
      return { available: '可用', completed: '可用', indexing: '处理中', waiting: '等待中', error: '失败' }[status] || '—'
    },
    documentStatusType() { return this.documentStatus === '可用' ? 'success' : this.documentStatus === '失败' ? 'danger' : 'info' },
    averageLength() {
      const count = Number(this.documentInfo.word_count || this.documentInfo.character_count || 0)
      return this.total && count ? `${Math.round(count / this.total)} characters` : '—'
    }
  },
  watch: {
    documentContext() {
      this.addMode = ''; this.editorOpen = false; this.activeSegmentId = ''; this.editorRevision++; this.savingEditor = false
      this.editorImages = []; this.editorImagesKnown = false; this.editorImagesDirty = false; this.editorImagesBlocked = false
      this.segmentActionRevision++; this.segmentActionBusy = false; this.segmentOperationError = ''; this.editorFullscreen = false; this.clearSelection()
      this.documentInfo = {}; this.segments = []; this.page = 1; this.keyword = ''; this.statusFilter = ''
      this.loadDocument(); this.loadSegments()
    }
  },
  created() { this.loadDocument(); this.loadSegments() },
  mounted() { window.addEventListener('keydown', this.onEditorKeydown) },
  beforeDestroy() { this.documentRevision++; this.segmentsRevision++; this.editorRevision++; this.segmentActionRevision++; window.removeEventListener('keydown', this.onEditorKeydown) },
  methods: {
    unwrap(result) { return result && result.data !== undefined ? result.data : result },
    goToKnowledgeList() { this.$router.push({ name: 'knowledge' }) },
    goToDocuments() { this.$router.push({ name: 'knowledge-detail', params: { id: this.$route.params.id }, query: this.$route.query }) },
    goToRetrieve() { this.$router.push({ name: 'knowledge-retrieve', params: { id: this.$route.params.id }, query: this.$route.query }) },
    goToSettings() { this.$router.push({ name: 'knowledge-settings', params: { id: this.$route.params.id }, query: this.$route.query }) },
    openAdd(mode) { if (this.mutationsBusy || this.loadingDocument || !this.documentInfo.id) return; this.closeEditor(); this.addMode = mode },
    sameDocument(event) { return event.knowledgeBaseId === this.$route.params.id && event.documentId === this.$route.params.documentId },
    segmentsChanged(event) {
      if (!this.sameDocument(event)) return
      this.page = 1; this.keyword = ''; this.statusFilter = ''
      this.loadSegments(); this.loadDocument()
    },
    metadataChanged(event) { if (this.sameDocument(event)) this.loadDocument() },
    metadataUpdated(event) { if (this.sameDocument(event)) { this.documentRevision++; this.loadingDocument = false; this.documentError = ''; this.documentInfo = event.document } },
    async loadDocument() {
      const revision = ++this.documentRevision
      this.loadingDocument = true; this.documentError = ''
      try {
        const detail = this.unwrap(await getKnowledgeDocument(this.$route.params.id, this.$route.params.documentId)) || {}
        if (revision !== this.documentRevision) return false
        if (!detail.id) throw new Error('文档详情响应异常')
        this.documentInfo = detail; return true
      } catch (e) { if (revision === this.documentRevision) this.documentError = knowledgeRetrievalError(e); return false }
      finally { if (revision === this.documentRevision) this.loadingDocument = false }
    },
    async loadSegments() {
      const revision = ++this.segmentsRevision
      this.loadingSegments = true; this.segmentsError = ''
      try {
        const result = this.unwrap(await getKnowledgeDocumentSegments(this.$route.params.id, this.$route.params.documentId, { page: this.page, limit: this.pageSize, keyword: this.keyword || undefined, status: this.statusFilter || undefined }))
        if (revision !== this.segmentsRevision) return false
        const list = Array.isArray(result) ? result : (result && (result.data || result.items || result.results)) || []
        this.segments = Array.isArray(list) ? list : []
        this.total = Array.isArray(result) ? result.length : Number(result && (result.total || result.count || result.total_count)) || this.segments.length
        this.selectedIds = []; this.selectAll = false
        return true
      } catch (e) { if (revision === this.segmentsRevision) { this.segments = []; this.total = 0; this.segmentsError = knowledgeRetrievalError(e) }; return false }
      finally { if (revision === this.segmentsRevision) this.loadingSegments = false }
    },
    resetAndLoad() { if (this.mutationsBusy) return; this.page = 1; this.segmentOperationError = ''; this.loadSegments() },
    changePage(page) { if (this.mutationsBusy) return; this.page = page; this.segmentOperationError = ''; this.loadSegments() },
    changePageSize(size) { if (this.mutationsBusy) return; this.pageSize = size; this.resetAndLoad() },
    toggleSegment(id) {
      if (!id || this.mutationsBusy) return
      this.selectedIds = this.selectedIds.includes(id) ? this.selectedIds.filter(value => value !== id) : this.selectedIds.concat(id)
      this.selectAll = this.segments.length > 0 && this.segments.every(segment => this.selectedIds.includes(segment.id))
    },
    toggleAll(checked) { if (this.mutationsBusy) return; this.selectedIds = checked ? this.segments.map(segment => segment.id).filter(Boolean) : [] },
    clearSelection() { this.selectedIds = []; this.selectAll = false },
    deleteSegment(segment) { if (segment.id) return this.runSegmentAction('delete', [segment.id], false) },
    setSegmentEnabled(segment, enabled) { if (segment.id) return this.runSegmentAction(enabled ? 'enable' : 'disable', [segment.id], false) },
    async runSegmentAction(action, segmentIds, batch = false) {
      if (this.mutationsBusy || this.loadingSegments || !['enable', 'disable', 'delete'].includes(action)) return
      const ids = [...new Set(segmentIds)].filter(Boolean)
      if (!ids.length || ids.length > 100) return this.$message.warning('请选择 1 至 100 个分段')
      const revision = ++this.segmentActionRevision, kb = this.$route.params.id, doc = this.$route.params.documentId
      const current = () => revision === this.segmentActionRevision && kb === this.$route.params.id && doc === this.$route.params.documentId
      this.segmentActionBusy = true; this.segmentOperationError = ''
      const label = { enable: '启用', disable: '禁用', delete: '删除' }[action]
      try {
        if (action === 'delete') {
          try { await this.$confirm(batch ? `确定删除选中的 ${ids.length} 个分段？删除后无法恢复。` : '删除这个分段？删除后无法恢复。', '删除分段', { type: 'warning', confirmButtonText: '我确定', cancelButtonText: '取消', confirmButtonClass: 'segment-delete-confirm' }) }
          catch (error) { return }
          if (!current()) return
        }
        let succeededIds = ids, failedItems = []
        if (batch) {
          const result = parseSegmentBatchResult(await batchKnowledgeDocumentSegments(kb, doc, action, ids), action, ids)
          if (!current()) return
          succeededIds = result.succeededIds; failedItems = result.failedItems
        } else if (action === 'delete') await deleteKnowledgeDocumentSegment(kb, doc, ids[0])
        else await updateKnowledgeDocumentSegment(kb, doc, ids[0], { enabled: action === 'enable' })
        if (!current()) return
        if (action === 'delete' && succeededIds.includes(this.activeSegmentId)) this.closeEditor()
        if (failedItems.length) this.segmentOperationError = `${label}成功 ${succeededIds.length} 个，失败 ${failedItems.length} 个。` + failedItems.slice(0, 5).map(item => `${item.id}：${item.message}`).join('；')
        else this.$message.success(batch ? `已${label} ${succeededIds.length} 个分段` : `分段已${label}`)
        await this.refreshAfterSegmentAction(current, failedItems.map(item => item.id))
        if (current()) this.loadDocument()
      } catch (error) {
        if (!current()) return
        this.segmentOperationError = knowledgeRetrievalError(error)
        await this.refreshAfterSegmentAction(current, batch ? ids : [])
      } finally { if (current()) this.segmentActionBusy = false }
    },
    async refreshAfterSegmentAction(current, failedIds) {
      const loaded = await this.loadSegments()
      if (!current() || !loaded) return
      const lastPage = Math.max(1, Math.ceil(this.total / this.pageSize))
      if (this.page > lastPage) { this.page = lastPage; await this.loadSegments() }
      if (!current()) return
      this.selectedIds = failedIds.filter(id => this.segments.some(segment => segment.id === id))
      this.selectAll = this.segments.length > 0 && this.segments.every(segment => this.selectedIds.includes(segment.id))
    },
    fillEditor(segment, fallback = {}) {
      const content = typeof segment.content === 'string' && segment.content.length
        ? segment.content
        : (typeof segment.text === 'string' && segment.text.length ? segment.text : (fallback.content || fallback.text || ''))
      const keywords = Array.isArray(segment.keywords) && segment.keywords.length
        ? segment.keywords
        : (Array.isArray(fallback.keywords) ? fallback.keywords : [])
      this.editor = {
        content,
        answer: typeof segment.answer === 'string' ? segment.answer : (fallback.answer || ''),
        summary: segment.summary || fallback.summary || '',
        keywords: keywords.slice()
      }
      const images = readSegmentImages(segment, fallback)
      this.editorImages = images.images; this.editorImagesKnown = images.known; this.editorImagesDirty = false; this.editorImagesBlocked = false
    },
    segmentDetailData(result) {
      let data = this.unwrap(result)
      for (let depth = 0; depth < 3 && data && typeof data === 'object'; depth += 1) {
        if (data.segment && typeof data.segment === 'object') data = data.segment
        else if (data.data && typeof data.data === 'object') data = data.data
        else break
      }
      return data
    },
    async openSegment(segment, index) {
      if (this.mutationsBusy || this.addMode) return
      if (!segment.id) return this.$message.warning('该分段缺少 ID，无法编辑')
      const revision = ++this.editorRevision
      this.activeSegmentId = segment.id
      this.activeSegmentPosition = segment.position || segment.index || (this.page - 1) * this.pageSize + index + 1
      this.keywordInput = ''
      this.editorFullscreen = false
      this.fillEditor(segment)
      this.editorOpen = true
      this.loadingEditor = true
      try {
        const detail = this.segmentDetailData(await getKnowledgeDocumentSegment(this.$route.params.id, this.$route.params.documentId, segment.id))
        if (revision === this.editorRevision && this.activeSegmentId === segment.id && detail && typeof detail === 'object') this.fillEditor(detail, segment)
      } catch (e) { /* 保留列表中的内容，允许直接编辑 */ }
      finally { if (revision === this.editorRevision && this.activeSegmentId === segment.id) this.loadingEditor = false }
    },
    closeEditor() {
      if (this.savingEditor) return
      this.editorRevision++
      this.editorOpen = false
      this.activeSegmentId = ''
      this.keywordInput = ''
      this.editorImages = []; this.editorImagesKnown = false; this.editorImagesDirty = false; this.editorImagesBlocked = false
    },
    addKeyword() {
      if (this.savingEditor) return
      const word = this.keywordInput.trim()
      if (word && !this.editor.keywords.includes(word)) this.editor.keywords.push(word)
      this.keywordInput = ''
    },
    removeKeyword(index) { if (!this.savingEditor) this.editor.keywords.splice(index, 1) },
    onEditorKeydown(event) {
      if (!this.editorOpen) return
      if (event.key === 'Escape') this.closeEditor()
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') { event.preventDefault(); this.saveSegment() }
    },
    async saveSegment() {
      if (!this.editorOpen || this.mutationsBusy || this.loadingEditor) return
      if (this.editorImagesBlocked) return this.$message.warning('请等待图片上传完成，失败的图片请重试或移除')
      this.addKeyword()
      if (!this.editor.content.trim()) return this.$message.warning('分段内容不能为空')
      if (this.documentInfo.doc_form === 'qa_model' && !this.editor.answer.trim()) return this.$message.warning('答案不能为空')
      let imageData = {}
      if (this.editorImagesDirty) {
        if (!this.editorImagesKnown) return this.$message.warning('附件信息不完整，请重新打开分段后修改图片')
        try { imageData = { attachment_ids: segmentImageIds(this.editorImages) } } catch (error) { return this.$message.warning(error.message) }
      }
      this.savingEditor = true
      const revision = this.editorRevision
      try {
        await updateKnowledgeDocumentSegment(this.$route.params.id, this.$route.params.documentId, this.activeSegmentId, {
          content: this.editor.content,
          summary: this.editor.summary,
          keywords: this.editor.keywords,
          ...(this.documentInfo.doc_form === 'qa_model' ? { answer: this.editor.answer } : {}),
          ...imageData
        })
        if (revision !== this.editorRevision) return
        this.$message.success('分段已保存')
        this.savingEditor = false
        this.closeEditor()
        await this.loadSegments()
      } catch (e) { if (revision === this.editorRevision) this.$message.error(knowledgeRetrievalError(e)) }
      finally { if (revision === this.editorRevision) this.savingEditor = false }
    },
    segmentLength(segment) { return segment.word_count || segment.character_count || (segment.content || segment.text || '').length },
    segmentEnabled(segment) { return segment.enabled !== false && segment.status !== 'disabled' },
    formatTime(value) {
      if (!value) return '—'
      const date = new Date(typeof value === 'number' && value < 100000000000 ? value * 1000 : value)
      if (Number.isNaN(date.getTime())) return '—'
      const pad = number => String(number).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    formatSize(value) {
      const size = Number(value)
      if (!size) return '—'
      return size >= 1048576 ? `${(size / 1048576).toFixed(1)} MB` : `${(size / 1024).toFixed(1)} KB`
    }
  }
}
</script>

<style scoped>
.document-detail-page { position: relative; display: flex; min-height: calc(100vh - 100px); background: #fff; color: #344054; font-size: 14px; }
.document-sidebar { display: flex; flex: 0 0 250px; flex-direction: column; padding: 20px 16px; border-right: 1px solid #edf0f4; box-sizing: border-box; }
.sidebar-breadcrumb button, .base-nav button, .back-button { background: none; border: 0; cursor: pointer; font: inherit; }.sidebar-breadcrumb button { color: #344054; font-weight: 600; }.sidebar-breadcrumb i { margin-right: 7px; }
.base-summary { display: flex; align-items: center; gap: 10px; margin: 36px 0 26px; }.base-icon { display: grid; place-items: center; width: 42px; height: 42px; color: #1649f5; background: #edf2ff; border-radius: 10px; font-size: 21px; }.base-summary strong, .base-summary small { display: block; }.base-summary strong { color: #17233d; }.base-summary small { margin-top: 4px; color: #98a2b3; font-size: 11px; }
.base-nav { display: grid; gap: 5px; }.base-nav button { padding: 11px 13px; border-radius: 8px; color: #526079; text-align: left; }.base-nav button i { margin-right: 11px; font-size: 16px; }.base-nav .is-active { color: #1649f5; background: #f0f4ff; }.base-nav button:disabled { cursor: default; opacity: .6; }
.sidebar-bottom { display: grid; gap: 18px; margin-top: auto; color: #667085; font-size: 12px; }.sidebar-bottom span:last-child { padding: 10px; border: 1px solid #e9edf3; border-radius: 8px; }.sidebar-bottom i { float: right; color: #28bd80; }
.document-main { flex: 1; min-width: 0; }.document-header { display: flex; align-items: center; gap: 12px; height: 72px; padding: 0 22px; border-bottom: 1px solid #edf0f4; box-sizing: border-box; }.back-button { color: #344054; font-size: 18px; }.file-icon { color: #409eff; font-size: 22px; }.document-heading strong, .document-heading small { display: block; }.document-heading strong { color: #17233d; }.document-heading small { margin-top: 3px; color: #667085; font-size: 11px; }.header-spacer { flex: 1; }
.document-body { display: flex; min-height: 650px; }.segments-panel { flex: 1; min-width: 0; padding: 16px 22px; }.segments-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 14px; min-height: 40px; }.segments-toolbar strong { white-space: nowrap; }.segments-toolbar /deep/ .el-checkbox { margin-right: 10px; }.segment-filters { display: flex; gap: 8px; }.status-filter { width: 112px; }.segment-search { width: 210px; }
.segment-list { margin-top: 16px; max-height: calc(100vh - 270px); min-height: 380px; overflow-y: auto; }.segment-item { display: flex; align-items: flex-start; gap: 16px; padding: 18px 10px; border-bottom: 1px solid #edf0f4; border-radius: 9px; cursor: pointer; }.segment-item:hover, .segment-item.is-selected { background: #f6f8fb; }.segment-item.is-selected .segment-meta > span:first-child { color: #1649f5; }.segment-item > .el-checkbox { margin-top: 3px; }.segment-content { flex: 1; min-width: 0; }.segment-meta { display: flex; justify-content: space-between; gap: 10px; color: #667085; font-size: 12px; }.segment-meta i { color: #98a2b3; }.meta-dot { margin: 0 5px; }.segment-status { white-space: nowrap; }.segment-status i { display: inline-block; width: 8px; height: 8px; margin-left: 5px; background: #31c68a; border-radius: 50%; }.segment-status.is-off i { background: #c4cad4; }
.segment-text { margin: 11px 0; color: #344054; line-height: 1.7; white-space: pre-wrap; overflow-wrap: anywhere; }.keywords { display: flex; gap: 8px; flex-wrap: wrap; color: #8993a5; font-size: 12px; }.segment-empty { padding: 120px 0; color: #98a2b3; text-align: center; }.pagination { display: flex; justify-content: center; padding-top: 16px; }
.info-panel { flex: 0 0 320px; padding: 16px 18px 30px; border-left: 1px solid #edf0f4; box-sizing: border-box; }.metadata-card { padding: 16px; background: #f7f8fa; border-radius: 12px; }.metadata-card strong { color: #17233d; }.metadata-card p { margin: 8px 0; color: #667085; font-size: 12px; line-height: 1.5; }.metadata-card .el-button { background: #1649f5; border-color: #1649f5; }.info-panel h2 { margin: 22px 0 14px; color: #17233d; font-size: 14px; }.info-panel dl { display: grid; grid-template-columns: 1fr 1.15fr; row-gap: 14px; margin: 0; font-size: 12px; }.info-panel dt { color: #667085; }.info-panel dd { margin: 0; overflow-wrap: anywhere; }
.segment-editor { position: absolute; z-index: 5; top: 72px; right: 0; bottom: 0; display: flex; flex-direction: column; width: min(630px, calc(100% - 250px)); min-height: 640px; background: #fff; border: 1px solid #e5e9f0; border-radius: 14px 0 0 14px; box-shadow: -8px 0 28px rgba(22, 34, 61, .09); box-sizing: border-box; }
.editor-header { display: flex; align-items: flex-start; justify-content: space-between; padding: 18px 20px 12px; }.editor-header h2 { margin: 0 0 5px; color: #17233d; font-size: 17px; }.editor-header small { color: #667085; font-size: 12px; }.editor-header small span { margin: 0 6px; }.editor-header button { padding: 2px 4px; color: #667085; background: none; border: 0; cursor: pointer; font-size: 18px; }
.editor-body { display: flex; flex: 1; flex-direction: column; min-height: 0; padding: 0 20px 20px; overflow-y: auto; }.content-editor { flex: 1; min-height: 230px; }.segment-editor /deep/ .content-editor textarea { height: 100%; min-height: 230px; padding: 10px 0; color: #344054; border: 0; box-shadow: none; line-height: 1.7; }.segment-editor /deep/ .content-editor textarea:focus { border: 0; }
.attachment-warning { color: #b54708; font-size: 12px; line-height: 1.6; }
.editor-label { margin: 0 0 7px; color: #667085; font-size: 12px; }.segment-editor /deep/ .editor-body > .el-textarea textarea { color: #344054; border-color: #e5e9f0; }.editor-keywords { display: flex; align-items: center; flex-wrap: wrap; gap: 5px; }.editor-keywords /deep/ .el-tag { color: #526079; background: #fff; border-color: #dce2eb; }.editor-keywords .el-input { width: 130px; }.editor-keywords /deep/ .el-input__inner { border: 1px dashed #dce2eb; }
.editor-footer { display: flex; justify-content: flex-end; gap: 8px; padding: 14px 20px; border-top: 1px solid #edf0f4; }.editor-footer .el-button { margin: 0; border-radius: 7px; }.editor-footer .el-button--primary { background: #1649f5; border-color: #1649f5; }.save-shortcut { margin-left: 5px; font-size: 11px; opacity: .8; }
.segment-item { position: relative; cursor: default; }.segment-item.is-checked { background: #f0f4ff; }.segment-meta { min-height: 28px; align-items: center; }.segment-meta > span:first-child { padding-right: 140px; }.segment-status { position: absolute; right: 12px; top: 24px; }.segment-tools { position: absolute; top: 16px; right: 10px; display: flex; align-items: center; gap: 12px; padding: 2px 6px; background: #f6f8fb; border-radius: 6px; opacity: 0; pointer-events: none; transition: opacity .15s; }.segment-item:hover .segment-tools, .segment-item:focus-within .segment-tools { opacity: 1; pointer-events: auto; }.segment-item:hover .segment-status, .segment-item:focus-within .segment-status { visibility: hidden; }.segment-tools .el-button { margin: 0; padding: 6px 0; color: #667085; font-size: 16px; }.segment-tools .el-button:not(.is-disabled):hover { color: #1649f5; }.segment-tools .el-button--text:nth-child(2):not(.is-disabled):hover { color: #f04438; }.segment-item.is-checked .segment-tools { background: #f0f4ff; }.tool-divider { width: 1px; height: 18px; background: #dce2eb; }
.operation-alert { margin-top: 12px; white-space: pre-wrap; overflow-wrap: anywhere; }.segment-batch-bar { position: sticky; z-index: 4; bottom: 18px; display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 12px; width: fit-content; max-width: 100%; margin: 16px auto 0; padding: 8px 18px; border: 1px solid #e5e9f0; border-radius: 12px; background: #fff; box-shadow: 0 6px 24px rgba(22, 34, 61, .14); box-sizing: border-box; font-size: 12px; }.segment-batch-bar strong { padding: 3px 7px; color: #1649f5; background: #edf2ff; border-radius: 5px; }.segment-batch-bar .el-button { margin: 0; color: #526079; }.segment-batch-bar .batch-delete:not(.is-disabled) { color: #f04438; }
.segment-editor.is-fullscreen { position: fixed; z-index: 2000; inset: 0; width: 100%; min-height: 0; border-radius: 0; }.editor-header button:disabled { opacity: .4; cursor: not-allowed; }
@media (hover: none) { .segment-tools { opacity: 1; pointer-events: auto; }.segment-status { display: none; } }
@media (max-width: 1150px) { .document-sidebar { flex-basis: 190px; }.info-panel { flex-basis: 265px; } }
@media (max-width: 900px) { .document-detail-page { flex-wrap: wrap; }.document-sidebar { flex: 1 0 100%; padding: 12px 16px; border-right: 0; border-bottom: 1px solid #edf0f4; }.base-summary, .base-nav, .sidebar-bottom { display: none; }.document-body { flex-wrap: wrap; }.segments-panel { flex: 1 0 100%; }.info-panel { flex: 1 0 100%; border-left: 0; border-top: 1px solid #edf0f4; }.segment-list { max-height: none; } }
@media (max-width: 600px) { .segments-toolbar { align-items: flex-start; flex-direction: column; }.segment-filters { width: 100%; }.segment-search { flex: 1; min-width: 0; }.status-filter { width: 95px; }.segment-meta { flex-wrap: wrap; } }
@media (max-width: 900px) { .segment-editor { position: fixed; top: 0; bottom: 0; width: min(630px, 100%); min-height: 0; border-radius: 0; } }
</style>

<style>
.el-button.segment-delete-confirm { color: #fff; background: #f04438; border-color: #f04438; }.el-button.segment-delete-confirm:hover, .el-button.segment-delete-confirm:focus { color: #fff; background: #d92d20; border-color: #d92d20; }
</style>
