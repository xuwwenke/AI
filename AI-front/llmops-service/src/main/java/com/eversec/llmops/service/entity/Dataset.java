package com.eversec.llmops.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.util.Date;

@TableName("dataset")
@Data
public class Dataset {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String label;
    @TableField("`describe`")
    private String describe;
    @TableField("source_type")
    private String sourceType;
    private String source;
    private String industry;
    private String icon;
    private String field;
    private String usage;
    private String research;
    @TableField("storage_class")
    private String storageClass;
    @TableField("file_type")
    private String fileType;
    private String status;
    private String years;
    private String url;
    private String path;
    @TableField("download_url")
    private String downloadUrl;
    @TableField("storage_size")
    private String storageSize;
    @TableField("entries_num")
    private String entriesNum;
    private String duration;
    private String price;
    private String owner;
    private String expand;
    @TableField("created_by_fk")
    private Integer createdByFk;
    @TableField("changed_by_fk")
    private Integer changedByFk;
    private String doc;
    private String features;
    private String info;
    @TableField("metric_info")
    private String metricInfo;
    private String secret;
    private String segment;
    private String split;
    private String subdataset;
    private String version;
    @TableField("created_on")
    private Date createdOn;
    @TableField("changed_on")
    private Date changedOn;


    @TableField("project_id")
    private Integer projectId;

}
