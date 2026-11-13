CREATE TABLE IF NOT EXISTS `cluster`  (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                            `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '集群名称',
                            `k8s_server_url` varchar(248) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'k8s https serverurl',
                            `token` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'k8s token',
                            `service_ip` varchar(248) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '服务ip，用于拼接端口，供用户调用',
                            `createtime` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                            `updatetime` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
                            `operator` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '操作人',
                            `description` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '描述',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE INDEX `uqe`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

CREATE TABLE  IF NOT EXISTS file_delete_record (
                                                   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                   bucket VARCHAR(128) NOT NULL COMMENT '存储桶名称',
    object_key VARCHAR(1024) NOT NULL COMMENT '对象键（文件路径）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待删除，1-删除中，2-已完成，3-失败',
    delete_time DATETIME DEFAULT NULL COMMENT '实际删除完成时间（状态为2时记录）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录插入时间（即删除任务创建时间）',
    deletor VARCHAR(64) DEFAULT NULL COMMENT '执行删除操作的标识（程序名/实例ID）',
    retry_count TINYINT NOT NULL DEFAULT 0 COMMENT '重试次数（用于失败状态）',
    last_error VARCHAR(512) DEFAULT NULL COMMENT '最后一次错误信息',
    PRIMARY KEY (id),
    KEY idx_status_created_at (status, created_at),
    KEY idx_bucket_object (bucket, object_key(255))
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件删除任务表';

CREATE TABLE  IF NOT EXISTS  ide_service_task (
                              id VARCHAR(64) PRIMARY KEY COMMENT '任务ID，通常为UUID',
                              user_name VARCHAR(1024) NOT NULL COMMENT '用户en name',
                              namespace VARCHAR(128) NOT NULL DEFAULT 'default' COMMENT 'Kubernetes命名空间',
                              status VARCHAR(20) NOT NULL DEFAULT 'CREATING' COMMENT '任务状态：CREATING, SUCCESS, FAILED',
                              error_msg TEXT COMMENT '失败时的错误信息',
                              user_ide_link VARCHAR(2048) COMMENT '底层服务或K8s返回的结果链接/标识',
                              storage_name VARCHAR(256) DEFAULT NULL COMMENT 'PVC名称',
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步创建服务任务表';



CREATE TABLE IF NOT EXISTS  `pro_manager`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `project_id` bigint NOT NULL COMMENT 'project_id',
                                `pro_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '项目名称',
                                `pro_type` int NULL DEFAULT NULL COMMENT '项目类型，1：虚拟机，2：容器服务',
                                `bz` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注信息',
                                `status` int NOT NULL DEFAULT 0 COMMENT '0：进行，1：完成 ，2：暂停',
                                `createtime` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                                `updatetime` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
                                `operator` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '操作人',
                                `gpu` int NULL DEFAULT NULL COMMENT 'gpu',
                                `cpu` int NULL DEFAULT NULL COMMENT 'CPU',
                                `memory` int NULL DEFAULT NULL COMMENT '内存',
                                `cluster_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '集群名称',
                                `desc_zh` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '项目中文名',
                                `group_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '项目分组名称',
                                `node_group_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '所属节点分组名称',
                                `storage` int NULL DEFAULT NULL COMMENT '存储',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_name`(`pro_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;


CREATE TABLE IF NOT EXISTS   `pro_manager`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `project_id` bigint NOT NULL COMMENT 'project_id',
                                `pro_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '项目名称',
                                `pro_type` int NULL DEFAULT NULL COMMENT '项目类型，1：虚拟机，2：容器服务',
                                `bz` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注信息',
                                `status` int NOT NULL DEFAULT 0 COMMENT '0：进行，1：完成 ，2：暂停',
                                `createtime` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                                `updatetime` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
                                `operator` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '操作人',
                                `gpu` int NULL DEFAULT NULL COMMENT 'gpu',
                                `cpu` int NULL DEFAULT NULL COMMENT 'CPU',
                                `memory` int NULL DEFAULT NULL COMMENT '内存',
                                `cluster_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '集群名称',
                                `desc_zh` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '项目中文名',
                                `group_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '项目分组名称',
                                `node_group_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '所属节点分组名称',
                                `storage` int NULL DEFAULT NULL COMMENT '存储',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_name`(`pro_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS   `ai_experiment`  (
                                  `created_on` datetime(0) NULL DEFAULT NULL,
                                  `changed_on` datetime(0) NULL DEFAULT NULL,
                                  `id` int NOT NULL AUTO_INCREMENT,
                                  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '中文名',
                                  `created_by_fk` int NULL DEFAULT NULL,
                                  `changed_by_fk` int NULL DEFAULT NULL,
                                  `project_id` int NULL DEFAULT NULL,
                                  `start_time` datetime(0) NULL DEFAULT NULL,
                                  `end_time` datetime(0) NULL DEFAULT NULL,
                                  `status` int NULL DEFAULT NULL,
                                  `model_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
                                  `config` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
                                  `desc` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;


CREATE TABLE  IF NOT EXISTS `pro_user`  (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `pro_id` bigint NULL DEFAULT NULL COMMENT '项目id',
                             `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
                             `is_admin` int NULL DEFAULT NULL COMMENT '是否管理员,0:是，1:否',
                             `createtime` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                             `updatetime` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
                             `username` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户名称',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;