package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 批量 ProUser DTO
 *
 * @author NiYongZhe
 * @since 2026-04-07
 */
@Getter
@Setter
public class BatchProUserDTO {

    /**
     * 项目ID
     */
    private Integer proId;

    /**
     * ProUser 列表
     */
    private List<ProUserDTO> proUserList;
}