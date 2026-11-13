package com.eversec.llmops.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InferenceServiceRequestDTO {
    private Integer id;

    @JsonProperty("service_type")
    private String serviceType;

    private Integer project;

    private String label;

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;

    private String images;

    @JsonProperty("model_path")
    private String modelPath;

    @JsonProperty("resource_memory")
    private String resourceMemory;

    @JsonProperty("resource_cpu")
    private String resourceCpu;

    @JsonProperty("resource_gpu")
    private String resourceGpu;

    @JsonProperty("min_replicas")
    private Integer minReplicas;

    @JsonProperty("max_replicas")
    private Integer maxReplicas;

    private String hpa;

    private Integer priority;

    private String canary;

    private String shadow;

    private String host;

    private String sidecar;

    @JsonProperty("inference_config")
    private String inferenceConfig;

    @JsonProperty("working_dir")
    private String workingDir;

    private String command;

    private String env;

    private String ports;

    private String metrics;

    private String health;
}