package com.eversec.llmops.service.service;

import com.eversec.llmops.service.entity.Notebook;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;

public interface NotebookService extends IService<Notebook> {
    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    QueryResult<Notebook> getPageList(Integer pageNum, Integer pageSize);

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param namespace 项目名称
     * @param name 名称
     * @param status 状态
     * @param creator 创建者
     * @return QueryResult
     */
    QueryResult<Notebook> customGetPageList(Integer pageNum, Integer pageSize, String namespace, String name, String status, String creator);


    Notebook getById(Integer id);
}
