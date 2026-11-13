package com.eversec.llmops.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.file.entity.UploadPart;
import com.eversec.llmops.file.entity.UploadTask;
import com.eversec.llmops.file.mapper.UploadTaskMapper;
import com.eversec.llmops.file.service.UploadPartService;
import com.eversec.llmops.file.service.UploadTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件上传任务表 服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-05-21
 */
@Service
public class UploadTaskServiceImpl extends ServiceImpl<UploadTaskMapper, UploadTask> implements UploadTaskService {

    private static final byte STATUS_INIT = 0;
    private static final byte STATUS_UPLOADING = 1;
    private static final byte STATUS_COMPLETED = 2;
    private static final byte STATUS_ABORTED = 3;

    @Resource
    private UploadPartService uploadPartService;

    @Override
    public UploadTask createUploadTask(String bucketName, String objectKey, String fileName, String uploadId,
                                       Long creatorId, Long fileSize, String mimeType, Integer totalParts) {
        LocalDateTime now = LocalDateTime.now();
        UploadTask uploadTask = new UploadTask();
        uploadTask.setBucket(bucketName);
        uploadTask.setObjectKey(objectKey);
        uploadTask.setFileName(fileName);
        uploadTask.setUploadId(uploadId);
        uploadTask.setCreatorId(creatorId);
        uploadTask.setFileSize(fileSize);
        uploadTask.setMimeType(mimeType);
        uploadTask.setTotalParts(totalParts);
        uploadTask.setUploadedParts(0);
        uploadTask.setStatus(STATUS_INIT);
        uploadTask.setCreateTime(now);
        uploadTask.setUpdateTime(now);
        save(uploadTask);
        return uploadTask;
    }

    @Override
    public void markPartUploaded(String uploadId) {
        UploadTask uploadTask = getByUploadId(uploadId);
        if (uploadTask == null) {
            return;
        }
        LambdaQueryWrapper<UploadPart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadPart::getUploadId, uploadId);
        java.util.List<UploadPart> uploadParts = uploadPartService.list(queryWrapper);
        int uploadedParts = uploadParts.size();
        long uploadedFileSize = 0L;
        for (UploadPart uploadPart : uploadParts) {
            if (uploadPart.getPartSize() != null) {
                uploadedFileSize += uploadPart.getPartSize();
            }
        }
        uploadTask.setUploadedParts(uploadedParts);
        uploadTask.setFileSize(uploadedFileSize);
        uploadTask.setStatus(STATUS_UPLOADING);
        uploadTask.setUpdateTime(LocalDateTime.now());
        updateById(uploadTask);
    }

    @Override
    public void markCompleted(String uploadId) {
        UploadTask uploadTask = getByUploadId(uploadId);
        if (uploadTask == null) {
            return;
        }
        LambdaQueryWrapper<UploadPart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadPart::getUploadId, uploadId);
        java.util.List<UploadPart> uploadParts = uploadPartService.list(queryWrapper);
        int uploadedParts = uploadParts.size();
        long fileSize = 0L;
        for (UploadPart uploadPart : uploadParts) {
            if (uploadPart.getPartSize() != null) {
                fileSize += uploadPart.getPartSize();
            }
        }
        LocalDateTime now = LocalDateTime.now();
        uploadTask.setUploadedParts(uploadedParts);
        if (uploadTask.getTotalParts() == null) {
            uploadTask.setTotalParts(uploadedParts);
        }
        if (fileSize > 0) {
            uploadTask.setFileSize(fileSize);
        }
        uploadTask.setStatus(STATUS_COMPLETED);
        uploadTask.setUpdateTime(now);
        uploadTask.setCompleteTime(now);
        updateById(uploadTask);
    }

    @Override
    public void markAborted(String uploadId) {
        UploadTask uploadTask = getByUploadId(uploadId);
        if (uploadTask == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        uploadTask.setStatus(STATUS_ABORTED);
        uploadTask.setUpdateTime(now);
        uploadTask.setCompleteTime(now);
        updateById(uploadTask);
    }

    @Override
    public UploadTask getByUploadId(String uploadId) {
        LambdaQueryWrapper<UploadTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadTask::getUploadId, uploadId).last("limit 1");
        return getOne(queryWrapper, false);
    }

    @Override
    public UploadTask getByBucketAndObjectKey(String bucketName, String objectKey) {
        LambdaQueryWrapper<UploadTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UploadTask::getBucket, bucketName)
                .eq(UploadTask::getObjectKey, objectKey)
                .last("limit 1");
        return getOne(queryWrapper, false);
    }
}
