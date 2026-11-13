import request from '@/utils/httpRequest'

const KNOWLEDGE_API_BASE_URL = process.env.VUE_APP_KNOWLEDGE_API_BASE_URL || '/knowledge-api'
const KNOWLEDGE_SUCCESS_CODES = [0, 200, 201]

export function getKnowledgeBaseList() {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: '/api/knowledge-bases/',
    method: 'get',
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function getKnowledgeBase(knowledgeBaseId) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/`,
    method: 'get',
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function getKnowledgeModels(modelType) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/models/${encodeURIComponent(modelType)}/`,
    method: 'get',
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function getKnowledgeTagList() {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: '/api/knowledge-bases/tags/', method: 'get', successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function createKnowledgeTag(name) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: '/api/knowledge-bases/tags/', method: 'post', data: { name }, successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function renameKnowledgeTag(tagId, name) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: '/api/knowledge-bases/tags/', method: 'patch', data: { tag_id: tagId, name }, successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function deleteKnowledgeTag(tagId) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: '/api/knowledge-bases/tags/', method: 'delete', data: { tag_id: tagId }, successCodes: [0, 200, 204] })
}

export function getKnowledgeBaseTags(knowledgeBaseId) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/tags/`, method: 'get', successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function getKnowledgeMetadata(knowledgeBaseId) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/metadata/`, method: 'get', successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function createKnowledgeMetadata(knowledgeBaseId, data) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/metadata/`, method: 'post', data, successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function renameKnowledgeMetadata(knowledgeBaseId, metadataId, name) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/metadata/${metadataId}/`, method: 'patch', data: { name }, successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function deleteKnowledgeMetadata(knowledgeBaseId, metadataId) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/metadata/${metadataId}/`, method: 'delete', successCodes: [0, 200, 204] })
}

export function getKnowledgeBuiltInMetadata(knowledgeBaseId) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/metadata/built-in/`, method: 'get', successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function updateKnowledgeBuiltInMetadata(knowledgeBaseId, enabled) {
  if (typeof enabled !== 'boolean') return Promise.reject(new Error('请选择有效的内置元数据状态'))
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/metadata/built-in/${enabled ? 'enable' : 'disable'}/`, method: 'post', successCodes: [0, 200, 201, 204] })
}

export function bindKnowledgeBaseTags(knowledgeBaseId, tagIds) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/tags/binding/`, method: 'post', data: { tag_ids: tagIds }, successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function unbindKnowledgeBaseTag(knowledgeBaseId, tagId) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/tags/unbinding/`, method: 'post', data: { tag_id: tagId }, successCodes: KNOWLEDGE_SUCCESS_CODES })
}

export function retrieveKnowledgeBase(knowledgeBaseId, data) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/retrieve/`,
    method: 'post',
    data,
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function createKnowledgeBase(data) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: '/api/knowledge-bases/',
    method: 'post',
    data,
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function updateKnowledgeBase(knowledgeBaseId, data) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/`,
    method: 'patch',
    data,
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function deleteKnowledgeBase(knowledgeBaseId) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/`,
    method: 'delete',
    successCodes: [0, 200, 204]
  })
}

export function getKnowledgeDocumentList(knowledgeBaseId, params) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/`,
    method: 'get',
    params,
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function getKnowledgeDocument(knowledgeBaseId, documentId) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/`,
    method: 'get',
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function getKnowledgeDocumentDownload(knowledgeBaseId, documentId) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/download/`,
    method: 'get',
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function updateKnowledgeDocumentStatus(knowledgeBaseId, action, documentIds) {
  if (!['enable', 'disable', 'archive', 'un_archive'].includes(action)) return Promise.reject(new Error('不支持的文档状态操作'))
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/status/${action}/`,
    method: 'patch',
    data: { document_ids: documentIds },
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function deleteKnowledgeDocument(knowledgeBaseId, documentId) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/`,
    method: 'delete',
    successCodes: [0, 200, 204]
  })
}

export function getKnowledgeDocumentSegments(knowledgeBaseId, documentId, params) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/segments/`,
    method: 'get',
    params,
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function createKnowledgeDocumentSegments(knowledgeBaseId, documentId, segments) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/segments/`,
    method: 'post',
    data: { segments },
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function uploadKnowledgeSegmentImage(knowledgeBaseId, documentId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/segments/images/upload/`,
    method: 'post',
    data: formData,
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function updateKnowledgeDocumentMetadata(knowledgeBaseId, documentId, metadataList) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/metadata/`,
    method: 'post',
    data: { operation_data: [{ document_id: documentId, metadata_list: metadataList, partial_update: false }] },
    successCodes: [0, 200, 201, 204]
  })
}

export function getKnowledgeDocumentSegment(knowledgeBaseId, documentId, segmentId) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/segments/${segmentId}/`,
    method: 'get',
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function updateKnowledgeDocumentSegment(knowledgeBaseId, documentId, segmentId, data) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/segments/${segmentId}/`,
    method: 'post',
    data: { segment: data },
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function deleteKnowledgeDocumentSegment(knowledgeBaseId, documentId, segmentId) {
  return request({ baseURL: KNOWLEDGE_API_BASE_URL, url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/segments/${segmentId}/`, method: 'delete', successCodes: [0, 200, 204] })
}

export function batchKnowledgeDocumentSegments(knowledgeBaseId, documentId, action, segmentIds) {
  if (!['enable', 'disable', 'delete'].includes(action)) return Promise.reject(new Error('不支持的分段操作'))
  if (!Array.isArray(segmentIds) || !segmentIds.length || segmentIds.length > 100 || segmentIds.some(id => typeof id !== 'string' || !id.trim())) return Promise.reject(new Error('请选择 1 至 100 个有效分段'))
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${documentId}/segments/batch/`,
    method: 'post',
    data: { action, segment_ids: [...new Set(segmentIds)] },
    successCodes: [0, 200, 207],
    acceptedBusinessCodes: ['BATCH_PARTIALLY_FAILED', 'BATCH_FAILED'],
    // The documented 502 includes per-item failures; let the caller display them.
    validateStatus: status => status >= 200 && status < 300 || status === 502
  })
}

export function uploadKnowledgeDocument(knowledgeBaseId, file, options) {
  const formData = new FormData()
  formData.append('file', file)
  if (options) formData.append('data', JSON.stringify(options))

  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/`,
    method: 'post',
    data: formData,
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}

export function getDocumentIndexingStatus(knowledgeBaseId, batch) {
  return request({
    baseURL: KNOWLEDGE_API_BASE_URL,
    url: `/api/knowledge-bases/${knowledgeBaseId}/documents/${batch}/indexing-status/`,
    method: 'get',
    successCodes: KNOWLEDGE_SUCCESS_CODES
  })
}
