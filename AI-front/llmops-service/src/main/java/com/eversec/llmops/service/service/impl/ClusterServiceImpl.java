package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.ClusterDTO;
import com.eversec.llmops.service.dto.ClusterUsageDTO;
import com.eversec.llmops.service.entity.Cluster;
import com.eversec.llmops.service.mapper.ClusterMapper;
import com.eversec.llmops.service.service.ClusterService;
import com.eversec.llmops.service.service.KubernetesResourceService;
import com.eversec.llmops.service.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-28
 */
@Service
public class ClusterServiceImpl extends ServiceImpl<ClusterMapper, Cluster> implements ClusterService {

    @Autowired
    private KubernetesResourceService kubernetesResourceService;

    @Override
    public QueryResult<Cluster> getPageList(Integer pageNum, Integer pageSize, String name, String ip) {
        Page<Cluster> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Cluster> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like(Cluster::getName, name);
        }
        if (ip != null && !ip.isEmpty()) {
            wrapper.like(Cluster::getServiceIp, ip);
        }

        wrapper.orderByDesc(Cluster::getId);
        Page<Cluster> resultPage = page(page, wrapper);

        QueryResult<Cluster> queryResult = new QueryResult<>();
        queryResult.setResultData(resultPage.getRecords());
        queryResult.setTotalRecord(resultPage.getTotal());
        return queryResult;
    }

    @Override
    public Cluster createCluster(ClusterDTO dto) {
        Cluster cluster = new Cluster();
        BeanUtils.copyProperties(dto, cluster);
        cluster.setCreatetime(LocalDateTime.now());
        cluster.setUpdatetime(LocalDateTime.now());
        cluster.setOperator(UserUtil.getAbUserUserName());
        save(cluster);
        return cluster;
    }

    @Override
    public boolean updateCluster(ClusterDTO dto) {
        Cluster cluster = getById(dto.getId());
        if (cluster == null) {
            return false;
        }
        BeanUtils.copyProperties(dto, cluster);
        cluster.setUpdatetime(LocalDateTime.now());
        cluster.setOperator(UserUtil.getAbUserUserName());
        return updateById(cluster);
    }

    @Override
    public String getServiceIpByName(String clusterName) {
        Cluster cluster = lambdaQuery()
                .eq(Cluster::getName, clusterName)
                .one();
        return cluster != null ? cluster.getServiceIp() : null;
    }

    @Override
    public ClusterUsageDTO getClusterUsage(String clusterName) {
        return kubernetesResourceService.getClusterUsage(clusterName);
    }
}