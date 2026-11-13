package com.eversec.llmops.service.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.service.entity.SysSecurityUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * @Description 用户表-mapper类
 * @Author CodeGenerator
 * @Date 2022-04-30 10:14:19
 */
@Mapper
@DS("system")
public interface SysSecurityUserMapper extends BaseMapper<SysSecurityUserDO> {

    Integer countAdminRoleByUserId(Long userId);

}
