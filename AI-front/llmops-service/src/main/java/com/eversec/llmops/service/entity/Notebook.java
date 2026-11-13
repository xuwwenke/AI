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
@TableName("notebook")
public class Notebook implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("project_id")
    private Integer projectId;

    @TableField("name")
    private String name;

    /**
     * 描述
     */
    @TableField("describe")
    private String describe;

    @TableField("namespace")
    private String namespace;

    /**
     * 镜像
     */
    @TableField("images")
    private String images;

    /**
     * ide类型
     */
    @TableField("ide_type")
    private String ideType;

    @TableField("working_dir")
    private String workingDir;

    @TableField("volume_mount")
    private String volumeMount;

    @TableField("node_selector")
    private String nodeSelector;

    @TableField("image_pull_policy")
    private String imagePullPolicy;

    @TableField("resource_memory")
    private String resourceMemory;

    @TableField("resource_cpu")
    private String resourceCpu;

    @TableField("resource_gpu")
    private String resourceGpu;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    @TableField("expand")
    private String expand;

    @TableField("env")
    private String env;

    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private String creator;

    @TableField(exist = false)
    private String clusterName;

    @TableField(exist = false)
    private String ip;
}
