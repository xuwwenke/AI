package com.eversec.llmops.file.config;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class BucketStartupInitializer {

    private static final String[] REQUIRED_BUCKETS = {"model", "dataset","icon","project-file"};
    private static final String ICON_BUCKET = "icon";
    private static final String PROJECT_FILE_BUCKET = "project-file";

    @Autowired
    private MinioClient minioClient;

    private String buildIconReadOnlyPolicy(String bucketName) {
        return ("{"
                + "\"Version\":\"2012-10-17\","
                + "\"Statement\":["
                + "{"
                + "\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]"
                + "}"
                + "]"
                + "}").replaceAll("\\s+", "");
    }

    private String normalizePolicy(String policy) {
        return policy == null ? "" : policy.replaceAll("\\s+", "");
    }

    private void ensureIconBucketPolicy(String bucketName) {
        try {
            String expectedPolicy = buildIconReadOnlyPolicy(bucketName);
            String currentPolicy = normalizePolicy(
                    minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build())
            );
            if (!expectedPolicy.equals(currentPolicy)) {
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(expectedPolicy)
                                .build()
                );
                log.info("icon bucket策略已更新: {}", bucketName);
            } else {
                log.info("icon bucket策略无需更新: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("icon bucket策略设置失败，但不会影响服务启动: {}", bucketName, e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBuckets() {
        for (String bucketName : REQUIRED_BUCKETS) {
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (exists) {
                    log.info("bucket已存在: {}", bucketName);
                } else {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("bucket创建成功: {}", bucketName);
                }

                if (ICON_BUCKET.equals(bucketName)) {
                    ensureIconBucketPolicy(bucketName);
                }

                if (PROJECT_FILE_BUCKET.equals(bucketName)) {
                    setBucketAutoDeleteFiles(bucketName);
                }
            } catch (Exception e) {
                throw new RuntimeException("初始化bucket失败: " + bucketName, e);
            }
        }
    }

    private void setBucketAutoDeleteFiles(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Expiration expirationRule1 = new Expiration(
                (ZonedDateTime) null,
                1,
                (Boolean) null
        );
        RuleFilter filterRule1 = new RuleFilter("");
        LifecycleRule rule1 = new LifecycleRule(
                Status.ENABLED,
                null,
                expirationRule1,
                filterRule1,
                "expire-rule",
                null, null, null
        );
        List<LifecycleRule> rules = new LinkedList<>();
        rules.add(rule1);
        LifecycleConfiguration config = new LifecycleConfiguration(rules);
        minioClient.setBucketLifecycle(
                SetBucketLifecycleArgs.builder()
                        .bucket(bucketName)
                        .config(config)
                        .build()
        );
    }
}
