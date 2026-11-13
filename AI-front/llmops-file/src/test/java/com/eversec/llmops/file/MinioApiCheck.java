package com.eversec.llmops.file;

import io.minio.MinioClient;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MinioApiCheck {
    public static void main(String[] args) {
        System.out.println("=== MinioClient 所有方法 ===");
        Method[] methods = MinioClient.class.getMethods();
        Arrays.stream(methods)
                .map(Method::getName)
                .distinct()
                .sorted()
                .forEach(name -> System.out.println("  - " + name));

        System.out.println("\n=== 查找包含 multipart 或 listParts 的方法 ===");
        Arrays.stream(methods)
                .filter(m -> m.getName().toLowerCase().contains("multipart") || 
                             m.getName().toLowerCase().contains("listparts"))
                .forEach(m -> System.out.println("  - " + m.getName() + ": " + m));
    }
}
