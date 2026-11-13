package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.dto.ProjectManagerDTO;
import com.eversec.llmops.service.entity.ProManager;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
public interface ProManagerService extends IService<ProManager> {

    List<ProjectManagerDTO> getProjectManagersByUsername(String username);

    List<ProjectManagerDTO> getProjectManagersByUserId(Long userId);

    List<ProjectManagerDTO> getAllProjectManagers();


    ProManager getByName(String name);

    /**
     * 根据项目ID获取 ProManager
     * @param projectId 项目ID
     * @return ProManager
     */
    ProManager getByProjectId(Integer projectId);
}
