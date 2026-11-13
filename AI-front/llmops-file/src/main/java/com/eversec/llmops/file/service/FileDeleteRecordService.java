package com.eversec.llmops.file.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.file.entity.FileDeleteRecord;


/**
 * <p>
 * 文件删除任务表 服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-01
 */
public interface FileDeleteRecordService extends IService<FileDeleteRecord> {

    /**
     * 执行文件删除任务
     * 1. 查询所有 status=0 的记录
     * 2. 检查文件是否存在
     * 3. 如果存在则删除文件
     * 4. 删除成功后更新 status=1
     */
    void executeDelete();

}
