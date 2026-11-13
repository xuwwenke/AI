<template>
  <div class="training-detail-container">
    <div class="detail-header">
      <div class="header-left">
        <el-button type="text" icon="el-icon-back" class="back-btn" @click="$router.back()">返回</el-button>
        <span class="header-title" v-html="trainingInfo.name"></span>
      </div>
      <div class="header-right">
        <el-button size="small" type="primary"
          @click="handleResume">恢复</el-button>
        <el-button size="small" type="warning" @click="handlePause">暂停</el-button>
        <el-button size="small" @click="handleTerminate">终止</el-button>
      </div>
    </div>

    <div class="info-section">
      <div class="info-column">
        <div class="info-row">
          <span class="info-label">集群：</span>
          <span class="info-value">{{ trainingInfo.cluster }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">id：</span>
          <span class="info-value">{{ trainingInfo.id }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">状态：</span>
          <span class="info-value">{{ trainingInfo.status }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">消息：</span>
          <span class="info-value">{{ trainingInfo.message || '-' }}</span>
        </div>
      </div>

      <div class="info-column">
        <div class="info-row">
          <span class="info-label">创建时间：</span>
          <span class="info-value">{{ trainingInfo.createTime }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">开始时间：</span>
          <span class="info-value">{{ trainingInfo.startTime }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">结束时间：</span>
          <span class="info-value">{{ trainingInfo.endTime }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">run-id：</span>
          <span class="info-value">{{ trainingInfo.runId }}</span>
        </div>
      </div>

      <div class="info-column">
        <div class="info-row">
          <span class="info-label">创建人：</span>
          <span class="info-value">{{ trainingInfo.creator }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">执行人：</span>
          <span class="info-value">{{ trainingInfo.executor }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">进度：</span>
          <span class="info-value">{{ trainingInfo.progress }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">调度类型：</span>
          <span class="info-value">{{ trainingInfo.scheduleType }}</span>
        </div>
      </div>
    </div>

    <div class="graph-section">
      <div class="graph-toolbar">
        <el-tooltip content="放大" placement="top">
          <el-button size="mini" icon="el-icon-zoom-in" circle @click="zoomIn"></el-button>
        </el-tooltip>
        <el-tooltip content="缩小" placement="top">
          <el-button size="mini" icon="el-icon-zoom-out" circle @click="zoomOut"></el-button>
        </el-tooltip>
        <el-tooltip content="重置视图" placement="top">
          <el-button size="mini" icon="el-icon-refresh" circle @click="resetZoom"></el-button>
        </el-tooltip>
      </div>
      <div ref="dagContainer" class="dag-container"></div>
    </div>

    <el-drawer :visible.sync="drawerVisible" :with-header="false" :modal="false" size="640px" direction="rtl"
      append-to-body custom-class="node-detail-drawer">
      <div class="drawer-content" v-if="currentNode">
        <div class="drawer-header">
          <span class="drawer-title">节点详情</span>
          <i class="el-icon-close drawer-close" @click="drawerVisible = false"></i>
        </div>

        <el-tabs v-model="activeTab" class="drawer-tabs">
          <el-tab-pane v-for="tab in nodeDetailData" :key="tab.tabName" :label="tab.tabName" :name="tab.tabName">
            <div v-for="(group, gIdx) in tab.content" :key="gIdx" class="section-block">
              <div class="block-title">{{ group.groupName }}</div>

              <!-- HTML 类型 -->
              <div v-if="group.groupContent.type === 'html'" class="block-text" v-html="group.groupContent.value"></div>

              <!-- Map 类型 -->
              <div v-else-if="group.groupContent.type === 'map'" class="kv-table">
                <div v-for="(val, key) in group.groupContent.value" :key="key" class="kv-row">
                  <div class="kv-label">{{ key }}：</div>
                  <div class="kv-value">{{ val || '-' }}</div>
                </div>
              </div>

              <!-- ECharts 类型 -->
              <div v-else-if="group.groupContent.type === 'echart'" class="echart-wrapper">
                <e-chart-item :title="group.groupName" :option="parseChartOption(group.groupContent.value)" />
              </div>

              <!-- Text 类型 -->
              <div v-else class="block-text">
                <pre v-if="isJsonString(group.groupContent.value)" class="json-content">{{ formatJson(group.groupContent.value) }}</pre>
                <span v-else>{{ group.groupContent.value || '-' }}</span>
              </div>
            </div>

            <!-- 底部按钮 -->
            <div v-if="tab.bottomButton && tab.bottomButton.length > 0" class="bottom-actions">
              <el-button v-for="(btn, bIdx) in tab.bottomButton" :key="bIdx" size="mini" type="primary" plain
                @click="handleButtonClick(btn, tab)">
                {{ btn.text }}
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import * as d3 from 'd3';
import EChartItem from '@/components/EChartItem.vue';
import { pausePipeline, resumePipeline } from '@/api/llmops/orchestration';
import { stopWorkflow, getWorkflowDag, getWorkflowNodeDetail } from '@/api/llmops/instance';
import { chartOptions } from '@/utils/chartConfig';
import choosPodToWebssh from '@/utils/choosPodToWebssh';

export default {
  name: 'TrainingLog',
  components: {
    EChartItem
  },
  data() {
    return {
      trainingInfo: {
        name: '',
        cluster: '',
        id: '',
        status: '',
        message: '',
        createTime: '',
        startTime: '',
        endTime: '',
        runId: '',
        creator: '',
        executor: '',
        progress: '',
        scheduleType: ''
      },
      nodes: [],
      links: [],
      dagRaw: null,
      crdJson: null,
      svg: null,
      zoomBehavior: null,
      rootGroup: null,
      defaultScale: 0.65,
      drawerVisible: false,
      activeTab: 'io',
      currentNode: null,
      nodeDetailData: [], // 存储从 API 获取的节点详情
      chartInstances: {},
      chartOptions,
      stopId: '' // 用于终止接口的 ID
    };
  },
  computed: {
    nodeDetail() {
      const node = this.currentNode || {};
      const mounts = (node.volumeMounts || []).map(m => {
        const vol = (node.volumes || []).find(v => v.name === m.name);
        let host = '-';
        if (vol) {
          if (vol.persistentVolumeClaim) {
            host = `${vol.persistentVolumeClaim.claimName}(pvc)`;
          } else if (vol.hostPath) {
            host = `${vol.hostPath.path}(hostpath)`;
          } else if (vol.emptyDir) {
            host = vol.emptyDir.medium || 'EmptyDir';
          } else {
            host = vol.name;
          }
        }
        return { container: m.mountPath + (m.subPath ? `(subPath:${m.subPath})` : ''), host };
      });

      return {
        message: node.status === 'Skipped' ? "when 'false' evaluated false" : (node.message || ''),
        taskInfo: [
          { label: 'task id', value: node.taskId || '-' },
          { label: 'task name', value: node.taskName || '-' },
          { label: 'task label', value: node.taskLabel || '-', highlight: true },
          { label: 'task start', value: node.startTime || '-' },
          { label: 'task finish', value: node.endTime || '-' },
          { label: 'pod name', value: node.podName || '-' },
          { label: 'log path', value: node.logPath || '' },
          { label: 'metric path', value: node.metricKey || '' },
          { label: 'retry', value: node.retry != null ? node.retry : 0 },
          { label: 'node selector', value: node.nodeSelector || '-' },
          { label: 'resource cpu', value: node.resourceCpu || 0 },
          { label: 'resource memory', value: node.resourceMemory || 0 },
          { label: 'resource gpu', value: node.resourceGpu || 0 }
        ],
        mounts,
        json: node.raw ? JSON.stringify(node.raw, null, 2) : '-'
      };
    }
  },
  watch: {
    '$route.query.id': {
      handler: 'initData',
      immediate: true
    }
  },
  mounted() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    initData() {
      // 从路由参数中获取 ID
      const id = this.$route.query.id;
      const cluster = this.$route.query.cluster;

      if (id) {
        this.trainingInfo.id = id;
        this.trainingInfo.cluster = cluster || 'dev';
        this.fetchDag();
      }

      this.$nextTick(() => {
        this.renderGraph();
      });
    },
    async fetchDag() {
      const pipelineId = this.$route.query.id;
      if (!pipelineId) return;

      const cluster = this.$route.query.cluster
        || (this.$store && this.$store.state.project.currentCluster && this.$store.state.project.currentCluster.name)
        || 'dev';
      try {
        const res = await getWorkflowDag(cluster, pipelineId);
        const result = (res && (res.result || res.data || res)) || null;
        if (result) {
          this.applyDagResult(result);
        }
      } catch (error) {
        console.error('获取DAG详情失败', error);
      }
    },
    // ========== 核心修改：参考 RelationDiagram 的 preHandleNodes 逻辑 ==========
    applyDagResult(result) {
      this.dagRaw = result;
      const layout = result.layout || {};
      this.crdJson = layout.crd_json || null;

      // 提取终止 ID
      if (layout.right_button && layout.right_button.length > 0) {
        const stopUrl = layout.right_button[0].url;
        if (stopUrl) {
          const match = stopUrl.match(/\/stop\/(\d+)/);
          if (match && match[1]) {
            this.stopId = match[1];
          }
        }
      }

      // 更新基本信息
      const detailMap = {};
      (layout.detail || []).forEach(row => {
        (row || []).forEach(cell => {
          if (cell && cell.name != null) detailMap[cell.name] = cell.value;
        });
      });

      this.trainingInfo = {
        name: layout.title || layout['workflow-name'] || layout['pipeline-name'] || this.trainingInfo.name,
        cluster: detailMap.cluster || layout.cluster || this.trainingInfo.cluster,
        id: detailMap.id || layout['pipeline-id'] || this.trainingInfo.id,
        status: detailMap.status || layout.status || this.trainingInfo.status,
        message: detailMap.message || this.trainingInfo.message || '-',
        createTime: detailMap.create_time || layout.create_time || this.trainingInfo.createTime,
        startTime: detailMap.start_time || layout.start_time || this.trainingInfo.startTime,
        endTime: detailMap.finish_time || layout.finish_time || this.trainingInfo.endTime,
        runId: detailMap.run_id || layout['run-id'] || this.trainingInfo.runId,
        creator: detailMap.created_by || layout['pipeline-username'] || this.trainingInfo.creator,
        executor: detailMap.run_user || layout['run-username'] || this.trainingInfo.executor,
        progress: detailMap.progress || layout.progress || this.trainingInfo.progress,
        scheduleType: detailMap.schedule_type || layout.schedule_type || this.trainingInfo.scheduleType,
        describe: layout['pipeline-describe'] || ''
      };

      // ========== 关键修改：使用 preHandleNodes 算法处理多父节点 ==========
      const dag = Array.isArray(result.dag) ? result.dag : [];
      
      // 预处理节点，构建双向关系（核心算法）
      const processedData = this.preHandleDagNodes(dag);
      
      this.nodes = processedData.nodes;
      this.links = processedData.links;
      
      console.log('处理后的节点数量:', this.nodes.length);
      console.log('处理后的连线数量:', this.links.length);
      console.log('节点详情:', this.nodes.map(n => ({ 
        id: n.id, 
        title: n.title, 
        parentId: n.parentId,
        children: n.children 
      })));
      
      this.$nextTick(() => this.renderGraph());
    },
    
    /**
     * 预处理 DAG 节点，解决多父节点问题
     * 参考 RelationDiagram 的 preHandleNodes 算法
     */
    preHandleDagNodes(dagNodes) {
      // 用于存储处理后的节点
      const nodesMapByKey = new Map(); // key -> node
      const nodesMapByOriginalId = new Map(); // 原始 id -> node
      
      // 记录根节点的 key
      const rootKeys = [];
      
      // HTML 转义函数
      const htmlStrEnCode = (str) => {
        if (!str) return '';
        return `${str}`.replace(/[\u00A0-\u9999<>\-&:]/g, function (i) {
          return '&#' + i.charCodeAt(0) + ';';
        });
      };
      
      // 深度优先遍历，生成唯一 key
      const dfs = (nodes, level = 0, upItem = null, idPath = [], relativeKey = 'children') => {
        const childrenKey = relativeKey;
        const parentKey = relativeKey === 'children' ? 'parent' : 'children';
        const result = [];
        
        for (let i = 0; i < nodes.length; i++) {
          const node = nodes[i];
          idPath.push(`${i}`);
          
          const encodeKey = htmlStrEnCode(node.nid);
          let key = `node_${relativeKey}_${idPath.join('_')}`;
          
          // 如果已经存在相同的原始 id，使用已有的 key
          const nodeCacheById = nodesMapByOriginalId.get(node.nid);
          if (nodeCacheById) {
            key = nodeCacheById.key;
          }
          
          const nodeCache = nodesMapByKey.get(key);
          
          // 构建当前节点
          let tarNode = {
            ...node,
            key,
            level,
            relativeKey,
            [parentKey]: upItem ? [upItem] : [],
            [childrenKey]: [],
            originalNode: node
          };
          
          // 如果节点已存在，合并父节点（关键！不去重，而是合并）
          if (nodeCache) {
            const parentKeys = nodeCache[parentKey].map(p => p.key);
            const isParentExist = parentKeys.includes(upItem.key || '');
            
            if (isParentExist) {
              tarNode = nodeCache;
            } else {
              tarNode = {
                ...nodeCache,
                [parentKey]: upItem ? [upItem, ...nodeCache[parentKey]] : nodeCache[parentKey],
              };
            }
          }
          
          // 记录根节点 key
          if (level === 0 && upItem === null) {
            rootKeys.push(key);
          }
          
          // 递归处理子节点
          if (node[childrenKey] && node[childrenKey].length) {
            dfs(node[childrenKey] || [], level + 1, tarNode, idPath, relativeKey);
          }
          
          nodesMapByKey.set(tarNode.key, tarNode);
          nodesMapByOriginalId.set(tarNode.nid, tarNode);
          result.push(tarNode);
          
          idPath.pop();
        }
        return result;
      };
      
      // 从 children 方向处理
      dfs(dagNodes, 0, null, [], 'children');
      
      // 重建双向关系
      nodesMapByKey.forEach(item => {
        const parentItems = item.parent || [];
        for (let i = 0; i < parentItems.length; i++) {
          const parentItem = parentItems[i];
          const parentId = parentItem.key;
          const parentNode = nodesMapByKey.get(parentId);
          if (parentNode) {
            if (!parentNode.children.some(child => child.key === item.key)) {
              parentNode.children.push(item);
            }
            nodesMapByKey.set(parentId, parentNode);
          }
        }
        // 更新当前节点的 parent 引用为完整的节点对象
        item.parent = item.parent.map(p => nodesMapByKey.get(p.key)).filter(p => p);
        nodesMapByKey.set(item.key, item);
      });
      
      // 构建连线和平铺节点
      const linkSet = new Set();
      const links = [];
      const flatNodes = [];
      
      // 递归收集节点和连线
      const collectNodes = (node) => {
        if (!node) return;
        
        // 避免重复添加同一个 key 的节点（但这里 key 已经唯一）
        if (flatNodes.some(n => n.id === node.key)) return;
        
        // 生成显示标题
        let displayTitle = node.task_label || node.title || node.task_name || '';
        const baseTitle = displayTitle.split('(')[0].trim();
        if (node.retry && node.retry > 0 && !displayTitle.includes('重试')) {
          displayTitle = `${baseTitle}(有${node.retry}次重试机会)`;
        } else if (node.node_type === 'Pod' && node.title && node.title.includes('(')) {
          displayTitle = node.title;
        } else {
          displayTitle = baseTitle || node.title || node.task_name || '';
        }
        
        flatNodes.push({
          id: node.key,
          nid: node.nid,
          originalId: node.nid,
          parentId: node.parent && node.parent.length ? node.parent[0].key : null,
          parents: node.parent.map(p => p.key),
          children: node.children.map(c => c.key),
          title: displayTitle,
          subtitle: node.pod || node.name || '',
          status: (node.status && node.status.label) || 'Pending',
          nodeType: node.node_type,
          taskId: node.task_id,
          taskName: node.task_name,
          taskLabel: node.task_label,
          message: node.message,
          startTime: node.start_time,
          endTime: node.finish_time,
          podName: node.pod,
          nodeSelector: node.node_selector,
          resourceCpu: node.resource_cpu,
          resourceMemory: node.resource_memory,
          resourceGpu: node.resource_gpu,
          retry: node.retry,
          logPath: node.s3_key || '',
          metricKey: node.metric_key || '',
          outputKey: node.output_key || '',
          volumeMounts: node.volumeMounts || [],
          volumes: node.volumes || [],
          raw: node.originalNode || node
        });
        
        // 收集连线
        if (node.parent && node.parent.length) {
          node.parent.forEach(parent => {
            const linkKey = `${parent.key}->${node.key}`;
            if (!linkSet.has(linkKey)) {
              linkSet.add(linkKey);
              links.push({ source: parent.key, target: node.key });
            }
          });
        }
        
        // 递归处理子节点
        if (node.children && node.children.length) {
          node.children.forEach(child => collectNodes(child));
        }
      };
      
      // 从根节点开始收集
      rootKeys.forEach(key => {
        const rootNode = nodesMapByKey.get(key);
        if (rootNode) {
          collectNodes(rootNode);
        }
      });
      
      return { nodes: flatNodes, links };
    },
    
    handleResize() {
      if (this.$refs.dagContainer) {
        this.renderGraph();
      }
    },
    handleTerminate() {
      const id = this.stopId;
      if (!id) {
        this.$message.warning('未获取到WorkFlow ID');
        return;
      }
      this.$confirm('确定要终止该任务流吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await stopWorkflow(id);
          if (res) {
            this.$message.success('任务已终止');
            this.trainingInfo.status = 'terminated';
            // 刷新详情
            setTimeout(() => {
              this.fetchDag();
            }, 1000);
          } else {
            this.$message.error(res.message || '终止失败');
          }
        } catch (error) {
          console.error('终止失败:', error);
          this.$message.error('终止失败');
        }
      }).catch(() => { });
    },
    async handlePause() {
      const id = this.$route.query.id || this.trainingInfo.id;
      if (!id) return;
      this.$confirm('确定要暂停该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await pausePipeline(id);
          if (res) {
            this.trainingInfo.status = 'paused';
            this.$message.success('任务已暂停');
          } else {
            this.$message.error(res.message || '暂停失败');
          }
        } catch (error) {
          console.error('暂停失败:', error);
          this.$message.error('暂停失败');
        }
      }).catch(() => { });
    },
    async handleResume() {
      const id = this.$route.query.id || this.trainingInfo.id;
      if (!id) return;
      try {
        const res = await resumePipeline(id);
        if (res) {
          this.trainingInfo.status = 'running';
          this.$message.success('任务已恢复');
        } else {
          this.$message.error(res.message || '恢复失败');
        }
      } catch (error) {
        console.error('恢复失败:', error);
        this.$message.error('恢复失败');
      }
    },
    handleViewWorkflow() {
      this.$message.info('跳转任务流');
    },
    statusColor(status) {
      switch (status) {
        case 'Succeeded': return '#10b981';
        case 'Failed': return '#ef4444';
        case 'Running': return '#3b82f6';
        case 'Skipped': return '#6b7280';
        case 'Pending': return '#9ca3af';
        default: return '#9ca3af';
      }
    },
    statusIcon(status) {
      switch (status) {
        case 'Succeeded': return '✓';
        case 'Failed': return '✕';
        case 'Running': return '●';
        case 'Skipped': return '○';
        case 'Pending': return '⋯';
        default: return '⋯';
      }
    },
    renderGraph() {
      const container = this.$refs.dagContainer;
      if (!container || !this.nodes.length) return;

      d3.select(container).selectAll('*').remove();

      const width = container.clientWidth;
      const height = container.clientHeight || 500;

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
        .attr('refX', 9)
        .attr('refY', 5)
        .attr('markerWidth', 6)
        .attr('markerHeight', 6)
        .attr('orient', 'auto-start-reverse')
        .append('path')
        .attr('d', 'M 0 0 L 10 5 L 0 10 z')
        .attr('fill', '#94a3b8');

      const root = svg.append('g').attr('class', 'dag-root');
      this.svg = svg;
      this.rootGroup = root;

      const zoomBehavior = d3.zoom()
        .scaleExtent([0.1, 3])
        .on('zoom', (event) => {
          root.attr('transform', event.transform);
        });
      this.zoomBehavior = zoomBehavior;

      svg.call(zoomBehavior);
      svg.on('dblclick.zoom', null);

      // 节点尺寸
      const nodeWidth = 320;
      const nodeHeight = 64;
      const gapX = 80;
      const gapY = 50;
      const startY = 40;

      // 构建节点Map
      const nodeMap = new Map();
      this.nodes.forEach(n => nodeMap.set(n.id, n));
      
      // 计算每个节点的深度（从根节点开始）
      const depthMap = new Map();
      
      const computeDepth = (nodeId, visited = new Set()) => {
        if (visited.has(nodeId)) return 0;
        visited.add(nodeId);
        
        if (depthMap.has(nodeId)) return depthMap.get(nodeId);
        
        const node = nodeMap.get(nodeId);
        if (!node) return 0;
        
        // 如果有父节点，深度 = 父节点深度 + 1
        if (node.parentId && node.parentId !== 'null' && node.parentId !== null) {
          const parentDepth = computeDepth(node.parentId, visited);
          const depth = parentDepth + 1;
          depthMap.set(nodeId, depth);
          return depth;
        }
        
        depthMap.set(nodeId, 0);
        return 0;
      };
      
      this.nodes.forEach(node => {
        computeDepth(node.id);
      });
      
      // 按深度分组
      const columns = new Map();
      this.nodes.forEach(node => {
        const depth = depthMap.get(node.id) || 0;
        if (!columns.has(depth)) {
          columns.set(depth, []);
        }
        columns.get(depth).push(node);
      });
      
      // 计算位置
      const positioned = [];
      const sortedDepths = Array.from(columns.keys()).sort((a, b) => a - b);
      
      sortedDepths.forEach(depth => {
        const colNodes = columns.get(depth);
        // 按标题排序
        colNodes.sort((a, b) => a.title.localeCompare(b.title));
        
        colNodes.forEach((node, idx) => {
          positioned.push({
            ...node,
            x: 40 + depth * (nodeWidth + gapX),
            y: startY + idx * (nodeHeight + gapY)
          });
        });
      });
      
      const positionMap = positioned.reduce((acc, n) => {
        acc[n.id] = n;
        return acc;
      }, {});
      
      // 绘制连线
      const linkGroup = root.append('g').attr('class', 'links');
      linkGroup.selectAll('path')
        .data(this.links)
        .enter()
        .append('path')
        .attr('d', d => {
          const s = positionMap[d.source];
          const t = positionMap[d.target];
          if (!s || !t) return '';
          const x1 = s.x + nodeWidth;
          const y1 = s.y + nodeHeight / 2;
          const x2 = t.x;
          const y2 = t.y + nodeHeight / 2;
          const dx = x2 - x1;
          const curvature = 0.5;
          const hx1 = x1 + dx * curvature;
          const hx2 = x2 - dx * curvature;
          return `M ${x1} ${y1} C ${hx1} ${y1}, ${hx2} ${y2}, ${x2} ${y2}`;
        })
        .attr('stroke', '#94a3b8')
        .attr('stroke-width', 1.5)
        .attr('fill', 'none')
        .attr('marker-end', 'url(#arrow-end)');

      // 绘制节点
      const nodeGroup = root.append('g').attr('class', 'nodes');
      const node = nodeGroup.selectAll('g.node')
        .data(positioned)
        .enter()
        .append('g')
        .attr('class', 'node')
        .attr('transform', d => `translate(${d.x}, ${d.y})`)
        .style('cursor', 'pointer')
        .on('click', (event, d) => {
          event.stopPropagation();
          this.handleNodeClick(d);
        });

      // 背景矩形
      node.append('rect')
        .attr('width', nodeWidth)
        .attr('height', nodeHeight)
        .attr('rx', 6)
        .attr('ry', 6)
        .attr('fill', '#ffffff')
        .attr('stroke', d => this.statusColor(d.status))
        .attr('stroke-width', 1.5);

      // 左侧状态图标
      const iconG = node.append('g').attr('transform', `translate(16, ${nodeHeight / 2})`);
      iconG.append('circle')
        .attr('r', 12)
        .attr('fill', d => this.statusColor(d.status));
      
      iconG.append('text')
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'central')
        .attr('fill', '#ffffff')
        .style('font-size', '12px')
        .style('font-weight', 'bold')
        .text(d => this.statusIcon(d.status));

      // 文本信息
      const textG = node.append('g').attr('transform', 'translate(40, 0)');
      
      textG.append('text')
        .attr('y', 26)
        .attr('fill', '#1e293b')
        .style('font-size', '13px')
        .style('font-weight', '600')
        .text(d => {
          const maxLen = 22;
          return d.title.length > maxLen ? d.title.substring(0, maxLen) + '...' : d.title;
        });

      textG.append('text')
        .attr('y', 46)
        .attr('fill', '#64748b')
        .style('font-size', '11px')
        .text(d => {
          const sub = d.subtitle || d.id;
          const maxLen = 30;
          return sub.length > maxLen ? sub.substring(0, maxLen) + '...' : sub;
        });

      // 右上角状态标签
      const badgeG = node.append('g')
        .attr('transform', `translate(${nodeWidth - 70}, 8)`);

      badgeG.append('rect')
        .attr('width', 62)
        .attr('height', 18)
        .attr('rx', 9)
        .attr('fill', d => this.statusColor(d.status) + '15')
        .attr('stroke', d => this.statusColor(d.status))
        .attr('stroke-width', 0.5);

      badgeG.append('text')
        .attr('x', 31)
        .attr('y', 9)
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'central')
        .attr('fill', d => this.statusColor(d.status))
        .style('font-size', '10px')
        .style('font-weight', '500')
        .text(d => d.status);

      // 初始化视图
      const maxDepth = sortedDepths.length;
      const totalWidth = maxDepth * (nodeWidth + gapX) + 100;
      const initialScale = Math.min(0.8, width / totalWidth) || 0.65;
      const initialTransform = d3.zoomIdentity.translate(40, 40).scale(initialScale);
      svg.call(zoomBehavior.transform, initialTransform);
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
        const container = this.$refs.dagContainer;
        if (container) {
          const width = container.clientWidth;
          const nodeWidth = 320;
          const gapX = 80;
          // 计算最大深度
          let maxDepth = 0;
          this.nodes.forEach(node => {
            const depth = node.parentId ? 1 : 0; // 简化计算
            if (depth > maxDepth) maxDepth = depth;
          });
          const totalWidth = (maxDepth + 2) * (nodeWidth + gapX) + 100;
          const initialScale = Math.min(0.8, width / totalWidth) || 0.65;
          const resetTransform = d3.zoomIdentity.translate(40, 40).scale(initialScale);
          this.svg.transition().duration(300).call(this.zoomBehavior.transform, resetTransform);
        } else {
          this.svg.transition().duration(300).call(this.zoomBehavior.transform, d3.zoomIdentity);
        }
      }
    },
    handleNodeClick(node) {
      this.currentNode = node;
      this.nodeDetailData = [];
      this.activeTab = '';
      this.drawerVisible = true;

      const pipelineArgoId = this.$route.query.pipelineArgoId;
      const nodeId = node.nid;
      if (pipelineArgoId && nodeId) {
        this.fetchNodeDetail( pipelineArgoId, nodeId);
      }
    },
    async fetchNodeDetail(pipelineArgoId, nodeId) {
      try {
        const res = await getWorkflowNodeDetail(pipelineArgoId, nodeId);
        if (res && res.result && res.result.detail) {
          this.nodeDetailData = res.result.detail;
          if (this.nodeDetailData.length > 0) {
            this.activeTab = this.nodeDetailData[0].tabName;
          }
        }
      } catch (error) {
        console.error('获取节点详情失败:', error);
      }
    },
    parseChartOption(value) {
      if (!value) return {};
      if (typeof value === 'object') return value;
      try {
        // 使用 new Function 解析 JS 对象字符串，支持函数、注释等
        // eslint-disable-next-line no-new-func
        return new Function(`return ${value}`)();
      } catch (e) {
        console.error('解析图表配置失败:', e);
        // 降级尝试 JSON.parse
        try {
          return JSON.parse(value);
        } catch (jsonErr) {
          return {};
        }
      }
    },
    isJsonString(str) {
      if (typeof str !== 'string') return false;
      try {
        const obj = JSON.parse(str);
        return !!(typeof obj === 'object' && obj);
      } catch (e) {
        return false;
      }
    },
    formatJson(str) {
      try {
        return JSON.stringify(JSON.parse(str), null, 2);
      } catch (e) {
        return str;
      }
    },
    getTaskId(tab) {
      if (!tab || !tab.content) return null;
      const taskGroup = tab.content.find(g => g.groupName === '任务详情');
      if (taskGroup && taskGroup.groupContent && taskGroup.groupContent.value) {
        return taskGroup.groupContent.value['task id'] || taskGroup.groupContent.value['taskId'];
      }
      return null;
    },
    handleButtonClick(btn, tab) {
      if (btn.text === '资源使用') {
        const taskId = this.getTaskId(tab);
        if (taskId) {
          this.$router.push({
            path: '/llmops/serving/monitor',
            query: {
              id: taskId,
              type: 'task',
              namespace: this.trainingInfo.namespace || 'public'
            }
          });
          return;
        }
      }

      if (btn.url) {
        // 参考 flowBuilder 的逻辑进行跳转
        const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || 'public';
        const clusterName = (this.$store.state.project.currentCluster && this.$store.state.project.currentCluster.name) || 'default';
        const taskGroup = tab.content.find(g => g.groupName === '任务详情');
        const taskInfo = taskGroup ? taskGroup.groupContent.value : {};
        const podName = taskInfo['pod name'] || taskInfo['podName'];

        if (btn.text.includes('在线日志') || btn.text.includes('在线调试')) {
          const type = btn.text.includes('在线日志') ? 'log' : 'console';
          if (podName) {
            const { href } = this.$router.resolve({
              path: `/webssh/${type}/${namespace}/${podName}`,
              query: {
                proName: namespace,
                clusterName: clusterName
              }
            });
            window.open(href, '_blank');
          } else {
            this.$message.warning('未找到 Pod 名称，无法打开终端');
          }
          return;
        }

        if (btn.text.includes('相关容器')) {
          const serviceName = this.trainingInfo.name; // flowName
          const taskGroup = tab.content.find(g => g.groupName === '任务详情');
          const taskInfo = taskGroup ? taskGroup.groupContent.value : {};
          const taskName = taskInfo['task name'] || taskInfo['taskName'];
          const pipelineId = this.$route.query.id;
          
          let labelSelector = `pipeline-id=${pipelineId}`;
          if (taskName) {
            labelSelector += `,task-name=${taskName}`;
          }

          choosPodToWebssh(
            {
              serviceName: serviceName,
              namespace: namespace,
              type: 'console',
              labelSelector: labelSelector
            },
            true
          );
          return;
        }

        if (btn.url.startsWith('http')) {
          window.open(btn.url, '_blank');
        } else {
          // 处理内部路由跳转或特殊 URL
          const { href } = this.$router.resolve({
            path: btn.url
          });
          window.open(href, '_blank');
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.training-detail-container {
  display: flex;
  flex-direction: column;
  min-height: auto;
  padding: 0;
  gap: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  padding: 12px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
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

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }

  .header-right {
    display: flex;
    gap: 8px;
  }
}

.info-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  background: #fff;
  border-radius: 10px;
  padding: 18px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.info-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  font-size: 13px;
  line-height: 22px;

  .info-label {
    color: #6b7280;
    min-width: 80px;
    flex-shrink: 0;
  }

  .info-value {
    color: #1f2937;
    word-break: break-all;
  }
}

.graph-section {
  position: relative;
  flex: 1;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  min-height: 500px;
  overflow: hidden;
}

.graph-toolbar {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  display: flex;
  gap: 6px;
}

.dag-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }

  ::v-deep svg {
    display: block;
  }
}

@media (max-width: 992px) {
  .info-section {
    grid-template-columns: 1fr;
  }
}
</style>

<style lang="scss">
.node-detail-drawer {
  box-shadow: -8px 0 24px rgba(0, 0, 0, 0.08) !important;

  .el-drawer__body {
    padding: 0;
    overflow: hidden;
  }
}

.drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #f3f4f6;

  .drawer-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }

  .drawer-close {
    font-size: 18px;
    color: #9ca3af;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #374151;
    }
  }
}

.drawer-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  >.el-tabs__header {
    margin: 0;
    padding: 0 24px;
    border-bottom: 1px solid #f3f4f6;

    .el-tabs__nav-wrap::after {
      display: none;
    }

    .el-tabs__item {
      font-size: 14px;
      color: #6b7280;
      height: 44px;
      line-height: 44px;

      &.is-active {
        color: #1f2937;
        font-weight: 600;
      }
    }

    .el-tabs__active-bar {
      background-color: #6366f1;
      height: 2px;
    }
  }

  >.el-tabs__content {
    flex: 1;
    overflow-y: auto;
    padding: 20px 24px;
  }
}

.section-block {
  margin-bottom: 24px;

  .block-title {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    padding-left: 8px;
    border-left: 3px solid #6366f1;
    margin-bottom: 12px;
  }

  .block-text {
    font-size: 13px;
    color: #4b5563;
    line-height: 1.6;
    padding: 4px 0;
  }
}

.kv-table {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 14px 16px;
}

.kv-row {
  display: flex;
  font-size: 13px;
  line-height: 28px;

  .kv-label {
    color: #374151;
    font-weight: 600;
    min-width: 130px;
    flex-shrink: 0;
  }

  .kv-value {
    color: #4b5563;
    word-break: break-all;

    &.is-link {
      color: #f59e0b;
    }
  }
}

.mount-table {
  background-color: #f9fafb;
  border-radius: 8px;
  overflow: hidden;
}

.mount-row {
  display: grid;
  grid-template-columns: 200px 1fr;
  font-size: 13px;
  padding: 10px 16px;

  &.mount-head {
    background-color: #f3f4f6;
    font-weight: 600;
    color: #374151;
  }

  &+& {
    border-top: 1px solid #f3f4f6;
  }

  .mount-cell {
    color: #374151;
    word-break: break-all;
  }

  .mount-host {
    color: #4b5563;
  }
}

.empty-tab {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 40px 0;
}

.visual-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.bottom-actions {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .echart-wrapper {
    margin-bottom: 20px;
    background: #f9fafb;
    border-radius: 8px;
    overflow: hidden;
  }

</style>