export const MAX_SEGMENT_IMAGES = 10
export const MAX_SEGMENT_IMAGE_BYTES = 2 * 1024 * 1024

export function validateSegmentImage(file) {
  if (!file || !/\.(jpe?g|png|gif)$/i.test(file.name || '') || file.type && !['image/jpeg', 'image/png', 'image/gif'].includes(file.type.toLowerCase())) throw new Error('仅支持 JPG、JPEG、PNG、GIF 图片')
  if (!Number.isFinite(file.size) || file.size <= 0) throw new Error('不能上传空图片')
  if (file.size > MAX_SEGMENT_IMAGE_BYTES) throw new Error('单张图片不能超过 2 MB')
}

export function parseSegmentImageUpload(result) {
  let data = result
  for (let depth = 0; depth < 4 && data && typeof data === 'object' && !data.id; depth++) data = data.data
  if (!data || typeof data.id !== 'string' || !data.id.trim()) throw new Error('图片上传响应缺少文件 ID，请重新上传')
  return { id: data.id, name: data.name || '图片', preview_url: data.preview_url || '', source_url: safeSegmentImageUrl(data.source_url || data.url) }
}

export function safeSegmentImageUrl(value) {
  // Do not interpret server-provided markup or javascript/data URLs as an image.
  return typeof value === 'string' && (/^https?:\/\//i.test(value) || /^\/(?!\/)/.test(value)) ? value : ''
}

export function resolveSegmentImageUrl(value, apiBaseUrl = '/knowledge-api') {
  const source = safeSegmentImageUrl(value)
  if (!source) return ''
  // Only use the knowledge service's checked preview endpoint, never Dify /files/.
  const path = source.replace(/^https?:\/\/[^/]+/i, '').replace(/^\/knowledge-api(?=\/api\/)/, '')
  if (!/^\/api\/knowledge-bases\/[^/?#]+\/documents\/[^/?#]+\/segments\/[^/?#]+\/images\/[^/?#]+\/preview\/(?:\?|$)/.test(path)) return ''
  const base = safeSegmentImageUrl(apiBaseUrl)
  if (!base) return ''
  // Keep namespace and any signed query parameters byte-for-byte unchanged.
  return base.replace(/\/+$/, '') + path
}

export function readSegmentImages(segment, fallback = {}) {
  const source = ['attachments', 'attachment_ids'].some(key => Object.prototype.hasOwnProperty.call(segment, key)) ? segment : fallback
  const list = Object.prototype.hasOwnProperty.call(source, 'attachments') ? source.attachments : source.attachment_ids
  if (!Array.isArray(list)) return { known: false, images: [] }
  const images = [], ids = new Set()
  for (const field of list) {
    const item = typeof field === 'string' ? { id: field } : field
    if (!item || typeof item.id !== 'string' || !item.id.trim()) return { known: false, images: [] }
    if (ids.has(item.id)) continue
    ids.add(item.id)
    images.push({ id: item.id, name: item.name || '图片', preview_url: item.preview_url || '', source_url: safeSegmentImageUrl(item.source_url || item.url) })
  }
  return { known: true, images }
}

export function segmentImageIds(images) {
  if (!Array.isArray(images) || images.length > MAX_SEGMENT_IMAGES) throw new Error(`每个分段最多 ${MAX_SEGMENT_IMAGES} 张图片`)
  const ids = images.map(image => image && image.id)
  if (ids.some(id => typeof id !== 'string' || !id.trim()) || new Set(ids).size !== ids.length) throw new Error('图片文件 ID 无效或重复，请检查附件')
  return ids
}
