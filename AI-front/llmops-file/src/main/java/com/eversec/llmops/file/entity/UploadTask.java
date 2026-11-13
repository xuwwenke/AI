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
 * 文件上传任务表
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-05-21
 */
@Getter
@Setter
@TableName("upload_tasks")
public class UploadTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * MinIO 对象键（含路径）
     */
    @TableField("object_key")
    private String objectKey;

    /**
     * 上传者用户ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * MinIO 桶名
     */
    @TableField("bucket")
    private String bucket;

    /**
     * 原始文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件总大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * MIME 类型
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * MinIO 分片上传 uploadId
     */
    @TableField("upload_id")
    private String uploadId;

    /**
     * 总分片数
     */
    @TableField("total_parts")
    private Integer totalParts;

    /**
     * 已完成分片数
     */
    @TableField("uploaded_parts")
    private Integer uploadedParts;

    /**
     * 状态：0-初始化 1-上传中 2-已完成 3-已中止
     */
    @TableField("status")
    private Byte status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 完成/中止时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;
}
