package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.AiExperimentRequestDTO;
import com.eversec.llmops.service.dto.AiExperimentSimpleDTO;
import com.eversec.llmops.service.dto.AiExperimentStatsDTO;
import com.eversec.llmops.service.entity.AiExperiment;
import com.eversec.llmops.service.mapper.AiExperimentMapper;
import com.eversec.llmops.service.service.AiExperimentService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
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
public class AiExperimentServiceImpl extends ServiceImpl<AiExperimentMapper, AiExperiment> implements AiExperimentService {

    @Override
    public List<AiExperiment> getExperimentsByProjectId(Integer projectId) {
        if (projectId == null) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<AiExperiment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiExperiment::getProjectId, projectId);
        wrapper.orderByDesc(AiExperiment::getId);
        return list(wrapper);
    }

    @Override
    public boolean save(AiExperiment entity) {
        entity.setCreatedOn(LocalDateTime.now());
        entity.setChangedOn(LocalDateTime.now());
        return super.save(entity);
    }

    @Override
    public boolean updateById(AiExperiment entity) {
        entity.setChangedOn(LocalDateTime.now());
        return super.updateById(entity);
    }

    @Override
    public AiExperiment createExperiment(AiExperimentRequestDTO dto, Integer projectId) {
        AiExperiment experiment = new AiExperiment();
        experiment.setName(dto.getName());
        experiment.setModelName(dto.getModelName());
        experiment.setDesc(dto.getDesc());
        experiment.setConfig(dto.getConfig());
        experiment.setProjectId(projectId);
        experiment.setStatus(0);
        experiment.setCreatedOn(LocalDateTime.now());
        experiment.setChangedOn(LocalDateTime.now());
        save(experiment);
        return experiment;
    }

    @Override
    public AiExperiment updateExperiment(AiExperimentRequestDTO dto) {
        AiExperiment experiment = getById(dto.getId());
        if (experiment != null) {
            experiment.setName(dto.getName());
            experiment.setModelName(dto.getModelName());
            experiment.setDesc(dto.getDesc());
            experiment.setConfig(dto.getConfig());
            experiment.setChangedOn(LocalDateTime.now());
            updateById(experiment);
        }
        return experiment;
    }

    @Override
    public AiExperimentStatsDTO getStatsByProjectId(Integer projectId) {
        AiExperimentStatsDTO stats = new AiExperimentStatsDTO();
        
        LambdaQueryWrapper<AiExperiment> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(AiExperiment::getProjectId, projectId);
        
        stats.setTotalCount((int) count(baseWrapper));
        
        LocalDateTime weekStart = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.now().with(LocalTime.MAX);
        LambdaQueryWrapper<AiExperiment> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.eq(AiExperiment::getProjectId, projectId)
                .ge(AiExperiment::getCreatedOn, weekStart)
                .le(AiExperiment::getCreatedOn, weekEnd);
        stats.setWeekNewCount((int) count(weekWrapper));
        
        LambdaQueryWrapper<AiExperiment> runningWrapper = new LambdaQueryWrapper<>();
        runningWrapper.eq(AiExperiment::getProjectId, projectId)
                .eq(AiExperiment::getStatus, 1);
        stats.setRunningCount((int) count(runningWrapper));
        
        LambdaQueryWrapper<AiExperiment> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(AiExperiment::getProjectId, projectId)
                .eq(AiExperiment::getStatus, 2);
        stats.setCompletedCount((int) count(completedWrapper));
        
        LambdaQueryWrapper<AiExperiment> failedWrapper = new LambdaQueryWrapper<>();
        failedWrapper.eq(AiExperiment::getProjectId, projectId)
                .eq(AiExperiment::getStatus, 3);
        stats.setFailedCount((int) count(failedWrapper));
        
        return stats;
    }

    @Override
    public Integer getRunningCountByProjectId(Integer projectId) {
        if (projectId == null) {
            return 0;
        }
        LambdaQueryWrapper<AiExperiment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiExperiment::getProjectId, projectId)
                .eq(AiExperiment::getStatus, 1);
        return (int) count(wrapper);
    }

    @Override
    public Integer getTotalCountByProjectId(Integer projectId) {
        return baseMapper.customGetTotalCount(projectId, null, null, null);
    }

    @Override
    public QueryResult<AiExperiment> getExperimentsByProjectIdWithPage(Integer projectId, Integer pageNum, Integer pageSize, Integer status, String modelName, String creator) {
        if (projectId == null) {
            QueryResult<AiExperiment> queryResult = new QueryResult<>();
            queryResult.setResultData(java.util.Collections.emptyList());
            queryResult.setTotalRecord(0);
            return queryResult;
        }
        int offset = (pageNum - 1) * pageSize;
        List<AiExperiment> experimentList = baseMapper.customGetPageList(offset, pageSize, projectId, status, modelName, creator);
        int totalCount = baseMapper.customGetTotalCount(projectId, status, modelName, creator);
        
        QueryResult<AiExperiment> queryResult = new QueryResult<>();
        queryResult.setResultData(experimentList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public List<AiExperimentSimpleDTO> getExperimentListByProjectId(Integer projectId) {
        if (projectId == null) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<AiExperiment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiExperiment::getProjectId, projectId);
        wrapper.select(AiExperiment::getId, AiExperiment::getName);
        wrapper.orderByDesc(AiExperiment::getId);
        List<AiExperiment> experiments = list(wrapper);
        
        return experiments.stream()
                .map(e -> {
                    AiExperimentSimpleDTO dto = new AiExperimentSimpleDTO();
                    dto.setId(e.getId());
                    dto.setName(e.getName());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean startExperiment(Integer id) {
        return baseMapper.startExperiment(id) > 0;
    }

    @Override
    public boolean pauseExperiment(Integer id) {
        return baseMapper.pauseExperiment(id) > 0;
    }

    @Override
    public boolean finishExperiment(Integer id) {
        return baseMapper.finishExperiment(id) > 0;
    }

    @Override
    public boolean failExperiment(Integer id) {
        return baseMapper.failExperiment(id) > 0;
    }
}
