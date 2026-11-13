<template>
  <div class="large-screen">
    <!-- 背景图 -->
    <div class="dashboard-bg"></div>
    <!-- Canvas粒子背景 -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>
    <!-- 扫描线动效 -->
    <div class="scan-line"></div>
    <!-- 顶部光晕 -->
    <div class="top-glow"></div>

    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="top-left">
        <span class="time-highlight">{{ currentTime }}</span>
        <span class="separator">|</span>
        <span class="normal-text">{{ currentDate }}</span>
        <span class="separator">|</span>
        <span class="normal-text">{{ currentWeekday }}</span>
        <span class="separator">|</span>
        <span class="weather-text">☁ 多云 22℃</span>
      </div>
      <div class="top-center">
        <div class="title-wrap">
          <div class="title-corner tl"></div>
          <div class="title-corner tr"></div>
          <div class="title-corner bl"></div>
          <div class="title-corner br"></div>
          <div class="main-title">AI模型迭代指挥平台</div>
        </div>
        <div class="deco-line">
          <span class="deco-diamond"></span>
          <span class="deco-seg"></span>
          <span class="deco-dot"></span>
          <span class="deco-seg long"></span>
          <span class="deco-dot"></span>
          <span class="deco-seg"></span>
          <span class="deco-diamond"></span>
        </div>
      </div>
      <div class="top-right">
        <button class="nav-btn">模型库</button>
        <button class="nav-btn">训练任务</button>
        <button class="nav-btn">推理服务</button>
        <button class="nav-btn fullscreen-btn" @click="toggleFullscreen">
          {{ isFullscreen ? '⛶ 退出全屏' : '⛶ 进入全屏' }}
        </button>
      </div>
    </div>

    <!-- 概览指标卡 -->
    <div class="overview-row">
      <div v-for="(item, idx) in overviewData" :key="idx" class="overview-card"
        :style="{ '--c': item.color, '--delay': idx * 0.1 + 's' }">
        <div class="ov-icon">{{ item.icon }}</div>
        <div class="ov-info">
          <div class="ov-label">{{ item.label }}</div>
          <div class="ov-value" :key="item.value">{{ item.value }}</div>
        </div>
        <div class="ov-trend" :class="{ up: item.trend > 0, down: item.trend < 0 }">
          {{ item.trend > 0 ? '↑' : '↓' }}{{ Math.abs(item.trend) }}%
        </div>
        <!-- 流光边框 -->
        <div class="card-shine"></div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="body-container">
      <!-- 左侧面板 -->
      <div class="side-panel left">
        <div class="panel-card">
          <div class="panel-title"><span class="title-icon">◈</span> 模型版本分布</div>
          <div ref="chartVersion" class="chart-container chart-large"></div>
          <div class="panel-water-ripple"></div>
        </div>
        <div class="panel-card">
          <div class="panel-title"><span class="title-icon">◈</span> 训练任务趋势</div>
          <div ref="chartTraining" class="chart-container chart-large"></div>
          <div class="panel-water-ripple"></div>
        </div>
        <div class="panel-card">
          <div class="panel-title"><span class="title-icon">◈</span> 数据集来源占比</div>
          <div ref="chartDataset" class="chart-container chart-large"></div>
          <div class="panel-water-ripple"></div>
        </div>
      </div>

      <!-- 中心 2.5D 场景 -->
      <div class="center-stage">
        <div class="scene-container" ref="sceneContainer">
          <!-- 背景网格透视 -->
          <div class="perspective-grid"></div>

          <!-- 地面光晕 -->
          <div class="ground-glow"></div>

          <!-- 等轴测地台 -->
          <div class="iso-platform">
            <div class="platform-top"></div>
            <div class="platform-left"></div>
            <div class="platform-right"></div>
          </div>

          <!-- 多层光晕圆环 -->
          <div class="halo-ring halo-1"></div>
          <div class="halo-ring halo-2"></div>
          <div class="halo-ring halo-3"></div>
          <div class="halo-ring halo-4"></div>

          <!-- 中心核心 -->
          <div class="central-core">
            <div class="core-outer"></div>
            <div class="core-mid"></div>
            <div class="core-inner"></div>
            <div class="core-pulse"></div>
            <div class="core-label">AI CORE</div>
          </div>

          <!-- SVG 链路层 -->
          <svg class="links-svg" :viewBox="'0 0 ' + containerW + ' ' + containerH" preserveAspectRatio="xMidYMid meet">
            <defs>
              <filter id="glow-filter">
                <feGaussianBlur stdDeviation="3" result="blur" />
                <feMerge>
                  <feMergeNode in="blur" />
                  <feMergeNode in="SourceGraphic" />
                </feMerge>
              </filter>
              <filter id="glow-heavy">
                <feGaussianBlur stdDeviation="6" result="blur" />
                <feMerge>
                  <feMergeNode in="blur" />
                  <feMergeNode in="SourceGraphic" />
                </feMerge>
              </filter>
              <linearGradient v-for="(node, i) in nodeConfigs" :key="'grad' + i" :id="'lg' + i" x1="0%" y1="0%"
                x2="100%" y2="0%">
                <stop offset="0%" :stop-color="node.hex" stop-opacity="0.1" />
                <stop offset="50%" :stop-color="node.hex" stop-opacity="0.9" />
                <stop offset="100%" :stop-color="nodeConfigs[(i + 1) % 7].hex" stop-opacity="0.1" />
              </linearGradient>
            </defs>
            <!-- 链路线 -->
            <path v-for="i in 7" :key="'link-' + i" :d="getLinkPath(i)" fill="none" :stroke="'url(#lg' + (i - 1) + ')'"
              stroke-width="3" stroke-dasharray="8,6" filter="url(#glow-filter)">
              <animate attributeName="stroke-dashoffset" from="0" to="-28" dur="1.2s" repeatCount="indefinite" />
            </path>
            <!-- 流动光点 - 增大尺寸 -->
            <circle v-for="i in 7" :key="'dot-' + i" r="6" :fill="nodeConfigs[i - 1].hex" filter="url(#glow-heavy)"
              opacity="0.95">
              <animateMotion :dur="(2 + i * 0.25) + 's'" repeatCount="indefinite" :path="getLinkPath(i)"
                rotate="auto" />
            </circle>
          </svg>

          <!-- 7 个节点 -->
          <div v-for="(node, idx) in nodeConfigs" :key="'node-' + idx" class="scene-node"
            :class="{ hover: activeNode === idx || highlightIndex === idx }" :style="getNodeStyle(idx)"
            @mouseenter="activeNode = idx" @mouseleave="activeNode = -1">
            <!-- 光柱 - 增强 -->
            <div class="node-beam" :style="{ '--c': node.hex }">
              <div class="beam-core"></div>
            </div>


            <!-- 图标区 -->
            <div class="node-icon-wrap" :style="{ '--c': node.hex }">
              <div class="node-glow"></div>
              <img :src="node.img" class="node-image" :alt="node.name" />
            </div>
            <!-- 底座 -->
            <div class="node-base-platform" :style="{ '--c': node.hex }">
              <div class="base-top"></div>
              <div class="base-side-l"></div>
              <div class="base-side-r"></div>
            </div>
          </div>

          <!-- 链路标注 -->
          <div v-for="(label, idx) in connLabels" :key="'label' + idx" class="conn-label"
            :style="getConnLabelStyle(idx)">
            {{ label }}
          </div>

          <!-- 增强粒子效果 -->
          <div v-for="i in 40" :key="'particle-' + i" class="floating-particle" :style="getParticleStyle(i)"></div>

          <!-- 节点详情弹窗 -->
          <transition name="fade">
            <div v-if="activeNode >= 0" class="node-tooltip" :style="getTooltipStyle(activeNode)">
              <div class="tooltip-corner tl"></div>
              <div class="tooltip-corner tr"></div>
              <div class="tooltip-corner bl"></div>
              <div class="tooltip-corner br"></div>
              <div class="tooltip-header" :style="{ '--c': nodeConfigs[activeNode].hex }">
                {{ nodeConfigs[activeNode].name }}
              </div>
              <div class="tooltip-body">{{ nodeConfigs[activeNode].desc }}</div>
            </div>
          </transition>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="side-panel right">
        <div class="panel-card">
          <div class="panel-title"><span class="title-icon">◈</span> 实时推理监控</div>
          <div ref="chartInference" class="chart-container chart-large"></div>
          <div class="panel-water-ripple"></div>
        </div>
        <div class="panel-card">
          <div class="panel-title"><span class="title-icon">◈</span> 模型性能雷达</div>
          <div ref="chartRadar" class="chart-container chart-large"></div>
          <div class="panel-water-ripple"></div>
        </div>
        <div class="panel-card">
          <div class="panel-title"><span class="title-icon">◈</span> 异常检测列表</div>
          <div class="list-header">
            <span>时间</span><span>模型</span><span>类型</span><span>级别</span><span>状态</span>
          </div>
          <div class="list-container" ref="anomalyList" @mouseenter="pauseScroll = true"
            @mouseleave="pauseScroll = false">
            <div v-for="(a, idx) in anomalies" :key="idx" class="anomaly-row" :class="'lvl-' + a.level">
              <span class="anomaly-time">{{ a.time }}</span>
              <span class="anomaly-model">{{ a.model }}</span>
              <span class="anomaly-type">{{ a.type }}</span>
              <span class="anomaly-level"
                :style="{ color: getLevelColor(a.level), textShadow: '0 0 12px ' + getLevelColor(a.level) }">{{ a.level
                }}</span>
              <span class="anomaly-status">{{ a.status }}</span>
            </div>
          </div>
          <div class="panel-water-ripple"></div>
        </div>
      </div>
    </div>

    <!-- 底部面板 -->
    <div class="bottom-row">
      <div class="bottom-card">
        <div class="panel-title"><span class="title-icon">◈</span> 模型迭代流水线</div>
        <div class="pipeline-container">
          <div v-for="(p, idx) in pipelineData" :key="idx" class="pipe-item">
            <div class="pipe-label">{{ p.name }}</div>
            <div class="pipe-track">
              <div class="pipe-fill"
                :style="{ '--target-w': p.percent + '%', '--c': p.color, '--delay': idx * 0.3 + 's' }">
                <div class="pipe-shine"></div>
              </div>
            </div>
            <span class="pipe-value" :style="{ color: p.color }">{{ p.days }}天</span>
          </div>
        </div>
        <div class="panel-water-ripple"></div>
      </div>
      <div class="bottom-card">
        <div class="panel-title"><span class="title-icon">◈</span> 资源利用率热力图</div>
        <div ref="chartHeatmap" class="chart-container chart-large"></div>
        <div class="panel-water-ripple"></div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getLargeScreenOverview,
  getLargeScreenConnLabels,
  getLargeScreenAnomalies,
  getLargeScreenPipelineData,
  getLargeScreenVersionChart,
  getLargeScreenDatasetChart,
  getLargeScreenTrainingChart,
  getLargeScreenInferenceChart,
  getLargeScreenRadarChart,
  getLargeScreenHeatmapChart
} from '@/api/llmops/largeScreen'

export default {
  name: 'largeScreen',
  data() {
    return {
      currentTime: '00:00:00',
      currentDate: '2024-01-01',
      currentWeekday: '星期一',
      timer: null,
      scrollTimer: null,
      highlightTimer: null,
      pauseScroll: false,
      activeNode: -1,
      highlightIndex: -1,
      charts: {},
      containerW: 900,
      containerH: 600,
      particleAnimId: null,
      isFullscreen: false,
      chartPayload: null,

      overviewData: [
        { label: '活跃训练任务', value: 47, trend: 12, color: '#00d4ff', icon: '⚙️' },
        { label: '模型版本数', value: 328, trend: 8.5, color: '#0099ff', icon: '🏷️' },
        { label: '数据集总量', value: '1,256', trend: 23, color: '#00b8ff', icon: '🗃️' },
        { label: '推理请求/日', value: '89.6K', trend: 34, color: '#0077ff', icon: '⚡' },
        { label: '模型准确率', value: '94.7%', trend: 2.1, color: '#00e5ff', icon: '🎯' },
        { label: '异常告警', value: 23, trend: 56, color: '#ff4757', icon: '🔔' }
      ],

      nodeConfigs: [
        { hex: '#00e5ff', name: '客户', desc: '需求驱动 · 价值交付', img: require('@/assets/nodes/client.png') },
        { hex: '#00d4ff', name: 'AI基地', desc: '智能计算 · 调度中枢', img: require('@/assets/nodes/aibase.png') },
        { hex: '#00b8ff', name: '数据采集', desc: '多源采集 · 清洗 · 标注', img: require('@/assets/nodes/dataset.png') },
        { hex: '#0099ff', name: '模型仓库', desc: '版本管理 · 存储 · 检索', img: require('@/assets/nodes/warehouse.png') },
        { hex: '#0077ff', name: '训练工厂', desc: '模型训练 · 调优 · 监控', img: require('@/assets/nodes/training.png') },
        { hex: '#0055ff', name: '推理验证', desc: 'AI应用 · 机器人 · 部署', img: require('@/assets/nodes/inference.png') },
        { hex: '#0033ff', name: '模型库', desc: '模型沉淀 · 持续迭代', img: require('@/assets/nodes/modellib.png') }
      ],

      connLabels: ['需求驱动', '智能调度', '转换标注', '机器执行', 'AI驱动', '迭代沉淀', '价值交付'],

      anomalies: [
        { time: '16:53:20', model: 'yolov8', type: '误报', level: '高', status: '处理中' },
        { time: '16:52:45', model: 'bert-base', type: '超时', level: '中', status: '待处理' },
        { time: '16:51:18', model: 'resnet50', type: '精度下降', level: '高', status: '处理中' },
        { time: '16:49:30', model: 'gpt2', type: '显存溢出', level: '严重', status: '已修复' },
        { time: '16:47:12', model: 'yolov8', type: '误报', level: '低', status: '已忽略' },
        { time: '16:45:55', model: 'bert-base', type: '超时', level: '中', status: '待处理' }
      ],

      pipelineData: [
        { name: '数据准备', days: 2.5, percent: 38, color: '#00d4ff' },
        { name: '训练', days: 8.0, percent: 100, color: '#0099ff' },
        { name: '验证', days: 1.5, percent: 22, color: '#0077ff' },
        { name: '部署', days: 1.0, percent: 15, color: '#0055ff' },
        { name: '监控', days: 3.0, percent: 45, color: '#0033ff' }
      ]
    }
  },
  watch: {
    $route: {
      immediate: true,
      handler() {
        this.loadLargeScreenData()
      }
    }
  },

  mounted() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
    this.$nextTick(() => {
      this.updateContainerSize()
      this.initCharts()
      if (this.chartPayload) {
        this.applyChartData(this.chartPayload)
      }
      this.startAnomalyScroll()
      this.startNodeHighlight()
      this.initParticles()
      if (typeof ResizeObserver !== 'undefined' && this.$refs.sceneContainer) {
        this._ro = new ResizeObserver(this.updateContainerSize)
        this._ro.observe(this.$refs.sceneContainer)
      }
    })
    window.addEventListener('resize', this.handleResize)

    // 监听全屏变化
    document.addEventListener('fullscreenchange', this.handleFullscreenChange)
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange)
    document.addEventListener('msfullscreenchange', this.handleFullscreenChange)
  },

  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
    if (this.scrollTimer) clearInterval(this.scrollTimer)
    if (this.highlightTimer) clearInterval(this.highlightTimer)
    if (this.particleAnimId) cancelAnimationFrame(this.particleAnimId)
    if (this._ro) this._ro.disconnect()
    window.removeEventListener('resize', this.handleResize)
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange)
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange)
    document.removeEventListener('msfullscreenchange', this.handleFullscreenChange)
    Object.values(this.charts).forEach(c => c && c.dispose())
  },

  methods: {
    async loadLargeScreenData() {
      console.log('[largeScreen] request large screen data')
      const normalize = res => res && res.body
        ? res.body
        : res && res.data
          ? res.data
          : res

      const [
        overview,
        connLabels,
        anomalies,
        pipelineData,
        version,
        dataset,
        training,
        inference,
        radar,
        heatmap
      ] = await Promise.all([
        getLargeScreenOverview().then(normalize).catch(() => null),
        getLargeScreenConnLabels().then(normalize).catch(() => null),
        getLargeScreenAnomalies().then(normalize).catch(() => null),
        getLargeScreenPipelineData().then(normalize).catch(() => null),
        getLargeScreenVersionChart().then(normalize).catch(() => null),
        getLargeScreenDatasetChart().then(normalize).catch(() => null),
        getLargeScreenTrainingChart().then(normalize).catch(() => null),
        getLargeScreenInferenceChart().then(normalize).catch(() => null),
        getLargeScreenRadarChart().then(normalize).catch(() => null),
        getLargeScreenHeatmapChart().then(normalize).catch(() => null)
      ])

      if (Array.isArray(overview)) this.overviewData = overview
      if (Array.isArray(connLabels)) this.connLabels = connLabels
      if (Array.isArray(anomalies)) this.anomalies = anomalies
      if (Array.isArray(pipelineData)) this.pipelineData = pipelineData

      this.chartPayload = { version, dataset, training, inference, radar, heatmap }
      this.$nextTick(() => this.applyChartData(this.chartPayload))
    },

    applyChartData(chartData) {
      if (!chartData) return

      if (this.charts.version) {
        this.charts.version.setOption({ series: [{ data: this.toPieData(chartData.version) }] })
      }
      if (this.charts.dataset) {
        this.charts.dataset.setOption({ series: [{ data: this.toPieData(chartData.dataset) }] })
      }
      if (this.charts.training && chartData.training) {
        this.charts.training.setOption({
          xAxis: { data: chartData.training.xAxis || [] },
          series: [
            { data: chartData.training.taskCount || [] },
            { data: chartData.training.gpu || [] }
          ]
        })
      }
      if (this.charts.inference && chartData.inference) {
        this.charts.inference.setOption({
          xAxis: { data: chartData.inference.xAxis || [] },
          series: [
            { data: chartData.inference.requests || [] },
            { data: chartData.inference.latency || [] }
          ]
        })
      }
      if (this.charts.radar && chartData.radar) {
        this.charts.radar.setOption({
          radar: { indicator: chartData.radar.indicators || [] },
          series: [{ data: [{ value: chartData.radar.value || [] }] }]
        })
      }
      if (this.charts.heatmap && Array.isArray(chartData.heatmap)) {
        this.charts.heatmap.setOption({ series: [{ data: chartData.heatmap }] })
      }
    },

    toPieData(list) {
      return (list || []).map(item => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: item.color }
      }))
    },

    updateTime() {
      const now = new Date()
      const p = n => String(n).padStart(2, '0')
      this.currentTime = `${p(now.getHours())}:${p(now.getMinutes())}:${p(now.getSeconds())}`
      this.currentDate = `${now.getFullYear()}-${p(now.getMonth() + 1)}-${p(now.getDate())}`
      const wd = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      this.currentWeekday = wd[now.getDay()]
    },

    getLevelColor(lvl) {
      return { '严重': '#ff4757', '高': '#ff6b35', '中': '#ffa502', '低': '#2ed573' }[lvl] || '#868e96'
    },

    updateContainerSize() {
      const el = this.$refs.sceneContainer
      if (el) {
        this.containerW = el.clientWidth || 900
        this.containerH = el.clientHeight || 600
      }
    },

    getNodePos(i) {
      const angle = (-90 + i * (360 / 7)) * Math.PI / 180
      const R = Math.min(this.containerW * 0.34, 250)
      return {
        x: R * Math.cos(angle),
        y: R * Math.sin(angle) * 0.5
      }
    },

    getNodeStyle(i) {
      const { x, y } = this.getNodePos(i)
      return {
        left: (this.containerW / 2 + x - 75) + 'px',
        top: (this.containerH / 2 + y - 85) + 'px',
        '--c': this.nodeConfigs[i].hex
      }
    },

    getTooltipStyle(i) {
      const angle = (-90 + i * (360 / 7)) * Math.PI / 180
      const R = Math.min(this.containerW * 0.34, 250) * 1.45
      const x = R * Math.cos(angle)
      const y = R * Math.sin(angle) * 0.5
      return {
        left: (this.containerW / 2 + x - 80) + 'px',
        top: (this.containerH / 2 + y - 36) + 'px',
        '--c': this.nodeConfigs[i].hex
      }
    },

    getConnLabelStyle(i) {
      const angle = (-90 + (i + 0.5) * (360 / 7)) * Math.PI / 180
      const R = Math.min(this.containerW * 0.34, 250) * 0.60
      const x = R * Math.cos(angle)
      const y = R * Math.sin(angle) * 0.5
      return {
        left: (this.containerW / 2 + x - 32) + 'px',
        top: (this.containerH / 2 + y - 9) + 'px'
      }
    },

    getLinkPath(i) {
      const idx1 = (i - 1 + 7) % 7
      const idx2 = i % 7
      const p1 = this.getNodePos(idx1)
      const p2 = this.getNodePos(idx2)
      const cx = this.containerW / 2
      const cy = this.containerH / 2
      const x1 = cx + p1.x
      const y1 = cy + p1.y
      const x2 = cx + p2.x
      const y2 = cy + p2.y
      const mx = (x1 + x2) / 2
      const my = (y1 + y2) / 2 - 48
      return `M ${x1} ${y1} Q ${mx} ${my} ${x2} ${y2}`
    },

    getParticleStyle(i) {
      const seed = i * 137.5
      const left = (seed % 90) + 5
      const top = ((seed * 1.3) % 85) + 5
      const size = 2 + (i % 4)
      const dur = 3 + (i % 6)
      const delay = (i * 0.3) % 4
      const colors = ['#00d4ff', '#0099ff', '#00e5ff', '#0077ff', '#ffffff', '#ff6b35']
      return {
        left: left + '%',
        top: top + '%',
        width: size + 'px',
        height: size + 'px',
        background: colors[i % colors.length],
        animationDuration: dur + 's',
        animationDelay: delay + 's',
        boxShadow: `0 0 ${size * 2}px ${colors[i % colors.length]}`
      }
    },

    initParticles() {
      const canvas = this.$refs.particleCanvas
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const dpr = window.devicePixelRatio || 1
      const rect = canvas.parentElement.getBoundingClientRect()
      canvas.width = window.innerWidth * dpr
      canvas.height = window.innerHeight * dpr
      canvas.style.width = window.innerWidth + 'px'
      canvas.style.height = window.innerHeight + 'px'
      ctx.scale(dpr, dpr)

      const particles = []
      const count = 60
      for (let i = 0; i < count; i++) {
        particles.push({
          x: Math.random() * window.innerWidth,
          y: Math.random() * window.innerHeight,
          vx: (Math.random() - 0.5) * 0.6,
          vy: (Math.random() - 0.5) * 0.6,
          r: Math.random() * 2.5 + 1,
          alpha: Math.random() * 0.5 + 0.2
        })
      }

      let mouseX = -1000
      let mouseY = -1000

      const onMouseMove = (e) => {
        mouseX = e.clientX
        mouseY = e.clientY
      }
      window.addEventListener('mousemove', onMouseMove)

      const animate = () => {
        ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)

        particles.forEach(p => {
          // 鼠标吸引效果
          const dx = mouseX - p.x
          const dy = mouseY - p.y
          const dist = Math.sqrt(dx * dx + dy * dy)
          if (dist < 200) {
            p.vx += dx * 0.002
            p.vy += dy * 0.002
          }

          p.vx *= 0.999
          p.vy *= 0.999
          p.x += p.vx
          p.y += p.vy

          if (p.x < 0) p.x = window.innerWidth
          if (p.x > window.innerWidth) p.x = 0
          if (p.y < 0) p.y = window.innerHeight
          if (p.y > window.innerHeight) p.y = 0

          ctx.beginPath()
          ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
          ctx.fillStyle = `rgba(0, 212, 255, ${p.alpha})`
          ctx.shadowColor = 'rgba(0, 212, 255, 0.5)'
          ctx.shadowBlur = 10
          ctx.fill()
        })

        // 连线
        ctx.shadowBlur = 0
        for (let i = 0; i < particles.length; i++) {
          for (let j = i + 1; j < particles.length; j++) {
            const dx = particles[i].x - particles[j].x
            const dy = particles[i].y - particles[j].y
            const dist = Math.sqrt(dx * dx + dy * dy)
            if (dist < 150) {
              const alpha = 0.08 * (1 - dist / 150)
              ctx.beginPath()
              ctx.moveTo(particles[i].x, particles[i].y)
              ctx.lineTo(particles[j].x, particles[j].y)
              ctx.strokeStyle = `rgba(0, 212, 255, ${alpha})`
              ctx.lineWidth = 0.8
              ctx.stroke()
            }
          }
        }
        this.particleAnimId = requestAnimationFrame(animate)
      }
      animate()
    },

    startNodeHighlight() {
      this.highlightTimer = setInterval(() => {
        this.highlightIndex = (this.highlightIndex + 1) % 7
        // 保持高亮1.5秒后自动取消
        setTimeout(() => {
          if (this.activeNode === -1) {
            this.highlightIndex = -1
          }
        }, 1500)
      }, 3500)
    },

    initCharts() {
      const dark = {
        backgroundColor: 'transparent',
        tooltip: {
          backgroundColor: 'rgba(5,15,35,0.95)',
          borderColor: 'rgba(0,212,255,0.4)',
          textStyle: { color: '#e8f8ff', fontSize: 12 }
        }
      }
      const axisStyle = {
        axisLine: { lineStyle: { color: '#1a3a5c' } },
        axisLabel: { color: '#74c0fc', fontSize: 12 },
        splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)', type: 'dashed' } }
      }

      // 1. 模型版本分布
      this.charts.version = echarts.init(this.$refs.chartVersion)
      this.charts.version.setOption({
        ...dark,
        tooltip: { ...dark.tooltip, trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        series: [{
          type: 'pie', radius: ['42%', '72%'], center: ['50%', '52%'],
          label: { color: '#74c0fc', fontSize: 13, fontWeight: 500 },
          labelLine: { lineStyle: { color: '#1a3a5c' } },
          itemStyle: { borderColor: '#050b1a', borderWidth: 2, shadowBlur: 20, shadowColor: 'rgba(0,212,255,0.2)' },
          data: [
            { value: 42, name: '训练中', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#00e5ff' }, { offset: 1, color: '#00a8cc' }]) } },
            { value: 28, name: '待验证', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#0099ff' }, { offset: 1, color: '#0066cc' }]) } },
            { value: 156, name: '已部署', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#0077ff' }, { offset: 1, color: '#0044cc' }]) } },
            { value: 102, name: '已归档', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#0033ff' }, { offset: 1, color: '#001199' }]) } }
          ]
        }]
      })

      // 2. 训练任务趋势
      this.charts.training = echarts.init(this.$refs.chartTraining)
      this.charts.training.setOption({
        ...dark,
        tooltip: { ...dark.tooltip, trigger: 'axis' },
        legend: { data: ['任务数', 'GPU%'], textStyle: { color: '#74c0fc', fontSize: 12 }, top: 2, right: 4 },
        grid: { top: 32, bottom: 24, left: 44, right: 44 },
        xAxis: { type: 'category', data: ['12/18', '12/19', '12/20', '12/21', '12/22', '12/23', '12/24'], ...axisStyle, axisLabel: { ...axisStyle.axisLabel, fontSize: 12 } },
        yAxis: [
          { type: 'value', ...axisStyle },
          { type: 'value', max: 100, ...axisStyle, splitLine: { show: false }, axisLabel: { ...axisStyle.axisLabel, formatter: '{value}%', fontSize: 12 } }
        ],
        series: [
          {
            name: '任务数', type: 'line', data: [12, 15, 18, 14, 20, 22, 19], smooth: true,
            lineStyle: { color: '#00d4ff', width: 3, shadowBlur: 20, shadowColor: 'rgba(0,212,255,0.3)' },
            itemStyle: { color: '#00d4ff' },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,212,255,0.35)' }, { offset: 1, color: 'rgba(0,212,255,0.02)' }]) },
            symbol: 'circle', symbolSize: 10
          },
          {
            name: 'GPU%', type: 'line', yAxisIndex: 1, data: [65, 72, 68, 80, 75, 85, 78], smooth: true,
            lineStyle: { color: '#ff6b35', width: 3, shadowBlur: 20, shadowColor: 'rgba(255,107,53,0.3)' },
            itemStyle: { color: '#ff6b35' },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(255,107,53,0.25)' }, { offset: 1, color: 'rgba(255,107,53,0.02)' }]) },
            symbol: 'diamond', symbolSize: 10
          }
        ]
      })

      // 3. 数据集来源
      this.charts.dataset = echarts.init(this.$refs.chartDataset)
      this.charts.dataset.setOption({
        ...dark,
        tooltip: { ...dark.tooltip, trigger: 'item', formatter: '{b}: {d}%' },
        series: [{
          type: 'pie', radius: ['35%', '65%'], center: ['50%', '52%'],
          roseType: 'radius',
          label: { color: '#74c0fc', fontSize: 13, fontWeight: 500 },
          labelLine: { lineStyle: { color: '#1a3a5c' } },
          itemStyle: { borderColor: '#050b1a', borderWidth: 2, shadowBlur: 20, shadowColor: 'rgba(0,212,255,0.15)' },
          data: [
            { value: 45, name: '公开数据集', itemStyle: { color: '#00d4ff' } },
            { value: 30, name: '自采', itemStyle: { color: '#0099ff' } },
            { value: 15, name: '合成', itemStyle: { color: '#0077ff' } },
            { value: 10, name: '第三方', itemStyle: { color: '#0055ff' } }
          ]
        }]
      })

      // 4. 实时推理监控
      this.charts.inference = echarts.init(this.$refs.chartInference)
      const xData = Array.from({ length: 12 }, (_, i) => `16:53:${String(i * 5).padStart(2, '0')}`)
      this.charts.inference.setOption({
        ...dark,
        tooltip: { ...dark.tooltip, trigger: 'axis' },
        legend: { data: ['请求量', '延迟ms'], textStyle: { color: '#74c0fc', fontSize: 12 }, top: 2, right: 4 },
        grid: { top: 32, bottom: 28, left: 44, right: 48 },
        xAxis: { type: 'category', data: xData, ...axisStyle, axisLabel: { ...axisStyle.axisLabel, fontSize: 11, rotate: 20 } },
        yAxis: [
          { type: 'value', ...axisStyle },
          { type: 'value', ...axisStyle, splitLine: { show: false }, axisLabel: { ...axisStyle.axisLabel, formatter: '{value}ms', fontSize: 12 } }
        ],
        series: [
          {
            name: '请求量', type: 'line', data: [23, 45, 38, 56, 42, 39, 51, 48, 62, 55, 47, 58], smooth: true,
            lineStyle: { color: '#00d4ff', width: 3, shadowBlur: 20, shadowColor: 'rgba(0,212,255,0.3)' },
            itemStyle: { color: '#00d4ff' },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,212,255,0.35)' }, { offset: 1, color: 'rgba(0,212,255,0.02)' }]) },
            symbol: 'circle', symbolSize: 8
          },
          {
            name: '延迟ms', type: 'line', yAxisIndex: 1, data: [120, 95, 110, 85, 130, 100, 90, 105, 80, 115, 95, 100], smooth: true,
            lineStyle: { color: '#ff6b35', width: 3, shadowBlur: 20, shadowColor: 'rgba(255,107,53,0.3)' },
            itemStyle: { color: '#ff6b35' },
            symbol: 'diamond', symbolSize: 8
          }
        ]
      })

      // 5. 模型性能雷达
      this.charts.radar = echarts.init(this.$refs.chartRadar)
      this.charts.radar.setOption({
        ...dark,
        tooltip: { ...dark.tooltip },
        radar: {
          indicator: [
            { name: '准确率', max: 100 }, { name: '召回率', max: 100 },
            { name: 'F1', max: 100 }, { name: '吞吐', max: 100 }, { name: '稳定性', max: 100 }
          ],
          center: ['50%', '54%'], radius: '68%',
          axisName: { color: '#74c0fc', fontSize: 13 },
          splitArea: { areaStyle: { color: ['rgba(0,50,100,0.1)', 'rgba(0,100,180,0.1)', 'rgba(0,150,255,0.1)'] } },
          splitLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
          axisLine: { lineStyle: { color: 'rgba(0,212,255,0.3)' } }
        },
        series: [{
          type: 'radar',
          data: [{
            value: [94, 88, 91, 85, 92],
            areaStyle: { color: 'rgba(0,212,255,0.3)' },
            lineStyle: { color: '#00d4ff', width: 3, shadowBlur: 20, shadowColor: 'rgba(0,212,255,0.3)' },
            itemStyle: { color: '#00d4ff' }
          }]
        }]
      })

      // 6. 资源热力图
      this.charts.heatmap = echarts.init(this.$refs.chartHeatmap)
      const hours = Array.from({ length: 24 }, (_, i) => `${i}`)
      const days = ['一', '二', '三', '四', '五', '六', '日']
      const heatData = []
      for (let d = 0; d < 7; d++) for (let h = 0; h < 24; h++) heatData.push([h, d, Math.floor(Math.random() * 100)])
      this.charts.heatmap.setOption({
        ...dark,
        tooltip: { ...dark.tooltip, position: 'top', formatter: p => `周${days[p.data[1]]} ${p.data[0]}:00<br/>利用率: ${p.data[2]}%` },
        grid: { top: 8, bottom: 32, left: 32, right: 12 },
        xAxis: { type: 'category', data: hours, ...axisStyle, axisLabel: { ...axisStyle.axisLabel, fontSize: 10 }, splitArea: { show: false } },
        yAxis: { type: 'category', data: days, ...axisStyle, axisLabel: { ...axisStyle.axisLabel, fontSize: 12 }, splitArea: { show: false } },
        visualMap: { min: 0, max: 100, show: false, inRange: { color: ['#050b1a', '#062040', '#0055cc', '#00d4ff'] } },
        series: [{ type: 'heatmap', data: heatData, itemStyle: { borderColor: '#050b1a', borderWidth: 1 } }]
      })
    },

    handleResize() {
      this.updateContainerSize()
      Object.values(this.charts).forEach(c => c && c.resize())
      this.$forceUpdate()
    },

    startAnomalyScroll() {
      this.scrollTimer = setInterval(() => {
        if (this.pauseScroll || !this.$refs.anomalyList) return
        const el = this.$refs.anomalyList
        if (el.scrollTop + el.clientHeight >= el.scrollHeight - 2) {
          el.scrollTop = 0
        } else {
          el.scrollTop += 1
        }
      }, 40)
    },

    toggleFullscreen() {
      const el = document.documentElement
      if (!this.isFullscreen) {
        if (el.requestFullscreen) {
          el.requestFullscreen()
        } else if (el.webkitRequestFullscreen) {
          el.webkitRequestFullscreen()
        } else if (el.msRequestFullscreen) {
          el.msRequestFullscreen()
        }
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen()
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen()
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen()
        }
      }
    },

    handleFullscreenChange() {
      this.isFullscreen = !!(
        document.fullscreenElement ||
        document.webkitFullscreenElement ||
        document.msFullscreenElement
      )
    }
  }
}
</script>

<style scoped>
.large-screen {
  width: 100vw;
  height: 100vh;
  background: #020c1a;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  color: #a8d8ff;
  box-sizing: border-box;
}

.dashboard-bg {
  position: absolute;
  inset: 0;
  background: url('~@/assets/nodes/bg2.png') center center / cover no-repeat;
  z-index: 0;
}

.dashboard-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(0, 212, 255, 0.08) 1px, transparent 1px);
  background-size: 40px 40px;
  animation: bgShift 20s linear infinite;
}

/* 粒子Canvas */
.particle-canvas {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  opacity: 0.6;
}

/* 顶部光晕 */
.top-glow {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 300px;
  background: radial-gradient(ellipse, rgba(0, 100, 255, 0.15), transparent 70%);
  z-index: 0;
  pointer-events: none;
  animation: topGlowPulse 4s ease-in-out infinite;
}

@keyframes topGlowPulse {

  0%,
  100% {
    opacity: 0.6;
    transform: translateX(-50%) scale(1);
  }

  50% {
    opacity: 1;
    transform: translateX(-50%) scale(1.1);
  }
}

@keyframes bgShift {
  0% {
    background-position: 0 0;
  }

  100% {
    background-position: 40px 40px;
  }
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.8), transparent);
  z-index: 1;
  animation: scanDown 6s linear infinite;
  pointer-events: none;
}

@keyframes scanDown {
  0% {
    top: 0;
    opacity: 0.8;
  }

  95% {
    opacity: 0.6;
  }

  100% {
    top: 100%;
    opacity: 0;
  }
}

/* === TOP BAR === */
.top-bar {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 58px;
  flex-shrink: 0;
  background: linear-gradient(180deg, rgba(0, 30, 70, 0.92) 0%, rgba(2, 12, 26, 0.6) 100%);
  border-bottom: 1px solid rgba(0, 212, 255, 0.22);
  box-shadow: 0 4px 24px rgba(0, 212, 255, 0.1);
}

.top-left,
.top-right {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  min-width: 240px;
}

.top-right {
  justify-content: flex-end;
}

.time-highlight {
  font-size: 20px;
  font-weight: 700;
  color: #00d4ff;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.9), 0 0 40px rgba(0, 212, 255, 0.3);
  letter-spacing: 2px;
  font-variant-numeric: tabular-nums;
}

.separator {
  color: rgba(0, 212, 255, 0.3);
  margin: 0 2px;
}

.normal-text {
  color: #74c0fc;
}

.weather-text {
  color: #a8d8ff;
}

.top-center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title-wrap {
  position: relative;
  padding: 4px 28px;
}

.title-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border-color: #00d4ff;
  border-style: solid;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.3);
}

.title-corner.tl {
  top: 0;
  left: 0;
  border-width: 2px 0 0 2px;
}

.title-corner.tr {
  top: 0;
  right: 0;
  border-width: 2px 2px 0 0;
}

.title-corner.bl {
  bottom: 0;
  left: 0;
  border-width: 0 0 2px 2px;
}

.title-corner.br {
  bottom: 0;
  right: 0;
  border-width: 0 2px 2px 0;
}

.main-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 5px;
  background: linear-gradient(90deg, #00b8ff, #00e5ff, #fff, #00e5ff, #00b8ff);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleShimmer 4s linear infinite;
  white-space: nowrap;
  text-shadow: 0 0 40px rgba(0, 212, 255, 0.1);
}

@keyframes titleShimmer {
  0% {
    background-position: 0% center;
  }

  100% {
    background-position: 200% center;
  }
}

.deco-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 2px;
}

.deco-diamond {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #00d4ff;
  transform: rotate(45deg);
  box-shadow: 0 0 10px #00d4ff;
}

.deco-seg {
  display: inline-block;
  height: 1px;
  width: 24px;
  background: linear-gradient(90deg, transparent, #00d4ff, transparent);
}

.deco-seg.long {
  width: 48px;
}

.deco-dot {
  display: inline-block;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #00d4ff;
  box-shadow: 0 0 6px #00d4ff;
}

.nav-btn {
  background: rgba(0, 100, 200, 0.15);
  border: 1px solid rgba(0, 212, 255, 0.35);
  color: #74c0fc;
  padding: 5px 14px;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
  letter-spacing: 1px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.nav-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.1), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s;
}

.nav-btn:hover::before {
  transform: translateX(100%);
}

.nav-btn:hover {
  background: rgba(0, 212, 255, 0.2);
  border-color: #00d4ff;
  color: #00d4ff;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.4), inset 0 0 20px rgba(0, 212, 255, 0.1);
}

/* 全屏按钮 */
.fullscreen-btn {
  font-size: 14px !important;
  padding: 5px 12px !important;
  background: rgba(0, 212, 255, 0.12) !important;
  border-color: rgba(0, 212, 255, 0.35) !important;
  min-width: 80px;
}

.fullscreen-btn:hover {
  background: rgba(0, 212, 255, 0.25) !important;
  border-color: #00d4ff !important;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.4) !important;
}

/* 全屏模式样式 */
.large-screen:-webkit-full-screen {
  background: #020c1a;
}

.large-screen:-moz-full-screen {
  background: #020c1a;
}

.large-screen:fullscreen {
  background: #020c1a;
}

/* === OVERVIEW ROW === */
.overview-row {
  position: relative;
  z-index: 10;
  display: flex;
  gap: 8px;
  padding: 6px 16px;
  flex-shrink: 0;
}

.overview-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(0, 20, 50, 0.85), rgba(0, 40, 90, 0.55));
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-left: 3px solid var(--c, #00d4ff);
  border-radius: 4px;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: cardSlideIn 0.6s ease-out both;
  animation-delay: var(--delay, 0s);
}

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.overview-card::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 6px;
  background: conic-gradient(from var(--angle, 0deg), transparent, var(--c, #00d4ff), transparent 40%);
  z-index: -1;
  animation: rotateBorder 4s linear infinite;
  opacity: 0.4;
}

@property --angle {
  syntax: '<angle>';
  initial-value: 0deg;
  inherits: false;
}

@keyframes rotateBorder {
  to {
    --angle: 360deg;
  }
}

.card-shine {
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(255, 255, 255, 0.05) 45%, rgba(255, 255, 255, 0.1) 50%, rgba(255, 255, 255, 0.05) 55%, transparent 60%);
  background-size: 200% 100%;
  animation: shineMove 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes shineMove {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

.overview-card:hover {
  border-color: var(--c, #00d4ff);
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.2), inset 0 0 30px rgba(0, 212, 255, 0.05);
  transform: translateY(-4px) scale(1.01);
}

.ov-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.ov-info {
  flex: 1;
  min-width: 0;
}

.ov-label {
  font-size: 11px;
  color: #74c0fc;
  white-space: nowrap;
}

.ov-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--c, #00d4ff);
  text-shadow: 0 0 20px var(--c, #00d4ff);
  line-height: 1.1;
  animation: valuePop 0.5s ease-out;
}

@keyframes valuePop {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }

  70% {
    transform: scale(1.1);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.ov-trend {
  margin-left: auto;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 2px;
  animation: trendPulse 2s ease-in-out infinite;
}

@keyframes trendPulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.7;
  }
}

.ov-trend.up {
  color: #2ed573;
  background: rgba(46, 213, 115, 0.15);
}

.ov-trend.down {
  color: #ff4757;
  background: rgba(255, 71, 87, 0.15);
}

/* === BODY CONTAINER === */
.body-container {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  gap: 8px;
  padding: 0 12px;
  min-height: 0;
  overflow: hidden;
}

/* === SIDE PANELS === */
.side-panel {
  width: 24%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow: hidden;
}

.panel-card {
  flex: 1;
  background: linear-gradient(135deg, rgba(0, 15, 40, 0.92), rgba(0, 30, 70, 0.7));
  border: 1px solid rgba(0, 212, 255, 0.18);
  border-radius: 6px;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.panel-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.6), transparent);
  animation: panelLineMove 3s ease-in-out infinite;
}

@keyframes panelLineMove {

  0%,
  100% {
    opacity: 0.4;
  }

  50% {
    opacity: 1;
  }
}

.panel-card:hover {
  border-color: rgba(0, 212, 255, 0.45);
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.08), inset 0 0 30px rgba(0, 212, 255, 0.03);
}

.panel-water-ripple {
  position: absolute;
  bottom: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 80%, rgba(0, 212, 255, 0.03), transparent 50%);
  animation: rippleMove 8s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes rippleMove {

  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }

  50% {
    transform: translate(-5%, -3%) scale(1.1);
  }
}

.panel-title {
  font-size: 12px;
  color: #00d4ff;
  letter-spacing: 1px;
  font-weight: 600;
  margin-bottom: 4px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 1;
}

.title-icon {
  color: #00d4ff;
  text-shadow: 0 0 12px #00d4ff;
  animation: iconPulse 2s ease-in-out infinite;
}

@keyframes iconPulse {

  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.4;
    transform: scale(0.9);
  }
}

.chart-container {
  flex: 1;
  min-height: 0;
  z-index: 1;
}

.chart-large {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.chart-large>div {
  width: 100% !important;
  height: 100% !important;
}

/* === CENTER STAGE === */
.center-stage {
  flex: 1;
  display: flex;
  align-items: stretch;
  min-width: 0;
  position: relative;
}

.scene-container {
  flex: 1;
  position: relative;
  background: radial-gradient(ellipse 80% 70% at 50% 50%, rgba(0, 80, 180, 0.14) 0%, rgba(0, 20, 60, 0.06) 60%, transparent 100%);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 0 80px rgba(0, 100, 255, 0.12), inset 0 0 100px rgba(0, 50, 150, 0.08);
}

.scene-container::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 10px;
  box-shadow: inset 0 0 80px rgba(0, 80, 200, 0.1);
  pointer-events: none;
  z-index: 25;
}

.perspective-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(0, 212, 255, 0.07) 1px, transparent 1px), linear-gradient(90deg, rgba(0, 212, 255, 0.07) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(600px) rotateX(35deg) scaleY(1.5);
  transform-origin: 50% 65%;
  opacity: 0.55;
  animation: gridMove 8s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }

  100% {
    background-position: 0 50px;
  }
}

.ground-glow {
  position: absolute;
  bottom: 6%;
  left: 50%;
  transform: translateX(-50%);
  width: 72%;
  height: 200px;
  background: radial-gradient(ellipse, rgba(0, 100, 255, 0.25) 0%, rgba(0, 50, 150, 0.1) 40%, transparent 70%);
  pointer-events: none;
  animation: glowPulse 3s ease-in-out infinite;
}

@keyframes glowPulse {

  0%,
  100% {
    opacity: 0.7;
    transform: translateX(-50%) scaleX(1);
  }

  50% {
    opacity: 1;
    transform: translateX(-50%) scaleX(1.08);
  }
}

/* === ISO PLATFORM === */
.iso-platform {
  position: absolute;
  left: 50%;
  top: 52%;
  transform: translate(-50%, -50%);
  width: min(28vw, 420px);
  height: min(14vw, 210px);
}

.platform-top {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 88%;
  height: 56%;
  background: linear-gradient(180deg, rgba(0, 80, 180, 0.3) 0%, rgba(0, 50, 130, 0.18) 100%);
  border: 1px solid rgba(0, 212, 255, 0.4);
  border-radius: 4px;
  box-shadow: 0 0 40px rgba(0, 100, 255, 0.25), inset 0 1px 0 rgba(0, 212, 255, 0.5);
}

.platform-left {
  position: absolute;
  bottom: 0;
  left: 8%;
  width: 42%;
  height: 50%;
  background: linear-gradient(180deg, rgba(0, 40, 100, 0.45), rgba(0, 20, 60, 0.65));
  border: 1px solid rgba(0, 150, 255, 0.2);
  border-top: none;
  transform: skewY(15deg);
}

.platform-right {
  position: absolute;
  bottom: 0;
  right: 8%;
  width: 42%;
  height: 50%;
  background: linear-gradient(180deg, rgba(0, 30, 80, 0.45), rgba(0, 15, 50, 0.65));
  border: 1px solid rgba(0, 100, 200, 0.2);
  border-top: none;
  transform: skewY(-15deg);
}

/* === HALO RINGS === */
.halo-ring {
  position: absolute;
  left: 50%;
  top: 50%;
  border-radius: 50%;
  border: 1px solid rgba(0, 212, 255, 0.2);
  transform: translate(-50%, -50%) rotateX(70deg);
  pointer-events: none;
  animation: haloRotate 8s linear infinite;
}

.halo-1 {
  width: min(10vw, 160px);
  height: min(10vw, 160px);
  border-color: rgba(0, 212, 255, 0.6);
  animation-duration: 4s;
  box-shadow: 0 0 25px rgba(0, 212, 255, 0.4);
}

.halo-2 {
  width: min(17vw, 280px);
  height: min(17vw, 280px);
  border-color: rgba(0, 180, 255, 0.35);
  animation-duration: 7s;
  animation-direction: reverse;
}

.halo-3 {
  width: min(25vw, 420px);
  height: min(25vw, 420px);
  border-color: rgba(0, 150, 255, 0.2);
  animation-duration: 12s;
}

.halo-4 {
  width: min(35vw, 580px);
  height: min(35vw, 580px);
  border-color: rgba(0, 100, 255, 0.12);
  animation-duration: 18s;
  animation-direction: reverse;
}

@keyframes haloRotate {
  from {
    transform: translate(-50%, -50%) rotateX(70deg) rotateZ(0deg);
  }

  to {
    transform: translate(-50%, -50%) rotateX(70deg) rotateZ(360deg);
  }
}

/* === CENTRAL CORE === */
.central-core {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: min(7vw, 110px);
  height: min(7vw, 110px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.core-outer {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid rgba(0, 212, 255, 0.7);
  animation: coreRotate 4s linear infinite;
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.5);
}

.core-outer::before {
  content: '';
  position: absolute;
  top: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #00d4ff;
  box-shadow: 0 0 20px #00d4ff;
}

.core-mid {
  position: absolute;
  width: 71%;
  height: 71%;
  border-radius: 50%;
  border: 2px solid rgba(0, 180, 255, 0.5);
  animation: coreRotate 3s linear infinite reverse;
}

.core-inner {
  position: absolute;
  width: 44%;
  height: 44%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.95) 0%, rgba(0, 100, 255, 0.65) 50%, rgba(0, 50, 150, 0.3) 100%);
  box-shadow: 0 0 40px rgba(0, 212, 255, 0.9), 0 0 80px rgba(0, 100, 255, 0.5);
  animation: corePulse 2s ease-in-out infinite;
}

.core-pulse {
  position: absolute;
  width: 44%;
  height: 44%;
  border-radius: 50%;
  border: 2px solid rgba(0, 212, 255, 0.8);
  animation: pulseExpand 2s ease-out infinite;
}

@keyframes coreRotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@keyframes corePulse {

  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 0 40px rgba(0, 212, 255, 0.9), 0 0 80px rgba(0, 100, 255, 0.5);
  }

  50% {
    transform: scale(1.14);
    box-shadow: 0 0 60px rgba(0, 212, 255, 1), 0 0 100px rgba(0, 100, 255, 0.7);
  }
}

@keyframes pulseExpand {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }

  100% {
    transform: scale(3.2);
    opacity: 0;
  }
}

.core-label {
  position: absolute;
  top: 43%;
  left: 50%;
  transform: translateX(-50%);
  font-size: min(0.8vw, 11px);
  letter-spacing: 3px;
  color: #00d4ff;
  text-shadow: 0 0 12px #00d4ff;
  white-space: nowrap;
  font-weight: 700;
}

/* === SVG LINKS === */
.links-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 3;
}

/* === SCENE NODES === */
.scene-node {
  position: absolute;
  width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), filter 0.4s;
  z-index: 5;
}

.scene-node:hover,
.scene-node.hover {
  transform: translateY(-12px) scale(1.05) !important;
  z-index: 8;
  filter: drop-shadow(0 0 20px var(--c, #00d4ff)) drop-shadow(0 0 40px var(--c, #00d4ff));
}

.node-name {
  font-size: min(1.1vw, 13px);
  color: var(--c, #00d4ff);
  text-shadow: 0 0 15px var(--c, #00d4ff);
  font-weight: 700;
  margin-bottom: 4px;
  letter-spacing: 2px;
  white-space: nowrap;
}

.node-beam {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 3px;
  height: 60px;
  background: linear-gradient(180deg, var(--c, #00d4ff), transparent);
  opacity: 0.7;
  filter: blur(2px);
  animation: beamPulse 2s ease-in-out infinite;
}

.beam-core {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 30px;
  background: var(--c, #00d4ff);
  filter: blur(4px);
  opacity: 0.5;
}

@keyframes beamPulse {

  0%,
  100% {
    opacity: 0.5;
    height: 50px;
  }

  50% {
    opacity: 1;
    height: 80px;
  }
}

.node-icon-wrap {
  position: relative;
  width: min(9vw, 120px);
  height: min(9vw, 120px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.node-ring {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 1px solid var(--c, #00d4ff);
  animation: ringPulse 2s ease-in-out infinite;
  opacity: 0.4;
}

@keyframes ringPulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 0.4;
  }

  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.node-glow {
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.2), transparent 65%);
  animation: nodeGlow 2s ease-in-out infinite;
}

@keyframes nodeGlow {

  0%,
  100% {
    opacity: 0.5;
  }

  50% {
    opacity: 1;
  }
}

.node-image {
  /* width: 88%; */
  height: 100%;
  object-fit: cover;
  /* filter: drop-shadow(0 0 15px var(--c, #00d4ff)); */
  z-index: 1;
  position: relative;
}

.node-base-platform {
  margin-top: 4px;
  position: relative;
  width: min(7vw, 96px);
  height: min(1.4vw, 22px);
}

.base-top {
  position: absolute;
  top: 0;
  left: 5px;
  right: 5px;
  height: 6px;
  background: linear-gradient(180deg, rgba(0, 212, 255, 0.5), rgba(0, 150, 255, 0.25));
  border: 1px solid rgba(0, 212, 255, 0.6);
  border-radius: 2px 2px 0 0;
}

.base-side-l {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 24px;
  height: 10px;
  background: rgba(0, 80, 180, 0.35);
  border: 1px solid rgba(0, 150, 255, 0.25);
  border-top: none;
  transform: skewX(20deg);
}

.base-side-r {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 10px;
  background: rgba(0, 60, 150, 0.35);
  border: 1px solid rgba(0, 100, 200, 0.25);
  border-top: none;
  transform: skewX(-20deg);
}

/* === CONN LABELS === */
.conn-label {
  position: absolute;
  font-size: 10px;
  color: rgba(0, 212, 255, 0.8);
  background: rgba(0, 20, 50, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.25);
  padding: 2px 8px;
  border-radius: 2px;
  letter-spacing: 1px;
  pointer-events: none;
  white-space: nowrap;
  z-index: 6;
  backdrop-filter: blur(4px);
}

/* === FLOATING PARTICLES === */
.floating-particle {
  position: absolute;
  border-radius: 50%;
  opacity: 0;
  animation: floatUp linear infinite;
  pointer-events: none;
  z-index: 4;
}

@keyframes floatUp {
  0% {
    opacity: 0;
    transform: translateY(0) scale(0.5);
  }

  10% {
    opacity: 0.9;
  }

  90% {
    opacity: 0.6;
  }

  100% {
    opacity: 0;
    transform: translateY(-80px) scale(1.4);
  }
}

/* === NODE TOOLTIP === */
.node-tooltip {
  position: absolute;
  background: rgba(2, 12, 30, 0.96);
  border: 1px solid rgba(0, 212, 255, 0.5);
  border-radius: 4px;
  padding: 8px 14px;
  z-index: 20;
  pointer-events: none;
  width: 160px;
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.35), inset 0 0 30px rgba(0, 212, 255, 0.05);
  backdrop-filter: blur(8px);
}

.tooltip-corner {
  position: absolute;
  width: 8px;
  height: 8px;
  border-color: #00d4ff;
  border-style: solid;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.3);
}

.tooltip-corner.tl {
  top: -1px;
  left: -1px;
  border-width: 2px 0 0 2px;
}

.tooltip-corner.tr {
  top: -1px;
  right: -1px;
  border-width: 2px 2px 0 0;
}

.tooltip-corner.bl {
  bottom: -1px;
  left: -1px;
  border-width: 0 0 2px 2px;
}

.tooltip-corner.br {
  bottom: -1px;
  right: -1px;
  border-width: 0 2px 2px 0;
}

.tooltip-header {
  font-size: 13px;
  font-weight: 700;
  color: var(--c, #00d4ff);
  text-shadow: 0 0 12px var(--c, #00d4ff);
  margin-bottom: 4px;
  letter-spacing: 1px;
}

.tooltip-body {
  font-size: 11px;
  color: #74c0fc;
  line-height: 1.5;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* === ANOMALY LIST === */
.list-header {
  display: grid;
  grid-template-columns: 72px 72px 1fr 44px 60px;
  font-size: 10px;
  color: rgba(0, 212, 255, 0.55);
  padding: 2px 4px 4px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.12);
  flex-shrink: 0;
  gap: 2px;
  z-index: 1;
}

.list-container {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  z-index: 1;
}

.list-container::-webkit-scrollbar {
  width: 3px;
}

.list-container::-webkit-scrollbar-track {
  background: transparent;
}

.list-container::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.3);
  border-radius: 2px;
}

.anomaly-row {
  display: grid;
  grid-template-columns: 72px 72px 1fr 44px 60px;
  font-size: 10px;
  padding: 5px 4px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.06);
  transition: background 0.2s, transform 0.2s;
  gap: 2px;
}

.anomaly-row:hover {
  background: rgba(0, 212, 255, 0.08);
  transform: translateX(4px);
}

.anomaly-row.lvl-严重 {
  border-left: 2px solid #ff4757;
}

.anomaly-row.lvl-高 {
  border-left: 2px solid #ff6b35;
}

.anomaly-row.lvl-中 {
  border-left: 2px solid #ffa502;
}

.anomaly-row.lvl-低 {
  border-left: 2px solid #2ed573;
}

.anomaly-time {
  color: #5b8ab0;
  font-size: 9px;
}

.anomaly-model {
  color: #74c0fc;
}

.anomaly-type {
  color: #a8d8ff;
}

.anomaly-level {
  font-weight: 700;
  font-size: 10px;
}

.anomaly-status {
  color: #5b8ab0;
  font-size: 9px;
}

/* === BOTTOM ROW === */
.bottom-row {
  position: relative;
  z-index: 10;
  display: flex;
  gap: 8px;
  padding: 0 12px 8px;
  flex-shrink: 0;
  height: 160px;
}

.bottom-card {
  flex: 1;
  background: linear-gradient(135deg, rgba(0, 15, 40, 0.92), rgba(0, 30, 70, 0.7));
  border: 1px solid rgba(0, 212, 255, 0.18);
  border-radius: 6px;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.bottom-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.6), transparent);
  animation: panelLineMove 3s ease-in-out infinite;
}

.bottom-card:hover {
  border-color: rgba(0, 212, 255, 0.45);
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.08);
}

/* === PIPELINE === */
.pipeline-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  padding: 2px 0;
  z-index: 1;
}

.pipe-item {
  display: flex;
  align-items: center;
  gap: 8px;
  animation: pipeItemIn 0.6s ease-out both;
  animation-delay: calc(var(--idx, 0) * 0.1s);
}

.pipe-item:nth-child(1) {
  --idx: 0;
}

.pipe-item:nth-child(2) {
  --idx: 1;
}

.pipe-item:nth-child(3) {
  --idx: 2;
}

.pipe-item:nth-child(4) {
  --idx: 3;
}

.pipe-item:nth-child(5) {
  --idx: 4;
}

@keyframes pipeItemIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.pipe-label {
  font-size: 12px;
  color: #74c0fc;
  width: 56px;
  flex-shrink: 0;
  text-align: right;
  font-weight: 500;
}

.pipe-track {
  flex: 1;
  height: 12px;
  background: rgba(0, 30, 70, 0.8);
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(0, 212, 255, 0.12);
  position: relative;
}

.pipe-fill {
  height: 100%;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--c, #00d4ff), rgba(0, 212, 255, 0.5));
  position: relative;
  overflow: hidden;
  box-shadow: 0 0 12px var(--c, #00d4ff);
  animation: pipeAnim 1.5s ease-out forwards;
  animation-delay: var(--delay, 0s);
  width: 0;
}

@keyframes pipeAnim {
  to {
    width: var(--target-w, 100%);
  }
}

.pipe-shine {
  position: absolute;
  top: 0;
  left: -100%;
  bottom: 0;
  width: 60%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: shineSlide 2s ease-in-out infinite;
}

@keyframes shineSlide {
  0% {
    left: -100%;
  }

  100% {
    left: 200%;
  }
}

.pipe-value {
  font-size: 12px;
  font-weight: 700;
  width: 40px;
  text-align: right;
  flex-shrink: 0;
}

* {
  box-sizing: border-box;
}

.chart-container>div {
  width: 100% !important;
  height: 100% !important;
}

@media (max-width: 1280px) {
  .node-icon-wrap {
    width: min(8.5vw, 110px);
    height: min(8.5vw, 110px);
  }

  .scene-node {
    width: 130px;
  }
}

@media (max-width: 900px) {
  .node-icon-wrap {
    width: min(10vw, 90px);
    height: min(10vw, 90px);
  }

  .scene-node {
    width: 110px;
  }
}
</style>
