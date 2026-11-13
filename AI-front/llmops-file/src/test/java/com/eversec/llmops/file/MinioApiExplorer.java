package com.eversec.llmops.file;

import io.minio.MinioClient;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MinioApiExplorer {
    public static void main(String[] args) {
        System.out.println("=== MinioClient 类的所有公共方法 ===");
        Method[] methods = MinioClient.class.getMethods();
        Arrays.sort(methods, (m1, m2) -> m1.getName().compareTo(m2.getName()));
        
        for (Method method : methods) {
            System.out.println("\n" + method.getName());
            System.out.println("  完整签名: " + method);
            System.out.println("  参数类型: " + Arrays.toString(method.getParameterTypes()));
        }
    }
}
