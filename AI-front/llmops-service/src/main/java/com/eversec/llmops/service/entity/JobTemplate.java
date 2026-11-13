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
@TableName("job_template")
public class JobTemplate implements Serializable {

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

    @TableField("version")
    private String version;

    @TableField("images_id")
    private Integer imagesId;

    /**
     * 域名映射
     */
    @TableField("host_aliases")
    private String hostAliases;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    @TableField("workdir")
    private String workdir;

    @TableField("entrypoint")
    private String entrypoint;

    @TableField("args")
    private String args;

    @TableField("env")
    private String env;

    /**
     * 强制必须挂载
     */
    @TableField("volume_mount")
    private String volumeMount;

    @TableField("privileged")
    private Boolean privileged;

    @TableField("accounts")
    private String accounts;

    @TableField("demo")
    private String demo;

    @TableField("expand")
    private String expand;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;


    @TableField("biz_project_id")
    private Integer bizProjectId;

    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private String image;


    @TableField(exist = false)
    private String project;
}
