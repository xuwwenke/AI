package com.eversec.llmops.service.dto;

import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.entity.ProjectManager;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目和项目管理器DTO
 *
 * @author NiYongZhe
 * @since 2026-04-07
 */
@Getter
@Setter
public class ProjectWithManagerDTO {

    /**
     * 项目信息
     */
    private Project project;

    /**
     * 项目管理器信息
     */
    private ProjectManager projectManager;
}