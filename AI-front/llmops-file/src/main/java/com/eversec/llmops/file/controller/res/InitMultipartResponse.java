package com.eversec.llmops.file.controller.res;

import java.util.List;

public class InitMultipartResponse {
    private String uploadId;
    private String objectName;
    private List<PartUploadUrl> partUrls;

    public InitMultipartResponse(String uploadId, String objectName, List<PartUploadUrl> partUrls) {
        this.uploadId = uploadId;
        this.objectName = objectName;
        this.partUrls = partUrls;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<PartUploadUrl> getPartUrls() {
        return partUrls;
    }

    public void setPartUrls(List<PartUploadUrl> partUrls) {
        this.partUrls = partUrls;
    }
}
