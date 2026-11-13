package com.eversec.llmops.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eversec.llmops.file.entity.UploadTask;
import com.eversec.llmops.file.service.UploadTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 文件上传任务表 前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-05-21
 */
@RestController
@RequestMapping("/assessment/upload-tasks")
public class UploadTasksController {

    @Resource
    private UploadTaskService uploadTaskService;

    @GetMapping("")
    public List<UploadTask> list(@RequestParam(required = false) String uploadId,
                                 @RequestParam(required = false) String bucket) {
        LambdaQueryWrapper<UploadTask> queryWrapper = new LambdaQueryWrapper<>();
        if (uploadId != null && !uploadId.trim().isEmpty()) {
            queryWrapper.eq(UploadTask::getUploadId, uploadId);
        }
        if (bucket != null && !bucket.trim().isEmpty()) {
            queryWrapper.eq(UploadTask::getBucket, bucket);
        }
        queryWrapper.orderByDesc(UploadTask::getId);
        return uploadTaskService.list(queryWrapper);
    }
}
