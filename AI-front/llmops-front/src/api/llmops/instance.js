import request from '@/utils/httpRequest'
import axios from 'axios'
export function getWorkflowList(params) {
    // return axios.get('http://192.168.224.251:22553/workflow_modelview/api', { params }).then(res => {
    //     const data = (res.data && res.data.result) || []
    //     return {
    //         resultData: data.data,
    //         totalRecord: data.count
    //     }
    // })
    return request({
        url: '/llmops-service/api/workflow/page',
        method: 'get',
        params
    })
}

export function stopWorkflow(id) {
     return request({
        url: `/llmops-service/api/workflow/stop/${id}`,
        method: 'get'
    })
}

export function getWorkflowDetail(id) {
    return request({
        url: `/llmops-service/api/workflow/${id}`,
        method: 'get'
    })
}

export function createWorkflow(data) {
    return request({
        url: '/llmops-service/api/workflow',
        method: 'post',
        data
    })
}

export function updateWorkflow(data) {
    return request({
        url: '/llmops-service/api/workflow',
        method: 'put',
        data
    })
}

export function deleteWorkflow(id) {
    return request({
        url: `/llmops-service/api/workflow/${id}`,
        method: 'delete'
    })
}

// 获取训练实例统计数据
export function getWorkflowStats(params) {
    return request({
        url: '/llmops-service/api/workflow/stats',
        method: 'get',
        params
    })
}

// 获取 pipeline 对应的 DAG 详情
export function getWorkflowDag(cluster, pipelineId) {
    return request({
        url: `/llmops-service/api/workflow/dag/dev/pipeline`,
        method: 'get',
        params: { pipelineId }
    })
}

// 获取节点详情
export function getWorkflowNodeDetail(pipelineArgoId, nodeId) {
    return request({
        url: `/llmops-service/api/workflow/node/detail/dev/pipeline`,
        method: 'get',
        params: { pipelineArgoId, nodeId }
    })
}


