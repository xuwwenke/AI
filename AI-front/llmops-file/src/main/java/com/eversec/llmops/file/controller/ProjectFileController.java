package com.eversec.llmops.file.controller;

import com.eversec.llmops.file.controller.req.CompleteUploadRequest;
import com.eversec.llmops.file.controller.req.PartCompleteRequest;
import com.eversec.llmops.file.entity.UploadPart;
import com.eversec.llmops.file.service.MultipartUploadService;
import com.eversec.llmops.file.service.UploadPartService;
import com.eversec.llmops.file.service.UploadTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Project 文件控制器
 * <p>
 * 业务逻辑：
 * - bucket 固定为 "project-file"
 * - project 从 header 中获取
 * - 文件路径结构: {project}/{uniquePrefix_originalFileName}
 * <p>
 * 当前同时处理上传与下载相关接口
 */
@RestController
@RequestMapping("/api/project-file")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectFileController {

    private static final String BUCKET_NAME = "project-file";
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
     * POST /api/project-file/upload/init?fileName=xxx
     */
    @PostMapping("/upload/init")
    public Map<String, String> initUpload(
            @RequestHeader(value = HEADER_PROJECT, required = false) String project,
            @RequestParam String fileName) {
        String objectKey = buildObjectKey(project, fileName);
        return multipartUploadService.initUpload(BUCKET_NAME, objectKey, fileName);
    }

    /**
     * 上传小文件（直接上传）
     * POST /api/project-file/upload/small
     */
    @PostMapping("/upload/small")
    public Map<String, String> uploadSmallFile(
            @RequestHeader(value = HEADER_PROJECT, required = false) String project,
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String objectKey = buildObjectKey(project, fileName);
        return multipartUploadService.uploadSmallFile(BUCKET_NAME, objectKey, file);
    }

    /**
     * 2. 获取单个分片的预签名上传 URL
     * GET /api/project-file/upload/presigned-url
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
     * GET /api/project-file/download/presigned-url
     */
    @GetMapping("/download/presigned-url")
    public Map<String, Object> getPresignedDownloadUrl(@RequestParam String objectKey) {
        return multipartUploadService.getDownloadPresignedUrl(BUCKET_NAME, objectKey,true);
    }

    /**
     * 4. 完成分片合并
     * POST /api/project-file/upload/complete
     */
    @PostMapping("/upload/complete")
    public Map<String, String> completeUpload(@RequestBody CompleteUploadRequest body) {
        return multipartUploadService.completeUpload(BUCKET_NAME, body);
    }

    /**
     * 5. 中止分片上传
     * DELETE /api/project-file/upload/abort
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
