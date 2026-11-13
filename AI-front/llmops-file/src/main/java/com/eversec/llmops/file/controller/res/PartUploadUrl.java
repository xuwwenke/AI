package com.eversec.llmops.file.controller.res;

public class PartUploadUrl {
    private int partNumber;
    private String url;

    public PartUploadUrl(int partNumber, String url) {
        this.partNumber = partNumber;
        this.url = url;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
