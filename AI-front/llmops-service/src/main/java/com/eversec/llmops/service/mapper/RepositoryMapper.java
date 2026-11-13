package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.dto.RepositoryNameDTO;
import com.eversec.llmops.service.entity.Repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RepositoryMapper extends BaseMapper<Repository> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param name 仓库名称
     * @param server 服务器地址
     * @param user 用户名
     * @param hubsecret 密钥
     * @return 列表
     */
    List<Repository> customGetPageList(
            @Param("offset") Integer offset, 
            @Param("limit") Integer limit,
            @Param("name") String name,
            @Param("server") String server,
            @Param("user") String user,
            @Param("hubsecret") String hubsecret
    );

    /**
     * 获取总数
     * @param name 仓库名称
     * @param server 服务器地址
     * @param user 用户名
     * @param hubsecret 密钥
     * @return 总数
     */
    int customGetTotalCount(
            @Param("name") String name,
            @Param("server") String server,
            @Param("user") String user,
            @Param("hubsecret") String hubsecret
    );

    /**
     * 查询所有仓库的id和name
     * @return 列表
     */
    List<RepositoryNameDTO> selectIdAndName();
}
