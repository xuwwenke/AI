package com.eversec.llmops.service.proxy.http;

import javax.servlet.http.HttpServletRequest;

public interface HttpFilter {

    void onPre(HttpServletRequest servletRequest, String originalBody);

    byte[] onPost(HttpServletRequest servletRequest, String originalBody, int statusCode, byte[] content);
}
