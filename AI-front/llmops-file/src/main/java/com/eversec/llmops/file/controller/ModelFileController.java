package com.eversec.llmops.file.controller;

import com.eversec.llmops.file.controller.req.CompleteUploadRequest;
import com.eversec.llmops.file.controller.req.PartCompleteRequest;
import com.eversec.llmops.file.entity.UploadPart;
import com.eversec.llmops.file.service.MultipartUploadService;
import com.eversec.llmops.file.service.UploadPartService;
import com.eversec.llmops.file.service.UploadTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Model 文件控制器
 * <p>
 * 业务逻辑：
 * - bucket 固定为 "model"
 * - project 从 header 中获取
 * - 文件路径结构: {project}/{uniquePrefix_originalFileName}
 * <p>
 * 当前同时处理上传与下载相关接口
 */
@RestController
@RequestMapping("/api/model")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ModelFileController {

    private static final String BUCKET_NAME = "model";
    private static final String HEADER_PROJECT = "X-Project-Name";

    @Autowired
    private MultipartUploadService multipartUploadService;

    @Autowired
    private UploadPartService uploadPartService;

    @Autowired
    private UploadTaskService uploadTaskService;

    /**
     * 构建 objectKey: {project}/{uniquePrefix_originalFileName}
     */
    private String buildObjectKey(String project, String fileName) {
        if (project == null || project.trim().isEmpty()) {
            project = "default";
        }
        // 清理 project 名称，防止路径遍历
        project = project.replaceAll("[^a-zA-Z0-9_\\-]", "");
        if (project.isEmpty()) {
            project = "default";
        }
        return project + "/" + generateUniqueFileName(fileName);
    }

    private String generateUniqueFileName(String fileName) {
        String normalizedFileName = (fileName == null || fileName.trim().isEmpty()) ? "file" : fileName.trim();
        normalizedFileName = normalizedFileName.replace("\\", "_").replace("/", "_");
        return UUID.randomUUID().toString().replace("-", "") + "_" + normalizedFileName;
    }

    /**
     * 1. 初始化分片上传
     * POST /api/model/upload/init?fileName=xxx
     */
    @PostMapping("/upload/init")
    public Map<String, String> initUpload(
            @RequestHeader(value = HEADER_PROJECT, required = false) String project,
            @RequestParam String fileName) {
        String objectKey = buildObjectKey(project, fileName);
        return multipartUploadService.initUpload(BUCKET_NAME, objectKey, fileName);
    }

    /**
     * 2. 获取单个分片的预签名上传 URL
     * GET /api/model/upload/presigned-url
     */
    @GetMapping("/upload/presigned-url")
    public Map<String, Object> getPresignedUrl(
            @RequestParam String objectKey,
            @RequestParam String uploadId,
            @RequestParam int partNumber) {
        return multipartUploadService.getPresignedUrl(BUCKET_NAME, objectKey, uploadId, partNumber);
    }

    /**
     * 3. 获取下载预签名 URL
     * GET /api/model/download/presigned-url
     */
    @GetMapping("/download/presigned-url")
    public Map<String, Object> getPresignedDownloadUrl(@RequestParam String objectKey) {
        return multipartUploadService.getProjectFIleDownloadPresignedUrl(BUCKET_NAME, objectKey,false);
    }


    /**
     * 3. 获取下载预签名 URL
     * GET /api/model/download/presigned-url
     */
    @GetMapping("/download/presigned-url-in-cluster")
    public Map<String, Object> getPresignedDownloadUrlInCluster(@RequestParam String objectKey) {
        return multipartUploadService.getDownloadPresignedUrl(BUCKET_NAME, objectKey,true);
    }
    /**
     * 4. 完成分片合并
     * POST /api/model/upload/complete
     */
    @PostMapping("/upload/complete")
    public Map<String, String> completeUpload(@RequestBody CompleteUploadRequest body) {
        return multipartUploadService.completeUpload(BUCKET_NAME, body);
    }

    /**
     * 5. 中止分片上传
     * DELETE /api/model/upload/abort
     */
    @DeleteMapping("/upload/abort")
    public Map<String, String> abortUpload(
            @RequestParam String objectKey,
            @RequestParam String uploadId) {
        return multipartUploadService.abortUpload(BUCKET_NAME, objectKey, uploadId);
    }

    @PostMapping("/upload/part-complete")
    public Map<String, String> recordPartComplete(@RequestBody PartCompleteRequest request) {
        UploadPart uploadPart = new UploadPart();
        uploadPart.setUploadId(request.getUploadId());
        uploadPart.setPartNumber(request.getPartNumber());
        uploadPart.setEtag(request.getEtag());
        uploadPart.setPartSize(request.getPartSize());
        uploadPartService.save(uploadPart);
        uploadTaskService.markPartUploaded(request.getUploadId());
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }
}
