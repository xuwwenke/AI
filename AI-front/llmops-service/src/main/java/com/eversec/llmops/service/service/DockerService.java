package com.eversec.llmops.service.service;

import com.eversec.llmops.service.entity.Docker;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;

public interface DockerService extends IService<Docker> {
    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    QueryResult<Docker> getPageList(Integer pageNum, Integer pageSize);

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param namespace 项目名称
     * @param creatorName 创建者名称
     * @return QueryResult
     */
    QueryResult<Docker> customGetPageList(Integer pageNum, Integer pageSize, String namespace, String creatorName);


    Docker getById(Integer id);
}
