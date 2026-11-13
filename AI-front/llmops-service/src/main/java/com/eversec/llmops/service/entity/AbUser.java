package com.eversec.llmops.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
@Getter
@Setter
@TableName("ab_user")
public class AbUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("first_name")
    private String firstName;

    @TableField("last_name")
    private String lastName;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("active")
    private Boolean active;

    @TableField("email")
    private String email;

    @TableField("last_login")
    private LocalDateTime lastLogin;

    @TableField("login_count")
    private Integer loginCount;

    @TableField("fail_login_count")
    private Integer failLoginCount;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    @TableField("org")
    private String org;

    @TableField("quota")
    private String quota;

    @TableField("balance")
    private String balance;

    @TableField("wechat")
    private String wechat;

    @TableField("phone")
    private String phone;

    @TableField("coupon")
    private String coupon;

    @TableField("voucher")
    private String voucher;

    @TableField("bill")
    private String bill;

    @TableField("real_name_authentication")
    private String realNameAuthentication;

    @TableField("subaccount")
    private String subaccount;

    @TableField("nickname")
    private String nickname;
}
