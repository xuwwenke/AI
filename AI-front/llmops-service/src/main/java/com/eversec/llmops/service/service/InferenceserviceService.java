package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.InferenceServiceStatsDTO;
import com.eversec.llmops.service.entity.Inferenceservice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.framework.core.data.QueryResult;

public interface InferenceserviceService extends IService<Inferenceservice> {
    /**
     * 分页查询
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param namespace 项目名称
     * @param name 名称
     * @param label 标签
     * @param modelName 模型名称
     * @param modelVersion 模型版本
     * @param modelUrl 模型URL
     * @param status 状态
     * @param gpu GPU
     * @return QueryResult
     */
    QueryResult<Inferenceservice> getPageList(Integer pageNum, Integer pageSize, String namespace, String name, String label, String modelName, String modelVersion, String modelUrl, String status, String gpu);

    /**
     * 通过id查询，关联pro_manager信息
     * @param id 主键id
     * @return Inferenceservice
     */
    Inferenceservice getByIdWithProManager(Integer id);

    /**
     * 通过projectId获取处于online状态且pod就绪的inferenceservice数量
     * @param projectId 项目id
     * @return 数量
     */
    Integer getReadyOnlineCountByProjectId(Integer projectId);

    /**
     * 通过projectId获取model_status为online的inferenceservice数量
     * @param projectId 项目id
     * @return 数量
     */
    Integer getOnlineCountByProjectId(Integer projectId);

    /**
     * 通过clusterName和namespace获取inference类型的pod数量统计
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @return 包含readyCount和unreadyCount的Map
     */
    java.util.Map<String, Integer> getInferencePodCounts(String clusterName, String namespace);

    /**
     * 通过projectId获取统计信息
     * @param projectId 项目id
     * @return 统计信息DTO
     */
    InferenceServiceStatsDTO getStatsByProjectId(Integer projectId);
    }
