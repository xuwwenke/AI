package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.service.dto.ProjectManagerDTO;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.mapper.ProManagerMapper;
import com.eversec.llmops.service.service.ProManagerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
@Service
public class ProManagerServiceImpl extends ServiceImpl<ProManagerMapper, ProManager> implements ProManagerService {

    private final Map<String, ProManager> proManagerCache = new ConcurrentHashMap<>();

    @Override
    public List<ProjectManagerDTO> getProjectManagersByUsername(String username) {
        return baseMapper.getProjectManagersByUsername(username);
    }

    @Override
    public List<ProjectManagerDTO> getProjectManagersByUserId(Long userId) {
        return baseMapper.getProjectManagersByUserId(userId);
    }

    @Override
    public List<ProjectManagerDTO> getAllProjectManagers() {
        LambdaQueryWrapper<ProManager> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(ProManager::getClusterName)
                .ne(ProManager::getClusterName, "");
        return list(wrapper).stream()
                .filter(item -> StringUtils.hasText(item.getClusterName()))
                .map(item -> {
                    ProjectManagerDTO dto = new ProjectManagerDTO();
                    dto.setId(item.getId());
                    dto.setName(item.getProName());
                    dto.setDesc(item.getDescZh());
                    dto.setShowName(item.getBz());
                    dto.setClusterName(item.getClusterName());
                    dto.setCreateTime(item.getCreatetime());
                    dto.setOperator(item.getOperator());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProManager getByName(String name) {
        if (proManagerCache.containsKey(name)) {
            return proManagerCache.get(name);
        }
        LambdaQueryWrapper<ProManager> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProManager::getProName, name);
        ProManager proManager = getOne(wrapper);
        if (proManager != null) {
            proManagerCache.put(name, proManager);
        }
        return proManager;
    }

    @Override
    public ProManager getByProjectId(Integer projectId) {
        if (projectId == null) {
            return null;
        }
        LambdaQueryWrapper<ProManager> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProManager::getProjectId, projectId);
        return getOne(wrapper);
    }
}
