package com.eversec.llmops.service.service.impl;

import com.eversec.llmops.service.dto.PodDTO;
import com.eversec.llmops.service.entity.Inferenceservice;
import com.eversec.llmops.service.entity.Pipeline;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.entity.Task;
import com.eversec.llmops.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private InferenceserviceService inferenceserviceService;

    @Autowired
    private PodService podService;

    @Autowired
    private PrometheusService prometheusService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProManagerService proManagerService;

    @Autowired
    private PipelineService pipelineService;

    @Override
    public Map<String, Object> getMockTaskMonitorMetrics(String namespace, String podName) {
        Map<String, Object> result = new HashMap<>();
        result.put("namespace", namespace);
        result.put("podName", podName);
        result.put("podNames", buildMockPodNames(podName));
        result.put("metrics", queryTaskMetrics(buildPodRegex(podName)));
        return result;
    }

    @Override
    public Map<String, Object> getTaskMonitorMetrics(Integer taskId) {
        Map<String, Object> result = new HashMap<>();

        Task task = taskService.getById(taskId);
        if (task == null) {
            result.put("error", "Task not found");
            return result;
        }

        if (task.getNamespace() == null || task.getName() == null) {
            result.put("error", "Task namespace or name is empty");
            return result;
        }

        ProManager proManager = proManagerService.getByName(task.getNamespace());
        if (proManager == null || proManager.getClusterName() == null) {
            result.put("error", "Task cluster not found");
            return result;
        }

        List<PodDTO> pods = podService.getPodsByLabel(
                proManager.getClusterName(),
                task.getNamespace(),
                "task-id",
                taskId.toString());
        List<String> podNames = pods.stream()
                .map(PodDTO::getName)
                .collect(Collectors.toList());

        result.put("taskId", taskId);
        result.put("taskName", task.getName());
        result.put("namespace", task.getNamespace());
        result.put("podNames", podNames);

        if (podNames.isEmpty()) {
            result.put("metrics", new HashMap<String, Object>());
            return result;
        }

        result.put("metrics", queryTaskMetrics(buildPodRegex(podNames)));

        return result;
    }

    @Override
    public Map<String, Object> getInferenceServiceMonitorMetrics(Integer inferenceServiceId) {
        Map<String, Object> result = new HashMap<>();

        Inferenceservice inferenceService = inferenceserviceService.getByIdWithProManager(inferenceServiceId);
        if (inferenceService == null) {
            result.put("error", "InferenceService not found");
            return result;
        }

        List<String> podNames = getInferenceServicePodNames(inferenceService);
        result.put("podNames", podNames);

        Map<String, Object> metrics = new HashMap<>();
        for (String podName : podNames) {
            Map<String, Object> podMetrics = new HashMap<>();
            podMetrics.put("memory", prometheusService.queryRange(
                    PrometheusQueryBuilder.buildPodMemoryUsageQuery(podName), null, null, null));
            podMetrics.put("cpu", prometheusService.queryRange(
                    PrometheusQueryBuilder.buildPodCpuUsageQuery(podName), null, null, null));
            metrics.put(podName, podMetrics);
        }
        result.put("metrics", metrics);

        return result;
    }

    @Override
    public Map<String, Object> getInferenceServiceMemoryMetrics(Integer inferenceServiceId) {
        Map<String, Object> result = new HashMap<>();

        Inferenceservice inferenceService = inferenceserviceService.getByIdWithProManager(inferenceServiceId);
        if (inferenceService == null) {
            result.put("error", "InferenceService not found");
            return result;
        }

        List<String> podNames = getInferenceServicePodNames(inferenceService);
        result.put("podNames", podNames);

        Map<String, Object> metrics = new HashMap<>();
        for (String podName : podNames) {
            metrics.put(podName, prometheusService.queryRange(
                    PrometheusQueryBuilder.buildPodMemoryUsageQuery(podName), null, null, null));
        }
        result.put("data", metrics);

        return result;
    }

    @Override
    public Map<String, Object> getInferenceServiceCpuMetrics(Integer inferenceServiceId) {
        Map<String, Object> result = new HashMap<>();

        Inferenceservice inferenceService = inferenceserviceService.getByIdWithProManager(inferenceServiceId);
        if (inferenceService == null) {
            result.put("error", "InferenceService not found");
            return result;
        }

        List<String> podNames = getInferenceServicePodNames(inferenceService);
        result.put("podNames", podNames);

        Map<String, Object> metrics = new HashMap<>();
        for (String podName : podNames) {
            metrics.put(podName, prometheusService.queryRange(
                    PrometheusQueryBuilder.buildPodCpuUsageQuery(podName), null, null, null));
        }
        result.put("data", metrics);

        return result;
    }

    @Override
    public Map<String, Object> getPipelineTaskMonitorMetrics(Integer pipelineId) {
        Map<String, Object> result =  new HashMap<>();
        Pipeline pipeline = pipelineService.getById(pipelineId);
        ProManager proManager = proManagerService.getByName(pipeline.getNamespace());
        List<PodDTO> pods = podService.getPodsByLabel(
                proManager.getClusterName(),
                pipeline.getNamespace(),
                "pipeline-id",
                pipelineId.toString());
        List<String> podNames = pods.stream()
                .map(PodDTO::getName)
                .collect(Collectors.toList());

        result.put("pipelineId", pipelineId);
        result.put("pipelineName", pipeline.getName());
        result.put("namespace", pipeline.getNamespace());
        result.put("podNames", podNames);
        if (podNames.isEmpty()) {
            result.put("metrics", new HashMap<String, Object>());
            return result;
        }
        result.put("metrics", queryTaskMetrics(buildPodRegex(podNames)));
        return result;
    }

    private List<String> getInferenceServicePodNames(Inferenceservice inferenceService) {
        List<String> podNames = new ArrayList<>();
        List<PodDTO> pods = podService.getPodsByLabels(
                inferenceService.getClusterName(),
                inferenceService.getNamespace(),
                inferenceService.getName(),
                "inference");
        for (PodDTO pod : pods) {
            podNames.add(pod.getName());
        }
        return podNames;
    }

    private String buildPodRegex(List<String> podNames) {
        if (podNames == null || podNames.isEmpty()) {
            return ".*";
        }
        return ".*(" + String.join("|", podNames) + ").*";
    }

    private String buildPodRegex(String podName) {
        return ".*" + podName + ".*";
    }

    private List<String> buildMockPodNames(String podName) {
        List<String> podNames = new ArrayList<>();
        podNames.add(podName);
        return podNames;
    }

    private Map<String, Object> queryTaskMetrics(String podRegex) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("memory", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodMemoryUsageRegexQuery(podRegex), null, null, null));
        metrics.put("cpu", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodCpuUsageRegexQuery(podRegex), null, null, null));
        metrics.put("gpuUtilization", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodGpuUtilizationQuery(podRegex), null, null, null));
        metrics.put("gpuMemoryUsageUtil", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodGpuMemoryUsageUtilQuery(podRegex), null, null, null));
        metrics.put("npuUtilization", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodNpuUtilizationQuery(podRegex), null, null, null));
        metrics.put("npuUsedMemory", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodNpuUsedMemoryQuery(podRegex), null, null, null));
        metrics.put("networkReceive", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodNetworkReceiveRegexQuery(podRegex), null, null, null));
        metrics.put("networkTransmit", prometheusService.queryRange(
                PrometheusQueryBuilder.buildPodNetworkTransmitRegexQuery(podRegex), null, null, null));
        return metrics;
    }
}
