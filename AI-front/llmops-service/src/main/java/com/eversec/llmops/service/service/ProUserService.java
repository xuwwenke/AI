package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.dto.BatchProUserDTO;
import com.eversec.llmops.service.entity.ProUser;
import com.eversec.llmops.service.entity.SysSecurityUserDO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
public interface ProUserService extends IService<ProUser> {

    /**
     * 批量新增 ProUser
     * @param dto 批量 ProUser DTO
     * @return 新增的 ProUser 列表
     */
    List<ProUser> batchAdd( String namespace,BatchProUserDTO dto);

    /**
     * 根据项目 ID 批量删除 ProUser
     * @param proId 项目 ID
     * @return 删除的记录数
     */
    int deleteByProId(Integer proId);

    List<SysSecurityUserDO> getValidUsers();
}

