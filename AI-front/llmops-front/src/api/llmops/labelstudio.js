﻿import axios from 'axios'

const LABEL_STUDIO_API_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6ODA5Njc1MDg0MywiaWF0IjoxNzg5NTUwODQzLCJqdGkiOiJlN2Y3MjA1MTAxMzc0OTIxYTA4MTczOGFhMzc2MDcyYyIsInVzZXJfaWQiOiIyIn0.7KjSvSA85tbt0uNTSsaMvhjv3tZjfFw1jBOd9Yjv-e8'

const http = axios.create({
  baseURL: '/labelstudio',
  timeout: 30000,
  headers: {
    'Authorization': `Token ${LABEL_STUDIO_API_KEY}`
  }
})

http.interceptors.response.use(
  res => {
    if (res.config.responseType === 'blob') {
      return res
    }
    return res.data
  },
  err => {
    console.error('[LabelStudio API Error]', err.response && err.response.status, err.response && err.response.data || err.message)
    return Promise.reject(err)
  }
)

export function getProjects(params) {
  return http({
    url: '/api/projects/',
    method: 'get',
    params
  })
}

export function getTemplates() {
  return http({
    url: '/api/templates',
    method: 'get'
  })
}

export function getProject(id) {
  return http({
    url: `/api/projects/${id}`,
    method: 'get'
  })
}

export function createProject(data) {
  return http({
    url: '/api/projects/',
    method: 'post',
    data
  })
}

export function updateProject(id, data) {
  return http({
    url: `/api/projects/${id}`,
    method: 'patch',
    data
  })
}

export function deleteProject(id) {
  return http({
    url: `/api/projects/${id}`,
    method: 'delete'
  })
}

// 获取项目已连接的 ML 模型列表
export function getProjectModels(projectId) {
  return http({
    url: `/api/projects/${projectId}/models`,
    method: 'get'
  })
}

// 向项目添加 ML 模型
export function createProjectModel(projectId, data) {
  return http({
    url: `/api/projects/${projectId}/models`,
    method: 'post',
    data
  })
}

// 删除（断开）模型连接
export function deleteProjectModel(modelId) {
  return http({
    url: `/api/models/${modelId}`,
    method: 'delete'
  })
}

// 更新模型连接配置
export function updateProjectModel(modelId, data) {
  return http({
    url: `/api/models/${modelId}`,
    method: 'patch',
    data
  })
}

// 手动触发模型训练
export function trainModel(modelId) {
  return http({
    url: `/api/models/${modelId}/train`,
    method: 'post'
  })
}

// 发送测试预测请求（随机选取一个任务）
export function testModelPredict(modelId) {
  return http({
    url: `/api/models/${modelId}/predict/test`,
    method: 'post',
    params: { random: true }
  })
}

// 获取项目的预测模型版本列表（含数量和最新创建时间）
export function getProjectModelVersions(projectId) {
  return http({
    url: `/api/projects/${projectId}/model-versions`,
    method: 'get',
    params: { extended: true, include_live_models: true }
  })
}

// 删除指定模型版本的所有预测
export function deleteProjectPredictions(projectId, modelVersion) {
  return http({
    url: `/api/projects/${projectId}/model-versions`,
    method: 'delete',
    data: { model_version: modelVersion }
  })
}

// 重置项目缓存
export function resetProjectSummary(projectId) {
  return http({
    url: `/api/projects/${projectId}/summary/reset`,
    method: 'post'
  })
}

// 重置数据管理器所有标签页
export function resetDataManagerViews(projectId) {
  return http({
    url: `/api/dm/views/reset`,
    method: 'delete',
    params: { project: projectId }
  })
}

// 获取项目支持的导出格式列表
export function getProjectExportFormats(projectId) {
  return http({
    url: `/api/projects/${projectId}/export-formats`,
    method: 'get'
  })
}

// 导出项目数据（返回文件流）
export function exportProjectData(projectId, exportType) {
  return http({
    url: `/api/projects/${projectId}/export`,
    method: 'get',
    params: { export_type: exportType, download_resources: false },
    responseType: 'blob',
    headers: { accept: 'application/octet-stream' }
  })
}

export function getProjectTasks(projectId, params) {
  return http({
    url: '/api/tasks',
    method: 'get',
    params: { project: projectId, ...params }
  })
}

export function getProjectViews(projectId) {
  return http({
    url: '/api/dm/views',
    method: 'get',
    params: { project: projectId }
  })
}

export function createProjectView(projectId, data) {
  return http({
    url: '/api/dm/views',
    method: 'post',
    params: { tabID: 0, project: projectId },
    data
  })
}

export function getProjectUsers(projectId) {
  return http({
    url: '/api/users',
    method: 'get',
    params: { project: projectId }
  })
}

// 设置项目标注配置
export function setProjectLabelConfig(projectId, labelConfig) {
  return http({
    url: `/api/projects/${projectId}/label-config`,
    method: 'put',
    data: { label_config: labelConfig }
  })
}

// 校验标注配置
export function validateLabelConfig(projectId, labelConfig) {
  return http({
    url: `/api/projects/${projectId}/validate`,
    method: 'post',
    data: { label_config: labelConfig }
  })
}

// 上传数据集文件（multipart/form-data）
export function uploadDatasetFiles(projectId, files) {
  const formData = new FormData()
  if (Array.isArray(files)) {
    files.forEach(file => formData.append('files', file))
  } else {
    formData.append('files', files)
  }
  return http({
    url: `/api/projects/${projectId}/datasets/upload`,
    method: 'post',
    data: formData
  })
}

// 通过 URL 上传数据集
export function uploadDatasetByUrl(projectId, urls) {
  return http({
    url: `/api/projects/${projectId}/datasets/upload-by-url`,
    method: 'post',
    data: { urls: Array.isArray(urls) ? urls : [urls] }
  })
}

// 生成示例任务
export function generateSampleTask(projectId, labelConfig) {
  return http({
    url: `/api/projects/${projectId}/sample-task`,
    method: 'post',
    data: { label_config: labelConfig }
  })
}

export function getTask(id) {
  return http({
    url: `/api/tasks/${id}`,
    method: 'get'
  })
}

export function getTaskAnnotations(taskId) {
  return http({
    url: `/api/tasks/${taskId}/annotations`,
    method: 'get'
  })
}

export function getTaskPredictions(taskId) {
  return http({
    url: `/api/tasks/${taskId}/predictions`,
    method: 'get'
  })
}

// 触发任务的 AI 标注预测
export function aiAnnotation(taskId, data) {
  return http({
    url: `/api/tasks/${taskId}/ai-annotation`,
    method: 'post',
    data
  })
}

// Label Studio 用户登录（返回 JWT access token）
export function loginLabelStudio(data) {
  return http({
    url: '/api/auth/login',
    method: 'post',
    data,
    headers: { Authorization: '' }
  })
}

// Label Studio 用户注册
export function registerLabelStudio(data) {
  return http({
    url: '/api/auth/register',
    method: 'post',
    data,
    headers: { Authorization: '' }
  })
}

// 以指定用户身份提交标注（Authorization: Bearer <token>）
export function createTaskAnnotation(taskId, data, token) {
  return http({
    url: `/api/tasks/${taskId}/annotations`,
    method: 'post',
    data,
    headers: { Authorization: `Bearer ${token}` }
  })
}

// 删除标注记录
export function deleteAnnotation(annotationId) {
  return http({
    url: `/api/annotations/${annotationId}`,
    method: 'delete'
  })
}

// 删除预测记录
export function deletePrediction(predictionId) {
  return http({
    url: `/api/predictions/${predictionId}`,
    method: 'delete'
  })
}

// 创建 ML 后端（Deployment + Service）
export function createMlBackend(data) {
  return http({
    url: '/api/ml-backends',
    method: 'post',
    data
  })
}

// 删除 ML 后端（Deployment + Service）
export function deleteMlBackend(name, namespace) {
  return http({
    url: `/api/ml-backends/${name}`,
    method: 'delete',
    params: { namespace }
  })
}

// 更新标注记录（以指定用户身份）
export function updateAnnotation(annotationId, data, token) {
  return http({
    url: `/api/annotations/${annotationId}`,
    method: 'put',
    data,
    headers: { Authorization: `Bearer ${token}` }
  })
}

// ---------------------------------------------------------------------------
// data-labeling-service（FastAPI 聚合服务）
// 开发环境走 vue.config.js 中的 /labeling-service 代理，生产环境由 nginx 转发
// ---------------------------------------------------------------------------
const labelingHttp = axios.create({
  baseURL: '/labeling-service',
  timeout: 30000
})

labelingHttp.interceptors.response.use(
  res => res.data,
  err => {
    console.error('[DataLabeling API Error]', err.response && err.response.status, err.response && err.response.data || err.message)
    return Promise.reject(err)
  }
)

// 数据标注首页数据（全局统计 + 项目列表）
export function getLabelStudioDashboard() {
  return labelingHttp({
    url: '/api/labelstudio/dashboard',
    method: 'get'
  })
}
