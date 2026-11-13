package com.eversec.llmops.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.entity.IdeServiceTask;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.entity.ProUser;
import com.eversec.llmops.service.service.IdeServiceTaskService;
import com.eversec.llmops.service.service.ProManagerService;
import com.eversec.llmops.service.service.ProUserService;
import com.eversec.llmops.service.service.SysSecurityUserService;
import com.eversec.llmops.service.util.UserUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 异步创建服务任务表 前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-04
 */
@RestController
@RequestMapping("/api/ide-service-task")
public class IdeServiceTaskController {


    @Resource
    private IdeServiceTaskService ideServiceTaskService;

    @Resource
    private SysSecurityUserService sysSecurityUserService;
    @GetMapping
    public RestResponse<IdeServiceTask> get(@RequestHeader String namespace) {
        Long userId = UserUtil.getLoginUserId();
        String userName = UserUtil.getLoginUsername();
        Boolean ifAdmin = sysSecurityUserService.ifAilabxAdmin(userId);
        IdeServiceTask newTask = ideServiceTaskService.get(namespace,userId,userName,ifAdmin);
        return ResponseHolder.build(() -> newTask);
    }
}
