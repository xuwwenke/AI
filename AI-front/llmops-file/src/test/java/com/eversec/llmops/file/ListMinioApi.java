package com.eversec.llmops.file;

import io.minio.MinioClient;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ListMinioApi {
    public static void main(String[] args) throws Exception {
        // 查找 MinIO JAR 文件
        String userHome = System.getProperty("user.home");
        String jarPath = userHome + "\\.m2\\repository\\io\\minio\\minio\\8.5.7\\minio-8.5.7.jar";
        
        System.out.println("正在读取 JAR: " + jarPath);
        
        List<String> classNames = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && !name.contains("$")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    classNames.add(className);
                }
            }
        }

        System.out.println("\n=== io.minio 包下的类 ===");
        for (String className : classNames) {
            if (className.startsWith("io.minio.")) {
                System.out.println(className);
            }
        }

        System.out.println("\n=== MinioClient 类的所有公共方法 ===");
        Method[] methods = MinioClient.class.getMethods();
        for (Method method : methods) {
            System.out.println(method.getName() + ": " + method);
        }
    }
}
