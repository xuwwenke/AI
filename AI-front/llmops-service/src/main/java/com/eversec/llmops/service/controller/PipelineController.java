package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.PipelineBatchRequestDTO;
import com.eversec.llmops.service.service.PipelineService;
import com.eversec.llmops.service.dto.PipelineDTO;
import com.eversec.llmops.service.dto.PipelineRequestDTO;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.service.AiExperimentPipelineService;
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
@RequestMapping("/api/pipeline")
public class PipelineController {

    private static final String PIPELINE_API = "/pipeline_modelview/api/";
    private static final String RUN_PIPELINE_PATH = "run_pipeline/";
    private static final String PAUSE_PIPELINE_PATH = "pause_pipeline/";
    private static final String RESUME_PIPELINE_PATH = "resume_pipeline/";
    private static final String MULTI_ACTION_PATH = "multi_action/";
    private static final String COPY_ACTION = "copy";
    private static final String MULDELETE_ACTION = "muldelete";

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private HttpProxyService httpProxyService;

    @Autowired
    private AiExperimentPipelineService aiExperimentPipelineService;

    @Autowired
    private ProjectService projectService;

    private String pipelineDetailPath(Integer id) {
        return PIPELINE_API + id;
    }

    private String pipelineActionPath(String actionPrefix, Integer id) {
        return PIPELINE_API + actionPrefix + id;
    }

    private String multiActionPath(String action) {
        return PIPELINE_API + MULTI_ACTION_PATH + action;
    }

    /**
     * 根据ID获取详情
     * @param id ID
     * @return PipelineDTO对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Object> getById(@PathVariable Integer id) {
        Object response = httpProxyService.get(pipelineActionPath("", id),UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 自定义分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<PipelineDTO>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String describe,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) Integer aiExperimentId,
            @RequestHeader String namespace) {
        return ResponseHolder.build(() -> pipelineService.customGetPageList(pageNum, pageSize, id, name, describe, namespace, creator, aiExperimentId));
    }

    /**
     * 根据实验ID分页查询Pipeline
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param aiExperimentId 实验ID
     * @return QueryResult
     */
    @GetMapping("/page-by-experiment")
    @Operation(summary = "项目实验分页查询")
    public RestResponse<QueryResult<PipelineDTO>> getPageByExperimentId(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Integer aiExperimentId) {
        return ResponseHolder.build(() -> pipelineService.customGetPageList(pageNum, pageSize, null, null, null, null, null, aiExperimentId));
    }

    @PostMapping("")
    @Operation(summary = "新增")
    public RestResponse<Object> createPipeline(@RequestBody PipelineRequestDTO dto,@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        dto.setProject(project.getId());
        Object response = httpProxyService.post(PIPELINE_API, dto, UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    @PutMapping("")
    @Operation(summary = "修改")
    public RestResponse<Object> updatePipeline(@RequestBody PipelineRequestDTO dto, @RequestHeader String namespace) {
        pipelineService.fillEmptyFields(dto);
        Project project = projectService.getByName(namespace);
        dto.setProject(project.getId());
        Object response = httpProxyService.put(pipelineDetailPath(dto.getId()), dto, UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> deletePipeline(@PathVariable Integer id) {
        Object response = httpProxyService.delete(pipelineDetailPath(id), Object.class);
        return ResponseHolder.build(() -> response);
    }


    @PutMapping("/run_pipeline/{id}")
    @Operation(summary = "运行指定任务流")
    public RestResponse<Object> runPipeline(@PathVariable Integer id) {
        Object response = httpProxyService.get(pipelineActionPath(RUN_PIPELINE_PATH, id),UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }


    @PutMapping("/pause_pipeline/{id}")
    @Operation(summary = "暂停指定任务流")
    public RestResponse<Object> pausePipeline(@PathVariable Integer id) {
        Object response = httpProxyService.get(pipelineActionPath(PAUSE_PIPELINE_PATH, id),UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    @PutMapping("/resume_pipeline/{id}")
    @Operation(summary = "恢复指定任务流")
    public RestResponse<Object> resumePipeline(@PathVariable Integer id) {
        Object response = httpProxyService.get(pipelineActionPath(RESUME_PIPELINE_PATH, id),UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }



    @PostMapping("/mulcopy")
    @Operation(summary = "批量复制")
    public RestResponse<Object> mulCopy(@RequestBody PipelineBatchRequestDTO ids) {
        Object response = httpProxyService.post(multiActionPath(COPY_ACTION) ,ids,UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    @PostMapping("/muldelete")
    @Operation(summary = "批量删除")
    public RestResponse<Object> mulDelete(@RequestBody PipelineBatchRequestDTO ids) {
        Object response = httpProxyService.post(multiActionPath(MULDELETE_ACTION) ,ids,UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

}
