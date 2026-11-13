package com.eversec.llmops.service.util.k8s;

import com.eversec.cloud.fileuploader.FileOperator;
import com.eversec.llmops.service.service.K8sFileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author wangfei
 */
@Slf4j
@AllArgsConstructor
public class KubeFileOperator implements FileOperator {
    private final String clusterName;
    private final String namespace;
    private final String name;
    private final K8sFileService k8sFileService;

    public KubeFileOperator(K8sFileService k8sFileService, String clusterName, String namespace, String name) {
        this.k8sFileService = k8sFileService;
        this.clusterName = clusterName;
        this.namespace = namespace;
        this.name = name;
    }

    @Override
    public byte[] readTempFile(String dir, String fileName) throws IOException {
        String path = getTempFilePath(dir, fileName);
        log.info("读取临时文件: {}", path);
        if (!exists(path)) {
            return null;
        }
        String ret = k8sFileService.getPodFileContent(clusterName, namespace, name, path);
        return ret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void writeTempFile(String dir, String fileName, byte[] data) throws IOException {
        String path = getTempFilePath(dir, fileName);
        mkdirs(getTempFilePath(dir, ""));
        log.info("写入临时文件: {}", path);
        k8sFileService.upload(clusterName, namespace, name, path, data);
    }

    @Override
    public String getTempFilePath(String dir, String fileName) {
        return "/var/" + dir + "/" + fileName;
    }

    @Override
    public void mergeFiles(List<String> sourceFiles, String targetFilePath) throws IOException {
        mkdirs(new File(targetFilePath).getParentFile().getAbsolutePath());
        StringBuilder builder = new StringBuilder("cat ");
        sourceFiles.forEach(sourceFile -> {
            builder.append("'").append(sourceFile).append("' ");
        });
        builder.append(" > '").append(targetFilePath).append("'");
        k8sFileService.exec(clusterName, namespace, name, builder.toString());
    }

    @Override
    public String getFileMd5(String filePath) throws IOException {
        String cmd = MessageFormat.format("md5sum ''{0}''", filePath);
        String ret = k8sFileService.exec(clusterName, namespace, name, cmd);
        return ret.split(" ")[0];
    }

    @Override
    public void move(String sourcePath, String targetPath) throws IOException {
        String cmd = MessageFormat.format("mv ''{0}'' ''{1}''", sourcePath, targetPath);
        k8sFileService.exec(clusterName, namespace, name, cmd);
    }

    @Override
    public void removeTempDir(String dir) {
        String cmd = MessageFormat.format("rm -rf ''{0}''", getTempFilePath(dir, ""));
        k8sFileService.exec(clusterName, namespace, name, cmd);
    }

    private void mkdirs(String dir) {
        k8sFileService.exec(clusterName, namespace, name, "mkdir -p '" + dir + "'");
    }

    private boolean exists(String filePath) {
        List<PodFile> podFiles = k8sFileService.getPodFiles(clusterName, namespace, name, filePath, false, true);
        if (CollectionUtils.isEmpty(podFiles)) {
            return false;
        }
        return true;
    }
}
