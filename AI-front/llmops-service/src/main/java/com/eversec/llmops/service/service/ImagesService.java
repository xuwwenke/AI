package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.ImageNameDTO;
import com.eversec.llmops.service.entity.Images;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;

import java.util.List;

public interface ImagesService extends IService<Images> {
    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
    QueryResult<Images> getPageList(Integer pageNum, Integer pageSize);

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 镜像名称
     * @param creator 创建者名称
     * @param describe 描述
     * @param projectId 项目id
     * @return QueryResult
     */
    QueryResult<Images> customGetPageList(Integer pageNum, Integer pageSize, String name, String creator, String describe, Integer projectId);


    Images getById(Integer id);

    List<ImageNameDTO> getImageNamesByNamespace(String namespace);

    List<ImageNameDTO> getImageNamesToSelect(Integer projectId);
}
