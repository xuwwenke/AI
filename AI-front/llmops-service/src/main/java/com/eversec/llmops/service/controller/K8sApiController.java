package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.service.k8s.K8sPodService;
import com.eversec.llmops.service.service.k8s.ProxyService;
import io.kubernetes.client.openapi.models.V1PodList;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@AllArgsConstructor
public class K8sApiController {



    private ProxyService proxyService;


    private K8sPodService k8sPodService;


    private final HttpServletRequest servletRequest;

    private final HttpServletResponse servletResponse;

    @ApiOperation("查询pods")
    @GetMapping(path = { "/k8s-api/api/v1/namespaces/{namespace}/pods" }, produces = { "application/json;charset=UTF-8" })
    public void getPods(
            @RequestHeader("clusterName") final String clusterName,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse, @PathVariable("namespace") String namespace)
            throws IOException {
        proxyService.request(clusterName, servletRequest, servletResponse, k8sPodService::getPods, null, V1PodList.class, true);
    }


    @RequestMapping(path = { "/k8s-api/api/v1/namespaces/{namespace}/persistentvolumeclaims" }, produces = { "application/json;charset=UTF-8" })
    public void proxy(@RequestHeader("clusterName") String clusterName) throws IOException {
        proxyService.request(clusterName, servletRequest, servletResponse);
    }


    @DeleteMapping(path = { "/k8s-api/api/v1/namespaces/{namespace}/persistentvolumeclaims/{name}" }, produces = { "application/json;charset=UTF-8" })
    public void deletePvc(@RequestHeader("clusterName") String clusterName) throws IOException {
        proxyService.request(clusterName, servletRequest, servletResponse);
    }
}
