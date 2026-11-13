package com.eversec.llmops.service.service.impl;

import com.eversec.framework.core.response2.ResponseHolder;
import com.eversec.llmops.service.entity.IdeServiceTask;
import com.eversec.llmops.service.entity.ProManager;
import com.eversec.llmops.service.entity.ProUser;
import com.eversec.llmops.service.mapper.IdeServiceTaskMapper;
import com.eversec.llmops.service.mapper.ProUserMapper;
import com.eversec.llmops.service.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.llmops.service.util.UserUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 异步创建服务任务表 服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-06-04
 */
@Service
public class IdeServiceTaskServiceImpl extends ServiceImpl<IdeServiceTaskMapper, IdeServiceTask> implements IdeServiceTaskService {

    private final String WAITING_STATUS = "waiting";
    private final String DEPLOYING_STATUS = "deploying";
    private final String DEPLOYED_STATUS = "deployed";
    private final String FAILED_STATUS = "failed";
    private final String TO_DELETED_STATUS = "to_delete";
    private final String DELETED_STATUS = "deleted";


    @Resource
    private RedissonClient redissonClient;

    @Resource
    private PVCService pvcService;

    @Value("${code-server-operator-server}")
    private String codeServer;


    @Resource
    private ProManagerService proManagerService;

    @Resource
    private SysSecurityUserService sysSecurityUserService;


    @Resource
    private ProUserMapper proUserMapper;

    @Override
    @Transactional
    public void saveIdeServiceTask(String namespace, List<ProUser> insertList) {
        if (insertList == null || insertList.isEmpty()) return;
        List<IdeServiceTask> ideServiceTasks = new ArrayList<>();
        for (ProUser proUser : insertList) {
            IdeServiceTask ideServiceTask = new IdeServiceTask();
            ideServiceTask.setNamespace(namespace);
            ideServiceTask.setUserName(proUser.getUsername());
            ideServiceTask.setStatus(WAITING_STATUS);
            ideServiceTasks.add(ideServiceTask);
        }
        this.saveBatch(ideServiceTasks);
    }

    @Override
    @Transactional
    public void deleteIdeServiceTask(String namespace, List<String> userNames) {
        if (userNames == null || userNames.isEmpty()) {
            return;
        }
        // 查询指定namespace和userNames的记录
        LambdaQueryWrapper<IdeServiceTask> queryWrapper =
                new LambdaQueryWrapper<>();
        queryWrapper.eq(IdeServiceTask::getNamespace, namespace)
                .in(IdeServiceTask::getUserName, userNames);
        List<IdeServiceTask> tasks = this.list(queryWrapper);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        // 更新状态为 to_delete
        for (IdeServiceTask task : tasks) {
            task.setStatus(TO_DELETED_STATUS);
        }
        // 批量更新
        this.updateBatchById(tasks);
    }

    @Override
    @Async("taskExecutor")
    public void doDealIdeServiceAsync(String namespace) {
        doDealIdeService(namespace);
    }

    @Override
    @Transactional
    public IdeServiceTask get(String namespace, Long userId, String userName,Boolean ifAdmin) {
        LambdaQueryWrapper<IdeServiceTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IdeServiceTask::getNamespace, namespace);
        queryWrapper.eq(IdeServiceTask::getUserName, UserUtil.tryGetLoginUsername());
        IdeServiceTask ideServiceTask = this.getOne(queryWrapper);
        //异常或link为空，则返回null,前端页面提示加载
        if(ideServiceTask != null && (ideServiceTask.getUserIdeLink()==null || ideServiceTask.getUserIdeLink().equals("Error")) ){
            return null;
        }
        //正常，则返回链接
        if (ideServiceTask != null) {
            return ideServiceTask;
        }
        ProManager proManager = proManagerService.getByName(namespace);
        // ideServiceTask 为空，检查 pro_user 里是否有记录
        LambdaQueryWrapper<ProUser> proUserQueryWrapper = new LambdaQueryWrapper<>();
        proUserQueryWrapper.eq(ProUser::getUserId, userId)
                .eq(ProUser::getProId, proManager.getProjectId());
        ProUser proUser = proUserMapper.selectOne(proUserQueryWrapper);
        if (proUser == null&&!ifAdmin) {
            // 普通用户，pro_user 里没有记录，返回空
            return null;
        }
        if(proUser==null){
            proUser = new ProUser();
            proUser.setUserId(userId);
            proUser.setUsername(userName);
            proUser.setProId(proManager.getProjectId());
            proUser.setIsAdmin(1);
            proUserMapper.insert(proUser);
        }
        List<ProUser> proUsers = new ArrayList<>();
        proUsers.add(proUser);
        // 有记录，执行 ideServiceTask 插入逻辑
        this.saveIdeServiceTask(namespace, proUsers);
        // 异步执行创建 codeServer 的逻辑
        this.doDealIdeServiceAsync(namespace);
        // 重新查询返回
        return this.getOne(queryWrapper);
    }

    @Override
    public void doDealIdeService(String namespace) {
        RLock lock = redissonClient.getLock("doDealIdeService:" + namespace);
        try {
            boolean isLocked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            if (isLocked) {
                // 查询指定 namespace 下 status 为 waiting 或 to_delete 的任务
                LambdaQueryWrapper<IdeServiceTask> queryWrapper =
                        new LambdaQueryWrapper<>();
                queryWrapper.eq(IdeServiceTask::getNamespace, namespace)
                        .in(IdeServiceTask::getStatus, WAITING_STATUS, TO_DELETED_STATUS);
                List<IdeServiceTask> tasks = this.list(queryWrapper);
                if (tasks == null || tasks.isEmpty()) {
                    return;
                }
                // 获取集群名称
                String clusterName = proManagerService.getByName(namespace).getClusterName();
                for (IdeServiceTask task : tasks) {
                    try {
                        String username = task.getUserName();
                        if (WAITING_STATUS.equals(task.getStatus())) {
                            String pvcName = task.getStorageName();
                            if (pvcName == null || pvcName.isEmpty()) {
                                pvcName = buildPvcName(username);
                            }
                            // waiting 状态：创建 PVC，然后创建 codeServer link，最后更新状态为 deploying
                            handleWaitingTask(task, clusterName, namespace, pvcName);
                        } else if (TO_DELETED_STATUS.equals(task.getStatus())) {
                            // to_delete 状态：删除 PVC，然后删除 codeServer link
                            handleToDeleteTask(task, clusterName, namespace, task.getStorageName());
                        }
                    } catch (Exception e) {
                        task.setStatus(FAILED_STATUS);
                        task.setErrorMsg(e.getMessage());
                        this.updateById(task);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    /**
     * 每3分钟执行一次：查询 deploying 状态且 user_ide_link 为空的记录，
     * 调用 codeServer 的 /link 接口获取 link，并更新 ide_service_task
     */
    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void checkDeployingTasks() {
        retryFailedOrErrorTasks();
        syncDeployingTasks();
    }

    private void retryFailedOrErrorTasks() {
        List<IdeServiceTask> retryTasks = listRetryTasks();
        if (retryTasks == null || retryTasks.isEmpty()) {
            return;
        }

        for (IdeServiceTask task : retryTasks) {
            handleRetryTask(task);
        }
    }

    private List<IdeServiceTask> listRetryTasks() {
        LambdaQueryWrapper<IdeServiceTask> retryQueryWrapper = new LambdaQueryWrapper<>();
        retryQueryWrapper.eq(IdeServiceTask::getStatus, FAILED_STATUS)
                .or()
                .eq(IdeServiceTask::getUserIdeLink, "Error");
        return this.list(retryQueryWrapper);
    }

    private void handleRetryTask(IdeServiceTask task) {
        try {
            CodeServerLinkResponse linkResponse = getCodeServerLink(task.getUserName(), task.getNamespace());
            if (isValidUserIdeLinkResponse(linkResponse)) {
                task.setUserIdeLink(linkResponse.getUserIdeLink());
                task.setStatus(DEPLOYED_STATUS);
                task.setErrorMsg(null);
                this.updateById(task);
            }
        } catch (Exception e) {
            task.setErrorMsg(e.getMessage());
            this.updateById(task);
        }
    }

    private void syncDeployingTasks() {
        List<IdeServiceTask> tasks = listDeployingTasks();
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (IdeServiceTask task : tasks) {
            handleDeployingTask(task);
        }
    }

    private List<IdeServiceTask> listDeployingTasks() {
        LambdaQueryWrapper<IdeServiceTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IdeServiceTask::getStatus, DEPLOYING_STATUS)
                .isNull(IdeServiceTask::getUserIdeLink);
        return this.list(queryWrapper);
    }

    private void handleDeployingTask(IdeServiceTask task) {
        try {
            CodeServerLinkResponse linkResponse = getCodeServerLink(task.getUserName(), task.getNamespace());
            if (linkResponse == null) {
                return;
            }
            if (!linkResponse.isSuccess()) {
                task.setStatus(FAILED_STATUS);
                this.updateById(task);
                return;
            }
            if (hasValidUserIdeLink(linkResponse.getUserIdeLink())) {
                task.setUserIdeLink(linkResponse.getUserIdeLink());
                task.setStatus(DEPLOYED_STATUS);
                this.updateById(task);
            }
        } catch (Exception e) {
            // 记录错误信息但不影响其他任务
            task.setStatus(FAILED_STATUS);
            task.setErrorMsg(e.getMessage());
            this.updateById(task);
        }
    }

    private boolean isValidUserIdeLinkResponse(CodeServerLinkResponse linkResponse) {
        return linkResponse != null
                && linkResponse.isSuccess()
                && hasValidUserIdeLink(linkResponse.getUserIdeLink());
    }

    private boolean hasValidUserIdeLink(String userIdeLink) {
        return userIdeLink != null
                && !userIdeLink.isEmpty()
                && !"Error".equalsIgnoreCase(userIdeLink);
    }

    /**
     * 处理 waiting 状态的任务
     * 1. 创建 PVC code-server-${username}
     * 2. 创建 codeServer link
     * 3. 更新状态为 deploying
     */
    private void handleWaitingTask(IdeServiceTask task, String clusterName, String namespace, String pvcName) {
        String username = task.getUserName();

        // 创建 PVC

        Map<String, String> labels = new HashMap<>();
        labels.put("buz-type", "code-server");
        pvcService.createPVC(clusterName, namespace, pvcName, "local-path", "1Gi",
                java.util.Collections.singletonList("ReadWriteOnce"),
                labels);
        // 创建 codeServer link
        task.setStorageName(pvcName);
        CodeServerLinkResponse linkResponse = createCodeServerLink(username, namespace, pvcName);
        if (linkResponse != null) {
            // 更新状态为 deploying
            task.setStatus(DEPLOYING_STATUS);
            if (linkResponse.getUserIdeLink() != null) {
                task.setUserIdeLink(linkResponse.getUserIdeLink());
            }
            this.updateById(task);
        } else {
            // 创建 link 失败
            task.setStatus(FAILED_STATUS);
            task.setErrorMsg("创建 codeServer link 失败");
            this.updateById(task);
        }
    }



    private String buildPvcName(String username) {
        return "code-server-" + username + "-" + Long.toString(System.currentTimeMillis(), 36);
    }

    /**
     * 处理 to_delete 状态的任务
     * 1. 删除 PVC code-server-${username}
     * 2. 删除 codeServer link
     */
    private void handleToDeleteTask(IdeServiceTask task, String clusterName, String namespace, String pvcName) {
        String username = task.getUserName();
        DeleteCodeServerResponse deleteCodeServerResponse = deleteCodeServerLink(username, namespace);
        if(deleteCodeServerResponse==null){
            return;
        }
        pvcService.markPVCToDelete(clusterName, namespace, pvcName, java.time.Instant.now().toString());
        this.removeById(task.getId());
    }

    /**
     * 检查 codeServer 健康状态
     *
     * @return true-健康，false-不健康
     */
    public boolean checkCodeServerHealth() {
        try {
            RestTemplate restTemplate = createRestTemplate();
            String url = codeServer + "/health";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "curl/7.68.0");
            headers.set("Accept", "application/json");

            HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object status = response.getBody().get("status");
                return "ok".equals(status);
            }
            return false;
        } catch (Exception e) {
            // 记录日志但不抛出异常
            return false;
        }
    }

    /**
     * 创建 codeServer link
     *
     * @param userId      用户ID
     * @param namespace   命名空间
     * @param storageName 存储名称
     * @return CodeServerLinkResponse 响应对象
     */
    public CodeServerLinkResponse createCodeServerLink(String userId, String namespace, String storageName) {
        try {
            RestTemplate restTemplate = createRestTemplate();
            String url = codeServer + "/link";

            // 构建请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("namespace", namespace);
            requestBody.put("storage_name", storageName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "curl/7.68.0");
            headers.set("Accept", "application/json");

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<CodeServerLinkResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, CodeServerLinkResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            // 记录日志但不抛出异常
            return null;
        }
    }

    /**
     * 获取 codeServer link
     *
     * @param userId    用户ID
     * @param namespace 命名空间
     * @return CodeServerLinkResponse 响应对象
     */
    public CodeServerLinkResponse getCodeServerLink(String userId, String namespace) {
        try {
            RestTemplate restTemplate = createRestTemplate();
            String url = codeServer + "/link?user_id=" + userId + "&namespace=" + namespace;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "curl/7.68.0");
            headers.set("Accept", "application/json");

            HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);

            ResponseEntity<CodeServerLinkResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, CodeServerLinkResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            // 记录日志但不抛出异常
            return null;
        }
    }

    /**
     * 删除 codeServer link
     *
     * @param userId    用户ID
     * @param namespace 命名空间
     * @return DeleteCodeServerResponse 响应对象
     */
    public DeleteCodeServerResponse deleteCodeServerLink(String userId, String namespace) {
        try {
            RestTemplate restTemplate = createRestTemplate();
            String url = codeServer + "/link";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "curl/7.68.0");
            headers.set("Accept", "application/json,text/html,*/*");

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("namespace", namespace);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            try {
                ResponseEntity<DeleteCodeServerResponse> response = restTemplate.exchange(
                        url, HttpMethod.DELETE, requestEntity, DeleteCodeServerResponse.class);
                return response.getBody();
            } catch (org.springframework.web.client.UnknownContentTypeException e) {
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.DELETE, requestEntity, String.class);
                DeleteCodeServerResponse deleteResponse = new DeleteCodeServerResponse();
                deleteResponse.setSuccess(response.getStatusCode().is2xxSuccessful());
                deleteResponse.setMessage(response.getBody());
                return deleteResponse;
            }
        } catch (Exception e) {
            DeleteCodeServerResponse deleteResponse = new DeleteCodeServerResponse();
            deleteResponse.setSuccess(false);
            deleteResponse.setMessage(e.getMessage());
            return deleteResponse;
        }
    }

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(HttpStatus statusCode) {
                return false;
            }
        });
        return restTemplate;
    }

    /**
     * CodeServer Link 响应对象
     */
    public static class CodeServerLinkResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("user_ide_link")
        private String userIdeLink;
        private boolean deploying;
        private boolean success;

        public String getUserIdeLink() {
            return userIdeLink;
        }

        public void setUserIdeLink(String userIdeLink) {
            this.userIdeLink = userIdeLink;
        }

        public boolean isDeploying() {
            return deploying;
        }

        public void setDeploying(boolean deploying) {
            this.deploying = deploying;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }

    /**
     * 删除 CodeServer 响应对象
     */
    public static class DeleteCodeServerResponse {
        private boolean success;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
