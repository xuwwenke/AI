package com.eversec.llmops.file;

import io.minio.MinioClient;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinioReflectionTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== MinioClient 方法 ===");
        Method[] methods = MinioClient.class.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.contains("Multipart") || name.contains("listParts")) {
                System.out.println(name + ": " + method);
            }
        }

        System.out.println("\n=== io.minio 包下的类:");
        List<Class<?>> classes = findClasses("io.minio");
        for (Class<?> clazz : classes) {
            System.out.println(clazz.getName());
        }
    }

    public static List<Class<?>> findClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            // 这里简化实现，只打印已知的重要类
            String[] knownClasses = {
                "io.minio.CreateMultipartUploadArgs",
                "io.minio.CreateMultipartUploadResponse",
                "io.minio.CompleteMultipartUploadArgs",
                "io.minio.CompleteMultipartUploadResponse",
                "io.minio.AbortMultipartUploadArgs",
                "io.minio.ListPartsArgs",
                "io.minio.ListPartsResponse",
                "io.minio.Result",
                "io.minio.messages.Part"
            };
            for (String className : knownClasses) {
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                    System.out.println("找到类: " + className);
                    System.out.println("  方法: " + Arrays.toString(clazz.getDeclaredMethods()));
                } catch (Exception e) {
                    System.out.println("未找到类: " + className);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }
}
