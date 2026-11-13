package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskUpdateFilterDTO {
    private String col;
    private String opr;
    private Integer value;
}
