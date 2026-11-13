package com.eversec.llmops.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.dto.ClusterDTO;
import com.eversec.llmops.service.dto.ClusterUsageDTO;
import com.eversec.llmops.service.entity.Cluster;
import com.eversec.framework.core.data.QueryResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-28
 */
public interface ClusterService extends IService<Cluster> {

    QueryResult<Cluster> getPageList(Integer pageNum, Integer pageSize, String name, String ip);

    Cluster createCluster(ClusterDTO dto);

    boolean updateCluster(ClusterDTO dto);

    String getServiceIpByName(String clusterName);

    ClusterUsageDTO getClusterUsage(String clusterName);
}