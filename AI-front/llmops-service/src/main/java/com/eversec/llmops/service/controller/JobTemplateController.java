package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.JobTemplateCopyDTO;
import com.eversec.llmops.service.dto.JobTemplateDTO;
import com.eversec.llmops.service.dto.JobTemplateRequestDTO;
import com.eversec.llmops.service.entity.JobTemplate;
import com.eversec.llmops.service.service.JobTemplateService;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/job-template")
public class JobTemplateController {

    @Autowired
    private JobTemplateService jobTemplateService;

    @Autowired
    private HttpProxyService httpProxyService;


    @Autowired
    private ProjectService projectService;

    /**
     * 根据ID获取详情
     * @param id ID
     * @return JobTemplate对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<JobTemplate> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> jobTemplateService.getDetailsById(id));
    }

    /**
     * 自定义分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<JobTemplateDTO>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String modifier,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) Integer images,
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) Boolean ifPublic,
            @RequestHeader String namespace) {
        Integer bizProjectId;
        if(ifPublic!=null&&ifPublic){
            bizProjectId = null;
        }else {
            bizProjectId = projectService.getByName(namespace).getId();
        }
        return ResponseHolder.build(() -> jobTemplateService.customGetPageList(pageNum, pageSize, creator, modifier, version, images, projectId, bizProjectId));
    }


    /**
     * JobTemplateRequestDTO转JobTemplate entity
     * @param dto 请求DTO
     * @return JobTemplate entity
     */
    private JobTemplate convertToEntity(JobTemplateRequestDTO dto) {
        JobTemplate jobTemplate = new JobTemplate();
        jobTemplate.setName(dto.getName());
        jobTemplate.setVersion(dto.getVersion());
        jobTemplate.setImagesId(dto.getImages());
        jobTemplate.setDescribe(dto.getDescribe());
        jobTemplate.setWorkdir(dto.getWorkdir());
        jobTemplate.setEntrypoint(dto.getEntrypoint());
        jobTemplate.setArgs(dto.getArgs());
        jobTemplate.setEnv(dto.getEnv());
        jobTemplate.setVolumeMount(dto.getVolume_mount());
        jobTemplate.setHostAliases(dto.getHost_aliases());
        jobTemplate.setPrivileged(dto.getPrivileged());
        jobTemplate.setAccounts(dto.getAccounts());
        jobTemplate.setExpand(dto.getExpand());
        jobTemplate.setProjectId(dto.getProject());
        return jobTemplate;
    }

    /**
     * 更新JobTemplate entity（从DTO更新已有记录）
     * @param jobTemplate 现有entity
     * @param dto 请求DTO
     */
    private void updateEntityFromDto(JobTemplate jobTemplate, JobTemplateRequestDTO dto) {
        if (dto.getName() != null) {
            jobTemplate.setName(dto.getName());
        }
        if (dto.getVersion() != null) {
            jobTemplate.setVersion(dto.getVersion());
        }
        if (dto.getImages() != null) {
            jobTemplate.setImagesId(dto.getImages());
        }
        if (dto.getDescribe() != null) {
            jobTemplate.setDescribe(dto.getDescribe());
        }
        if (dto.getWorkdir() != null) {
            jobTemplate.setWorkdir(dto.getWorkdir());
        }
        if (dto.getEntrypoint() != null) {
            jobTemplate.setEntrypoint(dto.getEntrypoint());
        }
        if (dto.getArgs() != null) {
            jobTemplate.setArgs(dto.getArgs());
        }
        if (dto.getEnv() != null) {
            jobTemplate.setEnv(dto.getEnv());
        }
        if (dto.getVolume_mount() != null) {
            jobTemplate.setVolumeMount(dto.getVolume_mount());
        }
        if (dto.getHost_aliases() != null) {
            jobTemplate.setHostAliases(dto.getHost_aliases());
        }
        if (dto.getPrivileged() != null) {
            jobTemplate.setPrivileged(dto.getPrivileged());
        }
        if (dto.getAccounts() != null) {
            jobTemplate.setAccounts(dto.getAccounts());
        }
        if (dto.getExpand() != null) {
            jobTemplate.setExpand(dto.getExpand());
        }
        if (dto.getProject() != null) {
            jobTemplate.setProjectId(dto.getProject());
        }
    }

    /**
     * 新增JobTemplate
     * @param dto JobTemplateRequestDTO对象
     * @return RestResponse
     */
    @PostMapping("")
    @Operation(summary = "新增")
    public RestResponse<JobTemplate> createJobTemplate(@RequestBody JobTemplateRequestDTO dto,@RequestHeader String namespace) {
        return ResponseHolder.build(() -> {
            JobTemplate jobTemplate = convertToEntity(dto);
            Integer userId = UserUtil.getAbUserID();
            jobTemplate.setCreatedByFk(userId);
            jobTemplate.setChangedByFk(userId);
            jobTemplate.setCreatedOn(LocalDateTime.now());
            jobTemplate.setChangedOn(LocalDateTime.now());
            jobTemplate.setBizProjectId(projectService.getByName(namespace).getId());
            jobTemplateService.save(jobTemplate);
            return jobTemplate;
        });
    }

    /**
     * 修改JobTemplate
     * @param dto JobTemplateRequestDTO对象
     * @return RestResponse
     */
    @PutMapping("")
    @Operation(summary = "修改")
    public RestResponse<JobTemplate> updateJobTemplate(@RequestBody JobTemplateRequestDTO dto) {
        return ResponseHolder.build(() -> {
            JobTemplate jobTemplate = jobTemplateService.getById(dto.getId());
            if (jobTemplate == null) {
                throw new RuntimeException("JobTemplate not found");
            }
            
            updateEntityFromDto(jobTemplate, dto);
            
            jobTemplate.setChangedByFk(UserUtil.getAbUserID());
            jobTemplate.setChangedOn(LocalDateTime.now());
            
            jobTemplateService.updateById(jobTemplate);
            return jobTemplate;
        });
    }

    /**
     * 删除JobTemplate
     * @param id ID
     * @return RestResponse
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> deleteJobTemplate(@PathVariable Integer id) {
        return ResponseHolder.build(() -> {
            JobTemplate jobTemplate = jobTemplateService.getById(id);
            if (jobTemplate != null && Integer.valueOf(1).equals(jobTemplate.getBizProjectId())) {
                return false;
            }
            return httpProxyService.delete("/job_template_fab_modelview/api/" + id, Object.class);
        });
    }

    /**
     * 批量复制JobTemplate
     * @param dto JobTemplateCopyDTO对象
     * @return RestResponse
     */
    @PostMapping("/copy")
    @Operation(summary = "复制")
    public RestResponse<Object> copyJobTemplate(@RequestBody JobTemplateCopyDTO dto) {
        Object response = httpProxyService.post("/job_template_fab_modelview/api/multi_action/copy", dto, Object.class);
        return ResponseHolder.build(() -> response);
    }



    @GetMapping("/list")
    @Operation(summary = "查询列表")
    public RestResponse<QueryResult<JobTemplateDTO>> list(@RequestHeader String namespace) {
        Integer projectId =  projectService.getByName(namespace).getId();
        return ResponseHolder.build(() -> jobTemplateService.list(projectId));
    }
}
