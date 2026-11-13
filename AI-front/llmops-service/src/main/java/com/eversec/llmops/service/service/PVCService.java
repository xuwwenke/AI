package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.PVCDTO;

import java.util.List;
import java.util.Map;

public interface PVCService {

    /**
     * 获取指定namespace下的所有PVC
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @return PVC列表
     */
    List<PVCDTO> getPVCsByNamespace(String clusterName, String namespace);

    /**
     * 获取指定namespace下的单个PVC
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return PVC详情
     */
    PVCDTO getPVC(String clusterName, String namespace, String pvcName);

    /**
     * 创建PVC
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @param storageClass 存储类
     * @param storageSize 存储大小
     * @param accessModes 访问模式
     * @param labels 标签
     * @return 创建的PVC
     */
    PVCDTO createPVC(String clusterName, String namespace, String pvcName,
                     String storageClass, String storageSize, List<String> accessModes,
                     Map<String, String> labels);

    /**
     * 删除PVC
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @param pvcName PVC名称
     */
    void deletePVC(String clusterName, String namespace, String pvcName);

    void markPVCToDelete(String clusterName, String namespace, String pvcName, String deletedAt);

    /**
     * 根据标签选择器获取PVC列表
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @param labels 标签选择器
     * @return PVC列表
     */
    List<PVCDTO> getPVCsByLabels(String clusterName, String namespace, Map<String, String> labels);

    /**
     * 检查PVC是否存在
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return 是否存在
     */
    boolean exists(String clusterName, String namespace, String pvcName);
}
