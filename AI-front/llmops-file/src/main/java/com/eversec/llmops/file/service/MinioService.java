package com.eversec.llmops.file.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    // 分片大小：5MB
    private static final long PART_SIZE = 5 * 1024 * 1024;

    private void putObject(MultipartFile file, String bucketName, String objectName, long partSize) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), partSize)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    /**
     * 普通文件上传
     */
    public String uploadFile(MultipartFile file, String bucketName) {
        try {
            // 检查桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;

            // 上传文件
            putObject(file, bucketName, fileName, -1);

            return fileName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 大文件上传（稳定版）
     * 使用分片上传，支持大文件，更稳定
     */
    public String uploadLargeFile(MultipartFile file, String bucketName) {
        try {
            log.info("开始上传大文件，文件名：{}，文件大小：{} bytes", file.getOriginalFilename(), file.getSize());
            
            // 检查桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + extension;

            long startTime = System.currentTimeMillis();
            
            // 使用分片上传，指定分片大小
            putObject(file, bucketName, fileName, PART_SIZE);

            long endTime = System.currentTimeMillis();
            log.info("大文件上传完成，耗时：{} ms，文件名：{}", (endTime - startTime), fileName);

            return fileName;
        } catch (Exception e) {
            log.error("大文件上传失败，文件名：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("大文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用指定 objectKey 上传小文件
     */
    public String uploadFileWithObjectKey(MultipartFile file, String bucketName, String objectKey) {
        try {
            putObject(file, bucketName, objectKey, -1);
            return objectKey;
        } catch (Exception e) {
            log.error("文件上传失败, bucket: {}, objectKey: {}", bucketName, objectKey, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
}
