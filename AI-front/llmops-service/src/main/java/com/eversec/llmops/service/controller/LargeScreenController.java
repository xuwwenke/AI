package com.eversec.llmops.service.controller;

import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.service.DatasetService;
import com.eversec.llmops.service.service.ModelService;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.llmops.service.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/large-screen")
public class LargeScreenController {

    @Resource
    private WorkflowService workflowService;

    @Resource
    private ModelService modelService;

    @Resource
    private DatasetService datasetService;

    @Resource
    private ProjectService projectService;

    @GetMapping("/overview")
    @Operation(summary = "获取概览指标")
    public RestResponse<List<Map<String, Object>>> getOverview(@RequestHeader String namespace) {
        Integer projectId = projectService.getByName(namespace).getId();
        return ResponseHolder.build(() -> buildOverviewData(namespace,projectId));
    }

    @GetMapping("/conn-labels")
    @Operation(summary = "获取链路标签")
    public RestResponse<List<String>> getConnLabels() {
        return ResponseHolder.build(() -> Arrays.asList("需求驱动", "智能调度", "转换标注", "机器执行", "AI驱动", "迭代沉淀", "价值交付"));
    }

    @GetMapping("/anomalies")
    @Operation(summary = "获取异常列表")
    public RestResponse<List<Map<String, Object>>> getAnomalies() {
        return ResponseHolder.build(this::buildAnomalies);
    }

    @GetMapping("/pipeline-data")
    @Operation(summary = "获取模型迭代流水线数据")
    public RestResponse<List<Map<String, Object>>> getPipelineData() {
        return ResponseHolder.build(this::buildPipelineData);
    }

    @GetMapping("/charts/version")
    @Operation(summary = "获取模型版本分布")
    public RestResponse<List<Map<String, Object>>> getVersionChart() {
        return ResponseHolder.build(() -> Arrays.asList(
            chartItem("训练中", ThreadLocalRandom.current().nextInt(30, 60), "#00e5ff"),
            chartItem("待验证", ThreadLocalRandom.current().nextInt(20, 40), "#0099ff"),
            chartItem("已部署", ThreadLocalRandom.current().nextInt(120, 180), "#0077ff"),
            chartItem("已归档", ThreadLocalRandom.current().nextInt(80, 130), "#0033ff")
        ));
    }

    @GetMapping("/charts/dataset")
    @Operation(summary = "获取数据集来源占比")
    public RestResponse<List<Map<String, Object>>> getDatasetChart() {
        return ResponseHolder.build(() -> Arrays.asList(
            chartItem("公开数据集", ThreadLocalRandom.current().nextInt(35, 55), "#00d4ff"),
            chartItem("自采", ThreadLocalRandom.current().nextInt(20, 40), "#0099ff"),
            chartItem("合成", ThreadLocalRandom.current().nextInt(10, 25), "#0077ff"),
            chartItem("第三方", ThreadLocalRandom.current().nextInt(5, 15), "#0055ff")
        ));
    }

    @GetMapping("/charts/training")
    @Operation(summary = "获取训练任务趋势")
    public RestResponse<Map<String, Object>> getTrainingChart() {
        return ResponseHolder.build(this::buildTrainingChart);
    }

    @GetMapping("/charts/inference")
    @Operation(summary = "获取实时推理监控")
    public RestResponse<Map<String, Object>> getInferenceChart() {
        return ResponseHolder.build(this::buildInferenceChart);
    }

    @GetMapping("/charts/radar")
    @Operation(summary = "获取模型性能雷达")
    public RestResponse<Map<String, Object>> getRadarChart() {
        return ResponseHolder.build(this::buildRadarChart);
    }

    @GetMapping("/charts/heatmap")
    @Operation(summary = "获取资源利用率热力图")
    public RestResponse<List<List<Integer>>> getHeatmapChart() {
        return ResponseHolder.build(this::buildHeatmap);
    }

    private List<Map<String, Object>> buildOverviewData(String namespace,Integer projectId) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(overviewItem("活跃训练任务", workflowService.getRunningCount(namespace), ThreadLocalRandom.current().nextInt(5, 25), "#00d4ff", "⚙️"));
        list.add(overviewItem("模型版本数",modelService.getTotalCount(namespace), ThreadLocalRandom.current().nextInt(3, 16), "#0099ff", "🏷️"));
        list.add(overviewItem("数据集总量", datasetService.getCountByProjectId(projectId), ThreadLocalRandom.current().nextInt(10, 30), "#00b8ff", "🗃️"));
        list.add(overviewItem("推理请求/日", ThreadLocalRandom.current().nextInt(70, 110), ThreadLocalRandom.current().nextInt(20, 40), "#0077ff", "⚡"));
        list.add(overviewItem("模型准确率", ThreadLocalRandom.current().nextInt(91, 98), ThreadLocalRandom.current().nextInt(1, 5), "#00e5ff", "🎯"));
        list.add(overviewItem("异常告警", ThreadLocalRandom.current().nextInt(15, 35), ThreadLocalRandom.current().nextInt(30, 60), "#ff4757", "🔔"));
        return list;
    }

    private Map<String, Object> overviewItem(String label, Object value, Integer trend, String color, String icon) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("trend", trend);
        item.put("color", color);
        item.put("icon", icon);
        return item;
    }

    private List<Map<String, Object>> buildAnomalies() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(anomaly("yolov8", "误报", "高", "处理中"));
        list.add(anomaly("bert-base", "超时", "中", "待处理"));
        list.add(anomaly("resnet50", "精度下降", "高", "处理中"));
        list.add(anomaly("gpt2", "显存溢出", "严重", "已修复"));
        list.add(anomaly("yolov8", "误报", "低", "已忽略"));
        list.add(anomaly("bert-base", "超时", "中", "待处理"));
        return list;
    }

    private Map<String, Object> anomaly(String model, String type, String level, String status) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("time", randomTime());
        item.put("model", model);
        item.put("type", type);
        item.put("level", level);
        item.put("status", status);
        return item;
    }

    private String randomTime() {
        int h = ThreadLocalRandom.current().nextInt(16, 17);
        int m = ThreadLocalRandom.current().nextInt(0, 60);
        int s = ThreadLocalRandom.current().nextInt(0, 60);
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private List<Map<String, Object>> buildPipelineData() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(pipelineItem("数据准备", randomDays(), ThreadLocalRandom.current().nextInt(20, 60), "#00d4ff"));
        list.add(pipelineItem("训练", randomDays(), ThreadLocalRandom.current().nextInt(75, 100), "#0099ff"));
        list.add(pipelineItem("验证", randomDays(), ThreadLocalRandom.current().nextInt(15, 45), "#0077ff"));
        list.add(pipelineItem("部署", randomDays(), ThreadLocalRandom.current().nextInt(10, 35), "#0055ff"));
        list.add(pipelineItem("监控", randomDays(), ThreadLocalRandom.current().nextInt(30, 65), "#0033ff"));
        return list;
    }

    private Map<String, Object> pipelineItem(String name, double days, int percent, String color) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("days", days);
        item.put("percent", percent);
        item.put("color", color);
        return item;
    }

    private double randomDays() {
        return Math.round(ThreadLocalRandom.current().nextDouble(0.5, 9.5) * 10) / 10.0;
    }

    private Map<String, Object> chartItem(String name, int value, String color) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        item.put("color", color);
        return item;
    }

    private Map<String, Object> buildTrainingChart() {
        List<String> xAxis = Arrays.asList("12/18", "12/19", "12/20", "12/21", "12/22", "12/23", "12/24");
        List<Integer> taskCount = new ArrayList<>();
        List<Integer> gpu = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            taskCount.add(ThreadLocalRandom.current().nextInt(10, 30));
            gpu.add(ThreadLocalRandom.current().nextInt(55, 95));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("xAxis", xAxis);
        data.put("taskCount", taskCount);
        data.put("gpu", gpu);
        return data;
    }

    private Map<String, Object> buildInferenceChart() {
        List<String> xAxis = new ArrayList<>();
        List<Integer> requests = new ArrayList<>();
        List<Integer> latency = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            xAxis.add(String.format("16:%02d:%02d", i, ThreadLocalRandom.current().nextInt(0, 60)));
            requests.add(ThreadLocalRandom.current().nextInt(20, 80));
            latency.add(ThreadLocalRandom.current().nextInt(70, 150));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("xAxis", xAxis);
        data.put("requests", requests);
        data.put("latency", latency);
        return data;
    }

    private Map<String, Object> buildRadarChart() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("indicators", Arrays.asList(
            radarIndicator("准确率"), radarIndicator("召回率"), radarIndicator("F1"),
            radarIndicator("吞吐"), radarIndicator("稳定性")
        ));
        List<Integer> value = new ArrayList<>();
        for (int i = 0; i < 5; i++) value.add(ThreadLocalRandom.current().nextInt(75, 98));
        data.put("value", value);
        return data;
    }

    private Map<String, Object> radarIndicator(String name) {
        Map<String, Object> indicator = new LinkedHashMap<>();
        indicator.put("name", name);
        indicator.put("max", 100);
        return indicator;
    }

    private List<List<Integer>> buildHeatmap() {
        List<List<Integer>> data = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            for (int h = 0; h < 24; h++) {
                List<Integer> point = new ArrayList<>();
                point.add(h);
                point.add(d);
                point.add(ThreadLocalRandom.current().nextInt(0, 100));
                data.add(point);
            }
        }
        return data;
    }
}
