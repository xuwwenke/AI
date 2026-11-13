package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.dto.AiExperimentRequestDTO;
import com.eversec.llmops.service.dto.AiExperimentStatsDTO;
import com.eversec.llmops.service.entity.AiExperiment;
import com.eversec.framework.core.data.QueryResult;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-13
 */
public interface AiExperimentService extends IService<AiExperiment> {

    List<AiExperiment> getExperimentsByProjectId(Integer projectId);

    AiExperiment createExperiment(AiExperimentRequestDTO dto, Integer projectId);

    AiExperiment updateExperiment(AiExperimentRequestDTO dto);

    AiExperimentStatsDTO getStatsByProjectId(Integer projectId);

    Integer getRunningCountByProjectId(Integer projectId);

    Integer getTotalCountByProjectId(Integer projectId);

    QueryResult<AiExperiment> getExperimentsByProjectIdWithPage(Integer projectId, Integer pageNum, Integer pageSize, Integer status, String modelName, String creator);

    List<com.eversec.llmops.service.dto.AiExperimentSimpleDTO> getExperimentListByProjectId(Integer projectId);

    /**
     * 开始实验：状态从 0(未开始) 或 4(已暂停) 变为 1(运行中)
     */
    boolean startExperiment(Integer id);

    /**
     * 暂停实验：状态从 0(未开始) 或 1(运行中) 变为 4(已暂停)
     */
    boolean pauseExperiment(Integer id);

    /**
     * 结束实验：状态从 0(未开始)、1(运行中) 或 4(已暂停) 变为 2(已完成)
     */
    boolean finishExperiment(Integer id);

    /**
     * 失败实验：状态从 0(未开始)、1(运行中) 或 4(已暂停) 变为 3(失败)
     */
    boolean failExperiment(Integer id);
}
