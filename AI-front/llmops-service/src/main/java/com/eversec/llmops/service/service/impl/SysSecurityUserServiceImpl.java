package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eversec.llmops.service.entity.SysSecurityUserDO;
import com.eversec.llmops.service.mapper.SysSecurityUserMapper;
import com.eversec.llmops.service.service.SysSecurityUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysSecurityUserServiceImpl implements SysSecurityUserService {

    @Resource
    private SysSecurityUserMapper sysSecurityUserMapper;
    @Override
    public List<SysSecurityUserDO> getValidUsers() {
        LambdaQueryWrapper<SysSecurityUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSecurityUserDO::getIsValid, 0);
        List<SysSecurityUserDO> sysSecurityUserDOS = sysSecurityUserMapper.selectList(queryWrapper);
        return sysSecurityUserDOS;
    }

    @Override
    public SysSecurityUserDO getByUserName(String userName) {
        if (userName == null || userName.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SysSecurityUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSecurityUserDO::getUserName, userName);
        queryWrapper.eq(SysSecurityUserDO::getIsValid, 0);
        return sysSecurityUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Boolean ifAilabxAdmin(Long userId) {
        return sysSecurityUserMapper.countAdminRoleByUserId(userId)>0;
    }
}
