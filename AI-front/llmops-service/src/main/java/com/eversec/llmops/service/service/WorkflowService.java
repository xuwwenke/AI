package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.dto.WorkflowStatsDTO;
import com.eversec.llmops.service.entity.Workflow;
import com.eversec.framework.core.data.QueryResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-21
 */
public interface WorkflowService extends IService<Workflow> {

    QueryResult<Workflow> getPageList(Integer pageNum, Integer pageSize, String namespace, String status, String label, String name, String username);

    WorkflowStatsDTO getStatsByNamespace(String namespace);

    Long getRunningCount(String namespace);
}
