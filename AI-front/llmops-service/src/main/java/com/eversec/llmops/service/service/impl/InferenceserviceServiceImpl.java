package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.InferenceServiceStatsDTO;
import com.eversec.llmops.service.entity.Inferenceservice;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.mapper.InferenceserviceMapper;
import com.eversec.llmops.service.service.ClusterService;
import com.eversec.llmops.service.service.InferenceserviceService;
import com.eversec.llmops.service.service.PodService;
import com.eversec.llmops.service.service.ProManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class InferenceserviceServiceImpl extends ServiceImpl<InferenceserviceMapper, Inferenceservice> implements InferenceserviceService {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ProManagerService proManagerService;

    @Autowired
    private PodService podService;
    @Override
    public QueryResult<Inferenceservice> getPageList(Integer pageNum, Integer pageSize, String namespace, String name, String label, String modelName, String modelVersion, String modelUrl, String status, String gpu) {
        int offset = (pageNum - 1) * pageSize;
        List<Inferenceservice> inferenceserviceList = baseMapper.customGetPageList(offset, pageSize, namespace, name, label, modelName, modelVersion, modelUrl, status, gpu);
        ProManager proManager = proManagerService.getByName(namespace);
        String serviceIp = null;
        String clusterName = proManager != null ? proManager.getClusterName() : null;

        for (Inferenceservice inferenceservice : inferenceserviceList) {
            if (serviceIp == null && inferenceservice.getClusterName() != null) {
                serviceIp = clusterService.getServiceIpByName(inferenceservice.getClusterName());
            }
            if (serviceIp != null) {
                Integer port = inferenceservice.getId() * 10 + 20000;
                inferenceservice.setIp(serviceIp + ":" + port);
            }

            if (inferenceservice.getModelStatus().equals("online")) {
                if (clusterName != null) {
                    boolean podReady = podService.isPodReady(clusterName, namespace, inferenceservice.getName());
                    if (podReady) {
                        inferenceservice.setStatus("online");
                    } else {
                        inferenceservice.setStatus("deploying");
                    }
                } else {
                    inferenceservice.setStatus("deploying");
                }
            } else {
                inferenceservice.setStatus(inferenceservice.getModelStatus());
            }
        }

        int totalCount = baseMapper.customGetTotalCount(namespace, name, label, modelName, modelVersion, modelUrl, status, gpu);

        QueryResult<Inferenceservice> queryResult = new QueryResult<>();
        queryResult.setResultData(inferenceserviceList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public Inferenceservice getByIdWithProManager(Integer id) {
        Inferenceservice inferenceservice = baseMapper.selectByIdWithProManager(id);
        return inferenceservice;
    }

    @Override
    public Integer getReadyOnlineCountByProjectId(Integer projectId) {
        List<Inferenceservice> onlineList = baseMapper.selectOnlineByProjectId(projectId);
        if (onlineList == null || onlineList.isEmpty()) {
            return 0;
        }
        
        // 获取第一个inferenceservice来确定clusterName和namespace
        Inferenceservice firstService = onlineList.get(0);
        String clusterName = firstService.getClusterName();
        String namespace = firstService.getProName();
        if (clusterName == null || namespace == null) {
            return 0;
        }
        
        // 一次性获取所有包含app标签的pod
        List<com.eversec.llmops.service.dto.PodDTO> allPods = podService.getPodsWithAppLabel(clusterName, namespace);
        
        // 将inferenceservice名称放入一个set中方便查找
        java.util.Set<String> serviceNames = new java.util.HashSet<>();
        for (Inferenceservice service : onlineList) {
            serviceNames.add(service.getName());
        }
        
        // 统计就绪的pod数量
        int count = 0;
        for (com.eversec.llmops.service.dto.PodDTO pod : allPods) {
            if (serviceNames.contains(pod.getAppLabel())) {
                if (isPodReady(pod)) {
                    count++;
                    // 一个服务只需统计一次就绪的pod
                    serviceNames.remove(pod.getAppLabel());
                }
            }
        }
        
        return count;
    }

    @Override
    public Integer getOnlineCountByProjectId(Integer projectId) {
        if (projectId == null) {
            return 0;
        }
        return baseMapper.selectOnlineCountByProjectId(projectId);
    }
    
    private boolean isPodReady(com.eversec.llmops.service.dto.PodDTO pod) {
        if (pod.getContainerStatuses() == null || pod.getContainerStatuses().isEmpty()) {
            return false;
        }
        for (com.eversec.llmops.service.dto.PodDTO.ContainerStatus containerStatus : pod.getContainerStatuses()) {
            if (!"true".equals(containerStatus.getReady())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Integer> getInferencePodCounts(String clusterName, String namespace) {
        Map<String, Integer> result = new HashMap<>();
        int readyCount = 0;
        int unreadyCount = 0;
        
        if (clusterName == null || namespace == null) {
            result.put("serviceReadyCount", 0);
            result.put("serviceUnreadyCount", 0);
            return result;
        }
        
        List<com.eversec.llmops.service.dto.PodDTO> pods = podService.getInferencePods(clusterName, namespace);
        for (com.eversec.llmops.service.dto.PodDTO pod : pods) {
            if (isPodReady(pod)) {
                readyCount++;
            } else {
                unreadyCount++;
            }
        }
        
        result.put("serviceReadyCount", readyCount);
        result.put("serviceUnreadyCount", unreadyCount);
        return result;
    }

    @Override
    public InferenceServiceStatsDTO getStatsByProjectId(Integer projectId) {
        InferenceServiceStatsDTO stats = new InferenceServiceStatsDTO();
        
        // 获取总数
        stats.setTotalCount(baseMapper.selectTotalCountByProjectId(projectId));
        
        // 获取本月新增数
        stats.setThisMonthCount(baseMapper.selectThisMonthCountByProjectId(projectId));
        
        // 获取在线数（model_status=online 且 pod 就绪）
        List<Inferenceservice> allServices = baseMapper.selectAllByProjectId(projectId);
        if (allServices == null || allServices.isEmpty()) {
            stats.setOnlineCount(0);
            return stats;
        }
        
        // 获取所有model_status=online的服务
        List<Inferenceservice> onlineServices = allServices.stream()
                .filter(s -> "online".equals(s.getModelStatus()))
                .collect(Collectors.toList());
        
        if (onlineServices.isEmpty()) {
            stats.setOnlineCount(0);
            return stats;
        }
        
        // 获取第一个服务来确定clusterName和namespace
        Inferenceservice firstService = onlineServices.get(0);
        String clusterName = firstService.getClusterName();
        String namespace = firstService.getProName();
        
        if (clusterName == null || namespace == null) {
            stats.setOnlineCount(0);
            return stats;
        }
        
        // 一次性获取所有inference类型的pod，过滤掉debug pod
        List<com.eversec.llmops.service.dto.PodDTO> allPods = podService.getInferencePods(clusterName, namespace).stream()
                .filter(pod -> pod.getName() == null || !pod.getName().contains("debug"))
                .collect(Collectors.toList());
        
        // 将online服务名称放入一个set中方便查找
        Set<String> onlineServiceNames = new HashSet<>();
        for (Inferenceservice service : onlineServices) {
            onlineServiceNames.add(service.getName());
        }
        
        // 统计就绪的pod数量
        int onlineCount = 0;
        for (com.eversec.llmops.service.dto.PodDTO pod : allPods) {
            if (onlineServiceNames.contains(pod.getAppLabel())) {
                if (isPodReady(pod)) {
                    onlineCount++;
                    // 一个服务只需统计一次就绪的pod
                    onlineServiceNames.remove(pod.getAppLabel());
                }
            }
        }
        
        stats.setOnlineCount(onlineCount);
        return stats;
    }

}
