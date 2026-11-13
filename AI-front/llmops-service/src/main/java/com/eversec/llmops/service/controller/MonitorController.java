package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.service.MonitorService;
import com.eversec.framework.core.response2.ResponseHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @GetMapping("/task/mock")
    public Map<String, Object> getMockTaskMonitorMetrics(@RequestParam String namespace,
                                                         @RequestParam String podName) {
        return ResponseHolder.build(() -> monitorService.getMockTaskMonitorMetrics(namespace, podName)).getBody();
    }

    @GetMapping("/task/{id}")
    public Map<String, Object> getTaskMonitorMetrics(@PathVariable Integer id) {
        return ResponseHolder.build(() -> monitorService.getTaskMonitorMetrics(id)).getBody();
    }

    @GetMapping("/inference/{id}")
    public Map<String, Object> getInferenceServiceMonitorMetrics(@PathVariable Integer id) {
        return ResponseHolder.build(() -> monitorService.getInferenceServiceMonitorMetrics(id)).getBody();
    }

    @GetMapping("/inference/{id}/memory")
    public Map<String, Object> getInferenceServiceMemoryMetrics(@PathVariable Integer id) {
        return ResponseHolder.build(() -> monitorService.getInferenceServiceMemoryMetrics(id)).getBody();
    }

    @GetMapping("/inference/{id}/cpu")
    public Map<String, Object> getInferenceServiceCpuMetrics(@PathVariable Integer id) {
        return ResponseHolder.build(() -> monitorService.getInferenceServiceCpuMetrics(id)).getBody();
    }



    @GetMapping("/pipeline/{id}")
    public Map<String, Object> getPipelineTaskMonitorMetrics(@PathVariable Integer id) {
        return ResponseHolder.build(() -> monitorService.getPipelineTaskMonitorMetrics(id)).getBody();
    }
}
