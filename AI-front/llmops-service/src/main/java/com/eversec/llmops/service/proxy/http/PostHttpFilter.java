package com.eversec.llmops.service.proxy.http;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * @author wangfei
 */
@AllArgsConstructor
public class PostHttpFilter<T, R> implements HttpFilter {

    private static final int ERROR_CODE = 400;

    private final String clusterName;
    private final HttpClient client;
    private final PostHandler<T, R> handler;
    private final Class<T> reqClz;
    private final Class<R> respClz;

    @Override
    public void onPre(HttpServletRequest servletRequest, String originalBody) {
    }

    @Override
    public byte[] onPost(HttpServletRequest servletRequest, String originalBody, int statusCode, byte[] content) {
        if (statusCode >= ERROR_CODE || this.handler == null) {
            return content;
        }
        T req = null;

        if (!StringUtils.isEmpty(originalBody)) {
            req = this.client.parseObject(originalBody, reqClz);
        }
        R resp = this.client.parseObject(new String(content, StandardCharsets.UTF_8), respClz);
        resp = handler.handle(clusterName, req, resp);
        return this.client.toJSONString(resp).getBytes(StandardCharsets.UTF_8);
    }
}
