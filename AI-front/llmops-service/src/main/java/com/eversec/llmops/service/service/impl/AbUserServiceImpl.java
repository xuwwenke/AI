package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.service.entity.AbUser;
import com.eversec.llmops.service.entity.AbUserRole;
import com.eversec.llmops.service.entity.SysSecurityUserDO;
import com.eversec.llmops.service.mapper.AbUserMapper;
import com.eversec.llmops.service.service.AbUserRoleService;
import com.eversec.llmops.service.service.AbUserService;
import com.eversec.llmops.service.service.RedisService;
import com.eversec.llmops.service.service.SysSecurityUserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
@Service
public class AbUserServiceImpl extends ServiceImpl<AbUserMapper, AbUser> implements AbUserService {

    @Resource
    private SysSecurityUserService sysSecurityUserService;


    @Resource
    private RedisService redisService;
    @Resource
    private AbUserRoleService abUserRoleService;

    private static final String USER_REDIS_PREFIX = "ab_user_";

    private final String emailSuffix = "@eversec.cn";
    @Override
    public AbUser getByUsername(String username) {
        LambdaQueryWrapper<AbUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AbUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public Integer getUserIdByUsername(String username) {
        SysSecurityUserDO sysSecurityUserDO = sysSecurityUserService.getByUserName(username);
        Integer userId = redisService.getInt(USER_REDIS_PREFIX + username);
        if (userId != null) {
            return userId;
        }
        AbUser abUser = this.getByUsername(username);
        if(abUser != null){
            redisService.setInt(USER_REDIS_PREFIX + username, abUser.getId());
            return abUser.getId();
        }
        AbUser abUserToAdd = getAbUser(sysSecurityUserDO);
        redisService.setInt(USER_REDIS_PREFIX + username, abUserToAdd.getId());
        addRole(abUserToAdd);
        return abUserToAdd.getId();
    }

    private void addRole(AbUser abUserToAdd) {
        AbUserRole abUserRole = new AbUserRole();
        abUserRole.setUserId(abUserToAdd.getId());
        abUserRole.setRoleId(3);
        abUserRoleService.save(abUserRole);
    }

    @NotNull
    private AbUser getAbUser(SysSecurityUserDO sysSecurityUserDO) {
        AbUser abUserToAdd = new AbUser();
        abUserToAdd.setUsername(sysSecurityUserDO.getUserName());
        abUserToAdd.setLastName(sysSecurityUserDO.getUserName());
        abUserToAdd.setFirstName(sysSecurityUserDO.getUserName());
        abUserToAdd.setActive(true);
        if(sysSecurityUserDO.getRealName() != null){
            abUserToAdd.setNickname(sysSecurityUserDO.getRealName());
        }else {
            abUserToAdd.setNickname(sysSecurityUserDO.getUserName());
        }
        if(sysSecurityUserDO.getEmail()==null || sysSecurityUserDO.getEmail().isEmpty()){
            abUserToAdd.setEmail(sysSecurityUserDO.getUserName()+emailSuffix);
        }else{
            abUserToAdd.setEmail(sysSecurityUserDO.getEmail());
        }
        abUserToAdd.setPhone(sysSecurityUserDO.getTel());

        abUserToAdd.setChangedByFk(1);
        abUserToAdd.setCreatedByFk(1);
        this.save(abUserToAdd);
        return abUserToAdd;
    }
}
