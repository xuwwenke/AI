package com.eversec.llmops.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.dto.JobTemplateDTO;
import com.eversec.llmops.service.entity.JobTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
@Mapper
public interface JobTemplateMapper extends BaseMapper<JobTemplate> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param creator 创建者
     * @param modifier 修改者
     * @param version 版本
     * @param imagesId 镜像ID
     * @param projectId 项目ID
     * @param bizProjectId 业务项目ID
     * @return 列表
     */
    List<JobTemplateDTO> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("creator") String creator,
            @Param("modifier") String modifier,
            @Param("version") String version,
            @Param("imagesId") Integer imagesId,
            @Param("projectId") Integer projectId,
            @Param("bizProjectId") Integer bizProjectId);

    /**
     * 获取总数
     * @param creator 创建者
     * @param modifier 修改者
     * @param version 版本
     * @param imagesId 镜像ID
     * @param projectId 项目ID
     * @param bizProjectId 业务项目ID
     * @return 总数
     */
    int customGetTotalCount(
            @Param("creator") String creator,
            @Param("modifier") String modifier,
            @Param("version") String version,
            @Param("imagesId") Integer imagesId,
            @Param("projectId") Integer projectId,
            @Param("bizProjectId") Integer bizProjectId);

    JobTemplate getDetailsById( @Param("id") Integer id);


    List<JobTemplateDTO> list(@Param("bizProjectId") Integer bizProjectId);
}
