package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.PodDTO;

import java.util.List;

public interface PodService {

    List<PodDTO> getPodsByNamespace(String clusterName, String namespace);

    List<PodDTO> getAllPods(String clusterName);

    PodDTO getPod(String clusterName, String namespace, String podName);

    void deletePod(String clusterName, String namespace, String podName);

    List<PodDTO> getPodsByLabel(String clusterName, String namespace, String labelKey, String labelValue);

    List<PodDTO> getPodsByLabels(String clusterName, String namespace, String appLabel, String podType);

    boolean isPodReady(String clusterName, String namespace, String appLabel);

    /**
     * 获取指定namespace下所有包含app标签的pod
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @return pod列表
     */
    List<PodDTO> getPodsWithAppLabel(String clusterName, String namespace);

    /**
     * 获取指定namespace下pod-type=inference的所有pod
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @return pod列表
     */
    List<PodDTO> getInferencePods(String clusterName, String namespace);

    /**
     * 获取指定namespace下pod-type=task的所有pod
     * @param clusterName 集群名称
     * @param namespace 命名空间
     * @return pod列表
     */
    List<PodDTO> getTaskPods(String clusterName, String namespace);
}
