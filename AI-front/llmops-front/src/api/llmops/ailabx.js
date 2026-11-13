import request from '@/utils/httpRequest'

export function getCategories() {
  return request({
    url: '/llmops/ailabx/categories',
    method: 'get'
  })
}

export function getModelApps() {
  return request({
    url: '/llmops/ailabx/modelApps',
    method: 'get'
  })
}

export function getLongxdApps() {
  return request({
    url: '/llmops/ailabx/longxdApps',
    method: 'get'
  })
}

export function getLlmApps() {
  return request({
    url: '/llmops/ailabx/llmApps',
    method: 'get'
  })
}

export function getPipelineProgress() {
  return request({
    url: '/llmops-service/api/screen/pipeline-progress',
    method: 'get'
  })
}
