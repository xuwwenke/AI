package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceQuotaDTO {
    private String namespace;
    private String quotaName;
    private String cpuLimit;
    private String cpuUsed;
    private String memoryLimit;
    private String memoryUsed;
    private String gpuLimit;
    private String gpuUsed;
    private String storageLimit;
    private String storageUsed;
}