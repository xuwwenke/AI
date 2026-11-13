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
 * @since 2026-04-28
 */
@Getter
@Setter
@TableName("cluster")
public class Cluster implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 集群名称
     */
    @TableField("name")
    private String name;

    /**
     * k8s https serverurl
     */
    @TableField("k8s_server_url")
    private String k8sServerUrl;

    /**
     * k8s token
     */
    @TableField("token")
    private String token;

    /**
     * 服务ip，用于拼接端口，供用户调用
     */
    @TableField("service_ip")
    private String serviceIp;

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
     * 描述
     */
    @TableField("description")
    private String description;
}
