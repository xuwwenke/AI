import request from '@/utils/httpRequest'
import axios from 'axios'


export function getDatasetList(params) {
  return request({
    url: '/llmops-service/api/dataset/page',
    method: 'get',
    params
  })
}

export function getDatasetDetail(id) {
  return request({
    url: `/llmops-service/api/dataset/${id}`,
    method: 'get'
  })
}

export function createDataset(data) {
  return request({
    url: '/llmops-service/api/dataset',
    method: 'post',
    data
  })
}

export function updateDataset(data) {
  return request({
    url: '/llmops-service/api/dataset',
    method: 'put',
    data
  })
}

export function deleteDataset(id) {
  return request({
    url: `/llmops-service/api/dataset/${id}`,
    method: 'delete'
  })
}

// 初始化分片上传
export function initDatasetMultipartUpload(params, headers) {
  const config = {
    url: '/llmops-file/api/dataset/upload/init',
    method: 'post',
    params
  }
  if (headers) config.headers = headers
  return request(config)
}

// 获取分片预签名地址
export function getDatasetUploadPresignedUrl(params) {
  return request({
    url: '/llmops-file/api/dataset/upload/presigned-url',
    method: 'get',
    params
  })
}

// 完成分片上传（合并）
export function completeDatasetMultipartUpload(data) {
  return request({
    url: '/llmops-file/api/dataset/upload/complete',
    method: 'post',
    data
  })
}

// 后端可能返回相对路径，统一补齐为当前域名下的 /minio 代理地址
function resolveMinioUrl(url) {
  if (!url) return url

  let path = url.replace(/^(?:https?:)?\/\/[^/]+/i, '')
  if (!path.startsWith('/')) path = `/${path}`
  const minioPath = path === '/minio' || path.startsWith('/minio/') ? path : `/minio${path}`
  return `${window.location.origin}${minioPath}`
}

// 上传分片到 S3 (直接调用预签名 URL)
export function uploadDatasetPartToS3(url, blob) {
  return axios({
    url: resolveMinioUrl(url),
    method: 'put',
    data: blob,
    headers: {
      'Content-Type': 'application/octet-stream'
    },
    transformRequest: [(data) => data]
  })
}

// 上传图标
export function uploadIcon(formData, projectName) {
  return request({
    url: '/llmops-file/api/icon/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data', 'X-Project-Name': projectName || '' }
  })
}

// 记录分片上传完成
export function notifyDatasetPartComplete(data) {
  return request({
    url: '/llmops-file/api/dataset/upload/part-complete',
    method: 'post',
    data
  })
}
