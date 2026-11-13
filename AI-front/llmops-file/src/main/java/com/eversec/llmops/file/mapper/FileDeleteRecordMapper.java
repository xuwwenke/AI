package com.eversec.llmops.file.mapper;




import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.file.entity.FileDeleteRecord;
import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 文件删除任务表 Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-01
 */
@Mapper
@DS("kubeflow")
public interface FileDeleteRecordMapper extends BaseMapper<FileDeleteRecord> {

}
