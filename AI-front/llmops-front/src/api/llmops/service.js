import request from '@/utils/httpRequest'

export function getServiceList(params) {
  return request({
    url: '/llmops/service/list',
    method: 'get',
    params
  })
}

export function getServiceDetail(id) {
  return request({
    url: `/llmops/service/${id}`,
    method: 'get'
  })
}

export function createService(data) {
  return request({
    url: '/llmops/service',
    method: 'post',
    data
  })
}

export function updateService(data) {
  return request({
    url: '/llmops/service',
    method: 'put',
    data
  })
}

export function deleteService(id) {
  return request({
    url: `/llmops/service/${id}`,
    method: 'delete'
  })
}

export function deployService(id) {
  return request({
    url: `/llmops/service/${id}/deploy`,
    method: 'post'
  })
}

export function stopService(id) {
  return request({
    url: `/llmops/service/${id}/stop`,
    method: 'post'
  })
}
