package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.dto.ModelStatsDTO;
import com.eversec.llmops.service.entity.Model;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ModelMapper extends BaseMapper<Model> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param namespace 项目名称
     * @param name 模型名称
     * @param version 版本
     * @param framework 框架
     * @param path 路径
     * @return 列表
     */
    List<Model> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("namespace") String namespace,
            @Param("name") String name,
            @Param("version") String version,
            @Param("framework") String framework,
            @Param("path") String path
    );

    /**
     * 获取总数
     * @param namespace 项目名称
     * @param name 模型名称
     * @param version 版本
     * @param framework 框架
     * @param path 路径
     * @return 总数
     */
    int customGetTotalCount(
            @Param("namespace") String namespace,
            @Param("name") String name,
            @Param("version") String version,
            @Param("framework") String framework,
            @Param("path") String path
    );

    ModelStatsDTO getStats(@Param("namespace") String namespace);


    Model getById(@Param("id")Integer id);
}
