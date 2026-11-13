package com.eversec.llmops.service.controller;

import cn.hutool.core.util.StrUtil;
import com.eversec.cloud.core.api.exception.ClientException;
import com.eversec.cloud.fileuploader.FileChunkData;
import com.eversec.cloud.fileuploader.param.FileChunkBeginParam;
import com.eversec.cloud.fileuploader.param.FileChunkEndParam;
import com.eversec.cloud.fileuploader.param.FileChunkUploadParam;
import com.eversec.llmops.service.service.K8sFileService;
import com.eversec.llmops.service.util.k8s.PodFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author wangfei
 */
@Api(tags = "容器文件管理")
@Slf4j
@AllArgsConstructor
@RestController
public class K8sFileController {
	private final K8sFileService k8sFileService;

	/**
	 * 自定义接口：exec 执行k8s容器命令
	 */
	@ApiOperation("执行k8s容器命令")
	@GetMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/exec" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String exec(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace, @PathVariable("name") String name,
			@RequestParam("cmd") String cmd) {
		String ret = k8sFileService.exec(clusterName, namespace, name, cmd);
		return ret;
	}

	/**
	 * 自定义接口：遍历文件目录
	 */
	@ApiOperation("遍历文件目录")
	@GetMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/list" }, produces = { "application/json;charset=UTF-8" })
	public List<PodFile> getPodFiles(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace,
			@PathVariable("name") String name, @RequestParam(value = "dirPath", required = false) String dirPath,
			@RequestParam(value = "showHidden", required = false) Boolean showHidden) {
		return k8sFileService.getPodFiles(clusterName, namespace, name, dirPath, showHidden, false);
	}

	/**
	 * 自定义接口：查询文件内容
	 */
	@ApiOperation("查询文件内容")
	@GetMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/file" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPodFileContent(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace,
			@PathVariable("name") String name, @RequestParam("filePath") String filePath) {
		return k8sFileService.getPodFileContent(clusterName, namespace, name, filePath);
	}

	@ApiOperation("查询文件内容（只要其中一个文件有内容就返回）")
	@GetMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/files" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPodFileContentByFiles(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace,
			@PathVariable("name") String name, @RequestParam("filePaths") List<String> filePaths) {
		for (String filePath : filePaths) {
			try {
				String podFileContent = k8sFileService.getPodFileContent(clusterName, namespace, name, filePath);
				return podFileContent;
			} catch (Exception e) {
				log.info("文件不存在：{}：{}：{}", namespace, name, filePath);
			}
		}
		throw new ClientException(StrUtil.format("版本文件不存在：{}：{}：{}", namespace, name, filePaths));
	}

	/**
	 * 上传文件到容器
	 *
	 * @param namespace
	 * @param name
	 * @param file
	 * @throws IOException
	 */
	@ApiOperation("上传文件到容器")
	@PostMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/upload" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String  upload(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace, @PathVariable("name") String name,
			@RequestParam("path") String filePath, @RequestParam(required = false) MultipartFile file) throws IOException {
		k8sFileService.upload(clusterName, namespace, name, filePath, file.getInputStream());
		return filePath;
	}

	// TODO: 2022-11-04 此处没有用到minio和nfs，在部署多实例时可能会存在问题

	/**
	 * 分片上传文件到容器：开始上传
	 */
	@ApiOperation("分片上传文件到容器：开始上传")
	@PostMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/chunk/begin" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public FileChunkData chunkBegin(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace,
			@PathVariable("name") String name, @RequestBody FileChunkBeginParam param) throws IOException {
		return k8sFileService.chunkBegin(clusterName, namespace, name, param);
	}

	/**
	 * 分片上传文件到容器：上传分片
	 */
	@ApiOperation("分片上传文件到容器：上传分片")
	@PostMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/chunk/upload" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public void chunkUpload(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace, @PathVariable("name") String name,
			@RequestParam("fileName") String fileName, @RequestParam("fileMd5") String fileMd5, @RequestParam("chunkIndex") int chunkIndex,
			@RequestParam("file") MultipartFile chunkData) throws Exception {
		k8sFileService.chunkUpload(clusterName, namespace, name, new FileChunkUploadParam(fileName, fileMd5, chunkData.getBytes(), chunkIndex));
	}

	/**
	 * 分片上传文件到容器：上传完成
	 */
	@ApiOperation("分片上传文件到容器：上传完成")
	@PostMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/chunk/end" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public void chunkEnd(
			@RequestHeader("clusterName") String clusterName,
			@PathVariable("namespace") String namespace, @PathVariable("name") String name,
			@RequestBody FileChunkEndParam param) throws Exception {
		k8sFileService.chunkEnd(clusterName, namespace, name, param);
	}

	/**
	 * 从容器下载文件
	 */
	@ApiOperation("从容器下载文件，下载通过新窗口打开clusterName通过 url 传递")
	@GetMapping(path = { "/api/k8s-file/apps/v1/namespaces/{namespace}/pods/{name}/download" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public void download(
			@RequestParam("clusterName") String clusterName,
			HttpServletResponse servletResponse, @PathVariable("namespace") String namespace,
			@PathVariable("name") String name, @RequestParam("path") String path) throws IOException {
		List<PodFile> podFiles = k8sFileService.getPodFiles(clusterName, namespace, name, path, true, true);
		if (podFiles.isEmpty()) {
			throw new ClientException(MessageFormat.format("文件不存在: {0}/{1}:{2}", namespace, name, path));
		}
		int pos = path.lastIndexOf("/");
		String fileName = path.substring(pos + 1);
		fileName = URLEncoder.encode(fileName, "UTF-8");
		servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		servletResponse.setContentType("application/octet-stream;charset=UTF-8");
		servletResponse.setContentLengthLong(podFiles.get(0).getFileSize());
		k8sFileService.download(clusterName, namespace, name, path, servletResponse.getOutputStream());
	}
}
