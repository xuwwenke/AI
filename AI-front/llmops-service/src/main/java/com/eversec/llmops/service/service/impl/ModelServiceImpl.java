package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.ModelStatsDTO;
import com.eversec.llmops.service.entity.Model;
import com.eversec.llmops.service.mapper.ModelMapper;
import com.eversec.llmops.service.service.FileDeleteRecordService;
import com.eversec.llmops.service.service.InferenceserviceService;
import com.eversec.llmops.service.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {

    @Autowired
    private InferenceserviceService inferenceserviceService;

    @Autowired
    private FileDeleteRecordService fileDeleteRecordService;

    @Override
    public QueryResult<Model> getPageList(Integer pageNum, Integer pageSize) {
        Page<Model> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        Page<Model> resultPage = page(page, wrapper);
        QueryResult<Model> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public QueryResult<Model> customGetPageList(Integer pageNum, Integer pageSize, String namespace, String name, String version, String framework, String path) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 查询分页数据
        List<Model> modelList = baseMapper.customGetPageList(offset, pageSize, namespace, name, version, framework, path);
        
        // 查询总数
        int totalCount = baseMapper.customGetTotalCount(namespace, name, version, framework, path);
        
        // 构建返回结果
        QueryResult<Model> queryResult = new QueryResult<>();
        queryResult.setResultData(modelList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public ModelStatsDTO getStats(String namespace, Integer projectId) {
        ModelStatsDTO stats = baseMapper.getStats(namespace);
        if (stats == null) {
            stats = new ModelStatsDTO();
            stats.setTotalCount(0);
            stats.setTypeCount(0);
        }
        stats.setOnlineCount(inferenceserviceService.getOnlineCountByProjectId(projectId));
        return stats;
    }

    @Override
    public Model getById(Integer id) {
        return baseMapper.getById(id);
    }

    @Override
    public void delete(Integer id) {
        Model model = this.getById(id);
        fileDeleteRecordService.deleteModel(model.getPath(),null);
        this.removeById(id);
    }

    @Override
    public int getTotalCount(String namespace) {
        return  baseMapper.customGetTotalCount(namespace, null, null, null, null);
    }
}
