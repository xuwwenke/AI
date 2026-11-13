package com.eversec.llmops.service.service;

public class PrometheusQueryBuilder {

    private PrometheusQueryBuilder() {
    }

    public static String buildPodMemoryUsageQuery(String podName) {
        return String.format("container_memory_working_set_bytes{job=\"kubelet\",container=\"\",pod=\"%s\"}", podName);
    }

    public static String buildPodMemoryUsageRegexQuery(String podRegex) {
        return String.format("container_memory_working_set_bytes{job=\"kubelet\",container=\"\",image=\"\",pod=~\"%s\"}", podRegex);
    }

    public static String buildPodCpuUsageQuery(String podName) {
        return String.format("sum(rate(container_cpu_usage_seconds_total{job=\"kubelet\",container!=\"POD\",pod=\"%s\"}[5m]))", podName);
    }

    public static String buildPodCpuUsageRegexQuery(String podRegex) {
        return String.format("rate(container_cpu_usage_seconds_total{job=\"kubelet\",container=\"\",image=\"\",pod=~\"%s\"}[2m])", podRegex);
    }

    public static String buildPodGpuUtilizationQuery(String podRegex) {
        return String.format("DCGM_FI_DEV_GPU_UTIL{pod=~\"%s\"}", podRegex);
    }

    public static String buildPodGpuMemoryUsageUtilQuery(String podRegex) {
        return String.format("DCGM_FI_DEV_FB_USED{pod=~\"%s\"}/(DCGM_FI_DEV_FB_USED{pod=~\"%s\"} + DCGM_FI_DEV_FB_FREE{pod=~\"%s\"})", podRegex, podRegex, podRegex);
    }

    public static String buildPodNpuUtilizationQuery(String podRegex) {
        return String.format("npu_chip_info_utilization{pod_name=~\"%s\"}", podRegex);
    }

    public static String buildPodNpuUsedMemoryQuery(String podRegex) {
        return String.format("npu_chip_info_used_memory{pod_name=~\"%s\"}", podRegex);
    }

    public static String buildPodNetworkReceiveRegexQuery(String podRegex) {
        return String.format("sum by (pod) (rate(container_network_receive_bytes_total{job=\"kubelet\",pod=~\"%s\"}[2m]))", podRegex);
    }

    public static String buildPodNetworkTransmitRegexQuery(String podRegex) {
        return String.format("sum by (pod) (rate(container_network_transmit_bytes_total{job=\"kubelet\",pod=~\"%s\"}[2m]))", podRegex);
    }

    public static String buildPodNetworkReceiveQuery(String podName) {
        return String.format("sum(rate(container_network_receive_bytes_total{job=\"kubelet\",pod=\"%s\"}[5m]))", podName);
    }

    public static String buildPodNetworkTransmitQuery(String podName) {
        return String.format("sum(rate(container_network_transmit_bytes_total{job=\"kubelet\",pod=\"%s\"}[5m]))", podName);
    }

    public static String buildPodDiskReadQuery(String podName) {
        return String.format("sum(rate(container_fs_reads_bytes_total{job=\"kubelet\",pod=\"%s\"}[5m]))", podName);
    }

    public static String buildPodDiskWriteQuery(String podName) {
        return String.format("sum(rate(container_fs_writes_bytes_total{job=\"kubelet\",pod=\"%s\"}[5m]))", podName);
    }

    public static String buildNamespaceMemoryUsageQuery(String namespace) {
        return String.format("sum(container_memory_working_set_bytes{job=\"kubelet\",namespace=\"%s\"})", namespace);
    }

    public static String buildNamespaceCpuUsageQuery(String namespace) {
        return String.format("sum(rate(container_cpu_usage_seconds_total{job=\"kubelet\",namespace=\"%s\",container!=\"POD\"}[5m]))", namespace);
    }

    public static String buildNodeMemoryUsageQuery(String nodeName) {
        return String.format("sum(container_memory_working_set_bytes{job=\"kubelet\",node=\"%s\"})", nodeName);
    }

    public static String buildNodeCpuUsageQuery(String nodeName) {
        return String.format("sum(rate(container_cpu_usage_seconds_total{job=\"kubelet\",node=\"%s\",container!=\"POD\"}[5m]))", nodeName);
    }

    public static String buildDeploymentMemoryUsageQuery(String deploymentName, String namespace) {
        return String.format("sum(container_memory_working_set_bytes{job=\"kubelet\",namespace=\"%s\",pod=~\"%s-.*\"})", namespace, deploymentName);
    }

    public static String buildDeploymentCpuUsageQuery(String deploymentName, String namespace) {
        return String.format("sum(rate(container_cpu_usage_seconds_total{job=\"kubelet\",namespace=\"%s\",pod=~\"%s-.*\",container!=\"POD\"}[5m]))", namespace, deploymentName);
    }

    public static String buildContainerMemoryUsageQuery(String containerName, String podName) {
        return String.format("container_memory_working_set_bytes{job=\"kubelet\",pod=\"%s\",container=\"%s\"}", podName, containerName);
    }

    public static String buildContainerCpuUsageQuery(String containerName, String podName) {
        return String.format("rate(container_cpu_usage_seconds_total{job=\"kubelet\",pod=\"%s\",container=\"%s\"}[5m])", podName, containerName);
    }
}
