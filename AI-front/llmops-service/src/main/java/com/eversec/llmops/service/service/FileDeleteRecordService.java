package com.eversec.llmops.service.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.entity.FileDeleteRecord;

/**
 * <p>
 * 文件删除任务表 服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-01
 */
public interface FileDeleteRecordService extends IService<FileDeleteRecord> {



    void deleteModel(String objectKey,String deletor);

    void deleteDataSet(String objectKey,String deletor);


    void deleteIcon(String objectKey,String deletor);
}
