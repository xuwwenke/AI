<template>
  <div class="knowledge-metadata-manager">
    <el-drawer :visible="visible" direction="rtl" size="480px" :with-header="false" append-to-body custom-class="knowledge-metadata-drawer" :wrapper-closable="!busy && !createVisible" :close-on-press-escape="!busy && !createVisible && !renameVisible" :before-close="beforeClose" @update:visible="$emit('update:visible', $event)">
      <div class="metadata-panel">
        <header class="metadata-header"><h2>元数据</h2><button type="button" :disabled="busy" aria-label="关闭元数据面板" @click="closePanel"><i class="el-icon-close"></i></button></header>
        <p class="metadata-description">元数据是关于文档的数据，用于描述文档的属性。元数据可以帮助您更好地组织和管理文档。</p>
        <el-popover placement="left-start" width="330" trigger="manual" :value="createVisible" :visible-arrow="false" popper-class="knowledge-metadata-create-popover">
          <div class="metadata-create-form" @keydown.esc.stop="cancelCreate">
            <button type="button" class="metadata-form-back" :disabled="busy" @click="cancelCreate"><i class="el-icon-back"></i> 返回</button>
            <h3>新建元数据</h3>
            <label>类型</label><div class="metadata-type-options" role="group" aria-label="元数据类型"><button v-for="type in metadataTypes" :key="type.value" type="button" :class="{ 'is-selected': createForm.type === type.value }" :aria-pressed="createForm.type === type.value" :disabled="busy" @click="createForm.type = type.value">{{ type.label }}</button></div>
            <label for="metadata-create-name">名称</label><el-input id="metadata-create-name" ref="createNameInput" v-model="createForm.name" size="small" placeholder="添加元数据名称" :disabled="busy" @keyup.enter.native="saveCreate" />
            <p v-if="createError" class="metadata-field-error" role="alert">{{ createError }}</p>
            <footer><el-button size="small" :disabled="busy" @click="cancelCreate">取消</el-button><el-button type="primary" size="small" :loading="pending === 'create'" :disabled="busy || !ready || !createForm.name.trim()" @click="saveCreate">保存</el-button></footer>
          </div>
          <el-button slot="reference" type="primary" size="small" icon="el-icon-plus" :disabled="busy || !ready" @click="openCreate">添加元数据</el-button>
        </el-popover>
        <div v-if="loadError" class="metadata-load-error" role="alert"><p>{{ loadError }}</p><el-button type="text" size="small" :disabled="loading || busy" @click="loadMetadata">重新加载</el-button></div>
        <div v-loading="loading" class="metadata-list-content">
          <div v-if="fields.length" class="metadata-custom-list">
            <div v-for="field in fields" :key="field.id" class="metadata-field-row"><i :class="fieldIcon(field.type)"></i><span class="metadata-field-name" :title="field.name">{{ field.name }}</span><small>{{ field.type }}</small><div class="metadata-row-actions"><el-button type="text" icon="el-icon-edit" :aria-label="'重命名元数据 ' + field.name" :disabled="busy || !ready" @click="openRename(field)" /><el-button type="text" icon="el-icon-delete" class="metadata-delete-button" :aria-label="'删除元数据 ' + field.name" :disabled="busy || !ready" @click="removeField(field)" /></div></div>
          </div>
          <div class="metadata-built-in-heading"><el-switch :value="builtInEnabled === true" :width="32" :disabled="busy || !ready" aria-label="启用内置元数据" @change="toggleBuiltIn" /><strong>内置</strong><el-tooltip content="控制当前知识库的内置元数据是否启用，内置字段不支持重命名或删除。" placement="top"><i class="el-icon-question" tabindex="0" aria-label="内置元数据说明"></i></el-tooltip><i v-if="pending === 'built-in'" class="el-icon-loading"></i></div>
          <div v-for="field in builtInFields" :key="field.name" class="metadata-field-row metadata-built-in-row" :class="{ 'is-disabled': !builtInEnabled }"><i :class="fieldIcon(field.type)"></i><span class="metadata-field-name" :title="field.name">{{ field.name }}</span><small>{{ field.type }}</small><span class="metadata-field-state">{{ builtInEnabled ? '已启用' : '已禁用' }}</span></div>
          <p v-if="ready && !builtInFields.length" class="metadata-empty">暂无内置元数据字段</p>
        </div>
      </div>
    </el-drawer>
    <el-dialog title="重命名" :visible.sync="renameVisible" width="540px" custom-class="knowledge-metadata-rename-dialog" append-to-body :close-on-click-modal="false" :close-on-press-escape="!busy" :show-close="!busy" :before-close="beforeRenameClose">
      <label class="metadata-rename-label" for="metadata-rename-name">名称</label><el-input id="metadata-rename-name" ref="renameNameInput" v-model="renameForm.name" size="small" :disabled="busy" @keyup.enter.native="saveRename" />
      <p v-if="renameError" class="metadata-field-error" role="alert">{{ renameError }}</p>
      <span slot="footer"><el-button size="small" :disabled="busy" @click="renameVisible = false">取消</el-button><el-button type="primary" size="small" :loading="pending === 'rename'" :disabled="busy || !ready || !renameForm.name.trim()" @click="saveRename">保存</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import { getKnowledgeMetadata, createKnowledgeMetadata, renameKnowledgeMetadata, deleteKnowledgeMetadata, getKnowledgeBuiltInMetadata, updateKnowledgeBuiltInMetadata } from '@/api/llmops/knowledge'
import { parseKnowledgeMetadata, validateKnowledgeMetadataName } from '@/utils/knowledgeMetadata'
import { knowledgeRetrievalError } from '@/utils/knowledgeRetrieval'

export default {
  name: 'KnowledgeMetadataManager',
  props: { visible: { type: Boolean, default: false }, knowledgeBaseId: { type: String, default: '' } },
  data() {
    return {
      fields: [], builtInFields: [], builtInEnabled: null, loading: false, loadError: '', pending: '', requestVersion: 0, contextVersion: 0,
      createVisible: false, createForm: { name: '', type: 'string' }, createError: '',
      renameVisible: false, renameForm: { id: '', name: '' }, renameError: '',
      metadataTypes: [{ value: 'string', label: 'String' }, { value: 'number', label: 'Number' }, { value: 'time', label: 'Time' }]
    }
  },
  computed: {
    busy() { return !!this.pending },
    ready() { return this.visible && !!this.knowledgeBaseId && !this.loading && !this.loadError && typeof this.builtInEnabled === 'boolean' }
  },
  watch: {
    visible: { immediate: true, handler(value) { this.resetContext(); if (value) this.loadMetadata() } },
    knowledgeBaseId() { this.resetContext(); if (this.visible) this.loadMetadata() }
  },
  beforeDestroy() { ++this.contextVersion; ++this.requestVersion },
  methods: {
    resetContext() {
      ++this.contextVersion; ++this.requestVersion
      this.fields = []; this.builtInFields = []; this.builtInEnabled = null
      this.loading = false; this.loadError = ''; this.pending = ''; this.createVisible = false; this.renameVisible = false
    },
    closePanel() { if (!this.busy) { this.createVisible = false; this.renameVisible = false; this.$emit('update:visible', false) } },
    beforeClose(done) { if (!this.busy) { this.createVisible = false; this.renameVisible = false; done() } },
    beforeRenameClose(done) { if (!this.busy) done() },
    fieldIcon(type) { return type === 'time' ? 'el-icon-time' : type === 'number' ? 'el-icon-data-analysis' : 'el-icon-tickets' },
    async loadMetadata() {
      const id = this.knowledgeBaseId
      if (!this.visible || !id) return false
      const version = ++this.requestVersion
      this.loading = true; this.loadError = ''
      try {
        const [result, builtIn] = await Promise.all([getKnowledgeMetadata(id), getKnowledgeBuiltInMetadata(id)])
        if (!this.visible || id !== this.knowledgeBaseId || version !== this.requestVersion) return false
        const state = parseKnowledgeMetadata(result, builtIn)
        this.fields = state.fields; this.builtInFields = state.builtInFields; this.builtInEnabled = state.builtInEnabled
        return true
      } catch (e) {
        if (version === this.requestVersion) this.loadError = knowledgeRetrievalError(e)
        return false
      } finally { if (version === this.requestVersion) this.loading = false }
    },
    openCreate() {
      if (this.busy || !this.ready) return
      this.renameVisible = false; this.createForm = { name: '', type: 'string' }; this.createError = ''; this.createVisible = true
      this.$nextTick(() => { if (this.$refs.createNameInput) this.$refs.createNameInput.focus() })
    },
    cancelCreate() { if (!this.busy) this.createVisible = false },
    editable(field) { return field && !!field.id && this.fields.some(item => item.id === field.id) && !this.builtInFields.some(item => item.name === field.name) },
    openRename(field) {
      if (this.busy || !this.ready || !this.editable(field)) return
      this.createVisible = false; this.renameForm = { id: field.id, name: field.name }; this.renameError = ''; this.renameVisible = true
      this.$nextTick(() => { if (this.$refs.renameNameInput) this.$refs.renameNameInput.focus() })
    },
    async mutate(key, request, message, verify) {
      if (this.busy || !this.ready) return false
      const id = this.knowledgeBaseId
      const context = this.contextVersion
      this.pending = key
      try {
        await request(id)
        this.$emit('change', { knowledgeBaseId: id })
        if (context !== this.contextVersion) return false
        const loaded = await this.loadMetadata()
        if (context !== this.contextVersion) return false
        if (!loaded) this.$message.warning('操作已提交，但元数据刷新失败，请重新加载确认，勿重复提交')
        else if (!verify()) this.$message.warning('接口已响应，但元数据状态尚未生效，已展示服务端当前结果，请刷新确认')
        else this.$message.success(message)
        return true
      } catch (e) {
        if (context === this.contextVersion) {
          const error = knowledgeRetrievalError(e)
          if (key === 'create') this.createError = error
          else if (key === 'rename') this.renameError = error
          this.$message.error(error)
        }
        return false
      } finally { if (context === this.contextVersion) this.pending = '' }
    },
    async saveCreate() {
      if (this.busy || !this.ready || !this.createVisible) return
      let name
      try {
        name = validateKnowledgeMetadataName(this.createForm.name, this.fields, this.builtInFields)
        if (!this.metadataTypes.some(type => type.value === this.createForm.type)) throw new Error('请选择有效的元数据类型')
      } catch (e) { this.createError = e.message; return }
      this.createError = ''
      const type = this.createForm.type
      if (await this.mutate('create', id => createKnowledgeMetadata(id, { name, type }), '元数据已添加', () => this.fields.some(field => field.name === name && field.type === type))) this.createVisible = false
    },
    async saveRename() {
      if (this.busy || !this.ready || !this.renameVisible) return
      const field = this.fields.find(item => item.id === this.renameForm.id)
      if (!this.editable(field)) return
      let name
      try { name = validateKnowledgeMetadataName(this.renameForm.name, this.fields, this.builtInFields, field.id) }
      catch (e) { this.renameError = e.message; return }
      if (name === field.name) { this.renameVisible = false; return }
      this.renameError = ''
      if (await this.mutate('rename', id => renameKnowledgeMetadata(id, field.id, name), '元数据已重命名', () => this.fields.some(item => item.id === field.id && item.name === name))) this.renameVisible = false
    },
    async removeField(field) {
      if (this.busy || !this.ready || !this.editable(field)) return
      const context = this.contextVersion
      this.pending = 'confirm-delete'
      try {
        await this.$confirm(`确定删除元数据“${field.name}”吗？删除字段可能清除文档中对应的元数据值，文档本身不会删除。此操作不可撤销。`, '删除元数据', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger', closeOnClickModal: false })
      } catch (e) { if (context === this.contextVersion) this.pending = ''; return }
      if (context !== this.contextVersion) return
      this.pending = ''
      await this.mutate('delete', id => deleteKnowledgeMetadata(id, field.id), '元数据已删除', () => !this.fields.some(item => item.id === field.id))
    },
    async toggleBuiltIn(enabled) {
      if (typeof enabled !== 'boolean' || enabled === this.builtInEnabled) return
      await this.mutate('built-in', id => updateKnowledgeBuiltInMetadata(id, enabled), enabled ? '内置元数据已启用' : '内置元数据已禁用', () => this.builtInEnabled === enabled)
    }
  }
}
</script>

<style>
.knowledge-metadata-drawer.el-drawer { top: 8px; right: 8px; bottom: 8px; height: auto; max-width: calc(100vw - 16px); border: 1px solid #edf0f4; border-radius: 16px; box-shadow: 0 12px 36px rgba(16,24,40,.12); }.knowledge-metadata-drawer .el-drawer__body { overflow-y: auto; }.knowledge-metadata-drawer .metadata-panel { padding: 24px 18px; color: #17233d; }.knowledge-metadata-drawer .metadata-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }.knowledge-metadata-drawer .metadata-header h2 { margin: 0; font-size: 18px; font-weight: 600; }.knowledge-metadata-drawer .metadata-header button { padding: 4px; border: 0; color: #7c8598; background: none; font-size: 17px; cursor: pointer; }.knowledge-metadata-drawer .metadata-description { margin: 0 0 14px; color: #7c8598; font-size: 13px; line-height: 1.6; }.knowledge-metadata-drawer .el-button--primary, .knowledge-metadata-create-popover .el-button--primary, .knowledge-metadata-rename-dialog .el-button--primary { background: #1649f5; border-color: #1649f5; border-radius: 8px; }.knowledge-metadata-drawer .el-button--primary.is-disabled { opacity: .55; }.knowledge-metadata-drawer .metadata-list-content { min-height: 180px; margin-top: 16px; }.knowledge-metadata-drawer .metadata-custom-list { display: grid; gap: 6px; margin-bottom: 18px; }.knowledge-metadata-drawer .metadata-field-row { display: flex; align-items: center; gap: 5px; min-height: 38px; padding: 0 10px; color: #344054; border: 1px solid #e8ebf1; border-radius: 7px; font-size: 13px; box-sizing: border-box; }.knowledge-metadata-drawer .metadata-field-row > i { flex: none; color: #7d89a2; font-size: 15px; }.knowledge-metadata-drawer .metadata-field-name { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.knowledge-metadata-drawer .metadata-field-row small { flex: none; color: #7c8598; font-size: 12px; }.knowledge-metadata-drawer .metadata-row-actions { display: flex; gap: 8px; flex: none; margin-left: auto; }.knowledge-metadata-drawer .metadata-row-actions .el-button { margin: 0; padding: 3px; color: #7c8598; font-size: 16px; }.knowledge-metadata-drawer .metadata-row-actions .el-button:hover { background: #f3f5f9; color: #1649f5; border-radius: 4px; }.knowledge-metadata-drawer .metadata-row-actions .metadata-delete-button:hover { color: #f56c6c; }.knowledge-metadata-drawer .metadata-built-in-heading { display: flex; align-items: center; gap: 8px; margin: 18px 0 9px; color: #344054; font-size: 13px; }.knowledge-metadata-drawer .metadata-built-in-heading > i { color: #98a2b3; }.knowledge-metadata-drawer .metadata-built-in-row { margin-bottom: 5px; }.knowledge-metadata-drawer .metadata-field-state { flex: none; margin-left: auto; color: #8b95a7; font-size: 12px; }.knowledge-metadata-drawer .metadata-built-in-row.is-disabled { color: #b5bbc6; }.knowledge-metadata-drawer .metadata-built-in-row.is-disabled > i, .knowledge-metadata-drawer .metadata-built-in-row.is-disabled small, .knowledge-metadata-drawer .metadata-built-in-row.is-disabled .metadata-field-state { color: #c5cad4; }.knowledge-metadata-drawer .metadata-load-error { margin-top: 15px; padding: 10px; color: #b54708; background: #fffaeb; border-radius: 7px; font-size: 12px; }.knowledge-metadata-drawer .metadata-load-error p { margin: 0; line-height: 1.6; }.knowledge-metadata-drawer .metadata-empty { color: #98a2b3; font-size: 12px; }
.knowledge-metadata-create-popover.el-popover { max-width: calc(100vw - 40px); padding: 16px; color: #17233d; border-radius: 12px; box-shadow: 0 12px 32px rgba(16,24,40,.16); }.metadata-create-form .metadata-form-back { padding: 0; color: #1649f5; background: none; border: 0; cursor: pointer; font: inherit; font-size: 13px; }.metadata-create-form h3 { margin: 10px 0 18px; font-size: 16px; }.metadata-create-form > label, .knowledge-metadata-rename-dialog .metadata-rename-label { display: block; margin-bottom: 8px; color: #344054; font-size: 13px; font-weight: 600; }.metadata-create-form .metadata-type-options { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 20px; }.metadata-create-form .metadata-type-options button { height: 36px; color: #475467; background: #fafbfc; border: 1px solid #e4e8ef; border-radius: 7px; font: inherit; font-size: 13px; cursor: pointer; }.metadata-create-form .metadata-type-options .is-selected { background: #fff; border-color: #1649f5; color: #1649f5; }.metadata-create-form .el-input__inner { background: #f3f4f7; border-color: #f3f4f7; border-radius: 7px; }.metadata-create-form footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 18px; }.metadata-create-form footer .el-button { margin: 0; border-radius: 8px; }.metadata-field-error { margin: 8px 0 0; color: #f56c6c; font-size: 12px; line-height: 1.6; }.knowledge-metadata-rename-dialog { max-width: calc(100vw - 32px); border-radius: 16px; }.knowledge-metadata-rename-dialog .el-dialog__header { padding: 26px 28px 10px; }.knowledge-metadata-rename-dialog .el-dialog__title { color: #17233d; font-size: 20px; font-weight: 600; }.knowledge-metadata-rename-dialog .el-dialog__body { padding: 12px 28px 4px; }.knowledge-metadata-rename-dialog .el-input__inner { border-radius: 7px; }.knowledge-metadata-rename-dialog .el-dialog__footer { padding: 18px 28px 26px; }.knowledge-metadata-rename-dialog .el-button { border-radius: 8px; }
@media (max-width: 800px) { .knowledge-metadata-create-popover.el-popover { left: 50% !important; top: 110px !important; transform: translateX(-50%); } }
</style>
