package com.eversec.llmops.service.util.k8s;

import com.eversec.cloud.core.api.exception.UnknownException;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class Copy extends Exec {
	private static final Logger LOGGER = LoggerFactory.getLogger(Copy.class);

	private final static int TRANS_SIZE = 1024 * 1024;

	/**
	 * Copy Constructor
	 *
	 * @param apiClient The api client to use.
	 */
	public Copy(ApiClient apiClient) {
		super(apiClient);
	}

	public InputStream copyFileFromPod(String namespace, String pod, String srcPath) throws ApiException, IOException {
		return copyFileFromPod(namespace, pod, null, srcPath);
	}

	public InputStream copyFileFromPod(String namespace, String pod, String container, String srcPath) throws ApiException, IOException {
		Process proc = this.exec(namespace, pod, new String[] { "sh", "-c", "dd if='" + srcPath + "'" }, container, false, false);
		return proc.getInputStream();
	}

	public void copyFileToPod(String namespace, String pod, String container, InputStream inputStream, Path destPath) {
		copyFileToPod(namespace, pod, container, inputStream, destPath.toString());
	}

	public void copyFileToPod(String namespace, String pod, InputStream inputStream, Path destPath) {
		copyFileToPod(namespace, pod, null, inputStream, destPath.toString());
	}

	public void copyFileToPod(String namespace, String pod, String container, byte[] src, Path destPath) {
		copyFileToPod(namespace, pod, container, new ByteArrayInputStream(src), destPath.toString());
	}

	public void copyFileToPod(String namespace, String pod, byte[] src, Path destPath) {
		copyFileToPod(namespace, pod, null, new ByteArrayInputStream(src), destPath.toString());
	}

	public void copyFileToPod(String namespace, String pod, String container, Path srcPath, Path destPath) throws FileNotFoundException {
		copyFileToPod(namespace, pod, container, new FileInputStream(srcPath.toFile()), destPath.toString());
	}

	public void copyFileToPod(String namespace, String pod, Path srcPath, Path destPath) throws FileNotFoundException {
		copyFileToPod(namespace, pod, null, new FileInputStream(srcPath.toFile()), destPath.toString());
	}

	public void copyFileToPod(String namespace, String pod, String container, InputStream input, String fileInPod) {
		ForkJoinTask<Integer> ret = ForkJoinPool.commonPool().submit(() -> {
			try {
				LOGGER.info("开始执行命令");

				Process proc = this.exec(namespace, pod,
						new String[] { "sh", "-c", MessageFormat.format("rm -rf ''{0}'' && dd conv=notrunc of=''{1}''", fileInPod, fileInPod) }, container,
						true, false);

				LOGGER.info("复制文件内容");

				OutputStream outputStream = proc.getOutputStream();
				copyStream(input, outputStream, true);
				LOGGER.info("复制完成，关闭连接");

				proc.destroy();

			} catch (IOException e) {
				throw new UnknownException("执行命令失败", e);
			}
			return 0;
		});
		try {
			ret.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("执行文件复制失败", e);
			throw new UnknownException("执行文件复制失败", e);
		}
	}

	public Integer execWithStdin(String namespace, String pod, String container, InputStream input, String cmd) {
		ForkJoinTask<Integer> ret = ForkJoinPool.commonPool().submit(() -> {
			try {
				LOGGER.info("开始执行命令");

				Process proc = this.exec(namespace, pod, new String[] { "sh", "-c", cmd }, container, true, false);

				LOGGER.info("标准输入写入数据");

				OutputStream outputStream = proc.getOutputStream();
				Streams.copy(input, outputStream);
				outputStream.flush();
				outputStream.close();

				LOGGER.info("结束程序");

				proc.destroy();

			} catch (IOException e) {
				throw new UnknownException("执行命令失败", e);
			}
			return 0;
		});
		try {
			return ret.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("execWithStdin 异常", e);
			return -2;
		}
	}

	public static long copyStream(InputStream in, OutputStream out, boolean immediately) throws IOException {
		byte[] buffer = new byte[TRANS_SIZE];
		long totalBytes = 0;
		int bytesRead;
		while ((bytesRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
			totalBytes += bytesRead;
			if (immediately || totalBytes % TRANS_SIZE == 0) {
				out.flush();
			}
		}
		out.flush();
		return totalBytes;
	}
}
