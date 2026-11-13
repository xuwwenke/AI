package com.eversec.llmops.file.controller;

import com.eversec.llmops.file.service.FileDeleteRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * FileDeleteRecord 测试接口
 */
@RestController
@RequestMapping("/api/file-delete/test")
public class FileDeleteRecordTestController {

    @Autowired
    private FileDeleteRecordService fileDeleteRecordService;

    /**
     * 手动触发文件删除任务
     * 立即执行一次 executeDelete 方法
     */
    @PostMapping("/execute")
    public Map<String, Object> executeDelete() {
        Map<String, Object> response = new HashMap<>();
        try {
            fileDeleteRecordService.executeDelete();
            response.put("status", "success");
            response.put("message", "文件删除任务执行完成");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "文件删除任务执行失败: " + e.getMessage());
        }
        return response;
    }
}
