package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * ProUser DTO
 *
 * @author NiYongZhe
 * @since 2026-04-07
 */
@Getter
@Setter
public class ProUserDTO {

    /**
     * 用户名称
     */
    private String username;

    /**
     * 角色,0:管理员，1:普通用户
     */
    private Integer isAdmin;

    /**
     * 用户ID
     */
    private Long userId;
}