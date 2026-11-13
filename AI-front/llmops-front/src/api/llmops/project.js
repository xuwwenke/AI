import request from '@/utils/httpRequest'

export function getProjectList(params) {
  return request({
    url: '/llmops-service/api/project/page-with-manager',
    method: 'get',
    params
  })
}

export function getProjectToSelect() {
  return request({
    url: '/llmops-service/api/project/getCurrentUserProjectManagers',
    method: 'get'
  })
}

export function getProjectDetail(id) {
  return request({
    url: `/llmops-service/api/project/get-with-manager/${id}`,
    method: 'get'
  })
}

export function createProject(data) {
  return request({
    url: '/llmops-service/api/project',
    method: 'post',
    data
  })
}

export function updateProject(data) {
  return request({
    url: '/llmops-service/api/project',
    method: 'put',
    data
  })
}

export function deleteProject(id) {
  return request({
    url: `/llmops-service/api/project/${id}`,
    method: 'delete'
  })
}

//用户-项目关联
export function getUserProjectList(params) {
  return request({
    url: '/llmops-service/llmopsservice/pro-user/list',
    method: 'get',
    params
  })
}

export function getValidUsersList(params) {
  return request({
    url: '/llmops-service/llmopsservice/pro-user/validUsers',
    method: 'get',
    params
  })
}

export function addUserProject(data) {
  return request({
    url: '/llmops-service/llmopsservice/pro-user/batch-add',
    method: 'post',
    data
  })
}

//项目资源概览
export function getProjectResourceOverview(params) {
  return request({
    url: `/llmops-service/api/project/resource-quota`,
    method: 'get',
    params
  })
}

//任务模板类型
export function getJobTemplateType(params) {
  return request({
    url: '/llmops-service/api/project/job-template-type',
    method: 'get',
    params
  })
}
//数据信息概览
export function getProjectDataInfo(params) {
  return request({
    url: '/llmops-service/api/project/dataInfo',
    method: 'get',
    params
  })
}
//首页
export function getProjectIndex(params) {
  return request({
    url: '/llmops-service/api/project/index',
    method: 'get',
    params
  })
}
