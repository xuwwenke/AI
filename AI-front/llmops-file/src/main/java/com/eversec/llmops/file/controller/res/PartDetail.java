package com.eversec.llmops.file.controller.res;

import lombok.Data;

// PartDetail.java
@Data
public class PartDetail {
    private int partNumber;
    private String etag;
    private long size;

}