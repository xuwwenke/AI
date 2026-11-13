package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.entity.Notebook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotebookMapper extends BaseMapper<Notebook> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param namespace 项目名称
     * @param name 名称
     * @param status 状态
     * @param creator 创建者
     * @return 列表
     */
    List<Notebook> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("namespace") String namespace,
            @Param("name") String name,
            @Param("status") String status,
            @Param("creator") String creator
    );

    /**
     * 获取总数
     * @param namespace 项目名称
     * @param name 名称
     * @param status 状态
     * @param creator 创建者
     * @return 总数
     */
    int customGetTotalCount(
            @Param("namespace") String namespace,
            @Param("name") String name,
            @Param("status") String status,
            @Param("creator") String creator
    );

    Notebook getById(@Param("id") Integer id);
}
