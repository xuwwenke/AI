package com.eversec.llmops.service.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.service.mapper.FileDeleteRecordMapper;
import org.springframework.stereotype.Service;
import com.eversec.llmops.service.service.FileDeleteRecordService;
import com.eversec.llmops.service.entity.FileDeleteRecord;

/**
 * <p>
 * 文件删除任务表 服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-01
 */
@Service
public class FileDeleteRecordServiceImpl extends ServiceImpl<FileDeleteRecordMapper, FileDeleteRecord> implements FileDeleteRecordService {

    private final byte toDeleteStatus = 0;

    private void deleteFile(String bucket, String objectKey, String deletor) {
        FileDeleteRecord toDelete = new FileDeleteRecord();
        toDelete.setBucket(bucket);
        toDelete.setObjectKey(objectKey);
        toDelete.setDeletor(deletor);
        toDelete.setStatus(toDeleteStatus);
        this.save(toDelete);
    }

    @Override
    public void deleteModel(String objectKey, String deletor) {
        this.deleteFile("model", objectKey, deletor);
    }

    @Override
    public void deleteDataSet(String objectKey, String deletor) {
        this.deleteFile("dataset", objectKey, deletor);
    }

    @Override
    public void deleteIcon(String objectKey, String deletor) {
        this.deleteFile("icon", objectKey, deletor);
    }

}
