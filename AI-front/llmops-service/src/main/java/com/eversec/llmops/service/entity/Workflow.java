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
 * @since 2026-04-21
 */
@Getter
@Setter
@TableName("workflow")
public class Workflow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("namespace")
    private String namespace;

    @TableField("create_time")
    private String createTime;

    @TableField("status")
    private String status;

    @TableField("annotations")
    private String annotations;

    @TableField("labels")
    private String labels;

    @TableField("spec")
    private String spec;

    @TableField("status_more")
    private String statusMore;

    @TableField("username")
    private String username;

    @TableField("info_json")
    private String infoJson;

    @TableField("add_row_time")
    private LocalDateTime addRowTime;

    @TableField("foreign_key")
    private String foreignKey;

    @TableField("change_time")
    private String changeTime;

    @TableField("cluster")
    private String cluster;

    @TableField(exist = false)
    private String phase;

    @TableField(exist = false)
    private Double runningHours;
}
