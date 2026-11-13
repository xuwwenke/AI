//package com.eversec.llmops.service.controller;
//
//import com.eversec.framework.core.response2.ResponseHolder;
//import com.eversec.framework.core.response2.RestResponse;
//import com.eversec.llmops.service.service.impl.IdeServiceTaskServiceImpl;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * IdeServiceTask 测试接口
// */
//@RestController
//@RequestMapping("/api/v1/ide-service-task/test")
//@Api(tags = "IdeServiceTask测试接口")
//public class IdeServiceTaskTestController {
//
//    @Autowired
//    private IdeServiceTaskServiceImpl ideServiceTaskService;
//
//    /**
//     * 测试删除IdeServiceTask（更新状态为to_delete）
//     */
//    @PostMapping("/delete")
//    @ApiOperation("测试删除IdeServiceTask（更新状态为to_delete）")
//    public RestResponse<String> testDeleteIdeServiceTask(
//            @ApiParam("命名空间") @RequestParam String namespace,
//            @ApiParam("用户名列表") @RequestBody List<String> userNames) {
//
//        ideServiceTaskService.deleteIdeServiceTask(namespace, userNames);
//        return ResponseHolder.build(() -> "删除任务已提交，状态已更新为 to_delete");
//    }
//
//    /**
//     * 测试检查codeServer健康状态
//     */
//    @GetMapping("/health")
//    @ApiOperation("测试检查codeServer健康状态")
//    public RestResponse<String> testCheckCodeServerHealth() {
//        return ResponseHolder.build(() -> {
//            boolean isHealthy = ideServiceTaskService.checkCodeServerHealth();
//            if (isHealthy) {
//                return "codeServer 健康状态: 正常";
//            } else {
//                return "codeServer 健康状态: 异常或无法访问";
//            }
//        });
//    }
//
//    /**
//     * 测试创建 codeServer link
//     */
//    @PostMapping("/link")
//    @ApiOperation("测试创建codeServer link")
//    public RestResponse<IdeServiceTaskServiceImpl.CodeServerLinkResponse> testCreateCodeServerLink(
//            @ApiParam("用户ID") @RequestParam String userId,
//            @ApiParam("命名空间") @RequestParam String namespace,
//            @ApiParam("存储名称") @RequestParam String storageName) {
//
//        return ResponseHolder.build(() -> {
//            IdeServiceTaskServiceImpl.CodeServerLinkResponse response =
//                ideServiceTaskService.createCodeServerLink(userId, namespace, storageName);
//            if (response == null) {
//                throw new RuntimeException("创建codeServer link失败");
//            }
//            return response;
//        });
//    }
//
//    /**
//     * 测试获取 codeServer link
//     */
//    @GetMapping("/link")
//    @ApiOperation("测试获取codeServer link")
//    public RestResponse<IdeServiceTaskServiceImpl.CodeServerLinkResponse> testGetCodeServerLink(
//            @ApiParam("用户ID") @RequestParam String userId,
//            @ApiParam("命名空间") @RequestParam String namespace) {
//
//        return ResponseHolder.build(() -> {
//            IdeServiceTaskServiceImpl.CodeServerLinkResponse response =
//                ideServiceTaskService.getCodeServerLink(userId, namespace);
//            if (response == null) {
//                throw new RuntimeException("获取codeServer link失败");
//            }
//            return response;
//        });
//    }
//
//    /**
//     * 测试删除 codeServer link
//     */
//    @DeleteMapping("/link")
//    @ApiOperation("测试删除codeServer link")
//    public RestResponse<IdeServiceTaskServiceImpl.DeleteCodeServerResponse> testDeleteCodeServerLink(
//            @ApiParam("用户ID") @RequestParam String userId,
//            @ApiParam("命名空间") @RequestParam String namespace) {
//
//        return ResponseHolder.build(() -> {
//            IdeServiceTaskServiceImpl.DeleteCodeServerResponse response =
//                ideServiceTaskService.deleteCodeServerLink(userId, namespace);
//            if (response == null) {
//                throw new RuntimeException("删除codeServer link失败");
//            }
//            return response;
//        });
//    }
//
//    /**
//     * 测试执行 checkDeployingTasks 定时任务
//     * 手动触发一次，处理所有 deploying 状态且 user_ide_link 为空的记录
//     */
//    @PostMapping("/check-deploying")
//    @ApiOperation("测试执行checkDeployingTasks定时任务")
//    public RestResponse<String> testCheckDeployingTasks() {
//        return ResponseHolder.build(() -> {
//            ideServiceTaskService.checkDeployingTasks();
//            return "checkDeployingTasks 执行完成";
//        });
//    }
//}
