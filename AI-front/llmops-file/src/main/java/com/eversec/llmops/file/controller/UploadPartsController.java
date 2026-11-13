package com.eversec.llmops.file.controller;

import com.eversec.llmops.file.entity.UploadTask;
import com.eversec.llmops.file.service.UploadTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 分片上传详情表 前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-05-21
 */
@RestController
@RequestMapping("/assessment/upload-parts")
public class UploadPartsController {

    @Resource
    private UploadTaskService uploadTaskService;
    @GetMapping("")
    public List<UploadTask> get(){
        return uploadTaskService.list();
    }
}
