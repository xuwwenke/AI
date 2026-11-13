package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.RepositoryNameDTO;
import com.eversec.llmops.service.entity.Repository;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;

import java.util.List;

public interface RepositoryService extends IService<Repository> {
    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    QueryResult<Repository> getPageList(Integer pageNum, Integer pageSize);

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 仓库名称
     * @param server 服务器地址
     * @param user 用户名
     * @param hubsecret 密钥
     * @return QueryResult
     */
    QueryResult<Repository> customGetPageList(Integer pageNum, Integer pageSize, String name, String server, String user, String hubsecret);

    /**
     * 查询所有仓库的id和name
     * @return 列表
     */
    List<RepositoryNameDTO> getRepositoryNames();
}
