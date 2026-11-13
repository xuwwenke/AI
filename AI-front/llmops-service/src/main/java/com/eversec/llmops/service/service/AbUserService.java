package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.entity.AbUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
public interface AbUserService extends IService<AbUser> {

    AbUser getByUsername(String username);


    Integer getUserIdByUsername(String username);
}
