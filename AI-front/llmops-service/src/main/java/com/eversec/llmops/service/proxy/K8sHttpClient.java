package com.eversec.llmops.service.proxy;




import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.auth.Authentication;
import okhttp3.OkHttpClient;
import com.eversec.llmops.service.proxy.http.HttpClient;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangfei
 */
public class K8sHttpClient implements HttpClient {
    private ApiClient apiClient;

    public K8sHttpClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getBasePath() {
        return apiClient.getBasePath();
    }

    @Override
    public void auth(Map<String, String> headers) {
        Authentication authentication = this.apiClient.getAuthentication("BearerToken");
        if (authentication != null) {
            authentication.applyToParams(null, headers, null);
        }
    }

    @Override
    public Map<String, String> headers(HttpServletRequest servletRequest) {
        Map<String, String> headers = new HashMap<>();
        this.auth(headers);
        return headers;
    }

    @Override
    public OkHttpClient getHttpClient() {
        return apiClient.getHttpClient();
    }

    @Override
    public String replacePrefix(String url) {
        return "/k8s-api";
    }

    @Override
    public <T> T parseObject(String s, Class<T> clz) {
        return this.apiClient.getJSON().deserialize(s, clz);
    }

    @Override
    public <T> String toJSONString(T data) {
        return this.apiClient.getJSON().serialize(data);
    }

    public ApiClient getApiClient() {
        return this.apiClient;
    }
}
