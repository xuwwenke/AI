package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequestDTO {
    private Integer id;
    private String name;
    private String describe;
    private String cluster;
    private String volumeMount;
    private String serviceExternalIp;
    private String org;
    private ProjectManagerSimpleDTO projectManager;
}