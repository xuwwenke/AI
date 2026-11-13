<template>
  <div class="projects-container">
    <div class="projects-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">项目管理</h1>
          <p class="page-subtitle">管理和协作您的机器学习项目</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="create-btn" @click="handleCreateProject">
            <i class="el-icon-plus"></i> 创建项目
          </el-button>
        </div>
      </div>

      <div class="filter-bar">
        <!-- <div class="filter-item">
          <span class="filter-label">所属部门:</span>
          <el-select v-model="filters.department" size="small" placeholder="全部部门" clearable>
            <el-option label="AI Lab" value="ai-lab"></el-option>
            <el-option label="推荐组" value="recommend"></el-option>
            <el-option label="视觉组" value="vision"></el-option>
          </el-select>
        </div> -->
        <div class="filter-item">
          <span class="filter-label">状态:</span>
          <el-select v-model="filters.status" size="small" placeholder="全部状态" clearable>
            <el-option label="进行中" :value="0"></el-option>
            <el-option label="已暂停" :value="1"></el-option>
            <el-option label="已完成" :value="2"></el-option>
          </el-select>
        </div>
        <div class="filter-item search-item">
          <el-input 
            v-model="filters.keyword" 
            size="small" 
            placeholder="搜索项目..."
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
        </div>
        <div class="view-toggle">
          <i 
            class="el-icon-s-grid view-icon" 
            :class="{ active: viewMode === 'grid' }" 
            @click="viewMode = 'grid'"
          ></i>
          <i 
            class="el-icon-s-operation view-icon" 
            :class="{ active: viewMode === 'list' }" 
            @click="viewMode = 'list'"
          ></i>
        </div>
      </div>

      <div class="projects-grid" v-if="viewMode === 'grid'"  v-loading="loading" element-loading-text="加载中...">
        <div 
          v-for="project in filteredProjects" 
          :key="project.id"
          class="project-card card-hover"
          :class="{ 'warning-card': project.isWarning }"
        >
          <div class="project-header">
            <div class="project-title">{{ project.name }}</div>
            <div class="project-department">{{ project.department }}</div>
            <div class="warning-tag" v-if="project.isWarning">配额告警</div>
          </div>
          <div class="project-stats">
            <div class="stat-item">
              <div class="stat-value">{{ project.gpu }}</div>
              <div class="stat-label">GPU</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value">{{ project.userCount }}</div>
              <div class="stat-label">成员</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value">{{ project.experimentCount }}</div>
              <div class="stat-label">实验</div>
            </div>
          </div>
          <div class="project-footer">
            <el-button class="enter-btn" @click="handleViewProject(project)">进入项目</el-button>
            <div class="settings-icon" @click.stop="handleEditProject(project)">
              <i class="el-icon-setting"></i>
            </div>
            <el-button class="upgrade-btn" v-if="project.isWarning" @click="handleUpgrade(project)">申请扩容</el-button>
          </div>
        </div>
        <div class="create-card card-hover" @click="handleCreateProject">
          <div class="create-icon">
            <i class="el-icon-plus"></i>
          </div>
          <div class="create-text">创建新项目</div>
          <div class="create-subtext">开始新的机器学习旅程</div>
        </div>
      </div>

      <div class="projects-list" v-else  v-loading="loading" element-loading-text="加载中...">
        <div class="list-header">
          <div class="list-col">项目名称</div>
          <div class="list-col">所属部门</div>
          <div class="list-col">GPU</div>
          <div class="list-col">成员</div>
          <div class="list-col">实验</div>
          <div class="list-col">操作</div>
        </div>
        <div 
          v-for="project in filteredProjects" 
          :key="project.id"
          class="list-item card-hover"
        >
          <div class="list-col">{{ project.name }}</div>
          <div class="list-col">{{ project.department }}</div>
          <div class="list-col">{{ project.gpu }}</div>
          <div class="list-col">{{ project.userCount }}</div>
          <div class="list-col">{{ project.experimentCount }}</div>
          <div class="list-col">
            <el-button size="mini" type="text" @click="handleViewProject(project)">进入项目</el-button>
            <el-button size="mini" type="text" @click="handleEditProject(project)">设置</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 项目详情弹框 -->
    <el-dialog
      :visible.sync="projectDetailVisible"
      width="800px"
      :close-on-click-modal="false"
      class="project-detail-dialog"
      custom-class="project-detail-dialog"
    >
      <div slot="title" class="dialog-title-custom">
        <div class="project-detail-header">
          <div class="project-detail-title">{{ currentProject.name }}</div>
          <div class="project-detail-subtitle">AI Lab · 创建于 {{ currentProject.changedOn }}</div>
        </div>
      </div>
      <div class="project-detail-content">
        <el-tabs v-model="activeTab" class="project-detail-tabs">
          <el-tab-pane label="概览" name="overview">
            <div class="overview-content" v-loading="detailLoading">
              <div class="resource-cards">
                <div class="resource-card">
                  <div class="resource-label-row">
                    <span class="resource-label">GPU 配额</span>
                    <span class="resource-usage-text">{{ resourceOverview.gpuUsed || 0 }}/{{ resourceOverview.gpuLimit || 0 }} 卡</span>
                  </div>
                  <div class="resource-bar">
                    <div class="resource-fill" :style="{ width: calculatePercent(resourceOverview.gpuUsed, resourceOverview.gpuLimit) + '%' }"></div>
                  </div>
                </div>
                <div class="resource-card">
                  <div class="resource-label-row">
                    <span class="resource-label">CPU 配额</span>
                    <span class="resource-usage-text">{{ resourceOverview.cpuUsed || 0 }}/{{ resourceOverview.cpuLimit || 0 }} 核</span>
                  </div>
                  <div class="resource-bar">
                    <div class="resource-fill" :style="{ width: calculatePercent(resourceOverview.cpuUsed, resourceOverview.cpuLimit) + '%' }"></div>
                  </div>
                </div>
                <div class="resource-card">
                  <div class="resource-label-row">
                    <span class="resource-label">内存配额</span>
                    <span class="resource-usage-text">{{ resourceOverview.memoryUsed || 0 }}/{{ resourceOverview.memoryLimit || 0 }} GB</span>
                  </div>
                  <div class="resource-bar">
                    <div class="resource-fill" :style="{ width: calculatePercent(resourceOverview.memoryUsed, resourceOverview.memoryLimit) + '%' }"></div>
                  </div>
                </div>
                <div class="resource-card">
                  <div class="resource-label-row">
                    <span class="resource-label">存储配额</span>
                    <span class="resource-usage-text">{{ resourceOverview.storageUsed || 0 }}/{{ resourceOverview.storageLimit || 0 }} GB</span>
                  </div>
                  <div class="resource-bar">
                    <div class="resource-fill" :style="{ width: calculatePercent(resourceOverview.storageUsed, resourceOverview.storageLimit) + '%' }"></div>
                  </div>
                </div>
              </div>
              <!-- <div class="recent-activities">
                <div class="activities-title">最近活动</div>
                <div class="activities-list">
                  <div class="activity-item">
                    <div class="activity-dot blue-dot"></div>
                    <div class="activity-content">
                      <div class="activity-text">张三 启动了实验 <span class="activity-link">"ResNet50训练"</span></div>
                      <div class="activity-time">今天 10:23</div>
                    </div>
                  </div>
                  <div class="activity-item">
                    <div class="activity-dot green-dot"></div>
                    <div class="activity-content">
                      <div class="activity-text">李四 上传了数据集 <span class="activity-link">"训练集v2"</span></div>
                      <div class="activity-time">昨天 15:45</div>
                    </div>
                  </div>
                </div>
              </div> -->
            </div>
          </el-tab-pane>
          <el-tab-pane label="成员" name="members">
            <div class="members-content" v-loading="memberLoading">
              <div class="member-management-wrapper">
                <div class="member-sidebar">
                  <div 
                    class="sidebar-item" 
                    :class="{ active: memberCategory === 'admin' }"
                    @click="memberCategory = 'admin'"
                  >管理员</div>
                  <div 
                    class="sidebar-item" 
                    :class="{ active: memberCategory === 'user' }"
                    @click="memberCategory = 'user'"
                  >普通用户</div>
                </div>
                
                <div class="transfer-container">
                  <!-- 未添加列表 -->
                  <div class="transfer-panel">
                    <div class="panel-header">
                      <el-checkbox :indeterminate="selectedUnaddedUsers.length > 0 && selectedUnaddedUsers.length < filteredUnaddedUsers.length" 
                        v-model="checkAllUnadded" @change="handleCheckAllUnadded">未添加</el-checkbox>
                      <span class="count">{{ selectedUnaddedUsers.length }}/{{ filteredUnaddedUsers.length }}</span>
                    </div>
                    <div class="panel-search">
                      <el-input v-model="unaddedSearchKeyword" size="mini" placeholder="请输入搜索关键字" prefix-icon="el-icon-search" clearable></el-input>
                    </div>
                    <div class="panel-list">
                      <el-checkbox-group v-model="selectedUnaddedUsers">
                        <div v-for="user in filteredUnaddedUsers" :key="user.id" class="list-item-checkbox">
                          <el-checkbox :label="user">{{ user.userName }}</el-checkbox>
                        </div>
                      </el-checkbox-group>
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div class="transfer-buttons">
                    <el-button type="primary" size="mini" :disabled="selectedAddedUsers.length === 0" @click="removeMembers" icon="el-icon-arrow-left">移除</el-button>
                    <el-button type="primary" size="mini" :disabled="selectedUnaddedUsers.length === 0" @click="addMembers">添加<i class="el-icon-arrow-right el-icon--right"></i></el-button>
                  </div>

                  <!-- 已添加列表 -->
                  <div class="transfer-panel">
                    <div class="panel-header">
                      <el-checkbox :indeterminate="selectedAddedUsers.length > 0 && selectedAddedUsers.length < filteredAddedUsers.length" 
                        v-model="checkAllAdded" @change="handleCheckAllAdded">已添加</el-checkbox>
                      <span class="count">{{ selectedAddedUsers.length }}/{{ filteredAddedUsers.length }}</span>
                    </div>
                    <div class="panel-search">
                      <el-input v-model="addedSearchKeyword" size="mini" placeholder="请输入搜索关键字" prefix-icon="el-icon-search" clearable></el-input>
                    </div>
                    <div class="panel-list">
                      <el-checkbox-group v-model="selectedAddedUsers">
                        <div v-for="member in filteredAddedUsers" :key="member.id" class="list-item-checkbox">
                          <el-checkbox :label="member">{{ member.userName }}</el-checkbox>
                        </div>
                      </el-checkbox-group>
                    </div>
                  </div>
                </div>
              </div>
              <div class="member-footer">
                <el-button size="small" @click="projectDetailVisible = false">取消</el-button>
                <el-button size="small" type="primary" @click="handleSaveMembers" :loading="memberLoading">确定</el-button>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="资源" name="resources">
            <div class="resources-content">
                <el-table :data="resourceData" border stripe v-loading="resourceLoading" style="width: 100%" class="resource-table">
                  <el-table-column prop="quotaName" label="名称" align="center" width="150"></el-table-column>
                  <el-table-column label="CPU (核)" align="center">
                    <el-table-column prop="cpuUsed" label="已使用" align="center"></el-table-column>
                    <el-table-column prop="cpuAssigned" label="已分配" align="center"></el-table-column>
                    <el-table-column prop="cpuLimit" label="总配额" align="center"></el-table-column>
                  </el-table-column>
                  <el-table-column label="内存 (Gi)" align="center">
                    <el-table-column prop="memoryUsed" label="已使用" align="center"></el-table-column>
                    <el-table-column prop="memoryAssigned" label="已分配" align="center"></el-table-column>
                    <el-table-column prop="memoryLimit" label="总配额" align="center"></el-table-column>
                  </el-table-column>
                  <el-table-column label="存储 (Gi)" align="center">
                    <el-table-column prop="storageUsed" label="已使用" align="center"></el-table-column>
                    <el-table-column prop="storageAssigned" label="已分配" align="center"></el-table-column>
                    <el-table-column prop="storageLimit" label="总配额" align="center"></el-table-column>
                  </el-table-column>
                  <el-table-column label="GPU (卡)" align="center">
                    <el-table-column prop="gpuUsed" label="已使用" align="center"></el-table-column>
                    <el-table-column prop="gpuAssigned" label="已分配" align="center"></el-table-column>
                    <el-table-column prop="gpuLimit" label="总配额" align="center"></el-table-column>
                  </el-table-column>
                </el-table>
            </div>
          </el-tab-pane>
          <el-tab-pane label="实验" name="experiments">
            <div class="experiments-content">
               
                <el-table :data="experimentList" border stripe v-loading="experimentLoading" style="width: 100%" class="resource-table">
                  <el-table-column prop="name" label="实验名称" align="center"></el-table-column>
                  <el-table-column prop="modelName" label="模型" align="center"></el-table-column>
                  <el-table-column prop="status" label="状态" align="center">
                    <template slot-scope="scope">
                       <el-tag :type="getStatusTagType(scope.row.status)">{{ statusMap[scope.row.status] || scope.row.status || '-' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="startTime" label="开始时间" align="center">
                    <template slot-scope="scope">
                      {{ scope.row.startTime || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="endTime" label="结束时间" align="center">
                    <template slot-scope="scope">
                      {{ scope.row.endTime || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="creator" label="创建者" align="center">
                    <template slot-scope="scope">
                      {{ scope.row.creator || '--' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100" align="center">
                    <template slot-scope="scope">
                      <el-button type="text" size="small" @click="handleExperimentDetail(scope.row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="pagination-container">
                  <span class="total-items">显示{{ (experimentPage - 1) * experimentPageSize + 1 }}-{{ Math.min(experimentPage * experimentPageSize, totalExperiments) }}条,共{{ totalExperiments }}条</span>
                  <el-pagination
                    @size-change="handleExperimentSizeChange"
                    @current-change="handleExperimentPageChange"
                    background
                    :current-page="experimentPage"
                    :page-sizes="[10, 20, 50]"
                    :page-size="experimentPageSize"
                    layout="total, sizes, prev, pager, next"
                    :total="totalExperiments">
                  </el-pagination>
                </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 实验详情弹窗 -->
    <experiment-detail v-if="experimentDetailVisible" ref="experimentDetail"></experiment-detail>

    <el-dialog 
      :title="dialogTitle" 
      :visible.sync="dialogVisible" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="projectForm" :rules="formRules" ref="projectForm" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="projectForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="进行中" :value="0"></el-option>
            <el-option label="已暂停" :value="1"></el-option>
            <el-option label="已完成" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input 
            v-model="projectForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入项目描述"
          ></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="CPU (核)" prop="cpu">
              <el-input-number v-model="projectForm.cpu" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="内存 (GB)" prop="memory">
              <el-input-number v-model="projectForm.memory" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存储 (GB)" prop="storage">
              <el-input-number v-model="projectForm.storage" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="GPU (卡)" prop="gpu">
              <el-input-number v-model="projectForm.gpu" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标签">
          <el-input 
            v-model="tagsInput" 
            placeholder="多个标签用逗号分隔"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProject">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getProjectList, getProjectDetail, createProject, updateProject, getUserProjectList, getValidUsersList, addUserProject, getProjectResourceOverview } from "@/api/llmops/project";
import { getExperimentList } from '@/api/llmops/experiment';
import ExperimentDetail from '../experiment/detail';

export default {
  name: 'Projects',
  components: {
    ExperimentDetail
  },
  data() {
    return {
      filters: {
        department: '',
        status: '',
        keyword: ''
      },
      viewMode: 'grid',
      projects: [],
      stats: {
        total: 0,
        active: 0,
        paused: 0,
        completed: 0
      },
      dialogVisible: false,
      isEdit: false,
      currentProjectId: null,
      tagsInput: '',
      projectForm: {
        name: '',
        status: '',
        description: '',
        gpu: 0,
        cpu: 0,
        memory: 0,
        storage: 0,
        projectManagerId: 0
      },
      formRules: {
        name: [
          { required: true, message: '请输入项目名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
          { pattern: /^[a-zA-Z-]+$/, message: '项目名称只能包含英文和中划线', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ]
      },
      projectDetailVisible: false,
      currentProject: {},
      activeTab: 'overview',
      loading: false,
      // 成员管理相关数据
      memberCategory: 'admin', // 当前选择的类别：admin 或 user
      allValidUsers: [], // 所有有效用户
      projectMembers: [], // 当前项目已添加的成员
      unaddedSearchKeyword: '', // 未添加用户搜索关键字
      addedSearchKeyword: '', // 已添加用户搜索关键字
      selectedUnaddedUsers: [], // 勾选的未添加用户
      selectedAddedUsers: [], // 勾选的已添加用户
      memberLoading: false,
      checkAllUnadded: false,
      checkAllAdded: false,
      detailLoading: false,
      // 实验列表相关数据
      statusMap: {
        0: '未开始',
        1: '运行中',
        2: '已完成',
        3: '失败',
        4: '已暂停'
      },
      experimentList: [],
      totalExperiments: 0,
      experimentPage: 1,
      experimentPageSize: 10,
      experimentLoading: false,
      experimentDetailVisible: false,
      // 资源管理相关数据
      resourceData: [],
      resourceLoading: false,
      resourceOverview: {}
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑项目' : '创建项目'
    },
    filteredProjects() {
      let result = [...this.projects]
      
      if (this.filters.department) {
        result = result.filter(p => p.department === this.filters.department)
      }
      
      if (this.filters.status) {
        result = result.filter(p => p.status === this.filters.status)
      }
      
      if (this.filters.keyword) {
        const keyword = this.filters.keyword.toLowerCase()
        result = result.filter(p => 
          p.name.toLowerCase().includes(keyword)
        )
      }
      
      return result
    },
    // 未添加用户列表过滤
    filteredUnaddedUsers() {
      const addedUserIds = new Set(this.projectMembers.map(m => m.id));
      return this.allValidUsers
        .filter(u => {
          const userId = u.id;
          return userId && userId !== 'undefined' && !addedUserIds.has(userId);
        })
        .filter(u => !this.unaddedSearchKeyword || (u.userName && u.userName.toLowerCase().includes(this.unaddedSearchKeyword.toLowerCase())));
    },
    // 已添加用户列表过滤
    filteredAddedUsers() {
      return this.projectMembers
        .filter(m => m.role === this.memberCategory)
        .filter(m => !this.addedSearchKeyword || m.userName.toLowerCase().includes(this.addedSearchKeyword.toLowerCase()));
    }
  },
  watch: {
    selectedUnaddedUsers(val) {
      this.checkAllUnadded = val.length > 0 && val.length === this.filteredUnaddedUsers.length;
    },
    selectedAddedUsers(val) {
      this.checkAllAdded = val.length > 0 && val.length === this.filteredAddedUsers.length;
    },
    memberCategory() {
      // 切换类别时重置勾选
      this.selectedUnaddedUsers = [];
      this.selectedAddedUsers = [];
    },
    activeTab(val) {
      if (val === 'experiments') {
        this.experimentPage = 1;
        this.fetchExperimentData();
      } else if (val === 'resources') {
        this.fetchResourceOverview();
      }
    }
  },
  methods: {
    handleCheckAllUnadded(val) {
      this.selectedUnaddedUsers = val ? this.filteredUnaddedUsers : [];
    },
    handleCheckAllAdded(val) {
      this.selectedAddedUsers = val ? this.filteredAddedUsers : [];
    },
    handleCreateProject() {
      this.isEdit = false
      this.currentProjectId = null
      this.tagsInput = ''
      this.projectForm = {
        name: '',
        status: '',
        description: '',
        gpu: 0,
        cpu: 0,
        memory: 0,
        storage: 0,
        projectManagerId: 0
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.projectForm && this.$refs.projectForm.clearValidate()
      })
    },
    handleEditProject(project) {
      this.isEdit = true
      this.currentProjectId = project.id
      this.tagsInput = ''
      const pm = project.projectManager || {}
      this.projectForm = {
        name: project.name || '',
        status: pm.status || 0,
        description: project.describe || project.description || '',
        gpu: pm.gpu || 0,
        cpu: pm.cpu || 0,
        memory: pm.memory || 0,
        storage: pm.storage || 0,
        projectManagerId: pm.id || 0
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.projectForm && this.$refs.projectForm.clearValidate()
      })
    },
    async handleViewProject(project) {
      this.currentProject = { ...project }
      this.currentProjectId = project.id
      this.activeTab = 'overview'
      this.projectDetailVisible = true
      this.loadMemberData() // 加载成员数据
      
      // 加载项目详情
      this.detailLoading = true
      try {
        const [detailRes, resourceRes] = await Promise.all([
          getProjectDetail(this.currentProjectId),
          getProjectResourceOverview({ namespace: this.currentProject.name })
        ])

        if (detailRes && detailRes.resultData) {
          this.$set(this.currentProject, 'projectManager', detailRes.resultData.projectManager || {})
          this.$set(this.currentProject, 'describe', detailRes.resultData.describe || '')
        }

        if (resourceRes) {
          this.resourceOverview = resourceRes || {}
        }
      } catch (err) {
        this.$message.error('获取详情失败')
      } finally {
        this.detailLoading = false
      }
    },
    // 加载成员相关数据
    async loadMemberData() {
      if (!this.currentProjectId) return;
      this.memberLoading = true;
      try {
        const [allUsersRes, membersRes] = await Promise.all([
          getValidUsersList(), // 获取系统所有用户
          getUserProjectList({ proId: this.currentProjectId }) // 获取该项目已添加的成员
        ]);
        
        // 映射系统所有用户
        this.allValidUsers = (allUsersRes || []).map(u => ({
          id: u.id || u.userId || u.userid,
          userName: u.userName || u.username || u.name
        }));

        // 映射已添加的项目成员
        this.projectMembers = (membersRes || []).map(m => ({
          id: m.userId || m.id || m.userid,
          userName: m.userName || m.username || m.name,
          role: (m.role === 0 || m.isAdmin === 1) ? 'admin' : 'user'
        }));
      } catch (err) {
        this.$message.error('加载成员数据失败');
      } finally {
        this.memberLoading = false;
      }
    },
    // 添加成员到已选列表 (前端过滤)
    addMembers() {
      if (this.selectedUnaddedUsers.length === 0) return;
      const newMembers = this.selectedUnaddedUsers.map(u => ({
        ...u,
        role: this.memberCategory
      }));
      // 按 id 去重合并
      const existIdSet = new Set(this.projectMembers.map(m => m.id));
      const merged = [...this.projectMembers];
      newMembers.forEach(u => {
        if (!existIdSet.has(u.id)) merged.push(u);
      });
      this.projectMembers = merged;
      this.selectedUnaddedUsers = [];
    },
    // 从已选列表移除成员 (前端过滤)
    removeMembers() {
      if (this.selectedAddedUsers.length === 0) return;
      const removedIdsSet = new Set(this.selectedAddedUsers.map(u => u.id));
      // 从已添加集合移除
      this.projectMembers = this.projectMembers.filter(m => !removedIdsSet.has(m.id));
      // 确保被移除的用户回到未添加集合（如果 allValidUsers 中不存在则补充）
      const allIdSet = new Set(this.allValidUsers.map(u => u.id));
      this.selectedAddedUsers.forEach(u => {
        if (!allIdSet.has(u.id)) {
          this.allValidUsers.push({
            id: u.id,
            userName: u.userName || u.username || '',
            username: u.username || u.userName || ''
          });
        }
      });
      this.selectedAddedUsers = [];
    },
    // 保存成员设置（使用 userId 上送）
    async handleSaveMembers() {
      if (!this.currentProjectId) return;
      this.memberLoading = true;
      try {
        // 构造后端所需 proUserList（按 userId 提交）
        const proUserList = this.projectMembers.map(m => {
          return {
            userId: m.id,
            isAdmin: m.role === 'admin' ? 1 : 0,
            username: m.userName || m.username || m.name,
          }
        });

        await addUserProject({ proId: this.currentProjectId, proUserList });
        this.$message.success('成员更新成功');
        await this.loadMemberData(); // 重新加载以确保同步
        this.selectedUnaddedUsers = [];
        this.selectedAddedUsers = [];
      } catch (err) {
        this.$message.error('保存成员设置失败');
      } finally {
        this.memberLoading = false;
      }
    },
    // 实验列表相关方法
    async fetchExperimentData() {
      if (!this.currentProject.name) return;
      this.experimentLoading = true;
      try {
        const res = await getExperimentList({
          namespace: this.currentProject.name, // 传参 namespace 是当前项目的名字
          pageNum: this.experimentPage,
          pageSize: this.experimentPageSize
        });
        if (res && res.resultData) {
          this.experimentList = res.resultData || [];
          this.totalExperiments = res.totalRecord || 0;
        } else {
          this.experimentList = [];
          this.totalExperiments = 0;
        }
      } catch (error) {
        console.error('Error fetching experiment list:', error);
      } finally {
        this.experimentLoading = false;
      }
    },
    handleExperimentPageChange(page) {
      this.experimentPage = page;
      this.fetchExperimentData();
    },
    handleExperimentSizeChange(size) {
      this.experimentPageSize = size;
      this.experimentPage = 1;
      this.fetchExperimentData();
    },
    handleExperimentDetail(row) {
      this.experimentDetailVisible = true;
      this.$nextTick(() => {
        this.$refs.experimentDetail.init(row);
      });
    },
    async fetchResourceOverview() {
      if (!this.currentProject.name) return;
      this.resourceLoading = true;
      try {
        const res = await getProjectResourceOverview({
          namespace: this.currentProject.name
        });
        if (res) {
          // 接口返回的是单个对象，包装成数组供 table 使用
          // 同时处理一下可能缺失的字段（如已分配字段如果接口没给，暂时补 0 或从 limit 逻辑推断）
          const data = res;
          this.resourceData = [{
            quotaName: data.quotaName || '-',
            cpuUsed: data.cpuUsed || '0',
            cpuAssigned: data.cpuAssigned || '0', // 如果接口没有该字段，按业务逻辑展示
            cpuLimit: data.cpuLimit || '0',
            memoryUsed: data.memoryUsed || '0',
            memoryAssigned: data.memoryAssigned || '0',
            memoryLimit: data.memoryLimit || '0',
            storageUsed: data.storageUsed || '0',
            storageAssigned: data.storageAssigned || '0',
            storageLimit: data.storageLimit || '0',
            gpuUsed: data.gpuUsed || '0',
            gpuAssigned: data.gpuAssigned || '0',
            gpuLimit: data.gpuLimit || '0'
          }];
        } else {
          this.resourceData = [];
        }
      } catch (error) {
        console.error('Error fetching resource overview:', error);
        this.$message.error('获取资源概览失败');
      } finally {
        this.resourceLoading = false;
      }
    },
    getStatusTagType(status) {
      switch (status) {
        case 0:
          return 'info';
        case 1:
          return 'primary';
        case 2:
          return 'success';
        case 3:
          return 'danger';
        case 4:
          return 'warning';
        default:
          return 'info';
      }
    },
    calculatePercent(used, limit) {
      if (!limit || limit === '0') return 0
      const u = parseFloat(used) || 0
      const l = parseFloat(limit) || 0
      if (l <= 0) return 0
      const percent = (u / l) * 100
      return percent > 100 ? 100 : percent
    },
    handleUpgrade(project) {
      this.$message.info(`为项目 ${project.name} 申请扩容`)
    },
    handleSubmitProject() {
      this.$refs.projectForm.validate((valid) => {
        if (!valid) return
        const payload = {
          id: this.isEdit ? this.currentProjectId : 0,
          name: this.projectForm.name,
          describe: this.projectForm.description,
          projectManager: {
            id: this.isEdit && this.projectForm.projectManagerId ? this.projectForm.projectManagerId : 0,
            status: this.projectForm.status,
            gpu: this.projectForm.gpu,
            cpu: this.projectForm.cpu,
            memory: this.projectForm.memory,
            storage: this.projectForm.storage
          }
        }
        const op = this.isEdit ? updateProject : createProject

        op(payload)
          .then(async (res) => {
            const successMsg = this.isEdit ? '项目更新成功' : '项目创建成功'
            this.$message.success(successMsg)
            this.dialogVisible = false
            await this.loadProjects()
          })
          .catch((err) => {
            // 拦截器通常已经处理了错误提示，这里作为兜底，如果拦截器没弹窗或需要特殊处理可在此增加
            console.error('提交项目失败:', err)
          })
          .finally(() => {
          
          })
      })
    },
    async loadProjects() {
      this.loading = true
      try {
        const res = await getProjectList({
          pageNum: 1,
          pageSize: 100,
          name: this.filters.keyword || undefined
        })
        const list = (res && res.resultData) || []
        this.projects = list.map(this.mapApiProjectToUi)
        this.stats.total = this.projects.length
        this.stats.active = this.projects.length
        this.stats.paused = 0
        this.stats.completed = 0
      } finally {
        this.loading = false
      }
    },
    mapApiProjectToUi(p) {
      const pm = p.projectManager || {}
      return {
        ...p, // 保留原始数据以供编辑使用
        id: p.id,
        name: p.name || '',
        department: pm.clusterName || pm.groupName || 'AI Lab',
        gpu: pm.gpu != null ? String(pm.gpu) : '0',
        userCount: p.userCount || 0,
        experimentCount: p.experimentCount || 0,
        isWarning: false,
        changedOn: p.changedOn || p.createdOn || ''
      }
    }
  },
  created() {
    this.loadProjects()
  }
}
</script>

<style lang="scss" scoped>
.projects-container {
  min-height: calc(100vh - 60px);
}

.projects-content {
  // max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #374151;
}

.search-item {
  flex: 1;
  min-width: 200px;
}

.view-toggle {
  display: flex;
  gap: 8px;
  margin-left: 8px;
}

.view-icon {
  font-size: 20px;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.3s;
}

.view-icon:hover,
.view-icon.active {
  color: #2563eb;
}

.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.15);
}

/* 网格视图 */
.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.project-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  overflow: hidden;
  position: relative;
}

.project-card.warning-card {
  border-color: #f59e0b;
}

.project-header {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  padding: 24px 20px;
  position: relative;
  overflow: hidden;
}

.project-header::before {
  content: '';
  position: absolute;
  right: -50px;
  bottom: -50px;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.project-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
  position: relative;
  z-index: 1;
}

.project-department {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  position: relative;
  z-index: 1;
}

.warning-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #f59e0b;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  z-index: 2;
}

.project-stats {
  display: flex;
  justify-content: space-around;
  padding: 24px 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.stat-divider {
  width: 1px;
  background-color: #e5e7eb;
}

.project-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background-color: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.enter-btn {
  flex: 1;
  background-color: #eff6ff;
  color: #2563eb;
  border: none;
  font-weight: 500;
}

.enter-btn:hover {
  background-color: #dbeafe !important;
  color: #1d4ed8 !important;
}

.settings-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.3s;
}

.settings-icon:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.upgrade-btn {
  background-color: #fef3c7;
  color: #d97706;
  border: none;
  font-size: 12px;
  padding: 8px 12px;
}

.upgrade-btn:hover {
  background-color: #fde68a !important;
  color: #b45309 !important;
}

.create-card {
  background: #f9fafb;
  border-radius: 12px;
  border: 2px dashed #d1d5db;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  cursor: pointer;
}

.create-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: white;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  font-size: 24px;
  color: #6b7280;
  transition: all 0.3s;
}

.create-card:hover .create-icon {
  border-color: #2563eb;
  color: #2563eb;
}

.create-text {
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 4px;
}

.create-subtext {
  font-size: 13px;
  color: #9ca3af;
}

/* 列表视图 */
.projects-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.list-header {
  display: flex;
  background-color: #f9fafb;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.list-item {
  display: flex;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
}

.list-item:hover {
  background-color: #f9fafb;
}

.list-col {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #374151;
}

/* 项目详情弹框样式 */
.project-detail-dialog {
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
    padding: 24px;
  }
}

.dialog-title-custom {
  margin: 0;
}

.project-detail-header {
  display: flex;
  flex-direction: column;
}

.project-detail-title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4px;
}

.project-detail-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.project-detail-content {
  margin-top: 8px;
}

.project-detail-tabs {
  ::v-deep .el-tabs__header {
    margin-bottom: 24px;
  }

  ::v-deep .el-tabs__nav-wrap::after {
    display: none;
  }

  ::v-deep .el-tabs__item {
    padding: 0 32px !important;
    font-size: 14px;
    font-weight: 500;
    color: #6b7280;
    border: 1px solid transparent;
    border-radius: 8px;
    background-color: #f9fafb;
    margin-right: 8px;
  }

  ::v-deep .el-tabs__item:hover {
    color: #2563eb;
  }

  ::v-deep .el-tabs__item.is-active {
    color: #1f2937;
    background-color: white;
    border-color: #e5e7eb;
  }

  ::v-deep .el-tabs__active-bar {
    display: none;
  }
}

.overview-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.resource-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.resource-card {
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 20px;
}

.resource-label {
  font-size: 14px;
  color: #6b7280;
}

.resource-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.resource-usage-text {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.resource-value {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.resource-bar {
  width: 100%;
  height: 10px;
  background-color: #e5e7eb;
  border-radius: 5px;
  overflow: hidden;
}

.resource-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #2563eb);
  border-radius: 5px;
}

.recent-activities {
  display: flex;
  flex-direction: column;
}

.activities-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 4px;
  flex-shrink: 0;
}

.blue-dot {
  background-color: #3b82f6;
}

.green-dot {
  background-color: #10b981;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}

.activity-link {
  color: #2563eb;
  cursor: pointer;
}

.activity-time {
  font-size: 13px;
  color: #6b7280;
}

.experiments-content {
  padding: 0;
}

.experiments-content .list-section {
  box-shadow: none;
  border: none;
}

.experiments-content .section-title {
  display: none;
}

.experiments-content .pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.experiments-content .total-items {
  margin-right: auto;
  color: #606266;
  font-size: 14px;
}

.resources-content {
    padding: 0;
  }
  
  .resource-table ::v-deep th {
    background-color: #f9fafb !important;
    color: #374151;
    font-weight: 600;
  }
  
  .resource-table ::v-deep .el-table__header-wrapper th {
    border-bottom: 1px solid #ebeef5;
  }
  
  .resource-table ::v-deep td {
    padding: 12px 0;
  }

  .members-content,
.resources-content,
.experiments-content {
  color: #6b7280;
}

.member-management-wrapper {
  display: flex;
  height: 300px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.member-sidebar {
  width: 120px;
  border-right: 1px solid #e5e7eb;
  padding: 16px 0;
  
  .sidebar-item {
    padding: 12px 20px;
    font-size: 14px;
    color: #6b7280;
    cursor: pointer;
    position: relative;
    transition: all 0.3s;
    
    &:hover {
      color: #2563eb;
      background: #f9fafb;
    }
    
    &.active {
      color: #2563eb;
      font-weight: 500;
      
      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: 15%;
        height: 70%;
        width: 3px;
        background: #2563eb;
      }
    }
  }
}

.transfer-container {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  align-items: stretch;
}

.transfer-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
  
  .panel-header {
    padding: 10px 16px;
    background: #f9fafb;
    border-bottom: 1px solid #e5e7eb;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .count {
      font-size: 12px;
      color: #9ca3af;
    }
  }
  
  .panel-search {
    padding: 8px 12px;
    border-bottom: 1px solid #f3f4f6;
  }
  
  .panel-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;
    
    .list-item-checkbox {
      padding: 6px 16px;
      
      &:hover {
        background: #f9fafb;
      }
      
      ::v-deep .el-checkbox {
        width: 100%;
        display: flex;
        align-items: center;
        
        .el-checkbox__label {
          font-size: 14px;
          color: #374151;
        }
      }
    }
  }
}

.transfer-buttons {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
  align-items: baseline;
  
  ::v-deep .el-button--mini {
    padding: 10px 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    margin: 0;
    
    &.is-disabled {
      background-color: #f3f4f6;
      border-color: #e5e7eb;
      color: #9ca3af;
    }
  }
}

.member-footer {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1200px) {
  .projects-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .projects-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-item {
    width: 100%;
  }
}
</style>
