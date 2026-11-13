package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InferenceServiceListDTO {
    private Integer id;
    private String serviceType;
    private String label;
    private String modelName;
    private String modelVersion;
    private String resourceMemory;
    private String resourceCpu;
    private String resourceGpu;
    private LocalDateTime changedOn;
    private String modelStatus;
    private String creator;
}