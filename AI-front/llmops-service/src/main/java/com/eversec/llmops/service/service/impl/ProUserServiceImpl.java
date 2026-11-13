package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.service.dto.BatchProUserDTO;
import com.eversec.llmops.service.dto.ProUserDTO;
import com.eversec.llmops.service.entity.ProUser;
import com.eversec.llmops.service.entity.Project;
import com.eversec.llmops.service.entity.SysSecurityUserDO;
import com.eversec.llmops.service.mapper.ProUserMapper;
import com.eversec.llmops.service.mapper.SysSecurityUserMapper;
import com.eversec.llmops.service.service.IdeServiceTaskService;
import com.eversec.llmops.service.service.ProUserService;
import com.eversec.llmops.service.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
@Service
public class ProUserServiceImpl extends ServiceImpl<ProUserMapper, ProUser> implements ProUserService {


    @Resource
    private SysSecurityUserMapper sysSecurityUserMapper;


    @Resource
    private IdeServiceTaskService ideServiceTaskService;


    @Override
    @Transactional
    public List<ProUser> batchAdd(String namespace,BatchProUserDTO dto) {
        LambdaQueryWrapper<ProUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProUser::getProId, dto.getProId());
        List<ProUser> existingList = list(queryWrapper);
        List<ProUserDTO> proUserDTOList = dto.getProUserList();
        LocalDateTime now = LocalDateTime.now();
        Map<String, ProUser> existingMap = new HashMap<>();
        for (ProUser existing : existingList) {
            existingMap.put(buildCompareKey(existing.getUserId(), existing.getUsername()), existing);
        }
        List<String> deleteUserNames = new ArrayList<>();
        List<ProUser> insertList = new ArrayList<>();
        List<Long> deleteIds = new ArrayList<>();
        dealData(dto, proUserDTOList, deleteUserNames, existingList, deleteIds, existingMap, now, insertList);
        if (!deleteIds.isEmpty()) {
            ideServiceTaskService.deleteIdeServiceTask(namespace,deleteUserNames);
        }
        if (!insertList.isEmpty()) {
            ideServiceTaskService.saveIdeServiceTask(namespace,insertList);
        }
        deleteByProId(dto.getProId());
        List<ProUser> proUserList = new ArrayList<>();
        if (proUserDTOList != null && !proUserDTOList.isEmpty()) {
            for (ProUserDTO proUserDTO : proUserDTOList) {
                ProUser proUser = new ProUser();
                proUser.setProId(dto.getProId());
                proUser.setUsername(proUserDTO.getUsername());
                proUser.setIsAdmin(proUserDTO.getIsAdmin());
                proUser.setUserId(proUserDTO.getUserId());
                proUser.setCreatetime(now);
                proUser.setUpdatetime(now);
                proUserList.add(proUser);
            }
            saveBatch(proUserList);
        }
        return proUserList;
    }


    private void dealData(BatchProUserDTO dto, List<ProUserDTO> proUserDTOList, List<String> deleteUserNames, List<ProUser> existingList, List<Long> deleteIds, Map<String, ProUser> existingMap, LocalDateTime now, List<ProUser> insertList) {
        Map<String, ProUserDTO> incomingMap = new HashMap<>();
        if (proUserDTOList != null) {
            for (ProUserDTO proUserDTO : proUserDTOList) {
                incomingMap.put(buildCompareKey(proUserDTO.getUserId(), proUserDTO.getUsername()), proUserDTO);
            }
        }
        for (ProUser existing : existingList) {
            String key = buildCompareKey(existing.getUserId(), existing.getUsername());
            if (!incomingMap.containsKey(key) && existing.getId() != null) {
                deleteIds.add(existing.getId());
                deleteUserNames.add(existing.getUsername());
            }
        }
        for (Map.Entry<String, ProUserDTO> entry : incomingMap.entrySet()) {
            ProUserDTO proUserDTO = entry.getValue();
            ProUser existing = existingMap.get(entry.getKey());
            if (existing == null) {
                ProUser proUser = new ProUser();
                proUser.setUsername(proUserDTO.getUsername());
                insertList.add(proUser);
            }
        }
    }

    @Override
    public int deleteByProId(Integer proId) {
        return baseMapper.deleteByProId(proId);
    }

    @Override
    public List<SysSecurityUserDO> getValidUsers() {
        LambdaQueryWrapper<SysSecurityUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSecurityUserDO::getIsValid, 0);
        List<SysSecurityUserDO> sysSecurityUserDOS = sysSecurityUserMapper.selectList(queryWrapper);
        return sysSecurityUserDOS;
    }

    private String buildCompareKey(Long userId, String username) {
        if (userId != null) {
            return "userId:" + userId;
        }
        if (StringUtils.hasText(username)) {
            return "username:" + username;
        }
        return "empty";
    }

    private boolean equalsNullable(Object left, Object right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}

