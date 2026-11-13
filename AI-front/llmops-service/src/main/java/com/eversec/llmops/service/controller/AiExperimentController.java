package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.AiExperimentRequestDTO;
import com.eversec.llmops.service.dto.AiExperimentSimpleDTO;
import com.eversec.llmops.service.dto.AiExperimentStatsDTO;
import com.eversec.llmops.service.entity.AiExperiment;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.service.AiExperimentService;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/api/ai-experiment")
public class AiExperimentController {

    @Autowired
    private AiExperimentService aiExperimentService;

    @Autowired
    private ProjectService projectService;


    @GetMapping("/page")
    @Operation(summary = "分页")
    public RestResponse<QueryResult<AiExperiment>> getExperimentsByNamespace(
            @RequestHeader String namespace,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String creator) {
        Project project = projectService.getByName(namespace);
        QueryResult<AiExperiment> result = aiExperimentService.getExperimentsByProjectIdWithPage(project.getId(), pageNum, pageSize, status, modelName, creator);
        return ResponseHolder.build(() -> result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<AiExperiment> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> aiExperimentService.getById(id));
    }

    @PostMapping("")
    @Operation(summary = "新增")
    public RestResponse<AiExperiment> createExperiment(@RequestBody AiExperimentRequestDTO dto, @RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        AiExperiment aiExperiment = new AiExperiment();
        BeanUtils.copyProperties(dto, aiExperiment);
        aiExperiment.setProjectId(project.getId());
        aiExperiment.setStatus(0);
        aiExperiment.setStartTime(LocalDateTime.now());
        Integer userId = UserUtil.getAbUserID();
        aiExperiment.setCreatedByFk(userId);
        aiExperiment.setChangedByFk(userId);
        aiExperimentService.save(aiExperiment);
        return ResponseHolder.build(() -> aiExperiment);
    }

    @PutMapping("")
    @Operation(summary = "修改")
    public RestResponse<Boolean> updateExperiment(@RequestBody AiExperimentRequestDTO dto) {
        AiExperiment aiExperiment = new AiExperiment();
        BeanUtils.copyProperties(dto, aiExperiment);
        aiExperiment.setChangedByFk(UserUtil.getAbUserID());
        return ResponseHolder.build(() -> aiExperimentService.updateById(aiExperiment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Boolean> deleteExperiment(@PathVariable Integer id) {
        return ResponseHolder.build(() -> aiExperimentService.removeById(id));
    }

    @GetMapping("/stats")
    @Operation(summary = "统计")
    public RestResponse<AiExperimentStatsDTO> getStats(@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        AiExperimentStatsDTO stats = aiExperimentService.getStatsByProjectId(project.getId());
        return ResponseHolder.build(() -> stats);
    }

    @GetMapping("/running-count")
    @Operation(summary = "运行中的数量")
    public RestResponse<Integer> getRunningCount(@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        Integer count = aiExperimentService.getRunningCountByProjectId(project.getId());
        return ResponseHolder.build(() -> count);
    }

    @GetMapping("/list-by-project")
    @Operation(summary = "项目下的实验列表")
    public RestResponse<List<AiExperimentSimpleDTO>> getExperimentsByProjectId(@RequestHeader String namespace) {
        Project project = projectService.getByName(namespace);
        List<AiExperimentSimpleDTO> experiments = aiExperimentService.getExperimentListByProjectId(project.getId());
        return ResponseHolder.build(() -> experiments);
    }

    @PutMapping("/{id}/start")
    @Operation(summary = "开始实验")
    public RestResponse<Boolean> startExperiment(@PathVariable Integer id) {
        boolean success = aiExperimentService.startExperiment(id);
        return ResponseHolder.build(() -> success);
    }

    @PutMapping("/{id}/pause")
    @Operation(summary = "暂停实验")
    public RestResponse<Boolean> pauseExperiment(@PathVariable Integer id) {
        boolean success = aiExperimentService.pauseExperiment(id);
        return ResponseHolder.build(() -> success);
    }

    @PutMapping("/{id}/finish")
    @Operation(summary = "结束实验")
    public RestResponse<Boolean> finishExperiment(@PathVariable Integer id) {
        boolean success = aiExperimentService.finishExperiment(id);
        return ResponseHolder.build(() -> success);
    }

    @PutMapping("/{id}/fail")
    @Operation(summary = "实验失败")
    public RestResponse<Boolean> failExperiment(@PathVariable Integer id) {
        boolean success = aiExperimentService.failExperiment(id);
        return ResponseHolder.build(() -> success);
    }
}
