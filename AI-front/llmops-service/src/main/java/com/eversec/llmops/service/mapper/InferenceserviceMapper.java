package com.eversec.llmops.service.mapper;

import com.eversec.llmops.service.entity.Inferenceservice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InferenceserviceMapper extends BaseMapper<Inferenceservice> {
    /**
     * 自定义分页查询
     * @param offset 偏移量
     * @param limit 每页数量
     * @param namespace 项目名称
     * @param name 名称
     * @param label 标签
     * @param modelName 模型名称
     * @param modelVersion 模型版本
     * @param modelUrl 模型URL
     * @param status 状态
     * @param gpu GPU
     * @return 列表
     */
    List<Inferenceservice> customGetPageList(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("namespace") String namespace, @Param("name") String name, @Param("label") String label, @Param("modelName") String modelName, @Param("modelVersion") String modelVersion, @Param("modelUrl") String modelUrl, @Param("status") String status, @Param("gpu") String gpu);

    /**
     * 获取总数
     * @param namespace 项目名称
     * @param name 名称
     * @param label 标签
     * @param modelName 模型名称
     * @param modelVersion 模型版本
     * @param modelUrl 模型URL
     * @param status 状态
     * @param gpu GPU
     * @return 总数
     */
    int customGetTotalCount(@Param("namespace") String namespace, @Param("name") String name, @Param("label") String label, @Param("modelName") String modelName, @Param("modelVersion") String modelVersion, @Param("modelUrl") String modelUrl, @Param("status") String status, @Param("gpu") String gpu);

    /**
     * 通过id查询，关联pro_manager信息
     * @param id 主键id
     * @return Inferenceservice
     */
    Inferenceservice selectByIdWithProManager(@Param("id") Integer id);

    /**
     * 通过projectId查询model_status为online的inferenceservice列表
     * @param projectId 项目id
     * @return 列表
     */
    List<Inferenceservice> selectOnlineByProjectId(@Param("projectId") Integer projectId);

    /**
     * 通过projectId查询总数
     * @param projectId 项目id
     * @return 总数
     */
    int selectTotalCountByProjectId(@Param("projectId") Integer projectId);

    /**
     * 通过projectId查询online数量
     * @param projectId 项目id
     * @return online数量
     */
    int selectOnlineCountByProjectId(@Param("projectId") Integer projectId);

    /**
     * 通过projectId查询本月新增数
     * @param projectId 项目id
     * @return 本月新增数
     */
    int selectThisMonthCountByProjectId(@Param("projectId") Integer projectId);

    /**
     * 通过projectId查询所有inferenceservice
     * @param projectId 项目id
     * @return 列表
     */
    List<Inferenceservice> selectAllByProjectId(@Param("projectId") Integer projectId);
}
