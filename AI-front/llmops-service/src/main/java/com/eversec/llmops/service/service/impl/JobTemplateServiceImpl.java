package com.eversec.llmops.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eversec.framework.core.data.QueryResult;
import com.eversec.llmops.service.dto.JobTemplateDTO;
import com.eversec.llmops.service.entity.JobTemplate;
import com.eversec.llmops.service.mapper.JobTemplateMapper;
import com.eversec.llmops.service.service.JobTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-10
 */
@Service
public class JobTemplateServiceImpl extends ServiceImpl<JobTemplateMapper, JobTemplate> implements JobTemplateService {

    @Override
    public QueryResult<JobTemplateDTO> customGetPageList(Integer pageNum, Integer pageSize, String creator, String modifier, String version, Integer imagesId, Integer projectId, Integer bizProjectId) {
        int offset = (pageNum - 1) * pageSize;
        List<JobTemplateDTO> jobTemplateList = baseMapper.customGetPageList(offset, pageSize, creator, modifier, version, imagesId, projectId, bizProjectId);
        int totalCount = baseMapper.customGetTotalCount(creator, modifier, version, imagesId, projectId, bizProjectId);
        QueryResult<JobTemplateDTO> queryResult = new QueryResult<>();
        queryResult.setResultData(jobTemplateList);
        queryResult.setTotalRecord(totalCount);
        return queryResult;
    }

    @Override
    public JobTemplate getDetailsById(Integer id) {
        return baseMapper.getDetailsById(id);
    }

    @Override
    public QueryResult<JobTemplateDTO> list(Integer projectId) {
        QueryResult<JobTemplateDTO> queryResult = new QueryResult<>();
        List<JobTemplateDTO> data = this.baseMapper.list(projectId);
        queryResult.setResultData(data);
        return queryResult;
    }
}
