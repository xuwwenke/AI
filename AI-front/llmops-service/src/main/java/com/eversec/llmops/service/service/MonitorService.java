package com.eversec.llmops.service.service;

import java.util.Map;

public interface MonitorService {

    Map<String, Object> getMockTaskMonitorMetrics(String namespace, String podName);

    Map<String, Object> getTaskMonitorMetrics(Integer taskId);

    Map<String, Object> getInferenceServiceMonitorMetrics(Integer inferenceServiceId);

    Map<String, Object> getInferenceServiceMemoryMetrics(Integer inferenceServiceId);

    Map<String, Object> getInferenceServiceCpuMetrics(Integer inferenceServiceId);

    Map<String, Object> getPipelineTaskMonitorMetrics(Integer pipelineId);
}
