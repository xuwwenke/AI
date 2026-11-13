package com.eversec.llmops.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.file.entity.UploadPart;
import com.eversec.llmops.file.mapper.UploadPartMapper;
import com.eversec.llmops.file.service.UploadPartService;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 分片上传详情表 服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-05-21
 */
@Service
public class UploadPartServiceImpl extends ServiceImpl<UploadPartMapper, UploadPart> implements UploadPartService {

}
