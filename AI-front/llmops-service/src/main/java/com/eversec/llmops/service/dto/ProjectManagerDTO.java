package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ProjectManager DTO
 *
 * @author NiYongZhe
 * @since 2026-04-07
 */
@Getter
@Setter
public class ProjectManagerDTO {

    /**
     * pro_manager.id
     */
    private Long id;

    /**
     * pro_manager.pro_name
     */
    private String name;

    /**
     * pro_manager.desc_zh
     */
    private String desc;

    /**
     * pro_manager.bz
     */
    private String showName;

    /**
     * pro_manager.cluster_name
     */
    private String clusterName;

    /**
     * pro_manager.createtime
     */
    private LocalDateTime createTime;

    /**
     * pro_manager.operator
     */
    private String operator;

    /**
     * pro_user.is_admin
     */
    private Integer role;
}