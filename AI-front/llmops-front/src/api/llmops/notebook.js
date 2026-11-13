import request from '@/utils/httpRequest'

export function getNotebookList(params) {
  return request({
    url: '/llmops-service/api/notebook/page',
    method: 'get',
    params
  })
}

export function getNotebookDetail(id) {
  return request({
    url: `/llmops-service/api/notebook/${id}`,
    method: 'get'
  })
}

export function createNotebook(data) {
  return request({
    url: '/llmops-service/api/notebook',
    method: 'post',
    data
  })
}

export function updateNotebook(data) {
  return request({
    url: '/llmops-service/api/notebook',
    method: 'put',
    data
  })
}

export function deleteNotebook(id) {
  return request({
    url: `/llmops-service/api/notebook/${id}`,
    method: 'delete'
  })
}
//保存
export function saveNotebook(id) {
  return request({
    url: `/llmops-service/api/notebook/save/${id}`,
    method: 'get'
  })
}
//重置
export function resetNotebook(id) {
  return request({
    url: `/llmops-service/api/notebook/reset/${id}`,
    method: 'get'
  })
}
//续期
export function renewNotebook(id) {
  return request({
    url: `/llmops-service/api/notebook/renew/${id}`,
    method: 'get'
  })
}

//停止
export function stopNotebook(id) {
  return request({
    url: `/llmops-service/api/notebook/stop/${id}`,
    method: 'get'
  })
}

//镜像下拉数据
export function getImagesOpt() {
  return request({
    url: `/llmops-service/api/images/names`,
    method: 'get'
  })
}

export function getIdeServiceTask() {
  return request({
    url: '/llmops-service/api/ide-service-task',
    method: 'get'
  })
}

