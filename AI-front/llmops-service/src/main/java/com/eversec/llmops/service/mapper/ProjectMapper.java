package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.dto.ProjectSimpleDTO;
import com.eversec.llmops.service.entity.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 列表
     */
    List<Project> customGetPageList(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 获取总数
     * @return 总数
     */
    int customGetTotalCount();

    /**
     * 关联分页查询（project 和 pro_manager）
     * @param offset 偏移量
     * @param limit 每页数量
     * @param name 项目名称
     * @param clusterName 集群名称
     * @return 列表
     */
    List<Project> customGetPageListWithManager(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("name") String name, @Param("clusterName") String clusterName);

    /**
     * 获取关联查询总数
     * @param name 项目名称
     * @param clusterName 集群名称
     * @return 总数
     */
    int customGetTotalCountWithManager(@Param("name") String name, @Param("clusterName") String clusterName);

    /**
     * 根据ID获取详情（包含项目管理器信息）
     * @param id ID
     * @return Project对象
     */
    Project getByIdWithManager(@Param("id") Integer id);

    /**
     * 获取当前用户项目列表（包含项目管理器信息）
     * @param userId 用户ID
     * @return 列表
     */
    List<Project> getCurrentUserProjectListWithManager(@Param("userId") Long userId);

    /**
     * 获取当前用户项目总数
     * @param userId 用户ID
     * @return 总数
     */
    int getCurrentUserProjectTotalCountWithManager(@Param("userId") Long userId);

    /**
     * 获取所有 clusterName 非空项目列表（包含项目管理器信息）
     * @return 列表
     */
    List<Project> getAllProjectListWithManager();

    /**
     * 获取所有 clusterName 非空项目总数
     * @return 总数
     */
    int getAllProjectTotalCountWithManager();

    /**
     * 获取type=job-template的项目列表，只返回id和name
     * @return 项目列表
     */
    List<ProjectSimpleDTO> getJobTemplateProjects();
}
