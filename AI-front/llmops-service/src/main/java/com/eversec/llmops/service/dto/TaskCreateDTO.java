package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    private Integer job_template;
    private Integer pipeline;
    private String name;
    private String label;
    private String volume_mount;
    private String image_pull_policy;
    private String working_dir;
    private String command;
    private Integer overwrite_entrypoint;
    private String node_selector;
    private String resource_memory;
    private String resource_cpu;
    private String resource_gpu;
    private String resource_rdma;
    private Integer timeout;
    private Integer retry;
    private String args;
}
