CREATE TABLE pvc_backup_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    namespace_name VARCHAR(128),
    pvc_name VARCHAR(255),
    job_name VARCHAR(255),
    task_type VARCHAR(20),
    status VARCHAR(20),
    create_time DATETIME,
    finish_time DATETIME
);

CREATE TABLE pvc_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    namespace_name VARCHAR(128),
    pvc_name VARCHAR(255),
    snapshot_id VARCHAR(255),
    create_time DATETIME
);