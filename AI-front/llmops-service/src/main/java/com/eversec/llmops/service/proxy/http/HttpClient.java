package com.eversec.llmops.service.proxy.http;

import com.alibaba.fastjson.JSON;
import okhttp3.OkHttpClient;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public interface HttpClient {

    String getBasePath();

    void auth(Map<String, String> headers);

    default Map<String, String> headers(HttpServletRequest servletRequest) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> enumeration = servletRequest.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            // 移除gzip，让 okhttp3 自动解码gzip
            if (name.equalsIgnoreCase("Accept-Encoding")) {
                continue;
            }
            headers.put(name, servletRequest.getHeader(name));
        }
        this.auth(headers);
        return headers;
    }

    OkHttpClient getHttpClient();

    String replacePrefix(String url);

    default <T> T parseObject(String s, Class<T> clz) {
        return JSON.parseObject(s, clz);
    }

    default <T> String toJSONString(T data) {
        return JSON.toJSONString(data);
    }
}
