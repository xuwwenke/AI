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
@TableName("docker")
public class Docker implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("describe")
    private String describe;

    @TableField("base_image")
    private String baseImage;

    @TableField("target_image")
    private String targetImage;

    @TableField("consecutive_build")
    private Boolean consecutiveBuild;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    @TableField("expand")
    private String expand;

    @TableField("last_image")
    private String lastImage;

    @TableField("need_gpu")
    private Boolean needGpu;

    @TableField("project_id")
    private Integer projectId;

    /**
     * 命名空间
     */
    @TableField("namespace")
    private String namespace;

    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private String creatorName;
} 
