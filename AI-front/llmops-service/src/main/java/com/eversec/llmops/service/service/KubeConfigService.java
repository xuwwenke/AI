package com.eversec.llmops.service.service;

import com.alibaba.fastjson.JSON;
import com.eversec.llmops.service.config.KubeConfig;
import com.eversec.llmops.service.entity.Cluster;
import com.eversec.llmops.service.mapper.ClusterMapper;
import io.kubernetes.client.util.Yaml;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangfei
 */
@AllArgsConstructor
@Service
public class KubeConfigService {
    private ClusterMapper clusterApi;

    public KubeConfig getKubeConfig() {
        KubeConfig kubeConfig = new KubeConfig();
        List<Cluster> clusters = clusterApi.selectList( null);
        clusters.forEach(cluster -> {
            kubeConfig.add(cluster.getName(), cluster.getK8sServerUrl(), cluster.getToken());
        });
        return kubeConfig;
    }

    public String getKubeConfigYaml() {
        KubeConfig kubeConfig = getKubeConfig();
        return Yaml.dump(JSON.parseObject(JSON.toJSONString(kubeConfig)));
    }
}
