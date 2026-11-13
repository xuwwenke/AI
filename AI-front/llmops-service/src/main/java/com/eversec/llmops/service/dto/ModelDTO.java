package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelDTO {
    private Integer id;
    private Integer project;
    private String name;
    private String version;
    private String describe;
    private String path;
    private String framework;
    private String metrics;
    private String api_type;
    private String run_id;
    private String run_time;
    private String md5;
    private Integer pipeline_id;
}