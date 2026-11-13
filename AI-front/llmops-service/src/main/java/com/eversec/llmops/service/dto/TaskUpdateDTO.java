package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskUpdateDTO {
    private String resource_cpu;
    private String resource_memory;
    private String resource_gpu;
    private String resource_rdma;
    private String timeout;
    private String retry;
    private Boolean skip;
    private String args;
    private String label;
}
