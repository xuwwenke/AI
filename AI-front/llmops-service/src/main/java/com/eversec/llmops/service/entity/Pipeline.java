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
@TableName("pipeline")
public class Pipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("`describe`")
    private String describe;

    @TableField("project_id")
    private Integer projectId;

    @TableField("dag_json")
    private String dagJson;

    @TableField("namespace")
    private String namespace;

    @TableField("global_env")
    private String globalEnv;

    @TableField("schedule_type")
    private String scheduleType;

    @TableField("cron_time")
    private String cronTime;

    @TableField("pipeline_file")
    private String pipelineFile;

    @TableField("pipeline_argo_id")
    private String pipelineArgoId;

    @TableField("version_id")
    private String versionId;

    @TableField("run_id")
    private String runId;

    @TableField("node_selector")
    private String nodeSelector;

    @TableField("image_pull_policy")
    private String imagePullPolicy;

    @TableField("parallelism")
    private Integer parallelism;

    @TableField("alert_status")
    private String alertStatus;

    @TableField("alert_user")
    private String alertUser;

    @TableField("expand")
    private String expand;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    @TableField("depends_on_past")
    private Boolean dependsOnPast;

    @TableField("max_active_runs")
    private Integer maxActiveRuns;

    @TableField("parameter")
    private String parameter;

    @TableField("expired_limit")
    private Integer expiredLimit;

    @TableField("cronjob_start_time")
    private String cronjobStartTime;

    /**
     * 优先级
     */
    @TableField("priority")
    private String priority;

    /**
     * 任务流类型
     */
    @TableField("type")
    private String type;


    @TableField("ai_experiment_id")
    private Integer aiExperimentId;

    @TableField(exist = false)
    private String project;

    @TableField(exist = false)
    private String creator;

    @TableField(exist = false)
    private String version;

    @TableField(exist = false)
    private String modified;
}
