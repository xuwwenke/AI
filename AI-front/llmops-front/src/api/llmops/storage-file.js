import request from '@/utils/httpRequest'
import axios from 'axios'

// 文件分片大小：5MB
export const PART_SIZE = 5 * 1024 * 1024

// ProjectFileController 所有接口统一前缀
const BASE = '/llmops-file/api/project-file'

// 后端可能返回相对路径，统一补齐为当前域名下的 /minio 代理地址
function resolveMinioUrl(url) {
  if (!url) return url

  let path = url.replace(/^(?:https?:)?\/\/[^/]+/i, '')
  if (!path.startsWith('/')) path = `/${path}`
  const minioPath = path === '/minio' || path.startsWith('/minio/') ? path : `/minio${path}`
  return `${window.location.origin}${minioPath}`
}

// 上传分片到 S3/MinIO（直接调用预签名 URL）
export function uploadProjectFilePartToS3(url, blob) {
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

// 0. 直接上传小文件
export function uploadProjectSmallFile(data, headers) {
  return request({
    url: `${BASE}/upload/small`,
    method: 'post',
    data,
    headers
  })
}

// 1. 初始化分片上传
export function initProjectFileUpload(params, headers) {
  return request({
    url: `${BASE}/upload/init`,
    method: 'post',
    params,
    headers
  })
}

// 2. 获取分片预签名上传 URL
export function getProjectFilePresignedUrl(params) {
  return request({
    url: `${BASE}/upload/presigned-url`,
    method: 'get',
    params
  })
}

// 3. 完成分片上传（合并）
export function completeProjectFileUpload(data) {
  return request({
    url: `${BASE}/upload/complete`,
    method: 'post',
    data
  })
}

// 4. 记录分片上传完成（业务台账）
export function notifyProjectFilePartComplete(data) {
  return request({
    url: `${BASE}/upload/part-complete`,
    method: 'post',
    data
  })
}

// 5. 中止分片上传
export function abortProjectFileUpload(params) {
  return request({
    url: `${BASE}/upload/abort`,
    method: 'delete',
    params
  })
}

// 6. 获取下载预签名 URL
export function getProjectFileDownloadUrl(params) {
  return request({
    url: `${BASE}/download/presigned-url`,
    method: 'get',
    params
  })
}
