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
 * @since 2026-04-15
 */
@Getter
@Setter
@TableName("ai_experiment")
public class AiExperiment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("created_on")
    private LocalDateTime createdOn;

    @TableField("changed_on")
    private LocalDateTime changedOn;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 中文名
     */
    @TableField("name")
    private String name;

    @TableField("created_by_fk")
    private Integer createdByFk;

    @TableField("changed_by_fk")
    private Integer changedByFk;

    @TableField("project_id")
    private Integer projectId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    //0:未开始 1:运行中 2:已完成 3:失败
    @TableField("status")
    private Integer status;

    @TableField("model_name")
    private String modelName;

    @TableField("config")
    private String config;

    @TableField("`desc`")
    private String desc;

    @TableField(exist = false)
    private String creator;

    @TableField(exist = false)
    private String modifier;
}
