package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClusterUsageDTO {

    private String clusterName;

    private String cpu;

    private String cpuUsed;

    private String memory;

    private String memoryUsed;


}