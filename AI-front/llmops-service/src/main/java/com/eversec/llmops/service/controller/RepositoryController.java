package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.RepositoryDTO;
import com.eversec.llmops.service.dto.RepositoryNameDTO;
import com.eversec.llmops.service.entity.Repository;
import com.eversec.llmops.service.service.RepositoryService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repository")
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HttpProxyService httpProxyService;

    /**
     * 根据ID获取详情
     * @param id ID
     * @return Repository对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Repository> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> repositoryService.getById(id));
    }

    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 仓库名称
     * @param server 服务器地址
     * @param user 用户名
     * @param hubsecret 密钥
     * @return QueryResult
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Repository>> getPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String server,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String hubsecret) {
        return ResponseHolder.build(() -> repositoryService.customGetPageList(pageNum, pageSize, name, server, user, hubsecret));
    }

    /**
     * 创建仓库
     * @param repositoryDTO 仓库DTO对象
     * @return 响应
     */
    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Object> createRepository(@RequestBody RepositoryDTO repositoryDTO) {
        Object response = httpProxyService.post("/repository_modelview/api/", repositoryDTO, Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 更新仓库
     * @param repositoryDTO 仓库DTO对象
     * @return 响应
     */
    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Object> updateRepository(@RequestBody RepositoryDTO repositoryDTO) {
        Object response = httpProxyService.put("/repository_modelview/api/" + repositoryDTO.getId(), repositoryDTO, Object.class);
        return ResponseHolder.build(() -> response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> delete(@PathVariable Integer id) {
        Object response = httpProxyService.delete("/repository_modelview/api/" + id, Object.class);
        return ResponseHolder.build(() -> response);
    }

    @GetMapping("/names")
    @Operation(summary = "查询仓库名称和id")
    public RestResponse<List<RepositoryNameDTO>> getRepositoryNames() {
        return ResponseHolder.build(() -> repositoryService.getRepositoryNames());
    }
}
