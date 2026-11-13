import request from '@/utils/httpRequest'

export function getImageList(params) {
  return request({
    url: '/llmops-service/api/images',
    method: 'get',
    params
  })
}

export function getImageDetail(id) {
  return request({
    url: `/llmops-service/api/images/${id}`,
    method: 'get'
  })
}

export function createImage(data) {
  return request({
    url: '/llmops-service/api/images',
    method: 'post',
    data
  })
}

export function updateImage(data) {
  return request({
    url: '/llmops-service/api/images',
    method: 'put',
    data
  })
}

export function deleteImage(id) {
  return request({
    url: `/llmops-service/api/images/${id}`,
    method: 'delete'
  })
}

export function buildImage(data) {
  return request({
    url: '/llmops-service/images/build',
    method: 'post',
    data
  })
}

export function getBuildLog(id) {
  return request({
    url: `/llmops-service/images/build/log/${id}`,
    method: 'get'
  })
}

export function getImagesOpt(params) {
  return request({
    url: '/llmops-service/api/images/names',
    method: 'get',
    params
  })
}

export function getImagesNamesToSelect() {
  return request({
    url: `/llmops-service/api/images/namesToSelect`,
    method: 'get',
  })
}


