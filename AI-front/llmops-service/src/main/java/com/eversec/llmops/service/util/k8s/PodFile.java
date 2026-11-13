package com.eversec.llmops.service.util.k8s;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PodFile {
    /**
     * 0. 文件后缀满足上述条件可以查看
     * 1. 文件大小小于10KB可以查看
     * 2. 文件大小大于1MB不能查看
     */
    private final static int VIEW_MODE_ALLOW = 0;
    private final static int VIEW_MODE_DENY_FORMAT = 1;
    private final static int VIEW_MODE_DENY_SIZE = 2;
    private final static long MAX_VIEW_FILE_SIZE = 1024 * 1024;
    private final static long ALLOW_VIEW_FILE_SIZE = 10 * 1024;
    private final static String[] VIEW_EXT_LIST = new String[]{
        ".txt",
        ".ini",
        ".conf",
        ".properties",
        ".xml",
        ".yaml",
        ".yml",
        ".json",
        ".lock",
        ".config",
        ".cfg",
        ".md",
        ".js",
        ".html",
        ".css",
        ".pem",
        ".toml",
        ".log",
        ".py",
        ".sh",
        ".bat",
        ".cmd",
        ".php",
        ".java",
        ".groovy",
        ".gradle"
    };

    private String permission;
    private long fileSize;
    private String fileName;
    private String owner;
    private String group;
    private String time;
    private String link;
    /**
     * VIEW_MODE_ALLOW
     * VIEW_MODE_DENY_FORMAT
     * VIEW_MODE_DENY_SIZE
     */
    private int viewMode;

    public PodFile(String permission, long fileSize, String fileName, String owner, String group, String time, String link) {
        this.permission = permission;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.owner = owner;
        this.group = group;
        this.time = time;
        this.link = link;
        this.viewMode = getMode(fileSize, fileName);
    }

    private static int getMode(long fileSize, String fileName) {
        if (ALLOW_VIEW_FILE_SIZE >= fileSize) {
            return VIEW_MODE_ALLOW;
        }
        if (MAX_VIEW_FILE_SIZE < fileSize) {
            return VIEW_MODE_DENY_SIZE;
        }

        for (int i = 0; i < VIEW_EXT_LIST.length; i++) {
            if (fileName.endsWith(VIEW_EXT_LIST[i])) {
                return VIEW_MODE_ALLOW;
            }
        }

        return VIEW_MODE_DENY_FORMAT;
    }
}
