package com.eversec.llmops.service.proxy.http;

/**
 * 可修改返回结果
 * @author wangfei
 */
public interface PostHandler<T, R> {
    /**
     * 在接口调用后，进行业务处理封装
     * @param req 请求参数
     * @param resp 应答参数
     * @return 更新后应答参数
     */
    R handle(String clusterName, T req, R resp);
}
