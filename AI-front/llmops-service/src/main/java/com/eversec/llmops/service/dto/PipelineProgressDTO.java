package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PipelineProgressDTO {
    private String pipelineId;
    private String pipelineName;
    private Integer overall;
    private String status;
    private String statusText;
    private Integer currentStage;
    private String currentStageName;
    private String updatedAt;
    private List<PipelineProgressStageDTO> stages;
}
