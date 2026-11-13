package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.PipelineRequestDTO;
import com.eversec.llmops.service.entity.Pipeline;
import com.eversec.llmops.service.mapper.PipelineMapper;
import com.eversec.llmops.service.service.PipelineService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
@Service
public class PipelineServiceImpl extends ServiceImpl<PipelineMapper, Pipeline> implements PipelineService {

    @Override
    public QueryResult<com.eversec.llmops.service.dto.PipelineDTO> customGetPageList(Integer pageNum, Integer pageSize, Integer id, String name, String describe, String namespace, String creator, Integer aiExperimentId) {
        int offset = (pageNum - 1) * pageSize;
        List<com.eversec.llmops.service.dto.PipelineDTO> pipelineList = baseMapper.customGetPageList(offset, pageSize, id, name, describe, namespace, creator, aiExperimentId);
        int totalCount = baseMapper.customGetTotalCount(id, name, describe, namespace, creator, aiExperimentId);
        QueryResult<com.eversec.llmops.service.dto.PipelineDTO> queryResult = new QueryResult<>();
        queryResult.setResultData(pipelineList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public com.eversec.llmops.service.dto.PipelineDTO getPipelineDetailById(Integer id) {
        return baseMapper.getPipelineDetailById(id);
    }

    @Override
    public void fillEmptyFields(PipelineRequestDTO dto) {
        Pipeline existingPipeline = getById(dto.getId());
        if (existingPipeline == null) {
            return;
        }

        if (dto.getName() == null || dto.getName().isEmpty()) {
            dto.setName(existingPipeline.getName());
        }
        if (dto.getDescribe() == null || dto.getDescribe().isEmpty()) {
            dto.setDescribe(existingPipeline.getDescribe());
        }
        if (dto.getSchedule_type() == null || dto.getSchedule_type().isEmpty()) {
            dto.setSchedule_type(existingPipeline.getScheduleType());
        }
        if (dto.getCron_time() == null || dto.getCron_time().isEmpty()) {
            dto.setCron_time(existingPipeline.getCronTime());
        }
        if (dto.getDepends_on_past() == null) {
            dto.setDepends_on_past(existingPipeline.getDependsOnPast());
        }
        if (dto.getMax_active_runs() == null) {
            dto.setMax_active_runs(existingPipeline.getMaxActiveRuns());
        }
        if (dto.getExpired_limit() == null) {
            dto.setExpired_limit(existingPipeline.getExpiredLimit());
        }
        if (dto.getParallelism() == null) {
            dto.setParallelism(existingPipeline.getParallelism());
        }
        if (dto.getDag_json() == null || dto.getDag_json().isEmpty()) {
            dto.setDag_json(existingPipeline.getDagJson());
        }
        if (dto.getGlobal_env() == null || dto.getGlobal_env().isEmpty()) {
            dto.setGlobal_env(existingPipeline.getGlobalEnv());
        }
        if (dto.getAlert_status() == null || dto.getAlert_status().isEmpty()) {
            dto.setAlert_status(existingPipeline.getAlertStatus());
        }
        if (dto.getAlert_user() == null || dto.getAlert_user().isEmpty()) {
            dto.setAlert_user(existingPipeline.getAlertUser());
        }
        if (dto.getExpand() == null || dto.getExpand().isEmpty()) {
            dto.setExpand(existingPipeline.getExpand());
        }
        if (dto.getParameter() == null || dto.getParameter().isEmpty()) {
            dto.setParameter(existingPipeline.getParameter());
        }
        if (dto.getCronjob_start_time() == null || dto.getCronjob_start_time().isEmpty()) {
            dto.setCronjob_start_time(existingPipeline.getCronjobStartTime());
        }
    }
}
