package com.eversec.llmops.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.dto.ProjectManagerDTO;
import com.eversec.llmops.service.entity.ProManager;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
public interface ProManagerMapper extends BaseMapper<ProManager> {

    List<ProjectManagerDTO> getProjectManagersByUsername(String username);

    List<ProjectManagerDTO> getProjectManagersByUserId(Long userId);
}
