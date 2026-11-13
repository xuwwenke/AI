import request from '@/utils/httpRequest'
import axios from 'axios'


export function getInferenceList(params) {
    // return axios.get('http://192.168.224.251:22553/inferenceservice_modelview/api', { params }).then(res => {
    //     const data = (res.data && res.data.result) || []
    //     return {
    //         resultData: data.data,
    //         totalRecord: data.count
    //     }
    // })
    return request({
        url: '/llmops-service/api/inferenceservice/page',
        method: 'get',
        params
    })
}

export function getInferenceDetail(id) {
    return request({
        url: `/llmops-service/api/inferenceservice/${id}`,
        method: 'get'
    })
}

export function createInference(data) {
    return request({
        url: '/llmops-service/api/inferenceservice',
        method: 'post',
        data
    })
}

export function updateInference(data) {
    return request({
        url: '/llmops-service/api/inferenceservice',
        method: 'put',
        data
    })
}

export function deleteInference(id) {
    return request({
        url: `/llmops-service/api/inferenceservice/${id}`,
        method: 'delete'
    })
}

//部署推理服务
export function deployInference(id) {
    return request({
        url: `/llmops-service/api/inferenceservice/deploy/prod/${id}`,
        method: 'post',
       
    })
}
//调试推理服务
export function debugInference(id) {
    return request({
        url: `/llmops-service/api/inferenceservice/deploy/debug/${id}`,
        method: 'post',
       
    })
}
//推理服务统计数据
export function getInferenceStats(params) {
    return request({
        url: '/llmops-service/api/inferenceservice/stats',
        method: 'get',
        params
    })
}
//清理推理服务
export function cleanInference(id) {
    return request({
        url: `/llmops-service/api/inferenceservice/clear/${id}`,
        method: 'post',
      
    })
}

// 获取推理服务类型
export function getInferenceServiceTypes() {
    return request({
        url: '/llmops-service/api/inferenceservice/service_types',
        method: 'get'
    })
}
