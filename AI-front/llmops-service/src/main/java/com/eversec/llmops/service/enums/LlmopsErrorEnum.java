package com.eversec.llmops.service.enums;

public enum LlmopsErrorEnum {

    CODE_1400("项目名称重复，{0}"),;
    public String message;

    LlmopsErrorEnum(String message) {
        this.message = message;
    }
}
