package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.entity.Dataset;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-02
 */
public interface DatasetService extends IService<Dataset> {

    QueryResult<Dataset> getPageList(Integer pageNum, Integer pageSize);

    /**
     * 自定义分页查询数据集（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 数据集名称
     * @param version 数据集版本
     * @param describe 数据集描述
     * @param projectId 项目id
     * @return 包含数据集列表和总数的QueryResult
     */
    QueryResult<Dataset> customGetPageList(Integer pageNum, Integer pageSize, String name, String version, String describe, Integer projectId);


    Dataset getById(Integer id);

    /**
     * 通过projectId获取dataset数量
     * @param projectId 项目id
     * @return 数量
     */
    Integer getCountByProjectId(Integer projectId);



    Boolean delete(Integer datasetId,String deletor);
}
