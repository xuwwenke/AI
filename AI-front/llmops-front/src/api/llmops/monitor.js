import request from '@/utils/httpRequest'

export function getInferenceMemory(id) {
    return request({
        url: `/llmops-service/api/monitor/inference/${id}/memory`,
        method: 'get'
    })
}

export function getInferenceCpu(id) {
    return request({
        url: `/llmops-service/api/monitor/inference/${id}/cpu`,
        method: 'get'
    })
}

// 任务监控
export function getTaskMonitor(id) {
    return request({
        url: `/llmops-service/api/monitor/task/${id}`,
        method: 'get',
    })
}

// Pipeline 监控
export function getPipelineMonitor(id) {
    return request({
        url: `/llmops-service/api/monitor/pipeline/${id}`,
        method: 'get',
    })
}
