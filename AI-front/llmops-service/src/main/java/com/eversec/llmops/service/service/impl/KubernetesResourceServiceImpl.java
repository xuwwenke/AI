package com.eversec.llmops.service.service.impl;

import com.eversec.llmops.service.client.K8sClientFactory;
import com.eversec.llmops.service.dto.ClusterUsageDTO;
import com.eversec.llmops.service.dto.ResourceQuotaDTO;
import com.eversec.llmops.service.service.KubernetesResourceService;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleRef;
import io.fabric8.kubernetes.api.model.rbac.Subject;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KubernetesResourceServiceImpl implements KubernetesResourceService {

    private static final String RBAC_API_GROUP = "rbac.authorization.k8s.io";
    private static final String CLUSTER_ROLE_KIND = "ClusterRole";
    private static final String SERVICE_ACCOUNT_KIND = "ServiceAccount";
    private static final String KUBE_SYSTEM_NAMESPACE = "kube-system";

    private static final String PIPELINE_RUNNER_NAME = "pipeline-runner";
    private static final String PIPELINE_RUNNER_BINDING_NAME = "pipeline-runner-binding";
    private static final String CLUSTER_ROLE_PIPELINE_RUNNER = "pipeline-runner";

    private static final String NOTEBOOK_BINDING_NAME = "kubernetes-dashboard-project-jupyter";
    private static final String CLUSTER_ROLE_NOTEBOOK = "kubernetes-dashboard-jupyter";
    private static final String SERVICE_ACCOUNT_NOTEBOOK = "kubernetes-dashboard-user1";

    @Autowired
    private K8sClientFactory k8sClientFactory;

    @Override
    public boolean createNamespace(String clusterName, String namespaceName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        Namespace namespace = new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespaceName)
                .endMetadata()
                .build();
        client.namespaces().create(namespace);

        createServiceAccount(client, namespaceName);
        createPipelineRunnerRoleBinding(client, namespaceName);
        createNotebookRoleBinding(client, namespaceName);

        return true;
    }

    private void createServiceAccount(OpenShiftClient client, String namespaceName) {
        ServiceAccount serviceAccount = new ServiceAccountBuilder()
                .withNewMetadata()
                .withName(PIPELINE_RUNNER_NAME)
                .withNamespace(namespaceName)
                .endMetadata()
                .build();

        client.serviceAccounts().inNamespace(namespaceName).create(serviceAccount);
    }

    private void createPipelineRunnerRoleBinding(OpenShiftClient client, String namespaceName) {
        RoleRef roleRef = new RoleRef();
        roleRef.setApiGroup(RBAC_API_GROUP);
        roleRef.setKind(CLUSTER_ROLE_KIND);
        roleRef.setName(CLUSTER_ROLE_PIPELINE_RUNNER);

        Subject subject = new Subject();
        subject.setKind(SERVICE_ACCOUNT_KIND);
        subject.setName(PIPELINE_RUNNER_NAME);
        subject.setNamespace(namespaceName);

        List<Subject> subjects = new ArrayList<>();
        subjects.add(subject);

        RoleBinding roleBinding = new RoleBindingBuilder()
                .withNewMetadata()
                .withName(PIPELINE_RUNNER_BINDING_NAME)
                .withNamespace(namespaceName)
                .endMetadata()
                .withRoleRef(roleRef)
                .withSubjects(subjects)
                .build();

        client.rbac().roleBindings().inNamespace(namespaceName).create(roleBinding);
    }

    private void createNotebookRoleBinding(OpenShiftClient client, String namespaceName) {
        RoleRef roleRef = new RoleRef();
        roleRef.setApiGroup(RBAC_API_GROUP);
        roleRef.setKind(CLUSTER_ROLE_KIND);
        roleRef.setName(CLUSTER_ROLE_NOTEBOOK);

        Subject subject = new Subject();
        subject.setKind(SERVICE_ACCOUNT_KIND);
        subject.setName(SERVICE_ACCOUNT_NOTEBOOK);
        subject.setNamespace(KUBE_SYSTEM_NAMESPACE);

        List<Subject> subjects = new ArrayList<>();
        subjects.add(subject);

        RoleBinding roleBinding = new RoleBindingBuilder()
                .withNewMetadata()
                .withName(NOTEBOOK_BINDING_NAME)
                .withNamespace(namespaceName)
                .endMetadata()
                .withRoleRef(roleRef)
                .withSubjects(subjects)
                .build();

        client.rbac().roleBindings().inNamespace(namespaceName).create(roleBinding);
    }

    @Override
    public boolean createResourceQuota(String clusterName, String namespaceName, Integer cpu, Integer memory, Integer gpu, Integer storage) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        Map<String, Quantity> hardLimits = new HashMap<>();

        String cpuValue = (cpu != null && cpu > 0) ? cpu.toString() : "1";
        hardLimits.put("limits.cpu", new Quantity(cpuValue));

        String memoryValue = (memory != null && memory > 0) ? memory + "Gi" : "1Gi";
        hardLimits.put("limits.memory", new Quantity(memoryValue));

        gpu = gpu == null ? 0 : gpu;
        hardLimits.put("nvidia.com/gpu", new Quantity(gpu.toString()));

        String storageValue = (storage != null && storage > 0) ? storage + "Gi" : "100Gi";
        hardLimits.put("requests.storage", new Quantity(storageValue));

        ResourceQuota quota = new ResourceQuotaBuilder()
                .withNewMetadata()
                .withName(namespaceName + "-quota")
                .withNamespace(namespaceName)
                .endMetadata()
                .withNewSpec()
                .withHard(hardLimits)
                .endSpec()
                .build();

        String quotaName = namespaceName + "-quota";
        ResourceQuota existingQuota = client.resourceQuotas().inNamespace(namespaceName).withName(quotaName).get();

        if (existingQuota != null) {
            quota.getMetadata().setResourceVersion(existingQuota.getMetadata().getResourceVersion());
            client.resourceQuotas().inNamespace(namespaceName).withName(quotaName).replace(quota);
        } else {
            client.resourceQuotas().inNamespace(namespaceName).create(quota);
        }
        return true;
    }

    @Override
    public ResourceQuotaDTO getResourceQuota(String clusterName, String namespaceName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        ResourceQuotaDTO dto = new ResourceQuotaDTO();
        dto.setNamespace(namespaceName);

        try {
            ResourceQuota quota = client.resourceQuotas().inNamespace(namespaceName).withName(namespaceName + "-quota").get();
            if (quota != null) {
                dto.setQuotaName(quota.getMetadata().getName());

                Map<String, Quantity> hard = quota.getSpec().getHard();
                Map<String, Quantity> used = quota.getStatus().getUsed();

                dto.setCpuLimit(getCpuQuantityValue(hard, "limits.cpu"));
                dto.setCpuUsed(getCpuQuantityValue(used, "limits.cpu"));
                dto.setMemoryLimit(getMemoryQuantityValue(hard, "limits.memory"));
                dto.setMemoryUsed(getMemoryQuantityValue(used, "limits.memory"));
                dto.setGpuLimit(getQuantityValue(hard, "nvidia.com/gpu"));
                dto.setGpuUsed(getQuantityValue(used, "nvidia.com/gpu"));
                dto.setStorageLimit(getMemoryQuantityValue(hard, "requests.storage"));
                dto.setStorageUsed(getMemoryQuantityValue(used, "requests.storage"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    @Override
    public ClusterUsageDTO getClusterUsage(String clusterName) {
        OpenShiftClient client = k8sClientFactory.getClient(clusterName);
        ClusterUsageDTO dto = new ClusterUsageDTO();
        dto.setClusterName(clusterName);

        try {
            Map<String, io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics> nodeMetricsMap = new HashMap<>();
            List<io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics> nodeMetricsList = 
                client.top().nodes().metrics().getItems();
            for (io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics metrics : nodeMetricsList) {
                nodeMetricsMap.put(metrics.getMetadata().getName(), metrics);
            }

            List<Node> nodes = client.nodes().list().getItems();

            long totalCpuCores = 0;
            long totalMemoryGi = 0;
            long usedCpuCores = 0;
            long usedMemoryGi = 0;

            for (Node node : nodes) {
                String nodeName = node.getMetadata().getName();

                Map<String, Quantity> capacity = node.getStatus().getAllocatable();
                if (capacity != null) {
                    Quantity cpuQty = capacity.get("cpu");
                    Quantity memQty = capacity.get("memory");
                    if (cpuQty != null) {
                        String format = memQty.getFormat()==null?"":cpuQty.getFormat();
                        totalCpuCores += parseCpuToCores(cpuQty.getAmount()+format);
                    }
                    if (memQty != null) {
                        String format = memQty.getFormat()==null?"":memQty.getFormat();
                        totalMemoryGi += parseMemoryToGi(memQty.getAmount()+format);
                    }
                }

                io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics metrics = nodeMetricsMap.get(nodeName);
                if (metrics != null) {
                    Map<String, Quantity> usage = metrics.getUsage();
                    if (usage != null) {
                        Quantity cpuUsage = usage.get("cpu");
                        Quantity memUsage = usage.get("memory");
                        if (cpuUsage != null) {
                            String format = cpuUsage.getFormat()==null?"":cpuUsage.getFormat();
                            usedCpuCores += parseCpuToCores(cpuUsage.getAmount()+format);
                        }
                        if (memUsage != null) {
                            String format = memUsage.getFormat()==null?"":memUsage.getFormat();
                            usedMemoryGi += parseMemoryToGi(memUsage.getAmount()+format);
                        }
                    }
                }
            }

            dto.setCpu(String.valueOf(totalCpuCores));
            dto.setCpuUsed(String.valueOf(Math.max(0, usedCpuCores)));
            dto.setMemory(totalMemoryGi + "Gi");
            dto.setMemoryUsed(Math.max(0, usedMemoryGi) + "Gi");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    private long parseCpuToCores(String cpuAmount) {
        if (cpuAmount == null || cpuAmount.isEmpty()) {
            return 0;
        }
        try {
            if (cpuAmount.endsWith("m")) {
                return Long.parseLong(cpuAmount.substring(0, cpuAmount.length() - 1)) / 1000;
            } else if (cpuAmount.endsWith("n")) {
                return Long.parseLong(cpuAmount.substring(0, cpuAmount.length() - 1)) / 1000000000;
            } else {
                return Long.parseLong(cpuAmount);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseMemoryToGi(String memoryAmount) {
        if (memoryAmount == null || memoryAmount.isEmpty()) {
            return 0;
        }
        try {
            long bytes = parseMemoryToBytes(memoryAmount);
            return bytes / (1024 * 1024 * 1024);
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseMemoryToBytes(String memoryAmount) {
        if (memoryAmount == null || memoryAmount.isEmpty()) {
            return 0;
        }
        try {
            String amount = memoryAmount;
            long multiplier = 1;

            if (memoryAmount.endsWith("Ki")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = 1024;
            } else if (memoryAmount.endsWith("Mi")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = 1024 * 1024;
            } else if (memoryAmount.endsWith("Gi")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = 1024 * 1024 * 1024;
            } else if (memoryAmount.endsWith("Ti")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = 1024L * 1024 * 1024 * 1024;
            } else if (memoryAmount.endsWith("K")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = 1000;
            } else if (memoryAmount.endsWith("M")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = 1000 * 1000;
            } else if (memoryAmount.endsWith("G")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = 1000L * 1000 * 1000;
            } else if (memoryAmount.endsWith("T")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = 1000L * 1000 * 1000 * 1000;
            }

            return Long.parseLong(amount) * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getQuantityValue(Map<String, Quantity> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return "0";
        }
        return map.get(key).getAmount();
    }

    private String getCpuQuantityValue(Map<String, Quantity> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return "0";
        }
        return formatDecimal(parseCpuToCoreDecimal(buildQuantityValue(map.get(key))));
    }

    private String getMemoryQuantityValue(Map<String, Quantity> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return "0";
        }
        BigDecimal number = parseMemoryToGiDecimal(buildQuantityValue(map.get(key)));
        BigDecimal result = number.setScale(2, RoundingMode.HALF_UP);
        return formatDecimal(result);
    }

    private String buildQuantityValue(Quantity quantity) {
        String amount = quantity.getAmount() == null ? "" : quantity.getAmount();
        String format = quantity.getFormat() == null ? "" : quantity.getFormat();
        return amount + format;
    }

    private BigDecimal parseCpuToCoreDecimal(String cpuAmount) {
        if (cpuAmount == null || cpuAmount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            if (cpuAmount.endsWith("m")) {
                return new BigDecimal(cpuAmount.substring(0, cpuAmount.length() - 1))
                        .divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP);
            } else if (cpuAmount.endsWith("n")) {
                return new BigDecimal(cpuAmount.substring(0, cpuAmount.length() - 1))
                        .divide(new BigDecimal("1000000000"), 6, RoundingMode.HALF_UP);
            }
            return new BigDecimal(cpuAmount);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parseMemoryToGiDecimal(String memoryAmount) {
        if (memoryAmount == null || memoryAmount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal bytes = parseMemoryToBytesDecimal(memoryAmount);
            return bytes.divide(new BigDecimal(1024L * 1024 * 1024), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parseMemoryToBytesDecimal(String memoryAmount) {
        if (memoryAmount == null || memoryAmount.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            String amount = memoryAmount;
            BigDecimal multiplier = BigDecimal.ONE;

            if (memoryAmount.endsWith("Ki")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = new BigDecimal(1024);
            } else if (memoryAmount.endsWith("Mi")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = new BigDecimal(1024 * 1024);
            } else if (memoryAmount.endsWith("Gi")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = new BigDecimal(1024L * 1024 * 1024);
            } else if (memoryAmount.endsWith("Ti")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 2);
                multiplier = new BigDecimal(1024L * 1024 * 1024 * 1024);
            } else if (memoryAmount.endsWith("K")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = new BigDecimal(1000);
            } else if (memoryAmount.endsWith("M")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = new BigDecimal(1000 * 1000);
            } else if (memoryAmount.endsWith("G")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = new BigDecimal(1000L * 1000 * 1000);
            } else if (memoryAmount.endsWith("T")) {
                amount = memoryAmount.substring(0, memoryAmount.length() - 1);
                multiplier = new BigDecimal(1000L * 1000 * 1000 * 1000);
            }

            return new BigDecimal(amount).multiply(multiplier);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }
}
