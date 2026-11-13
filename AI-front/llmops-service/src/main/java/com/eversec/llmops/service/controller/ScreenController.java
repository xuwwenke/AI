package com.eversec.llmops.service.controller;

import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.dto.PipelineProgressDTO;
import com.eversec.llmops.service.dto.PipelineProgressStageDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/screen")
public class ScreenController {

    @GetMapping("/pipeline-progress")
    @Operation(summary = "获取大屏模型迭代进度")
    public RestResponse<PipelineProgressDTO> getPipelineProgress() {
        return ResponseHolder.build(() -> buildMockPipelineProgress());
    }

    private PipelineProgressDTO buildMockPipelineProgress() {
        List<PipelineProgressStageDTO> stages = buildStages();
        int currentStage = ThreadLocalRandom.current().nextInt(2, stages.size());
        int runningPercent = ThreadLocalRandom.current().nextInt(10, 96);
        int totalPercent = 0;
        for (int i = 0; i < stages.size(); i++) {
            PipelineProgressStageDTO stage = stages.get(i);
            if (i < currentStage) {
                stage.setPercent(100);
                stage.setStatus("done");
            } else if (i == currentStage) {
                stage.setPercent(runningPercent);
                stage.setStatus("running");
            } else {
                stage.setPercent(0);
                stage.setStatus("pending");
            }
            totalPercent += stage.getPercent();
        }

        PipelineProgressStageDTO runningStage = stages.get(currentStage);
        PipelineProgressDTO dto = new PipelineProgressDTO();
        dto.setPipelineId("mock-pipeline-001");
        dto.setPipelineName("AI 驱动的模型迭代闭环");
        dto.setOverall(Math.round(totalPercent / (float) stages.size()));
        dto.setStatus("running");
        dto.setStatusText(runningStage.getName() + "任务执行中");
        dto.setCurrentStage(currentStage);
        dto.setCurrentStageName(runningStage.getName());
        dto.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setStages(stages);
        return dto;
    }

    private List<PipelineProgressStageDTO> buildStages() {
        List<PipelineProgressStageDTO> stages = new ArrayList<>();
        stages.add(buildStage("client", "客户", "需求驱动 · 价值交付", 100, "done", "#00e5ff"));
        stages.add(buildStage("ai-base", "AI基地", "智能计算 · 调度中枢", 100, "done", "#00d4ff"));
        stages.add(buildStage("data-collection", "数据采集", "多源采集 · 清洗 · 标注", 100, "done", "#00b8ff"));
        stages.add(buildStage("model-repository", "模型仓库", "版本管理 · 存储 · 检索", 100, "done", "#0099ff"));
        stages.add(buildStage("training-factory", "训练工厂", "模型训练 · 调优 · 监控", 68, "done", "#0077ff"));
        stages.add(buildStage("inference-validation", "推理验证", "AI应用 · 机器人 · 部署", 0, "running", "#0077ff"));
        stages.add(buildStage("model-library", "模型库", "模型沉淀 · 持续迭代", 0, "pending", "#0033ff"));
        return stages;
    }

    private PipelineProgressStageDTO buildStage(String key, String name, String desc, Integer percent, String status, String color) {
        PipelineProgressStageDTO stage = new PipelineProgressStageDTO();
        stage.setKey(key);
        stage.setName(name);
        stage.setDesc(desc);
        stage.setPercent(percent);
        stage.setStatus(status);
        stage.setColor(color);
        return stage;
    }
}
