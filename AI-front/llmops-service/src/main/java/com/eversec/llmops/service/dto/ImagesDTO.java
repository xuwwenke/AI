package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagesDTO {
    private Integer id;
    private Integer project;
    private Integer repository;
    private String name;
    private String describe;
    private String dockerfile;
    private String gitpath;
}