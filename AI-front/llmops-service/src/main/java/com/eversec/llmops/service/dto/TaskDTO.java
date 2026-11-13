package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO {
    private Integer id;
    private String name;
    private String label;
    private String jobTemplateName;
    private String pipelineName;
    private String creator;
    private String modifier;
    private String modified;
    private String expand;
}