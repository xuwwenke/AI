package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.ClusterDTO;
import com.eversec.llmops.service.dto.ClusterUsageDTO;
import com.eversec.llmops.service.entity.Cluster;
import com.eversec.llmops.service.service.ClusterService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.service.KubeConfigService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-28
 */
@RestController
@RequestMapping("/api/cluster")
public class ClusterController {

    @Autowired
    private ClusterService clusterService;


    @Autowired
    private KubeConfigService kubeConfigService;

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Cluster> getById(@PathVariable Long id) {
        return ResponseHolder.build(() -> clusterService.getById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Cluster>> getPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ip) {
        return ResponseHolder.build(() -> clusterService.getPageList(pageNum, pageSize, name, ip));
    }

    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Cluster> createCluster(@RequestBody ClusterDTO dto) {
        return ResponseHolder.build(() -> clusterService.createCluster(dto));
    }

    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Boolean> updateCluster(@RequestBody ClusterDTO dto) {
        return ResponseHolder.build(() -> clusterService.updateCluster(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Boolean> deleteCluster(@PathVariable Long id) {
        return ResponseHolder.build(() -> clusterService.removeById(id));
    }

    @GetMapping("/usage")
    @Operation(summary = "集群资源使用情况")
    public RestResponse<ClusterUsageDTO> getClusterUsage(@RequestParam String clusterName) {
        return ResponseHolder.build(() -> clusterService.getClusterUsage(clusterName));
    }

    @ApiOperation("K8S集群查询接口")
    @GetMapping(value = "/kubeconfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getKubeConfig() {
        return kubeConfigService.getKubeConfigYaml();
    }
}