package com.eversec.llmops.file.controller;

import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.file.entity.UploadStatus;
import com.eversec.llmops.file.service.AsyncMinioService;
import com.eversec.llmops.file.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/minio")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MinioController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private AsyncMinioService asyncMinioService;



    @GetMapping("/test")
    @Operation(summary = "测试接口")
    public RestResponse<Map<String, String>> test() {
        log.info("测试接口被调用");
        return ResponseHolder.build(() -> {
            Map<String, String> result = new HashMap<>();
            result.put("message", "OK");
            return result;
        });
    }

    @PostMapping("/upload")
    @Operation(summary = "普通文件上传")
    public RestResponse<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("bucketName") String bucketName) {
        log.info("收到普通文件上传请求, bucket: {}, filename: {}", bucketName, file.getOriginalFilename());
        return ResponseHolder.build(() -> {
            String fileName = minioService.uploadFile(file, bucketName);
            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            return result;
        });
    }

    @PostMapping("/upload-large")
    @Operation(summary = "大文件上传（稳定版）")
    public RestResponse<Map<String, String>> uploadLargeFile(@RequestParam("file") MultipartFile file,
                                                              @RequestParam("bucketName") String bucketName) {
        log.info("收到大文件上传请求, bucket: {}, filename: {}, size: {}", bucketName, file.getOriginalFilename(), file.getSize());
        return ResponseHolder.build(() -> {
            String fileName = minioService.uploadLargeFile(file, bucketName);
            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            return result;
        });
    }

    @PostMapping("/upload-async")
    @Operation(summary = "异步文件上传（带进度）")
    public RestResponse<Map<String, String>> uploadAsync(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("bucketName") String bucketName)  {
        log.info("收到异步文件上传请求, bucket: {}, filename: {}, size: {}", bucketName, file.getOriginalFilename(), file.getSize());
        return ResponseHolder.build(() -> {
            // 1. 初始化上传
            UploadStatus initStatus = asyncMinioService.initUpload(bucketName, file.getOriginalFilename(), file.getSize());
            String uploadId = initStatus.getUploadId();

            // 2. 先保存到本地
            String localPath = null;
            try {
                localPath = asyncMinioService.saveToLocal(file, uploadId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.info("文件已保存到本地: {}", localPath);

            // 3. 更新状态为保存中
            UploadStatus savingStatus = asyncMinioService.getUploadStatus(uploadId);
            savingStatus.setStatus("SAVING");
            savingStatus.setMessage("正在保存到 MinIO 存储...");
            savingStatus.setUploadedSize(file.getSize());
            savingStatus.setProgress(50);

            // 4. 异步上传到 MinIO
            asyncMinioService.uploadToMinioAsync(uploadId, localPath, bucketName, file.getOriginalFilename());

            Map<String, String> result = new HashMap<>();
            result.put("uploadId", uploadId);
            return result;
        });
    }

    @GetMapping("/status/{uploadId}")
    @Operation(summary = "查询上传进度")
    public RestResponse<UploadStatus> getUploadStatus(@PathVariable("uploadId") String uploadId) {
        return ResponseHolder.build(() -> {
            UploadStatus status = asyncMinioService.getUploadStatus(uploadId);
            if (status == null) {
                throw new RuntimeException("上传任务不存在: " + uploadId);
            }
            return status;
        });
    }


}
