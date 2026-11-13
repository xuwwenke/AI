package com.eversec.llmops.file.service;

import com.eversec.llmops.file.entity.UploadStatus;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AsyncMinioService {

    @Autowired
    private MinioClient minioClient;

    private static final long PART_SIZE = 5 * 1024 * 1024; // 5MB

    private final Map<String, UploadStatus> uploadStatusMap = new ConcurrentHashMap<>();

    public UploadStatus initUpload(String bucketName, String fileName, long fileSize) {
        String uploadId = UUID.randomUUID().toString();
        UploadStatus status = new UploadStatus();
        status.setUploadId(uploadId);
        status.setFileName(fileName);
        status.setBucketName(bucketName);
        status.setTotalSize(fileSize);
        status.setUploadedSize(0L);
        status.setProgress(0);
        status.setStatus("UPLOADING");
        status.setMessage("正在上传到服务器...");
        status.setStartTime(System.currentTimeMillis());
        uploadStatusMap.put(uploadId, status);
        return status;
    }

    public UploadStatus getUploadStatus(String uploadId) {
        return uploadStatusMap.get(uploadId);
    }

    public String saveToLocal(MultipartFile file, String uploadId) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String localFileName = uploadId + "_" + file.getOriginalFilename();
        Path localPath = Paths.get(tempDir, localFileName);
        Files.copy(file.getInputStream(), localPath);
        return localPath.toString();
    }

    @Async
    public void uploadToMinioAsync(String uploadId, String localFilePath, String bucketName, String originalFileName) {
        UploadStatus status = uploadStatusMap.get(uploadId);
        if (status == null) {
            return;
        }

        try {
            // 更新状态为保存中
            status.setStatus("SAVING");
            status.setMessage("正在保存到 MinIO 存储...");
            status.setProgress(50);
            uploadStatusMap.put(uploadId, status);

            // 启动进度模拟线程
            Thread progressThread = new Thread(() -> {
                try {
                    for (int i = 55; i < 100; i += 5) {
                        UploadStatus currentStatus = uploadStatusMap.get(uploadId);
                        if (currentStatus == null || !"SAVING".equals(currentStatus.getStatus())) {
                            break;
                        }
                        currentStatus.setProgress(i);
                        uploadStatusMap.put(uploadId, currentStatus);
                        Thread.sleep(200); // 每200ms增加5%进度
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            progressThread.start();

            // 检查桶是否存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 生成唯一文件名
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String minioFileName = UUID.randomUUID().toString() + extension;

            // 上传文件
            File localFile = new File(localFilePath);
            long fileSize = localFile.length();

            try (FileInputStream fis = new FileInputStream(localFile)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(minioFileName)
                                .stream(fis, fileSize, PART_SIZE)
                                .contentType("application/octet-stream")
                                .build()
                );
            }

            // 等待进度线程完成，确保进度至少到100%
            progressThread.interrupt();
            try {
                progressThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 更新进度到100%
            status.setUploadedSize(fileSize);
            status.setProgress(100);
            status.setMinioFileName(minioFileName);
            status.setStatus("COMPLETED");
            status.setMessage("上传成功！");
            status.setEndTime(System.currentTimeMillis());
            uploadStatusMap.put(uploadId, status);

            // 删除本地临时文件
            try {
                Files.deleteIfExists(Paths.get(localFilePath));
            } catch (Exception e) {
                log.warn("删除临时文件失败: {}", localFilePath, e);
            }

        } catch (Exception e) {
            log.error("上传到 MinIO 失败: {}", uploadId, e);
            status.setStatus("FAILED");
            status.setMessage("上传失败: " + e.getMessage());
            uploadStatusMap.put(uploadId, status);
        }
    }
}
