package com.eversec.llmops.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PipelineRequestDTO {
    private Integer id;
    private Integer project;
    private String name;
    private String describe;
    private String schedule_type;
    private String cron_time;
    private Boolean depends_on_past;
    private Integer max_active_runs;
    private Integer expired_limit;
    private Integer parallelism;
    private String dag_json;
    private String global_env;
    private String alert_status;
    private String alert_user;
    private String expand;
    private String parameter;
    private String cronjob_start_time;
    private Integer ai_experiment_id;
}