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
@TableName("model")
public class Model implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("version")
    private String version;

    @TableField("describe")
    private String describe;

    @TableField("path")
    private String path;

    @TableField("download_url")
    private String downloadUrl;

    @TableField("project_id")
    private Integer projectId;

    @TableField("pipeline_id")
    private Integer pipelineId;

    @TableField("run_id")
    private String runId;

    @TableField("run_time")
    private String runTime;

    @TableField("framework")
    private String framework;

    @TableField("metrics")
    private String metrics;

    @TableField("md5")
    private String md5;

    @TableField("api_type")
    private String apiType;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    /**
     * 扩展参数
     */
    @TableField("expand")
    private String expand;
}
