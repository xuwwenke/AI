import request from '@/utils/httpRequest'

export function getLargeScreenOverview() {
  return request({
    url: '/llmops-service/api/large-screen/overview',
    method: 'get'
  })
}

export function getLargeScreenConnLabels() {
  return request({
    url: '/llmops-service/api/large-screen/conn-labels',
    method: 'get'
  })
}

export function getLargeScreenAnomalies() {
  return request({
    url: '/llmops-service/api/large-screen/anomalies',
    method: 'get'
  })
}

export function getLargeScreenPipelineData() {
  return request({
    url: '/llmops-service/api/large-screen/pipeline-data',
    method: 'get'
  })
}

export function getLargeScreenVersionChart() {
  return request({
    url: '/llmops-service/api/large-screen/charts/version',
    method: 'get'
  })
}

export function getLargeScreenDatasetChart() {
  return request({
    url: '/llmops-service/api/large-screen/charts/dataset',
    method: 'get'
  })
}

export function getLargeScreenTrainingChart() {
  return request({
    url: '/llmops-service/api/large-screen/charts/training',
    method: 'get'
  })
}

export function getLargeScreenInferenceChart() {
  return request({
    url: '/llmops-service/api/large-screen/charts/inference',
    method: 'get'
  })
}

export function getLargeScreenRadarChart() {
  return request({
    url: '/llmops-service/api/large-screen/charts/radar',
    method: 'get'
  })
}

export function getLargeScreenHeatmapChart() {
  return request({
    url: '/llmops-service/api/large-screen/charts/heatmap',
    method: 'get'
  })
}
