<template>
  <div class="flow-builder-container">
    <template>
      <!-- 顶部一行：标题 / 主操作 -->
      <div class="flow-topbar">
        <div class="flow-row top-row">
          <div class="title-cell">
            <el-button type="text" icon="el-icon-back" class="back-btn" @click="handleBack">返回</el-button>
            <span class="flow-title">{{ flowName }}</span>
            <i class="el-icon-setting title-setting" @click="handleSettings"></i>
          </div>
          <div class="action-cell">
            <el-button type="primary" size="small" icon="el-icon-caret-right" class="primary-btn" :loading="runLoading"
              @click="handleRun">运行</el-button>
            <el-button type="warning" size="small" icon="el-icon-copy-document" class="primary-btn"
              :loading="copyLoading" @click="handleCopy">复制</el-button>
          </div>
        </div>
      </div>

      <!-- 主体：左面板 + 画布 -->
      <div class="flow-builder-content" :class="{ 'panel-collapsed': panelCollapsed }">
        <!-- 左侧任务面板 -->
        <div v-show="!panelCollapsed" class="task-panel" v-loading="panelLoading" element-loading-text="加载中...">
          <div class="panel-header">
            <div class="assets-info">
              <span class="assets-count">{{ totalCount }} assets in total</span>
            </div>
            <div class="panel-actions">
              <i class="el-icon-refresh-right action-icon" @click="fetchJobTemplates" title="刷新"></i>
              <i class="el-icon-plus action-icon" @click="handleAddTask" title="新建"></i>
            </div>
          </div>
          <div class="panel-search">
            <el-input v-model="searchText" placeholder="搜索任务模板" prefix-icon="el-icon-search" size="small"
              class="search-input"></el-input>
          </div>
          <div class="panel-body">
            <div v-for="project in treeData" :key="project.id" class="project-group">
              <div class="project-title" @click="project.expanded = !project.expanded">
                <div>
                  <i :class="project.expanded ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" class="arrow-icon"></i>
                  <span class="project-name">{{ project.label }}</span>
                </div>

                <span class="project-count">({{ project.children.length }})</span>

              </div>
              <div v-show="project.expanded" class="task-list">
                <div v-for="task in project.children" :key="task.id" class="task-item" draggable="true"
                  @dragstart="handleDragStart($event, task)" @dragend="handleDragEnd"
                  @mouseenter="showTaskTooltip($event, task)" @mouseleave="hideTaskTooltip">
                  <div class="task-name">{{ task.name }}</div>
                  <div class="task-desc">{{ task.describe }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Task Detail Tooltip -->
        <div v-if="showTooltip && hoverTask" class="task-tooltip" :style="tooltipStyle">
          <div class="tooltip-title">{{ hoverTask.name }}</div>
          <div class="tooltip-section">
            <div class="tooltip-label">描述</div>
            <div class="tooltip-value">{{ hoverTask.describe ||
              '-' }}</div>
          </div>
          <div class="tooltip-section">
            <div class="tooltip-label">创建人</div>
            <div class="tooltip-value">{{ hoverTask.creator || '-' }}</div>
          </div>
          <div class="tooltip-section">
            <div class="tooltip-label">镜像</div>
            <div class="tooltip-value">{{ hoverTask.imageName || '-' }}</div>
          </div>
          <div class="tooltip-section">
            <div class="tooltip-label">上次修改时间</div>
            <div class="tooltip-value">{{ hoverTask.modified || '-' }}</div>
          </div>
          <div class="tooltip-section">
            <div class="tooltip-label">版本</div>
            <div class="tooltip-value">{{ hoverTask.version || '-' }}</div>
          </div>
        </div>

        <!-- 右侧画布区域 -->
        <div class="canvas-wrapper" v-loading="canvasLoading" element-loading-text="加载中...">
          <div class="canvas-toolbar">
            <span class="tb-btn" @click="togglePanel">
              <i class="el-icon-menu"></i> 展开/关闭菜单
            </span>
            <span class="tb-btn" @click="saveFlow">
              <i class="el-icon-folder-checked"></i> 保存
            </span>
            <span class="tb-btn" @click="handleSchedule">
              <i class="el-icon-d-arrow-right"></i> 调度实例
            </span>
            <span class="tb-btn" :class="{ 'is-disabled': !pipelineArgoId }" @click="pipelineArgoId && handleViewLog()">
              <i class="el-icon-document"></i> 日志
            </span>
            <span class="tb-btn" @click="handleContainer">
              <i class="el-icon-cpu"></i> 容器
            </span>
            <!-- <span class="tb-btn" @click="handleSchedRecord"> -->
            <!-- <i class="el-icon-time"></i> 定时记录 -->
            <!-- </span> -->
            <span class="tb-btn" :class="{ 'is-disabled': !selectedNode }" @click="deleteSelectedNode">
              <i class="el-icon-delete"></i> 删除节点
            </span>
            <span class="tb-btn" :class="{ 'is-disabled': !selectedLink }" @click="deleteSelectedLink">
              <i class="el-icon-delete"></i> 删除连接线
            </span>
            <span class="tb-btn" @click="handleMonitor">
              <i class="el-icon-data-line"></i> 监控
            </span>
            <!-- <span class="saved-status">
              <i class="el-icon-success"></i> 已保存
            </span> -->
          </div>
          <div ref="canvasContainer" class="canvas-container"></div>
          <!-- 画布悬浮缩放工具 -->
          <div class="canvas-zoom-tools">
            <el-button size="mini" icon="el-icon-zoom-in" circle @click="zoomIn"></el-button>
            <el-button size="mini" icon="el-icon-zoom-out" circle @click="zoomOut"></el-button>
            <el-button size="mini" icon="el-icon-refresh" circle @click="resetZoom"></el-button>
          </div>

          <!-- 右侧配置面板 -->
          <div v-if="selectedNodeData" class="config-panel" v-loading="configLoading" element-loading-text="加载中...">
            <div class="config-header">
              <div class="config-title">{{ selectedNodeData.title || selectedNodeData.name }}</div>
              <div class="config-actions">
                <el-button type="primary" size="mini" :loading="saveNodeLoading" @click="saveNodeConfig">保存</el-button>
                <i class="el-icon-close close-icon" @click="closeConfigPanel"></i>
              </div>
            </div>
            <div class="config-actions-bar">
              <span class="config-action-btn" @click="handleDebugTask(selectedNodeData)">
                <i class="el-icon-video-play"></i> debug
              </span>
              <span class="config-action-btn" @click="handleRunTask(selectedNodeData)">
                <i class="el-icon-right"></i> run
              </span>
              <span class="config-action-btn" @click="handleGetTaskLog(selectedNodeData)">
                <i class="el-icon-document"></i> log
              </span>
              <span class="config-action-btn" @click="handleClearTask(selectedNodeData)">
                <i class="el-icon-delete"></i> clear
              </span>
            </div>
            <div class="config-body">
              <div class="config-section">
                <div class="config-section-title">任务模板</div>
                <div class="config-input-wrapper">
                  <el-input v-model="selectedNodeData.name" size="small" disabled></el-input>
                </div>
                <div class="config-link">配置文档</div>
              </div>

              <div class="config-section">
                <div class="config-section-title">模板描述</div>
                <div class="config-input-wrapper">
                  <el-input v-model="selectedNodeData.desc" type="textarea" :rows="2" size="small" disabled>
                  </el-input>
                </div>
              </div>

              <div class="config-section">
                <div class="config-section-title">名称</div>
                <div class="config-input-wrapper">
                  <el-input v-model="selectedNodeData.nodeId" size="small"></el-input>
                </div>
              </div>

              <div class="config-section">
                <div class="config-section-title">标签</div>
                <div class="config-input-wrapper">
                  <el-input v-model="selectedNodeData.title" size="small" placeholder="请输入标签"></el-input>
                </div>
              </div>

              <div class="config-section">
                <div class="config-section-title">节点参数</div>

                <div class="config-field">
                  <div class="config-field-label">内存申请</div>
                  <div class="config-input-wrapper">
                    <el-input v-model="selectedNodeConfig.memory" size="small" placeholder="2G"></el-input>
                  </div>
                  <div class="config-field-tip">内存的资源使用配置，示例：1G，10G，最大100G，如需更多联系管理员</div>
                </div>

                <div class="config-field">
                  <div class="config-field-label">CPU申请</div>
                  <div class="config-input-wrapper">
                    <el-input v-model="selectedNodeConfig.cpu" size="small" placeholder="2"></el-input>
                  </div>
                  <div class="config-field-tip">CPU的资源使用配置(单位：核)，示例：0.4，1.0，最大10核，如需更多联系管理员</div>
                </div>

                <div class="config-field">
                  <div class="config-field-label">GPU申请</div>
                  <div class="config-input-wrapper">
                    <el-input v-model="selectedNodeConfig.gpu" size="small" placeholder="0"></el-input>
                  </div>
                  <div class="config-field-tip">gpu的资源使用配置(单位：卡)，示例：1，2，训练任务每个容器独占整卡。中国具体的卡型号，可以类似 (v100)</div>
                </div>

                <div class="config-field">
                  <div class="config-field-label">RDMA申请</div>
                  <div class="config-input-wrapper">
                    <el-input v-model="selectedNodeConfig.rdma" size="small" placeholder="0"></el-input>
                  </div>
                  <div class="config-field-tip">RDMA的资源使用配置，示例：0，1，10， 填写方式咨询管理员</div>
                </div>

                <div class="config-field">
                  <div class="config-field-label">超时中断</div>
                  <div class="config-input-wrapper">
                    <el-input v-model="selectedNodeConfig.timeout" size="small" placeholder="0"></el-input>
                  </div>
                  <div class="config-field-tip">任务运行时长限制，为0表示不限制(单位：s)</div>
                </div>
                <div class="config-field">
                  <div class="config-field-label">重试次数</div>
                  <div class="config-input-wrapper">
                    <el-input v-model="selectedNodeConfig.retry" size="small" placeholder="0"></el-input>
                  </div>
                  <div class="config-field-tip">task重试次数</div>
                </div>

                <div class="config-field">
                  <div class="config-field-label">是否跳过</div>
                  <div class="config-switch-wrapper">
                    <el-switch v-model="selectedNodeConfig.skip"></el-switch>
                  </div>
                </div>

                <!-- 动态参数表单区域 -->
                <div v-if="dynamicFormGroups.length > 0" class="dynamic-form-container">
                  <el-collapse v-model="activeDynamicGroups">
                    <el-collapse-item v-for="(group, groupIdx) in dynamicFormGroups" :key="groupIdx" :title="group.name"
                      :name="group.name">
                      <div v-for="(field, key) in group.fields" :key="key" class="config-field">
                        <div class="config-field-label">
                          <span v-if="field.require" style="color: #f56c6c;">*</span>
                          {{ field.label || key }}
                        </div>

                        <div class="config-input-wrapper">
                          <!-- 字符串类型 -->
                          <el-input v-if="field.type === 'str'" v-model="dynamicFormData[key]" size="small"
                            :placeholder="field.default || ''"></el-input>

                          <!-- 整数类型 -->
                          <el-input-number v-else-if="field.type === 'int'" v-model="dynamicFormData[key]" size="small"
                            style="width: 100%;"></el-input-number>

                          <!-- 布尔类型 -->
                          <el-switch v-else-if="field.type === 'bool'" v-model="dynamicFormData[key]"></el-switch>

                          <!-- 下拉选择类型 -->
                          <el-select v-else-if="field.type === 'select'" v-model="dynamicFormData[key]" size="small"
                            style="width: 100%;">
                            <el-option v-for="opt in field.choices" :key="opt" :label="opt" :value="opt"></el-option>
                          </el-select>

                          <!-- 默认兜底 -->
                          <el-input v-else v-model="dynamicFormData[key]" size="small"></el-input>
                        </div>

                        <div v-if="field.describe" class="config-field-tip">{{ field.describe }}</div>
                      </div>
                    </el-collapse-item>
                  </el-collapse>
                </div>


              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import * as d3 from 'd3';
import { getAllJobTemplateList } from '@/api/llmops/paradigm';
import {
  getPipelineDetail,
  updatePipeline,
  runPipeline,
  runTask,
  getTaskLog,
  debugTask,
  clearTask,
  deleteTask,
  getTask,
  createTask,
  updateTask,
  mulCopyPipeline
} from '@/api/llmops/orchestration';
import * as k8s_api from '@/api/app/k8s';
import choosPodToWebssh from '@/utils/choosPodToWebssh';

export default {
  name: 'FlowBuilder',

  data() {
    return {
      viewMode: 'builder',
      currentInstance: null,
      flowName: '',
      searchText: '',
      panelCollapsed: false,
      showTooltip: false,
      hoverTask: null,
      tooltipStyle: {},
      flowMeta: {},
      pipelineArgoId: '',
      jobTemplates: [],
      treeData: [],
      nodes: [],
      links: [],
      svg: null,
      zoomBehavior: null,
      rootGroup: null,
      selectedNode: null,
      selectedLink: null,
      selectedNodeData: null,
      selectedNodeConfig: {
        memory: '2G',
        cpu: '2',
        gpu: '0',
        rdma: '0',
        timeout: '0',
        retry: '0',
        skip: false
      },
      dynamicFormGroups: [], // 存储解析后的表单结构
      dynamicFormData: {},   // 存储表单实际值
      activeDynamicGroups: [], // 存储激活的折叠面板
      nodeIdCounter: 0,
      tempLink: null,
      connectingNode: null,
      isDragging: false,
      panelLoading: false,
      canvasLoading: false,
      configLoading: false,
      runLoading: false,
      copyLoading: false,
      saveNodeLoading: false
    };
  },
  computed: {
    totalCount() {
      return this.jobTemplates.length;
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initCanvas();
    });
    this.fetchJobTemplates();
    this.fetchPipelineDetail();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    async fetchJobTemplates() {
      this.panelLoading = true;
      try {
        const res = await getAllJobTemplateList();
        if (res && res.resultData) {
          this.jobTemplates = res.resultData || [];
          this.buildTreeData();
        }
      } catch (error) {
        console.error('获取任务模板失败:', error);
        this.$message.error('获取任务模板失败');
      } finally {
        this.panelLoading = false;
      }
    },
    async fetchPipelineDetail() {
      const id = this.$route.query.id;
      if (!id) return;

      this.canvasLoading = true;
      try {
        const res = await getPipelineDetail(id);
        if (res) {
          const data = res;

          if (data.name) {
            this.flowName = data.name;
          }

          if (data) {
            this.flowMeta = { ...data };
            this.pipelineArgoId = data.pipelineArgoId || '';
          }

          // 解析 expand 中的节点和连线信息
          let expandNodes = [];
          let expandLinks = [];
          if (data.expand) {
            try {
              const parsedExpand = typeof data.expand === 'string' ? JSON.parse(data.expand) : data.expand;
              if (Array.isArray(parsedExpand)) {
                parsedExpand.forEach(item => {
                  if (item.source && item.target) {
                    expandLinks.push({
                      id: item.id || `edge-${item.source}-${item.target}`,
                      source: String(item.source),
                      target: String(item.target),
                      arrowHeadType: item.arrowHeadType || 'arrow'
                    });
                  } else {
                    const data_ = item.data || {};
                    const info = data_.info || {};
                    expandNodes.push({
                      id: String(item.id),
                      type: item.type || 'dataSet',
                      nodeId: data_.name || `node-${item.id}`,
                      name: data_.name || item.id,
                      title: data_.label || data_.name || String(item.id),
                      subtitle: info.describe || '',
                      desc: info.describe || '',
                      x: item.position ? Number(item.position.x) || 20 : 20,
                      y: item.position ? Number(item.position.y) || 20 : 20,
                      status: 'Pending',
                      _raw: item
                    });
                  }
                });
              }
            } catch (e) {
              console.error('解析 expand 详情失败:', e);
            }
          }

          if (expandNodes.length > 0) {
            this.nodes = expandNodes;
            this.links = expandLinks;
            // 让后续新增节点的自增 id 不与已有冲突
            const maxNumId = expandNodes.reduce((m, n) => {
              const num = parseInt(n.id, 10);
              return Number.isFinite(num) && num > m ? num : m;
            }, 0);
            this.nodeIdCounter = maxNumId;
          } else {
            if (data.nodes) {
              this.nodes = data.nodes.map(node => ({
                ...node,
                x: node.x || 20,
                y: node.y || 20
              }));
            }
            if (data.links) {
              this.links = data.links;
            }
          }

          this.$nextTick(() => {
            this.render();
          });
        }
      } catch (error) {
        console.error('获取流程详情失败:', error);
        this.$message.error('获取流程详情失败');
      } finally {
        this.canvasLoading = false;
      }
    },
    buildTreeData() {
      const projectMap = {};

      this.jobTemplates.forEach(template => {
        const project = template.project || '默认项目';
        if (!projectMap[project]) {
          projectMap[project] = {
            id: `project-${project}`,
            label: project,
            expanded: false,
            children: []
          };
        }

        let expandData = {};
        try {
          if (template.expand) {
            expandData = typeof template.expand === 'string'
              ? JSON.parse(template.expand)
              : template.expand;
          }
        } catch (e) {
          console.error('解析 expand 数据失败:', e);
        }

        projectMap[project].children.push({
          ...template,
          templateId: template.id,
          id: `template-${template.id}`,
          name: template.name,
          desc: expandData.rec_job_template || '',
          type: expandData.rec_job_template || template.name
        });
      });

      this.treeData = Object.values(projectMap);
    },
    handleSettings() {
      this.$message.info('打开设置');
    },
    async handleRun() {
      const id = this.$route.query.id;
      if (!id) {
        this.$message.warning('请先保存任务编排再运行');
        return;
      }

      this.runLoading = true;
      try {
        const res = await runPipeline(id);
        if (res) {
          this.$message.success('运行成功');
          this.pipelineArgoId = res.result.pipelineArgoId || '';
          this.handleViewLog();
        } else {
          this.$message.error(res.message || '运行失败');
        }
      } catch (error) {
        console.error('运行失败:', error);
        this.$message.error('运行失败');
      } finally {
        this.runLoading = false;
      }
    },
    async handleRunTask(node) {
      if (!node || !node.id) return;
      this.configLoading = true;
      try {
        const res = await runTask(node.id);
        if (res) {
          this.$message.success(`任务 ${node.title || node.name} 运行指令已发送`);
        }
      } catch (error) {
        console.error('运行任务失败:', error);
        this.$message.error('运行任务失败');
      } finally {
        this.configLoading = false;
      }
    },
    async handleDebugTask(node) {
      if (!node || !node.id) return;
      console.log(node)
      const loading = this.$loading({
        target: 'body',
        text: '正在获取调试信息...',
        background: 'rgba(0, 0, 0, 0.3)'
      });

      try {
        const res = await debugTask(node.id);
        if (res && res.status !== 0) {
          this.$message.error(res.message || '调试启动失败');
          loading.close();
          return;
        }

        const taskName = node.name || node.title;
        if (!taskName) {
          this.$message.warning('任务名称缺失，无法进行调试');
          loading.close();
          return;
        }

        const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || 'public';
        const clusterName = (this.$store.state.project.currentCluster && this.$store.state.project.currentCluster.name) || 'default';

        // 尝试获取对应的 Pod
        const labelSelector = `app=${this.flowName},pod-type=task,pod-name=debug-${this.flowName}-${taskName}`;
        const podRes = await k8s_api.getContainerGroup(namespace, clusterName, labelSelector);
        const items = (podRes && podRes.items) || [];
        const runningPods = items.filter(i => i.status && i.status.phase === 'Running');
        const pod = runningPods[0] || items[0];

        loading.close();

        if (!pod) {
          this.$message.warning('调试容器创建中，请稍后重试');
          return;
        }

        // 跳转到 WebSSH 终端
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
        console.error('调试任务失败:', error);
        this.$message.error('调试失败');
      }
    },
    async handleGetTaskLog(node) {
      if (!node || !node.id) return;
      const taskName = node.name || node.title;
      if (!taskName) {
        this.$message.warning('任务名称缺失，无法获取日志');
        return;
      }

      const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || 'public';
      const clusterName = (this.$store.state.project.currentCluster && this.$store.state.project.currentCluster.name) || 'default';
      const loading = this.$loading({
        target: 'body',
        text: '正在获取日志容器...',
        background: 'rgba(0, 0, 0, 0.3)'
      });

      try {
        const labelSelector = `app=${this.flowName},pod-type=task,pod-name=run-${this.flowName}-${taskName}`;
        const podRes = await k8s_api.getContainerGroup(namespace, clusterName, labelSelector);
        const items = (podRes && podRes.items) || [];
        const runningPods = items.filter(i => i.status && i.status.phase === 'Running');
        const pod = runningPods[0] || items[0];

        loading.close();

        if (!pod) {
          this.$message.warning('未找到运行中的任务容器');
          return;
        }

        const { href } = this.$router.resolve({
          path: `/webssh/log/${namespace}/${pod.metadata.name}`,
          query: {
            proName: namespace,
            clusterName: clusterName
          }
        });
        window.open(href, '_blank');
      } catch (error) {
        loading.close();
        console.error('获取任务日志失败:', error);
        this.$message.error('获取任务日志失败');
      }
    },
    async handleClearTask(node) {
      if (!node || !node.id) return;
      this.configLoading = true;
      try {
        const res = await clearTask(node.id);
        if (res) {
          this.$message.success(`任务 ${node.title || node.name} 已清除`);
        }
      } catch (error) {
        console.error('清除任务失败:', error);
        this.$message.error('清除任务失败');
      } finally {
        this.configLoading = false;
      }
    },
    async handleCopy() {
      const id = this.$route.query.id;
      if (!id) {
        this.$message.warning('当前任务编排未保存，无法复制');
        return;
      }
      try {
        await this.$confirm(`确定复制当前任务编排 "${this.flowName}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        this.copyLoading = true;
        const res = await mulCopyPipeline({ ids: [Number(id) || id] });
        if (res) {
          this.$message.success('复制成功');
        } else {
          this.$message.error((res && res.message) || '复制失败');
        }
      } catch (error) {
        if (error === 'cancel') return;
        console.error('复制失败:', error);
        this.$message.error('复制失败');
      } finally {
        this.copyLoading = false;
      }
    },
    handleAddTask() {
      this.$router.push('/llmops/training/paradigm');
    },
    togglePanel() {
      this.panelCollapsed = !this.panelCollapsed;
    },
    handleSchedule() {
      this.$router.push({
        path: '/llmops/training/instance',

      });
    },
    handleViewLog() {
      const id = this.$route.query.id;
      if (!id) return;
      this.$router.push({
        path: '/llmops/training/log',
        query: {
          id: id,
          pipelineArgoId: this.pipelineArgoId
        }
      });
    },
    handleContainer() {
      const pipelineId = this.$route.query.id;
      if (!pipelineId) {
        this.$message.warning('流程信息缺失');
        return;
      }
      const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || 'public';
      choosPodToWebssh(
        {
          serviceName: this.flowName,
          namespace,
          type: 'console',
          labelSelector: `pipeline-id=${pipelineId}`
        },
        true
      );
    },
    handleSchedRecord() {
      this.$message.info('定时记录');
    },
    handleMonitor() {
      const pipelineId = this.$route.query.id;
      if (!pipelineId) {
        this.$message.warning('流程信息缺失');
        return;
      }
      const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || 'public';
      this.$router.push({
        path: '/llmops/serving/monitor',
        query: {
          type: 'pipeline',
          id: pipelineId,
          namespace: namespace
        }
      });
    },
    handleBack() {
      this.$router.back();
    },
    handleStop() {
      this.$message.info('终止流程');
    },
    handleViewWorkflow() {
      this.$message.info('跳转任务流');
    },
    handleDragStart(e, task) {
      e.dataTransfer.effectAllowed = 'copy';
      e.dataTransfer.setData('task', JSON.stringify(task));
      e.target.style.opacity = '0.5';
    },
    handleDragEnd(e) {
      e.target.style.opacity = '1';
    },
    showTaskTooltip(e, task) {
      this.hoverTask = task;
      this.showTooltip = true;

      const x = e.currentTarget.getBoundingClientRect().right + 10;
      const y = e.currentTarget.getBoundingClientRect().top;

      this.tooltipStyle = {
        left: x + 'px',
        top: y + 'px'
      };
    },
    hideTaskTooltip() {
      this.showTooltip = false;
      this.hoverTask = null;
    },
    handleDragOver(e) {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'copy';
    },
    async handleDrop(e) {
      e.preventDefault();
      const taskData = e.dataTransfer.getData('task');
      if (!taskData) return;

      const pipelineId = this.$route.query.id;
      if (!pipelineId) {
        this.$message.warning('请先保存任务编排再添加任务');
        return;
      }

      const task = JSON.parse(taskData);
      const { x: cx, y: cy } = this.getCanvasPoint(e.clientX, e.clientY);
      const x = cx - 200;
      const y = cy;

      const tempId = `node-${++this.nodeIdCounter}`;
      const taskName = `${task.name}-${Date.now()}`;
      const taskLabel = `新建 ${task.name} 任务`;

      const newNode = {
        ...task,
        id: tempId,
        nodeId: taskName,
        name: taskName,
        title: taskLabel,
        subtitle: task.describe || '',
        x: Math.max(20, x),
        y: Math.max(20, y),
        status: 'Pending'
      };
      this.nodes.push(newNode);
      this.render();

      const argsObj = {
        images: task.images || task.imageName || '',
        workdir: task.working_dir || '',
        command: task.command || ''
      };

      const payload = {
        job_template: task.templateId,
        pipeline: Number(pipelineId),
        name: taskName,
        label: taskLabel,
        volume_mount: task.volume_mount || '',
        image_pull_policy: task.image_pull_policy || 'Always',
        working_dir: task.working_dir || '',
        command: task.command || '',
        overwrite_entrypoint: task.overwrite_entrypoint || 0,
        node_selector: task.node_selector || '',
        resource_memory: task.resource_memory || '2G',
        resource_cpu: task.resource_cpu || '2',
        resource_gpu: task.resource_gpu || '0',
        resource_rdma: task.resource_rdma || '0',
        timeout: 0,
        retry: 0,
        args: JSON.stringify(argsObj),
        filters: [{ col: 'pipeline', opr: 'rel_o_m', value: Number(pipelineId) }]
      };

      this.canvasLoading = true;
      try {
        const res = await createTask(payload);
        const newId = res && (res.result && (res.result.id || res.result.task_id) || res.id);
        if (newId) {
          const target = this.nodes.find(n => n.id === tempId);
          if (target) {
            target.id = String(newId);
            target.status = 'Pending';
          }
          this.render();
          this.$message.success('任务节点已保存');
        } else {
          this.$message.warning('任务保存返回缺少 id');
        }
      } catch (error) {
        console.error('保存任务节点失败:', error);
        this.$message.error('保存任务节点失败');
        this.nodes = this.nodes.filter(n => n.id !== tempId);
        this.render();
      } finally {
        this.canvasLoading = false;
      }
    },
    handleCanvasMouseMove(e) {
      if (this.tempLink && this.connectingNode) {
        const { x, y } = this.getCanvasPoint(e.clientX, e.clientY);
        this.tempLink.endX = x;
        this.tempLink.endY = y;

        // 性能优化：直接更新或创建临时路径的 d 属性，避免全量重绘导致的严重卡顿
        const nodeWidth = 300;
        const nodeHeight = 60;
        const x1 = this.connectingNode.x + nodeWidth / 2;
        const y1 = this.connectingNode.y + nodeHeight + 6;
        const midY = (y1 + y) / 2;

        let tempPath = this.rootGroup.select('path.temp-link-line');
        if (tempPath.empty()) {
          tempPath = this.rootGroup.append('path')
            .attr('class', 'temp-link-line')
            .attr('stroke', '#3b82f6')
            .attr('stroke-width', 2)
            .attr('stroke-dasharray', '5,5')
            .attr('fill', 'none');
        }
        tempPath.attr('d', `M ${x1} ${y1} C ${x1} ${midY}, ${x} ${midY}, ${x} ${y}`);
      }
    },
    getCanvasPoint(clientX, clientY) {
      // 屏幕坐标 → SVG 坐标 → root 组（含 d3.zoom transform）坐标
      const svgEl = this.svg.node();
      const ctm = svgEl.getScreenCTM();
      if (!ctm) return { x: 0, y: 0 };
      const pt = svgEl.createSVGPoint();
      pt.x = clientX;
      pt.y = clientY;
      const inSvg = pt.matrixTransform(ctm.inverse());
      const transform = d3.zoomTransform(svgEl);
      return {
        x: (inSvg.x - transform.x) / transform.k,
        y: (inSvg.y - transform.y) / transform.k
      };
    },
    handleCanvasMouseUp() {
      if (this.tempLink) {
        this.tempLink = null;
        this.connectingNode = null;
        // 移除临时路径并重绘一次最终状态
        if (this.rootGroup) {
          this.rootGroup.select('path.temp-link-line').remove();
        }
        this.render();
      }
    },
    statusColor(status) {
      switch (status) {
        case 'Succeeded': return '#10b981';
        case 'Failed': return '#ef4444';
        case 'Running': return '#3b82f6';
        case 'Skipped': return '#1f2937';
        case 'Pending':
        default: return '#9ca3af';
      }
    },
    statusIcon(status) {
      switch (status) {
        case 'Succeeded': return '✓';
        case 'Failed': return '✕';
        case 'Running': return '○';
        case 'Skipped': return '?';
        case 'Pending':
        default: return '?';
      }
    },
    getLinkKey(link) {
      return link && (link.id || `edge-${link.source}-${link.target}`);
    },
    initCanvas() {
      const container = this.$refs.canvasContainer;
      if (!container) return;

      d3.select(container).selectAll('*').remove();

      const width = container.clientWidth;
      const height = container.clientHeight || 600;

      const svg = d3.select(container)
        .append('svg')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('viewBox', `0 0 ${width} ${height}`)
        .attr('preserveAspectRatio', 'xMidYMid meet');

      const defs = svg.append('defs');

      defs.append('marker')
        .attr('id', 'arrow-end')
        .attr('viewBox', '0 0 10 10')
        .attr('markerUnits', 'userSpaceOnUse')
        .attr('refX', 9)
        .attr('refY', 5)
        .attr('markerWidth', 8)
        .attr('markerHeight', 8)
        .attr('orient', 'auto-start-reverse')
        .append('path')
        .attr('d', 'M 0 0 L 10 5 L 0 10 z')
        .attr('fill', '#9ca3af');

      const filter = defs.append('filter')
        .attr('id', 'shadow')
        .attr('x', '-5%')
        .attr('y', '-5%')
        .attr('width', '110%')
        .attr('height', '110%');

      filter.append('feDropShadow')
        .attr('dx', 0)
        .attr('dy', 2)
        .attr('stdDeviation', 3)
        .attr('flood-color', 'rgba(0, 0, 0, 0.12)');

      const root = svg.append('g').attr('class', 'flow-root');
      this.svg = svg;
      this.rootGroup = root;

      const zoomBehavior = d3.zoom()
        .scaleExtent([0.3, 2])
        .filter((event) => {
          if (event.target && event.target.closest && event.target.closest('g.node')) return false;
          return !event.button;
        })
        .on('zoom', (event) => {
          root.attr('transform', event.transform);
        });
      this.zoomBehavior = zoomBehavior;

      svg.call(zoomBehavior);
      svg.on('dblclick.zoom', null);
      svg.on('click', () => {
        this.selectedNode = null;
        this.selectedLink = null;
        this.selectedNodeData = null;
        this.render();
      });

      container.addEventListener('dragover', this.handleDragOver);
      container.addEventListener('drop', this.handleDrop);
      container.addEventListener('mousemove', this.handleCanvasMouseMove);
      container.addEventListener('mouseup', this.handleCanvasMouseUp);

      this.render();
    },
    handleResize() {
      if (this.$refs.canvasContainer) {
        this.initCanvas();
      }
    },
    render() {
      if (!this.rootGroup) return;

      const root = this.rootGroup;
      const nodeWidth = 300;
      const nodeHeight = 60;

      root.selectAll('*').remove();

      const linkGroup = root.append('g').attr('class', 'links');
      const nodeGroup = root.append('g').attr('class', 'nodes');

      const getLinkPath = d => {
        const source = this.nodes.find(n => n.id === d.source);
        const target = this.nodes.find(n => n.id === d.target);
        if (!source || !target) return '';
        const x1 = source.x + nodeWidth / 2;
        const y1 = source.y + nodeHeight + 6;
        const x2 = target.x + nodeWidth / 2;
        const y2 = target.y - 8;
        const midY = (y1 + y2) / 2;
        return `M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}`;
      };

      linkGroup.selectAll('path.link-hit')
        .data(this.links)
        .enter()
        .append('path')
        .attr('class', 'link-hit')
        .attr('d', getLinkPath)
        .attr('stroke', 'transparent')
        .attr('stroke-width', 12)
        .attr('fill', 'none')
        .style('cursor', 'pointer')
        .style('pointer-events', 'stroke')
        .on('click', (event, d) => {
          event.stopPropagation();
          this.handleLinkClick(d);
        });

      linkGroup.selectAll('path.link')
        .data(this.links)
        .enter()
        .append('path')
        .attr('class', 'link')
        .attr('d', getLinkPath)
        .attr('stroke', d => this.getLinkKey(d) === this.selectedLink ? '#ef4444' : '#9ca3af')
        .attr('stroke-width', 2)
        .attr('fill', 'none')
        .attr('marker-end', 'url(#arrow-end)')
        .style('cursor', 'pointer')
        .style('pointer-events', 'none')
        .on('click', (event, d) => {
          event.stopPropagation();
          this.handleLinkClick(d);
        });

      if (this.tempLink) {
        const source = this.connectingNode;
        const x1 = source.x + nodeWidth / 2;
        const y1 = source.y + nodeHeight + 6;
        const x2 = this.tempLink.endX;
        const y2 = this.tempLink.endY;
        const midY = (y1 + y2) / 2;
        linkGroup.append('path')
          .attr('d', `M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}`)
          .attr('stroke', '#3b82f6')
          .attr('stroke-width', 2)
          .attr('stroke-dasharray', '5,5')
          .attr('fill', 'none');
      }

      const node = nodeGroup.selectAll('g.node')
        .data(this.nodes, d => d.id)
        .enter()
        .append('g')
        .attr('class', 'node')
        .attr('transform', d => `translate(${d.x}, ${d.y})`)
        .style('cursor', 'move')
        .on('click', (event, d) => {
          event.stopPropagation();
          this.handleNodeClick(d);
        });

      node.append('rect')
        .attr('width', nodeWidth)
        .attr('height', nodeHeight)
        .attr('rx', 8)
        .attr('ry', 8)
        .attr('fill', '#ffffff')
        .attr('stroke', d => d.id === this.selectedNode ? '#3b82f6' : '#e5e7eb')
        .attr('stroke-width', d => d.id === this.selectedNode ? 3 : 2)
        .attr('filter', 'url(#shadow)');

      node.append('circle')
        .attr('cx', 35)
        .attr('cy', nodeHeight / 2)
        .attr('r', 20)
        .attr('fill', '#3b82f6');

      node.append('text')
        .attr('x', 35)
        .attr('y', nodeHeight / 2 + 4)
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'middle')
        .attr('fill', '#fff')
        .style('font-size', '18px')
        .style('font-weight', 'bold')
        .text('📦');

      node.append('text')
        .attr('x', 70)
        .attr('y', 28)
        .attr('fill', '#1f2937')
        .style('font-size', '13px')
        .style('font-weight', '600')
        .text(d => {
          const t = d.title || d.name;
          return t.length > 32 ? t.substring(0, 32) + '...' : t;
        });

      node.append('text')
        .attr('x', 70)
        .attr('y', 50)
        .attr('fill', '#9ca3af')
        .style('font-size', '11px')
        .style('font-family', 'monospace')
        .text(d => {
          const s = d.subtitle || '';
          return s.length > 40 ? s.substring(0, 40) + '...' : s;
        });

      node.append('circle')
        .attr('class', 'port port-input')
        .attr('cx', nodeWidth / 2)
        .attr('cy', 0)
        .attr('r', 6)
        .attr('fill', 'white')
        .attr('stroke', '#3b82f6')
        .attr('stroke-width', 2)
        .style('cursor', 'crosshair')
        .on('mouseover', function () {
          d3.select(this).attr('r', 8).attr('fill', '#3b82f6');
        })
        .on('mouseout', function () {
          d3.select(this).attr('r', 6).attr('fill', 'white');
        })
        .on('mousedown', (event) => {
          event.preventDefault();
          event.stopPropagation();
        })
        .on('mouseup', (event, d) => {
          event.stopPropagation();
          if (this.tempLink && this.connectingNode && this.connectingNode.id !== d.id) {
            const exists = this.links.some(
              l => l.source === this.connectingNode.id && l.target === d.id
            );
            if (!exists) {
              this.links.push({
                id: `link-${Date.now()}`,
                source: this.connectingNode.id,
                target: d.id
              });
            }
            this.tempLink = null;
            this.connectingNode = null;
            this.render();
          }
        });

      node.append('circle')
        .attr('class', 'port port-output')
        .attr('cx', nodeWidth / 2)
        .attr('cy', nodeHeight)
        .attr('r', 6)
        .attr('fill', 'white')
        .attr('stroke', '#3b82f6')
        .attr('stroke-width', 2)
        .style('cursor', 'crosshair')
        .on('mouseover', function () {
          d3.select(this).attr('r', 8).attr('fill', '#3b82f6');
        })
        .on('mouseout', function () {
          d3.select(this).attr('r', 6).attr('fill', 'white');
        })
        .on('mousedown', (event, d) => {
          event.preventDefault();
          event.stopPropagation();
          this.connectingNode = d;
          this.tempLink = {
            endX: d.x + nodeWidth / 2,
            endY: d.y + nodeHeight
          };
        });

      const dragBehavior = d3.drag()
        .filter(event => {
          if (event.target && event.target.classList && event.target.classList.contains('port')) return false;
          return true;
        })
        .on('start', (event, d) => {
          this.isDragging = true;
          const transform = d3.zoomTransform(this.svg.node());
          const [x, y] = d3.pointer(event, this.rootGroup.node());
          d._dragOffsetX = x - d.x;
          d._dragOffsetY = y - d.y;
        })
        .on('drag', (event, d) => {
          const [x, y] = d3.pointer(event, this.rootGroup.node());
          d.x = Math.max(20, x - d._dragOffsetX);
          d.y = Math.max(20, y - d._dragOffsetY);
          this.updateNodePosition(d);
        })
        .on('end', (event, d) => {
          this.isDragging = false;
          delete d._dragOffsetX;
          delete d._dragOffsetY;
        });

      node.call(dragBehavior);
    },
    updateNodePosition(d) {
      if (!this.rootGroup) return;
      const nodeWidth = 300;
      const nodeHeight = 60;

      this.rootGroup.selectAll('g.node')
        .filter(n => n.id === d.id)
        .attr('transform', `translate(${d.x}, ${d.y})`);

      this.rootGroup.selectAll('path.link, path.link-hit')
        .filter(l => l.source === d.id || l.target === d.id)
        .attr('d', l => {
          const source = this.nodes.find(n => n.id === l.source);
          const target = this.nodes.find(n => n.id === l.target);
          if (!source || !target) return '';
          const x1 = source.x + nodeWidth / 2;
          const y1 = source.y + nodeHeight + 6;
          const x2 = target.x + nodeWidth / 2;
          const y2 = target.y - 8;
          const midY = (y1 + y2) / 2;
          return `M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}`;
        });
    },
    zoomIn() {
      if (this.svg && this.zoomBehavior) {
        this.svg.transition().duration(200).call(this.zoomBehavior.scaleBy, 1.3);
      }
    },
    zoomOut() {
      if (this.svg && this.zoomBehavior) {
        this.svg.transition().duration(200).call(this.zoomBehavior.scaleBy, 1 / 1.3);
      }
    },
    resetZoom() {
      if (this.svg && this.zoomBehavior) {
        this.svg.transition().duration(300).call(this.zoomBehavior.transform, d3.zoomIdentity);
      }
    },
    async handleNodeClick(d) {
      this.selectedNode = d.id;
      this.selectedLink = null;
      this.selectedNodeData = d;

      // 性能优化：仅通过 D3 更新选中状态的样式，不触发耗时的 render() 全量重绘
      if (this.rootGroup) {
        this.rootGroup.selectAll('g.node rect')
          .attr('stroke', n => n.id === this.selectedNode ? '#3b82f6' : '#e5e7eb')
          .attr('stroke-width', n => n.id === this.selectedNode ? 3 : 2);
        this.rootGroup.selectAll('path.link')
          .attr('stroke', '#9ca3af')
          .attr('stroke-width', 2);
      }

      // 重置配置
      this.selectedNodeConfig = {
        memory: '2G',
        cpu: '2',
        gpu: '0',
        rdma: '0',
        timeout: '0',
        retry: '0',
        skip: false
      };
      this.dynamicFormGroups = [];
      this.dynamicFormData = {};
      this.activeDynamicGroups = [];

      let nodeId = d.id.replace(/^node-/, '');
      this.configLoading = true;
      try {
        const res = await getTask(nodeId);
        if (res.status === 0) {
          const task = res.result;
          // 回显配置参数，对接实际反参字段
          this.selectedNodeConfig = {
            memory: task.resource_memory || '2G',
            cpu: task.resource_cpu || '2',
            gpu: task.resource_gpu || '0',
            rdma: task.resource_rdma || '0',
            timeout: String(task.timeout || '0'),
            retry: String(task.retry || '0'), // 接口字段为 retry
            skip: !!task.skip
          };

          // 映射字段：job_template.describe 为描述，label 为标签
          this.selectedNodeData.title = task.label || task.name || this.selectedNodeData.title;
          this.selectedNodeData.desc = (task.job_template && task.job_template.describe) || this.selectedNodeData.desc;
          this.selectedNodeData.nodeId = task.name || this.selectedNodeData.nodeId;

          // --- 动态表单解析逻辑 ---
          if (task.job_template && task.job_template.args) {
            try {
              const templateArgs = typeof task.job_template.args === 'string'
                ? JSON.parse(task.job_template.args)
                : task.job_template.args;

              const instanceArgs = task.args
                ? (typeof task.args === 'string' ? JSON.parse(task.args) : task.args)
                : {};

              const groups = [];
              const formData = {};
              const activeGroups = [];

              // 遍历模板中的分组
              Object.keys(templateArgs).forEach(groupName => {
                const groupFields = templateArgs[groupName];
                groups.push({
                  name: groupName,
                  fields: groupFields
                });
                activeGroups.push(groupName); // 默认展开

                // 初始化表单数据
                Object.keys(groupFields).forEach(fieldKey => {
                  const fieldDef = groupFields[fieldKey];
                  // 优先级：实例值 > 默认值
                  if (instanceArgs[fieldKey] !== undefined) {
                    formData[fieldKey] = instanceArgs[fieldKey];
                  } else {
                    formData[fieldKey] = fieldDef.default !== undefined ? fieldDef.default : '';
                  }
                });
              });

              this.dynamicFormGroups = groups;
              this.dynamicFormData = formData;
              this.activeDynamicGroups = activeGroups;
            } catch (e) {
              console.error('解析动态表单参数失败:', e);
            }
          }
        }
      } catch (error) {
        console.error('获取任务详情失败:', error);
      } finally {
        this.configLoading = false;
      }
    },
    handleLinkClick(link) {
      this.selectedLink = this.getLinkKey(link);
      this.selectedNode = null;
      this.selectedNodeData = null;

      if (this.rootGroup) {
        this.rootGroup.selectAll('g.node rect')
          .attr('stroke', '#e5e7eb')
          .attr('stroke-width', 1);
        this.rootGroup.selectAll('path.link')
          .attr('stroke', l => this.getLinkKey(l) === this.selectedLink ? '#ef4444' : '#9ca3af')
          .attr('stroke-width', 2);
      }
    },
    deleteSelectedLink() {
      if (!this.selectedLink) return;

      this.$confirm('确定要删除选中的连接线吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.links = this.links.filter(link => this.getLinkKey(link) !== this.selectedLink);
        this.selectedLink = null;
        this.render();
        this.$message.success('连接线已移除');
      }).catch(() => { });
    },
    async deleteSelectedNode() {
      if (!this.selectedNode) return;

      const node = this.nodes.find(n => n.id === this.selectedNode);
      if (!node) return;

      this.$confirm(`确定要删除节点 "${node.title || node.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        if (node.id && !String(node.id).startsWith('node-')) {
          this.canvasLoading = true;
          try {
            await deleteTask(node.id);
            this.$message.success('节点已从后端删除');
          } catch (error) {
            console.error('从后端删除节点失败:', error);
            this.$message.error('从后端删除节点失败');
            this.canvasLoading = false;
            return;
          } finally {
            this.canvasLoading = false;
          }
        }

        this.nodes = this.nodes.filter(n => n.id !== this.selectedNode);
        this.links = this.links.filter(
          l => l.source !== this.selectedNode && l.target !== this.selectedNode
        );
        this.selectedNode = null;
        this.selectedLink = null;
        this.selectedNodeData = null;
        this.render();
        this.$message.success('节点已移除');
      }).catch(() => { });
    },
    closeConfigPanel() {
      this.selectedNode = null;
      this.selectedLink = null;
      this.selectedNodeData = null;
      this.render();
    },
    async saveNodeConfig() {
      if (!this.selectedNodeData || !this.selectedNodeData.id) return;
      const nodeId = String(this.selectedNodeData.id).replace(/^node-/, '');
      if (!nodeId || nodeId === String(this.selectedNodeData.id) && String(this.selectedNodeData.id).startsWith('node-')) {
        this.$message.warning('该节点尚未保存，无法修改');
        return;
      }

      const payload = {
        resource_cpu: String(this.selectedNodeConfig.cpu || ''),
        resource_memory: String(this.selectedNodeConfig.memory || ''),
        resource_gpu: String(this.selectedNodeConfig.gpu || ''),
        resource_rdma: String(this.selectedNodeConfig.rdma || ''),
        timeout: String(this.selectedNodeConfig.timeout || '0'),
        retry: String(this.selectedNodeConfig.retry || '0'),
        skip: !!this.selectedNodeConfig.skip,
        args: JSON.stringify(this.dynamicFormData || {}),
        label: this.selectedNodeData.title || this.selectedNodeData.name || ''
      };

      this.saveNodeLoading = true;
      try {
        await updateTask(nodeId, payload);
        this.$message.success('节点配置已保存');
      } catch (error) {
        console.error('保存节点配置失败:', error);
        this.$message.error('保存节点配置失败');
      } finally {
        this.saveNodeLoading = false;
      }
    },
    clearCanvas() {
      this.$confirm('确定要清空画布吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.nodes = [];
        this.links = [];
        this.selectedNode = null;
        this.selectedLink = null;
        this.render();
        this.$message.success('画布已清空');
      }).catch(() => { });
    },
    async saveFlow() {
      const expand = [
        ...this.nodes.map(n => ({
          id: String(n.id),
          type: n.type || 'dataSet',
          position: { x: n.x, y: n.y },
          data: {
            info: { describe: n.desc || n.subtitle || '' },
            name: n.nodeId || n.name || '',
            label: n.title || n.name || ''
          }
        })),
        ...this.links.map(l => ({
          source: String(l.source),
          arrowHeadType: l.arrowHeadType || 'arrow',
          target: String(l.target),
          id: l.id || `edge-${l.source}-${l.target}`
        }))
      ];

      const idToName = {};
      this.nodes.forEach(n => {
        idToName[String(n.id)] = n.nodeId || n.name || String(n.id);
      });
      const dag = {};
      this.nodes.forEach(n => {
        const key = idToName[String(n.id)];
        if (key) dag[key] = { upstream: [] };
      });
      this.links.forEach(l => {
        const srcName = idToName[String(l.source)];
        const tgtName = idToName[String(l.target)];
        if (!srcName || !tgtName) return;
        if (!dag[tgtName]) dag[tgtName] = { upstream: [] };
        if (!dag[tgtName].upstream.includes(srcName)) {
          dag[tgtName].upstream.push(srcName);
        }
      });

      const payload = {
        id: this.$route.query.id,
        // name: this.flowName,
        // ...this.flowMeta,
        expand: JSON.stringify(expand),
        dag_json: JSON.stringify(dag)
      };

      this.canvasLoading = true;
      try {
        await updatePipeline(payload);
        this.$message.success('流程已保存');
      } catch (e) {
        console.error('保存流程失败:', e);
        this.$message.error('保存流程失败');
      } finally {
        this.canvasLoading = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.flow-builder-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  min-height: 600px;
  gap: 10px;
  overflow: hidden;
}

.flow-topbar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: white;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.flow-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.top-row {
  .title-cell {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .action-cell {
    display: flex;
    gap: 8px;
  }
}

.flow-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.back-btn {
  font-size: 16px;
  font-weight: 600;
  color: #3b82f6;
  padding: 0;
  margin-right: 4px;

  &:hover {
    color: #2563eb;
  }

  ::v-deep i {
    font-weight: bold;
    font-size: 18px;
  }
}

.title-setting {
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #3b82f6;
  }
}

.primary-btn {
  border-radius: 6px;
}

.flow-builder-content {
  display: flex;
  flex: 1;
  gap: 16px;
  min-height: 0;
  overflow: hidden;

  &.panel-collapsed {
    .task-panel {
      display: none;
    }
  }
}

.task-panel {
  width: 240px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  min-height: 0;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid #f3f4f6;

  .panel-actions {
    display: flex;
    gap: 8px;
  }
}

.action-icon {
  font-size: 16px;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #3b82f6;
  }
}

.panel-search {
  padding: 10px;
  border-bottom: 1px solid #f3f4f6;
}

.search-input {
  ::v-deep .el-input__inner {
    border-radius: 6px;
  }
}

.assets-info {
  .assets-count {
    font-size: 12px;
    color: #6b7280;
  }
}

.panel-body {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

.project-group {
  margin-bottom: 4px;
}

.project-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  background: #f9fafb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f3f4f6;
  }

  .project-name {
    font-size: 13px;
    font-weight: 600;
    color: #374151;
    margin-left: 10px;
  }

  .project-count {
    font-size: 11px;
    color: #9ca3af;
    margin-right: 4px;
  }

  .arrow-icon {
    font-size: 12px;
    color: #9ca3af;
  }
}

.task-list {
  padding: 4px 0;
}

.task-item {
  padding: 8px 10px;
  background: white;
  border: 1px solid #f3f4f6;
  border-radius: 6px;
  margin: 4px 0;
  cursor: grab;
  transition: all 0.2s;

  &:hover {
    border-color: #3b82f6;
    box-shadow: 0 1px 4px rgba(59, 130, 246, 0.12);
  }

  &:active {
    cursor: grabbing;
  }

  .task-name {
    font-size: 13px;
    font-weight: 600;
    color: #1f2937;
    line-height: 18px;
  }

  .task-desc {
    font-size: 11px;
    color: #9ca3af;
    line-height: 16px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-top: 2px;
  }
}

.canvas-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  min-width: 0;
  position: relative;
}

.canvas-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid #f3f4f6;
  background: #fafbfc;
}

.tb-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: #f3f4f6;
    color: #3b82f6;
  }

  &.is-disabled {
    opacity: 0.5;
    cursor: not-allowed;

    &:hover {
      background: transparent;
      color: #374151;
    }
  }

  i {
    font-size: 14px;
  }
}

.saved-status {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #10b981;

  i {
    font-size: 14px;
  }
}

.canvas-container {
  flex: 1;
  background: #f8fafc;
  background-image: radial-gradient(circle, #d1d5db 1px, transparent 1px);
  background-size: 20px 20px;
  overflow: hidden;
  min-height: 600px;
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;

  &:active {
    cursor: grabbing;
  }

  ::v-deep svg {
    display: block;

    text {
      user-select: none;
      -webkit-user-select: none;
      pointer-events: none;
    }
  }
}

.canvas-zoom-tools {
  position: absolute;
  bottom: 20px;
  left: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 10;

  ::v-deep .el-button+.el-button {
    margin-left: 0;
  }
}

@media (max-width: 992px) {
  .flow-topbar {
    .flow-row {
      flex-wrap: wrap;
    }
  }
}

.task-tooltip {
  position: fixed;
  z-index: 9999;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 16px;
  min-width: 240px;
  max-width: 320px;
  border-left: 4px solid #3b82f6;
}

.tooltip-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.tooltip-section {
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.tooltip-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.tooltip-value {
  font-size: 14px;
  color: #374151;
  word-break: break-word;
}

.config-panel {
  position: absolute;
  right: 0px;
  top: 50px;
  bottom: 0px;
  width: 360px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 100;
  overflow: hidden;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.config-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.config-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.close-icon {
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #3b82f6;
  }
}

.dynamic-form-container {
  margin-top: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;

  ::v-deep .el-collapse-item__header {
    padding: 0 12px;
    background-color: #f8f9fa;
    font-weight: 600;
    height: 40px;
    line-height: 40px;
  }

  ::v-deep .el-collapse-item__content {
    padding: 12px;
  }

  .config-field {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.config-actions-bar {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
}

.config-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 13px;
  color: #374151;
  background: #f9fafb;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f3f4f6;
    color: #3b82f6;
  }

  i {
    font-size: 14px;
  }
}

.config-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

.config-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.config-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.config-input-wrapper {
  margin-bottom: 8px;

  ::v-deep .el-input__inner {
    border-radius: 4px;
  }
}

.config-link {
  font-size: 12px;
  color: #3b82f6;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.config-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.config-field {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.config-field-label {
  font-size: 13px;
  color: #374151;
  margin-bottom: 8px;
}

.config-field-tip {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 4px;
  line-height: 1.5;
}

.config-switch-wrapper {
  display: flex;
  align-items: center;
}
</style>
