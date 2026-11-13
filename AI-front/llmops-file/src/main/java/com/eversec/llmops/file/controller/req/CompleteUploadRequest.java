package com.eversec.llmops.file.controller.req;

import java.util.List;

public class CompleteUploadRequest {
    private String objectKey;
    private String uploadId;
    private List<PartInfo> parts;

    // getters and setters
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }

    public List<PartInfo> getParts() { return parts; }
    public void setParts(List<PartInfo> parts) { this.parts = parts; }

    public static class PartInfo {
        private int partNumber;
        private String etag;

        public int getPartNumber() { return partNumber; }
        public void setPartNumber(int partNumber) { this.partNumber = partNumber; }

        public String getEtag() { return etag; }
        public void setEtag(String etag) { this.etag = etag; }
    }
}