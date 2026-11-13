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
@TableName("images")
public class Images implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("project_id")
    private Integer projectId;

    /**
     * 英文名
     */
    @TableField("name")
    private String name;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    @TableField("repository_id")
    private Integer repositoryId;

    @TableField("entrypoint")
    private String entrypoint;

    @TableField("dockerfile")
    private String dockerfile;

    @TableField("gitpath")
    private String gitpath;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;


    @TableField("biz_project_id")
    private Integer bizProjectId;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String modifierName;
}
