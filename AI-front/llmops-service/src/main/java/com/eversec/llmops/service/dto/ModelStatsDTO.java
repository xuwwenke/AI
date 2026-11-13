package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelStatsDTO {
    private Integer totalCount;
    private Integer typeCount;
    private Integer onlineCount;
}
