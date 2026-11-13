package com.eversec.llmops.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * @Description 用户表-实体类
 * @Author CodeGenerator
 * @Date 2022-04-30 10:14:19
 */
@Data
@Accessors(chain = true)
@TableName("sys_security_user")
@Schema(description = "用户表")
public class SysSecurityUserDO {

	private static final long serialVersionUID = 1L;

    @Schema
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_name")
    @Schema(description = "用户名（登录账号）")
    private String userName;

    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @TableField("email")
    @Schema(description = "电子邮箱(也用于邮箱登录的账号）")
    private String email;

    @TableField("tel")
    @Schema(description = "联系电话(也用于手机号登录的账号）")
    private String tel;

    @TableField("is_valid")
    @Schema(description = "用户可用状态（0:可用 ，1：禁用，3：注销）")
    private Integer isValid;

}
