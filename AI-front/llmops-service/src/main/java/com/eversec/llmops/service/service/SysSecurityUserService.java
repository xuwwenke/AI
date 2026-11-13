package com.eversec.llmops.service.service;

import com.eversec.llmops.service.entity.SysSecurityUserDO;

import java.util.List;

public interface SysSecurityUserService {
    List<SysSecurityUserDO> getValidUsers();

    SysSecurityUserDO getByUserName(String userName);


    Boolean ifAilabxAdmin(Long userId);
}
