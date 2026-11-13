package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.DockerDTO;
import com.eversec.llmops.service.entity.Docker;
import com.eversec.llmops.service.service.DockerService;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docker")
public class DockerController {

    private final String baseUrl = "/docker_modelview/api/";
    @Autowired
    private DockerService dockerService;

    @Autowired
    private HttpProxyService httpProxyService;

    @Autowired
    private ProjectService projectService;

    /**
     * 根据ID获取详情
     * @param id ID
     * @return Docker对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Docker> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> dockerService.getById(id));
    }



    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param namespace 项目名称
     * @param creatorName 创建者名称
     * @return QueryResult
     */
    @GetMapping("/page")
    @Operation(summary = "分页")
    public RestResponse<QueryResult<Docker>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader String namespace,
            @RequestParam(required = false) String creator) {
        return ResponseHolder.build(() -> dockerService.customGetPageList(pageNum, pageSize, namespace, creator));
    }

    /**
     * 创建Docker镜像
     * @param dockerDTO DockerDTO对象
     * @return 响应
     */
    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Object> createDocker(@RequestBody DockerDTO dockerDTO,
                                             @RequestHeader String namespace) {
        dockerDTO.setProject(projectService.getByName(namespace).getId());
        Object response = httpProxyService.post(baseUrl, dockerDTO,UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 更新Docker镜像
     * @param dockerDTO DockerDTO对象
     * @return 响应
     */
    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Object> updateDocker(@RequestBody DockerDTO dockerDTO,@RequestHeader String namespace) {
        dockerDTO.setProject(projectService.getByName(namespace).getId());
        Object response = httpProxyService.put(baseUrl + dockerDTO.getId(),dockerDTO,UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 删除Docker镜像
     * @param id Docker镜像ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> delete(@PathVariable Integer id) {
        Object response = httpProxyService.delete(baseUrl + id, Object.class);
        return ResponseHolder.build(() -> response);
    }

    @DeleteMapping("/deletePod/{id}")
    @Operation(summary = "删除Pod")
    public RestResponse<Object> deletePod(@PathVariable Integer id) {
        Object response = httpProxyService.get(baseUrl+"delete_pod/" + id, Object.class);
        return ResponseHolder.build(() -> response);
    }


    @PostMapping("/save/{id}")
    @Operation(summary = "保存")
    public RestResponse<Object> save(@PathVariable Integer id) {
        Object response = httpProxyService.post(baseUrl+"save/" + id,UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    @PostMapping("/debug/{id}")
    @Operation(summary = "调试")
    public RestResponse<Object> debug(@PathVariable Integer id) {
        Object response = httpProxyService.get(baseUrl+"debug/" + id, UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }
}
