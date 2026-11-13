package com.eversec.llmops.service.controller;

import com.eversec.framework.core.data.QueryResult;
import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.framework.core.response2.RestResponse;
import com.eversec.llmops.service.dto.DatasetRequestDTO;
import com.eversec.llmops.service.entity.Dataset;
import com.eversec.llmops.service.service.DatasetService;
import com.eversec.llmops.service.service.FileDeleteRecordService;
import com.eversec.llmops.service.service.ProjectService;
import com.eversec.llmops.service.util.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/api/dataset")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FileDeleteRecordService fileDeleteRecordService;
    /**
     * 根据ID获取数据集详情
     * @param id 数据集ID
     * @return 数据集对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "详情")
    public Dataset getById(@PathVariable Integer id) {
        return datasetService.getById(id);
    }



    /**
     * 自定义分页查询数据集（不使用MyBatis Plus）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 数据集名称
     * @param version 数据集版本
     * @param describe 数据集描述
     * @return 包含数据集列表和总数的RestResponse
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public RestResponse<QueryResult<Dataset>> customGetPageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String describe,
            @RequestParam(required = false) Boolean ifPublic,
            @RequestHeader String namespace) {
        Integer projectId = null;
        if(!ifPublic){
            projectId = projectService.getByName(namespace).getId();
        }
        QueryResult<Dataset> result = datasetService.customGetPageList(pageNum, pageSize, name, version, describe, projectId);
        return ResponseHolder.build(() -> result);
    }

    /**
     * 新增数据集
     * @param dto 数据集请求DTO
     * @return 新增的数据集对象
     */
    @PostMapping
    @Operation(summary = "新增")
    public RestResponse<Dataset> add(@RequestBody DatasetRequestDTO dto,@RequestHeader String namespace) {
        Dataset dataset = getDataset(dto, namespace);
        return ResponseHolder.build(() -> {
            datasetService.save(dataset);
            return dataset;
        });
    }

    @NotNull
    private Dataset getDataset(DatasetRequestDTO dto, String namespace) {
        Dataset dataset = new Dataset();
        dataset.setName(dto.getName());
        dataset.setVersion(dto.getVersion());
        dataset.setLabel(dto.getLabel());
        dataset.setDescribe(dto.getDescribe());
        dataset.setUrl(dto.getUrl());
        dataset.setDownloadUrl(dto.getDownloadUrl());
        dataset.setPath(dto.getPath());
        dataset.setIcon(dto.getIcon());
        dataset.setOwner(dto.getOwner() != null ? dto.getOwner() : UserUtil.getAbUserUserName());
        dataset.setFeatures(dto.getFeatures());
        Integer abUserId = UserUtil.getAbUserID();
        dataset.setCreatedByFk(abUserId);
        dataset.setChangedByFk(abUserId);
        dataset.setCreatedOn(new Date());
        dataset.setChangedOn(new Date());
        dataset.setStorageClass("");
        dataset.setFileType("");
        dataset.setStatus("");
        Date date = new Date();
        dataset.setYears(String.valueOf(date.getYear()));
        dataset.setInfo( "{}");
        dataset.setMetricInfo("{}");
        dataset.setSecret("");
        dataset.setSegment("{}");
        dataset.setProjectId(projectService.getByName(namespace).getId());
        return dataset;
    }

    /**
     * 修改数据集
     * @param dataset 数据集对象
     * @return 修改的数据集对象
     */
    @PutMapping
    @Operation(summary = "修改")
    public RestResponse<Dataset> update(@RequestBody Dataset dataset, HttpServletRequest request) {
        Integer abUserId = UserUtil.getAbUserID(request);
        dataset.setChangedByFk(abUserId);
        return ResponseHolder.build(() -> {
            datasetService.updateById(dataset);
            return dataset;
        });
    }

    /**
     * 删除数据集
     * @param id 数据集ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public RestResponse<Boolean> delete(@PathVariable Integer id) {
        return ResponseHolder.build(() -> datasetService.delete(id,UserUtil.getAbUserUserName()));
    }
}

