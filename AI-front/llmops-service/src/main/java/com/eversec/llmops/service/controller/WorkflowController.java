package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.WorkflowStatsDTO;
import com.eversec.llmops.service.entity.Pipeline;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.entity.Workflow;
import com.eversec.llmops.service.service.PipelineService;
import com.eversec.llmops.service.service.ProManagerService;
import com.eversec.llmops.service.service.WorkflowService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-21
 */
@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    private static final String WORKFLOW_API = "/workflow_modelview/api/";
    private static final String STOP_PATH = "stop/";
    private static final String WEB_DAG_PATH = "web/dag/";
    private static final String WEB_NODE_DETAIL_PATH = "web/node_detail/";

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private ProManagerService proManagerService;

    @Autowired
    private HttpProxyService httpProxyService;

    private String workflowDetailPath(Integer id) {
        return WORKFLOW_API + id;
    }

    private String workflowActionPath(String actionPrefix, Integer id) {
        return WORKFLOW_API + actionPrefix + id;
    }

    private String dagPath(String clusterName, String namespace, String pipelineArgoId) {
        return WORKFLOW_API + WEB_DAG_PATH + clusterName + "/" + namespace + "/" + pipelineArgoId;
    }

    private String nodeDetailPath(String clusterName, String namespace, String pipelineArgoId, String nodeId) {
        return WORKFLOW_API + WEB_NODE_DETAIL_PATH + clusterName + "/" + namespace + "/" + pipelineArgoId + "/" + nodeId;
    }

    private String getClusterNameByNamespace(String namespace) {
        ProManager proManager = proManagerService.getByName(namespace);
        if (proManager == null || !StringUtils.hasText(proManager.getClusterName())) {
            return null;
        }
        return proManager.getClusterName();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Workflow>> getPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader String namespace,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username) {
        return ResponseHolder.build(() -> workflowService.getPageList(pageNum, pageSize, namespace, status, label, name, username));
    }

    @GetMapping("/stats")
    @Operation(summary = "统计")
    public RestResponse<WorkflowStatsDTO> getStats(@RequestHeader String namespace) {
        return ResponseHolder.build(() -> workflowService.getStatsByNamespace(namespace));
    }

    @GetMapping("/dag/dev/pipeline")
    @Operation(summary = "获取任务流DAG")
    public RestResponse<Object> getDagByPipelineId(@RequestParam Integer pipelineId,
                                                   @RequestHeader String namespace) {
        return ResponseHolder.build(() -> {
            Pipeline pipeline = pipelineService.getById(pipelineId);
            if (pipeline == null || !StringUtils.hasText(pipeline.getPipelineArgoId())) {
                return null;
            }
            String clusterName = getClusterNameByNamespace(namespace);
            if (!StringUtils.hasText(clusterName)) {
                return null;
            }
            return httpProxyService.get(dagPath(clusterName, namespace, pipeline.getPipelineArgoId()), Object.class);
        });
    }

    @GetMapping("/node/detail/dev/pipeline")
    @Operation(summary = "获取节点详情")
    public RestResponse<Object> getNodeDetail(@RequestHeader String namespace,
                                              @RequestParam String pipelineArgoId,
                                              @RequestParam String nodeId) {
        return ResponseHolder.build(() -> {
            String clusterName = getClusterNameByNamespace(namespace);
            if (!StringUtils.hasText(clusterName)) {
                return null;
            }
            return httpProxyService.get(
                    nodeDetailPath(clusterName, namespace, pipelineArgoId, nodeId), Object.class);
        });
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取详情")
    public RestResponse<Workflow> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> workflowService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> deleteWorkflow(@PathVariable Integer id) {
        return ResponseHolder.build(() -> httpProxyService.delete(workflowDetailPath(id), Object.class));
    }

    @GetMapping("/stop/{id}")
    @Operation(summary = "停止")
    public RestResponse<Object> stopWorkflow(@PathVariable Integer id) {
        return ResponseHolder.build(() -> httpProxyService.get(workflowActionPath(STOP_PATH, id), Object.class));
    }

}
