-- 文件上传任务表
CREATE TABLE IF NOT EXISTS `upload_tasks` (
                                `id`              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `object_key`      VARCHAR(512)    NOT NULL COMMENT 'MinIO 对象键（含路径）',
                                `creator_id`      BIGINT          NOT NULL DEFAULT 0 COMMENT '上传者用户ID',
                                `bucket`          VARCHAR(128)    NOT NULL COMMENT 'MinIO 桶名',
                                `file_name`       VARCHAR(256)    NOT NULL COMMENT '原始文件名',
                                `file_size`       BIGINT          NOT NULL DEFAULT 0 COMMENT '文件总大小（字节）',
                                `mime_type`       VARCHAR(128)    DEFAULT NULL COMMENT 'MIME 类型',
                                `upload_id`       VARCHAR(256)    NOT NULL COMMENT 'MinIO 分片上传 uploadId',
                                `total_parts`     INT             NOT NULL DEFAULT 0 COMMENT '总分片数',
                                `uploaded_parts`  INT             NOT NULL DEFAULT 0 COMMENT '已完成分片数',
                                `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0-初始化 1-上传中 2-已完成 3-已中止',
                                `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `complete_time`   DATETIME        DEFAULT NULL COMMENT '完成/中止时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_upload_id` (`upload_id`),
                                KEY `idx_object_key` (`object_key`),
                                KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传任务表';

-- 分片详情表（记录每个分片的上传结果）
CREATE TABLE IF NOT EXISTS `upload_parts` (
                                `id`              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `upload_id`       VARCHAR(256)    NOT NULL COMMENT '关联的 uploadId',
                                `part_number`     INT             NOT NULL COMMENT '分片编号（从1开始）',
                                `etag`            VARCHAR(64)     NOT NULL COMMENT '分片上传后返回的 ETag',
                                `part_size`       BIGINT          NOT NULL DEFAULT 0 COMMENT '该分片实际大小（字节）',
                                `upload_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传成功时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_upload_part` (`upload_id`, `part_number`),
                                KEY `idx_upload_id` (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传详情表';