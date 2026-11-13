<template>
  <section class="segment-images" aria-label="分段图片附件">
    <div class="images-heading"><strong>图片附件</strong><span>{{ items.length }} / {{ maxImages }}</span></div>
    <div class="image-upload-zone" :class="{ 'is-disabled': disabled || items.length >= maxImages }" @dragover.prevent @drop.prevent="dropFiles">
      <input ref="fileInput" class="image-file-input" type="file" multiple accept=".jpg,.jpeg,.png,.gif,image/jpeg,image/png,image/gif" :disabled="disabled || items.length >= maxImages" @change="chooseFiles" />
      <el-button type="text" icon="el-icon-upload2" :disabled="disabled || items.length >= maxImages" @click="$refs.fileInput.click()">点击或拖拽上传图片</el-button>
      <small>JPG、JPEG、PNG、GIF；单张不超过 2 MB，最多 10 张</small>
    </div>
    <div v-if="items.length" class="image-attachments">
      <article v-for="item in items" :key="item.key" class="image-attachment" :class="{ 'has-error': item.status === 'error' }">
        <div class="image-thumbnail"><img v-if="imageSource(item) && !item.previewFailed" :src="imageSource(item)" :alt="item.name" referrerpolicy="no-referrer" @error="item.previewFailed = true" /><i v-else class="el-icon-picture-outline"></i><span v-if="item.status === 'uploading'" class="image-progress"><i class="el-icon-loading"></i></span></div>
        <div class="image-description"><strong :title="item.name">{{ item.name }}</strong><small v-if="item.status === 'uploading'">上传中…</small><small v-else-if="item.status === 'error'" class="image-error">{{ item.error }}</small><small v-else>{{ item.previewFailed ? '已上传，预览暂不可用' : '已就绪' }}</small><el-button v-if="item.status === 'error'" type="text" size="mini" :disabled="disabled" @click="upload(item)">重新上传</el-button></div>
        <el-button type="text" icon="el-icon-close" :disabled="disabled" :aria-label="`移除图片 ${item.name}`" @click="remove(item)" />
      </article>
    </div>
    <p v-if="error" class="image-error" role="alert">{{ error }}</p>
    <small class="image-hint">保存分段后才会关联图片；移除仅取消关联，不会删除已上传文件。</small>
  </section>
</template>

<script>
import { uploadKnowledgeSegmentImage } from '@/api/llmops/knowledge'
import { validateSegmentImage, parseSegmentImageUpload, resolveSegmentImageUrl, MAX_SEGMENT_IMAGES } from '@/utils/knowledgeSegmentImages'
import { knowledgeRetrievalError } from '@/utils/knowledgeRetrieval'

export default {
  name: 'KnowledgeSegmentImageUpload',
  props: { value: { type: Array, default: () => [] }, knowledgeBaseId: { type: String, required: true }, documentId: { type: String, required: true }, disabled: Boolean },
  data() { return { items: [], error: '', revision: 0, sequence: 0, lastEmitted: null, ownedUrls: [], maxImages: MAX_SEGMENT_IMAGES } },
  computed: { context() { return `${this.knowledgeBaseId}/${this.documentId}` } },
  watch: { value(value) { if (value !== this.lastEmitted) this.hydrate(value) }, context() { this.hydrate(this.value) } },
  created() { this.hydrate(this.value) },
  beforeDestroy() { this.revision++; this.releaseUrls() },
  methods: {
    imageSource(item) { return item.preview || resolveSegmentImageUrl(item.preview_url, process.env.VUE_APP_KNOWLEDGE_API_BASE_URL || '/knowledge-api') },
    releaseUrls() { this.ownedUrls.forEach(url => URL.revokeObjectURL(url)); this.ownedUrls = [] },
    hydrate(value) {
      this.revision++; this.releaseUrls(); this.error = ''
      this.items = value.map(image => ({ ...image, key: ++this.sequence, status: 'done', preview: '', previewFailed: false }))
      this.notifyBlocked()
    },
    notifyBlocked() { this.$emit('blocked-change', this.items.some(item => item.status !== 'done')) },
    publish() {
      this.lastEmitted = this.items.filter(item => item.status === 'done').map(item => ({ id: item.id, name: item.name, preview_url: item.preview_url || '', source_url: item.source_url || '' }))
      this.$emit('input', this.lastEmitted)
      this.notifyBlocked()
    },
    chooseFiles(event) { this.addFiles(Array.from(event.target.files || [])); event.target.value = '' },
    dropFiles(event) { this.addFiles(Array.from(event.dataTransfer && event.dataTransfer.files || [])) },
    addFiles(files) {
      if (this.disabled) return
      const errors = []
      files.forEach(file => {
        try {
          validateSegmentImage(file)
          if (this.items.length >= MAX_SEGMENT_IMAGES) throw new Error('每个分段最多 10 张图片')
          let preview = ''
          try { preview = URL.createObjectURL(file); this.ownedUrls.push(preview) } catch (error) { /* Upload still works when local preview is unavailable. */ }
          const item = { key: ++this.sequence, file, name: file.name, id: '', preview_url: '', source_url: '', preview, previewFailed: false, status: 'waiting', error: '' }
          this.items.push(item)
          this.upload(item)
        } catch (error) { errors.push(`${file.name || '图片'}：${error.message}`) }
      })
      this.error = errors.slice(0, 3).join('；')
    },
    async upload(item) {
      if (this.disabled || item.status === 'uploading' || !this.items.includes(item)) return
      const revision = this.revision
      item.status = 'uploading'; item.error = ''; this.notifyBlocked()
      try {
        const image = parseSegmentImageUpload(await uploadKnowledgeSegmentImage(this.knowledgeBaseId, this.documentId, item.file))
        if (revision !== this.revision || !this.items.includes(item)) return
        item.id = image.id; item.source_url = image.source_url; item.preview_url = image.preview_url; item.status = 'done'
        // Keep a local preview: the upload response only guarantees the file ID.
        if (this.items.some(other => other !== item && other.id === image.id)) { this.items = this.items.filter(other => other !== item); this.releaseItemUrl(item) }
        this.publish()
      } catch (error) {
        if (revision !== this.revision || !this.items.includes(item)) return
        item.status = 'error'; item.error = knowledgeRetrievalError(error); this.notifyBlocked()
      }
    },
    releaseItemUrl(item) { if (item.preview) { URL.revokeObjectURL(item.preview); this.ownedUrls = this.ownedUrls.filter(url => url !== item.preview) } },
    remove(item) {
      if (this.disabled) return
      this.items = this.items.filter(other => other !== item)
      this.releaseItemUrl(item)
      this.error = ''
      if (item.status === 'done') this.publish()
      else this.notifyBlocked()
    }
  }
}
</script>

<style scoped>
.segment-images { margin: 18px 0; font-size: 12px; }.images-heading { display: flex; justify-content: space-between; margin-bottom: 10px; color: #667085; }.images-heading strong { font-weight: 500; }.images-heading span { color: #98a2b3; }.image-upload-zone { padding: 15px 8px; text-align: center; border: 1px dashed #d0d5dd; border-radius: 10px; background: #fafbfc; }.image-upload-zone small { display: block; color: #98a2b3; }.image-upload-zone.is-disabled { opacity: .65; }.image-upload-zone .el-button { padding: 5px 0 9px; color: #1649f5; }.image-file-input { display: none; }.image-attachments { display: grid; gap: 8px; margin-top: 12px; }.image-attachment { display: flex; gap: 12px; align-items: center; padding: 8px; border: 1px solid #e4e7ec; border-radius: 9px; }.image-attachment.has-error { border-color: #fecdca; }.image-thumbnail { position: relative; display: grid; place-items: center; width: 62px; height: 56px; flex-shrink: 0; background: #f2f4f7; border-radius: 6px; overflow: hidden; color: #98a2b3; font-size: 22px; }.image-thumbnail img { width: 100%; height: 100%; object-fit: cover; }.image-progress { position: absolute; inset: 0; display: grid; place-items: center; background: #ffffffb8; color: #1649f5; }.image-description { flex: 1; min-width: 0; }.image-description strong { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #344054; font-weight: 500; }.image-description small { display: block; color: #98a2b3; margin-top: 5px; overflow-wrap: anywhere; }.image-description .el-button { padding: 5px 0 0; }.image-attachment > .el-button { color: #98a2b3; }.image-error, .image-description .image-error { color: #f04438; line-height: 1.5; }.image-hint { display: block; margin-top: 10px; color: #98a2b3; line-height: 1.6; }
</style>
