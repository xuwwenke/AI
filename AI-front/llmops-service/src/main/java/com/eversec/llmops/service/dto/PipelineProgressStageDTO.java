package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PipelineProgressStageDTO {
    private String key;
    private String name;
    private String desc;
    private Integer percent;
    private String status;
    private String color;
}
