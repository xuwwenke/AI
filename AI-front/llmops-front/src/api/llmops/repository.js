import request from '@/utils/httpRequest'

export function getRepositoryList(params) {
  return request({
    url: '/llmops-service/api/repository/page',
    method: 'get',
    params
  })
}

export function getRepositoryDetail(id) {
  return request({
    url: `/llmops-service/api/repository/${id}`,
    method: 'get'
  })
}

export function createRepository(data) {
  return request({
    url: '/llmops-service/api/repository',
    method: 'post',
    data
  })
}

export function updateRepository(data) {
  return request({
    url: '/llmops-service/api/repository',
    method: 'put',
    data
  })
}

export function deleteRepository(id) {
  return request({
    url: `/llmops-service/api/repository/${id}`,
    method: 'delete'
  })
}

export function getRepositoryNames() {
  return request({
    url: '/llmops-service/api/repository/names',
    method: 'get'
  })
}
