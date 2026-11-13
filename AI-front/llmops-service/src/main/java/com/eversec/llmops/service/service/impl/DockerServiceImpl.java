package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.entity.Docker;
import com.eversec.llmops.service.mapper.DockerMapper;
import com.eversec.llmops.service.service.DockerService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DockerServiceImpl extends ServiceImpl<DockerMapper, Docker> implements DockerService {

    @Override
    public QueryResult<Docker> getPageList(Integer pageNum, Integer pageSize) {
        Page<Docker> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Docker> wrapper = new LambdaQueryWrapper<>();
        Page<Docker> resultPage = page(page, wrapper);
        QueryResult<Docker> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public QueryResult<Docker> customGetPageList(Integer pageNum, Integer pageSize, String namespace, String creatorName) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 查询分页数据
        List<Docker> dockerList = baseMapper.customGetPageList(offset, pageSize, namespace, creatorName);
        
        // 查询总数
        int totalCount = baseMapper.customGetTotalCount(namespace, creatorName);
        
        // 构建返回结果
        QueryResult<Docker> queryResult = new QueryResult<>();
        queryResult.setResultData(dockerList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public Docker getById(Integer id) {
        return baseMapper.getById(id);
    }
}
