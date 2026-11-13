package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.dto.ImageNameDTO;
import com.eversec.llmops.service.entity.Images;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImagesMapper extends BaseMapper<Images> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param name 镜像名称
     * @param creator 创建者名称
     * @param describe 描述
     * @param projectId 项目id
     * @return 列表
     */
    List<Images> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("name") String name,
            @Param("creator") String creator,
            @Param("describe") String describe,
            @Param("projectId") Integer projectId
    );

    /**
     * 获取总数
     * @param name 镜像名称
     * @param creator 创建者名称
     * @param describe 描述
     * @param projectId 项目id
     * @return 总数
     */
    int customGetTotalCount(
            @Param("name") String name,
            @Param("creator") String creator,
            @Param("describe") String describe,
            @Param("projectId") Integer projectId
    );


    Images getById(@Param("id") Integer id);

    List<ImageNameDTO> getImageNamesByNamespace(@Param("namespace") String namespace);


    List<ImageNameDTO> getImageNamesToSelect(@Param("projectId") Integer projectId);
}
