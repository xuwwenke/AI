package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobTemplateRequestDTO {
    private Integer id;
    private Integer project;
    private Integer images;
    private String name;
    private String version;
    private String describe;
    private String workdir;
    private String entrypoint;
    private String volume_mount;
    private String args;
    private String env;
    private String host_aliases;
    private Boolean privileged;
    private String accounts;
    private String expand;
}