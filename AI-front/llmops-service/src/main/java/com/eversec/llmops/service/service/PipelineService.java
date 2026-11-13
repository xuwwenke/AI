package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.entity.Pipeline;
import com.eversec.framework.core.data.QueryResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
public interface PipelineService extends IService<Pipeline> {
    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param id 管道ID
     * @param name 名称
     * @param describe 描述
     * @param namespace 项目名称
     * @param creator 创建者
     * @param aiExperimentId 实验ID
     * @return QueryResult
     */
    QueryResult<com.eversec.llmops.service.dto.PipelineDTO> customGetPageList(Integer pageNum, Integer pageSize, Integer id, String name, String describe, String namespace, String creator, Integer aiExperimentId);

    /**
     * 根据ID获取详情（包含关联信息）
     * @param id 管道ID
     * @return PipelineDTO
     */
    com.eversec.llmops.service.dto.PipelineDTO getPipelineDetailById(Integer id);

    /**
     * 补全PipelineRequestDTO中空字段，从数据库查询已有数据
     * @param dto 请求DTO
     */
    void fillEmptyFields(com.eversec.llmops.service.dto.PipelineRequestDTO dto);
}
