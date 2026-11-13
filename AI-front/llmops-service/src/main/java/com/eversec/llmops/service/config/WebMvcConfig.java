package com.eversec.llmops.service.config;

import com.eversec.llmops.service.interceptor.SecurityUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private SecurityUserInterceptor securityUserInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityUserInterceptor)
                .addPathPatterns("/**");
    }
}