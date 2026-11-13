package com.eversec.llmops.file;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MinioClassCheck {
    public static void main(String[] args) throws Exception {
        // 找到 MinIO JAR 包
        String userHome = System.getProperty("user.home");
        String mavenRepo = userHome + "\\.m2\\repository\\io\\minio\\minio\\8.5.7\\minio-8.5.7.jar";
        
        System.out.println("正在读取 MinIO JAR: " + mavenRepo);
        
        JarFile jarFile = new JarFile(mavenRepo);
        Enumeration<JarEntry> entries = jarFile.entries();
        
        System.out.println("\n=== MinIO 包中的类:");
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".class") && name.startsWith("io/minio/")) {
                String className = name.replace('/', '.').substring(0, name.length() - 6);
                System.out.println("  - " + className);
            }
        }
        jarFile.close();
    }
}
