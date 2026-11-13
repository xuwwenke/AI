package com.eversec.llmops.service.proxy.http;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eversec.llmops.service.entity.Cluster;
import com.eversec.llmops.service.service.ClusterService;
import io.kubernetes.client.openapi.ApiClient;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class K8sClientApi {

    private static Map<String, ApiClient> API_CLIENT_MAP = new ConcurrentHashMap<>();

    @Resource
    private ClusterService clusterService;

    public ApiClient getApiClient(String clusterName) {
        if(API_CLIENT_MAP.containsKey(clusterName)) {
            return API_CLIENT_MAP.get(clusterName);
        }
        LambdaQueryWrapper<Cluster> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cluster::getName, clusterName);
        Cluster cluster = clusterService.getOne(queryWrapper);
        ApiClient apiClient = getApiClient(cluster.getK8sServerUrl(), cluster.getToken());
        API_CLIENT_MAP.put(clusterName, apiClient);
        return apiClient;
    }


    public ApiClient getApiClient(String k8sServerUrl, String k8sToken) {
        ApiClient apiClient = new ApiClient()
                .setBasePath(k8sServerUrl)
                .setVerifyingSsl(false);
        if (StringUtils.isNotBlank(k8sToken)) {
            apiClient.setApiKeyPrefix("Bearer");
            apiClient.setApiKey(k8sToken);
        }
        int timeout = 10;
        OkHttpClient httpClient = apiClient.getHttpClient().newBuilder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .callTimeout(timeout, TimeUnit.SECONDS)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();
        apiClient.setHttpClient(httpClient);
        return apiClient;
    }
}

