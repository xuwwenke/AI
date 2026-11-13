<template>
  <div>
    <div v-if="mode === 'single'" class="add-segment-mask" @click.self="close">
      <section class="add-segment-editor" :class="{ 'is-fullscreen': fullscreen }" role="dialog" aria-modal="true" aria-label="新增分段">
        <header><div><h2>新增分段</h2><small>{{ content.length }} 字符</small></div><div><el-button type="text" :icon="fullscreen ? 'el-icon-copy-document' : 'el-icon-full-screen'" aria-label="切换全屏" @click="fullscreen = !fullscreen" /><el-button type="text" icon="el-icon-close" aria-label="关闭新增分段" :disabled="saving" @click="close" /></div></header>
        <div class="add-segment-body">
          <label v-if="qa">问题</label>
          <el-input ref="content" v-model="content" :disabled="saving" type="textarea" :rows="14" resize="none" :placeholder="qa ? '请输入问题' : '请输入分段内容'" />
          <template v-if="qa"><label>答案</label><el-input v-model="answer" :disabled="saving" type="textarea" :rows="5" resize="none" placeholder="请输入答案" /></template>
          <knowledge-segment-image-upload :key="imageRevision" v-model="images" :knowledge-base-id="knowledgeBaseId" :document-id="documentId" :disabled="saving || blocked" @blocked-change="imagesBlocked = $event" />
          <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
        </div>
        <footer><el-checkbox v-model="continuous" :disabled="saving || blocked">连续新增</el-checkbox><span class="footer-spacer"></span><el-button size="small" :disabled="saving" @click="close">取消</el-button><el-button size="small" type="primary" :loading="saving" :disabled="blocked || imagesBlocked" @click="saveSingle">保存 <small>Ctrl S</small></el-button></footer>
      </section>
    </div>
    <el-dialog title="批量添加分段" :visible="mode === 'batch'" width="640px" custom-class="segment-import-dialog" append-to-body :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" :before-close="close">
      <p class="import-description">上传 UTF-8 编码的 CSV 文件，每条记录将作为一个分段。每次最多 {{ maxRows }} 条，文件不超过 5 MB。</p>
      <el-upload ref="upload" class="segment-csv-upload" drag action="#" accept=".csv,text/csv" :auto-upload="false" :show-file-list="false" :disabled="saving || reading || blocked" :on-change="selectFile">
        <i class="el-icon-upload"></i><div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div><div class="el-upload__tip">仅支持 CSV 格式</div>
      </el-upload>
      <div v-if="fileName" class="selected-csv"><i class="el-icon-document"></i><span>{{ fileName }}</span><span>{{ reading ? '校验中…' : `${batchSegments.length} 个分段` }}</span><el-button type="text" icon="el-icon-delete" aria-label="移除 CSV 文件" :disabled="saving || reading || blocked" @click="clearFile" /></div>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <div class="csv-example"><strong>CSV 文件结构</strong><table><thead><tr><th>{{ qa ? '问题' : '分段内容' }}</th><th v-if="qa">答案</th></tr></thead><tbody><tr><td>{{ qa ? '示例问题' : '这里是第一条分段内容' }}</td><td v-if="qa">示例答案</td></tr><tr><td>{{ qa ? '第二个问题' : '这里是第二条分段内容' }}</td><td v-if="qa">第二个答案</td></tr></tbody></table><el-button type="text" icon="el-icon-download" @click="downloadTemplate">下载 CSV 模板</el-button></div>
      <span slot="footer"><el-button size="small" :disabled="saving" @click="close">取消</el-button><el-button size="small" type="primary" :loading="saving" :disabled="reading || !batchSegments.length || blocked" @click="importSegments">导入</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import { createKnowledgeDocumentSegments } from '@/api/llmops/knowledge'
import { parseSegmentCsv, segmentCsvTemplate, MAX_SEGMENT_IMPORT_ROWS, MAX_SEGMENT_IMPORT_BYTES, uncertainKnowledgeWrite } from '@/utils/knowledgeSegments'
import { knowledgeRetrievalError } from '@/utils/knowledgeRetrieval'
import { segmentImageIds } from '@/utils/knowledgeSegmentImages'
import KnowledgeSegmentImageUpload from './segment-image-upload.vue'

export default {
  name: 'KnowledgeDocumentAddSegments',
  components: { KnowledgeSegmentImageUpload },
  props: { mode: { type: String, default: '' }, knowledgeBaseId: { type: String, required: true }, documentId: { type: String, required: true }, qa: Boolean },
  data() { return { content: '', answer: '', continuous: false, fullscreen: false, saving: false, reading: false, blocked: false, error: '', fileName: '', batchSegments: [], revision: 0, fileRevision: 0, maxRows: MAX_SEGMENT_IMPORT_ROWS, images: [], imagesBlocked: false, imageRevision: 0 } },
  computed: { context() { return `${this.knowledgeBaseId}/${this.documentId}/${this.mode}/${this.qa}` } },
  watch: { context() { this.reset() } },
  mounted() { window.addEventListener('keydown', this.onKeydown); this.focusContent() },
  beforeDestroy() { this.revision++; this.fileRevision++; window.removeEventListener('keydown', this.onKeydown) },
  methods: {
    reset() {
      this.revision++; this.fileRevision++
      this.content = ''; this.answer = ''; this.continuous = false; this.fullscreen = false
      this.saving = false; this.reading = false; this.blocked = false; this.error = ''; this.fileName = ''; this.batchSegments = []
      this.images = []; this.imagesBlocked = false; this.imageRevision++
      if (this.$refs.upload) this.$refs.upload.clearFiles()
      this.focusContent()
    },
    focusContent() { this.$nextTick(() => { if (this.mode === 'single' && this.$refs.content) this.$refs.content.focus() }) },
    close() { if (!this.saving) this.$emit('update:mode', '') },
    onKeydown(event) {
      if (this.mode !== 'single') return
      if (event.key === 'Escape') this.close()
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') { event.preventDefault(); this.saveSingle() }
    },
    clearFile() { if (this.saving || this.blocked) return; this.fileRevision++; this.fileName = ''; this.batchSegments = []; this.error = ''; if (this.$refs.upload) this.$refs.upload.clearFiles() },
    async selectFile(upload) {
      if (this.saving || this.reading || this.blocked) return
      this.clearFile()
      const file = upload.raw
      if (!file || !/\.csv$/i.test(file.name)) { this.error = '请选择 CSV 文件'; return }
      this.fileName = file.name
      if (file.size > MAX_SEGMENT_IMPORT_BYTES) { this.error = '文件不能超过 5 MB'; return }
      this.reading = true
      const revision = this.revision, fileRevision = this.fileRevision
      try {
        const buffer = await new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => resolve(reader.result)
          reader.onerror = () => reject(new Error('读取文件失败，请重新选择'))
          reader.readAsArrayBuffer(file)
        })
        if (revision !== this.revision || fileRevision !== this.fileRevision) return
        let text
        try { text = new TextDecoder('utf-8', { fatal: true }).decode(buffer) } catch (error) { throw new Error('文件编码无效，请另存为 UTF-8 CSV 后上传') }
        this.batchSegments = parseSegmentCsv(text, this.qa)
      } catch (error) { if (revision === this.revision && fileRevision === this.fileRevision) this.error = error.message }
      finally { if (revision === this.revision && fileRevision === this.fileRevision) this.reading = false }
    },
    downloadTemplate() {
      const url = URL.createObjectURL(new Blob([segmentCsvTemplate(this.qa)], { type: 'text/csv;charset=utf-8' }))
      const link = document.createElement('a')
      link.href = url; link.download = this.qa ? '问答分段模板.csv' : '分段导入模板.csv'
      document.body.appendChild(link); link.click(); link.remove()
      setTimeout(() => URL.revokeObjectURL(url), 1000)
    },
    saveSingle() {
      if (this.mode !== 'single' || this.saving || this.blocked) return
      if (this.imagesBlocked) { this.error = '请等待图片上传完成，失败的图片请重试或移除'; return }
      if (!this.content.trim()) { this.error = '分段内容不能为空'; return }
      if (this.qa && !this.answer.trim()) { this.error = '答案不能为空'; return }
      let ids
      try { ids = segmentImageIds(this.images) } catch (error) { this.error = error.message; return }
      return this.submit([{ content: this.content, ...(this.qa ? { answer: this.answer } : {}), ...(ids.length ? { attachment_ids: ids } : {}) }])
    },
    importSegments() {
      if (this.mode !== 'batch' || this.reading || !this.batchSegments.length || this.saving || this.blocked) return
      return this.submit(this.batchSegments)
    },
    async submit(segments) {
      if (this.saving || this.blocked) return
      this.saving = true; this.error = ''
      const revision = this.revision, kb = this.knowledgeBaseId, doc = this.documentId
      try {
        await createKnowledgeDocumentSegments(kb, doc, segments)
        if (revision !== this.revision) return
        this.saving = false
        this.$emit('changed', { knowledgeBaseId: kb, documentId: doc })
        this.$message.success(this.mode === 'batch' ? `已提交 ${segments.length} 个分段，索引可能需要一些时间` : '分段已添加')
        if (this.mode === 'single' && this.continuous) { this.content = ''; this.answer = ''; this.images = []; this.imagesBlocked = false; this.imageRevision++; this.focusContent() }
        else this.close()
      } catch (error) {
        if (revision !== this.revision) return
        this.blocked = uncertainKnowledgeWrite(error)
        this.error = this.blocked ? '提交结果未能确认，请关闭窗口并检查分段列表，确认未添加后再重试，避免重复新增。' : knowledgeRetrievalError(error)
        if (this.blocked) this.$emit('changed', { knowledgeBaseId: kb, documentId: doc })
      } finally { if (revision === this.revision) this.saving = false }
    }
  }
}
</script>

<style scoped>
.add-segment-mask { position: fixed; inset: 0; z-index: 2000; background: rgba(16,24,40,.25); }
.add-segment-editor { position: absolute; right: 0; top: 0; bottom: 0; width: min(700px, 100%); display: flex; flex-direction: column; background: white; box-shadow: -8px 0 30px #14233b22; border-radius: 14px 0 0 14px; color: #344054; }
.add-segment-editor.is-fullscreen { width: 100%; border-radius: 0; }.add-segment-editor header { display: flex; justify-content: space-between; padding: 24px; }.add-segment-editor h2 { margin: 0 0 8px; font-size: 20px; color: #17233d; }.add-segment-editor header small { color: #98a2b3; }.add-segment-editor header .el-button { color: #667085; font-size: 18px; padding: 0; }
.add-segment-body { flex: 1; min-height: 0; padding: 0 24px 24px; overflow-y: auto; }.add-segment-body label { display: block; margin: 12px 0 8px; }.add-segment-body /deep/ textarea { line-height: 1.8; border-color: #e4e7ec; }
.add-segment-editor footer { display: flex; align-items: center; gap: 8px; padding: 18px 24px; border-top: 1px solid #eaecf0; }.footer-spacer { flex: 1; }.add-segment-editor footer .el-button { margin: 0; }.add-segment-editor footer small { opacity: .7; margin-left: 8px; }
.import-description { margin-top: 0; color: #667085; font-size: 13px; line-height: 1.7; }.segment-csv-upload /deep/ .el-upload, .segment-csv-upload /deep/ .el-upload-dragger { width: 100%; }.segment-csv-upload /deep/ .el-upload-dragger { height: 170px; }.selected-csv { display: flex; gap: 10px; align-items: center; padding: 10px 0; }.selected-csv span:first-of-type { flex: 1; overflow-wrap: anywhere; }.selected-csv span:last-of-type { color: #667085; font-size: 12px; }.csv-example { margin-top: 22px; }.csv-example strong { font-size: 13px; }.csv-example table { margin-top: 12px; width: 100%; border-collapse: collapse; }.csv-example th, .csv-example td { padding: 10px 14px; border: 1px solid #e4e7ec; text-align: left; font-size: 12px; }.csv-example th { background: #f9fafb; }.el-button--primary { background: #1649f5; border-color: #1649f5; }
</style>
<style>
.segment-import-dialog { max-width: calc(100vw - 32px); border-radius: 14px; }.segment-import-dialog .el-dialog__title { font-weight: 600; }.segment-import-dialog .el-dialog__body { padding-top: 16px; }
</style>
