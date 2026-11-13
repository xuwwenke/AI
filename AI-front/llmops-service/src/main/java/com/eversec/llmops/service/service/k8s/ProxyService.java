package com.eversec.llmops.service.service.k8s;

import com.eversec.llmops.service.proxy.http.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class ProxyService {

    @Resource
    private HttpClientBuilder httpClientBuilder;

    public <T, R> void request(String clusterName, HttpServletRequest servletRequest, HttpServletResponse servletResponse,
                               PostHandler<T, R> handler, Class<T> reqClz, Class<R> respClz, boolean filterChunked) throws IOException {
        HttpClient client = httpClientBuilder.createHttpClient(clusterName);
        HttpProxy httpProxy = new HttpProxy(client, new PostHttpFilter<>(clusterName, client, handler, reqClz, respClz), filterChunked);
        httpProxy.request(servletRequest, servletResponse);
    }



    public void request(String clusterName, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
        HttpClient client = httpClientBuilder.createHttpClient(clusterName);
        HttpProxy httpProxy = new HttpProxy(client, null, false);
        httpProxy.request(servletRequest, servletResponse);
    }

}
