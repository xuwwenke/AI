package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PodDTO {

    private String name;
    
    private String namespace;
    
    private String phase;
    
    private String nodeName;
    
    private String podIp;
    
    private String appLabel;
    
    private List<ContainerStatus> containerStatuses;
    
    private String createdAt;
    
    private String restartCount;

    @Getter
    @Setter
    public static class ContainerStatus {
        private String name;
        private String state;
        private String ready;
        private String restartCount;
        private String image;
    }
}