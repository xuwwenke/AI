package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.entity.Dataset;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.mapper.DatasetMapper;
import com.eversec.llmops.service.mapper.ProjectMapper;
import com.eversec.llmops.service.service.DatasetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eversec.llmops.service.service.FileDeleteRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements DatasetService {


    @Resource
    private FileDeleteRecordService fileDeleteRecordService;

    @Override
    public QueryResult<Dataset> getPageList(Integer pageNum, Integer pageSize) {
        Page<Dataset> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
        Page<Dataset> resultPage = page(page, wrapper);
        QueryResult<Dataset> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public QueryResult<Dataset> customGetPageList(Integer pageNum, Integer pageSize, String name, String version, String describe, Integer projectId) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 查询分页数据
        java.util.List<Dataset> datasetList = baseMapper.customGetPageList(offset, pageSize, name, version, describe, projectId);
        
        // 查询总数
        int totalCount = baseMapper.customGetTotalCount(name, version, describe, projectId);
        
        // 构建返回结果
        QueryResult<Dataset> queryResult = new QueryResult<>();
        queryResult.setResultData(datasetList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public Dataset getById(Integer id) {
        return baseMapper.getById(id);
    }

    @Override
    public Integer getCountByProjectId(Integer projectId) {
        return baseMapper.getCountByProjectId(projectId);
    }

    @Override
    @Transactional
    public Boolean delete(Integer id, String deletor) {
        Dataset dataset = this.getById(id);
        if (dataset != null && Integer.valueOf(1).equals(dataset.getProjectId())) {
            return false;
        }
        if(dataset.getPath()!=null && !dataset.getPath().isEmpty())  fileDeleteRecordService.deleteDataSet(dataset.getPath(),deletor);
        if(dataset.getIcon()!=null && !dataset.getIcon().isEmpty())  fileDeleteRecordService.deleteIcon(dataset.getIcon(),deletor);
        return this.removeById(id);
    }
}

