package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.entity.Notebook;
import com.eversec.llmops.service.mapper.NotebookMapper;
import com.eversec.llmops.service.service.ClusterService;
import com.eversec.llmops.service.service.NotebookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotebookServiceImpl extends ServiceImpl<NotebookMapper, Notebook> implements NotebookService {

    @Autowired
    private ClusterService clusterService;

    @Override
    public QueryResult<Notebook> getPageList(Integer pageNum, Integer pageSize) {
        Page<Notebook> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notebook> wrapper = new LambdaQueryWrapper<>();
        Page<Notebook> resultPage = page(page, wrapper);
        QueryResult<Notebook> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public QueryResult<Notebook> customGetPageList(Integer pageNum, Integer pageSize, String namespace, String name, String status, String creator) {
        int offset = (pageNum - 1) * pageSize;
        
        List<Notebook> notebookList = baseMapper.customGetPageList(offset, pageSize, namespace, name, status, creator);
        
        String serviceIp = null;
        for (Notebook notebook : notebookList) {
            if (serviceIp == null && notebook.getClusterName() != null) {
                serviceIp = clusterService.getServiceIpByName(notebook.getClusterName());
            }
            if (serviceIp != null && notebook.getNamespace() != null && notebook.getName() != null) {
                Integer port = notebook.getId() * 10 + 10000;
                notebook.setIp("http://" + serviceIp + ":" + port + "/notebook/" + notebook.getNamespace() + "/" + notebook.getName() + "/lab");
            }
        }
        
        int totalCount = baseMapper.customGetTotalCount(namespace, name, status, creator);
        
        QueryResult<Notebook> queryResult = new QueryResult<>();
        queryResult.setResultData(notebookList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public Notebook getById(Integer id) {
        return baseMapper.getById(id);
    }
}