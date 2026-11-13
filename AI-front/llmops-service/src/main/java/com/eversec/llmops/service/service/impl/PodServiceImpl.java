package com.eversec.llmops.service.service.impl;

import com.eversec.llmops.service.client.K8sClientFactory;
import com.eversec.llmops.service.dto.PodDTO;
import com.eversec.llmops.service.service.PodService;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PodServiceImpl implements PodService {

    @Autowired
    private K8sClientFactory k8sClientFactory;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<PodDTO> getPodsByNamespace(String clusterName, String namespace) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.pods().inNamespace(namespace).list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PodDTO> getAllPods(String clusterName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.pods().list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PodDTO getPod(String clusterName, String namespace, String podName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
        return pod != null ? convertToPodDTO(pod) : null;
    }

    @Override
    public void deletePod(String clusterName, String namespace, String podName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        client.pods().inNamespace(namespace).withName(podName).delete();
    }

    @Override
    public List<PodDTO> getPodsByLabel(String clusterName, String namespace, String labelKey, String labelValue) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.pods().inNamespace(namespace)
                .withLabel(labelKey, labelValue)
                .list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PodDTO> getPodsByLabels(String clusterName, String namespace, String appLabel, String podType) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.pods().inNamespace(namespace)
                .withLabel("app", appLabel)
                .withLabel("pod-type", podType)
                .list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPodReady(String clusterName, String namespace, String appLabel) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        List<PodDTO> pods = client.pods().inNamespace(namespace)
                .withLabel("app", appLabel)
                .list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
        
        if (pods == null || pods.isEmpty()) {
            return false;
        }
        for (PodDTO pod : pods) {
            if (pod.getContainerStatuses() == null || pod.getContainerStatuses().isEmpty()) {
                continue;
            }
            boolean allReady = true;
            for (PodDTO.ContainerStatus containerStatus : pod.getContainerStatuses()) {
                if (!"true".equals(containerStatus.getReady())) {
                    allReady = false;
                    break;
                }
            }
            if (allReady) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PodDTO> getPodsWithAppLabel(String clusterName, String namespace) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.pods().inNamespace(namespace)
                .withLabel("app")
                .list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PodDTO> getInferencePods(String clusterName, String namespace) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.pods().inNamespace(namespace)
                .withLabel("pod-type", "inference")
                .list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PodDTO> getTaskPods(String clusterName, String namespace) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.pods().inNamespace(namespace)
                .withLabel("pod-type", "task")
                .list().getItems().stream()
                .map(this::convertToPodDTO)
                .collect(Collectors.toList());
    }

    private PodDTO convertToPodDTO(Pod pod) {
        PodDTO dto = new PodDTO();
        dto.setName(pod.getMetadata().getName());
        dto.setNamespace(pod.getMetadata().getNamespace());
        dto.setPhase(pod.getStatus().getPhase());
        dto.setNodeName(pod.getSpec().getNodeName());
        dto.setPodIp(pod.getStatus().getPodIP());
        if (pod.getMetadata().getLabels() != null) {
            dto.setAppLabel(pod.getMetadata().getLabels().get("app"));
        }
        
        if (pod.getMetadata().getCreationTimestamp() != null) {
            dto.setCreatedAt(formatTimestamp(pod.getMetadata().getCreationTimestamp()));
        }

        int totalRestartCount = 0;
        List<PodDTO.ContainerStatus> containerStatuses = new ArrayList<>();
        if (pod.getStatus().getContainerStatuses() != null) {
            for (ContainerStatus status : pod.getStatus().getContainerStatuses()) {
                PodDTO.ContainerStatus containerStatus = new PodDTO.ContainerStatus();
                containerStatus.setName(status.getName());
                containerStatus.setImage(status.getImage());
                containerStatus.setReady(String.valueOf(status.getReady()));
                containerStatus.setRestartCount(String.valueOf(status.getRestartCount()));
                
                if (status.getState() != null) {
                    if (status.getState().getRunning() != null) {
                        containerStatus.setState("Running");
                    } else if (status.getState().getWaiting() != null) {
                        containerStatus.setState("Waiting");
                    } else if (status.getState().getTerminated() != null) {
                        containerStatus.setState("Terminated");
                    } else {
                        containerStatus.setState("Unknown");
                    }
                }
                containerStatuses.add(containerStatus);
                totalRestartCount += status.getRestartCount();
            }
        }
        dto.setContainerStatuses(containerStatuses);
        dto.setRestartCount(String.valueOf(totalRestartCount));
        
        return dto;
    }

    private String formatTimestamp(String timestamp) {
        try {
            Instant instant = Instant.parse(timestamp);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return localDateTime.format(DATE_FORMATTER);
        } catch (Exception e) {
            return timestamp;
        }
    }
}
