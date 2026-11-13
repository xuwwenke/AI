package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerDTO {
    private Integer id;
    private Integer project;
    private String describe;
    private String base_image;
    private String target_image;
    private boolean consecutive_build;
    private String resource_memory;
    private String resource_cpu;
    private String resource_gpu;
}