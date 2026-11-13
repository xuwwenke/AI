package com.eversec.llmops.file.controller.req;

import lombok.Data;

/**
 * 分片上传完成请求
 */
@Data
public class PartCompleteRequest {

    /**
     * 上传任务 ID（MinIO 返回的 uploadId）
     */
    private String uploadId;

    /**
     * 分片编号（从 1 开始）
     */
    private Integer partNumber;

    /**
     * MinIO 返回的 ETag
     */
    private String etag;

    /**
     * 该分片的大小（字节）
     */
    private Long partSize;
}
