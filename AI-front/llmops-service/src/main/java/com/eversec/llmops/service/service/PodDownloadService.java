package com.eversec.llmops.service.service;

import com.eversec.cloud.core.api.exception.ClientException;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class PodDownloadService {

    private final K8sFileService k8sFileService;

    /**
     * 在 Pod 内执行下载命令
     *
     * @param clusterName 集群名
     * @param namespace   命名空间
     * @param podName     Pod名称
     * @param url         预签名URL
     * @param targetPath  目标路径
     * @return 执行结果
     */
    public String downloadFromUrl(String clusterName, String namespace, String podName, String url, String targetPath) {
        if (StrUtil.isEmpty(url) || StrUtil.isEmpty(targetPath)) {
            throw new ClientException("url 和 targetPath 不能为空");
        }

        // 构造下载命令，为了兼容不同的基础镜像，依次尝试 wget 和 curl
        String cmd = String.format("wget -qO '%s' '%s' || curl -sS -o '%s' '%s'", targetPath, url, targetPath, url);
        log.info("在 Pod {}/{} 中执行下载命令: {}", namespace, podName, cmd);
        
        // 复用 K8sFileService 的 exec 方法执行命令，阻塞等待执行完成
        return k8sFileService.exec(clusterName, namespace, podName, cmd);
    }
}
