package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.webboot.common.exception.BusinessException;
import com.eversec.llmops.service.dto.ProjectRequestDTO;
import com.eversec.llmops.service.dto.ProjectSimpleDTO;
import com.eversec.llmops.service.dto.ResourceQuotaDTO;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.client.OpenShiftClientService;
import com.eversec.llmops.service.enums.LlmopsErrorEnum;
import com.eversec.llmops.service.mapper.AiExperimentMapper;
import com.eversec.llmops.service.mapper.ProjectMapper;
import com.eversec.llmops.service.mapper.WorkflowMapper;
import com.eversec.llmops.service.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Autowired
    private ProManagerService proManagerService;

    @Autowired
    private OpenShiftClientService openShiftClientService;

    @Autowired
    private KubernetesResourceService kubernetesResourceService;

    @Autowired
    private WorkflowMapper workflowMapper;

    @Autowired
    private AiExperimentMapper aiExperimentMapper;

    @Autowired
    private AiExperimentService aiExperimentService;

    @Autowired
    private InferenceserviceService inferenceserviceService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private TaskService taskService;

    private Map<String,Project> projectCache = new ConcurrentHashMap<>();

    @Override
    public QueryResult<Project> getPageList(Integer pageNum, Integer pageSize) {
        Page<Project> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        Page<Project> resultPage = page(page, wrapper);
        QueryResult<Project> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public QueryResult<Project> customGetPageList(Integer pageNum, Integer pageSize) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 查询分页数据
        List<Project> projectList = baseMapper.customGetPageList(offset, pageSize);
        
        // 查询总数
        int totalCount = baseMapper.customGetTotalCount();
        
        // 构建返回结果
        QueryResult<Project> queryResult = new QueryResult<>();
        queryResult.setResultData(projectList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public QueryResult<Project> customGetPageListWithManager(Integer pageNum, Integer pageSize, String name, String clusterName) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 查询分页数据
        List<Project> projectList = baseMapper.customGetPageListWithManager(offset, pageSize, name, clusterName);
        
        // 查询总数
        int totalCount = baseMapper.customGetTotalCountWithManager(name, clusterName);
        
        // 构建返回结果
        QueryResult<Project> queryResult = new QueryResult<>();
        queryResult.setResultData(projectList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public Project getByIdWithManager(Integer id) {
        return baseMapper.getByIdWithManager(id);
    }

    @Override
    public QueryResult<Project> getCurrentUserProjectListWithManager(Long userId) {
        List<Project> projectList = baseMapper.getCurrentUserProjectListWithManager(userId);
        int totalCount = baseMapper.getCurrentUserProjectTotalCountWithManager(userId);
        QueryResult<Project> queryResult = new QueryResult<>();
        queryResult.setResultData(projectList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public QueryResult<Project> getAllProjectListWithManager() {
        List<Project> projectList = baseMapper.getAllProjectListWithManager();
        int totalCount = baseMapper.getAllProjectTotalCountWithManager();
        QueryResult<Project> queryResult = new QueryResult<>();
        queryResult.setResultData(projectList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    @Transactional
    public Project createProject(ProjectRequestDTO dto) {
        Project projectExisted = this.getByName(dto.getName());
        if(projectExisted != null){
            throw new BusinessException(LlmopsErrorEnum.CODE_1400, dto.getName());
        }
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescribe(dto.getDescribe());
        project.setType("org");
        project.setCreatedOn(LocalDateTime.now());
        project.setChangedOn(LocalDateTime.now());
        String expandJson = buildExpandJson(dto.getCluster(), dto.getVolumeMount(), dto.getServiceExternalIp(), dto.getOrg());
        project.setExpand(expandJson);
        save(project);
        if (dto.getProjectManager() != null) {
            ProManager proManager = new ProManager();
            proManager.setProjectId(project.getId());
            proManager.setProName(project.getName());
            proManager.setClusterName(dto.getCluster());
            proManager.setGpu(dto.getProjectManager().getGpu());
            proManager.setCpu(dto.getProjectManager().getCpu());
            proManager.setMemory(dto.getProjectManager().getMemory());
            proManager.setStorage(dto.getProjectManager().getStorage());
            proManager.setCreatetime(LocalDateTime.now());
            proManager.setUpdatetime(LocalDateTime.now());
            proManagerService.save(proManager);
            String namespaceName = project.getName();
            String clusterName = dto.getCluster();
            kubernetesResourceService.createNamespace(clusterName, namespaceName);
            kubernetesResourceService.createResourceQuota(
                    clusterName,
                    namespaceName,
                    proManager.getCpu(),
                    proManager.getMemory(),
                    proManager.getGpu(),
                    proManager.getStorage()
            );
        }

        return project;
    }

    @Override
    @Transactional
    public Project updateProject(ProjectRequestDTO dto) {
        Project project = getById(dto.getId());
        project.setDescribe(dto.getDescribe());
        project.setChangedOn(LocalDateTime.now());
        String expandJson = buildExpandJson(null, dto.getVolumeMount(), dto.getServiceExternalIp(), dto.getOrg());
        project.setExpand(expandJson);
        updateById(project);
        if (dto.getProjectManager() != null) {
            ProManager proManager = proManagerService.getById(dto.getProjectManager().getId());
            if (proManager != null) {
                proManager.setGpu(dto.getProjectManager().getGpu());
                proManager.setCpu(dto.getProjectManager().getCpu());
                proManager.setMemory(dto.getProjectManager().getMemory());
                proManager.setStorage(dto.getProjectManager().getStorage());
                proManager.setStatus(dto.getProjectManager().getStatus());
                proManager.setUpdatetime(LocalDateTime.now());
                proManagerService.updateById(proManager);
                String namespaceName = project.getName();
                kubernetesResourceService.createResourceQuota(
                        proManager.getClusterName(),
                        namespaceName,
                        proManager.getCpu(),
                        proManager.getMemory(),
                        proManager.getGpu(),
                        proManager.getStorage()
                );
            }
        }
        return project;
    }

    private String buildExpandJson(String cluster, String volumeMount, String serviceExternalIp, String org) {
        Map<String, String> expandMap = new HashMap<>();
        if (cluster != null) {
            expandMap.put("cluster", cluster);
        }
        if (volumeMount != null) {
            expandMap.put("volume_mount", volumeMount);
        }
        if (serviceExternalIp != null) {
            expandMap.put("SERVICE_EXTERNAL_IP", serviceExternalIp);
        }
        if (org != null) {
            expandMap.put("org", org);
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(expandMap);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Override
    public List<ProjectSimpleDTO> getJobTemplateProjects() {
        return baseMapper.getJobTemplateProjects();
    }

    @Override
    public Project getByName(String name) {
        if(projectCache.containsKey( name)) {
            return projectCache.get(name);
        }
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getName, name);
        Project project = getOne(wrapper);
        if(project==null) return null;
        projectCache.put(name, project);
        return project;
    }

    @Override
    public Map<String, Object> overView(String name) {
        Map<String,Object> result = new HashMap();
        ResourceQuotaDTO resourceQuota = openShiftClientService.getResourceQuota(name);
        Project project = this.getByName(name);
        result.put("resourceQuota", resourceQuota);
        int aiExperimentCount = aiExperimentMapper.customGetTotalCount(project.getId(),null,null,null);
        result.put("aiExperimentCount", aiExperimentCount);
        //workflowMapper.selectCount( null)
        return result;
    }

    @Override
    public Map<String, Integer> dataInfo(Integer projectId) {
        Map<String, Integer> result = new HashMap<>();
        Integer aiExperimentCount = aiExperimentService.getTotalCountByProjectId(projectId);
        Integer inferenceServiceCount = inferenceserviceService.getReadyOnlineCountByProjectId(projectId);
        Integer datasetCount = datasetService.getCountByProjectId(projectId);
        result.put("aiExperimentCount", aiExperimentCount);
        result.put("inferenceServiceCount", inferenceServiceCount);
        result.put("datasetCount", datasetCount);
        return result;
    }

    @Override
    public Map<String, Integer> index(String namespace) {
        ProManager proManager = proManagerService.getByName(namespace);
        Map<String, Integer> result = new HashMap<>();
        Integer projectId = Integer.valueOf(proManager.getProjectId().toString()) ;
        Integer aiExperimentCount = aiExperimentService.getTotalCountByProjectId(projectId);
        Map<String, Integer> inferenceServicePodCount = inferenceserviceService.getInferencePodCounts(proManager.getClusterName(),proManager.getProName());
        Integer taskPodCount = taskService.getTaskPodCount(proManager.getClusterName(),proManager.getProName());
        result.put("task",taskPodCount);
        result.putAll(inferenceServicePodCount);
        result.put("aiExperimentCount", aiExperimentCount);
        return result;
    }


}
