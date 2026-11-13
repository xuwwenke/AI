package com.eversec.llmops.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-13
 */
@Getter
@Setter
@TableName("ai_experiment_pipeline")
public class AiExperimentPipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 实验ID
     */
    @TableField("experiment_id")
    private Integer experimentId;

    /**
     * 流水线ID
     */
    @TableField("pipeline_id")
    private Integer pipelineId;
}
