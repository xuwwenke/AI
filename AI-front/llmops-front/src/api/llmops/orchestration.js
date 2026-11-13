import request from '@/utils/httpRequest'


export function getPipelineList(params) {
    return request({
        url: '/llmops-service/api/pipeline/page',
        method: 'get',
        params
    })
}

export function getPipelineDetail(id) {
    return request({
        url: `/llmops-service/api/pipeline/${id}`,
        method: 'get'
    }).then(normalizePipelineDetail)
}

function normalizePipelineDetail(res) {
    const data = res.result
    if (!data) return data
    return {
        ...data,
        aiExperimentId: data.ai_experiment_id !== undefined ? data.ai_experiment_id : data.aiExperimentId,
        aiExperimentName: data.ai_experiment_name !== undefined ? data.ai_experiment_name : data.aiExperimentName,
        alertStatus: data.alert_status !== undefined ? data.alert_status : data.alertStatus,
        alertUser: data.alert_user !== undefined ? data.alert_user : data.alertUser,
        changedBy: data.changed_by !== undefined ? data.changed_by.username : data.changedBy,
        changedOn: data.changed_on !== undefined ? data.changed_on : data.changedOn,
        createdBy: data.created_by !== undefined ? data.created_by.username : data.createdBy,
        createdOn: data.created_on !== undefined ? data.created_on : data.createdOn,
        cronTime: data.cron_time !== undefined ? data.cron_time : data.cronTime,
        cronjobStartTime: data.cronjob_start_time !== undefined ? data.cronjob_start_time : data.cronjobStartTime,
        dagJson: data.dag_json !== undefined ? data.dag_json : data.dagJson,
        dependsOnPast: data.depends_on_past !== undefined ? data.depends_on_past : data.dependsOnPast,
        expiredLimit: data.expired_limit !== undefined ? data.expired_limit : data.expiredLimit,
        globalEnv: data.global_env !== undefined ? data.global_env : data.globalEnv,
        imagePullPolicy: data.image_pull_policy !== undefined ? data.image_pull_policy : data.imagePullPolicy,
        maxActiveRuns: data.max_active_runs !== undefined ? data.max_active_runs : data.maxActiveRuns,
        nodeSelector: data.node_selector !== undefined ? data.node_selector : data.nodeSelector,
        pipelineArgoId: data.pipeline_argo_id !== undefined ? data.pipeline_argo_id : data.pipelineArgoId,
        pipelineFile: data.pipeline_file !== undefined ? data.pipeline_file : data.pipelineFile,
        runId: data.run_id !== undefined ? data.run_id : data.runId,
        scheduleType: data.schedule_type !== undefined ? data.schedule_type : data.scheduleType,
        versionId: data.version_id !== undefined ? data.version_id : data.versionId,
        workflowYaml: data.workflow_yaml !== undefined ? data.workflow_yaml : data.workflowYaml
    }
}

// 根据实验ID分页查询编排列表
export function getPipelinePageByExperimentId(params) {
    return request({
        url: '/llmops-service/api/pipeline/page-by-experiment',
        method: 'get',
        params
    })
}

export function createPipeline(data) {
    return request({
        url: '/llmops-service/api/pipeline',
        method: 'post',
        data
    })
}

export function updatePipeline(data) {
    return request({
        url: '/llmops-service/api/pipeline',
        method: 'put',
        data
    })
}

export function deletePipeline(id) {
    return request({
        url: `/llmops-service/api/pipeline/${id}`,
        method: 'delete'
    })
}

export function copyPipeline(data) {
    return request({
        url: `/llmops-service/api/pipeline/copy`,
        method: 'post',
        data
    })
}

export function mulDeletePipeline(data) {
    return request({
        url: '/llmops-service/api/pipeline/muldelete',
        method: 'post',
        data
    })
}

export function mulCopyPipeline(data) {
    return request({
        url: '/llmops-service/api/pipeline/mulcopy',
        method: 'post',
        data
    })
}

//查询任务列表
export function getTaskList(params) {
    return request({
        url: '/llmops-service/api/task/page',
        method: 'get',
        params
    })
}
// 运行指定任务流
export function runPipeline(id) {
    return request({
        url: `/llmops-service/api/pipeline/run_pipeline/${id}`,
        method: 'put'
    })
}

// --- 任务相关接口 ---

// 运行任务
export function runTask(taskId) {
    return request({
        url: `/llmops-service/api/task/run/${taskId}`,
        method: 'post'
    })
}

// 获取任务日志
export function getTaskLog(taskId) {
    return request({
        url: `/llmops-service/api/task/log/${taskId}`,
        method: 'post'
    })
}

// 调试任务
export function debugTask(id) {
    return request({
        url: `/llmops-service/api/task/debug/${id}`,
        method: 'post'
    })
}

// 清除任务
export function clearTask(taskId) {
    return request({
        url: `/llmops-service/api/task/clear/${taskId}`,
        method: 'post'
    })
}

// 获取所有任务
export function getAllTasks() {
    return request({
        url: '/llmops-service/api/task',
        method: 'get'
    })
}

// 获取单个任务
export function getTask(id) {
    return request({
        url: `/llmops-service/api/task/${id}`,
        method: 'get'
    })
}

// 分页获取任务
export function getTaskPage(params) {
    return request({
        url: '/llmops-service/api/task/page',
        method: 'get',
        params
    })
}

// 创建任务
export function createTask(data) {
    return request({
        url: '/llmops-service/api/task',
        method: 'post',
        data
    })
}

// 更新任务
export function updateTask(id, data) {
    return request({
        url: `/llmops-service/api/task/${id}`,
        method: 'put',
        data
    })
}

// 删除任务
export function deleteTask(taskId) {
    return request({
        url: `/llmops-service/api/task/${taskId}`,
        method: 'delete'
    })
}

// 恢复指定任务流
export function resumePipeline(id) {
    return request({
        url: `/llmops-service/api/pipeline/resume_pipeline/${id}`,
        method: 'put'
    })
}
//暂停指定任务流
export function pausePipeline(id) {
    return request({
        url: `/llmops-service/api/pipeline/pause_pipeline/${id}`,
        method: 'put'
    })
}




