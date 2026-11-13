<template>
  <div class="knowledge-create-page" :class="{ 'is-adding-documents': isAddingDocuments && currentStep === 1 }">
    <header class="create-header">
      <button class="back-button" type="button" :aria-label="isAddingDocuments ? '返回文档列表' : '返回知识库'" @click="goBack">
        <i class="el-icon-back"></i>
      </button>
      <h1>知识库</h1>
      <div class="steps" aria-label="创建进度">
        <div class="step" :class="{ 'is-active': currentStep === 1 }">
          <span class="step-number">{{ currentStep === 1 ? 'STEP 1' : '1' }}</span>
          <strong>选择数据源</strong>
        </div>
        <span class="step-line"></span>
        <div class="step" :class="{ 'is-active': currentStep === 2 }">
          <span class="step-number">{{ currentStep === 2 ? 'STEP 2' : '2' }}</span>
          <strong>文本分段与清洗</strong>
        </div>
        <span class="step-line"></span>
        <div class="step">
          <span class="step-number">3</span>
          <strong>处理并完成</strong>
        </div>
      </div>
      <div class="header-spacer"></div>
    </header>

    <div v-if="targetBaseError" class="target-base-error"><el-alert :title="targetBaseError" type="error" :closable="false" show-icon /><el-button size="small" :loading="targetBaseLoading" @click="refreshTargetKnowledgeBase">重新加载知识库配置</el-button></div>

    <main v-if="currentStep === 1" class="create-content">
      <section class="create-panel">
        <h2>选择数据源</h2>
        <div class="source-list">
          <button
            v-for="source in sources"
            :key="source.value"
            type="button"
            :class="['source-card', { 'is-active': selectedSource === source.value }]"
            @click="selectSource(source.value)"
          >
            <span :class="['source-icon', source.iconClass]"><i :class="source.icon"></i></span>
            <span>{{ source.label }}</span>
          </button>
        </div>

        <template v-if="selectedSource === 'file'">
          <h2 class="upload-title">上传文本文件</h2>
          <div
            class="upload-area"
            :class="{ 'is-dragging': isDragging }"
            @dragenter.prevent="isDragging = true"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="onDrop"
          >
            <input ref="fileInput" class="file-input" type="file" multiple :accept="accept" @change="onFileChange">
            <div class="upload-main">
              <i class="el-icon-upload2"></i>
              <span>拖拽文件或文件夹至此，或者</span>
              <button type="button" @click="chooseFiles">选择文件</button>
            </div>
            <p>已支持 VTT、PDF、DOCX、HTM、MDX、HTML、MD、XLSX、MARKDOWN、XLS、TXT、CSV、PROPERTIES，每批最多 5 个文件，每个文件不超过 15 MB。</p>
          </div>

          <div v-if="files.length" class="file-list">
            <div v-for="(file, index) in files" :key="file.name + file.size" class="file-item">
              <span><i class="el-icon-document"></i>{{ file.name }}</span>
              <div class="file-status">
                <span v-if="fileState(file).status === 'uploading'" class="is-uploading"><i class="el-icon-loading"></i>上传中</span>
                <span v-else-if="fileState(file).status === 'processing'" class="is-uploading"><i class="el-icon-loading"></i>{{ processingText(file) }}</span>
                <span v-else-if="fileState(file).status === 'success'" class="is-success"><i class="el-icon-success"></i>已上传</span>
                <button v-else-if="fileState(file).status === 'error'" type="button" class="retry-button" :title="fileState(file).message || '上传失败，请重试'" @click="uploadFiles([file])"><i class="el-icon-warning-outline"></i>重试</button>
                <button v-else type="button" aria-label="移除文件" @click="removeFile(index)"><i class="el-icon-close"></i></button>
              </div>
            </div>
          </div>
        </template>

        <div v-else class="source-placeholder">
          <i :class="selectedSource === 'notion' ? 'el-icon-tickets' : 'el-icon-position'"></i>
          <p>{{ selectedSource === 'notion' ? '连接 Notion 后同步页面内容' : '输入网站地址后同步公开网页内容' }}</p>
          <el-button type="primary" size="small" disabled>即将支持</el-button>
        </div>

        <div class="actions">
          <el-button type="primary" :disabled="!canContinue" @click="continueCreate">下一步 <i class="el-icon-right"></i></el-button>
        </div>

        <div v-if="!isAddingDocuments" class="empty-link">
          <button type="button" @click="createEmpty"><i class="el-icon-folder"></i> 创建一个空知识库</button>
        </div>
      </section>
    </main>

    <main v-else class="settings-layout">
      <section class="settings-panel">
        <h2>分段设置</h2>
        <div class="setting-card is-selected">
          <div class="setting-card-title"><span class="setting-icon"><i class="el-icon-document"></i></span><div><strong>通用</strong><small>将文档拆分为独立分段，适用于大多数文本的检索和召回</small></div><i class="el-icon-success selected-mark"></i></div>
          <fieldset class="configuration-fields" :disabled="savingSettings">
          <div class="setting-grid">
            <label>分段标识符<el-input v-model="settings.separator" size="small" placeholder="\\n" :disabled="savingSettings" /></label>
            <label>分段最大长度 <span class="unit">tokens</span><el-input-number v-model="settings.maxLength" size="small" :min="100" :max="4000" :step="100" controls-position="right" :disabled="savingSettings" /></label>
            <label>分段重叠长度 <span class="unit">tokens</span><el-input-number v-model="settings.overlap" size="small" :min="0" :max="Math.max(0, settings.maxLength - 1)" :step="10" controls-position="right" :disabled="savingSettings" /></label>
          </div>
          <div class="setting-section-title">文本预处理规则</div>
          <div class="preprocessing-options"><el-checkbox v-model="settings.removeExtraSpaces" :disabled="savingSettings">替换掉连续的空格、换行符和制表符</el-checkbox><el-checkbox v-model="settings.removeUrlsEmails" :disabled="savingSettings">删除所有 URL 和电子邮件地址</el-checkbox></div>
          <div class="qa-option"><el-checkbox disabled>使用 Q&A 分段</el-checkbox><span>接口暂未提供此模式的上传配置</span></div>
          </fieldset>
          <div class="setting-actions"><el-button size="mini" icon="el-icon-view" :loading="previewLoading" :disabled="savingSettings" @click="previewSegments">预览块</el-button><el-button size="mini" type="text" :disabled="savingSettings" @click="resetSegmentation">重置</el-button><span>本地预估，最终以服务端分段为准</span></div>
        </div>
        <div class="setting-card muted-card" aria-disabled="true"><span class="setting-icon"><i class="el-icon-connection"></i></span><div><strong>父子分段</strong><small>使用子分段检索并返回父分段上下文；接口暂未提供上传配置</small></div><i class="el-icon-lock"></i></div>

        <h2 class="setting-heading">索引方式</h2>
        <div class="choice-grid" role="group" aria-label="索引方式"><button type="button" class="choice-card" :class="{ 'is-selected': isHighQuality }" :aria-pressed="isHighQuality" :disabled="indexingLocked || savingSettings || targetBaseLoading" @click="setIndexingTechnique('high_quality')"><i class="el-icon-magic-stick"></i><span><strong>高质量 <em>推荐</em></strong><small>调用 Embedding 模型构建向量索引，提供精准的语义检索</small></span><span class="radio-mark"></span></button><button type="button" class="choice-card" :class="{ 'is-selected': !isHighQuality }" :aria-pressed="!isHighQuality" :disabled="indexingLocked || savingSettings || targetBaseLoading" @click="setIndexingTechnique('economy')"><i class="el-icon-coin"></i><span><strong>经济</strong><small>使用关键词构建索引，无需调用 Embedding 模型</small></span><span class="radio-mark"></span></button></div>
        <p class="setting-hint">{{ indexingLocked ? '新增文档沿用知识库的索引方式；如需从经济升级为高质量，请前往知识库设置。已启用高质量后不可降级。' : '新建知识库将使用所选索引方式，高质量保存生效后不可切回经济。' }}</p>

        <div v-if="isHighQuality" class="embedding-section">
          <h2>Embedding 模型</h2><div class="model-field"><el-select v-model="settings.embeddingKey" size="small" filterable :loading="embeddingLoading" :disabled="configurationLocked || embeddingLoading || !!embeddingError" placeholder="请选择 Embedding 模型" aria-label="Embedding 模型"><el-option v-for="model in embeddingOptions" :key="model.key" :label="model.label + ' · ' + model.providerLabel" :value="model.key" :disabled="model.unavailable" /></el-select><el-button type="text" size="small" :loading="embeddingLoading" :disabled="savingSettings" @click="loadModels('text-embedding')">刷新</el-button></div>
          <p v-if="embeddingError" class="field-error">{{ embeddingError }}</p><p v-else-if="!embeddingLoading && !embeddingModels.length" class="setting-hint">暂无可用模型，请先在 Dify 工作区配置 Embedding 模型。</p><p v-else class="setting-hint">使用当前工作区的模型，模型名称和提供商将一并提交。</p>
        </div>

        <h2 class="setting-heading">检索设置</h2>
        <p v-if="indexingLocked" class="setting-hint"><i class="el-icon-lock"></i> 沿用知识库配置，如需修改请前往设置页。</p>
        <div v-for="method in visibleRetrievalMethods" :key="method.value" class="retrieval-card" :class="{ 'is-selected': settings.searchMethod === method.value }">
          <button type="button" class="retrieval-heading" :aria-expanded="settings.searchMethod === method.value" :disabled="configurationLocked" @click="settings.searchMethod = method.value"><span class="setting-icon"><i :class="method.icon"></i></span><span><strong>{{ method.label }}</strong><small>{{ method.description }}</small></span><span class="radio-mark"></span></button>
          <div v-if="settings.searchMethod === method.value" class="retrieval-body">
            <template v-if="isHighQuality">
              <div class="control-heading"><strong>Rerank 模型</strong><el-switch v-model="settings.rerankingEnable" :disabled="configurationLocked" /></div><p class="setting-hint">对召回结果进行二次排序，提升相关性。</p>
              <div v-if="settings.rerankingEnable" class="model-field"><el-select v-model="settings.rerankKey" filterable size="small" :loading="rerankLoading" :disabled="configurationLocked || rerankLoading || !!rerankError" placeholder="请选择 Rerank 模型" aria-label="Rerank 模型"><el-option v-for="model in rerankOptions" :key="model.key" :value="model.key" :label="model.label + ' · ' + model.providerLabel" :disabled="model.unavailable" /></el-select><el-button type="text" size="small" :loading="rerankLoading" :disabled="savingSettings" @click="loadModels('rerank')">刷新</el-button></div>
              <p v-if="settings.rerankingEnable && rerankError" class="field-error">{{ rerankError }}</p><p v-else-if="settings.rerankingEnable && !rerankLoading && !rerankModels.length" class="setting-hint">当前工作区没有可用的 Rerank 模型，请配置后刷新，或关闭此选项。</p>
              <div v-if="method.value === 'hybrid_search' && !settings.rerankingEnable" class="weight-control"><div class="control-heading"><strong>权重设置</strong><span>语义 {{ settings.vectorWeight.toFixed(2) }} / 关键词 {{ (1 - settings.vectorWeight).toFixed(2) }}</span></div><el-slider v-model="settings.vectorWeight" :min="0" :max="1" :step="0.01" :disabled="configurationLocked" /></div>
            </template>
            <div class="retrieval-controls"><div><div class="control-heading"><label :for="'top-k-' + method.value">Top K</label><el-input-number :id="'top-k-' + method.value" v-model="settings.topK" size="mini" :min="1" :max="100" controls-position="right" :disabled="configurationLocked" /></div><el-slider v-model="settings.topK" :min="1" :max="100" :disabled="configurationLocked" /></div><div v-if="isHighQuality"><div class="control-heading"><el-checkbox v-model="settings.scoreThresholdEnabled" :disabled="configurationLocked">Score 阈值</el-checkbox><el-input-number v-model="settings.scoreThreshold" size="mini" :min="0" :max="1" :step="0.01" :precision="2" controls-position="right" :disabled="configurationLocked || !settings.scoreThresholdEnabled" aria-label="Score 阈值" /></div><el-slider v-model="settings.scoreThreshold" :min="0" :max="1" :step="0.01" :disabled="configurationLocked || !settings.scoreThresholdEnabled" /></div></div>
          </div>
        </div>
        <div class="settings-footer"><el-button size="small" icon="el-icon-back" :disabled="savingSettings" @click="currentStep = 1">上一步</el-button><el-button type="primary" size="small" :loading="savingSettings" :disabled="targetBaseLoading || !!targetBaseError" @click="saveAndProcess">保存并处理</el-button></div>
      </section>

      <aside class="preview-panel"><div class="preview-header"><strong><i class="el-icon-view"></i> 预览</strong><el-select v-model="previewFileIndex" size="mini" class="preview-file" :disabled="previewLoading"><el-option v-for="(file, index) in files" :key="fileKey(file)" :label="file.name" :value="index" /></el-select><span>{{ previewReady ? previewChunks.length : 0 }} 预估块</span></div><div v-if="previewLoading" class="preview-empty" role="status"><i class="el-icon-loading"></i><p>正在读取文件并预估分段…</p></div><div v-else-if="!previewReady" class="preview-empty"><span class="preview-illustration"><i class="el-icon-document"></i><i class="el-icon-view"></i></span><strong>分段预览</strong><p>点击左侧“预览块”，查看分段与清洗效果</p></div><div v-else-if="previewError" class="preview-empty" role="status"><i class="el-icon-warning-outline"></i><p>{{ previewError }}</p></div><div v-else class="preview-list"><article v-for="(chunk, index) in previewChunks" :key="index"><div class="chunk-heading"><strong><i class="el-icon-menu"></i> 分段 {{ index + 1 }}</strong><span>{{ chunk.length }} 字符</span></div><p>{{ chunk }}</p></article></div><p class="preview-footnote">本地字符预估 · 最多展示前 512 KB 的 30 段<br>服务端按 tokens 分段，实际结果可能不同</p></aside>
    </main>

    <el-dialog
      v-if="!isAddingDocuments"
      title="创建空知识库"
      :visible.sync="emptyDialogVisible"
      width="586px"
      custom-class="empty-knowledge-dialog"
      :close-on-click-modal="false"
      @opened="focusEmptyName"
      @closed="resetEmptyForm"
    >
      <p class="dialog-description">空知识库中还没有文档，你可以在今后任何时候上传文档至该知识库。</p>
      <el-form ref="emptyForm" :model="emptyForm" :rules="emptyRules" label-position="top" @submit.native.prevent>
        <el-form-item label="知识库名称" prop="name">
          <el-input
            ref="emptyNameInput"
            v-model.trim="emptyForm.name"
            maxlength="50"
            placeholder="请输入知识库名称"
            @keyup.enter.native="submitEmptyKnowledge"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="emptyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="emptyCreating" @click="submitEmptyKnowledge">创建</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { createKnowledgeBase, getKnowledgeBase, getKnowledgeModels, uploadKnowledgeDocument, getDocumentIndexingStatus } from '@/api/llmops/knowledge'
import { normalizeKnowledgeSettings, normalizeKnowledgeModels, buildKnowledgeCreationPayload, verifyKnowledgeSettings } from '@/utils/knowledgeSettings'

const ALLOWED_EXTENSIONS = ['vtt', 'pdf', 'docx', 'htm', 'mdx', 'html', 'md', 'xlsx', 'markdown', 'xls', 'txt', 'csv', 'properties']
const MAX_FILE_SIZE = 15 * 1024 * 1024
const STORAGE_KEY = 'llmops_knowledge_draft_v1'
const segmentationDefaults = () => ({ separator: '\\n', maxLength: 1024, overlap: 50, removeExtraSpaces: true, removeUrlsEmails: false })
const creationDefaults = () => ({ ...normalizeKnowledgeSettings({}), ...segmentationDefaults(), searchMethod: 'keyword_search' })

export default {
  name: 'KnowledgeCreate',
  data() {
    return {
      selectedSource: 'file',
      currentStep: 1,
      files: [],
      uploadStates: {},
      draftKnowledgeBaseId: '',
      creatingKnowledgeBase: false,
      isDragging: false,
      emptyDialogVisible: false,
      emptyCreating: false,
      savingSettings: false,
      createdKnowledgeName: '',
      targetKnowledgeBase: null, targetBaseLoading: false, targetBaseError: '', targetBaseRequestVersion: 0,
      previewFileIndex: 0,
      previewReady: false,
      previewError: '',
      previewChunks: [],
      previewLoading: false, previewRequestVersion: 0,
      embeddingModels: [], rerankModels: [], embeddingLoading: false, rerankLoading: false, embeddingError: '', rerankError: '',
      settings: creationDefaults(),
      retrievalMethods: [
        { value: 'semantic_search', label: '向量检索', icon: 'el-icon-aim', description: '将查询转为向量，通过语义相似度检索相关分段。' },
        { value: 'full_text_search', label: '全文检索', icon: 'el-icon-document', description: '根据查询关键词匹配文档，返回相关文本分段。' },
        { value: 'hybrid_search', label: '混合检索', icon: 'el-icon-connection', description: '结合向量与全文检索，通过权重或 Rerank 融合结果。' }
      ],
      emptyForm: { name: '' },
      emptyRules: { name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }] },
      accept: ALLOWED_EXTENSIONS.map(extension => `.${extension}`).join(','),
      sources: [
        { value: 'file', label: '导入已有文本', icon: 'el-icon-document', iconClass: 'file-icon' },
        { value: 'notion', label: '同步自 Notion 内容', icon: 'el-icon-tickets', iconClass: 'notion-icon' },
        { value: 'web', label: '同步自 Web 站点', icon: 'el-icon-position', iconClass: 'web-icon' }
      ]
    }
  },
  computed: {
    existingKnowledgeBaseId() { return this.$route.params.id || this.$route.query.knowledgeBaseId || '' },
    isAddingDocuments() { return Boolean(this.existingKnowledgeBaseId) },
    knowledgeBaseId() { return this.existingKnowledgeBaseId || this.draftKnowledgeBaseId || '' },
    indexingLocked() { return Boolean(this.knowledgeBaseId) },
    configurationLocked() { return this.indexingLocked || this.savingSettings || this.targetBaseLoading },
    isHighQuality() { return this.settings.indexingTechnique === 'high_quality' },
    embeddingOptions() { return this.withCurrentModel(this.embeddingModels, this.settings.embeddingKey) },
    rerankOptions() { return this.withCurrentModel(this.rerankModels, this.settings.rerankKey) },
    visibleRetrievalMethods() { return this.isHighQuality ? this.retrievalMethods : [{ value: 'keyword_search', label: '关键词检索', icon: 'el-icon-search', description: '使用关键词索引匹配文档，无需调用模型。' }] },
    canContinue() {
      return this.selectedSource === 'file' && this.files.length > 0 && !this.targetBaseLoading && !this.targetBaseError
    }
  },
  created() { if (this.knowledgeBaseId) this.refreshTargetKnowledgeBase(); this.loadModels('text-embedding'); this.loadModels('rerank') },
  beforeDestroy() { this.invalidatePreview(); ++this.targetBaseRequestVersion },
  watch: {
    existingKnowledgeBaseId() {
      ++this.targetBaseRequestVersion
      this.targetKnowledgeBase = null
      this.targetBaseError = ''
      this.targetBaseLoading = false
      this.draftKnowledgeBaseId = ''
      this.createdKnowledgeName = ''
      this.currentStep = 1
      this.files = []
      this.uploadStates = {}
      this.resetSettings()
      if (this.knowledgeBaseId) this.refreshTargetKnowledgeBase()
    },
    settings: { handler() { this.invalidatePreview() }, deep: true },
    previewFileIndex() { this.invalidatePreview() }
  },
  methods: {
    syncTargetSettings() {
      const base = this.targetKnowledgeBase
      if (!base) return
      const form = normalizeKnowledgeSettings(base)
      ;['indexingTechnique', 'embeddingKey', 'searchMethod', 'topK', 'rerankingEnable', 'rerankKey', 'vectorWeight', 'scoreThresholdEnabled', 'scoreThreshold'].forEach(key => { this.settings[key] = form[key] })
      if (base.indexing_technique === 'economy') this.settings.searchMethod = 'keyword_search'
    },
    withCurrentModel(models, key) {
      if (!key || models.some(model => model.key === key)) return models
      try {
        const [provider, model] = JSON.parse(key)
        return [...models, { key, provider, model, label: model, providerLabel: provider + '（当前配置）', unavailable: true }]
      } catch (e) { return models }
    },
    async loadModels(type) {
      const prefix = type === 'rerank' ? 'rerank' : 'embedding'
      if (this[`${prefix}Loading`]) return
      this[`${prefix}Loading`] = true
      this[`${prefix}Error`] = ''
      try { this[`${prefix}Models`] = normalizeKnowledgeModels(await getKnowledgeModels(type), type) }
      catch (e) { this[`${prefix}Models`] = []; this[`${prefix}Error`] = e.message || '模型列表加载失败，请刷新重试' }
      finally { this[`${prefix}Loading`] = false }
    },
    async loadTargetKnowledgeBase() {
      const id = this.knowledgeBaseId
      if (!id) return null
      const version = ++this.targetBaseRequestVersion
      this.targetBaseLoading = true
      this.targetBaseError = ''
      try {
        const base = this.unwrapApiData(await getKnowledgeBase(id))
        if (id !== this.knowledgeBaseId || version !== this.targetBaseRequestVersion) return null
        if (!base || base.id !== id || !['economy', 'high_quality'].includes(base.indexing_technique)) throw new Error('知识库接口未返回有效的索引方式，暂不能确认上传配置')
        this.targetKnowledgeBase = base
        this.createdKnowledgeName = base.name || this.createdKnowledgeName
        this.syncTargetSettings()
        return base
      } catch (e) {
        if (id === this.knowledgeBaseId && version === this.targetBaseRequestVersion) this.targetBaseError = e.message || '知识库配置加载失败'
        throw e
      } finally { if (version === this.targetBaseRequestVersion) this.targetBaseLoading = false }
    },
    refreshTargetKnowledgeBase() { return this.loadTargetKnowledgeBase().catch(() => null) },
    setIndexingTechnique(value) {
      if (this.indexingLocked || this.savingSettings || this.targetBaseLoading) return
      this.settings.indexingTechnique = value
      this.settings.searchMethod = value === 'economy' ? 'keyword_search' : 'semantic_search'
    },
    retrievalOptions() {
      if (this.indexingLocked && this.targetKnowledgeBase) return JSON.parse(JSON.stringify(this.targetKnowledgeBase.retrieval_model_dict || this.targetKnowledgeBase.retrieval_model || {}))
      return this.creationOptions().retrieval_model
    },
    creationOptions() {
      if (this.isHighQuality && (this.embeddingLoading || this.embeddingError)) throw new Error('请等待 Embedding 模型列表加载完成，失败时请刷新重试')
      if (this.isHighQuality && this.settings.rerankingEnable && (this.rerankLoading || this.rerankError)) throw new Error('请等待 Rerank 模型列表加载完成，失败时请刷新重试')
      return buildKnowledgeCreationPayload(this.settings, this.embeddingModels, this.rerankModels)
    },
    goBack() {
      if (this.currentStep === 2) {
        this.currentStep = 1
        return
      }
      const query = { ...this.$route.query }
      delete query.knowledgeBaseId
      if (this.isAddingDocuments) {
        this.$router.push({ name: 'knowledge-detail', params: { id: this.existingKnowledgeBaseId }, query })
      } else {
        this.$router.push({ name: 'knowledge', query })
      }
    },
    selectSource(source) { this.selectedSource = source },
    chooseFiles() { this.$refs.fileInput.click() },
    onFileChange(event) {
      this.addFiles(Array.from(event.target.files || []))
      event.target.value = ''
    },
    onDrop(event) {
      this.isDragging = false
      this.addFiles(Array.from(event.dataTransfer.files || []))
    },
    addFiles(newFiles) {
      const remainingCount = 5 - this.files.length
      if (remainingCount <= 0) {
        this.$message.warning('每批最多上传 5 个文件')
        return
      }
      const validFiles = []
      newFiles.slice(0, remainingCount).forEach(file => {
        const extension = file.name.split('.').pop().toLowerCase()
        if (!ALLOWED_EXTENSIONS.includes(extension)) {
          this.$message.warning(`${file.name} 的文件格式不支持`)
        } else if (file.size > MAX_FILE_SIZE) {
          this.$message.warning(`${file.name} 超过 15 MB`)
        } else if (!this.files.some(item => item.name === file.name && item.size === file.size)) {
          validFiles.push(file)
        }
      })
      if (newFiles.length > remainingCount) this.$message.warning('每批最多上传 5 个文件')
      this.files = this.files.concat(validFiles)
      if (validFiles.length) this.invalidatePreview()
    },
    removeFile(index) { this.files.splice(index, 1); this.previewFileIndex = 0; this.invalidatePreview() },
    fileKey(file) { return `${file.name}_${file.size}_${file.lastModified}` },
    fileState(file) { return this.uploadStates[this.fileKey(file)] || { status: 'ready', progress: 0 } },
    processingText(file) {
      const state = this.fileState(file)
      return state.total ? `处理中 ${state.completed}/${state.total}` : '处理中'
    },
    unwrapApiData(result) {
      return result && result.data !== undefined ? result.data : result
    },
    knowledgeNameFromFile(file) {
      const name = file.name.replace(/\.[^.]+$/, '').trim() || '未命名知识库'
      return name.slice(0, 128)
    },
    saveKnowledgeCache(knowledgeBase) {
      let knowledge = []
      try { knowledge = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]') } catch (e) { knowledge = [] }
      if (!knowledge.some(item => String(item.id) === String(knowledgeBase.id))) {
        knowledge.unshift({
          id: knowledgeBase.id,
          type: 'quick',
          name: knowledgeBase.name,
          description: knowledgeBase.description || '即用型知识库',
          tag: ''
        })
        localStorage.setItem(STORAGE_KEY, JSON.stringify(knowledge))
      }
    },
    async ensureKnowledgeBase(file) {
      const selectedTechnique = this.settings.indexingTechnique
      if (this.knowledgeBaseId) {
        const base = await this.loadTargetKnowledgeBase()
        if (!base) throw new Error('目标知识库已变更，请重新确认后上传')
        if (base.indexing_technique !== selectedTechnique) throw new Error('知识库索引方式已变化，已同步最新配置，请确认后重新上传；高质量知识库不支持按经济模式上传')
        return base.id
      }
      if (this.creatingKnowledgeBase) return ''
      this.creatingKnowledgeBase = true
      try {
        const options = this.creationOptions()
        const result = await createKnowledgeBase({
          name: this.knowledgeNameFromFile(file),
          ...options
        })
        const knowledgeBase = this.unwrapApiData(result)
        if (!knowledgeBase || !knowledgeBase.id) throw new Error('创建知识库成功，但响应中缺少知识库 ID')
        this.draftKnowledgeBaseId = knowledgeBase.id
        this.createdKnowledgeName = knowledgeBase.name || this.knowledgeNameFromFile(file)
        this.saveKnowledgeCache(knowledgeBase)
        const confirmed = await this.loadTargetKnowledgeBase()
        if (!confirmed || confirmed.indexing_technique !== selectedTechnique) throw new Error('知识库实际索引方式与所选模式不一致，已停止上传，请检查后端创建接口是否保留 indexing_technique 参数')
        const mismatches = verifyKnowledgeSettings(confirmed, options)
        if (mismatches.length) throw new Error(`知识库已创建，但${mismatches.join('、')}与提交配置不一致，已停止上传，请先在设置页确认配置`)
        return knowledgeBase.id
      } finally {
        this.creatingKnowledgeBase = false
      }
    },
    async uploadFiles(files) {
      if (!files.length) return false
      let knowledgeBaseId
      try {
        this.validateSegmentation()
        knowledgeBaseId = await this.ensureKnowledgeBase(files[0])
      } catch (e) {
        const message = e.message || '创建知识库失败'
        files.forEach(file => this.$set(this.uploadStates, this.fileKey(file), { status: 'error', progress: 0, message }))
        this.$message.error(message)
        return false
      }
      if (!knowledgeBaseId) return false
      files.forEach(file => this.$set(this.uploadStates, this.fileKey(file), { status: 'uploading', progress: 0 }))
      const results = await Promise.all(files.map(async file => {
        try {
          const uploadResult = await uploadKnowledgeDocument(knowledgeBaseId, file, this.documentOptions())
          const uploadData = this.unwrapApiData(uploadResult) || {}
          const batch = uploadData.batch || uploadData.backend_batch_id || uploadData.batch_id
          if (batch) {
            this.$set(this.uploadStates, this.fileKey(file), { status: 'processing', progress: 100, batch })
            await this.waitForIndexing(knowledgeBaseId, batch, file)
          } else {
            this.$set(this.uploadStates, this.fileKey(file), { status: 'success', progress: 100 })
          }
          return true
        } catch (e) {
          const responseData = e.response && e.response.data
          const message = (responseData && (responseData.detail || responseData.message)) || e.message || '上传失败'
          this.$set(this.uploadStates, this.fileKey(file), { status: 'error', progress: 0, message })
          this.$message.error(`${file.name}：${message}`)
          return false
        }
      }))
      const successCount = results.filter(Boolean).length
      if (successCount) this.$message.success(`${successCount} 个文件上传成功`)
      return successCount === files.length
    },
    async waitForIndexing(knowledgeBaseId, batch, file) {
      for (let attempt = 0; attempt < 60; attempt += 1) {
        const result = await getDocumentIndexingStatus(knowledgeBaseId, batch)
        const payload = this.unwrapApiData(result) || {}
        const items = Array.isArray(payload.data) ? payload.data : []
        const status = items[0]
        if (status) {
          this.$set(this.uploadStates, this.fileKey(file), {
            status: status.indexing_status === 'completed' ? 'success' : 'processing',
            progress: 100,
            batch,
            completed: status.completed_segments || 0,
            total: status.total_segments || 0
          })
          if (status.indexing_status === 'completed') return
          if (status.indexing_status === 'error') throw new Error(status.error || '文档索引失败')
        }
        await new Promise(resolve => setTimeout(resolve, 2000))
      }
      throw new Error('文档处理超时，请稍后在文档列表查看状态')
    },
    continueCreate() {
      if (this.canContinue) this.currentStep = 2
    },
    resetSettings() {
      this.settings = creationDefaults()
      this.syncTargetSettings()
      this.invalidatePreview()
    },
    resetSegmentation() {
      Object.assign(this.settings, segmentationDefaults())
      this.invalidatePreview()
    },
    validateSegmentation() {
      if (typeof this.settings.separator !== 'string' || !this.settings.separator.length) throw new Error('请输入分段标识符')
      if (!Number.isInteger(this.settings.maxLength) || this.settings.maxLength < 100 || this.settings.maxLength > 4000) throw new Error('分段最大长度应为 100–4000 的整数')
      if (!Number.isInteger(this.settings.overlap) || this.settings.overlap < 0 || this.settings.overlap >= this.settings.maxLength) throw new Error('分段重叠长度应为非负整数，且小于最大长度')
    },
    documentOptions() {
      const base = this.targetKnowledgeBase || {}
      const embedding = this.isHighQuality && base.embedding_model && base.embedding_model_provider ? { embedding_model: base.embedding_model, embedding_model_provider: base.embedding_model_provider } : {}
      return {
        indexing_technique: this.settings.indexingTechnique,
        ...embedding,
        process_rule: {
          mode: 'custom',
          rules: {
            pre_processing_rules: [
              { id: 'remove_extra_spaces', enabled: this.settings.removeExtraSpaces },
              { id: 'remove_urls_emails', enabled: this.settings.removeUrlsEmails }
            ],
            segmentation: { separator: this.settings.separator.replace(/\\n/g, '\n'), max_tokens: this.settings.maxLength, chunk_overlap: this.settings.overlap }
          }
        }
      }
    },
    invalidatePreview() {
      ++this.previewRequestVersion
      this.previewReady = false
      this.previewLoading = false
      this.previewChunks = []
      this.previewError = ''
    },
    async previewSegments() {
      const file = this.files[this.previewFileIndex]
      if (!file) return
      this.invalidatePreview()
      const version = this.previewRequestVersion
      const snapshot = JSON.stringify(this.settings)
      const settings = { ...this.settings }
      this.previewReady = true
      try { this.validateSegmentation() } catch (e) { this.previewError = e.message; return }
      if (!/\.(txt|md|mdx|markdown|csv|vtt|html|htm|properties)$/i.test(file.name)) {
        this.previewError = '该文件格式需要由服务端解析，保存并处理后可在文档中查看分段。'
        return
      }
      this.previewLoading = true
      try {
        const text = await new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => resolve(String(reader.result || ''))
          reader.onerror = () => reject(new Error('读取文件失败'))
          reader.readAsText(file.slice(0, 512 * 1024))
        })
        if (version !== this.previewRequestVersion || file !== this.files[this.previewFileIndex] || snapshot !== JSON.stringify(this.settings)) return
        const separator = settings.separator.replace(/\\n/g, '\n')
        let clean = text.replace(/\r\n?/g, '\n')
        if (settings.removeExtraSpaces) clean = clean.replace(/[ \t]+/g, ' ').replace(/\n{3,}/g, '\n\n')
        if (settings.removeUrlsEmails) clean = clean.replace(/(?:https?:\/\/|www\.)[^\s<>]+/gi, '').replace(/[\w.+-]+@[\w.-]+\.[a-z]{2,}/gi, '')
        const chunks = []
        let start = 0
        while (start < clean.length && chunks.length < 30) {
          let end = Math.min(start + settings.maxLength, clean.length)
          if (end < clean.length) {
            const boundary = clean.lastIndexOf(separator, end - separator.length)
            if (boundary >= start && boundary + separator.length - start > settings.overlap) end = boundary + separator.length
          }
          const chunk = clean.slice(start, end)
          if (chunk.trim()) chunks.push(chunk)
          if (end === clean.length) break
          start = Math.max(start + 1, end - settings.overlap)
        }
        this.previewChunks = chunks
        if (!chunks.length) this.previewError = '文件中没有可预览的文本'
      } catch (e) { if (version === this.previewRequestVersion) this.previewError = e.message || '预览失败' }
      finally { if (version === this.previewRequestVersion) this.previewLoading = false }
    },
    async saveAndProcess() {
      if (this.savingSettings || !this.files.length) return
      if (this.targetBaseLoading || this.targetBaseError) return
      try {
        this.validateSegmentation()
        if (!this.indexingLocked) this.creationOptions()
      } catch (e) { this.$message.warning(e.message); return }
      this.savingSettings = true
      try {
        const pendingFiles = this.files.filter(file => this.fileState(file).status !== 'success')
        const success = pendingFiles.length ? await this.uploadFiles(pendingFiles) : true
        if (!success) return
        const query = { ...this.$route.query, name: this.createdKnowledgeName || this.$route.query.name || this.knowledgeNameFromFile(this.files[0]) }
        delete query.knowledgeBaseId
        this.$router.push({ name: 'knowledge-detail', params: { id: this.knowledgeBaseId }, query })
      } finally { this.savingSettings = false }
    },
    createEmpty() { this.emptyDialogVisible = true },
    focusEmptyName() { this.$refs.emptyNameInput && this.$refs.emptyNameInput.focus() },
    resetEmptyForm() {
      this.emptyCreating = false
      this.emptyForm.name = ''
      this.$refs.emptyForm && this.$refs.emptyForm.clearValidate()
    },
    submitEmptyKnowledge() {
      if (this.emptyCreating) return
      this.$refs.emptyForm.validate(async valid => {
        if (!valid) return
        this.emptyCreating = true
        try {
          const result = await createKnowledgeBase({ name: this.emptyForm.name, indexing_technique: 'economy', retrieval_model: { search_method: 'keyword_search', reranking_enable: false, top_k: 3, score_threshold_enabled: false } })
          const createdKnowledge = this.unwrapApiData(result)
          if (!createdKnowledge || !createdKnowledge.id) throw new Error('创建知识库成功，但响应中缺少知识库 ID')
          this.saveKnowledgeCache(createdKnowledge)
          this.$message.success('空知识库已创建')
          this.emptyDialogVisible = false
          this.$router.push({ name: 'knowledge', query: this.$route.query })
        } catch (e) {
          // 请求层会统一展示后端或网络错误，弹窗保持打开以便用户重试。
        } finally {
          this.emptyCreating = false
        }
      })
    }
  }
}
</script>

<style scoped>
.knowledge-create-page { min-height: 100%; background: #fff; color: #17233d; box-sizing: border-box; }
.target-base-error { display: flex; align-items: center; gap: 12px; padding: 16px 24px; }.target-base-error .el-alert { flex: 1; }.target-base-error .el-button { flex: none; }
.choice-card:disabled { cursor: not-allowed; }.choice-card:disabled:not(.is-selected) { opacity: .5; background: #fafbfc; }
.create-header { position: relative; display: flex; align-items: center; height: 72px; padding: 0 24px; border-bottom: 1px solid #edf0f5; box-sizing: border-box; }
.back-button { display: flex; align-items: center; justify-content: center; width: 28px; height: 32px; margin-right: 2px; padding: 0; color: #17233d; background: transparent; border: 0; cursor: pointer; font-size: 20px; }
.create-header h1 { margin: 0; font-size: 18px; font-weight: 600; }
.steps { position: absolute; left: 50%; display: flex; align-items: center; transform: translateX(-50%); white-space: nowrap; }
.step { display: flex; align-items: center; gap: 10px; color: #b2b8c5; font-size: 14px; }
.step strong { font-weight: 400; }
.step-number { display: flex; align-items: center; justify-content: center; min-width: 23px; height: 23px; padding: 0 6px; border: 1px solid #d9dde6; border-radius: 12px; box-sizing: border-box; font-size: 12px; }
.step.is-active { color: #1649f5; }
.step.is-active strong { font-weight: 600; }
.step.is-active .step-number { min-width: 54px; color: #fff; background: #1649f5; border-color: #1649f5; }
.step-line { width: 22px; margin: 0 12px; border-top: 2px solid #e5e8ee; }
.header-spacer { margin-left: auto; }
.create-content { padding: 64px 24px; }
.is-adding-documents .create-header { height: 56px; padding: 0 16px; }
.is-adding-documents .create-header h1 { font-size: 14px; }
.is-adding-documents .create-content { padding: 64px 18px; }
.create-panel { width: 720px; max-width: 100%; }
.create-panel h2 { margin: 0 0 16px; font-size: 16px; font-weight: 600; }
.source-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.source-card { display: flex; align-items: center; gap: 12px; height: 64px; padding: 0 14px; color: #27324a; background: #fff; border: 1px solid #e4e8ef; border-radius: 12px; cursor: pointer; font: inherit; font-size: 15px; text-align: left; transition: border-color .2s, box-shadow .2s; }
.source-card:hover { border-color: #9db0ff; }
.source-card.is-active { border: 2px solid #1f4cff; padding: 0 13px; box-shadow: 0 2px 7px rgba(31,76,255,.06); }
.source-icon { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; flex: none; border: 1px solid #e7eaf0; border-radius: 8px; font-size: 18px; }
.file-icon, .web-icon { color: #3157ef; }.notion-icon { color: #111827; }
.upload-title { margin-top: 42px !important; }
.upload-area { position: relative; padding: 14px 18px 12px; border: 1px dashed #d8dee9; border-radius: 12px; background: #fafbfc; transition: border-color .2s, background .2s; }
.upload-area.is-dragging { border-color: #3157ef; background: #f3f6ff; }
.file-input { display: none; }
.upload-main { display: flex; align-items: center; justify-content: center; gap: 9px; min-height: 23px; color: #344054; font-size: 14px; }
.upload-main > i { font-size: 21px; }.upload-main button, .empty-link button { padding: 0; color: #174cff; background: none; border: 0; cursor: pointer; font: inherit; }
.upload-area p { max-width: 660px; margin: 8px auto 0; color: #69758c; font-size: 12px; line-height: 1.55; }
.file-list { display: grid; gap: 8px; margin-top: 12px; }
.file-item { display: flex; align-items: center; justify-content: space-between; height: 38px; padding: 0 12px; background: #f7f8fa; border-radius: 7px; color: #4b5568; font-size: 13px; }
.file-item span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.file-item span i { margin-right: 8px; color: #3157ef; }
.file-item button { padding: 5px; color: #8b95a7; background: none; border: 0; cursor: pointer; }
.file-status { display: flex; align-items: center; flex: none; margin-left: 12px; }.file-status > span { font-size: 12px; }.file-status .is-uploading { color: #3157ef; }.file-status .is-success { color: #22a06b; }.file-status .retry-button { color: #e5484d; font-size: 12px; }.file-status i { margin-right: 4px; }
.source-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 150px; margin-top: 42px; color: #788397; background: #fafbfc; border: 1px dashed #d8dee9; border-radius: 12px; }
.source-placeholder > i { font-size: 25px; }.source-placeholder p { margin: 10px 0; font-size: 13px; }
.actions { display: flex; justify-content: flex-end; margin-top: 22px; }
.actions .el-button { min-width: 124px; border-radius: 9px; }.actions .el-button i { margin-left: 5px; }
.empty-link { margin-top: 36px; padding-top: 36px; border-top: 1px solid #e6e9ef; }
.empty-link button { font-size: 14px; }.empty-link i { margin-right: 6px; }
.dialog-description { margin: -2px 0 28px; color: #7b8495; font-size: 14px; line-height: 1.6; }
.knowledge-create-page /deep/ .empty-knowledge-dialog { border-radius: 16px; overflow: hidden; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__header { padding: 30px 36px 4px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__title { color: #172033; font-size: 20px; font-weight: 600; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__headerbtn { top: 30px; right: 32px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__headerbtn .el-dialog__close { color: #7c8799; font-size: 19px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__body { padding: 12px 36px 8px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-form-item { margin-bottom: 0; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-form-item__label { padding-bottom: 8px; color: #172033; font-size: 14px; font-weight: 600; line-height: 20px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-input__inner { height: 42px; padding: 0 14px; color: #344054; background: #f4f6f8; border-color: #f4f6f8; border-radius: 8px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-input__inner:focus { background: #fff; border-color: #3157ef; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__footer { padding: 28px 36px 26px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-button { width: 108px; height: 38px; margin: 0; border-radius: 8px; }
.knowledge-create-page /deep/ .empty-knowledge-dialog .el-button--primary { background: #1649f5; border-color: #1649f5; }
.settings-layout { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); align-items: start; min-height: calc(100vh - 72px); background: #fff; }
.settings-panel { min-width: 0; padding: 24px 32px 40px; }.settings-panel > h2 { margin: 0 0 12px; color: #17233d; font-size: 16px; }.settings-panel .setting-heading { margin-top: 26px; padding-top: 22px; border-top: 1px solid #e9edf3; }
.setting-card { padding: 14px; border: 1px solid #e4e8ef; border-radius: 10px; background: #fff; }.setting-card.is-selected, .choice-card.is-selected { border: 2px solid #1649f5; }.setting-card-title, .muted-card { display: flex; align-items: center; gap: 10px; }.setting-icon { display: grid; place-items: center; flex: none; width: 32px; height: 32px; color: #1649f5; background: #eef2ff; border-radius: 8px; }.setting-card strong, .choice-card strong { display: block; font-size: 13px; }.setting-card small, .choice-card small { display: block; margin-top: 4px; color: #8b95a7; font-size: 11px; line-height: 1.4; }
.setting-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; margin: 20px 0 18px; }.setting-grid label { min-width: 0; font-size: 12px; font-weight: 600; }.setting-grid /deep/ .el-input, .setting-grid /deep/ .el-input-number { display: block; width: 100%; margin-top: 7px; }.setting-grid /deep/ .el-input__inner { background: #f5f6f8; border-color: #f5f6f8; text-align: left; }
.setting-section-title { margin-bottom: 8px; font-size: 12px; font-weight: 600; }.setting-card > .el-checkbox { display: block; margin: 8px 0; }.setting-card /deep/ .el-checkbox__label { color: #667085; font-size: 12px; }.setting-actions { display: flex; align-items: center; gap: 8px; margin-top: 20px; padding-top: 13px; border-top: 1px solid #eef0f4; }.setting-actions .el-button + .el-button { margin-left: 0; }.muted-card { margin-top: 8px; background: #fafbfc; }
.choice-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }.choice-card { display: flex; align-items: flex-start; gap: 11px; min-height: 72px; padding: 12px; color: #344054; background: #fff; border: 1px solid #e4e8ef; border-radius: 10px; cursor: pointer; font: inherit; text-align: left; }.choice-card > i { color: #1649f5; font-size: 20px; }.choice-card span { min-width: 0; }.retrieval-select { width: 160px; }.setting-hint { margin: 8px 0 16px; color: #8b95a7; font-size: 11px; }.top-k-label { display: flex; align-items: center; gap: 12px; color: #667085; font-size: 12px; }.top-k-label /deep/ .el-input-number { width: 90px; flex: none; }.top-k-label /deep/ .el-slider { flex: 1; }.settings-footer { display: flex; justify-content: space-between; margin-top: 30px; }.settings-footer .el-button { border-radius: 8px; }.settings-footer .el-button--primary { background: #1649f5; border-color: #1649f5; }
.preview-panel { display: flex; flex-direction: column; min-width: 0; min-height: 650px; margin: 12px 14px 12px 0; background: #fff; border: 1px solid #e9edf3; border-radius: 10px; box-shadow: 0 2px 10px rgba(16,24,40,.04); }.preview-header { display: flex; align-items: center; gap: 10px; min-height: 52px; padding: 0 16px; border-bottom: 1px solid #edf0f4; }.preview-header strong { color: #1649f5; font-size: 12px; }.preview-file { flex: 1; min-width: 0; }.preview-header span { color: #98a2b3; white-space: nowrap; font-size: 11px; }.preview-empty { display: flex; align-items: center; justify-content: center; flex: 1; flex-direction: column; min-height: 330px; color: #a8b1c0; }.preview-empty i { font-size: 38px; }.preview-empty p { max-width: 260px; text-align: center; font-size: 12px; line-height: 1.5; }.preview-list { max-height: calc(100vh - 150px); overflow-y: auto; padding: 14px; }.preview-list article { padding: 12px; margin-bottom: 10px; color: #344054; background: #fafbfc; border: 1px solid #edf0f4; border-radius: 8px; }.preview-list strong { font-size: 12px; }.preview-list p { margin: 8px 0 0; font-size: 12px; line-height: 1.6; white-space: pre-wrap; overflow-wrap: anywhere; }
.configuration-fields { min-width: 0; margin: 0; padding: 0; border: 0; }
.setting-card.is-selected, .choice-card.is-selected { border-width: 1px; box-shadow: 0 0 0 1px #1649f5; }
.setting-card-title > div { flex: 1; }.selected-mark { margin-left: auto; color: #1649f5; font-size: 18px; }
.setting-grid .unit { color: #98a2b3; font-size: 10px; font-weight: 400; }
.preprocessing-options { display: grid; gap: 10px; }.preprocessing-options .el-checkbox { margin: 0; white-space: normal; }.qa-option { display: flex; flex-wrap: wrap; gap: 6px 12px; margin-top: 18px; padding-top: 14px; border-top: 1px solid #edf0f5; }.qa-option > span { color: #98a2b3; font-size: 11px; }
.setting-actions { flex-wrap: wrap; }.setting-actions > span { margin-left: auto; color: #98a2b3; font-size: 11px; }.muted-card > div { flex: 1; }.muted-card > i { color: #98a2b3; }
.choice-card { min-height: 100px; position: relative; padding-right: 26px; }.choice-card > span:not(.radio-mark) { flex: 1; }.choice-card em { display: inline-block; margin-left: 3px; padding: 1px 4px; border-radius: 4px; color: #1649f5; background: #eef2ff; font-size: 10px; font-style: normal; font-weight: 400; }.radio-mark { display: inline-block; flex: none; width: 12px; height: 12px; border: 1px solid #d0d5dd; border-radius: 50%; box-sizing: border-box; }.choice-card > .radio-mark { position: absolute; top: 15px; right: 10px; }.is-selected > .radio-mark, .is-selected > .retrieval-heading > .radio-mark { border: 4px solid #1649f5; }
.setting-hint { line-height: 1.7; }.embedding-section { margin-top: 22px; }.embedding-section > h2 { margin: 0 0 12px; font-size: 14px; }.model-field { display: flex; align-items: center; gap: 12px; }.model-field .el-select { flex: 1; min-width: 0; }.field-error { color: #f56c6c; font-size: 12px; line-height: 1.6; }
.retrieval-card { overflow: hidden; margin-top: 10px; border: 1px solid #e4e8ef; border-radius: 10px; background: #fff; }.retrieval-card.is-selected { border-color: #1649f5; box-shadow: 0 0 0 1px #1649f5; }.retrieval-heading { display: flex; align-items: center; gap: 10px; width: 100%; padding: 14px; border: 0; background: transparent; color: #344054; font: inherit; text-align: left; cursor: pointer; }.retrieval-heading:disabled { cursor: default; }.retrieval-heading > span:nth-child(2) { flex: 1; }.retrieval-heading strong { font-size: 13px; font-weight: 600; }.retrieval-heading small { display: block; margin-top: 5px; color: #8b95a7; font-size: 11px; line-height: 1.6; }.retrieval-heading .setting-icon { background: #f2f4f7; color: #667085; }.is-selected > .retrieval-heading .setting-icon { color: #1649f5; background: #eef2ff; }.retrieval-body { margin: 0 14px; padding: 16px 0 4px; border-top: 1px solid #edf0f5; }.control-heading { display: flex; align-items: center; justify-content: space-between; gap: 8px; min-height: 28px; color: #475467; font-size: 12px; }.control-heading strong { font-size: 12px; font-weight: 500; }.control-heading > span { color: #98a2b3; font-size: 11px; }.retrieval-body /deep/ .el-checkbox__label { font-size: 12px; }.retrieval-controls { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px; margin-top: 18px; }.control-heading /deep/ .el-input-number { width: 82px; flex: none; }.weight-control { margin-top: 18px; }.settings-footer { padding-top: 22px; border-top: 1px solid #edf0f5; }
.preview-panel { position: sticky; top: 16px; height: calc(100vh - 120px); min-height: 560px; margin: 24px 24px 24px 0; background: #fcfcfd; }.preview-header { flex: none; min-height: 60px; background: #fff; border-radius: 10px 10px 0 0; }.preview-header strong { white-space: nowrap; }.preview-header strong i { margin-right: 4px; }.preview-file /deep/ .el-input__inner { border-color: transparent; background: #f7f8fa; }.preview-list { min-height: 0; flex: 1; max-height: none; }.preview-empty strong { margin-top: 24px; color: #667085; font-size: 14px; font-weight: 500; }.preview-empty p { margin-top: 10px; padding: 0 18px; }.preview-illustration { position: relative; display: grid; place-items: center; width: 90px; height: 90px; background: #f0f3f9; border-radius: 24px; }.preview-illustration > .el-icon-document { color: #c2cad9; font-size: 56px; }.preview-illustration > .el-icon-view { position: absolute; right: -8px; bottom: -6px; padding: 8px; background: #fff; border-radius: 12px; box-shadow: 0 4px 14px rgba(16,24,40,.06); color: #8797b3; font-size: 25px; }.preview-footnote { flex: none; margin: 0; padding: 12px 16px; border-top: 1px solid #edf0f5; color: #98a2b3; text-align: center; font-size: 11px; line-height: 1.7; }.preview-list article { background: #fff; }.chunk-heading { display: flex; align-items: center; justify-content: space-between; gap: 8px; }.chunk-heading strong { color: #667085; font-weight: 500; }.chunk-heading span { color: #98a2b3; font-size: 11px; }
@media (max-width: 1200px) { .settings-panel { padding: 24px; }.retrieval-controls { grid-template-columns: 1fr; gap: 0; }.setting-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }.setting-grid > label:first-child { grid-column: 1 / -1; } }
@media (max-width: 1050px) { .settings-layout { grid-template-columns: 1fr; }.preview-panel { position: static; height: 520px; min-height: 360px; margin: 0 24px 24px; }.retrieval-controls { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px; } }
@media (max-width: 900px) { .steps { position: static; margin-left: auto; transform: none; }.create-content { padding-top: 40px; }.create-panel { width: 100%; } }
@media (max-width: 720px) { .create-header { height: auto; min-height: 64px; flex-wrap: wrap; padding: 14px 16px; }.steps { order: 3; width: 100%; margin-top: 12px; justify-content: center; }.step strong { display: none; }.create-content { padding: 28px 16px; }.source-list { grid-template-columns: 1fr; gap: 10px; }.source-card { height: 58px; }.upload-title { margin-top: 30px !important; }.knowledge-create-page /deep/ .empty-knowledge-dialog { width: calc(100% - 32px) !important; }.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__header { padding: 24px 24px 4px; }.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__body { padding: 12px 24px 8px; }.knowledge-create-page /deep/ .empty-knowledge-dialog .el-dialog__footer { padding: 24px; } }
@media (max-width: 720px) { .settings-panel { padding: 20px 16px; }.setting-grid { grid-template-columns: 1fr; }.choice-grid { grid-template-columns: 1fr; }.preview-panel { margin: 0 16px 20px; } }
@media (max-width: 720px) { .is-adding-documents .create-header { height: auto; }.is-adding-documents .create-content { padding: 28px 16px; } }
</style>
