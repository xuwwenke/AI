package com.eversec.llmops.service.service;

import com.alibaba.fastjson.JSON;
import com.eversec.cloud.core.api.exception.ClientException;
import com.eversec.cloud.fileuploader.FileChunkData;
import com.eversec.cloud.fileuploader.param.FileChunkBeginParam;
import com.eversec.cloud.fileuploader.param.FileChunkEndParam;
import com.eversec.cloud.fileuploader.param.FileChunkUploadParam;
import com.eversec.llmops.service.client.K8sClientFactory;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class OpenShiftFileChunkService {

    private static final String CHUNK_DATA_FILE = "chunk_data.json";
    private static final String CHUNK_PART_FILE_PREFIX = "chunk_data.part.";
    private static final String TEMP_ROOT = "/tmp/";

    private final K8sClientFactory k8sClientFactory;
    private final Map<String, ReentrantLock> uploadLocks = new ConcurrentHashMap<>();

    public OpenShiftFileChunkService(K8sFileService k8sFileService, K8sClientFactory k8sClientFactory) {
        // 保留原构造函数签名以兼容 Spring 注入，弃用内部的 k8sFileService 依赖
        this.k8sClientFactory = k8sClientFactory;
    }

    public FileChunkData chunkBegin(String clusterName, String namespace, String podName, FileChunkBeginParam param) throws Exception {
        String lockKey = buildLockKey(clusterName, namespace, podName, param.getFileName(), param.getFileMd5());
        ReentrantLock lock = acquireLock(lockKey);
        try {
            OpenShiftClient client = k8sClientFactory.getClient(clusterName);
            FileChunkData chunkData = readChunkData(client, namespace, podName, param.getFileName(), param.getFileMd5());
            if (chunkData == null) {
                chunkData = new FileChunkData(param.getFileName(), param.getFileMd5(), param.getFileSize(), param.getChunkSize(),
                        param.getTargetFilePath(), (param.getFileSize() - 1) / param.getChunkSize() + 1, 0);
            } else {
                chunkData.setTargetFilePath(param.getTargetFilePath());
            }
            saveChunkData(client, namespace, podName, chunkData);
            return chunkData;
        } finally {
            releaseLock(lockKey, lock);
        }
    }

    public void chunkUpload(String clusterName, String namespace, String podName, FileChunkUploadParam param) throws Exception {
        String lockKey = buildLockKey(clusterName, namespace, podName, param.getFileName(), param.getFileMd5());
        ReentrantLock lock = acquireLock(lockKey);
        try {
            OpenShiftClient client = k8sClientFactory.getClient(clusterName);
            saveChunkPart(client, namespace, podName, param.getFileName(), param.getFileMd5(), param.getChunkIndex(), param.getChunkData());
            //chunkData.setUploadedChunk(param.getChunkIndex() + 1);
            //saveChunkData(client, namespace, podName, chunkData);
        } finally {
            releaseLock(lockKey, lock);
        }
    }

    public FileChunkData chunkEnd(String clusterName, String namespace, String podName, FileChunkEndParam param) throws Exception {
        String lockKey = buildLockKey(clusterName, namespace, podName, param.getFileName(), param.getFileMd5());
        ReentrantLock lock = acquireLock(lockKey);
        try {
            OpenShiftClient client = k8sClientFactory.getClient(clusterName);
            FileChunkData chunkData = readChunkData(client, namespace, podName, param.getFileName(), param.getFileMd5());
            if (chunkData == null) {
                throw new ClientException("文件上传失败, 找不到 CHUNK_DATA_FILE");
            }
            if (!chunkData.isFinished()) {
                throw new ClientException("文件未上传完成：" + JSON.toJSONString(chunkData));
            }

            String tempTargetFilePath = chunkData.getTargetFilePath() + ".tmp";
            
            // 拼接合并命令
            List<String> partFiles = buildChunkPartPaths(chunkData);
            StringBuilder catCmd = new StringBuilder("cat ");
            for (String part : partFiles) {
                catCmd.append("'").append(escapeSingleQuote(part)).append("' ");
            }
            catCmd.append("> '").append(escapeSingleQuote(tempTargetFilePath)).append("'");

            // 执行合并（不读取输出，避免 socket close）
            execCmd(client, namespace, podName, catCmd.toString(), false);

            // 校验 MD5
            String md5Result = execCmd(client, namespace, podName, "md5sum '" + escapeSingleQuote(tempTargetFilePath) + "'", true);
            String md5 = md5Result != null ? md5Result.split(" ")[0].trim() : "";
            if (!md5.equalsIgnoreCase(param.getFileMd5())) {
                throw new ClientException("上传的文件已损坏，请重新上传");
            }

            // 移动并清理
            int lastSlashIndex = chunkData.getTargetFilePath().lastIndexOf('/');
            String targetDir = lastSlashIndex >= 0 ? chunkData.getTargetFilePath().substring(0, lastSlashIndex) : "/";
            execCmd(client, namespace, podName, "mkdir -p '" + escapeSingleQuote(targetDir) + "' && mv -f '" + escapeSingleQuote(tempTargetFilePath) + "' '" + escapeSingleQuote(chunkData.getTargetFilePath()) + "'", false);

            String dir = buildTempFilePath(buildUploadDir(param.getFileName(), param.getFileMd5()), "");
            execCmd(client, namespace, podName, "rm -rf '" + escapeSingleQuote(dir) + "'", false);

            return chunkData;
        } finally {
            releaseLock(lockKey, lock);
        }
    }

    private FileChunkData readChunkData(OpenShiftClient client, String namespace, String podName, String fileName, String fileMd5) {
        String chunkDataPath = buildTempFilePath(buildUploadDir(fileName, fileMd5), CHUNK_DATA_FILE);
        try (InputStream is = client.pods().inNamespace(namespace).withName(podName).file(chunkDataPath).read()) {
            if (is == null) {
                return null;
            }
            String content = readStream(is);
            if (content == null || content.trim().isEmpty()) {
                return null;
            }
            return JSON.parseObject(content, FileChunkData.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveChunkData(OpenShiftClient client, String namespace, String podName, FileChunkData chunkData) throws Exception {
        String path = buildTempFilePath(buildUploadDir(chunkData.getFileName(), chunkData.getFileMd5()), CHUNK_DATA_FILE);
        byte[] bytes = JSON.toJSONString(chunkData).getBytes(StandardCharsets.UTF_8);
        uploadLocalFile(client, namespace, podName, path, bytes);
    }

    private void saveChunkPart(OpenShiftClient client, String namespace, String podName, String fileName, String fileMd5, int chunkIndex, byte[] chunkData) throws Exception {
        String path = buildTempFilePath(buildUploadDir(fileName, fileMd5), CHUNK_PART_FILE_PREFIX + chunkIndex);
        uploadLocalFile(client, namespace, podName, path, chunkData);
    }

    /**
     * 通过生成本地临时文件并调用 upload(Path) 上传，避免直接使用 InputStream/stdin 导致的 socket close 和截断空文件问题。
     */
    private void uploadLocalFile(OpenShiftClient client, String namespace, String podName, String remotePath, byte[] data) throws Exception {
        log.info("remotePath:"+remotePath);
        log.info("dataLength:"+data.length);
        try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            client.pods().inNamespace(namespace).withName(podName)
                    .file(remotePath)
                    .upload(is);
        }
    }

    /**
     * 执行命令。对于不需要读取输出的耗时命令（如 cat 合并），readOutput 传 false，从而避免长时间等待 stdout 导致的 socket close 异常。
     */
    private String execCmd(OpenShiftClient client, String namespace, String podName, String cmd, boolean readOutput) throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> failureRef = new AtomicReference<>();

        ExecListener listener = new ExecListener() {
            @Override
            public void onFailure(Throwable t, Response failureResponse) {
                failureRef.set(t);
                latch.countDown();
            }

            @Override
            public void onClose(int code, String reason) {
                latch.countDown();
            }
        };

        ExecWatch watch;
        if (readOutput) {
            watch = client.pods().inNamespace(namespace).withName(podName)
                    .writingOutput(stdout)
                    .usingListener(listener)
                    .exec("sh", "-c", cmd);
        } else {
            // 耗时命令（如合并、删除）不绑定 writingOutput，避免长连接监听导致 socket close
            watch = client.pods().inNamespace(namespace).withName(podName)
                    .usingListener(listener)
                    .exec("sh", "-c", cmd);
        }

        try {
            // 增加超时防止死锁
            latch.await(5, TimeUnit.MINUTES);
        } finally {
            watch.close();
        }

        if (failureRef.get() != null) {
            throw new Exception("执行命令失败: " + cmd, failureRef.get());
        }

        if (readOutput) {
            return stdout.toString(StandardCharsets.UTF_8.name());
        }
        return null;
    }

    private String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private List<String> buildChunkPartPaths(FileChunkData chunkData) {
        List<String> sourceFiles = new ArrayList<>();
        String dir = buildUploadDir(chunkData.getFileName(), chunkData.getFileMd5());
        for (int i = 0; i < chunkData.getChunkCount(); i++) {
            sourceFiles.add(buildTempFilePath(dir, CHUNK_PART_FILE_PREFIX + i));
        }
        return sourceFiles;
    }

    private String buildUploadDir(String fileName, String fileMd5) {
        return fileName + "_" + fileMd5;
    }

    private String buildTempFilePath(String dir, String fileName) {
        return TEMP_ROOT + dir + "/" + fileName;
    }

    private String buildLockKey(String clusterName, String namespace, String podName, String fileName, String fileMd5) {
        return clusterName + "|" + namespace + "|" + podName + "|" + fileName + "|" + fileMd5;
    }

    private ReentrantLock acquireLock(String lockKey) {
        ReentrantLock lock = uploadLocks.computeIfAbsent(lockKey, key -> new ReentrantLock());
        lock.lock();
        return lock;
    }

    private void releaseLock(String lockKey, ReentrantLock lock) {
        try {
            lock.unlock();
        } finally {
            if (!lock.hasQueuedThreads()) {
                uploadLocks.remove(lockKey, lock);
            }
        }
    }

    private String escapeSingleQuote(String value) {
        return value.replace("'", "'\\''");
    }
}
