import request from '@/utils/httpRequest'


export function getDockerList(params) {
    return request({
        url: '/llmops-service/api/docker/page',
        method: 'get',
        params
    })
}

export function getDockerDetail(id) {
    return request({
        url: `/llmops-service/api/docker/${id}`,
        method: 'get'
    })
}

export function createDocker(data) {
    return request({
        url: '/llmops-service/api/docker',
        method: 'post',
        data
    })
}

export function updateDocker(data) {
    return request({
        url: '/llmops-service/api/docker',
        method: 'put',
        data
    })
}

export function deleteDocker(id) {
    return request({
        url: `/llmops-service/api/docker/${id}`,
        method: 'delete'
    })
}

export function deleteDockerPod(id) {
    return request({
        url: `/llmops-service/api/docker/deletePod/${id}`,
        method: 'delete'
    })
}
export function saveDocker(id) {
    return request({
        url: `/llmops-service/api/docker/save/${id}`,
        method: 'post'
    })
}

export function debugDocker(id) {
    return request({
        url: `/llmops-service/api/docker/debug/${id}`,
        method: 'post'
    })
}