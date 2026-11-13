package com.eversec.llmops.service.controller;

import com.eversec.llmops.service.dto.ImageNameDTO;
import com.eversec.llmops.service.dto.ImagesDTO;
import com.eversec.llmops.service.entity.Images;
import com.eversec.llmops.service.service.ImagesService;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.llmops.service.service.impl.HttpProxyService;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImagesController {

    @Autowired
    private ImagesService imagesService;

    @Autowired
    private HttpProxyService httpProxyService;


    @Autowired
    private ProjectService projectService;


    /**
     * 根据ID获取详情
     * @param id ID
     * @return Images对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public RestResponse<Images> getById(@PathVariable Integer id) {
        return ResponseHolder.build(() -> imagesService.getById(id));
    }

    /**
     * 分页查询
     * @param page 页码
     * @param pageSize 每页数量
     * @return QueryResult
     */
//    @GetMapping("/page")
//    public RestResponse<QueryResult<Images>> getPageList(
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer pageSize) {
//        return ResponseHolder.build(() -> imagesService.getPageList(page, pageSize));
//    }

    /**
     * 自定义分页查询（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 镜像名称
     * @param creator 创建者名称
     * @param describe 描述
     * @return QueryResult
     */
    @GetMapping
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Images>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String creator,
            @RequestParam(required = false) String describe,
            @RequestHeader String namespace) {
        Integer projectId = projectService.getByName(namespace).getId();
        return ResponseHolder.build(() -> imagesService.customGetPageList(pageNum, pageSize, name, creator, describe, projectId));
    }

    /**
     * 创建镜像
     * @param imagesDTO ImagesDTO对象
     * @return 响应
     */
    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Object> createImage(@RequestBody ImagesDTO imagesDTO,@RequestHeader String namespace) {
        Images images = getImages(imagesDTO);
        images.setCreatedByFk(UserUtil.getAbUserID());
        images.setChangedByFk(images.getCreatedByFk());
        images.setProjectId(imagesDTO.getProject());
        images.setBizProjectId(projectService.getByName(namespace).getId());
        return ResponseHolder.build(() -> imagesService.save(images));
    }

    @NotNull
    private Images getImages(ImagesDTO imagesDTO) {
        Images images = new Images();
        images.setId(imagesDTO.getId());
        images.setName(imagesDTO.getName());
        images.setDescribe(imagesDTO.getDescribe());
        images.setRepositoryId(imagesDTO.getRepository());
        images.setDockerfile(imagesDTO.getDockerfile());
        images.setGitpath(imagesDTO.getGitpath());
        images.setProjectId(imagesDTO.getProject());
        return images;
    }

    /**
     * 更新镜像
     * @param imagesDTO ImagesDTO对象
     * @return 响应
     */
    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Object> updateImage(@RequestBody ImagesDTO imagesDTO,@RequestHeader String namespace) {
        Images images  = getImages(imagesDTO);
        images.setChangedByFk(UserUtil.getAbUserID());
        return ResponseHolder.build(() -> imagesService.updateById(images));
    }

    /**
     * 删除镜像
     * @param id 镜像ID
     * @return 响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Object> delete(@PathVariable Integer id) {
        Object response = httpProxyService.delete("/images_modelview/api/" + id, Object.class);
        return ResponseHolder.build(() -> response);
    }

    @GetMapping("/names")
    @Operation(summary = "查询镜像名称和id")
    public RestResponse<List<ImageNameDTO>> getImageNames(@RequestHeader String namespace) {
        return ResponseHolder.build(() -> imagesService.getImageNamesByNamespace(namespace));
    }

    @GetMapping("/namesToSelect")
    @Operation(summary = "查询镜像名称和id")
    public RestResponse<List<ImageNameDTO>> getImageNamesToSelect(@RequestHeader String namespace) {
        Integer projectId = projectService.getByName(namespace).getId();
        return ResponseHolder.build(() -> imagesService.getImageNamesToSelect(projectId));
    }
}
