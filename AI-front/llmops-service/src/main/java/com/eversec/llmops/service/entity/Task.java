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
 * @since 2026-04-13
 */
@Getter
@Setter
@TableName("task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("label")
    private String label;

    @TableField("job_template_id")
    private Integer jobTemplateId;

    @TableField("pipeline_id")
    private Integer pipelineId;

    @TableField("working_dir")
    private String workingDir;

    @TableField("command")
    private String command;

    @TableField("overwrite_entrypoint")
    private Boolean overwriteEntrypoint;

    @TableField("args")
    private String args;

    /**
     * 挂载
     */
    @TableField("volume_mount")
    private String volumeMount;

    @TableField("node_selector")
    private String nodeSelector;

    @TableField("resource_memory")
    private String resourceMemory;

    @TableField("resource_cpu")
    private String resourceCpu;

    @TableField("resource_gpu")
    private String resourceGpu;

    @TableField("timeout")
    private Integer timeout;

    @TableField("retry")
    private Integer retry;

    @TableField("outputs")
    private String outputs;

    @TableField("monitoring")
    private String monitoring;

    @TableField("expand")
    private String expand;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    @TableField("skip")
    private Boolean skip;

    /**
     * rdma的资源数量
     */
    @TableField("resource_rdma")
    private String resourceRdma;

    /**
     * 命名空间
     */
    @TableField("namespace")
    private String namespace;
}
