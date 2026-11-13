package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiExperimentStatsDTO {
    private Integer totalCount;
    private Integer weekNewCount;
    private Integer runningCount;
    private Integer completedCount;
    private Integer failedCount;
}