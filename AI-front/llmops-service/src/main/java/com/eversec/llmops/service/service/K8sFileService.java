package com.eversec.llmops.service.service;

import cn.hutool.core.util.StrUtil;
import com.eversec.cloud.core.api.exception.ClientException;
import com.eversec.cloud.core.api.exception.UnknownException;
import com.eversec.cloud.fileuploader.FileChunkData;
import com.eversec.cloud.fileuploader.FileChunkUploader;
import com.eversec.cloud.fileuploader.param.FileChunkBeginParam;
import com.eversec.cloud.fileuploader.param.FileChunkEndParam;
import com.eversec.cloud.fileuploader.param.FileChunkUploadParam;
import com.eversec.llmops.service.client.K8sClientFactory;
import com.eversec.llmops.service.util.k8s.Copy;
import com.eversec.llmops.service.util.k8s.ExceptionUtil;
import com.eversec.llmops.service.util.k8s.KubeFileOperator;
import com.eversec.llmops.service.util.k8s.PodFile;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.openshift.client.OpenShiftClient;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.Streams;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.eversec.llmops.service.proxy.http.K8sClientApi;
/**
 * @author wangfei
 */
@Service
@Slf4j
@AllArgsConstructor
public class K8sFileService {


	@Resource
	private K8sClientApi k8sClientApi;


	@Resource
	private K8sClientFactory k8sClientFactory;

	public void download(String clusterName, String namespace, String podName, String podFilePath, OutputStream outputStream) {
		try {
			ApiClient apiClient = k8sClientApi.getApiClient(clusterName);
			Copy copy = new Copy(apiClient);
			InputStream dataStream = copy.copyFileFromPod(namespace, podName, podFilePath);
			Copy.copyStream(dataStream, outputStream, false);
		} catch (ApiException | IOException e) {
			if (ExceptionUtil.notFound(e)) {
				throw new ClientException("容器组不存在");
			}
			log.error("下载文件失败", e);
			throw new UnknownException("下载文件失败", e);
		}
	}

	public FileChunkData chunkBegin(String clusterName, String namespace, String name, FileChunkBeginParam param) throws IOException {
		FileChunkUploader fileChunkUploader = new FileChunkUploader(new KubeFileOperator(this, clusterName, namespace, name));
		return fileChunkUploader.begin(param);
	}

	public void chunkUpload(String clusterName, String namespace, String name, FileChunkUploadParam param) throws Exception {
		FileChunkUploader fileChunkUploader = new FileChunkUploader(new KubeFileOperator(this, clusterName, namespace, name));
		fileChunkUploader.upload(param);
	}

	public void chunkEnd(String clusterName, String namespace, String name, FileChunkEndParam param) throws Exception {
		FileChunkUploader fileChunkUploader = new FileChunkUploader(new KubeFileOperator(this, clusterName, namespace, name));
		fileChunkUploader.end(param);
	}

	public void upload(String clusterName, String namespace, String podName, String podFilePath, InputStream inputStream) {
		ApiClient apiClient = k8sClientApi.getApiClient(clusterName);
		Copy copy = new Copy(apiClient);
		copy.copyFileToPod(namespace, podName, inputStream, Paths.get(podFilePath));
	}

	public void upload(String clusterName, String namespace, String podName, String podFilePath, byte[] bytes) {
		ApiClient apiClient = k8sClientApi.getApiClient(clusterName);
		Copy copy = new Copy(apiClient);
		copy.copyFileToPod(namespace, podName, bytes, Paths.get(podFilePath));
	}

	public void upload(String clusterName, String namespace, String podName, String podFilePath, String filePath) throws FileNotFoundException {
		ApiClient apiClient = k8sClientApi.getApiClient(clusterName);
		Copy copy = new Copy(apiClient);
		copy.copyFileToPod(namespace, podName, Paths.get(filePath), Paths.get(podFilePath));
	}

	public String exec(String clusterName, String namespace, String podName, String cmd) {
		try {
			ApiClient apiClient = k8sClientApi.getApiClient(clusterName);
			Exec exec = new Exec(apiClient);
			String[] command = new String[] { "sh", "-c", cmd };
			final Process proc = exec.exec(namespace, podName, command, false, false);
			ForkJoinTask<String> result = ForkJoinPool.commonPool().submit(() -> {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				try {
					Streams.copy(proc.getInputStream(), byteArrayOutputStream);
				} catch (IOException e) {
					log.error("读取exec结果失败 {}", cmd, e);
				}
				return byteArrayOutputStream.toString();
			});
			String ret = result.get();
			proc.waitFor();
			proc.destroy();
			int exitValue = proc.exitValue();
			if (exitValue != 0) {
				throw new UnknownException(cmd + ", exit: " + exitValue + ", " + ret);
			}
			return ret;
		} catch (IOException | ApiException | InterruptedException | ExecutionException ex) {
			if (ExceptionUtil.notFound(ex)) {
				throw new ClientException("容器组不存在");
			}
			log.error("执行exec结果失败 {}", cmd, ex);
			throw new UnknownException(cmd + " failed");
		}
	}

	private static Pattern pattern = Pattern.compile("^([\\w-\\.]+)\\s+(\\d+)\\s+(\\w+)\\s+(\\w+)\\s+(\\d+) (.+) \"(.+)\"$");
	private static Pattern pattern_link = Pattern.compile("^([\\w-\\.]+)\\s+(\\d+)\\s+(\\w+)\\s+(\\w+)\\s+(\\d+) (.+) \"(.+)\" -> \"(.+)\"$");

	public List<PodFile> getPodFiles(String clusterName, String namespace, String name, String dirPath, Boolean showHidden, Boolean dir) {
		if (StringUtils.isEmpty(dirPath)) {
			dirPath = "/";
		}
		if (dir != null && dir) {
			showHidden = true;
		}
		String cmd = StrUtil.format("ls -l{}{}Q --color=never --full-time '{}'", (showHidden != null && showHidden) ? "a" : "", (dir != null && dir) ? "d" : "",
				dirPath);
		String ret = "";
		try {
			ret = this.exec(clusterName, namespace, name, cmd);
		} catch (Exception e) {
			// ignore
		}
		String[] lines = ret.split("\n");
		List<PodFile> podFiles = new ArrayList<>();
		for (String line : lines) {
			PodFile podFile = getPodFile(line);
			if (podFile != null) {
				podFiles.add(podFile);
			}
		}
		return podFiles;
	}

	public PodFile getPodFile(String line) {
		Matcher matcher = line.contains("->") ? pattern_link.matcher(line.trim()) : pattern.matcher(line.trim());
		if (matcher.matches()) {
			if (matcher.groupCount() == 7 || matcher.groupCount() == 8) {
				long fileSize = 0;
				String link = null;
				try {
					fileSize = Long.parseLong(matcher.group(5));
				} catch (NumberFormatException e) {

					// ignore
				}

				if (matcher.groupCount() == 8) {
					link = matcher.group(8);
				}

				return new PodFile(matcher.group(1), fileSize, matcher.group(7), matcher.group(3), matcher.group(4), matcher.group(6), link);
			}
		}
		return null;
	}

	public String getPodFileContent(String clusterName, String namespace, String name, String filePath) {
		String ret = this.exec(clusterName, namespace, name, "dd if='" + filePath + "' status=noxfer");
		if (isBinary(ret)) {
			ret = "这可能是一个二进制文件，暂时不支持查看";
		}
		return ret;
	}

	public static boolean isBinary(String content) {
		long len = content.length();
		for (int j = 0; j < (int) len; j++) {
			char ch = content.charAt(j);
			if (ch < 32 && ch != 9 && ch != 10 && ch != 13) {
				return true;
			}
		}
		return false;
	}

	public int syncFile(String cluster,String namespace,String podName,String targetPath,String presignedUrl) {
		String command = String.format("curl -o %s %s", targetPath, presignedUrl);
		OpenShiftClient client = k8sClientFactory.getClient(cluster);
		try (ExecWatch execWatch = client.pods()
				.inNamespace(namespace)
				.withName(podName)
				.writingOutput(System.out)  // 可获取stdout
				.writingError(System.err)   // 可获取stderr
				.exec("sh", "-c", command)) {
//			execWatch.
//			int exitCode = execWatch.exitCode().get(30, TimeUnit.SECONDS);
//			if (exitCode != 0) {
//				throw new RuntimeException("下载失败，退出码: " + exitCode);
//			}
		}
		return 0;
	}
}
