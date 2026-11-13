<template>
  <section class="document-metadata" v-loading="loading">
    <div v-if="!editing" class="metadata-intro"><strong>元数据</strong><p>为文档添加结构化属性，帮助组织文档和精准检索。</p><dl v-if="customValues.length"><template v-for="field in customValues"><dt :key="field.id + '-name'">{{ field.name }}</dt><dd :key="field.id + '-value'">{{ displayValue(field) }}</dd></template></dl><el-button type="primary" size="small" :disabled="loading || saving" @click="start">{{ customValues.length ? '编辑标注' : '开始标注' }} <i class="el-icon-right"></i></el-button></div>
    <template v-else>
      <div class="metadata-title"><strong>元数据</strong><div><el-button type="text" size="small" :disabled="saving" @click="cancel">取消</el-button><el-button type="primary" size="mini" :loading="saving" :disabled="loading || !ready" @click="save">保存</el-button></div></div>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <el-button v-if="!ready && !loading" type="text" @click="load">重新加载</el-button>
      <div v-for="(field, index) in rows" :key="field.id" class="metadata-value-row">
        <div class="field-label"><span>{{ field.name }} <small>{{ field.type }}</small></span><el-button v-if="!field.readonly" type="text" icon="el-icon-delete" :disabled="saving" aria-label="移除此文档的标注" @click="rows.splice(index, 1)" /></div>
        <span v-if="field.readonly" class="readonly-value">{{ displayValue(field) }}（字段不可用，保留原值）</span>
        <el-input v-else-if="field.type === 'string'" v-model="field.value" size="small" :disabled="saving" placeholder="输入值" />
        <el-input v-else-if="field.type === 'number'" v-model="field.value" size="small" :disabled="saving" placeholder="输入数字" />
        <el-date-picker v-else-if="field.type === 'time'" :value="dateValue(field.value)" type="datetime" size="small" :disabled="saving" placeholder="选择日期时间" @input="setDate(field, $event)" />
      </div>
      <el-popover placement="bottom-start" width="250" trigger="click" :disabled="!ready || saving">
        <div class="metadata-picker"><p v-if="!availableFields.length">{{ fields.length ? '全部字段已添加' : '请先在文档列表的“元数据”中创建字段' }}</p><el-button v-for="field in availableFields" :key="field.id" type="text" :disabled="saving" @click="add(field)">{{ field.name }} <small>{{ field.type }}</small></el-button></div>
        <el-button slot="reference" class="add-metadata" size="small" icon="el-icon-plus" :disabled="!ready || saving">添加元数据</el-button>
      </el-popover>
      <div class="built-in-heading">内置 <el-tag size="mini" type="info">只读{{ builtInEnabled ? '' : ' · 已禁用' }}</el-tag></div>
      <dl class="built-in-values"><template v-for="field in builtInValues"><dt :key="field.name + '-name'">{{ field.name }}</dt><dd :key="field.name + '-value'">{{ displayValue(field) }}</dd></template></dl>
      <p class="metadata-note">移除标注仅影响当前文档，不会删除字段定义。</p>
    </template>
  </section>
</template>

<script>
import { getKnowledgeMetadata, getKnowledgeBuiltInMetadata, getKnowledgeDocument, updateKnowledgeDocumentMetadata } from '@/api/llmops/knowledge'
import { parseKnowledgeMetadata } from '@/utils/knowledgeMetadata'
import { documentMetadataRows, serializeDocumentMetadata, isBuiltInDocumentMetadata } from '@/utils/knowledgeDocumentMetadata'
import { knowledgeRetrievalError } from '@/utils/knowledgeRetrieval'

export default {
  name: 'KnowledgeDocumentMetadata',
  props: { knowledgeBaseId: { type: String, required: true }, documentId: { type: String, required: true }, documentInfo: { type: Object, default: () => ({}) } },
  data() { return { editing: false, loading: false, saving: false, ready: false, fields: [], rows: [], snapshot: {}, builtInEnabled: true, error: '', revision: 0 } },
  computed: {
    context() { return `${this.knowledgeBaseId}/${this.documentId}` },
    customValues() { return (Array.isArray(this.documentInfo.doc_metadata) ? this.documentInfo.doc_metadata : []).filter(field => !isBuiltInDocumentMetadata(field)) },
    builtInValues() { return (Array.isArray(this.snapshot.doc_metadata) ? this.snapshot.doc_metadata : []).filter(isBuiltInDocumentMetadata) },
    availableFields() { return this.fields.filter(field => !this.rows.some(row => row.id === field.id)) }
  },
  watch: { context() { this.revision++; this.editing = false; this.loading = false; this.saving = false; this.ready = false; this.rows = []; this.snapshot = {}; this.error = '' } },
  beforeDestroy() { this.revision++ },
  methods: {
    unwrap(result) { return result && result.data !== undefined ? result.data : result },
    start() { if (this.loading || this.saving) return; this.editing = true; return this.load() },
    cancel() { if (this.saving) return; this.revision++; this.editing = false; this.loading = false; this.ready = false; this.error = ''; this.rows = [] },
    async load() {
      if (this.saving) return false
      const revision = ++this.revision, kb = this.knowledgeBaseId, doc = this.documentId
      this.loading = true; this.ready = false; this.error = ''
      try {
        const results = await Promise.all([getKnowledgeMetadata(kb), getKnowledgeBuiltInMetadata(kb), getKnowledgeDocument(kb, doc)])
        if (revision !== this.revision) return false
        const parsed = parseKnowledgeMetadata(results[0], results[1])
        this.snapshot = this.unwrap(results[2]) || {}
        this.fields = parsed.fields; this.builtInEnabled = parsed.builtInEnabled
        this.rows = documentMetadataRows(this.snapshot, this.fields)
        this.ready = true
        return true
      } catch (error) { if (revision === this.revision) this.error = knowledgeRetrievalError(error); return false }
      finally { if (revision === this.revision) this.loading = false }
    },
    add(field) { if (this.ready && !this.saving && !this.rows.some(row => row.id === field.id)) this.rows.push({ id: field.id, name: field.name, type: field.type, value: null, readonly: false }) },
    dateValue(value) { if (value === '' || value == null) return null; const date = new Date(Number(value) * 1000); return Number.isNaN(date.getTime()) ? null : date },
    setDate(field, value) { field.value = value ? value.getTime() / 1000 : null },
    displayValue(field) {
      if (field.value == null || field.value === '') return '—'
      if (field.type === 'time') { const date = this.dateValue(field.value); return date ? date.toLocaleString() : String(field.value) }
      return String(field.value)
    },
    async save() {
      if (this.saving || this.loading || !this.ready || !this.editing) return
      let values
      try { values = serializeDocumentMetadata(this.rows) } catch (error) { this.error = error.message; return }
      const revision = this.revision, kb = this.knowledgeBaseId, doc = this.documentId
      this.saving = true; this.error = ''
      try {
        await updateKnowledgeDocumentMetadata(kb, doc, values)
        if (revision !== this.revision) return
        // A committed write is never retried just because its subsequent GET failed.
        this.editing = false; this.ready = false
        try {
          const detail = this.unwrap(await getKnowledgeDocument(kb, doc))
          if (revision !== this.revision) return
          const actual = serializeDocumentMetadata(documentMetadataRows(detail, this.fields))
          const matches = values.length === actual.length && values.every(field => actual.some(item => item.id === field.id && item.name === field.name && (item.value == null && field.value == null || String(item.value) === String(field.value))))
          this.$emit('updated', { knowledgeBaseId: kb, documentId: doc, document: detail })
          if (matches) this.$message.success('元数据标注已保存')
          else this.$message.warning('请求已提交，但返回值与本次标注不一致，请重新检查')
        } catch (error) {
          if (revision === this.revision) {
            this.$message.warning('标注已提交，刷新文档失败，请刷新后确认')
            this.$emit('changed', { knowledgeBaseId: kb, documentId: doc })
          }
        }
      } catch (error) { if (revision === this.revision) this.error = knowledgeRetrievalError(error) }
      finally { if (revision === this.revision) this.saving = false }
    }
  }
}
</script>

<style scoped>
.document-metadata { font-size: 12px; }.metadata-intro { padding: 16px; background: #f7f8fa; border-radius: 12px; }.metadata-intro strong { font-size: 14px; color: #17233d; }.metadata-intro p { color: #667085; line-height: 1.6; }.document-metadata .el-button--primary { background: #1649f5; border-color: #1649f5; }.metadata-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }.metadata-title strong { color: #17233d; font-size: 14px; }.metadata-title .el-button + .el-button { margin-left: 8px; }.metadata-value-row { margin-bottom: 14px; }.field-label { display: flex; align-items: center; justify-content: space-between; min-height: 27px; overflow-wrap: anywhere; }.field-label small, .metadata-picker small { color: #98a2b3; margin-left: 5px; }.field-label .el-button { padding: 3px; color: #98a2b3; }.metadata-value-row /deep/ .el-date-editor { width: 100%; }.add-metadata { width: 100%; text-align: left; color: #667085; border-style: dashed; }.built-in-heading { margin: 24px 0 14px; color: #667085; }.built-in-heading .el-tag { margin-left: 8px; }.document-metadata dl { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 12px; margin: 14px 0; }.document-metadata dt { color: #98a2b3; overflow-wrap: anywhere; }.document-metadata dd { margin: 0; overflow-wrap: anywhere; }.metadata-note, .readonly-value { color: #98a2b3; line-height: 1.6; }.metadata-note { margin-top: 16px; }.metadata-picker { display: grid; max-height: 260px; overflow-y: auto; }.metadata-picker .el-button { text-align: left; margin: 0; }.metadata-picker p { color: #98a2b3; font-size: 12px; }
</style>
