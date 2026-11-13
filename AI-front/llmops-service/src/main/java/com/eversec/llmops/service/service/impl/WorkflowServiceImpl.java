package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.service.dto.WorkflowStatsDTO;
import com.eversec.llmops.service.entity.Workflow;
import com.eversec.llmops.service.mapper.WorkflowMapper;
import com.eversec.llmops.service.service.WorkflowService;
import com.eversec.framework.core.data.QueryResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-21
 */
@Service
public class WorkflowServiceImpl extends ServiceImpl<WorkflowMapper, Workflow> implements WorkflowService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public QueryResult<Workflow> getPageList(Integer pageNum, Integer pageSize, String namespace, String status, String label, String name, String username) {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", (pageNum - 1) * pageSize);
        params.put("pageSize", pageSize);
        params.put("namespace", namespace);
        params.put("status", status);
        params.put("label", label);
        params.put("name", name);
        params.put("username", username);

        List<Workflow> resultData = baseMapper.customGetPageList(params);
        Integer totalRecord = baseMapper.customGetTotalCount(params);

        for (Workflow workflow : resultData) {
            try {
                if (workflow.getStatusMore() != null && !workflow.getStatusMore().isEmpty()) {
                    JsonNode statusMoreNode = objectMapper.readTree(workflow.getStatusMore());
                    if (statusMoreNode.has("phase")) {
                        workflow.setPhase(statusMoreNode.get("phase").asText());
                    } else {
                        workflow.setPhase("unknown");
                    }

                    if (statusMoreNode.has("startedAt") && workflow.getChangeTime() != null && !workflow.getChangeTime().isEmpty()) {
                        String startedAtStr = statusMoreNode.get("startedAt").asText();
                        Instant startedAt = Instant.parse(startedAtStr);
                        Instant changeTime = Instant.parse(workflow.getChangeTime());
                        Duration duration = Duration.between(startedAt, changeTime);
                        double hours = duration.toMillis() / (1000.0 * 60 * 60);
                        workflow.setRunningHours(hours);
                    }
                } else {
                    workflow.setPhase("unknown");
                }
            } catch (Exception e) {
                workflow.setPhase("unknown");
            }
            if (workflow.getRunningHours()==null) workflow.setRunningHours(0d);
        }

        QueryResult<Workflow> queryResult = new QueryResult<>();
        queryResult.setResultData(resultData);
        queryResult.setTotalRecord(totalRecord);
        return queryResult;
    }

    @Override
    public WorkflowStatsDTO getStatsByNamespace(String namespace) {
        LocalDateTime weekStart = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.now().with(LocalTime.MAX);
        Map<String, Object> params = new HashMap<>();
        params.put("namespace", namespace);
        params.put("weekStart", weekStart);
        params.put("weekEnd", weekEnd);
        return baseMapper.getStatsByNamespace(params);
    }

    @Override
    public Long getRunningCount(String namespace) {
        LambdaQueryWrapper<Workflow> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Workflow::getNamespace,namespace);
        lambdaQueryWrapper.eq(Workflow::getStatus,"Running");
        return  this.count(lambdaQueryWrapper);
    }

}
