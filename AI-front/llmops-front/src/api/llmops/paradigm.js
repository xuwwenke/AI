import request from '@/utils/httpRequest'


export function getJobTemplateList(params) {
    return request({
        url: '/llmops-service/api/job-template/page',
        method: 'get',
        params
    })
}

export function getAllJobTemplateList(params) {
    return request({
        url: '/llmops-service/api/job-template/list',
        method: 'get',
        params
    })
}

export function getJobTemplateDetail(id) {
    return request({
        url: `/llmops-service/api/job-template/${id}`,
        method: 'get'
    })
}

export function createJobTemplate(data) {
    return request({
        url: '/llmops-service/api/job-template',
        method: 'post',
        data
    })
}

export function updateJobTemplate(data) {
    return request({
        url: '/llmops-service/api/job-template',
        method: 'put',
        data
    })
}

export function deleteJobTemplate(id) {
    return request({
        url: `/llmops-service/api/job-template/${id}`,
        method: 'delete'
    })
}

export function copyJobTemplate(data) {
    return request({
        url: `/llmops-service/api/job-template/copy`,
        method: 'post',
        data
    })
}

