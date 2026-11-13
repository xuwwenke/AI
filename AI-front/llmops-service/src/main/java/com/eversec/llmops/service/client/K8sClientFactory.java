package com.eversec.llmops.service.client;

import com.eversec.llmops.service.entity.Cluster;
import com.eversec.llmops.service.mapper.ClusterMapper;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class K8sClientFactory {

    @Autowired
    private ClusterMapper clusterMapper;

    private Map<String, OpenShiftClient> clientCache = new ConcurrentHashMap<>();

    @Value("${default.cluster}")
    private String DEFAULT_CLUSTER;

    //TODO 指定默认集群
    public OpenShiftClient getClient(String clusterName) {
        if(clusterName==null || clusterName.isEmpty()) {
            clusterName = DEFAULT_CLUSTER;
        }
        OpenShiftClient client = clientCache.get(clusterName);
        if (client != null) {
            return client;
        }

        Cluster cluster = getClusterByName(clusterName);
        if (cluster == null) {
            throw new RuntimeException("Cluster not found: " + clusterName);
        }

        OpenShiftConfig config = new OpenShiftConfigBuilder()
                .withMasterUrl(cluster.getK8sServerUrl())
                .withOauthToken(cluster.getToken())
                .withTrustCerts(true)
                .build();

        client = new DefaultOpenShiftClient(config);
        clientCache.put(clusterName, client);
        return client;
    }

    //TODO 指定默认集群
    private Cluster getClusterByName(String clusterName) {
        return clusterMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Cluster>()
                .eq(Cluster::getName, clusterName));
    }

    public void invalidateClient(String clusterName) {
        OpenShiftClient client = clientCache.remove(clusterName);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
            }
        }
    }
}