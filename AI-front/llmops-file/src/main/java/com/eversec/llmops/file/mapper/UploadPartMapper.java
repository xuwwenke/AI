package com.eversec.llmops.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eversec.llmops.file.entity.UploadPart;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 分片上传详情表 Mapper 接口
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-05-21
 */
@Mapper
public interface UploadPartMapper extends BaseMapper<UploadPart> {

}
