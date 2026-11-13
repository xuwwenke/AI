package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskDetailDTO {
    private LocalDateTime createdOn;
    private LocalDateTime changedOn;
    private Integer id;
    private String name;
    private String label;
    private Integer jobTemplateId;
    private Integer pipelineId;
    private String workingDir;
    private String command;
    private Boolean overwriteEntrypoint;
    private String args;
    private String volumeMount;
    private String nodeSelector;
    private String resourceMemory;
    private String resourceCpu;
    private String resourceGpu;
    private Integer timeout;
    private Integer retry;
    private String outputs;
    private String monitoring;
    private String expand;
    private Integer createdByFk;
    private Integer changedByFk;
    private Boolean skip;
    private String resourceRdma;
    private String namespace;
    private String creator;
    private String modifier;
}
