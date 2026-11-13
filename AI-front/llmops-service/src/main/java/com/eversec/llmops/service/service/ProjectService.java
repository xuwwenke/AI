package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.ProjectRequestDTO;
import com.eversec.llmops.service.dto.ProjectSimpleDTO;
import com.eversec.llmops.service.entity.Project;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;

import java.util.List;
import java.util.Map;

public interface ProjectService extends IService<Project> {
    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    QueryResult<Project> getPageList(Integer pageNum, Integer pageSize);

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    QueryResult<Project> customGetPageList(Integer pageNum, Integer pageSize);

    /**
     * 关联分页查询（project 和 pro_manager）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 项目名称
     * @param clusterName 集群名称
     * @return QueryResult
     */
    QueryResult<Project> customGetPageListWithManager(Integer pageNum, Integer pageSize, String name, String clusterName);

    /**
     * 根据ID获取详情（包含项目管理器信息）
     * @param id ID
     * @return Project对象
     */
    Project getByIdWithManager(Integer id);

    /**
     * 获取当前用户可访问项目（包含项目管理器信息）
     * @param userId 用户ID
     * @return QueryResult
     */
    QueryResult<Project> getCurrentUserProjectListWithManager(Long userId);

    /**
     * 获取所有可选项目（clusterName 非空，包含项目管理器信息）
     * @return QueryResult
     */
    QueryResult<Project> getAllProjectListWithManager();

    /**
     * 新增项目，同时新增ProManager
     * @param dto ProjectRequestDTO对象
     * @return Project对象
     */
    Project createProject(ProjectRequestDTO dto);

    /**
     * 修改项目，同时修改ProManager
     * @param dto ProjectRequestDTO对象
     * @return Project对象
     */
    Project updateProject(ProjectRequestDTO dto);

    /**
     * 获取type=job-template的项目列表，只返回id和name
     * @return 项目列表
     */
    List<ProjectSimpleDTO> getJobTemplateProjects();

    /**
     * 根据名称查询项目
     * @param name 项目名称
     * @return Project对象
     */
    Project getByName(String name);

    Map<String,Object> overView(String name);
    Map<String,Integer> dataInfo(Integer projectId);


    Map<String,Integer> index(String namespace);
}
