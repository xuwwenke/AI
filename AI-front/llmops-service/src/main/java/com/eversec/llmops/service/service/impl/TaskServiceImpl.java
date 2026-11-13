package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.TaskDTO;
import com.eversec.llmops.service.dto.TaskDetailDTO;
import com.eversec.llmops.service.entity.Task;
import com.eversec.llmops.service.mapper.TaskMapper;
import com.eversec.llmops.service.service.PodService;
import com.eversec.llmops.service.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-13
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Autowired
    private PodService podService;

    @Override
    public QueryResult<TaskDTO> customGetPageList(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<TaskDTO> taskList = baseMapper.customGetPageList(offset, pageSize);
        int totalCount = baseMapper.customGetTotalCount();

        QueryResult<TaskDTO> queryResult = new QueryResult<>();
        queryResult.setResultData(taskList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public QueryResult<Task> getTasksByPipelineId(Integer pageNum, Integer pageSize, Integer pipelineId) {
        int offset = (pageNum - 1) * pageSize;
        List<Task> taskList = baseMapper.getTasksByPipelineId(offset, pageSize, pipelineId);
        int totalCount = baseMapper.getTasksCountByPipelineId(pipelineId);

        QueryResult<Task> queryResult = new QueryResult<>();
        queryResult.setResultData(taskList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public QueryResult<TaskDetailDTO> getTaskDetailsByPipelineId(Integer pageNum, Integer pageSize, Integer pipelineId) {
        int offset = (pageNum - 1) * pageSize;
        List<TaskDetailDTO> taskList = baseMapper.getTaskDetailsByPipelineId(offset, pageSize, pipelineId);
        int totalCount = baseMapper.getTasksCountByPipelineId(pipelineId);

        QueryResult<TaskDetailDTO> queryResult = new QueryResult<>();
        queryResult.setResultData(taskList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public Integer getTaskPodCount(String clusterName, String namespace) {
        if (clusterName == null || namespace == null) {
            return 0;
        }
        List<com.eversec.llmops.service.dto.PodDTO> pods = podService.getTaskPods(clusterName, namespace);
        return pods != null ? pods.size() : 0;
    }
}
