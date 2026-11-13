package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClusterDTO {

    private Long id;

    private String name;

    private String k8sServerUrl;

    private String token;

    private String serviceIp;

    private String description;
}