package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.entity.Docker;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DockerMapper extends BaseMapper<Docker> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param namespace 项目名称
     * @param creatorName 创建者名称
     * @return 列表
     */
    List<Docker> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("namespace") String namespace,
            @Param("creatorName") String creatorName
    );

    /**
     * 获取总数
     * @param namespace 项目名称
     * @param creatorName 创建者名称
     * @return 总数
     */
    int customGetTotalCount(
            @Param("namespace") String namespace,
            @Param("creatorName") String creatorName
    );

    /**
     * 根据ID获取详情
     * @param id ID
     * @return Docker对象
     */
    Docker getById(@Param("id") Integer id);
}
