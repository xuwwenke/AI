package com.eversec.llmops.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.eversec.llmops.file.entity.FileDeleteRecord;
import com.eversec.llmops.file.mapper.FileDeleteRecordMapper;
import com.eversec.llmops.file.service.FileDeleteRecordService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 文件删除任务表 服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-01
 */
@Slf4j
@Service
public class FileDeleteRecordServiceImpl extends ServiceImpl<FileDeleteRecordMapper, FileDeleteRecord> implements FileDeleteRecordService {

    private final byte toDeleteStatus = 0;
    private final byte deletedStatus = 1;
    private final byte invalidStatus = 3;

    @Autowired
    private MinioClient minioClient;



    /**
     * 执行文件删除任务
     * 1. 查询所有 status=0 的记录
     * 2. 检查文件是否存在
     * 3. 如果存在则删除文件
     * 4. 删除成功后更新 status=1
     */
    @Override
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void executeDelete() {
        // 查询 status=0 的所有记录
        LambdaQueryWrapper<FileDeleteRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileDeleteRecord::getStatus, toDeleteStatus);
        List<FileDeleteRecord> records = this.list(queryWrapper);

        if (records == null || records.isEmpty()) {
            log.info("没有待删除的文件记录");
            return;
        }

        log.info("开始处理 {} 条待删除文件记录", records.size());
        int successCount = 0;
        int failCount = 0;

        for (FileDeleteRecord record : records) {
            try {
                String bucket = record.getBucket();
                String objectKey = record.getObjectKey();

                if (bucket == null || objectKey == null) {
                    log.warn("记录 id={} 的 bucket 或 objectKey 为空，删除该记录", record.getId());
                    this.removeById(record.getId());
                    failCount++;
                    continue;
                }

                // 检查文件是否存在
                boolean fileExists = checkFileExists(bucket, objectKey);
                if (fileExists) {
                    // 删除文件
                    deleteFileFromMinio(bucket, objectKey);
                    log.info("文件删除成功：bucket={}, objectKey={}", bucket, objectKey);
                } else {
                    log.info("文件不存在：bucket={}, objectKey={}", bucket, objectKey);
                }
                // 更新状态为 1（已删除）
                record.setStatus(deletedStatus);
                record.setDeleteTime(LocalDateTime.now());
                this.updateById(record);
                successCount++;

            } catch (Exception e) {
                log.error("删除文件失败：id={}, error={}", record.getId(), e.getMessage(), e);
                // 更新状态为 3（失败）
                record.setStatus(invalidStatus);
                record.setLastError(e.getMessage());
                this.updateById(record);
                failCount++;
            }
        }

        log.info("文件删除任务完成，成功：{}，失败：{}", successCount, failCount);
    }

    /**
     * 检查文件是否存在
     */
    private boolean checkFileExists(String bucket, String objectKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InternalException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException {
        StatObjectResponse statObjectResponse = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );
        return statObjectResponse != null;

    }

    /**
     * 从 MinIO 删除文件
     */
    private void deleteFileFromMinio(String bucket, String objectKey) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );
    }

}
