package com.eversec.llmops.file.controller.res;// ListPartsResponse.java
import java.util.List;

public class ListPartsResponse {
    private String uploadId;
    private List<PartDetail> parts;

    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }
    public List<PartDetail> getParts() { return parts; }
    public void setParts(List<PartDetail> parts) { this.parts = parts; }
}