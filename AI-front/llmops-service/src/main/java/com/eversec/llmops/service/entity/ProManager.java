package com.eversec.llmops.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

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
@TableName("pro_manager")
public class ProManager implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * project_id
     */
    @TableField("project_id")
    private Integer projectId;

    /**
     * 项目名称
     */
    @TableField("pro_name")
    private String proName;

    /**
     * 项目类型，1：虚拟机，2：容器服务
     */
    @TableField("pro_type")
    private Integer proType;

    /**
     * 备注信息
     */
    @TableField("bz")
    private String bz;

    /**
     * 状态，0：进行中，1：完成，2：暂停
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("createtime")
    private LocalDateTime createtime;

    /**
     * 修改时间
     */
    @TableField("updatetime")
    private LocalDateTime updatetime;

    /**
     * 操作人
     */
    @TableField("operator")
    private String operator;

    /**
     * gpu
     */
    @TableField("gpu")
    private Integer gpu;

    /**
     * CPU
     */
    @TableField("cpu")
    private Integer cpu;

    /**
     * 内存
     */
    @TableField("memory")
    private Integer memory;

    /**
     * 存储
     */
    @TableField("storage")
    private Integer storage;

    /**
     * 集群名称
     */
    @TableField("cluster_name")
    private String clusterName;

    /**
     * 项目中文名
     */
    @TableField("desc_zh")
    private String descZh;

    /**
     * 项目分组名称
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 所属节点分组名称
     */
    @TableField("node_group_name")
    private String nodeGroupName;
}
