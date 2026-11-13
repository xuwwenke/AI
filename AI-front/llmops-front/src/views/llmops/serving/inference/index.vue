<template>
  <div class="inference-container">
    <div class="inference-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">推理服务</h1>
          <p class="page-subtitle">管理和监控在线推理服务</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="add-btn" size="small" @click="showAddDialog">
            <i class="el-icon-plus"></i> 添加推理服务
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">总服务数</div>
          <div class="stat-value">{{ inferenceStats.totalCount }}</div>
          <div class="stat-trend up">
            <i class="el-icon-top"></i> +{{ inferenceStats.thisMonthCount }} 本月新增
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value">{{ inferenceStats.onlineCount }}</div>
          <div class="stat-trend">
            <i class="el-icon-check"></i> 全部正常
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">请求数/分钟</div>
          <div class="stat-value">{{ inferenceStats.requestsPerMin }}</div>
          <div class="stat-trend up">
            <i class="el-icon-top"></i> +12% 较昨日
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均响应时间</div>
          <div class="stat-value">{{ inferenceStats.avgResponseTime }} ms</div>
          <div class="stat-trend down">
            <i class="el-icon-bottom"></i> -5% 较昨日
          </div>
        </div>
      </div>

      <!-- 智能推理助手 -->
      <ai-robot-section title="智能推理助手" subtitle="通过问答形式快速完成推理任务"
        :welcome-message="{ role: 'assistant', content: '您好！我是智能推理助手，可以帮助您：', features: ['快速创建推理服务', '部署和管理服务', '监控服务性能', '优化推理配置'], question: '请问有什么可以帮助您的？' }"
        :quick-questions="['查看当前模型服务列表', '添加最新版本yolov8模型推理服务', '停止模型推理服务【id或名称】', '清理模型推理服务【id或名称】']"
        placeholder="请输入您的问题，例如：帮我创建一个文本分类推理服务" storage-key="llmops_serving_inference_chat_v1" />
    </div>

    <!-- 查询区域 -->
    <div class="search-section">
      <div class="section-title">推理服务查询</div>
      <el-form :model="searchForm" :inline="true" class="search-form" label-position="top">
        <!-- <el-form-item label="项目组">
          <el-select v-model="searchForm.project" placeholder="全部" clearable style="width: 150px;">
            <el-option label="AI Lab" value="AI Lab"></el-option>
            <el-option label="Data Science" value="Data Science"></el-option>
            <el-option label="Research" value="Research"></el-option>
          </el-select>
        </el-form-item> -->
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="服务名称" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="服务类型">
          <el-select v-model="searchForm.serviceType" placeholder="全部" clearable style="width: 280px;">
            <el-option v-for="item in jobTemplateTypes" :key="item.service_type || item.id" :label="item.service_type || item.name" :value="item.service_type || item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="searchForm.label" placeholder="标签" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="searchForm.modelName" placeholder="模型名称" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="模型版本">
          <el-input v-model="searchForm.modelVersion" placeholder="模型版本" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="模型地址">
          <el-input v-model="searchForm.modelUrl" placeholder="域名" clearable style="width: 280px;"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 280px;">
            <el-option label="运行中" value="online"></el-option>
            <el-option label="已停止" value="offline"></el-option>
            <el-option label="部署中" value="deploying"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="GPU申请">
          <el-select v-model="searchForm.gpu" placeholder="全部" clearable style="width: 280px;">
            <el-option label="是" value="yes"></el-option>
            <el-option label="否" value="no"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div class="search-actions">
        <el-button type="primary" size="small" @click="handleSearch">查询</el-button>
        <el-button size="small" @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 服务列表 -->
    <div class="table-section">
      <div class="section-title">推理服务列表</div>
      <el-table :data="serviceList" border style="width: 100%;" class="inference-table" v-loading="loading">
        <!-- <el-table-column prop="project" label="项目组" width="120"></el-table-column> -->

        <el-table-column prop="serviceType" label="服务类型" min-width="150">
          <template slot-scope="scope">
            <span>{{ getServiceTypeName(scope.row.serviceType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="label" label="标签" min-width="120"></el-table-column>
        <el-table-column prop="modelName" label="模型名称" min-width="130">
          <template slot-scope="scope">
            <div v-html="formatModelUrl(scope.row.modelName)"></div>
          </template>
        </el-table-column>
        <el-table-column prop="modelVersion" label="模型版本" min-width="120"></el-table-column>
        <el-table-column prop="host" label="域名" min-width="200">
          <template slot-scope="scope">
            <div v-html="scope.row.host"></div>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP&端口" min-width="170">
          <template slot-scope="scope">
            <div v-html="scope.row.ip"></div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template slot-scope="scope">
            <span class="status-tag" :class="getStatusClass(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="资源" min-width="180">
          <template slot-scope="scope">
            <span>cpu:{{ scope.row.resourceCpu || 0 }} ;memory:{{ scope.row.resourceMemory || 0 }} ;gpu:{{
              scope.row.resourceGpu ||
              0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="replicas_html" label="副本数" min-width="100">
          <template slot-scope="scope">
            <span>{{ scope.row.minReplicas || 0 }} ~ {{ scope.row.maxReplicas || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="creator" label="创建者" min-width="100"></el-table-column>
        <el-table-column prop="changedOn" label="修改时间" min-width="180"></el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="handleDebug(scope.row)">调试</el-button>
            <el-button type="text" size="mini" @click="handleDeploy(scope.row)">部署</el-button>
            <el-button type="text" size="mini" @click="handleMonitor(scope.row)">监控</el-button>
            <el-button type="text" size="mini" @click="handleClean(scope.row)">清理</el-button>
            <el-button type="text" size="mini" @click="showDetail(scope.row)">详情</el-button>
            <el-button type="text" size="mini" @click="showEditDialog(scope.row)">修改</el-button>
            <el-button type="text" size="mini" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <div class="pagination-left">
          显示 {{ totalCount ? (currentPage - 1) * pageSize + 1 : 0 }} - {{ Math.min(currentPage * pageSize, totalCount)
          }}
          条，共 {{ totalCount }} 条
        </div>
        <div class="pagination-right">
          <el-select v-model="pageSize" size="small" style="width: 100px; margin-right: 10px;"
            @change="handlePageSizeChange">
            <el-option :label="size + '条/页'" :value="size" v-for="size in [10, 20, 50, 100]" :key="size"></el-option>
          </el-select>
          <el-pagination :current-page="currentPage" :page-size="pageSize" layout="prev, pager, next, jumper"
            :total="totalCount" @current-change="handlePageChange" background></el-pagination>
        </div>
      </div>
    </div>

    <!-- 添加推理服务弹框 -->
    <el-dialog title="添加推理服务" :visible.sync="addDialogVisible" width="750px" :close-on-click-modal="false"
      class="add-inference-dialog">
      <!-- 步骤指示器 -->
      <div class="steps-wrapper">
        <el-steps :active="currentStep" finish-status="success" simple>
          <el-step title="模型配置"></el-step>
          <el-step title="推理配置"></el-step>
          <el-step title="管理员配置"></el-step>
        </el-steps>
      </div>

      <!-- 步骤1：模型配置 -->
      <div v-show="currentStep === 0" class="step-content">
        <div class="step-title">模型配置</div>
        <el-form :model="step1Form" :rules="rules" ref="step1Form" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="服务类型" prop="serviceType">
                <el-select v-model="step1Form.serviceType" placeholder="请选择" style="width: 100%;" @change="handleServiceTypeChange">
                  <el-option v-for="item in jobTemplateTypes" :key="item.service_type || item.id" :label="item.service_type || item.name" :value="item.service_type || item.id"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="项目组">
                <el-select v-model="step1Form.project" placeholder="请选择" style="width: 100%;" disabled>
                  <el-option label="AI Lab" value="AI Lab"></el-option>
                  <el-option label="Data Science" value="Data Science"></el-option>
                  <el-option label="Research" value="Research"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="标签">
                <el-input v-model="step1Form.label" placeholder="标签，逗号分隔"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="模型名称" prop="modelName">
                <el-input v-model="step1Form.modelName" placeholder="模型名称"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="模型版本">
                <el-input v-model="step1Form.model_version" placeholder="模型版本"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="镜像">
                <el-select v-model="step1Form.image" placeholder="请选择或输入镜像地址" filterable allow-create style="width: 100%;">
                  <el-option v-for="img in currentServiceTypeImages" :key="img.id" :label="img.value" :value="img.value"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="模型地址">
                <el-input v-model="step1Form.modelUrl" placeholder="模型地址"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 步骤2：推理配置 -->
      <div v-show="currentStep === 1" class="step-content">
        <div class="step-title">推理配置</div>
        <el-form :model="step2Form" :rules="rules" ref="step2Form" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="内存申请" prop="memory">
                <el-input v-model="step2Form.memory" placeholder="例如: 5G"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="CPU申请" prop="cpu">
                <el-input v-model="step2Form.cpu" placeholder="例如: 4"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="GPU申请">
                <el-input v-model="step2Form.gpu" placeholder="例如: 1"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最小副本数">
                <el-input v-model="step2Form.minReplicas" placeholder="最小副本数"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最大副本数">
                <el-input v-model="step2Form.maxReplicas" placeholder="最大副本数"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="优先级">
                <el-select v-model="step2Form.priority" placeholder="高" style="width: 100%;">
                  <el-option label="高" value="high"></el-option>
                  <el-option label="中" value="medium"></el-option>
                  <el-option label="低" value="low"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="域名">
                <el-input v-model="step2Form.host" placeholder="域名"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="挂载目录">
                <el-input v-model="step2Form.mountPath" placeholder="挂载目录"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="Sidecar">
                <el-select v-model="step2Form.sidecar" placeholder="无" style="width: 100%;">
                  <el-option label="无" value="none"></el-option>
                  <el-option label="Prometheus" value="prometheus"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item>
                <el-checkbox v-model="step2Form.autoScaling">弹性扩缩容</el-checkbox>
                <el-checkbox v-model="step2Form.trafficSplit" style="margin-left: 20px;">分流</el-checkbox>
                <el-checkbox v-model="step2Form.trafficCopy" style="margin-left: 20px;">流量复制</el-checkbox>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 步骤3：管理员配置 -->
      <div v-show="currentStep === 2" class="step-content">
        <div class="step-title">管理员配置</div>
        <el-form :model="step3Form" :rules="rules" ref="step3Form" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="推理配置">
                <el-input v-model="step3Form.inferenceConfig" type="textarea" :rows="4"
                  placeholder="会配置文件的形式挂载到容器/config目录下，留空时将被自动重置，格式：---文件名，多行文件内容，---文件名 多行文件内容"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="启动目录">
                <el-input v-model="step3Form.startupDir" placeholder="启动目录"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="启动命令">
                <el-input v-model="step3Form.startupCmd" placeholder="启动命令"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="环境变量">
                <el-input v-model="step3Form.envVars" placeholder="环境变量，逗号分隔"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="端口">
                <el-input v-model="step3Form.port" placeholder="端口"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="指标">
                <el-input v-model="step3Form.metrics" placeholder="指标"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="监控检查">
                <el-input v-model="step3Form.healthCheck" placeholder="监控检查地址"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 弹框底部 -->
      <span slot="footer" class="dialog-footer">
        <div class="footer-left">
          <el-button v-if="currentStep === 0" disabled>上一步</el-button>
          <el-button v-else @click="prevStep">上一步</el-button>
          <el-button v-if="currentStep !== 2" type="primary" @click="nextStep">下一步</el-button>
        </div>
        <div class="footer-right">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button v-if="currentStep === 2" type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </span>
    </el-dialog>

    <!-- 修改推理服务弹框 -->
    <el-dialog title="修改推理服务" :visible.sync="editDialogVisible" width="750px" :close-on-click-modal="false"
      class="edit-inference-dialog">
      <div class="steps-wrapper">
        <el-steps :active="editCurrentStep" finish-status="success" simple>
          <el-step title="模型配置"></el-step>
          <el-step title="推理配置"></el-step>
          <el-step title="管理员配置"></el-step>
        </el-steps>
      </div>

      <div v-show="editCurrentStep === 0" class="step-content">
        <div class="step-title">模型配置</div>
        <el-form :model="editStep1Form" :rules="rules" ref="editStep1Form" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="服务类型" prop="serviceType">
                <el-select v-model="editStep1Form.serviceType" placeholder="请选择" style="width: 100%;" @change="handleEditServiceTypeChange">
                  <el-option v-for="item in jobTemplateTypes" :key="item.service_type || item.id" :label="item.service_type || item.name" :value="item.service_type || item.id"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="项目组">
                <el-select v-model="editStep1Form.project" placeholder="请选择" style="width: 100%;" disabled>
                  <el-option label="AI Lab" value="AI Lab"></el-option>
                  <el-option label="Data Science" value="Data Science"></el-option>
                  <el-option label="Research" value="Research"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="标签">
                <el-input v-model="editStep1Form.label" placeholder="标签，逗号分隔"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="模型名称" prop="modelName">
                <el-input v-model="editStep1Form.modelName" placeholder="模型名称"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="模型版本">
                <el-input v-model="editStep1Form.modelVersion" placeholder="模型版本"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="镜像">
                <el-select v-model="editStep1Form.image" placeholder="请选择或输入镜像地址" filterable allow-create style="width: 100%;">
                  <el-option v-for="img in editServiceTypeImages" :key="img.id" :label="img.value" :value="img.value"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="模型地址">
                <el-input v-model="editStep1Form.modelUrl" placeholder="模型地址"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <div v-show="editCurrentStep === 1" class="step-content">
        <div class="step-title">推理配置</div>
        <el-form :model="editStep2Form" :rules="rules" ref="editStep2Form" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="内存申请" prop="memory">
                <el-input v-model="editStep2Form.memory" placeholder="例如: 5G"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="CPU申请" prop="cpu">
                <el-input v-model="editStep2Form.cpu" placeholder="例如: 4"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="GPU申请">
                <el-input v-model="editStep2Form.gpu" placeholder="例如: 1"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最小副本数">
                <el-input v-model="editStep2Form.minReplicas" placeholder="最小副本数"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最大副本数">
                <el-input v-model="editStep2Form.maxReplicas" placeholder="最大副本数"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="优先级">
                <el-select v-model="editStep2Form.priority" placeholder="高" style="width: 100%;">
                  <el-option label="高" value="high"></el-option>
                  <el-option label="中" value="medium"></el-option>
                  <el-option label="低" value="low"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="域名">
                <el-input v-model="editStep2Form.host" placeholder="域名"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="挂载目录">
                <el-input v-model="editStep2Form.mountPath" placeholder="挂载目录"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="Sidecar">
                <el-select v-model="editStep2Form.sidecar" placeholder="无" style="width: 100%;">
                  <el-option label="无" value="none"></el-option>
                  <el-option label="istio (服务网格)" value="istio"></el-option>
                  <el-option label="Prometheus" value="prometheus"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item>
                <el-checkbox v-model="editStep2Form.autoScaling">弹性扩缩容</el-checkbox>
                <el-checkbox v-model="editStep2Form.trafficSplit" style="margin-left: 20px;">分流</el-checkbox>
                <el-checkbox v-model="editStep2Form.trafficCopy" style="margin-left: 20px;">流量复制</el-checkbox>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <div v-show="editCurrentStep === 2" class="step-content">
        <div class="step-title">管理员配置</div>
        <el-form :model="editStep3Form" :rules="rules" ref="editStep3Form" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="推理配置">
                <el-input v-model="editStep3Form.inferenceConfig" type="textarea" :rows="4"
                  placeholder="会配置文件的形式挂载到容器/config目录下，留空时将被自动重置，格式：---文件名，多行文件内容，---文件名 多行文件内容"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="启动目录">
                <el-input v-model="editStep3Form.startupDir" placeholder="启动目录"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="启动命令">
                <el-input v-model="editStep3Form.startupCmd" placeholder="启动命令"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="环境变量">
                <el-input v-model="editStep3Form.envVars" placeholder="环境变量，逗号分隔"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="端口">
                <el-input v-model="editStep3Form.port" placeholder="端口"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="指标">
                <el-input v-model="editStep3Form.metrics" placeholder="指标"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="监控检查">
                <el-input v-model="editStep3Form.healthCheck" placeholder="监控检查地址"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <div class="footer-left">
          <el-button v-if="editCurrentStep === 0" disabled>上一步</el-button>
          <el-button v-else @click="editPrevStep">上一步</el-button>
          <el-button v-if="editCurrentStep !== 2" type="primary" @click="editNextStep">下一步</el-button>
        </div>
        <div class="footer-right">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button v-if="editCurrentStep === 2" type="primary" @click="handleEditSubmit">确定</el-button>
        </div>
      </span>
    </el-dialog>

    <!-- 推理服务详情弹窗 -->
    <el-dialog title="推理服务 详情" :visible.sync="detailDialogVisible" width="700px" class="detail-dialog">
      <div class="detail-content">
        <div class="detail-item">
          <label>服务类型：</label>
          <span>{{ getServiceTypeName(currentDetail.serviceType) }}</span>
        </div>
        <div class="detail-item">
          <label>项目组：</label>
          <span>{{ currentDetail.project || currentDetail.namespace || '--' }}</span>
        </div>
        <div class="detail-item">
          <label>名称：</label>
          <span>{{ currentDetail.name || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>标签：</label>
          <span>{{ currentDetail.label || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>模型名称：</label>
          <span>{{ currentDetail.modelName || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>模型版本：</label>
          <span>{{ currentDetail.modelVersion || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>镜像：</label>
          <span>{{ currentDetail.images || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>模型地址：</label>
          <span>{{ currentDetail.modelPath || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>挂载目录：</label>
          <span>{{ currentDetail.volumeMount || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>sidecar：</label>
          <span>{{ currentDetail.sidecar || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>启动目录：</label>
          <span>{{ currentDetail.workingDir || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>启动命令：</label>
          <span>{{ currentDetail.command || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>环境变量：</label>
          <span>{{ currentDetail.env || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>内存申请：</label>
          <span>{{ currentDetail.resourceMemory || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>cpu申请：</label>
          <span>{{ currentDetail.resourceCpu || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>gpu申请：</label>
          <span>{{ currentDetail.resourceGpu || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>最小副本数：</label>
          <span>{{ currentDetail.minReplicas || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>最大副本数：</label>
          <span>{{ currentDetail.maxReplicas || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>端口：</label>
          <span>{{ currentDetail.ports || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>域名：</label>
          <span style="color: #f56c6c;">{{ currentDetail.host || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>弹性伸缩容：</label>
          <span>{{ currentDetail.hpa === 'true' ? '开启' : '关闭' }}</span>
        </div>
        <div class="detail-item">
          <label>优先级：</label>
          <span>{{ currentDetail.priority || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>分流：</label>
          <span>{{ currentDetail.canary === 'true' ? '开启' : '关闭' }}</span>
        </div>
        <div class="detail-item">
          <label>流量复制：</label>
          <span>{{ currentDetail.shadow === 'true' ? '开启' : '关闭' }}</span>
        </div>
        <div class="detail-item">
          <label>健康检查：</label>
          <span>{{ currentDetail.health || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <span>{{ currentDetail.status || 'online' }}</span>
        </div>
        <div class="detail-item">
          <label>扩展：</label>
          <span>{{ currentDetail.expand || '{}' }}</span>
        </div>
        <div class="detail-item">
          <label>指标：</label>
          <span>{{ currentDetail.metrics || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>部署记录：</label>
          <span>{{ currentDetail.deployHistory || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>域名：</label>
          <span>{{ currentDetail.host || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>推理配置：</label>
          <span>{{ currentDetail.inferenceConfig || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>修改时间：</label>
          <span>{{ currentDetail.changedOn || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>创建时间：</label>
          <span>{{ currentDetail.createdOn || '-' }}</span>
        </div>
        <div class="detail-item">
          <label>id：</label>
          <span>{{ currentDetail.id || '-' }}</span>
        </div>
      </div>
      <span slot="footer" style="justify-content: end">
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getInferenceList, getInferenceDetail, createInference, updateInference, deleteInference, deployInference, cleanInference, debugInference, getInferenceStats, getInferenceServiceTypes } from '@/api/llmops/inference'
import { getJobTemplateType } from '@/api/llmops/project'
import { mapState } from 'vuex'
import * as k8s_api from '@/api/app/k8s'
import AiRobotSection from '@/components/llmops/AiRobotSection.vue'

export default {
  name: 'Inference',
  components: { AiRobotSection },
  computed: {
    ...mapState('project', ['currentProject'])
  },
  data() {
    return {
      loading: false,
      totalCount: 0,
      addDialogVisible: false,
      editDialogVisible: false,
      detailDialogVisible: false,
      currentDetail: {},
      editingInferenceId: null,
      editCurrentStep: 0,
      rules: {
        serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
        modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
        memory: [{ required: true, message: '请输入内存申请', trigger: 'blur' }],
        cpu: [{ required: true, message: '请输入CPU申请', trigger: 'blur' }]
      },
      editStep1Form: {
        serviceType: '',
        project: '',
        label: '',
        modelName: '',
        modelVersion: '',
        image: '',
        modelUrl: ''
      },
      editStep2Form: {
        memory: '',
        cpu: '',
        gpu: '',
        minReplicas: '',
        maxReplicas: '',
        priority: '',
        host: '',
        mountPath: '',
        sidecar: 'none',
        autoScaling: false,
        trafficSplit: false,
        trafficCopy: false
      },
      editStep3Form: {
        inferenceConfig: '',
        startupDir: '',
        startupCmd: '',
        envVars: '',
        port: '',
        metrics: '',
        healthCheck: ''
      },
      currentStep: 0,
      step1Form: {
        serviceType: '',
        project: '',
        label: '',
        modelName: '',
        modelVersion: '',
        image: '',
        modelUrl: ''
      },
      step2Form: {
        memory: '',
        cpu: '',
        gpu: '',
        minReplicas: '',
        maxReplicas: '',
        priority: '',
        host: '',
        mountPath: '',
        sidecar: 'none',
        autoScaling: false,
        trafficSplit: false,
        trafficCopy: false
      },
      step3Form: {
        inferenceConfig: '',
        startupDir: '',
        startupCmd: '',
        envVars: '',
        port: '',
        metrics: '',
        healthCheck: ''
      },
      searchForm: {
        project: '',
        name: '',
        label: '',
        modelName: '',
        model_version: '',
        modelUrl: '',
        status: '',
        gpu: '',
        serviceType: ''
      },
      currentPage: 1,
      pageSize: 10,
      serviceList: [],
      jobTemplateTypes: [],
      currentServiceTypeImages: [],
      editServiceTypeImages: [],
      inferenceStats: {
        totalCount: 0,
        onlineCount: 0,
        thisMonthCount: 0,
        requestsPerMin: 0,
        avgResponseTime: 0
      }
    }
  },
  mounted() {
    this.loadInferenceList()
    this.loadServiceTypeOptions()
    this.loadInferenceStats()
  },
  methods: {
    async loadInferenceList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        if (this.searchForm.project) params.project = this.searchForm.project
        if (this.searchForm.name) params.name = this.searchForm.name
        if (this.searchForm.label) params.label = this.searchForm.label
        if (this.searchForm.modelName) params.modelName = this.searchForm.modelName
        if (this.searchForm.modelVersion) params.modelVersion = this.searchForm.modelVersion
        if (this.searchForm.modelUrl) params.modelUrl = this.searchForm.modelUrl
        if (this.searchForm.status) params.status = this.searchForm.status
        if (this.searchForm.gpu) params.gpu = this.searchForm.gpu
        if (this.searchForm.serviceType) params.serviceType = this.searchForm.serviceType

        const res = await getInferenceList(params)
        if (res && res.resultData) {
          this.serviceList = res.resultData || []
          this.totalCount = res.totalRecord || 0
        } else {
          this.serviceList = []
          this.totalCount = 0
        }
      } catch (error) {
        console.error('加载推理服务列表失败', error)
        this.$message.error('加载推理服务列表失败')
        this.serviceList = []
        this.totalCount = 0
      } finally {
        this.loading = false
      }
    },
    getServiceTypeName(typeId) {
      if (!typeId) return '--'
      const type = this.jobTemplateTypes.find(item => (item.service_type || item.id) == typeId)
      return type ? (type.service_type || type.name) : typeId
    },
    async loadServiceTypeOptions() {
      try {
        const res = await getInferenceServiceTypes()
        if (res && res.result) {
          this.jobTemplateTypes = res.result || []
        }
      } catch (error) {
        console.error('获取任务模板类型失败', error)
      }
    },
    handleServiceTypeChange(val) {
      const type = this.jobTemplateTypes.find(item => item.service_type === val)
      if (type) {
        this.currentServiceTypeImages = type.images || []
        this.step1Form.image = this.currentServiceTypeImages.length > 0 ? this.currentServiceTypeImages[0].value : ''
        this.step2Form.host = type.host || ''
        this.step3Form.inferenceConfig = type.configmap || ''
        this.step3Form.startupCmd = type.command || ''
        this.step3Form.envVars = (type.env || []).join(',')
        this.step3Form.port = type.ports || ''
        this.step3Form.metrics = type.metrics || ''
        this.step3Form.healthCheck = type.health || ''
      }
    },
    handleEditServiceTypeChange(val) {
      const type = this.jobTemplateTypes.find(item => item.service_type === val)
      if (type) {
        this.editServiceTypeImages = type.images || []
        this.editStep1Form.image = this.editServiceTypeImages.length > 0 ? this.editServiceTypeImages[0].value : ''
        this.editStep2Form.host = type.host || ''
        this.editStep3Form.inferenceConfig = type.configmap || ''
        this.editStep3Form.startupCmd = type.command || ''
        this.editStep3Form.envVars = (type.env || []).join(',')
        this.editStep3Form.port = type.ports || ''
        this.editStep3Form.metrics = type.metrics || ''
        this.editStep3Form.healthCheck = type.health || ''
      }
    },
    async loadInferenceStats() {
      try {
        const res = await getInferenceStats()
        if (res) {
          this.inferenceStats = {
            totalCount: res.totalCount || 0,
            onlineCount: res.onlineCount || 0,
            thisMonthCount: res.thisMonthCount || 0,
            requestsPerMin: res.requestsPerMin || 0,
            avgResponseTime: res.avgResponseTime || 0
          }
        }
      } catch (error) {
        console.error('获取推理服务统计数据失败', error)
      }
    },
    formatModelUrl(urlHtml) {
      if (!urlHtml) return '--';
      const baseUrl = 'http://192.168.224.251:747';
      // 使用正则替换 href 中的相对路径，拼接上 IP 前缀
      return urlHtml.replace(/href=(['"])(.*?)\1/g, (match, quote, path) => {
        if (path.startsWith('http')) return match;
        return `href=${quote}${baseUrl}${path}${quote}`;
      });
    },
    handleSearch() {
      this.currentPage = 1
      this.loadInferenceList()
    },
    handleReset() {
      this.searchForm = {
        project: '',
        name: '',
        label: '',
        modelName: '',
        modelVersion: '',
        modelUrl: '',
        status: '',
        gpu: '',
        serviceType: ''
      }
      this.currentPage = 1
      this.loadInferenceList()
    },
    async showDetail(row) {
      this.currentDetail = row;
      this.detailDialogVisible = true;
      try {
        const res = await getInferenceDetail(row.id);
        if (res) {
          this.currentDetail = res;
        }
      } catch (error) {
        console.error('获取详情失败', error);
      }
    },
    async showEditDialog(row) {
      this.editingInferenceId = row.id
      this.editDialogVisible = true
      this.editCurrentStep = 0

      let detail = row
      try {
        const res = await getInferenceDetail(row.id)
        if (res) {
          detail = res
        }
      } catch (error) {
        console.error('获取详情失败，使用列表数据', error)
      }

      // 优先级反向映射
      const reversePriorityMap = {
        2: 'high',
        1: 'medium',
        0: 'low'
      }

      const serviceType = detail.serviceType || '--'
      const type = this.jobTemplateTypes.find(item => item.service_type === serviceType)
      if (type) {
        this.editServiceTypeImages = type.images || []
      } else {
        this.editServiceTypeImages = []
      }

      this.editStep1Form = {
        serviceType: serviceType,
        project: this.currentProject ? this.currentProject.name : (detail.project || ''),
        label: detail.label || '',
        modelName: detail.modelName || '',
        modelVersion: detail.modelVersion || '',
        image: detail.images || '',
        modelUrl: detail.modelPath || ''
      }
      this.editStep2Form = {
        memory: detail.resourceMemory || '',
        cpu: detail.resourceCpu || '',
        gpu: detail.resourceGpu || '',
        minReplicas: detail.minReplicas || '',
        maxReplicas: detail.maxReplicas || '',
        priority: reversePriorityMap[detail.priority] || 'medium',
        host: detail.host || '',
        mountPath: detail.volumeMount || '',
        sidecar: detail.sidecar || 'none',
        autoScaling: detail.hpa === 'true',
        trafficSplit: detail.canary === 'true',
        trafficCopy: detail.shadow === 'true'
      }
      this.editStep3Form = {
        inferenceConfig: detail.inferenceConfig || '',
        startupDir: detail.workingDir || '',
        startupCmd: detail.command || '',
        envVars: detail.env || '',
        port: detail.ports || '',
        metrics: detail.metrics || '',
        healthCheck: detail.health || ''
      }
    },
    resetEditForms() {
      this.editServiceTypeImages = []
      this.editStep1Form = {
        serviceType: '',
        project: '',
        label: '',
        modelName: '',
        modelVersion: '',
        image: '',
        modelUrl: ''
      }
      this.editStep2Form = {
        memory: '',
        cpu: '',
        gpu: '',
        minReplicas: '',
        maxReplicas: '',
        priority: '',
        host: '',
        mountPath: '',
        sidecar: 'none',
        autoScaling: false,
        trafficSplit: false,
        trafficCopy: false
      }
      this.editStep3Form = {
        inferenceConfig: '',
        startupDir: '',
        startupCmd: '',
        envVars: '',
        port: '',
        metrics: '',
        healthCheck: ''
      }
    },
    async editNextStep() {
      const currentFormRef = `editStep${this.editCurrentStep + 1}Form`
      const isValid = await this.$refs[currentFormRef].validate().catch(() => false)
      if (isValid) {
        this.editCurrentStep++
      }
    },
    editPrevStep() {
      this.editCurrentStep--
    },
    buildInferencePayload(step1, step2, step3) {
      // 内存申请必须后面加G
      let memory = step2.memory || ''
      if (memory && !/^[0-9]+G$/.test(memory)) {
        if (/^[0-9]+$/.test(memory)) {
          memory += 'G'
        }
      }

      // 优先级映射
      const priorityMap = {
        'high': 2,
        'medium': 1,
        'low': 0
      }

      return {
        project: this.currentProject ? this.currentProject.id : 0,
        label: step1.label || '',
        images: step1.image || '',
        hpa: step2.autoScaling ? 'true' : 'false',
        priority: priorityMap[step2.priority] || 0,
        canary: step2.trafficSplit ? 'true' : 'false',
        shadow: step2.trafficCopy ? 'true' : 'false',
        host: step2.host || '',
        sidecar: step2.sidecar || 'none',
        command: step3.startupCmd || '',
        env: step3.envVars || '',
        ports: step3.port || '',
        metrics: step3.metrics || '',
        health: step3.healthCheck || '',
        service_type: step1.serviceType || '',
        model_name: step1.modelName || '',
        model_version: step1.modelVersion || '',
        model_path: step1.modelUrl || '',
        resource_memory: memory,
        resource_cpu: step2.cpu || '',
        resource_gpu: step2.gpu || '',
        min_replicas: Number(step2.minReplicas) || 0,
        max_replicas: Number(step2.maxReplicas) || 0,
        inference_config: step3.inferenceConfig || '',
        working_dir: step3.startupDir || ''
      }
    },
    async handleEditSubmit() {
      try {
        if (!this.editingInferenceId) {
          this.$message.warning('当前记录缺少ID，无法修改')
          return
        }

        // 校验所有步骤的表单
        const form1Valid = await this.$refs.editStep1Form.validate().catch(() => false)
        if (!form1Valid) {
          this.editCurrentStep = 0
          return
        }
        const form2Valid = await this.$refs.editStep2Form.validate().catch(() => false)
        if (!form2Valid) {
          this.editCurrentStep = 1
          return
        }
        const form3Valid = await this.$refs.editStep3Form.validate().catch(() => false)
        if (!form3Valid) {
          this.editCurrentStep = 2
          return
        }

        const submitData = {
          id: this.editingInferenceId,
          ...this.buildInferencePayload(this.editStep1Form, this.editStep2Form, this.editStep3Form)
        }
        const res = await updateInference(submitData)
        if (res && res.status === 0) {
          this.$message.success('修改推理服务成功！')
          this.editDialogVisible = false
          await this.loadInferenceList()
        } else {
          this.$message.error(res.message || '修改推理服务失败')
        }
      } catch (error) {
        console.error('修改推理服务失败', error)
        this.$message.error('修改推理服务失败')
      }
    },
    showAddDialog() {
      this.addDialogVisible = true
      this.currentStep = 0
      this.resetForms()
      if (this.currentProject) {
        this.step1Form.project = this.currentProject.name || ''
      }
    },
    resetForms() {
      this.currentServiceTypeImages = []
      this.step1Form = {
        serviceType: '',
        project: this.currentProject ? (this.currentProject.name || '') : '',
        label: '',
        modelName: '',
        modelVersion: '',
        image: '',
        modelUrl: ''
      }
      this.step2Form = {
        memory: '',
        cpu: '',
        gpu: '0',
        minReplicas: '1',
        maxReplicas: '1',
        priority: 'medium',
        host: '',
        mountPath: '',
        sidecar: 'none',
        autoScaling: false,
        trafficSplit: false,
        trafficCopy: false
      }
      this.step3Form = {
        inferenceConfig: '',
        startupDir: '',
        startupCmd: '',
        envVars: '',
        port: '',
        metrics: '',
        healthCheck: ''
      }
    },
    async nextStep() {
      const currentFormRef = `step${this.currentStep + 1}Form`
      const isValid = await this.$refs[currentFormRef].validate().catch(() => false)
      if (isValid) {
        this.currentStep++
      }
    },
    prevStep() {
      this.currentStep--
    },
    async handleSubmit() {
      try {
        // 校验所有步骤的表单
        const form1Valid = await this.$refs.step1Form.validate().catch(() => false)
        if (!form1Valid) {
          this.currentStep = 0
          return
        }
        const form2Valid = await this.$refs.step2Form.validate().catch(() => false)
        if (!form2Valid) {
          this.currentStep = 1
          return
        }
        const form3Valid = await this.$refs.step3Form.validate().catch(() => false)
        if (!form3Valid) {
          this.currentStep = 2
          return
        }

        const submitData = this.buildInferencePayload(this.step1Form, this.step2Form, this.step3Form)
        const res = await createInference(submitData)
        if (res && res.status === 0) {
          this.$message.success('添加推理服务成功！')
          this.addDialogVisible = false
          await this.loadInferenceList()
        } else {
          this.$message.error(res.message || '添加推理服务失败')
        }
      } catch (error) {
        console.error('添加推理服务失败', error)
        this.$message.error('添加推理服务失败')
      }
    },
    async handleDelete(row) {
      if (!row || !row.id) {
        this.$message.warning('当前记录缺少ID，无法删除')
        return
      }
      this.$confirm(`确定要删除推理服务 "${row.name || row.modelName || row.id}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteInference(row.id)
          if (!res || res.status === 0) {
            this.$message.success('删除成功')
            await this.loadInferenceList()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        } catch (error) {
          console.error('删除推理服务失败', error)
          this.$message.error('删除推理服务失败')
        }
      }).catch(() => { })
    },
    handleMonitor(row) {
      this.$router.push({
        path: '/llmops/serving/monitor',
        query: {
          id: row.id || '',
        }
      });
    },
    async handleDebug(row) {
      if (!row || !row.id) {
        this.$message.warning('当前记录缺少ID，无法调试');
        return;
      }
      if (!row.name) {
        this.$message.warning('推理服务名称缺失，无法进行调试');
        return;
      }

      const loading = this.$loading({
        target: 'body',
        text: '正在获取调试信息...',
        background: 'rgba(0, 0, 0, 0.3)'
      });

      try {
        const res = await debugInference(row.id);
        if (res && res.status !== 0) {
          this.$message.error(res.message || '调试启动失败');
          loading.close();
          return;
        }

        const namespace = row.namespace || row.project || (this.currentProject && this.currentProject.name) || 'public';
        const clusterName = row.clusterName || this.$store.state.project.currentCluster.name;

        const podRes = await k8s_api.getContainerGroup(namespace, clusterName, `app=debug-${row.name},pod-type=inference`);
        const items = (podRes && podRes.items) || [];
        const runningPods = items.filter(i => i.status && i.status.phase === 'Running');
        const pod = runningPods[0] || items[0];

        loading.close();

        if (!pod) {
          this.$message.warning('调试容器创建中，请稍后重试');
          return;
        }

        const { href } = this.$router.resolve({
          path: `/webssh/console/${namespace}/${pod.metadata.name}`,
          query: {
            proName: namespace,
            clusterName: clusterName
          }
        });
        window.open(href, '_blank');
      } catch (error) {
        loading.close();
        console.error('调试推理服务失败', error);
        this.$message.error('调试失败');
      }
    },
    async handleDeploy(row) {
      if (!row || !row.id) {
        this.$message.warning('当前记录缺少ID，无法部署')
        return
      }
      try {
        const res = await deployInference(row.id)
        if (res && res.status === 0) {
          this.$message.success('部署指令已发送')
          await this.loadInferenceList()
        } else {
          this.$message.error(res.message || '部署失败')
        }
      } catch (error) {
        console.error('部署推理服务失败', error)
        this.$message.error('部署失败')
      }
    },
    async handleClean(row) {
      if (!row || !row.id) {
        this.$message.warning('当前记录缺少ID，无法清理')
        return
      }
      this.$confirm(`确定要清理推理服务 "${row.name || row.modelName || row.id}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await cleanInference(row.id)
          if (res && res.status === 0) {
            this.$message.success('清理成功')
            await this.loadInferenceList()
          } else {
            this.$message.error(res.message || '清理失败')
          }
        } catch (error) {
          console.error('清理推理服务失败', error)
          this.$message.error('清理失败')
        }
      }).catch(() => { })
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadInferenceList()
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadInferenceList()
    },
    getStatusText(status) {
      const map = {
        online: '运行中',
        offline: '已停止',
        deploying: '部署中'
      }
      return map[status] || (status || '-')
    },
    getStatusClass(status) {
      const map = {
        online: 'status-running',
        offline: 'status-stopped',
        deploying: 'status-deploying'
      }
      return map[status] || 'status-stopped'
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-dialog {
  .detail-content {
    // padding: 10px 20px;
    max-height: 500px;
    overflow-y: auto;
  }

  .detail-item {
    display: flex;
    margin-bottom: 12px;
    line-height: 1.6;
    // border-bottom: 1px solid #f0f2f5;
    padding-bottom: 8px;

    label {
      width: 140px;
      color: #606266;
      font-weight: 600;
      flex-shrink: 0;
    }

    span {
      color: #303133;
      word-break: break-all;
    }

    &:last-child {
      border-bottom: none;
    }
  }
}

.status-dot-online {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #10b981;
}

.status-text-online {
  font-size: 12px;
  color: #059669;
  font-weight: 500;
}

.add-inference-dialog,
.edit-inference-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px 24px 0;
    border-bottom: 1px solid #e5e7eb;
    margin-right: 0;
    padding-bottom: 16px;
  }

  ::v-deep .el-dialog__headerbtn {
    top: 24px;
  }

  ::v-deep .el-dialog__body {
    padding: 20px 30px;
  }

  ::v-deep .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e5e7eb;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.steps-wrapper {
  margin-bottom: 24px;
  padding-bottom: 16px;
}

.step-content {
  .step-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 20px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;

  .footer-left {
    display: flex;
    gap: 8px;
  }

  .footer-right {
    display: flex;
    gap: 8px;
  }
}

.inference-container {
  min-height: calc(100vh - 60px);
}

.inference-content {
  // max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
}

::v-deep .el-button--primary:not(.is-text) {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;

  &:hover,
  &:focus {
    opacity: 0.9;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-trend.up {
  color: #10b981;
}

.stat-trend.down {
  color: #ef4444;
}

.search-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 12px;
}

.search-form .el-form-item {
  margin-bottom: 12px;
}

.search-form .el-form-item__label {
  font-size: 13px;
  color: #374151;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
}

.table-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.inference-table {
  font-size: 13px;
}

.table-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #1f2937;
}

.status-tag {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-running {
  background-color: #dcfce7;
  color: #16a34a;
}

.status-stopped {
  background-color: #fee2e2;
  color: #dc2626;
}

.status-deploying {
  background-color: #fef3c7;
  color: #d97706;
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.pagination-left {
  font-size: 13px;
  color: #6b7280;
}

.pagination-right {
  display: flex;
  align-items: center;
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .search-form .el-form-item {
    width: 100%;
  }

  .pagination-section {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
