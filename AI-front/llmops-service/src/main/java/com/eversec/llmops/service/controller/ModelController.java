package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.ModelDTO;
import com.eversec.llmops.service.dto.ModelStatsDTO;
import com.eversec.llmops.service.entity.Model;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.service.ModelService;
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
@RequestMapping("/api/model")
public class ModelController {

    private static final String MODEL_WEB_API = "/training_model_modelview/web/api/";
    private static final String MODEL_DEPLOY_API = "/training_model_modelview/api/deploy/";

    @Autowired
    private ModelService modelService;

    @Autowired
    private HttpProxyService httpProxyService;

    @Autowired
    private ProjectService projectService;

    private String modelDetailPath(Integer id) {
        return MODEL_WEB_API + id;
    }

    private String deployPath(Integer id) {
        return MODEL_DEPLOY_API + id;
    }

    /**
     * 根据ID获取详情
     * @param id ID
     * @return Model对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Model> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> modelService.getById(id));
    }

    /**
     * 分页查询
     * @param page 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
//    @GetMapping("/page")
//    public RestResponse<QueryResult<Model>> getPageList(
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer pageSize) {
//        return ResponseHolder.build(() -> modelService.getPageList(page, pageSize));
//    }

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param namespace 项目名称
     * @param name 模型名称
     * @param version 版本
     * @param framework 框架
     * @param path 路径
     * @return QueryResult
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Model>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader String namespace,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String framework,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Boolean ifPublic) {
        if(ifPublic!=null && ifPublic){
            namespace = "public";
        }
        QueryResult<Model> result = modelService.customGetPageList(pageNum, pageSize, namespace, name, version, framework, path);
        return ResponseHolder.build(() -> result);
    }

    @GetMapping("/stats")
    @Operation(summary = "统计")
    public RestResponse<ModelStatsDTO> getStats(@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        ModelStatsDTO stats = modelService.getStats(namespace, project.getId());
        return ResponseHolder.build(() -> stats);
    }

    /**
     * 创建模型
     * @param modelDTO ModelDTO对象
     * @return 响应
     */
    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Object> createModel(@RequestBody ModelDTO modelDTO,@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        modelDTO.setProject(project.getId());
        Object response = httpProxyService.post(MODEL_WEB_API, modelDTO, UserUtil.getHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 更新模型
     * @param modelDTO ModelDTO对象
     * @return 响应
     */
    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Object> updateModel(@RequestBody ModelDTO modelDTO,@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        modelDTO.setProject(project.getId());
        Object response = httpProxyService.put(modelDetailPath(modelDTO.getId()), modelDTO, Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 删除模型
     * @param id 模型ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> delete(@PathVariable Integer id) {
        Object response = httpProxyService.delete(modelDetailPath(id), Object.class);
        return ResponseHolder.build(() -> response);
    }


    /**
     * 删除模型
     * @param id 模型ID
     * @return 响应
     */
    @PostMapping("/deploy/{id}")
    @Operation(summary = "发布")
    public RestResponse<Object> deploy(@PathVariable Integer id) {
        Object response = httpProxyService.post(deployPath(id),UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }
}
