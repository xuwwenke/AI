package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.InferenceServiceRequestDTO;
import com.eversec.llmops.service.dto.InferenceServiceStatsDTO;
import com.eversec.llmops.service.entity.Inferenceservice;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.service.InferenceserviceService;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inferenceservice")
public class InferenceserviceController {

    private static final String INFERENCE_SERVICE_API = "/inferenceservice_modelview/api/";
    private static final String CLEAR_PATH = "clear/";
    private static final String DEPLOY_PROD_PATH = "deploy/prod/";
    private static final String DEPLOY_DEBUG_PATH = "deploy/debug/";
    private static final String SERVICE_TYPES_PATH = "service_types";

    @Autowired
    private InferenceserviceService inferenceserviceService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private HttpProxyService httpProxyService;

    private String detailPath(Integer id) {
        return INFERENCE_SERVICE_API + id;
    }

    private String actionPath(String actionPrefix, Integer id) {
        return INFERENCE_SERVICE_API + actionPrefix + id;
    }

    private String apiPath(String path) {
        return INFERENCE_SERVICE_API + path;
    }

    /**
     * 根据ID获取详情
     *
     * @param id ID
     * @return Inferenceservice对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Inferenceservice> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> inferenceserviceService.getById(id));
    }

    /**
     * 分页查询
     *
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @param namespace 项目名称
     * @param name 名称
     * @param label 标签
     * @param modelName 模型名称
     * @param modelVersion 模型版本
     * @param modelUrl 模型URL
     * @param status 状态
     * @param gpu GPU
     * @return QueryResult
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Inferenceservice>> getPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader String namespace,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String modelVersion,
            @RequestParam(required = false) String modelUrl,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gpu) {
        return ResponseHolder.build(() -> inferenceserviceService.getPageList(pageNum, pageSize, namespace, name, label, modelName, modelVersion, modelUrl, status, gpu));
    }

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param page 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
//    @GetMapping("/custom-page")
//    public RestResponse<QueryResult<Inferenceservice>> customGetPageList(
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer pageSize) {
//        return ResponseHolder.build(() -> inferenceserviceService.customGetPageList(page, pageSize));
//    }

    /**
     * 创建推理服务
     *
     * @param dto       请求参数
     * @param namespace 项目名称
     * @return 响应
     */
    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Object> createInferenceService(@RequestBody InferenceServiceRequestDTO dto,@RequestHeader String namespace) {
        return ResponseHolder.build(() -> {
            Project project = projectService.getByName(namespace);
            if (project != null){
                dto.setProject(project.getId());
            }
            return httpProxyService.post(INFERENCE_SERVICE_API, dto, UserUtil.getAbUserHeaders(), Object.class);
        });
    }

    /**
     * 更新推理服务
     *
     * @param dto 请求参数
     * @return 响应
     */
    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Object> updateInferenceService(@RequestBody InferenceServiceRequestDTO dto) {
        return ResponseHolder.build(() -> {
//            Project project = projectService.getByName(namespace);
//            if (project != null){
//                dto.setProject(project.getId());
//            }
            return httpProxyService.put(detailPath(dto.getId()), dto, UserUtil.getAbUserHeaders(), Object.class);
        });
    }

    /**
     * 删除推理服务
     *
     * @param id 服务ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> deleteInferenceService(@PathVariable Integer id) {
        return ResponseHolder.build(() -> httpProxyService.delete(detailPath(id), Object.class));
    }


    @PostMapping("/clear/{id}")
    @Operation(summary = "清理")
    public RestResponse<Object> clearInferenceService(@PathVariable Integer id) {
        return ResponseHolder.build(() -> httpProxyService.get(actionPath(CLEAR_PATH, id), UserUtil.getAbUserHeaders(),Object.class));
    }

    @PostMapping("/deploy/prod/{id}")
    @Operation(summary = "部署")
    public RestResponse<Object> deploy(@PathVariable Integer id) {
        return ResponseHolder.build(() -> httpProxyService.get(actionPath(DEPLOY_PROD_PATH, id), UserUtil.getAbUserHeaders(),Object.class));
    }


    @PostMapping("/deploy/debug/{id}")
    @Operation(summary = "调试")
    public RestResponse<Object> debug(@PathVariable Integer id) {
        return ResponseHolder.build(() -> httpProxyService.get(actionPath(DEPLOY_DEBUG_PATH, id), UserUtil.getAbUserHeaders(), Object.class));
    }

    @GetMapping("/service_types")
    @Operation(summary = "获取服务类型")
    public RestResponse<Object> getServiceTypes() {
        return ResponseHolder.build(() -> httpProxyService.get(apiPath(SERVICE_TYPES_PATH), UserUtil.getAbUserHeaders(), Object.class));
    }

    @GetMapping("/stats")
    @Operation(summary = "统计信息")
    public RestResponse<InferenceServiceStatsDTO> getStats(@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        return ResponseHolder.build(() -> inferenceserviceService.getStatsByProjectId(project.getId()));
    }

}
