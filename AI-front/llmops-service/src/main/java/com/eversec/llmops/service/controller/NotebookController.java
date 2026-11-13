package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.NotebookDTO;
import com.eversec.llmops.service.entity.Notebook;
import com.eversec.llmops.service.service.NotebookService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notebook")
public class NotebookController {

    @Autowired
    private NotebookService notebookService;

    @Autowired
    private HttpProxyService httpProxyService;

    /**
     * 根据ID获取详情
     * @param id ID
     * @return Notebook对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Notebook> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> notebookService.getById(id));
    }

    /**
     * 分页查询
     * @param page 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param namespace 项目名称
     * @param name 名称
     * @param status 状态
     * @param creator 创建者
     * @return QueryResult
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Notebook>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader String namespace,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String creator) {
        return ResponseHolder.build(() -> notebookService.customGetPageList(pageNum, pageSize, namespace, name, status, creator));
    }

    /**
     * 创建Notebook
     * @param notebookDTO NotebookDTO对象
     * @return 响应
     */
    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Object> createNotebook(@RequestBody NotebookDTO notebookDTO) {;
        Object response = httpProxyService.post("/notebook_modelview/api/", notebookDTO, UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 更新Notebook
     * @param notebookDTO NotebookDTO对象
     * @return 响应
     */
    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Object> updateNotebook(@RequestBody NotebookDTO notebookDTO) {
        Object response = httpProxyService.put("/notebook_modelview/api/" + notebookDTO.getId(), notebookDTO, Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 删除Notebook
     * @param id Notebook ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> delete(@PathVariable Integer id) {
        Object response = httpProxyService.delete("/notebook_modelview/api/" + id, Object.class);
        return ResponseHolder.build(() -> response);
    }

    /**
     * 重置Notebook
     * @param id Notebook ID
     * @return 响应
     */
    @GetMapping("/reset/{id}")
    @Operation(summary = "重置")
    public RestResponse<Object> reset(@PathVariable Integer id) {
        Object response = httpProxyService.get("/notebook_modelview/api/reset/" + id, Object.class);
        return ResponseHolder.build(() -> response);
    }

    @GetMapping("/renew/{id}")
    @Operation(summary = "续期")
    public RestResponse<Object> renew(@PathVariable Integer id) {
        Object response = httpProxyService.get("/notebook_modelview/api/renew/" + id, Object.class);
        return ResponseHolder.build(() -> response);
    }

    @GetMapping("/save/{id}")
    @Operation(summary = "保存")
    public RestResponse<Object> save(@PathVariable Integer id) {
        Object response = httpProxyService.get("/notebook_modelview/api/save/" + id,UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }

    @GetMapping("/stop/{id}")
    @Operation(summary = "停止")
    public RestResponse<Object> stop(@PathVariable Integer id) {
        Object response =  httpProxyService.get("/notebook_modelview/api/stop/" + id, Object.class);
        return ResponseHolder.build(() -> response);
    }

    @GetMapping("/delete_pod/{id}")
    @Operation(summary = "清理")
    public RestResponse<Object> deletePod(@PathVariable Integer id) {
        Object response =   httpProxyService.get("/notebook_modelview/api/delete_pod/" + id,UserUtil.getAbUserHeaders(), Object.class);
        return ResponseHolder.build(() -> response);
    }


}
