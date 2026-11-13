package com.eversec.llmops.service.dto;

import lombok.Data;

@Data
public class StorageOverViewDTO {
    private String storageLimit;
    private String storageUsed;
    private Integer pvcNums;
    private Integer boundPvcNums;
    private Integer pendingPvcNums;
}
