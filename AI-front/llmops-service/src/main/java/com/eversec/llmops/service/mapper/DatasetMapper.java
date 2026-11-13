package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.entity.Dataset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DatasetMapper extends BaseMapper<Dataset> {
    /**
     * 自定义分页查询数据集
     * @param offset 偏移量
     * @param limit 每页数量
     * @param name 数据集名称
     * @param version 数据集版本
     * @param describe 数据集描述
     * @param projectId 项目id
     * @return 数据集列表
     */
    List<Dataset> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("name") String name,
            @Param("version") String version,
            @Param("describe") String describe,
            @Param("projectId") Integer projectId
    );

    /**
     * 获取数据集总数
     * @param name 数据集名称
     * @param version 数据集版本
     * @param describe 数据集描述
     * @param projectId 项目id
     * @return 数据集总数
     */
    int customGetTotalCount(
            @Param("name") String name,
            @Param("version") String version,
            @Param("describe") String describe,
            @Param("projectId") Integer projectId
    );

    Dataset getById(Integer id);

    /**
     * 通过projectId获取dataset数量
     * @param projectId 项目id
     * @return 数量
     */
    Integer getCountByProjectId(@Param("projectId") Integer projectId);
}


