package com.eversec.llmops.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.dto.BatchProUserDTO;
import com.eversec.llmops.service.dto.SysSecurityUserDTO;
import com.eversec.llmops.service.entity.*;
import com.eversec.llmops.service.service.IdeServiceTaskService;
import com.eversec.llmops.service.service.ProManagerService;
import com.eversec.llmops.service.service.ProUserService;
import com.eversec.llmops.service.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
@RestController
@RequestMapping("/llmopsservice/pro-user")
public class ProUserController {

    @Resource
    private ProUserService proUserService;


    @Resource
    private ProManagerService proManagerService;

    @Resource
    private IdeServiceTaskService ideServiceTaskService;
    @GetMapping("/list")
    @Operation(summary = "项目用户")
    public RestResponse<List<ProUser>> list(@RequestParam Integer proId){
        return ResponseHolder.build(() -> {
            LambdaQueryWrapper<ProUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProUser::getProId, proId);
            return proUserService.list(wrapper);
        });
    }

    /**
     * 批量新增 ProUser
     * @param dto 批量 ProUser DTO
     * @return 新增的 ProUser 列表
     */
    @PostMapping("/batch-add")
    @Operation(summary = "批量添加")
    public RestResponse<List<ProUser>> batchAdd(@RequestBody BatchProUserDTO dto) {
        ProManager proManager = proManagerService.getByProjectId(dto.getProId());
        List<ProUser> proUsers = proUserService.batchAdd(proManager.getProName(),dto);
        // 异步执行 IDE 服务处理任务，不阻塞当前请求
        ideServiceTaskService.doDealIdeServiceAsync(proManager.getProName());
        return ResponseHolder.build(() -> proUsers);
    }

    @GetMapping("/validUsers")
    @Operation(summary = "三件套可分配用户")
    public List<SysSecurityUserDTO> getUsers() {
        List<SysSecurityUserDTO> res = new ArrayList<>();
        List<SysSecurityUserDO> users = proUserService.getValidUsers();
        for (SysSecurityUserDO user : users) {
            SysSecurityUserDTO dto = new SysSecurityUserDTO();
            dto.setUserName(user.getUserName());
            dto.setRealName(user.getRealName());
            dto.setId(user.getId());
            res.add(dto);
        }
        return res;
    }
}
