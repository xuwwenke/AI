package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectManagerSimpleDTO {
    private Integer id;
    private Integer gpu;
    private Integer cpu;
    private Integer memory;
    private Integer storage;

    private Integer status;
}