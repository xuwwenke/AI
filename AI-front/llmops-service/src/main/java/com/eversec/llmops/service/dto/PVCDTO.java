package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PVCDTO {

    private String name;

    private String namespace;

    private String status;

    private String storageClass;

    private String capacity;

    private String accessMode;

    private String volumeName;

    private Map<String, String> labels;

    private String createdAt;

    private String pvcType;
}
