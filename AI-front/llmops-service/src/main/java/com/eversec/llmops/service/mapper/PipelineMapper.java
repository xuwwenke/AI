package com.eversec.llmops.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.entity.Pipeline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
@Mapper
public interface PipelineMapper extends BaseMapper<Pipeline> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param id 管道ID
     * @param name 名称
     * @param describe 描述
     * @param namespace 项目名称
     * @param creator 创建者
     * @param aiExperimentId 实验ID
     * @return 列表
     */
    List<com.eversec.llmops.service.dto.PipelineDTO> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("id") Integer id,
            @Param("name") String name,
            @Param("describe") String describe,
            @Param("namespace") String namespace,
            @Param("creator") String creator,
            @Param("aiExperimentId") Integer aiExperimentId);

    /**
     * 获取总数
     * @param id 管道ID
     * @param name 名称
     * @param describe 描述
     * @param namespace 项目名称
     * @param creator 创建者
     * @param aiExperimentId 实验ID
     * @return 总数
     */
    int customGetTotalCount(
            @Param("id") Integer id,
            @Param("name") String name,
            @Param("describe") String describe,
            @Param("namespace") String namespace,
            @Param("creator") String creator,
            @Param("aiExperimentId") Integer aiExperimentId);

    /**
     * 根据ID获取详情（包含关联信息）
     * @param id 管道ID
     * @return PipelineDTO
     */
    com.eversec.llmops.service.dto.PipelineDTO getPipelineDetailById(@Param("id") Integer id);
}
