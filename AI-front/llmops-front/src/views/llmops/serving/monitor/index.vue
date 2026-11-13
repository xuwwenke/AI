<template>
  <div class="monitor-container">
    <div class="monitor-topbar">
      <el-button size="small" type="primary" plain class="pod-tag">Pod</el-button>
      <el-select
        v-model="selectedPod"
        size="small"
        filterable
        placeholder="选择 Pod"
        class="pod-select"
        @change="handlePodChange"
      >
        <el-option
          v-for="p in pods"
          :key="p.value"
          :label="p.label"
          :value="p.value"
        ></el-option>
      </el-select>
    </div>

    <div class="chart-card">
      <div class="chart-header">
        <span class="chart-title">Memory Usage</span>
        <span class="chart-max" @click="toggleMaxMemory">
          max <i :class="showMaxMemory ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
        </span>
      </div>
      <div
        ref="memChart"
        class="chart-box"
      ></div>
    </div>

    <div class="chart-card">
      <div class="chart-header">
        <span class="chart-title">CPU Usage</span>
        <span class="chart-max" @click="toggleMaxCpu">
          max <i :class="showMaxCpu ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
        </span>
      </div>
      <div
        ref="cpuChart"
        class="chart-box"
      ></div>
    </div>
  </div>
</template>

<script>
import echarts from 'echarts';
import { getInferenceMemory, getInferenceCpu, getTaskMonitor, getPipelineMonitor } from '@/api/llmops/monitor';
import * as k8s_api from '@/api/app/k8s';

const CHART_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];

export default {
  name: 'FlowBuilderMonitor',
  data() {
    return {
      id: '',
      mode: 'inference', // inference | task
      namespace: '',
      pipelineId: '',
      selectedPod: '',
      pods: [],
      memChartInstance: null,
      cpuChartInstance: null,
      memLoading: false,
      cpuLoading: false,
      showMaxMemory: true,
      showMaxCpu: false,
      memSeries: [],
      cpuSeries: [],
      xAxisData: [],
      timer: null
    };
  },
  watch: {
    '$route.query': {
      handler(query) {
        this.id = query.taskId || query.id || '';
        const typeMap = {
          'task': 'task',
          'pipeline': 'pipeline',
          'inference': 'inference'
        };
        this.mode = typeMap[query.type] || 'inference';
        this.namespace = query.namespace || '';
        this.pipelineId = query.pipelineId || query.id || '';
        this.bootstrap();
      },
      immediate: true,
      deep: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts();
    });
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    if (this.timer) clearInterval(this.timer);
    if (this.memChartInstance) this.memChartInstance.dispose();
    if (this.cpuChartInstance) this.cpuChartInstance.dispose();
  },
  methods: {
    async bootstrap() {
      if (this.mode === 'task') {
        await this.loadTaskPods();
      } else {
        this.pods = [];
        this.selectedPod = '';
      }
      this.fetchData(); // 这里会启动第一次加载并触发后续轮询
    },
    async loadTaskPods() {
      if (!this.id && !this.pipelineId) return;
      const ns = this.namespace || ((this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || 'public');
      this.namespace = ns;
      const clusterName = this.$store.state.project.currentCluster && this.$store.state.project.currentCluster.name;
      try {
        // 如果有 taskId (this.id)，优先按 task_id 查 Pod；否则按 pipeline-id 查
        const labelSelector = this.id ? `task-id=${this.id}` : `pipeline-id=${this.pipelineId}`;
        const res = await k8s_api.getContainerGroup(ns, clusterName, labelSelector);
        const items = (res && res.items) || [];
        this.pods = items.map(i => ({ label: i.metadata.name, value: i.metadata.name }));
        if (this.pods.length > 0) {
          if (!this.selectedPod || !this.pods.some(p => p.value === this.selectedPod)) {
            this.selectedPod = this.pods[0].value;
          }
        } else {
          this.selectedPod = '';
        }
      } catch (e) {
        console.error('获取任务 Pod 列表失败', e);
      }
    },
    handlePodChange() {
      this.fetchData();
    },
    toggleMaxMemory() {
      this.showMaxMemory = !this.showMaxMemory;
    },
    toggleMaxCpu() {
      this.showMaxCpu = !this.showMaxCpu;
    },
    initCharts() {
      if (this.$refs.memChart) {
        this.memChartInstance = echarts.init(this.$refs.memChart);
      }
      if (this.$refs.cpuChart) {
        this.cpuChartInstance = echarts.init(this.$refs.cpuChart);
      }
      this.updateCharts();
    },
    buildTimeAxis(values) {
      if (values && values.length > 0) {
        return values.map(item => {
          const t = new Date(item[0] * 1000); // 接口返回的是秒级时间戳
          const hh = String(t.getHours()).padStart(2, '0');
          const mm = String(t.getMinutes()).padStart(2, '0');
          return `${hh}:${mm}`;
        });
      }
      return [];
    },
    buildOption({ yLabel, formatter, series, xAxis, emptyText }) {
      const hasData = Array.isArray(series) && series.length > 0 && series.some(s => s.data && s.data.length > 0);
      const isSingle = series.length === 1;
      
      return {
        color: CHART_COLORS,
        legend: {
          show: hasData && series.length > 1,
          bottom: 0,
          left: 'center',
          textStyle: { color: '#6b7280', fontSize: 11 },
          icon: 'roundRect',
          itemWidth: 12,
          itemHeight: 4,
          padding: [5, 10]
        },
        grid: { 
          left: 80, 
          right: 30, 
          top: 30, 
          bottom: series.length > 1 ? 60 : 30 
        },
        tooltip: hasData
          ? {
              trigger: 'axis',
              backgroundColor: 'rgba(255, 255, 255, 0.96)',
              borderColor: '#e5e7eb',
              borderWidth: 1,
              textStyle: { color: '#374151' },
              extraCssText: 'box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);',
              formatter: (params) => {
                let res = `<div style="font-weight:600;margin-bottom:4px">${params[0].name}</div>`;
                params.forEach(p => {
                  res += `<div style="display:flex;justify-content:space-between;gap:20px">
                            <span>${p.marker} ${p.seriesName}</span>
                            <span style="font-weight:600">${formatter(p.value)}</span>
                          </div>`;
                });
                return res;
              }
            }
          : { show: false },
        xAxis: {
          type: 'category',
          data: xAxis,
          boundaryGap: false,
          axisLine: { lineStyle: { color: '#e5e7eb' } },
          axisLabel: { color: '#9ca3af', fontSize: 11 },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          name: yLabel,
          nameTextStyle: { color: '#9ca3af', fontSize: 12 },
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
          axisLabel: {
            color: '#6b7280',
            fontSize: 11,
            formatter
          }
        },
        graphic: hasData
          ? []
          : [
              {
                type: 'text',
                left: 'center',
                top: 'middle',
                style: {
                  text: emptyText || 'No data',
                  fill: '#9ca3af',
                  fontSize: 14
                }
              }
            ],
        series: series.map((s, idx) => {
          return {
            name: s.name,
            type: 'line',
            data: s.data,
            smooth: true,
            showSymbol: false,
            lineStyle: { 
              width: isSingle ? 2 : 2.5,
              shadowColor: 'rgba(0,0,0,0.05)',
              shadowBlur: 10,
              shadowOffsetY: 5
            },
            emphasis: {
              focus: 'series',
              lineStyle: { width: 3 }
            },
            areaStyle: isSingle ? {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(59, 130, 246, 0.25)' },
                { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
              ])
            } : null
          };
        })
      };
    },
    updateCharts() {
      if (this.memChartInstance) {
        this.memChartInstance.setOption(
          this.buildOption({
            yLabel: '',
            formatter: (v) => this.formatBytes(v),
            series: this.memSeries,
            xAxis: this.xAxisData,
            emptyText: 'No data'
          }),
          true
        );
      }
      if (this.cpuChartInstance) {
        this.cpuChartInstance.setOption(
          this.buildOption({
            yLabel: '',
            formatter: (v) => Number(v).toFixed(3),
            series: this.cpuSeries,
            xAxis: this.xAxisData,
            emptyText: 'No data'
          }),
          true
        );
      }
    },
    formatBytes(v) {
      if (v == null || isNaN(v)) return '0 B';
      const num = Number(v);
      if (num >= Math.pow(1024, 3)) return (num / Math.pow(1024, 3)).toFixed(2) + ' GB';
      if (num >= Math.pow(1024, 2)) return (num / Math.pow(1024, 2)).toFixed(2) + ' MB';
      if (num >= 1024) return (num / 1024).toFixed(2) + ' KB';
      return num + ' B';
    },
    extractValues(node, podName) {
      if (!node) return [];
      const data = node.data || node;
      if (data.result && Array.isArray(data.result)) {
        // 如果指定了 podName，只返回该 pod 的数据
        if (podName) {
          const match = data.result.find(r => r.metric && r.metric.pod === podName);
          if (match) return [{ name: match.metric.pod, values: match.values || [] }];
        }
        // 否则返回所有数据
        return data.result.map(r => ({
          name: (r.metric && r.metric.pod) || 'unknown',
          values: r.values || []
        }));
      }
      if (Array.isArray(node.values)) {
        return [{ name: 'default', values: node.values }];
      }
      return [];
    },
    async fetchData() {
      if (this.timer) {
        clearTimeout(this.timer);
        this.timer = null;
      }

      if (this.mode === 'task') {
        await this.fetchTaskData();
      } else if (this.mode === 'pipeline') {
        await this.fetchPipelineData();
      } else {
        await this.fetchInferenceData();
      }

      // 只有在组件未销毁时才继续下一次轮询
      if (!this._isBeingDestroyed && !this._isDestroyed) {
        this.timer = setTimeout(() => {
          this.fetchData();
        }, 5000);
      }
    },
    async fetchTaskData() {
      if (!this.id) {
        this.memSeries = [];
        this.cpuSeries = [];
        this.xAxisData = [];
        this.updateCharts();
        return;
      }
      try {
        const res = await getTaskMonitor(this.id);
        const payload = (res && (res.metrics || res.result || res)) || {};

        if (res.podNames && res.podNames.length > 0) {
          this.pods = res.podNames.map(name => ({ label: name, value: name }));
          if (!this.selectedPod || !res.podNames.includes(this.selectedPod)) {
            this.selectedPod = res.podNames[0];
          }
        }

        const memNode = payload.memory || payload.mem || {};
        const cpuNode = payload.cpu || {};
        
        const memRes = this.extractValues(memNode);
        const cpuRes = this.extractValues(cpuNode);

        this.memSeries = memRes.map(s => ({
          name: s.name,
          data: s.values.map(v => Number(v[1]))
        }));
        this.cpuSeries = cpuRes.map(s => ({
          name: s.name,
          data: s.values.map(v => Number(v[1]))
        }));

        const firstValues = (memRes[0] && memRes[0].values) || (cpuRes[0] && cpuRes[0].values) || [];
        this.xAxisData = this.buildTimeAxis(firstValues);
      } catch (e) {
        console.error('拉取任务监控数据失败', e);
      } finally {
        this.updateCharts();
      }
    },
    async fetchPipelineData() {
      if (!this.id) return;
      try {
        const res = await getPipelineMonitor(this.id);
        const payload = (res && (res.metrics || res.result || res)) || {};

        if (res.podNames && res.podNames.length > 0) {
          this.pods = res.podNames.map(name => ({ label: name, value: name }));
          if (!this.selectedPod || !res.podNames.includes(this.selectedPod)) {
            this.selectedPod = res.podNames[0];
          }
        }

        const memNode = payload.memory || payload.mem || {};
        const cpuNode = payload.cpu || {};
        
        const memRes = this.extractValues(memNode);
        const cpuRes = this.extractValues(cpuNode);

        this.memSeries = memRes.map(s => ({
          name: s.name,
          data: s.values.map(v => Number(v[1]))
        }));
        this.cpuSeries = cpuRes.map(s => ({
          name: s.name,
          data: s.values.map(v => Number(v[1]))
        }));

        const firstValues = (memRes[0] && memRes[0].values) || (cpuRes[0] && cpuRes[0].values) || [];
        this.xAxisData = this.buildTimeAxis(firstValues);
      } catch (e) {
        console.error('拉取 Pipeline 监控数据失败', e);
      } finally {
        this.updateCharts();
      }
    },
    async fetchInferenceData() {
      if (!this.id) return;
      try {
        const [memRes, cpuRes] = await Promise.all([
          getInferenceMemory(this.id),
          getInferenceCpu(this.id)
        ]);

        const podKeys = Object.keys(memRes.data || {});
        if (podKeys.length > 0) {
          this.pods = podKeys.map(key => ({ label: key, value: key }));
          if (!this.selectedPod || !podKeys.includes(this.selectedPod)) {
            this.selectedPod = podKeys[0];
          }
        }

        const memPodData = memRes.data && memRes.data[this.selectedPod];
        const cpuPodData = cpuRes.data && cpuRes.data[this.selectedPod];

        const memSeriesRaw = this.extractValues(memPodData);
        const cpuSeriesRaw = this.extractValues(cpuPodData);

        this.memSeries = memSeriesRaw.map(s => ({
          name: this.selectedPod,
          data: s.values.map(v => Number(v[1]))
        }));
        this.cpuSeries = cpuSeriesRaw.map(s => ({
          name: this.selectedPod,
          data: s.values.map(v => Number(v[1]))
        }));

        const firstValues = (memSeriesRaw[0] && memSeriesRaw[0].values) || (cpuSeriesRaw[0] && cpuSeriesRaw[0].values) || [];
        this.xAxisData = this.buildTimeAxis(firstValues);
      } catch (e) {
        console.error('拉取监控数据失败', e);
      } finally {
        this.updateCharts();
      }
    },
    handleResize() {
      if (this.memChartInstance) this.memChartInstance.resize();
      if (this.cpuChartInstance) this.cpuChartInstance.resize();
    }
  }
};
</script>

<style lang="scss" scoped>
.monitor-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.monitor-topbar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border-radius: 8px;
  padding: 10px 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.pod-tag {
  pointer-events: none;
}

.pod-select {
  width: 260px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.chart-max {
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
  user-select: none;

  i {
    margin-left: 2px;
    font-size: 12px;
  }
}

.chart-box {
  width: 100%;
  height: 260px;
}
</style>
