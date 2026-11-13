package com.eversec.llmops.service.service;

import com.eversec.llmops.service.dto.ClusterUsageDTO;
import com.eversec.llmops.service.dto.ResourceQuotaDTO;

public interface KubernetesResourceService {

    boolean createNamespace(String clusterName, String namespaceName);

    boolean createResourceQuota(String clusterName, String namespaceName, Integer cpu, Integer memory, Integer gpu, Integer storage);

    ResourceQuotaDTO getResourceQuota(String clusterName, String namespaceName);

    ClusterUsageDTO getClusterUsage(String clusterName);
}