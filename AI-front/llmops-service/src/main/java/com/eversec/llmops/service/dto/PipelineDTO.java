package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PipelineDTO {
    private Integer id;
    private String project;
    private String name;
    private String describe;
    private String version;
    private String creator;
    private String modified;
    private String expand;
    private String aiExperimentName;
}