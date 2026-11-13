package com.eversec.llmops.service.config;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KubeConfig {
    private final String apiVersion = "v1";
    private final String kind = "Config";
    private final List<NamedKubeCluster> clusters = new ArrayList<>();
    private final List<NamedKubeContext> contexts = new ArrayList<>();
    private final List<NamedKubeUser> users = new ArrayList<>();

    public void add(String name, String server, String token) {
        clusters.add(new NamedKubeCluster(name, new KubeCluster(true, server)));
        users.add(new NamedKubeUser(name, new KubeUser(token)));
        contexts.add(new NamedKubeContext(name, new KubeContext(name, name)));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class KubeCluster {
        @JSONField(name = "insecure-skip-tls-verify")
        private final Boolean insecureSkipTlsVerify;
        private final String server;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class NamedKubeCluster {
        private final String name;
        private final KubeCluster cluster;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class KubeContext {
        private final String cluster;
        private final String user;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class NamedKubeContext {
        private final String name;
        private final KubeContext context;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class KubeUser {
        private final String token;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class NamedKubeUser {
        private final String name;
        private final KubeUser user;
    }
}
