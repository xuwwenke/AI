package com.eversec.llmops.service.proxy.http;


import com.eversec.llmops.service.proxy.K8sHttpClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;


/**
 * @author wangfei
 */
@AllArgsConstructor
@Service
public class HttpClientBuilder {


    @Resource
    private K8sClientApi k8sClientApi;

    public final static String CLUSTER_NAME_HTTP_HEADER = "clusterName";

    public HttpClient createHttpClient(String clusterName) {
        HttpClient httpClient = new K8sHttpClient(k8sClientApi.getApiClient(clusterName));
        return httpClient;
    }

}
