package com.eversec.llmops.service.enums;

import lombok.Getter;

@Getter
public enum ProjectEnum {


    PUBLIC("public", 1),
    ;
    public final String name;
    public final Integer id;

    ProjectEnum(String name, Integer id) {
        this.name = name;
        this.id = id;
    }
}
