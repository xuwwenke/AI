package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.TaskDTO;
import com.eversec.llmops.service.dto.TaskDetailDTO;
import com.eversec.llmops.service.entity.Task;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-13
 */
public interface TaskService extends IService<Task> {

    QueryResult<TaskDTO> customGetPageList(Integer pageNum, Integer pageSize);

    QueryResult<Task> getTasksByPipelineId(Integer pageNum, Integer pageSize, Integer pipelineId);

    QueryResult<TaskDetailDTO> getTaskDetailsByPipelineId(Integer pageNum, Integer pageSize, Integer pipelineId);

    /**
     * 通过clusterName和namespace获取pod-type=task的pod数量
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @return pod数量
     */
    Integer getTaskPodCount(String clusterName, String namespace);
}
