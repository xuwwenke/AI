package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.client.OpenShiftClientService;
import com.eversec.llmops.service.dto.ProjectRequestDTO;
import com.eversec.llmops.service.dto.ProjectSimpleDTO;
import com.eversec.llmops.service.dto.ResourceQuotaDTO;
import com.eversec.llmops.service.dto.StorageOverViewDTO;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.llmops.service.service.SysSecurityUserService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OpenShiftClientService openShiftClientService;

    @Autowired
    private SysSecurityUserService sysSecurityUserService;



    /**
     * 根据ID获取详情（包含项目管理器信息）
     * @param id ID
     * @return Project对象
     */
    @GetMapping("/get-with-manager/{id}")
    @Operation(summary = "根据id获取project&project-manager")
    public RestResponse<Project> getByIdWithManager(@PathVariable Integer id) {
        return ResponseHolder.build(() -> projectService.getByIdWithManager(id));
    }


    /**
     * 关联分页查询（project 和 pro_manager）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 项目名称
     * @param clusterName 集群名称
     * @return QueryResult
     */
    @GetMapping("/page-with-manager")
    @Operation(summary = "projectManager分页查询")
    public RestResponse<QueryResult<Project>> customGetPageListWithManager(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String clusterName) {
        return ResponseHolder.build(() -> projectService.customGetPageListWithManager(pageNum, pageSize, name, clusterName));
    }

    @GetMapping("/getCurrentUserProjectManagers")
    @Operation(summary = "获取当前用户可选项目")
    public RestResponse<QueryResult<Project>> getCurrentUserProjectManagers() {
        Long userId = UserUtil.getLoginUserId();
        return ResponseHolder.build(() -> {
            if (sysSecurityUserService.ifAilabxAdmin(userId)) {
                return projectService.getAllProjectListWithManager();
            }
            return projectService.getCurrentUserProjectListWithManager(userId);
        });
    }

//    @GetMapping("/project-with-manager/{id}")
//    public RestResponse<QueryResult<Project>> getProjectWithManagerById(
//            @@PathVariable Integer id) {
//        return ResponseHolder.build(() -> projectService.getProjectWithManagerById(pageNum, pageSize, name, clusterName));
//    }

    @PostMapping("")
    @Operation(summary = "新增")
    public Project createProject(@RequestBody ProjectRequestDTO dto) {
        return projectService.createProject(dto);
    }

    @PutMapping("")
    @Operation(summary = "修改")
    public RestResponse<Project> updateProject(@RequestBody ProjectRequestDTO dto) {
        return ResponseHolder.build(() -> projectService.updateProject(dto));
    }

    @GetMapping("/job-template-type")
    @Operation(summary = "job-template-type")
    public RestResponse<List<ProjectSimpleDTO>> getJobTemplateProjects() {
        return ResponseHolder.build(() -> projectService.getJobTemplateProjects());
    }

    @GetMapping("/resource-quota")
    @Operation(summary = "项目资源份额")
    public RestResponse<ResourceQuotaDTO> getResourceQuota(@RequestHeader String namespace) {
        return ResponseHolder.build(() -> openShiftClientService.getResourceQuota(namespace));
    }

    @GetMapping("/overview")
    @Operation(summary = "项目概览")
    public RestResponse<Map<String,Object>> overView(@RequestHeader String namespace) {
        return ResponseHolder.build(() -> projectService.overView(namespace));
    }

    @GetMapping("/dataInfo")
    @Operation(summary = "数据信息概览")
    public RestResponse<Map<String,Integer>> dataInfo(@RequestHeader String namespace) {
        return ResponseHolder.build(() -> projectService.dataInfo(projectService.getByName(namespace).getId()));
    }


    @GetMapping("/index")
    @Operation(summary = "首页")
    public RestResponse<Map<String,Integer>> index(@RequestHeader String namespace) {
        return ResponseHolder.build(() -> projectService.index(namespace));
    }



    @GetMapping("/storage-overview")
    @Operation(summary = "存储概览")
    public RestResponse<StorageOverViewDTO> getStorageOverView(@RequestHeader String namespace) {
        return ResponseHolder.build(() -> openShiftClientService.getStorageOverView(namespace));
    }
}
