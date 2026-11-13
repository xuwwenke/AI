package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatasetRequestDTO {
    private String name;
    private String version;
    private String label;
    private String describe;
    private String url;
    private String downloadUrl;
    private String path;
    private String icon;
    private String owner;
    private String features;
}