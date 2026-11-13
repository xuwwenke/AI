package com.eversec.llmops.service.proxy.http;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangfei
 */
@AllArgsConstructor
@Slf4j
public class HttpProxy {

    private final HttpClient httpClient;
    private final HttpFilter filter;
    private final boolean filterChunked;

    public void request(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
        String originalBody = null;
        RequestBody requestBody = null;
        if (servletRequest instanceof StandardMultipartHttpServletRequest) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            Map<String, String> kvMap = new HashMap<>();

            try {
                Collection<Part> parts = servletRequest.getParts();
                for (Part part : parts) {
                    if (part.getContentType() == null) {
                        String value = StreamUtils.copyToString(part.getInputStream(), StandardCharsets.UTF_8);
                        builder.addFormDataPart(part.getName(), value);
                        kvMap.put(part.getName(), value);
                    } else {
                        String fileName = part.getSubmittedFileName();
                        MediaType mediaType = MediaType.get(part.getContentType());
                        byte[] bytes = FileCopyUtils.copyToByteArray(part.getInputStream());
                        builder.addFormDataPart(part.getName(), fileName, RequestBody.create(mediaType, bytes));
                    }
                }
            } catch (ServletException e) {
                throw new RuntimeException("servletRequest.getParts failed", e);
            }
            originalBody = JSON.toJSONString(kvMap);
            if (this.filter != null) {
                this.filter.onPre(servletRequest, originalBody);
            }
            requestBody = builder.build();
        } else {
            originalBody = StreamUtils.copyToString(servletRequest.getInputStream(), StandardCharsets.UTF_8);

            if (this.filter != null) {
                this.filter.onPre(servletRequest, originalBody);
            }

            if (!StringUtils.isEmpty(originalBody)) {
                requestBody = RequestBody.create(MediaType.parse(servletRequest.getContentType()), originalBody);
            }
        }

        String url = servletRequest.getRequestURI() + "?" + servletRequest.getQueryString();
        String replacePrefix = httpClient.replacePrefix(servletRequest.getRequestURI());
        String prefix = getMatchPrefix(replacePrefix, url);
        if (prefix == null) {
            throw new RuntimeException("url not match prefix: " + replacePrefix);
        }

        url = this.httpClient.getBasePath() + url.substring(prefix.length());

        Map<String, String> headers = this.httpClient.headers(servletRequest);
        Headers.Builder builder = new Headers.Builder();
        headers.forEach(builder::add);

        Request request = new Request.Builder().url(url).method(
                servletRequest.getMethod(),
                requestBody
        ).headers(builder.build()).build();

        Call call = this.httpClient.getHttpClient().newCall(request);
        // request
        Response response = call.execute();

        String contentLengthHeader = null;
        // set response
        for (String name : response.headers().names()) {
            if (name.equalsIgnoreCase("Content-Length")) {
                contentLengthHeader = name;
            }
            // 不是所有的都加 Transfer-Encoding chunked
            if (name.equalsIgnoreCase("Transfer-Encoding")) {
                continue;
            }
            servletResponse.addHeader(name, response.headers().get(name));
        }

        try {
            servletResponse.setStatus(response.code());

            if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
                if (response.body() == null) {
                    return;
                }
                if (this.filterChunked && this.filter != null) {
//                    ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(8192);
//                    //
//                    Buffer buffer = new Buffer();
//                    while (true) {
//                        try {
//                            long bytes = response.body().source().read(buffer, 8192);
//                            if (bytes == -1) {
//                                break;
//                            }
//
//                            if (bytes == 0) {
//                                Thread.sleep(10);
//                                continue;
//                            }
//
//                            byteArrayBuffer.append(buffer.readByteArray(), 0, (int)bytes);
//                        } catch (Exception e) {
//                            log.warn("请求异常", e);
//                            break;
//                        }
//                    }

                    // 转换为对象返回
                    byte[] bytes = response.body().source().readByteArray();
                    bytes = this.filter.onPost(servletRequest, originalBody, response.code(), bytes);
                    servletResponse.getOutputStream().write(bytes);
                    servletResponse.getOutputStream().flush();
                } else {
                    // TODO: 2022-10-28
                    servletResponse.getOutputStream().write(response.body().source().readByteArray());
                    servletResponse.getOutputStream().flush();

                    // 分块返回
//                    servletResponse.addHeader("Transfer-Encoding", "chunked");
//                    Buffer buffer = new Buffer();
//                    while (true) {
//                        try {
//                            long bytes = response.body().source().read(buffer, 8192);
//                            if (bytes == -1) {
//                                break;
//                            }
//
//                            if (bytes == 0) {
//                                Thread.sleep(10);
//                                continue;
//                            }
//
//                            servletResponse.getOutputStream().write(buffer.readByteArray());
//                            servletResponse.getOutputStream().flush();
//                        } catch (Exception e) {
//                            log.warn("请求异常", e);
//                            break;
//                        }
//                    }
                }

            } else {
                byte[] bytes = new byte[0];
                if (response.body() != null) {
                    bytes = response.body().bytes();
                }
                if (this.filter != null) {
                    bytes = this.filter.onPost(servletRequest, originalBody, response.code(), bytes);
                }
                if (contentLengthHeader != null) {
                    servletResponse.setHeader(contentLengthHeader, bytes.length + "");
                }
                servletResponse.getOutputStream().write(bytes);
                servletResponse.getOutputStream().flush();
            }
        } finally {
            servletResponse.getOutputStream().close();
        }
    }

    private String getMatchPrefix(String replacePrefix, String url) {
        if (replacePrefix == null) {
            return null;
        }

        if (url.startsWith(replacePrefix)) {
            return replacePrefix;
        }

        return null;
    }
}
