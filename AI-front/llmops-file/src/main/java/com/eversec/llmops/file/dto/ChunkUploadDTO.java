package com.eversec.llmops.file.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChunkUploadDTO {
    private String uploadId;
    private Integer chunkNumber;
    private MultipartFile file;
    private String bucketName;
    private String fileName;
}
