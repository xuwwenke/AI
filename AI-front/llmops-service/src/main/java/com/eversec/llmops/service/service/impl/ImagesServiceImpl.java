package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.ImageNameDTO;
import com.eversec.llmops.service.entity.Images;
import com.eversec.llmops.service.mapper.ImagesMapper;
import com.eversec.llmops.service.service.ImagesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImagesServiceImpl extends ServiceImpl<ImagesMapper, Images> implements ImagesService {

    @Override
    public QueryResult<Images> getPageList(Integer pageNum, Integer pageSize) {
        Page<Images> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Images> wrapper = new LambdaQueryWrapper<>();
        Page<Images> resultPage = page(page, wrapper);
        QueryResult<Images> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public QueryResult<Images> customGetPageList(Integer pageNum, Integer pageSize, String name, String creator, String describe, Integer projectId) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 查询分页数据
        List<Images> imagesList = baseMapper.customGetPageList(offset, pageSize, name, creator, describe, projectId);
        
        // 查询总数
        int totalCount = baseMapper.customGetTotalCount(name, creator, describe, projectId);
        
        // 构建返回结果
        QueryResult<Images> queryResult = new QueryResult<>();
        queryResult.setResultData(imagesList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public Images getById(Integer id) {
        return baseMapper.getById(id);
    }

    @Override
    public List<ImageNameDTO> getImageNamesByNamespace(String namespace) {
        return baseMapper.getImageNamesByNamespace(namespace);
    }

    @Override
    public List<ImageNameDTO> getImageNamesToSelect(Integer projectId) {
        return baseMapper.getImageNamesToSelect(projectId);
    }
}
