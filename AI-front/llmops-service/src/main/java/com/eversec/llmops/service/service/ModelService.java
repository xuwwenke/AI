package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.ModelStatsDTO;
import com.eversec.llmops.service.entity.Model;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;

public interface ModelService extends IService<Model> {
    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    QueryResult<Model> getPageList(Integer pageNum, Integer pageSize);

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param namespace 项目名称
     * @param name 模型名称
     * @param version 版本
     * @param framework 框架
     * @param path 路径
     * @return QueryResult
     */
    QueryResult<Model> customGetPageList(Integer pageNum, Integer pageSize, String namespace, String name, String version, String framework, String path);

    ModelStatsDTO getStats(String namespace, Integer projectId);

    Model getById(Integer id);


    void delete(Integer id);

    int getTotalCount(String namespace);
}
