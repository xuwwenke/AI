package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiExperimentRequestDTO {
    private Integer id;
    private String name;
    private String modelName;
    private String desc;
    private String config;
}