package com.eversec.llmops.file.entity;

import lombok.Data;

@Data
public class UploadStatus {
    private String uploadId;
    private String fileName;
    private String bucketName;
    private Long totalSize;
    private Long uploadedSize;
    private Integer progress;
    private String status; // UPLOADING, SAVING, COMPLETED, FAILED
    private String message;
    private String minioFileName;
    private Long startTime;
    private Long endTime;
}
