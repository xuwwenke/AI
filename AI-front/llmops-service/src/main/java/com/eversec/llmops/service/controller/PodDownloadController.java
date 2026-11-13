package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.service.PodDownloadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "容器文件下载扩展管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/k8s-pod-download")
public class PodDownloadController {

    private final PodDownloadService podDownloadService;

    @ApiOperation("在容器中通过预签名URL下载大文件")
    @PostMapping(path = { "/namespaces/{namespace}/pods/{name}/download-from-url" }, produces = MediaType.APPLICATION_JSON_VALUE)
    public String downloadFromUrl(
            @RequestHeader("clusterName") String clusterName,
            @PathVariable("namespace") String namespace,
            @PathVariable("name") String name,
            @RequestBody Map<String, String> param) {
        
        String url = param.get("url");
        String targetPath = param.get("targetPath");
        
        return podDownloadService.downloadFromUrl(clusterName, namespace, name, url, targetPath);
    }
}
