import request from '@/utils/httpRequest'

export function getDatasetPreview(datasetId, params) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/preview`,
    method: 'get',
    params
  })
}

export function getDatasetStatistics(datasetId) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/statistics`,
    method: 'get'
  })
}

export function getColumnStatistics(datasetId, columnName) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/column/${columnName}`,
    method: 'get'
  })
}

export function getMissingValueAnalysis(datasetId) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/missing`,
    method: 'get'
  })
}

export function getCorrelationAnalysis(datasetId) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/correlation`,
    method: 'get'
  })
}

export function getDataSample(datasetId, sampleSize = 100) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/sample`,
    method: 'get',
    params: { size: sampleSize }
  })
}

export function exportDataset(datasetId, format = 'csv') {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/export`,
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

export function searchData(datasetId, params) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/search`,
    method: 'post',
    data: params
  })
}

export function filterData(datasetId, filters) {
  return request({
    url: `/llmops/dataexplorer/${datasetId}/filter`,
    method: 'post',
    data: filters
  })
}
