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
 * @since 2026-03-20
 */
@Getter
@Setter
@TableName("project")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    @TableField("type")
    private String type;

    @TableField("expand")
    private String expand;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    /**
     * 项目管理器
     */
    @TableField(exist = false)
    private ProjectManager projectManager;

    /**
     * 用户数量
     */
    @TableField(exist = false)
    private Integer userCount;

    /**
     * AI实验数量
     */
    @TableField(exist = false)
    private Integer experimentCount;
}
