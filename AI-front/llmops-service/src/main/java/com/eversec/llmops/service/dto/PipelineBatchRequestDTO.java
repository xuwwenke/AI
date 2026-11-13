package com.eversec.llmops.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PipelineBatchRequestDTO {

    private List<Integer> ids;
}
