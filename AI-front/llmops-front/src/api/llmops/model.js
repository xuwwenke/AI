import request from '@/utils/httpRequest'
import axios from 'axios'


export function getModelList(params) {
    return request({
        url: '/llmops-service/api/model/page',
        method: 'get',
        params
    })
}

export function getModelDetail(id) {
    return request({
        url: `/llmops-service/api/model/${id}`,
        method: 'get'
    })
}

export function createModel(data) {
    return request({
        url: '/llmops-service/api/model',
        method: 'post',
        data
    })
}

export function updateModel(data) {
    return request({
        url: '/llmops-service/api/model',
        method: 'put',
        data
    })
}

export function deleteModel(id) {
    return request({
        url: `/llmops-service/api/model/${id}`,
        method: 'delete'
    })
}

// 初始化分片上传
export function initMultipartUpload(params, headers) {
    return request({
        url: '/llmops-file/api/model/upload/init',
        method: 'post',
        params,
        headers
    })
}

// 获取分片预签名地址
export function getUploadPresignedUrl(params) {
    return request({
        url: '/llmops-file/api/model/upload/presigned-url',
        method: 'get',
        params
    })
}

// 完成分片上传（合并）
export function completeMultipartUpload(data) {
    return request({
        url: '/llmops-file/api/model/upload/complete',
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
export function uploadPartToS3(url, blob) {
    return axios({
        url: resolveMinioUrl(url),
        method: 'put',
        data: blob,
        headers: {
            'Content-Type': 'application/octet-stream'
        },
        transformRequest: [(data) => data] // 保持原始 Blob 数据
    })
}

// 记录分片上传完成
export function notifyPartComplete(data) {
    return request({
        url: '/llmops-file/api/model/upload/part-complete',
        method: 'post',
        data
    })
}

// 获取模型下载地址
export function getModelDownloadUrl(params) {
    return request({
        url: `/llmops-file/api/model/download/presigned-url`,
        method: 'get',
        params
    }).then(res => {
        if (res && res.url) {
            return { ...res, url: resolveMinioUrl(res.url) }
        }
        return res
    })
}

// 获取模型统计数据
export function getModelStats(params) {
    return request({
        url: '/llmops-service/api/model/stats',
        method: 'get',
        params
    })
}
//模型发布
export function deployModel(id) {
    return request({
        url: `/llmops-service/api/model/deploy/${id}`,
        method: 'post'
    })
}
