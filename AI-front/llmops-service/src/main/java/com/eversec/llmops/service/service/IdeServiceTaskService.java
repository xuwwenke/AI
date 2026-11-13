package com.eversec.llmops.service.service;

import com.eversec.llmops.service.entity.IdeServiceTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eversec.llmops.service.entity.ProUser;

import java.util.List;

/**
 * <p>
 * 异步创建服务任务表 服务类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-04
 */
public interface IdeServiceTaskService extends IService<IdeServiceTask> {

    void saveIdeServiceTask(String namespace,List<ProUser> insertList);

    void deleteIdeServiceTask(String namespace,List<String> userNames);

    void doDealIdeService(String namespace);

    /**
     * 异步执行 IDE 服务处理任务
     * @param namespace 命名空间
     */
    void doDealIdeServiceAsync(String namespace);

    IdeServiceTask get(String nameSpace,Long userId,String userName,Boolean ifAdmin);
}
