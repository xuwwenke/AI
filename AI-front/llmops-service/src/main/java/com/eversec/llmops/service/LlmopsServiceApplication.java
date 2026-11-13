package com.eversec.llmops.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("com.eversec.llmops.service.mapper")
public class LlmopsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmopsServiceApplication.class, args);
    }

}
