package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Autowired
    private HttpProxyService httpProxyService;

    /**
     * 获取资源使用情况
     * @return 资源使用情况
     */
    @GetMapping("/data")
    @Operation(summary = "资源概览")
    public RestResponse<Object> getResourceData() {
        Object response = httpProxyService.get("/total_resource/api/data", Object.class);
        // 处理响应，提取result部分
        if (response instanceof java.util.Map) {
            java.util.Map<?, ?> responseMap = (java.util.Map<?, ?>) response;
            Object result = responseMap.get("result");
            if (result != null) {
                return ResponseHolder.build(() -> result);
            }
        }
        return ResponseHolder.build(() -> response);
    }
}