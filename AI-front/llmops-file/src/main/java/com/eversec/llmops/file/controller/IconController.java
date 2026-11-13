package com.eversec.llmops.file.controller;

import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.file.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Icon 文件控制器
 * <p>
 * 业务逻辑：
 * - bucket 固定为 "icon"
 * - project 从 header 中获取
 * - 文件路径结构: {project}/{uniqueFileName}
 * - 文件较小，使用普通上传
 * - 为避免同名文件覆盖，上传前自动生成唯一文件名
 */
@RestController
@RequestMapping("/api/icon")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IconController {

    private static final String BUCKET_NAME = "icon";
    private static final String HEADER_PROJECT = "X-Project-Name";

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.publicEndpoint}")
    private String minioPublicEndpoint;


    @Autowired
    private MinioService minioService;

    private String normalizeProject(String project) {
        if (project == null || project.trim().isEmpty()) {
            project = "default";
        }
        project = project.replaceAll("[^a-zA-Z0-9_\\-]", "");
        if (project.isEmpty()) {
            project = "default";
        }
        return project;
    }

    private String generateUniqueFileName(String fileName) {
        String extension = "";
        if (fileName != null) {
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > -1 && lastDotIndex < fileName.length() - 1) {
                extension = fileName.substring(lastDotIndex);
            }
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 构建 objectKey: {project}/{uniqueFileName}
     */
    private String buildObjectKey(String project, String fileName) {
        return normalizeProject(project) + "/" + generateUniqueFileName(fileName);
    }

    private String buildPublicUrl(String objectKey) {
        String endpoint = minioPublicEndpoint.endsWith("/") ? minioPublicEndpoint.substring(0, minioPublicEndpoint.length() - 1) : minioPublicEndpoint;
        return endpoint + "/" + BUCKET_NAME + "/" + objectKey;
    }

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public RestResponse<Map<String, String>> upload(
            @RequestHeader(value = HEADER_PROJECT, required = false) String project,
            @RequestParam("file") MultipartFile file) {
        return ResponseHolder.build(() -> {
            String objectKey = buildObjectKey(project, file.getOriginalFilename());
            String storedObjectKey = minioService.uploadFileWithObjectKey(file, BUCKET_NAME, objectKey);
            Map<String, String> result = new HashMap<>();
            result.put("objectKey", storedObjectKey);
            result.put("fileName", storedObjectKey.substring(storedObjectKey.lastIndexOf('/') + 1));
            result.put("url", buildPublicUrl(storedObjectKey));
            return result;
        });
    }

    /**
     * 获取可直接访问的图片 URL
     * GET /api/icon/url
     */
    @GetMapping("/url")
    public RestResponse<Map<String, String>> getUrl(@RequestParam String objectKey) {
        return ResponseHolder.build(() -> {
            Map<String, String> result = new HashMap<>();
            result.put("objectKey", objectKey);
            result.put("url", buildPublicUrl(objectKey));
            return result;
        });
    }
}
