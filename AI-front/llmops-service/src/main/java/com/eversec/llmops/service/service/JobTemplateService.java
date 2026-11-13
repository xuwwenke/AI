package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.dto.JobTemplateDTO;
import com.eversec.llmops.service.entity.JobTemplate;
import com.eversec.framework.core.data.QueryResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
public interface JobTemplateService extends IService<JobTemplate> {
    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param creator 创建者
     * @param modifier 修改者
     * @param version 版本
     * @param imagesId 镜像ID
     * @param projectId 项目ID
     * @param bizProjectId 业务项目ID
     * @return QueryResult
     */
    QueryResult<JobTemplateDTO> customGetPageList(Integer pageNum, Integer pageSize, String creator, String modifier, String version, Integer imagesId, Integer projectId, Integer bizProjectId);

    JobTemplate getDetailsById(Integer id);



    QueryResult<JobTemplateDTO> list(Integer projectId);
}
