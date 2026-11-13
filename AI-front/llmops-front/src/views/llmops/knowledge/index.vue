<template>
  <div v-loading="loading" class="knowledge-page">
    <header class="knowledge-header">
      <h1>知识库</h1>
      <div class="header-links">
        <span><i class="el-icon-connection"></i> 外部知识库 API</span>
        <span><i class="status-dot"></i> 服务 API</span>
      </div>
    </header>

    <div class="toolbar">
      <div class="filters">
        <el-select v-model="selectedTag" size="small" placeholder="标签" clearable filterable :loading="tagOptionsLoading" :disabled="!!tagsError" class="tag-select">
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
        <el-button size="small" icon="el-icon-price-tag" @click="openTagManager()">管理标签</el-button>
        <el-input v-model.trim="keyword" size="small" clearable placeholder="搜索" prefix-icon="el-icon-search" class="search-input" />
        <span class="filter-divider"></span>
        <el-checkbox v-model="showAll">所有知识库</el-checkbox>
        <el-tooltip content="显示当前可访问的全部知识库" placement="top">
          <i class="el-icon-question help-icon"></i>
        </el-tooltip>
      </div>
      <el-dropdown trigger="click" @command="openCreateDialog">
        <el-button type="primary" size="small" class="create-button"><i class="el-icon-plus"></i> 创建 <i class="el-icon-arrow-down"></i></el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="quick">创建即用型知识库</el-dropdown-item>
          <el-dropdown-item command="custom">构建自定义知识库</el-dropdown-item>
          <el-dropdown-item command="external" divided>连接外部知识库</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <div v-if="tagsError || hasTagLoadError" class="tags-load-warning"><i class="el-icon-warning-outline"></i> {{ tagsError || '部分知识库标签加载失败，标签筛选结果可能不完整' }} <el-button type="text" :disabled="loading || tagOptionsLoading" @click="refreshTags">重试</el-button></div>

    <div v-if="filteredKnowledge.length" class="knowledge-grid">
      <div
        v-for="item in filteredKnowledge"
        :key="item.id"
        class="knowledge-card"
        role="button"
        tabindex="0"
        @click="openKnowledge(item)"
        @keyup.enter.self="openKnowledge(item)"
      >
        <div class="card-icon"><i :class="typeIcon(item.type)"></i></div>
        <div class="card-content">
          <h2>{{ item.name }}</h2>
          <p>{{ item.description || typeDescription(item.type) }}</p>
          <div class="card-tags"><el-tag v-for="tag in item.tags" :key="tag.id" size="mini" type="info">{{ tag.name }}</el-tag><span v-if="item.tagLoadError" class="tag-error">标签加载失败</span></div>
        </div>
        <el-dropdown class="card-actions" trigger="click" placement="bottom-end" @click.native.stop @keydown.native.stop @keyup.native.stop @command="handleKnowledgeCommand($event, item)">
          <button type="button" class="card-more" :aria-label="'管理知识库 ' + item.name"><i class="el-icon-more"></i></button>
          <el-dropdown-menu slot="dropdown" class="knowledge-actions-menu">
            <el-dropdown-item command="edit" icon="el-icon-edit">编辑</el-dropdown-item>
            <el-dropdown-item command="tags" icon="el-icon-price-tag">管理标签</el-dropdown-item>
            <el-dropdown-item command="copy-api" icon="el-icon-copy-document" :disabled="copyingApi">复制 API</el-dropdown-item>
            <el-dropdown-item command="remove" icon="el-icon-delete" divided class="knowledge-delete-action">删除</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <div v-else-if="knowledge.length" class="no-results">
      <i class="el-icon-search"></i><p>没有找到匹配的知识库</p>
    </div>

    <div v-else class="empty-stage">
      <div class="placeholder-grid" aria-hidden="true"><div v-for="n in 12" :key="n" class="placeholder-card"></div></div>
      <div class="empty-content">
        <div class="empty-icon"><i class="el-icon-notebook-2"></i></div>
        <p class="empty-title">创建你的第一个知识库</p>
        <div class="create-options">
          <button class="option-card" type="button" @click="openCreateDialog('quick')">
            <span class="option-icon"><i class="el-icon-plus"></i></span>
            <span><strong>创建即用型知识库</strong><small>上传文档，剩下的交给平台。最快的上手方式。</small></span>
            <em>推荐</em>
          </button>
          <button class="option-card" type="button" @click="openCreateDialog('custom')">
            <span class="option-icon"><i class="el-icon-share"></i></span>
            <span><strong>构建自定义知识库</strong><small>用灵活的节点和步骤搭建自定义的数据处理工作流。</small></span>
          </button>
          <div class="option-separator">或</div>
          <button class="option-card" type="button" @click="openCreateDialog('external')">
            <span class="option-icon"><i class="el-icon-connection"></i></span>
            <span><strong>连接外部知识库</strong><small>已有知识库？通过 API 直接接入，无需迁移数据。</small></span>
          </button>
        </div>
      </div>
    </div>

    <el-dialog :title="createTitle" :visible.sync="dialogVisible" width="480px" @closed="resetForm">
      <el-form ref="form" :model="form" :rules="rules" label-position="top">
        <el-form-item label="知识库名称" prop="name"><el-input v-model.trim="form.name" maxlength="50" show-word-limit placeholder="请输入知识库名称" /></el-form-item>
        <el-form-item label="描述"><el-input v-model.trim="form.description" type="textarea" :rows="3" maxlength="200" placeholder="简要描述知识库的用途" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="createKnowledge">创建</el-button></span>
    </el-dialog>

    <el-dialog
      title="知识库设置"
      :visible.sync="editDialogVisible"
      width="586px"
      custom-class="knowledge-settings-dialog"
      :close-on-click-modal="false"
      :close-on-press-escape="!savingEdit"
      :show-close="!savingEdit"
      @opened="focusEditName"
      @closed="resetEditForm"
    >
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-position="top" :disabled="savingEdit" @submit.native.prevent>
        <el-form-item label="知识库名称" prop="name">
          <div class="knowledge-name-field">
            <span class="edit-knowledge-icon"><i :class="typeIcon(editForm.type)"></i></span>
            <el-input ref="editNameInput" v-model.trim="editForm.name" maxlength="128" placeholder="请输入知识库名称" @keyup.enter.native="saveEdit" />
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="4" maxlength="400" resize="none" placeholder="简要描述知识库的用途" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="knowledge-dialog-footer">
        <el-button :disabled="savingEdit" @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingEdit" @click="saveEdit">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="要删除知识库吗？"
      :visible.sync="deleteDialogVisible"
      width="540px"
      custom-class="knowledge-delete-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="!deleting"
      @closed="deleteTarget = null"
    >
      <p class="delete-description">删除后将无法再访问知识库“{{ deleteTarget ? deleteTarget.name : '' }}”，其中的文档和分段数据也将被永久删除。此操作不可恢复。</p>
      <span slot="footer" class="knowledge-dialog-footer">
        <el-button :disabled="deleting" @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleting" @click="confirmDelete">确认</el-button>
      </span>
    </el-dialog>
    <el-dialog title="调用知识库 API" :visible.sync="apiDialogVisible" width="720px" custom-class="knowledge-api-dialog">
      <p class="api-description">知识库：<strong>{{ apiTarget.name }}</strong>。地址已包含当前项目和知识库信息，无需额外填写 namespace 或知识库 ID。</p>
      <el-alert v-if="apiLocalOnly" title="当前是 localhost 地址，仅供本机程序调用。其他机器调用时，请从可访问的前端 IP 或域名打开页面后重新复制。" type="warning" :closable="false" show-icon />
      <label class="api-label">POST · 检索 URL</label>
      <el-input ref="apiUrlInput" :value="apiTarget.url" readonly type="textarea" :rows="3" resize="none" aria-label="知识库检索 URL" @focus="$event.target.select()" />
      <p class="api-description">请求体只需传入 <code>{"query":"你的问题"}</code>。返回检索分段，不直接生成大模型回答；可将结果继续传给 demo 中的 Ollama。</p>
      <label class="api-label">Python 调用示例</label>
      <pre class="api-code"><code>{{ apiPythonExample }}</code></pre>
      <p class="api-security-note">URL 不含登录凭证，也不替代访问权限控制。对公网开放前，请在服务网关配置鉴权。</p>
      <span slot="footer"><el-button @click="apiDialogVisible = false">关闭</el-button><el-button :disabled="copyingApi" @click="copyApiValue(apiPythonExample)">复制调用示例</el-button><el-button type="primary" :loading="copyingApi" @click="copyApiValue(apiTarget.url)">复制 URL</el-button></span>
    </el-dialog>
    <knowledge-tags-manager :visible.sync="tagManagerVisible" :knowledge-base-id="tagTarget ? tagTarget.id : ''" :knowledge-name="tagTarget ? tagTarget.name : ''" @change="refreshTags" />
  </div>
</template>

<script>
import { getKnowledgeBaseList, updateKnowledgeBase, deleteKnowledgeBase, getKnowledgeTagList, getKnowledgeBaseTags } from '@/api/llmops/knowledge'
import { parseKnowledgeTags } from '@/utils/knowledgeTags'
import KnowledgeTagsManager from './tags-manager.vue'
import { buildKnowledgeInvokeUrl, knowledgeInvokePythonExample, isLocalKnowledgeInvokeUrl } from '@/utils/knowledgeInvoke'

const STORAGE_KEY = 'llmops_knowledge_draft_v1'

export default {
  name: 'KnowledgeIndex',
  components: { KnowledgeTagsManager },
  data() {
    return {
      selectedTag: '', keyword: '', showAll: false, dialogVisible: false, createType: 'quick', loading: false,
      knowledge: [], form: { name: '', description: '' },
      tags: [], tagsError: '', tagOptionsLoading: false, tagManagerVisible: false, tagTarget: null, knowledgeVersion: 0, tagsVersion: 0,
      editDialogVisible: false, savingEdit: false,
      editForm: { id: '', name: '', description: '', type: 'quick' },
      editRules: { name: [{ required: true, whitespace: true, message: '请输入知识库名称', trigger: 'blur' }] },
      deleteDialogVisible: false, deleting: false, deleteTarget: null,
      apiDialogVisible: false, copyingApi: false, apiTarget: { name: '', url: '' },
      rules: { name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }] }
    }
  },
  computed: {
    currentNamespace() { return this.$store.state.project.currentProject && this.$store.state.project.currentProject.name || '' },
    apiPythonExample() { return this.apiTarget.url ? knowledgeInvokePythonExample(this.apiTarget.url) : '' },
    apiLocalOnly() { return this.apiTarget.url ? isLocalKnowledgeInvokeUrl(this.apiTarget.url) : false },
    hasTagLoadError() { return this.knowledge.some(item => item.tagLoadError) },
    filteredKnowledge() {
      const keyword = this.keyword.toLowerCase()
      return this.knowledge.filter(item =>
        (!this.selectedTag || (item.tags || []).some(tag => tag.id === this.selectedTag)) &&
        (!keyword || item.name.toLowerCase().includes(keyword) || (item.description || '').toLowerCase().includes(keyword))
      )
    },
    createTitle() { return { quick: '创建即用型知识库', custom: '构建自定义知识库', external: '连接外部知识库' }[this.createType] }
  },
  watch: {
    currentNamespace() {
      this.apiDialogVisible = false; this.apiTarget = { name: '', url: '' }
      this.knowledge = []; this.selectedTag = ''; this.tagTarget = null; this.tagManagerVisible = false
      this.loadKnowledge(); this.loadTagOptions()
    }
  },
  created() {
    this.loadKnowledge()
    this.loadTagOptions()
  },
  activated() { if (!this.loading && this.$route.name === 'knowledge') this.refreshTags() },
  methods: {
    openKnowledge(item) {
      this.$router.push({
        name: 'knowledge-detail',
        params: { id: item.id },
        query: { ...this.$route.query, name: item.name }
      })
    },
    async loadKnowledge() {
      const version = ++this.knowledgeVersion
      this.loading = true
      try {
        const result = await getKnowledgeBaseList()
        const payload = result && result.data !== undefined ? result.data : result
        const list = Array.isArray(payload) ? payload : (payload && (payload.data || payload.items || payload.results))
        if (!Array.isArray(list)) throw new Error('知识库列表响应格式异常')
        const remoteKnowledge = list.map(item => ({
          ...item,
          type: item.backend_type === 'external' ? 'external' : 'quick',
          description: item.description || '',
          tags: [], tagLoadError: false
        }))
        // The base list has no tags. Read bindings with bounded concurrency instead of using draft labels.
        for (let offset = 0; offset < remoteKnowledge.length; offset += 4) {
          await Promise.all(remoteKnowledge.slice(offset, offset + 4).map(async item => {
            try { item.tags = parseKnowledgeTags(await getKnowledgeBaseTags(item.id)) }
            catch (e) { item.tagLoadError = true }
          }))
          if (version !== this.knowledgeVersion) return
        }
        if (version !== this.knowledgeVersion) return
        this.knowledge = remoteKnowledge
        this.save()
      } catch (e) {
        // 请求失败时保留当前已加载的列表，不再从本地缓存恢复已删除的记录。
      } finally {
        if (version === this.knowledgeVersion) this.loading = false
      }
    },
    async loadTagOptions() {
      const version = ++this.tagsVersion
      this.tagOptionsLoading = true
      this.tagsError = ''
      try {
        const tags = parseKnowledgeTags(await getKnowledgeTagList())
        if (version !== this.tagsVersion) return
        this.tags = tags
        if (this.selectedTag && !tags.some(tag => tag.id === this.selectedTag)) this.selectedTag = ''
      } catch (e) { if (version === this.tagsVersion) this.tagsError = e.message || '标签列表加载失败' }
      finally { if (version === this.tagsVersion) this.tagOptionsLoading = false }
    },
    refreshTags() { return Promise.all([this.loadTagOptions(), this.loadKnowledge()]) },
    openTagManager(item) { this.tagTarget = item ? { id: item.id, name: item.name } : null; this.tagManagerVisible = true },
    openCreateDialog(type) {
      if (type === 'quick') {
        this.$router.push({ name: 'knowledge-create' })
        return
      }
      this.createType = type
      this.dialogVisible = true
    },
    resetForm() { this.form = { name: '', description: '' }; this.$refs.form && this.$refs.form.clearValidate() },
    typeIcon(type) { return type === 'external' ? 'el-icon-connection' : type === 'custom' ? 'el-icon-share' : 'el-icon-notebook-2' },
    typeDescription(type) { return type === 'external' ? '外部知识库' : type === 'custom' ? '自定义知识库' : '即用型知识库' },
    save() {
      try { localStorage.setItem(STORAGE_KEY, JSON.stringify(this.knowledge)) }
      catch (e) { /* 本地缓存不可用不影响已成功的接口操作。 */ }
    },
    createKnowledge() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.knowledge.unshift({ id: Date.now(), type: this.createType, ...this.form })
        this.save()
        this.dialogVisible = false
        this.$message.success('知识库已创建')
      })
    },
    handleKnowledgeCommand(command, item) {
      if (command === 'edit') this.openEdit(item)
      else if (command === 'tags') this.openTagManager(item)
      else if (command === 'copy-api') return this.copyKnowledgeApi(item)
      else if (command === 'remove') this.removeKnowledge(item)
    },
    async copyKnowledgeApi(item) {
      if (this.copyingApi) return
      try {
        const url = buildKnowledgeInvokeUrl({ knowledgeBaseId: item.id, namespace: this.currentNamespace, origin: window.location.origin, baseUrl: process.env.VUE_APP_KNOWLEDGE_INVOKE_BASE_URL })
        this.apiTarget = { name: item.name, url }; this.apiDialogVisible = true
        await this.copyApiValue(url)
      } catch (error) { this.$message.error(error.message || '生成知识库调用地址失败') }
    },
    async copyApiValue(value) {
      if (this.copyingApi || !value) return
      this.copyingApi = true
      try { await this.$copyText(value); this.$message.success('已复制') }
      catch (error) {
        this.$message.warning('复制失败，请在弹窗中手动选择并复制')
        if (value === this.apiTarget.url) this.$nextTick(() => { if (this.$refs.apiUrlInput) this.$refs.apiUrlInput.select() })
      } finally { this.copyingApi = false }
    },
    openEdit(item) {
      this.editForm = { id: item.id, name: item.name, description: item.description || '', type: item.type }
      this.editDialogVisible = true
    },
    focusEditName() { this.$refs.editNameInput && this.$refs.editNameInput.focus() },
    resetEditForm() {
      this.editForm = { id: '', name: '', description: '', type: 'quick' }
      this.$refs.editForm && this.$refs.editForm.clearValidate()
    },
    saveEdit() {
      if (this.savingEdit) return
      this.$refs.editForm.validate(async valid => {
        if (!valid || this.savingEdit) return
        const id = this.editForm.id
        const data = { name: this.editForm.name.trim(), description: this.editForm.description }
        this.savingEdit = true
        try {
          await updateKnowledgeBase(id, data)
          this.knowledge = this.knowledge.map(item => String(item.id) === String(id) ? { ...item, ...data } : item)
          this.save()
          this.editDialogVisible = false
          this.$message.success('知识库设置已保存')
          await this.loadKnowledge()
        } catch (e) {
          // 请求层展示错误，保留表单供用户重试。
        } finally {
          this.savingEdit = false
        }
      })
    },
    removeKnowledge(item) {
      this.deleteTarget = { id: item.id, name: item.name }
      this.deleteDialogVisible = true
    },
    async confirmDelete() {
      if (this.deleting || !this.deleteTarget) return
      const id = this.deleteTarget.id
      this.deleting = true
      try {
        await deleteKnowledgeBase(id)
        this.knowledge = this.knowledge.filter(item => String(item.id) !== String(id))
        this.save()
        this.deleteDialogVisible = false
        this.$message.success('知识库已删除')
        await this.loadKnowledge()
      } catch (e) {
        // 删除失败时保留卡片和确认弹窗。
      } finally {
        this.deleting = false
      }
    }
  }
}
</script>

<style scoped>
.knowledge-page { min-height: 100%; padding: 16px 24px 36px; background: #f5f7fa; color: #344054; box-sizing: border-box; }
.knowledge-header, .toolbar, .filters, .header-links { display: flex; align-items: center; }
.knowledge-header, .toolbar { justify-content: space-between; }
.knowledge-header { margin-bottom: 18px; }
.knowledge-header h1 { margin: 0; color: #101828; font-size: 20px; font-weight: 600; }
.header-links { gap: 24px; color: #667085; font-size: 13px; }
.header-links i { margin-right: 5px; }
.status-dot { display: inline-block; width: 9px; height: 9px; border-radius: 50%; background: #32c98b; }
.toolbar { gap: 16px; margin-bottom: 18px; }
.filters { gap: 10px; flex-wrap: wrap; }
.tag-select { width: 128px; }.search-input { width: 225px; }
.tags-load-warning { padding: 8px 12px; margin-bottom: 16px; color: #b54708; background: #fffaeb; border-radius: 8px; font-size: 13px; }.card-tags { display: flex; flex-wrap: wrap; gap: 5px; }.card-tags .el-tag { max-width: 100%; overflow: hidden; text-overflow: ellipsis; }.tag-error { color: #e6a23c; font-size: 12px; }
.filter-divider { height: 18px; border-left: 1px solid #dfe3e9; }
.help-icon { color: #a8b0be; margin-left: -5px; }
.create-button { background: #1645ec; border-color: #1645ec; border-radius: 8px; }
.create-button .el-icon-arrow-down { margin-left: 3px; }
.knowledge-grid, .placeholder-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.knowledge-card { display: flex; gap: 14px; min-height: 142px; padding: 20px; background: #fff; border: 1px solid #edf0f4; border-radius: 13px; box-sizing: border-box; cursor: pointer; transition: border-color .2s, box-shadow .2s, transform .2s; }
.knowledge-card:hover, .knowledge-card:focus { border-color: #c7d2fe; box-shadow: 0 6px 18px rgba(31,76,255,.08); outline: none; transform: translateY(-1px); }
.card-icon, .option-icon, .empty-icon { display: flex; align-items: center; justify-content: center; flex: none; color: #667085; background: #f6f7f9; }
.card-icon { width: 42px; height: 42px; border-radius: 10px; font-size: 20px; }.card-content { flex: 1; min-width: 0; }
.card-content h2 { margin: 2px 0 8px; font-size: 16px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.card-content p { margin: 0 0 12px; color: #8b95a7; font-size: 13px; line-height: 1.5; }
.card-more { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; margin: -6px -8px 0 0; padding: 0; color: #98a2b3; background: transparent; border: 1px solid transparent; border-radius: 8px; font-size: 17px; cursor: pointer; }
.card-more:hover, .card-more:focus { color: #344054; background: #f5f7fa; border-color: #e9edf3; outline: none; }
.card-actions { align-self: flex-start; flex: none; }
.no-results { padding: 120px 0; text-align: center; color: #98a2b3; }.no-results i { font-size: 30px; }
.empty-stage { position: relative; min-height: 565px; }.placeholder-grid { opacity: .44; }.placeholder-card { height: 178px; background: #fff; border-radius: 13px; }
.empty-content { position: absolute; top: 50%; left: 50%; width: min(586px, 94%); transform: translate(-50%, -50%); text-align: center; }
.empty-icon { width: 62px; height: 62px; margin: 0 auto 12px; border: 1px solid #e9edf3; border-radius: 13px; background: #fff; font-size: 25px; }
.empty-title { margin: 0 0 24px; color: #667085; font-size: 14px; }.create-options { display: flex; flex-direction: column; gap: 8px; }
.option-card { position: relative; display: flex; align-items: center; gap: 14px; width: 100%; min-height: 67px; padding: 11px 14px; text-align: left; background: #fff; border: 1px solid #eef0f4; border-radius: 14px; box-shadow: 0 2px 4px rgba(16,24,40,.03); cursor: pointer; font: inherit; }
.option-card:hover { border-color: #aebfff; box-shadow: 0 4px 15px rgba(29,78,216,.09); }.option-icon { width: 40px; height: 40px; border-radius: 9px; font-size: 17px; }
.option-card strong, .option-card small { display: block; }.option-card strong { margin-bottom: 4px; color: #344054; font-size: 14px; font-weight: 600; }.option-card small { color: #8b95a7; font-size: 12px; }
.option-card em { position: absolute; top: 0; right: 0; padding: 4px 10px; color: #3e62df; background: #e5ecff; border-radius: 0 13px 0 13px; font-size: 11px; font-style: normal; }
.option-separator { display: flex; align-items: center; gap: 10px; color: #98a2b3; font-size: 12px; }.option-separator::before, .option-separator::after { content: ''; flex: 1; border-top: 1px solid #e5e8ef; }
.knowledge-page /deep/ .knowledge-settings-dialog, .knowledge-page /deep/ .knowledge-delete-dialog { max-width: calc(100% - 32px); border-radius: 14px; box-shadow: 0 18px 48px rgba(16,24,40,.16); }
.knowledge-page /deep/ .knowledge-settings-dialog .el-dialog__header { padding: 30px 36px 12px; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-dialog__title { color: #172033; font-size: 20px; font-weight: 600; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-dialog__headerbtn { top: 30px; right: 32px; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-dialog__body { padding: 24px 36px 8px; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-form-item { margin-bottom: 30px; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-form-item__label { padding-bottom: 8px; color: #172033; font-weight: 600; line-height: 20px; }
.knowledge-name-field { display: flex; align-items: center; gap: 10px; }
.edit-knowledge-icon { display: flex; align-items: center; justify-content: center; flex: none; width: 40px; height: 40px; color: #3157ef; background: #eef2ff; border: 1px solid #e2e8ff; border-radius: 10px; font-size: 20px; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-input__inner, .knowledge-page /deep/ .knowledge-settings-dialog .el-textarea__inner { color: #344054; background: #f3f5f8; border-color: #f3f5f8; border-radius: 8px; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-textarea__inner { padding: 12px 14px; line-height: 1.6; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-input__inner:focus, .knowledge-page /deep/ .knowledge-settings-dialog .el-textarea__inner:focus { background: #fff; border-color: #3157ef; }
.knowledge-page /deep/ .knowledge-settings-dialog .el-dialog__footer { padding: 14px 36px 28px; }
.knowledge-dialog-footer { display: flex; align-items: center; justify-content: flex-end; gap: 8px; }
.knowledge-dialog-footer .el-button { min-width: 64px; height: 36px; margin: 0; padding: 9px 16px; border-radius: 8px; }
.knowledge-dialog-footer .el-button--primary { background: #1649f5; border-color: #1649f5; }
.knowledge-dialog-footer .el-button--danger { background: #d92d20; border-color: #d92d20; }
.knowledge-page /deep/ .knowledge-delete-dialog .el-dialog__header { padding: 26px 28px 6px; }
.knowledge-page /deep/ .knowledge-delete-dialog .el-dialog__title { color: #172033; font-size: 18px; font-weight: 600; }
.knowledge-page /deep/ .knowledge-delete-dialog .el-dialog__body { padding: 0 28px; }
.delete-description { margin: 0; color: #788397; font-size: 14px; line-height: 1.7; overflow-wrap: anywhere; }
.knowledge-page /deep/ .knowledge-delete-dialog .el-dialog__footer { padding: 40px 28px 26px; }
.knowledge-page /deep/ .knowledge-api-dialog { max-width: calc(100% - 32px); border-radius: 12px; }.api-description { margin: 0 0 14px; line-height: 1.7; overflow-wrap: anywhere; }.api-description code { color: #1649f5; }.api-label { display: block; margin: 16px 0 8px; color: #344054; font-weight: 600; }.api-code { max-height: 290px; margin: 0; padding: 14px; overflow: auto; color: #344054; background: #f5f7fa; border: 1px solid #e5e9f0; border-radius: 8px; line-height: 1.6; font-size: 12px; }.api-security-note { margin-bottom: 0; color: #8993a5; font-size: 12px; line-height: 1.6; }.knowledge-page /deep/ .knowledge-api-dialog textarea { margin-bottom: 14px; font-family: monospace; }
@media (max-width: 1050px) { .knowledge-grid, .placeholder-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 650px) { .knowledge-page { padding: 16px; }.header-links { display: none; }.toolbar { align-items: flex-start; }.filters { gap: 8px; }.search-input { width: 170px; }.knowledge-grid, .placeholder-grid { grid-template-columns: 1fr; }.empty-content { top: 310px; }.placeholder-card { height: 160px; } }
</style>

<style>
.knowledge-actions-menu.el-dropdown-menu { min-width: 104px; padding: 4px; border: 1px solid #e9edf3; border-radius: 10px; box-shadow: 0 6px 18px rgba(16,24,40,.1); box-sizing: border-box; }
.knowledge-actions-menu.el-popper[x-placement^="bottom"] { margin-top: 6px; }
.knowledge-actions-menu .popper__arrow { display: none; }
.knowledge-actions-menu .el-dropdown-menu__item { padding: 0 8px; color: #475467; border-radius: 5px; font-size: 13px; line-height: 30px; }
.knowledge-actions-menu .el-dropdown-menu__item i { margin-right: 6px; font-size: 14px; }
.knowledge-actions-menu .el-dropdown-menu__item--divided { margin-top: 4px; padding-top: 4px; border-top-color: #edf0f4; }
.knowledge-actions-menu .el-dropdown-menu__item--divided::before { display: none; }
.knowledge-actions-menu .knowledge-delete-action { color: #d92d20; }
.knowledge-actions-menu .knowledge-delete-action:hover, .knowledge-actions-menu .knowledge-delete-action:focus { color: #d92d20; background: #fff1f0; }
</style>
