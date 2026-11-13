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
 * @since 2026-04-03
 */
@Getter
@Setter
@TableName("repository")
public class Repository implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 英文名
     */
    @TableField("name")
    private String name;

    /**
     * 仓库地址
     */
    @TableField("server")
    private String server;

    @TableField("user")
    private String user;

    @TableField("password")
    private String password;

    @TableField("hubsecret")
    private String hubsecret;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;
}
