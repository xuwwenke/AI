package com.eversec.llmops.service.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpProxyService {

    @Value("${infra.host}")
    private String infraHost;

    /**
     * 发送POST请求
     * @param path 请求路径
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 请求体类型
     * @param <R> 响应类型
     * @return 响应对象
     */
    public <T, R> R post(String path, T requestBody, Class<R> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        String url = infraHost + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                responseType
        );
        return response.getBody();
    }

    /**
     * 发送POST请求（带自定义header）
     * @param path 请求路径
     * @param requestBody 请求体
     * @param headers 自定义header
     * @param responseType 响应类型
     * @param <T> 请求体类型
     * @param <R> 响应类型
     * @return 响应对象
     */
    public <T, R> R post(String path, T requestBody, HttpHeaders headers, Class<R> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        String url = infraHost + path;
        if (headers == null) {
            headers = new HttpHeaders();
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                responseType
        );
        return response.getBody();
    }

    /**
     * 发送PUT请求
     * @param path 请求路径
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 请求体类型
     * @param <R> 响应类型
     * @return 响应对象
     */
    public <T, R> R put(String path, T requestBody, Class<R> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        String url = infraHost + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                responseType
        );
        return response.getBody();
    }


    public <T, R> R put(String path, T requestBody, HttpHeaders headers, Class<R> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        String url = infraHost + path;
        if(headers== null) headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                responseType
        );
        return response.getBody();
    }

    /**
     * 发送DELETE请求
     * @param path 请求路径
     * @param responseType 响应类型
     * @param <R> 响应类型
     * @return 响应对象
     */
    public <R> R delete(String path, Class<R> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        String url = infraHost + path;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                requestEntity,
                responseType
        );
        return response.getBody();
    }

    /**
     * 发送GET请求
     * @param path 请求路径
     * @param responseType 响应类型
     * @param <R> 响应类型
     * @return 响应对象
     */
    public <R> R get(String path, Class<R> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        String url = infraHost + path;
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "curl/7.68.0");  // 模拟 curl
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                responseType
        );
        return response.getBody();
    }

    /**
     * 发送GET请求（带自定义header）
     * @param path 请求路径
     * @param headers 自定义header
     * @param responseType 响应类型
     * @param <R> 响应类型
     * @return 响应对象
     */
    public <R> R get(String path, HttpHeaders headers, Class<R> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        String url = infraHost + path;
        if (headers == null) {
            headers = new HttpHeaders();
        }
        if (!headers.containsKey("User-Agent")) {
            headers.set("User-Agent", "curl/7.68.0");
        }
        if (!headers.containsKey("Accept")) {
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        }
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                responseType
        );
        return response.getBody();
    }

    /**
     * 发送GET请求（带自定义header），返回ResponseEntity
     * @param path 请求路径
     * @param headers 自定义header
     * @param responseType 响应类型
     * @param <R> 响应类型
     * @return ResponseEntity
     */
    public <R> ResponseEntity<R> getWithResponseEntity(String path, HttpHeaders headers, Class<R> responseType) {
        RestTemplate restTemplate = createRestTemplate();
        String url = infraHost + path;
        if (headers == null) {
            headers = new HttpHeaders();
        }
        if (!headers.containsKey("User-Agent")) {
            headers.set("User-Agent", "curl/7.68.0");
        }
        if (!headers.containsKey("Accept")) {
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        }
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
    }

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(HttpStatus statusCode) {
                return false;
            }
        });
        return restTemplate;
    }
}