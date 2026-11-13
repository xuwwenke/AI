package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookDTO {
    private Integer id;
    private Integer project;
    private String name;
    private String describe;
    private String images;
    private String resource_memory;
    private String resource_cpu;
    private String resource_gpu;
    private String volume_mount;
}