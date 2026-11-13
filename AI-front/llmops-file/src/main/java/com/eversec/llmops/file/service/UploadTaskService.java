package com.eversec.llmops.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.file.entity.UploadTask;

/**
 * <p>
 * 文件上传任务表 服务类
 * </p>

 * @author NiYongZhe
 * @since 2026-05-21
 */
public interface UploadTaskService extends IService<UploadTask> {
    UploadTask createUploadTask(String bucketName, String objectKey, String fileName, String uploadId,
                                Long creatorId, Long fileSize, String mimeType, Integer totalParts);

    void markPartUploaded(String uploadId);

    void markCompleted(String uploadId);

    void markAborted(String uploadId);

    UploadTask getByUploadId(String uploadId);

    UploadTask getByBucketAndObjectKey(String bucketName, String objectKey);
}
