package com.eversec.llmops.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.dto.ProjectManagerDTO;
import com.eversec.llmops.service.entity.ProUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
@Mapper
public interface ProUserMapper extends BaseMapper<ProUser> {

    /**
     * 根据项目 ID 批量删除 ProUser
     * @param proId 项目 ID
     * @return 删除的记录数
     */
    int deleteByProId(@Param("proId") Integer proId);


}

