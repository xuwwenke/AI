package com.eversec.llmops.file.entity;


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
 * 文件删除任务表
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-01
 */
@Getter
@Setter
@TableName("file_delete_record")
public class FileDeleteRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 存储桶名称
     */
    @TableField("bucket")
    private String bucket;

    /**
     * 对象键（文件路径）
     */
    @TableField("object_key")
    private String objectKey;

    /**
     * 状态：0-待删除，1-删除中，2-已完成，3-失败
     */
    @TableField("status")
    private Byte status;

    /**
     * 实际删除完成时间（状态为2时记录）
     */
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 记录插入时间（即删除任务创建时间）
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 执行删除操作的标识（程序名/实例ID）
     */
    @TableField("deletor")
    private String deletor;

    /**
     * 重试次数（用于失败状态）
     */
    @TableField("retry_count")
    private Byte retryCount;

    /**
     * 最后一次错误信息
     */
    @TableField("last_error")
    private String lastError;
}
