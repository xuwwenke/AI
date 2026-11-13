package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InferenceServiceStatsDTO {
    private Integer totalCount;
    private Integer thisMonthCount;
    private Integer onlineCount;
}
