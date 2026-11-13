package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.service.PrometheusService;
import com.eversec.framework.core.response2.ResponseHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class PrometheusController {

    @Autowired
    private PrometheusService prometheusService;

    @GetMapping("/service")
    public Map<String, Object> query(
            @RequestParam String clusterName,
            @RequestParam String serviceName) {
        return ResponseHolder.build(() -> prometheusService.query(clusterName, serviceName)).getBody();
    }

    @GetMapping("/service/range")
    public Map<String, Object> queryRange(
            @RequestParam String serviceName) {
        return ResponseHolder.build(() -> prometheusService.queryRange(serviceName, null, null, null)).getBody();
    }
}
