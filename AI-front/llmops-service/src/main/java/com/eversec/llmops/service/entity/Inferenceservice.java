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
@TableName("inferenceservice")
public class Inferenceservice implements Serializable {

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

    @TableField("label")
    private String label;

    @TableField("service_type")
    private String serviceType;

    @TableField("model_name")
    private String modelName;

    @TableField("model_version")
    private String modelVersion;

    @TableField("model_path")
    private String modelPath;

    @TableField("model_type")
    private String modelType;

    @TableField("model_input")
    private String modelInput;

    @TableField("model_output")
    private String modelOutput;

    @TableField("model_status")
    private String modelStatus;

    @TableField("transformer")
    private String transformer;

    @TableField("images")
    private String images;

    @TableField("working_dir")
    private String workingDir;

    @TableField("command")
    private String command;

    @TableField("args")
    private String args;

    @TableField("env")
    private String env;

    /**
     * 挂载
     */
    @TableField("volume_mount")
    private String volumeMount;

    @TableField("node_selector")
    private String nodeSelector;

    @TableField("min_replicas")
    private Integer minReplicas;

    @TableField("max_replicas")
    private Integer maxReplicas;

    @TableField("hpa")
    private String hpa;

    @TableField("metrics")
    private String metrics;

    @TableField("health")
    private String health;

    @TableField("sidecar")
    private String sidecar;

    @TableField("ports")
    private String ports;

    @TableField("resource_memory")
    private String resourceMemory;

    @TableField("resource_cpu")
    private String resourceCpu;

    @TableField("resource_gpu")
    private String resourceGpu;

    @TableField("deploy_time")
    private String deployTime;

    @TableField("host")
    private String host;

    @TableField("expand")
    private String expand;

    @TableField("canary")
    private String canary;

    @TableField("shadow")
    private String shadow;

    @TableField("run_id")
    private String runId;

    @TableField("run_time")
    private String runTime;

    @TableField("deploy_history")
    private String deployHistory;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    @TableField("inference_config")
    private String inferenceConfig;

    @TableField("priority")
    private Integer priority;

    /**
     * 定时伸缩
     */
    @TableField("cronhpa")
    private String cronhpa;

    /**
     * 命名空间
     */
    @TableField("namespace")
    private String namespace;


    @TableField(exist = false)
    private String ip;

    @TableField(exist = false)
    private String clusterName;

    @TableField(exist = false)
    private String proName;

    @TableField(exist = false)
    private String descZh;

    @TableField(exist = false)
    private String groupName;

    @TableField(exist = false)
    private String status;
}
