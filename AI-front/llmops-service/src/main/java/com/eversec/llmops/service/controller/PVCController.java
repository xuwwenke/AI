package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.PVCDTO;
import com.eversec.llmops.service.service.PVCService;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pvc")
@Api(tags = "PVC管理")
public class PVCController {

    @Autowired
    private PVCService pvcService;

    @GetMapping("/list")
    @ApiOperation("获取指定namespace下的所有PVC")
    public RestResponse<List<PVCDTO>> listPVCs(
            @ApiParam("集群名称") @RequestParam(required = false) String clusterName,
            @ApiParam("命名空间") @RequestParam String namespace) {
        return ResponseHolder.build(()->{
            return pvcService.getPVCsByNamespace(clusterName, namespace);
        });
    }

    @GetMapping("/get")
    @ApiOperation("获取单个PVC详情")
    public RestResponse<PVCDTO> getPVC(
            @ApiParam("集群名称") @RequestParam(required = false) String clusterName,
            @ApiParam("命名空间") @RequestParam String namespace,
            @ApiParam("PVC名称") @RequestParam String pvcName) {
        return ResponseHolder.build(() -> {
            PVCDTO pvc = pvcService.getPVC(clusterName, namespace, pvcName);
            if (pvc == null) {
                throw new RuntimeException("PVC not found");
            }
            return pvc;
        });
    }

    @PostMapping("/create")
    @ApiOperation("创建PVC")
    public RestResponse<PVCDTO> createPVC(
            @ApiParam("集群名称") @RequestParam(required = false) String clusterName,
            @ApiParam("命名空间") @RequestParam String namespace,
            @ApiParam("PVC名称") @RequestParam String pvcName,
            @ApiParam("存储类") @RequestParam(required = false) String storageClass,
            @ApiParam("存储大小") @RequestParam String storageSize,
            @ApiParam("访问模式") @RequestParam(required = false) List<String> accessModes,
            @ApiParam("标签") @RequestParam(required = false) Map<String, String> labels) {
        return ResponseHolder.build(() -> pvcService.createPVC(clusterName, namespace, pvcName, storageClass, storageSize, accessModes, labels));
    }

    @PostMapping("/delete")
    @ApiOperation("删除PVC")
    public RestResponse<Void> deletePVC(
            @ApiParam("集群名称") @RequestParam(required = false) String clusterName,
            @ApiParam("命名空间") @RequestParam String namespace,
            @ApiParam("PVC名称") @RequestParam String pvcName) {
        return ResponseHolder.build(() -> {
            pvcService.deletePVC(clusterName, namespace, pvcName);
            return null;
        });
    }

    @PostMapping("/listByLabels")
    @ApiOperation("根据标签获取PVC列表")
    public RestResponse<List<PVCDTO>> getPVCsByLabels(
            @ApiParam("集群名称") @RequestParam(required = false) String clusterName,
            @ApiParam("命名空间") @RequestParam String namespace,
            @ApiParam("标签") @RequestParam Map<String, String> labels) {
        return ResponseHolder.build(() -> {
            return pvcService.getPVCsByLabels(clusterName, namespace, labels);
        });
    }

    @GetMapping("/exists")
    @ApiOperation("检查PVC是否存在")
    public RestResponse<Boolean> exists(
            @ApiParam("集群名称") @RequestParam(required = false) String clusterName,
            @ApiParam("命名空间") @RequestParam String namespace,
            @ApiParam("PVC名称") @RequestParam String pvcName) {
        return ResponseHolder.build(()->{
          return   pvcService.exists(clusterName, namespace, pvcName);
        });
    }
}
