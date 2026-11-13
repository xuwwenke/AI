package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.TaskCreateDTO;
import com.eversec.llmops.service.dto.TaskDTO;
import com.eversec.llmops.service.dto.TaskDetailDTO;
import com.eversec.llmops.service.dto.TaskUpdateDTO;
import com.eversec.llmops.service.service.TaskService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.llmops.service.util.UserUtil;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final String baseUrl = "/task_modelview/api/";
    @Autowired
    private TaskService taskService;

    @Autowired
    private HttpProxyService httpProxyService;

    @GetMapping("/{id}")
    public RestResponse<Object> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> {
            ResponseEntity<Object> response = httpProxyService.getWithResponseEntity(baseUrl + id, UserUtil.getAbUserHeaders(), Object.class);
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("记录不存在");
            }
            return response.getBody();
        });
    }

    @GetMapping("/page")
    public RestResponse<QueryResult<TaskDTO>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseHolder.build(() -> taskService.customGetPageList(pageNum, pageSize));
    }

    @GetMapping("/search/{pipelineId}")
    public RestResponse<QueryResult<TaskDetailDTO>> getTasksByPipelineId(
            @PathVariable Integer pipelineId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseHolder.build(() -> taskService.getTaskDetailsByPipelineId(pageNum, pageSize, pipelineId));
    }

    @GetMapping("")
    public RestResponse<Object> getAll() {
        return ResponseHolder.build(() -> httpProxyService.get(baseUrl, UserUtil.getAbUserHeaders(), Object.class));
    }

    @PostMapping("")
    @ApiOperation("新增任务")
    public RestResponse<Object> create(@RequestBody TaskCreateDTO requestBody) {
        return ResponseHolder.build(() -> httpProxyService.post(baseUrl, requestBody, UserUtil.getAbUserHeaders(), Object.class));
    }

    @PutMapping("/{id}")
    @ApiOperation("修改任务")
    public RestResponse<Object> update(@PathVariable Integer id, @RequestBody TaskUpdateDTO requestBody) {
        return ResponseHolder.build(() -> httpProxyService.put(baseUrl + id, requestBody, UserUtil.getAbUserHeaders(), Object.class));
    }

    @PostMapping("/debug/{id}")
    public RestResponse<Object> debug(@PathVariable Integer id) {
        return ResponseHolder.build(() -> httpProxyService.get(baseUrl+"debug/" + id, UserUtil.getAbUserHeaders(), Object.class));
    }

    @PostMapping("/run/{task_id}")
    public RestResponse<Object> run(@PathVariable Integer task_id) {
        return ResponseHolder.build(() -> httpProxyService.post(baseUrl+"run/" + task_id, null, UserUtil.getAbUserHeaders(), Object.class));
    }

    @PostMapping("/clear/{task_id}")
    public RestResponse<Object> clear(@PathVariable Integer task_id) {
        return ResponseHolder.build(() -> httpProxyService.post(baseUrl+"clear/" + task_id, null, UserUtil.getAbUserHeaders(), Object.class));
    }

    @PostMapping("/log/{task_id}")
    public RestResponse<Object> log(@PathVariable Integer task_id) {
        return ResponseHolder.build(() -> httpProxyService.get(baseUrl+"log/" + task_id, UserUtil.getAbUserHeaders(), Object.class));
    }

    @DeleteMapping("/{task_id}")
    public RestResponse<Object> delete(@PathVariable Integer task_id) {
        return ResponseHolder.build(() -> httpProxyService.delete(baseUrl + task_id, Object.class));
    }
}
