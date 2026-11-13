package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrometheusQueryDTO {

    private String clusterName;

    private String query;
}
