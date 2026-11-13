package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.ProjectManagerDTO;
import com.eversec.llmops.service.service.ProManagerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
@RequestMapping("/api/pro-manager")
public class ProManagerController {

    @Resource
    private ProManagerService proManagerService;
    @GetMapping("/getProjectManagersByUsername")
    public List<ProjectManagerDTO> getProjectManagersByUsername(@RequestParam String username) {
        return proManagerService.getProjectManagersByUsername(username);
    }

}
