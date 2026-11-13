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
 * 分片上传详情表
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-05-21
 */
@Getter
@Setter
@TableName("upload_parts")
public class UploadPart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的 uploadId
     */
    @TableField("upload_id")
    private String uploadId;

    /**
     * 分片编号（从1开始）
     */
    @TableField("part_number")
    private Integer partNumber;

    /**
     * 分片上传后返回的 ETag
     */
    @TableField("etag")
    private String etag;

    /**
     * 该分片实际大小（字节）
     */
    @TableField("part_size")
    private Long partSize;

    /**
     * 上传成功时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;
}
