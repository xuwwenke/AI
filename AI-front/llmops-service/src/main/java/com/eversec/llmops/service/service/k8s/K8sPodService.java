package com.eversec.llmops.service.service.k8s;

import io.kubernetes.client.openapi.models.V1ContainerStateWaiting;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangfei
 * @date 2021/8/13
 */
@Slf4j
@Service
public class K8sPodService {
    public V1PodList getPods(String clusterName, Void ignore, V1PodList podList) {
        podList.getItems().forEach(pod -> {
            if (hasReason(pod)) {
                updatePodReason(pod);
            }
        });
        return podList;
    }

    /**
     * 返回true，标识需要调整reason和message
     * @param pod 容器组
     * @return true=需要调整reason和message
     */
    private boolean hasReason(V1Pod pod) {
        if (pod.getStatus() == null) {
            return false;
        }
        if (!StringUtils.isEmpty(pod.getStatus().getReason())) {
            return false;
        }
        return true;
    }

    private void updatePodReason(V1Pod pod) {
        if (pod.getStatus() == null) {
            return;
        }
        V1ContainerStateWaiting stateWaiting = getContainerStateWaiting(pod.getStatus().getInitContainerStatuses());
        if (stateWaiting == null) {
            stateWaiting = getContainerStateWaiting(pod.getStatus().getContainerStatuses());
        }
        if (stateWaiting != null) {
            pod.getStatus().setReason(stateWaiting.getReason());
            pod.getStatus().setMessage(stateWaiting.getMessage());
        }
    }

    private V1ContainerStateWaiting getContainerStateWaiting(List<V1ContainerStatus> containerStatuses) {
        if (containerStatuses == null) {
            return null;
        }
        for (V1ContainerStatus containerStatus : containerStatuses) {
            if (!containerStatus.getReady() || (containerStatus.getStarted() != null && !containerStatus.getStarted())) {
                if (containerStatus.getState() != null) {
                    return containerStatus.getState().getWaiting();
                }
            }
        }
        return null;
    }
}
