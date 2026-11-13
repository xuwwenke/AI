package com.eversec.llmops.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.dto.TaskDTO;
import com.eversec.llmops.service.dto.TaskDetailDTO;
import com.eversec.llmops.service.entity.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-13
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    List<TaskDTO> customGetPageList(Integer offset, Integer limit);

    Integer customGetTotalCount();

    List<Task> getTasksByPipelineId(Integer offset, Integer limit, Integer pipelineId);

    Integer getTasksCountByPipelineId(Integer pipelineId);

    List<TaskDetailDTO> getTaskDetailsByPipelineId(Integer offset, Integer limit, Integer pipelineId);
}
