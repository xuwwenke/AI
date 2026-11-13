package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.RepositoryNameDTO;
import com.eversec.llmops.service.entity.Repository;
import com.eversec.llmops.service.mapper.RepositoryMapper;
import com.eversec.llmops.service.service.RepositoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, Repository> implements RepositoryService {

    @Override
    public QueryResult<Repository> getPageList(Integer pageNum, Integer pageSize) {
        Page<Repository> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Repository> wrapper = new LambdaQueryWrapper<>();
        Page<Repository> resultPage = page(page, wrapper);
        QueryResult<Repository> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public QueryResult<Repository> customGetPageList(Integer pageNum, Integer pageSize, String name, String server, String user, String hubsecret) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 查询分页数据
        List<Repository> repositoryList = baseMapper.customGetPageList(offset, pageSize, name, server, user, hubsecret);
        
        // 查询总数
        int totalCount = baseMapper.customGetTotalCount(name, server, user, hubsecret);
        
        // 构建返回结果
        QueryResult<Repository> queryResult = new QueryResult<>();
        queryResult.setResultData(repositoryList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public List<RepositoryNameDTO> getRepositoryNames() {
        return baseMapper.selectIdAndName();
    }
}
