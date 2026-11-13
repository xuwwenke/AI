package com.eversec.llmops.file.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.eversec.llmops.file.config.MinioConfig;
import com.eversec.llmops.file.controller.req.CompleteUploadRequest;
import com.eversec.llmops.file.entity.UploadTask;
import com.eversec.llmops.file.service.UploadTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分片上传服务
 * <p>
 * 封装 Multipart Upload 的核心逻辑，供多个 Controller 复用
 */
@Service
public class MultipartUploadService {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private UploadTaskService uploadTaskService;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 初始化分片上传
     *
     * @param bucketName bucket 名称
     * @param objectKey  对象 key
     * @return 包含 uploadId 的响应
     */
    public Map<String, String> initUpload(String bucketName, String objectKey, String fileName) {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
        InitiateMultipartUploadResult result = s3Client.initiateMultipartUpload(request);
        UploadTask uploadTask = uploadTaskService.createUploadTask(
                bucketName,
                objectKey,
                fileName,
                result.getUploadId(),
                null,
                null,
                null,
                null);

        Map<String, String> response = new HashMap<>();
        response.put("uploadId", result.getUploadId());
        response.put("objectKey", objectKey);
        response.put("bucket", bucketName);
        response.put("taskId", uploadTask.getId() == null ? "" : String.valueOf(uploadTask.getId()));
        return response;
    }

    /**
     * 上传小文件（直接上传）
     *
     * @param bucketName bucket 名称
     * @param objectKey  对象 key
     * @param file       文件对象
     * @return 包含对象 key 的响应
     */
    public Map<String, String> uploadSmallFile(String bucketName, String objectKey, MultipartFile file) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        s3Client.putObject(new PutObjectRequest(bucketName, objectKey, file.getInputStream(), metadata));
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("bucket", bucketName);
        response.put("objectKey", objectKey);
        return response;
    }

    /**
     * 获取分片预签名 URL
     *
     * @param bucketName bucket 名称
     * @param objectKey  对象 key
     * @param uploadId   上传 ID
     * @param partNumber 分片序号
     * @return 包含预签名 URL 的响应
     */
    public Map<String, Object> getPresignedUrl(String bucketName, String objectKey,
                                                String uploadId, int partNumber) {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);
        request.addRequestParameter("uploadId", uploadId);
        request.addRequestParameter("partNumber", String.valueOf(partNumber));
        URL url = s3Client.generatePresignedUrl(request);
        String urlStr =  url.toString();
        urlStr = urlStr.replace(minioConfig.getEndpoint(),"");
        Map<String, Object> response = new HashMap<>();
        response.put("url", urlStr);
        response.put("partNumber", partNumber);
        return response;
    }

    /**
     * 获取下载预签名 URL
     *
     * @param bucketName bucket 名称
     * @param objectKey  对象 key
     * @return 包含下载预签名 URL 的响应
     */
    public Map<String, Object> getDownloadPresignedUrl(String bucketName, String objectKey,Boolean ifInCluster) {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        UploadTask uploadTask = uploadTaskService.getByBucketAndObjectKey(bucketName, objectKey);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        if (uploadTask != null && uploadTask.getFileName() != null && !uploadTask.getFileName().trim().isEmpty()) {
            ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
            responseHeaders.setContentDisposition(buildContentDisposition(uploadTask.getFileName()));
            request.setResponseHeaders(responseHeaders);
        }
        URL url = s3Client.generatePresignedUrl(request);
        String urlStr =  url.toString();
        if(!ifInCluster) urlStr = urlStr.replace(minioConfig.getEndpoint(),"");
        Map<String, Object> response = new HashMap<>();
        response.put("url", urlStr);
        response.put("objectKey", objectKey);
        if (uploadTask != null) {
            response.put("fileName", uploadTask.getFileName());
        }
        return response;
    }


    /**
     * 获取下载预签名 URL
     *
     * @param bucketName bucket 名称
     * @param objectKey  对象 key
     * @return 包含下载预签名 URL 的响应
     */
    public Map<String, Object> getProjectFIleDownloadPresignedUrl(String bucketName, String objectKey,Boolean ifInCluster) {
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        UploadTask uploadTask = uploadTaskService.getByBucketAndObjectKey(bucketName, objectKey);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        if (uploadTask != null && uploadTask.getFileName() != null && !uploadTask.getFileName().trim().isEmpty()) {
            ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
            responseHeaders.setContentDisposition(buildContentDisposition(uploadTask.getFileName()));
            request.setResponseHeaders(responseHeaders);
        }
        URL url = s3Client.generatePresignedUrl(request);
        String urlStr =  url.toString();
        urlStr = urlStr.replace(minioConfig.getEndpoint(),minioConfig.getPublicEndpoint());
        Map<String, Object> response = new HashMap<>();
        response.put("url", urlStr);
        response.put("objectKey", objectKey);
        if (uploadTask != null) {
            response.put("fileName", uploadTask.getFileName());
        }
        return response;
    }

    /**
     * 完成分片上传（合并分片）
     *
     * @param bucketName bucket 名称
     * @param body       完成上传请求体
     * @return 包含上传结果的响应
     */
    public Map<String, String> completeUpload(String bucketName, CompleteUploadRequest body) {
        List<PartETag> partETags = body.getParts().stream()
                .map(p -> new PartETag(p.getPartNumber(), p.getEtag()))
                .collect(Collectors.toList());

        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
                bucketName, body.getObjectKey(), body.getUploadId(), partETags);
        s3Client.completeMultipartUpload(compRequest);
        uploadTaskService.markCompleted(body.getUploadId());

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("bucket", bucketName);
        response.put("objectKey", body.getObjectKey());
        return response;
    }

    /**
     * 中止分片上传
     *
     * @param bucketName bucket 名称
     * @param objectKey  对象 key
     * @param uploadId   上传 ID
     * @return 包含中止结果的响应
     */
    public Map<String, String> abortUpload(String bucketName, String objectKey, String uploadId) {
        s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, objectKey, uploadId));
        uploadTaskService.markAborted(uploadId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "aborted");
        response.put("bucket", bucketName);
        response.put("objectKey", objectKey);
        return response;
    }

    /**
     * 列出已上传的分片（用于断点续传）
     *
     * @param bucketName bucket 名称
     * @param objectKey  对象 key
     * @param uploadId   上传 ID
     * @return 分片列表信息
     */
    public PartListing listParts(String bucketName, String objectKey, String uploadId) {
        ListPartsRequest request = new ListPartsRequest(bucketName, objectKey, uploadId);
        return s3Client.listParts(request);
    }

    private String buildContentDisposition(String fileName) {
        String safeFileName = fileName == null ? "file" : fileName.replace("\"", "'");
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(safeFileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encodedFileName = safeFileName;
        }
        return "attachment; filename=\"" + safeFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }
}
