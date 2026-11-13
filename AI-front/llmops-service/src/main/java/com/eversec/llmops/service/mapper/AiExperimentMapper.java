package com.eversec.llmops.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.entity.AiExperiment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface AiExperimentMapper extends BaseMapper<AiExperiment> {

    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param projectId 项目ID
     * @return 列表
     */
    List<AiExperiment> customGetPageList(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit,
            @Param("projectId") Integer projectId,
            @Param("status") Integer status,
            @Param("modelName") String modelName,
            @Param("creator") String creator);

    /**
     * 获取总数
     * @param projectId 项目ID
     * @param status 状态
     * @param modelName 模型名称
     * @param creator 创建者
     * @return 总数
     */
    int customGetTotalCount(
            @Param("projectId") Integer projectId,
            @Param("status") Integer status,
            @Param("modelName") String modelName,
            @Param("creator") String creator);

    /**
     * 开始实验：状态从 0(未开始) 或 4(已暂停) 变为 1(运行中)
     */
    int startExperiment(@Param("id") Integer id);

    /**
     * 暂停实验：状态从 0(未开始) 或 1(运行中) 变为 4(已暂停)
     */
    int pauseExperiment(@Param("id") Integer id);

    /**
     * 结束实验：状态从 0(未开始)、1(运行中) 或 4(已暂停) 变为 2(已完成)
     */
    int finishExperiment(@Param("id") Integer id);

    /**
     * 失败实验：状态从 0(未开始)、1(运行中) 或 4(已暂停) 变为 3(失败)
     */
    int failExperiment(@Param("id") Integer id);
}
