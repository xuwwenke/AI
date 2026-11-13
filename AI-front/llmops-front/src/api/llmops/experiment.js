import request from '@/utils/httpRequest'

export function getExperimentList(params) {
  return request({
    url: '/llmops-service/api/ai-experiment/page',
    method: 'get',
    params
  })
}

export function getExperimentDetail(id) {
  return request({
    url: `/llmops-service/api/ai-experiment/${id}`,
    method: 'get'
  })
}

export function createExperiment(data) {
  return request({
    url: '/llmops-service/api/ai-experiment',
    method: 'post',
    data
  })
}

export function updateExperiment(data) {
  return request({
    url: '/llmops-service/api/ai-experiment',
    method: 'put',
    data
  })
}

export function deleteExperiment(id) {
  return request({
    url: `/llmops-service/api/ai-experiment/${id}`,
    method: 'delete'
  })
}


export function getExperimentStatistics() {
  return request({
    url: '/llmops-service/api/ai-experiment/stats',
    method: 'get'
  })
}
export function startExperiment(id) {
  return request({
    url: `/llmops-service/api/ai-experiment/${id}/start`,
    method: 'put'
  })
}

export function pauseExperiment(id) {
  return request({
    url: `/llmops-service/api/ai-experiment/${id}/pause`,
    method: 'put'
  })
}

export function finishExperiment(id) {
  return request({
    url: `/llmops-service/api/ai-experiment/${id}/finish`,
    method: 'put'
  })
}

export function failExperiment(id) {
  return request({
    url: `/llmops-service/api/ai-experiment/${id}/fail`,
    method: 'put'
  })
}

//获取该项目下所有的实验列表
export function getExperimentOrchestrationList(params) {
  return request({
    url: '/llmops-service/api/ai-experiment/list-by-project',
    method: 'get',
    params
  })
}
