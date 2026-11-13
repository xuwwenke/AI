<template>
  <div v-if="images.length" class="segment-image-gallery" aria-label="分段图片" @click.stop @keydown.stop>
    <figure v-for="(image, index) in images" :key="image.key" class="segment-image-card">
      <el-image v-if="image.src" :key="image.src + ':' + retryRevision" class="segment-image-preview" :src="image.src" :alt="image.name" fit="cover" referrerpolicy="no-referrer" :preview-src-list="previewUrls" :z-index="4000" tabindex="0" role="button" :aria-label="`放大查看 ${image.name}`" @keydown.enter.native.prevent="openPreview(index)" @keydown.space.native.prevent="openPreview(index)" :ref="'image-' + index">
        <div slot="placeholder" class="segment-image-placeholder"><i class="el-icon-loading"></i> 加载中</div>
        <div slot="error" class="segment-image-failed"><i class="el-icon-picture-outline"></i><span>图片加载失败</span><button type="button" @click.stop="retry">重新加载</button></div>
      </el-image>
      <div v-else class="segment-image-failed segment-image-preview"><i class="el-icon-picture-outline"></i><span>暂未返回图片地址</span><button type="button" @click.stop="retry">刷新图片</button></div>
      <figcaption :title="image.name">{{ image.name }}</figcaption>
    </figure>
  </div>
</template>

<script>
import { resolveSegmentImageUrl } from '@/utils/knowledgeSegmentImages'

export default {
  name: 'KnowledgeSegmentImageGallery',
  props: { attachments: { type: Array, default: () => [] } },
  data() { return { retryRevision: 0 } },
  computed: {
    images() {
      return this.attachments.map((attachment, index) => {
        const item = attachment && typeof attachment === 'object' ? attachment : { id: attachment }
        return { key: `${item.id || 'image'}-${index}`, name: item.name || `图片 ${index + 1}`, src: resolveSegmentImageUrl(item.preview_url, process.env.VUE_APP_KNOWLEDGE_API_BASE_URL || '/knowledge-api') }
      })
    },
    previewUrls() { return this.images.map(image => image.src).filter(Boolean) }
  },
  methods: {
    retry() { this.retryRevision++; this.$emit('retry') },
    openPreview(index) {
      const refs = this.$refs['image-' + index]
      const image = Array.isArray(refs) ? refs[0] : refs
      if (image && !image.loading && !image.error) image.clickHandler()
    }
  }
}
</script>

<style scoped>
.segment-image-gallery { display: flex; flex-wrap: wrap; gap: 12px; margin: 14px 0; cursor: default; }.segment-image-card { width: 160px; max-width: 100%; margin: 0; }.segment-image-preview { display: block; width: 100%; height: 112px; border: 1px solid #e4e7ec; border-radius: 8px; background: #f8fafc; box-sizing: border-box; cursor: zoom-in; }.segment-image-preview:focus-visible { outline: 2px solid #1649f5; outline-offset: 3px; }.segment-image-preview /deep/ .el-image__inner { transition: transform .15s; }.segment-image-preview:hover /deep/ .el-image__inner { transform: scale(1.03); }.segment-image-placeholder, .segment-image-failed { display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 7px; width: 100%; height: 100%; color: #98a2b3; font-size: 12px; cursor: default; }.segment-image-failed > i { font-size: 22px; }.segment-image-failed button { border: 0; padding: 0; color: #1649f5; background: none; cursor: pointer; font-size: 12px; }.segment-image-card figcaption { margin-top: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #98a2b3; font-size: 11px; }
</style>
