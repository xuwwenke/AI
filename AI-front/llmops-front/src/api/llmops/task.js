import request from '@/utils/httpRequest'

export function searchTasks(pipelineId, params) {
  return request({
    url: `/llmops-service/api/task/search/${pipelineId}`,
    method: 'get',
    params
  })
}

export function getTaskDetail(id) {
  return request({
    url: `/llmops-service/api/task/${id}`,
    method: 'get'
  })
}

export function createTask(data) {
  return request({
    url: '/llmops-service/api/task',
    method: 'post',
    data
  })
}

export function updateTask(data) {
  return request({
    url: '/llmops-service/api/task',
    method: 'put',
    data
  })
}

export function deleteTask(id) {
  return request({
    url: `/llmops-service/api/task/${id}`,
    method: 'delete'
  })
}
