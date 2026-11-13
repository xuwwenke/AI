package com.eversec.llmops.service.service.impl;

import com.eversec.llmops.service.client.K8sClientFactory;
import com.eversec.llmops.service.dto.PVCDTO;
import com.eversec.llmops.service.service.PVCService;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PVCServiceImpl implements PVCService {

    @Autowired
    private K8sClientFactory k8sClientFactory;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PVC_TO_DELETE_LABEL_KEY = "to_delete";
    private static final String PVC_DELETE_AT_ANNOTATION_KEY = "deleted_at";

    @Override
    public List<PVCDTO> getPVCsByNamespace(String clusterName, String namespace) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        return client.persistentVolumeClaims().inNamespace(namespace).list().getItems().stream()
                .map(this::convertToPVCDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PVCDTO getPVC(String clusterName, String namespace, String pvcName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        PersistentVolumeClaim pvc = client.persistentVolumeClaims().inNamespace(namespace).withName(pvcName).get();
        return pvc != null ? convertToPVCDTO(pvc) : null;
    }

    @Override
    public PVCDTO createPVC(String clusterName, String namespace, String pvcName,
                          String storageClass, String storageSize, List<String> accessModes,
                          Map<String, String> labels) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        Map<String, String> pvcLabels = labels != null ? new HashMap<>(labels) : new HashMap<>();
        Map<String, Quantity> requests = new HashMap<>();
        requests.put("storage", new Quantity(storageSize));
        ResourceRequirements   resourceRequirements = new ResourceRequirements();
        resourceRequirements.setRequests(requests);
        PersistentVolumeClaimBuilder pvcBuilder = new PersistentVolumeClaimBuilder();
        pvcBuilder.withNewMetadata()
                .withName(pvcName)
                .withNamespace(namespace)
                .withLabels(pvcLabels)
                .endMetadata()
                .withNewSpec()
                .withStorageClassName(storageClass)
                .withResources(resourceRequirements)
                .withAccessModes(accessModes).endSpec();
        PersistentVolumeClaim pvc = client.persistentVolumeClaims().inNamespace(namespace).createOrReplace(pvcBuilder.build());
        return convertToPVCDTO(pvc);
    }

    @Override
    public void deletePVC(String clusterName, String namespace, String pvcName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        client.persistentVolumeClaims().inNamespace(namespace).withName(pvcName).delete();
    }

    @Override
    public void markPVCToDelete(String clusterName, String namespace, String pvcName, String deletedAt) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        PersistentVolumeClaim pvc = client.persistentVolumeClaims().inNamespace(namespace).withName(pvcName).get();
        if (pvc == null) {
            return;
        }

        Map<String, String> labels = pvc.getMetadata().getLabels();
        if (labels == null) {
            labels = new HashMap<>();
        }
        labels.put(PVC_TO_DELETE_LABEL_KEY, "true");
        pvc.getMetadata().setLabels(labels);

        Map<String, String> annotations = pvc.getMetadata().getAnnotations();
        if (annotations == null) {
            annotations = new HashMap<>();
        }
        annotations.put(PVC_DELETE_AT_ANNOTATION_KEY, deletedAt);
        pvc.getMetadata().setAnnotations(annotations);

        client.persistentVolumeClaims().inNamespace(namespace).withName(pvcName).replace(pvc);
    }

    @Override
    public List<PVCDTO> getPVCsByLabels(String clusterName, String namespace, Map<String, String> labels) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);

        List<PersistentVolumeClaim> pvcList;
        if (labels != null && !labels.isEmpty()) {
            pvcList = client.persistentVolumeClaims().inNamespace(namespace).withLabels(labels).list().getItems();
        } else {
            pvcList = client.persistentVolumeClaims().inNamespace(namespace).list().getItems();
        }

        return pvcList.stream()
                .map(this::convertToPVCDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String clusterName, String namespace, String pvcName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        PersistentVolumeClaim pvc = client.persistentVolumeClaims().inNamespace(namespace).withName(pvcName).get();
        return pvc != null;
    }

    private PVCDTO convertToPVCDTO(PersistentVolumeClaim pvc) {
        PVCDTO dto = new PVCDTO();
        dto.setName(pvc.getMetadata().getName());
        dto.setNamespace(pvc.getMetadata().getNamespace());
        dto.setLabels(pvc.getMetadata().getLabels());

        if (pvc.getStatus() != null) {
            dto.setStatus(pvc.getStatus().getPhase());
        }

        if (pvc.getSpec() != null) {
            dto.setStorageClass(pvc.getSpec().getStorageClassName());

            if (pvc.getSpec().getAccessModes() != null && !pvc.getSpec().getAccessModes().isEmpty()) {
                dto.setAccessMode(String.join(", ", pvc.getSpec().getAccessModes()));
            }

            if (pvc.getSpec().getResources() != null && pvc.getSpec().getResources().getRequests() != null) {
                Quantity storage = pvc.getSpec().getResources().getRequests().get("storage");
                if (storage != null) {
                    dto.setCapacity(storage.getAmount() + storage.getFormat());
                }
            }
        }

        if (pvc.getSpec() != null && pvc.getSpec().getVolumeName() != null) {
            dto.setVolumeName(pvc.getSpec().getVolumeName());
        }

        if (pvc.getMetadata().getCreationTimestamp() != null) {
            dto.setCreatedAt(formatTimestamp(pvc.getMetadata().getCreationTimestamp()));
        }

        if (pvc.getMetadata().getLabels() != null) {
            String pvcType = pvc.getMetadata().getLabels().get("pvc-type");
            if (pvcType != null) {
                dto.setPvcType(pvcType);
            }
        }

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
