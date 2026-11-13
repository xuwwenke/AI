package com.eversec.llmops.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.dto.WorkflowStatsDTO;
import com.eversec.llmops.service.entity.Workflow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-21
 */
@Mapper
public interface WorkflowMapper extends BaseMapper<Workflow> {

    List<Workflow> customGetPageList(Map<String, Object> params);

    Integer customGetTotalCount(Map<String, Object> params);

    WorkflowStatsDTO getStatsByNamespace(Map<String, Object> params);

}
