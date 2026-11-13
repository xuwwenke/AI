package com.eversec.llmops.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 异步创建服务任务表
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-04
 */
@Getter
@Setter
@TableName("ide_service_task")
public class IdeServiceTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID，通常为UUID
     */
    @TableId("id")
    private String id;

    /**
     * 用户标识
     */
    @TableField("user_name")
    private String userName;

    /**
     * Kubernetes命名空间
     */
    @TableField("namespace")
    private String namespace;

    /**
     * 任务状态：CREATING, SUCCESS, FAILED
     */
    @TableField("status")
    private String status;

    /**
     * 失败时的错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 底层服务或K8s返回的结果链接/标识
     */
    @TableField("user_ide_link")
    private String userIdeLink;

    /**
     * PVC名称
     */
    @TableField("storage_name")
    private String storageName;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
