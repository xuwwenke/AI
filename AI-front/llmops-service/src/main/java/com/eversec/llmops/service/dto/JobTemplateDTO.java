package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobTemplateDTO {
    private Integer id;
    private String project;
    private String name;
    private String version;
    private String creator;
    private String modifier;
    private String modified;
    private String expand;
    private String imageName;
    private String describe;
}