package com.eversec.llmops.service.client;

import com.eversec.llmops.service.dto.PVCDTO;
import com.eversec.llmops.service.dto.ResourceQuotaDTO;
import com.eversec.llmops.service.dto.StorageOverViewDTO;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.service.KubernetesResourceService;
import com.eversec.llmops.service.service.PVCService;
import com.eversec.llmops.service.service.ProManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OpenShiftClientService {

    @Autowired
    private KubernetesResourceService kubernetesResourceService;
    @Autowired
    private ProManagerService proManagerService;
    @Autowired
    private PVCService pvcService;

    private String getClusterNameByNamespace(String namespaceName) {
        ProManager proManager = proManagerService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProManager>()
                .eq(ProManager::getProName, namespaceName));
        if (proManager != null && proManager.getClusterName() != null) {
            return proManager.getClusterName();
        }
        throw new RuntimeException("Cannot find cluster name for namespace: " + namespaceName);
    }

    public ResourceQuotaDTO getResourceQuota(String namespaceName) {
        return kubernetesResourceService.getResourceQuota(getClusterNameByNamespace(namespaceName), namespaceName);
    }


    /**
     * 获取存储概览信息
     * @param namespaceName 命名空间名称
     * @return StorageOverViewDTO 存储概览
     */
    public StorageOverViewDTO getStorageOverView(String namespaceName) {
        String clusterName = getClusterNameByNamespace(namespaceName);
        // 获取所有PVC
        java.util.List<PVCDTO> pvcList = pvcService.getPVCsByNamespace(clusterName, namespaceName);
        // 计算PVC数量
        int totalPvcNums = pvcList != null ? pvcList.size() : 0;
        int boundPvcNums = 0;
        int pendingPvcNums = 0;
        if (pvcList != null && !pvcList.isEmpty()) {
            for (PVCDTO pvc : pvcList) {
                String status = pvc.getStatus();
                if ("Bound".equals(status)) {
                    boundPvcNums++;
                } else if ("Pending".equals(status)) {
                    pendingPvcNums++;
                }
            }
        }
        StorageOverViewDTO storageOverViewDTO = new StorageOverViewDTO();
        ResourceQuotaDTO resourceQuota = kubernetesResourceService.getResourceQuota(clusterName, namespaceName);
        storageOverViewDTO.setStorageLimit(resourceQuota.getStorageLimit());
        storageOverViewDTO.setStorageUsed(resourceQuota.getStorageUsed());
        storageOverViewDTO.setPvcNums(totalPvcNums);
        storageOverViewDTO.setBoundPvcNums(boundPvcNums);
        storageOverViewDTO.setPendingPvcNums(pendingPvcNums);
        return storageOverViewDTO;
    }
}