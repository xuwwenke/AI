package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryDTO {
    private Integer id;
    private String name;
    private String server;
    private String user;
    private String password;
    private String hubsecret;
}