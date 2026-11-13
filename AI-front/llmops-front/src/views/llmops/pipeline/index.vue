<template>
  <div class="pipeline-page" :class="{ embedded }" ref="pipelineContainer" @mousemove="onMouseMove">
    <!-- 全屏按钮 -->
    <button class="pipeline-fullscreen-btn" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏查看'">
      <i :class="isFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"></i>
      {{ isFullscreen ? '退出' : '全屏' }}
    </button>

    <div ref="canvasRef" class="three-canvas"></div>

    <!-- 顶部遮罩渐变 -->
    <div class="hud-top-fade"></div>

    <!-- 顶部标题 -->
    <div class="hud-center">
      <div class="hud-title">AI 驱动的模型迭代闭环</div>
      <div class="hud-line"><span></span><i></i><span></span></div>
    </div>

    <div v-for="(node, i) in allNodes" :key="i" class="node-label"
      :class="{ active: hoveredIdx === i, 'is-main': node.isMain }"
      :style="{ left: (labelPos[i] ? labelPos[i].x : -9999) + 'px', top: (labelPos[i] ? labelPos[i].y : -9999) + 'px', '--c': node.hex }"
      @mouseenter="hoveredIdx = i" @mouseleave="hoveredIdx = -1">
      <div class="nl-name">{{ node.name }}</div>
    </div>

    <!-- 悬停弹窗 -->
    <transition name="popup-fade">
      <div v-if="hoveredIdx >= 0 && labelPos[hoveredIdx]" class="node-popup"
        :class="{ clickable: allNodes[hoveredIdx].route !== '/' }"
        :style="{ left: labelPos[hoveredIdx].x + 'px', top: (labelPos[hoveredIdx].y - 16) + 'px', '--c': allNodes[hoveredIdx].hex }"
        @mouseenter="popupHovered = true" @mouseleave="popupHovered = false" @click="goTo(allNodes[hoveredIdx].route)">
        <div class="popup-name">{{ allNodes[hoveredIdx].name }}</div>
        <div class="popup-desc">{{ allNodes[hoveredIdx].desc }}</div>
      </div>
    </transition>

    <div v-for="(pair, i) in connLabelPairs" :key="'cl' + i" class="conn-label"
      :style="{ left: pair.pos.x + 'px', top: pair.pos.y + 'px', '--c': pair.color }">{{ pair.text }}</div>

    <!-- 左侧全息弹窗 -->
    <div class="hologram-panel hologram-left">
      <div class="hologram-item" :style="{ '--c': '#00d4ff' }" @click="goTo('/')">
        <div class="hologram-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"></circle>
            <path
              d="M12 1v6M12 17v6M4.22 4.22l4.24 4.24M15.54 15.54l4.24 4.24M1 12h6M17 12h6M4.22 19.78l4.24-4.24M15.54 8.46l4.24-4.24">
            </path>
          </svg>
        </div>
        <div class="hologram-content">
          <div class="hologram-title">AI 基地</div>
          <div class="hologram-desc">智能计算 · 调度中枢</div>
        </div>
      </div>
      <div class="hologram-item" :style="{ '--c': '#00b8ff' }" @click="goTo('/llmops/datasets')">
        <div class="hologram-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
            <line x1="8" y1="21" x2="16" y2="21"></line>
            <line x1="12" y1="17" x2="12" y2="21"></line>
          </svg>
        </div>
        <div class="hologram-content">
          <div class="hologram-title">数据采集</div>
          <div class="hologram-desc">多源采集 · 清洗 · 标注</div>
        </div>
      </div>
      <div class="hologram-item" :style="{ '--c': '#0099ff' }" @click="goTo('/llmops/storage')">
        <div class="hologram-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <path d="M16 10a4 4 0 0 1-8 0"></path>
          </svg>
        </div>
        <div class="hologram-content">
          <div class="hologram-title">模型仓库</div>
          <div class="hologram-desc">版本管理 · 存储 · 检索</div>
        </div>
      </div>
    </div>

    <!-- 右侧全息弹窗 -->
    <div class="hologram-panel hologram-right">
      <div class="hologram-item" :style="{ '--c': '#0077ff' }" @click="goTo('/llmops/training')">
        <div class="hologram-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect>
            <rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect>
            <line x1="6" y1="6" x2="6" y2="6"></line>
            <line x1="6" y1="18" x2="6" y2="18"></line>
          </svg>
        </div>
        <div class="hologram-content">
          <div class="hologram-title">训练工厂</div>
          <div class="hologram-desc">模型训练 · 调优 · 监控</div>
        </div>
      </div>
      <div class="hologram-item" :style="{ '--c': '#0055ff' }" @click="goTo('/llmops/serving')">
        <div class="hologram-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z"></path>
          </svg>
        </div>
        <div class="hologram-content">
          <div class="hologram-title">推理验证</div>
          <div class="hologram-desc">AI 应用 · 机器人 · 部署</div>
        </div>
      </div>
      <div class="hologram-item" :style="{ '--c': '#0033ff' }" @click="goTo('/')">
        <div class="hologram-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon
              points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2">
            </polygon>
          </svg>
        </div>
        <div class="hologram-content">
          <div class="hologram-title">模型库</div>
          <div class="hologram-desc">模型沉淀 · 持续迭代</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { getPipelineProgress } from '@/api/llmops/ailabx'

// Loop order: 客户→AI基地→数据采集→模型仓库→训练工厂→推理验证→模型库→(客户)
const ALL_NODES = [
  { hex: '#00e5ff', name: '客户', desc: '需求驱动 · 价值交付', route: '/', type: 'client', isMain: false },
  { hex: '#00d4ff', name: 'AI基地', desc: '智能计算 · 调度中枢', route: '/', type: 'aibase', isMain: false },
  { hex: '#00b8ff', name: '数据采集', desc: '多源采集 · 清洗 · 标注', route: '/llmops/datasets', type: 'factory', isMain: true },
  { hex: '#0099ff', name: '模型仓库', desc: '版本管理 · 存储 · 检索', route: '/llmops/storage', type: 'warehouse', isMain: true },
  { hex: '#0077ff', name: '训练工厂', desc: '模型训练 · 调优 · 监控', route: '/llmops/training', type: 'servers', isMain: true },
  { hex: '#0055ff', name: '推理验证', desc: 'AI应用 · 机器人 · 部署', route: '/llmops/serving', type: 'cloud', isMain: true },
  { hex: '#0033ff', name: '模型库', desc: '模型沉淀 · 持续迭代', route: '/', type: 'modellib', isMain: false },
]

// 连接线上的标注文字（颜色 = 目标节点的颜色，与管道颜色一致）
const CONN_LABELS = [
  { text: '需求驱动', color: '#00d4ff' }, // 0→1 客户→AI基地
  { text: '智能调度', color: '#00b8ff' }, // 1→2 AI基地→数据采集
  { text: '转换标注', color: '#0099ff' }, // 2→3 数据采集→模型仓库
  { text: '机器执行', color: '#0077ff' }, // 3→4 模型仓库→训练工厂
  { text: 'AI驱动', color: '#0055ff' }, // 4→5 训练工厂→推理验证
  { text: '迭代沉淀', color: '#0033ff' }, // 5→6 推理验证→模型库
  { text: '价值交付', color: '#00e5ff' }, // 6→0 模型库→客户
]

// 7 个节点均匀分布在正圆上（半径 24，中心 z=-12），任意相邻节点距离都相等
const LOOP_POS = (() => {
  const R = 24, cz = -12
  return Array.from({ length: 7 }, (_, i) => {
    const angle = -Math.PI / 2 + i * (2 * Math.PI / 7)
    return new THREE.Vector3(
      parseFloat((R * Math.cos(angle)).toFixed(1)),
      0,
      parseFloat((cz + R * Math.sin(angle)).toFixed(1))
    )
  })
})()

export default {
  name: 'PipelinePage',
  props: {
    embedded: { type: Boolean, default: false },
    progressData: { type: Object, default: null }
  },
  data() {
    return {
      isFullscreen: false,
      allNodes: ALL_NODES.map(n => ({ ...n })),
      hoveredIdx: -1,
      popupHovered: false,
      labelPos: ALL_NODES.map(() => null),
      connLabelDefs: CONN_LABELS,
      connLabelPos: Array(7).fill(null),
      descLabelPos: ALL_NODES.map(() => null),
      internalProgress: null,
    }
  },
  computed: {
    effectiveProgressData() {
      return this.progressData || this.internalProgress
    },
    progressStages() {
      const progress = this.effectiveProgressData
      return (progress && Array.isArray(progress.stages)) ? progress.stages : []
    },
    connLabelPairs() {
      const pairs = []
      this.connLabelPos.forEach((lp, i) => {
        const def = this.connLabelDefs[i]
        if (!lp || !def) return
        const text = def.text
        const half = Math.ceil(text.length / 2)
        pairs.push({ text: text.slice(0, half), color: def.color, pos: lp.left })
        pairs.push({ text: text.slice(half), color: def.color, pos: lp.right })
      })
      return pairs
    }
  },
  watch: {
    $route: {
      immediate: true,
      handler() {
        if (!this.progressData || !Array.isArray(this.progressData.stages) || !this.progressData.stages.length) {
          this.loadPipelineProgress()
        }
      }
    }
  },
  mounted() {
    this.initThree()
    window.addEventListener('resize', this.onResize)
    document.addEventListener('fullscreenchange', this.handleFullscreenChange)
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange)
    document.addEventListener('msfullscreenchange', this.handleFullscreenChange)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize)
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange)
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange)
    document.removeEventListener('msfullscreenchange', this.handleFullscreenChange)
    cancelAnimationFrame(this._raf)
    if (this._fullscreenResizeTimer) clearTimeout(this._fullscreenResizeTimer)
    if (this._controls) this._controls.dispose()
    if (this._renderer) { this._renderer.dispose(); this._renderer.forceContextLoss() }
  },
  methods: {
    async loadPipelineProgress() {
      console.log('[pipeline] request pipeline progress')
      try {
        const progress = await getPipelineProgress()
        const payload = progress && progress.body
          ? progress.body
          : progress && progress.data
            ? progress.data
            : progress
        console.log('[pipeline] pipeline progress loaded', payload)
        if (payload && Array.isArray(payload.stages) && payload.stages.length) {
          this.internalProgress = payload
        }
      } catch (e) {
        console.warn('[pipeline] pipeline progress request failed', e)
      }
    },
    clampPercent(value) {
      const n = Number(value)
      if (Number.isNaN(n)) return 0
      return Math.min(100, Math.max(0, Math.round(n)))
    },
    updatePipeProgress(t) {
      if (!this._connTubes || !this._connTubes.length) return
      const stages = this.progressStages
      if (!stages.length) return

      this._connTubes.forEach((conn, i) => {
        const sourceStage = stages[i % stages.length] || {}
        const targetStage = stages[(i + 1) % stages.length] || {}
        let status = 'pending'
        if (sourceStage.status === 'done' && targetStage.status === 'done') {
          status = 'done'
        } else if (sourceStage.status === 'done' && targetStage.status === 'running') {
          status = 'running'
        }
        const percent = this.clampPercent(targetStage.percent)
        const color = conn.colorB
        const phase = i * 0.65

        let tubeGlow = 0.08
        let innerOpacity = 0.08
        let haloOpacity = 0.04
        let ringGlow = 0.1
        let ringOpacity = 0.22

        if (status === 'done') {
          tubeGlow = 0.92
          innerOpacity = 0.72
          haloOpacity = 0.3
          ringGlow = 0.95
          ringOpacity = 0.78
        } else if (status === 'running') {
          const progressBoost = percent / 100
          const pulse = 0.58 + progressBoost * 0.24 + Math.sin(t * 4 + phase) * 0.12
          tubeGlow = pulse
          innerOpacity = 0.42 + progressBoost * 0.2 + Math.sin(t * 4 + phase) * 0.12
          haloOpacity = 0.15 + progressBoost * 0.12 + Math.sin(t * 3 + phase) * 0.05
          ringGlow = 0.62 + progressBoost * 0.3 + Math.sin(t * 4 + phase) * 0.18
          ringOpacity = 0.52 + Math.sin(t * 3.5 + phase) * 0.08
        }

        conn.tubeMat.emissive.setHex(color)
        conn.tubeMat.emissiveIntensity = tubeGlow
        conn.innerTubeMat.opacity = innerOpacity
        conn.haloTubeMat.opacity = haloOpacity
        conn.decoRings.forEach(({ mesh, material, baseT }) => {
          const speed = status === 'running' ? 0.28 : status === 'done' ? 0.18 : 0
          const flowT = speed > 0 ? (baseT + t * speed) % 1 : baseT
          const flowPoint = conn.curve.getPoint(flowT)
          const flowTangent = conn.curve.getTangent(flowT).normalize()

          mesh.position.copy(flowPoint)
          mesh.lookAt(flowPoint.clone().add(flowTangent))
          material.emissive.setHex(color)
          material.emissiveIntensity = ringGlow
          material.opacity = ringOpacity
        })
      })
    },
    goTo(r) { if (r && r !== '/') this.$router.push(r).catch(() => { }) },
    h2i(h) { return parseInt(h.replace('#', ''), 16) },

    // ===== 全屏控制 =====
    toggleFullscreen() {
      const el = this.$refs.pipelineContainer
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
      this.$nextTick(() => this.onResize())
      if (this._fullscreenResizeTimer) clearTimeout(this._fullscreenResizeTimer)
      this._fullscreenResizeTimer = setTimeout(() => this.onResize(), 120)
    },

    initThree() {
      const el = this.$refs.canvasRef
      const W = el.clientWidth, H = el.clientHeight

      const scene = new THREE.Scene()
      scene.background = new THREE.Color(0x050b1a)
      scene.fog = new THREE.FogExp2(0x050b1a, 0.006)
      this._scene = scene

      // 47° 俯视角：能看清模型顶面和侧面，FOV 宽一点保留透视纵深感
      const cam = new THREE.PerspectiveCamera(30, W / H, 0.1, 600)
      cam.position.set(0, 42, 70)
      cam.lookAt(0, 4, -12)
      this._cam = cam

      const renderer = new THREE.WebGLRenderer({ antialias: true })
      renderer.setSize(W, H)
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
      renderer.shadowMap.enabled = true
      renderer.shadowMap.type = THREE.PCFSoftShadowMap
      el.appendChild(renderer.domElement)
      this._renderer = renderer

      const controls = new OrbitControls(cam, renderer.domElement)
      controls.target.set(0, 4, -12)
      controls.enableDamping = true
      controls.dampingFactor = 0.06
      controls.minDistance = 30
      controls.maxDistance = 250
      controls.update()
      this._controls = controls

      this._mouse = new THREE.Vector2(-99, -99)
      this._raycaster = new THREE.Raycaster()
      this._nodeGroups = []
      this._nodes = []         // per-node animation data
      this._factoryBoxes = []
      this._aiBaseRings = []
      this._cloudGlow = null
      this._aiSphere = null
      this._aiOrbit = null
      this._connMidPts = Array(7).fill(null)
      this._connCurves = Array(7).fill(null)

      this.buildScene(scene)
      this.animate()
    },

    buildScene(scene) {
      scene.add(new THREE.AmbientLight(0x3050a0, 1.6))

      const sun = new THREE.DirectionalLight(0xc8e0ff, 1.0)
      sun.position.set(20, 60, 40)
      sun.castShadow = true
      sun.shadow.camera.left = -70; sun.shadow.camera.right = 70
      sun.shadow.camera.top = 50; sun.shadow.camera.bottom = -50
      scene.add(sun)

      // 青色 rim 光（从背后勾边，增强立体感）
      const rim = new THREE.DirectionalLight(0x00d4c8, 0.6)
      rim.position.set(-20, 30, -80)
      scene.add(rim)
      // 暖色侧光（点亮模型阴影面，让模型更立体）
      const fill = new THREE.DirectionalLight(0x7c63ff, 0.35)
      fill.position.set(-50, 20, 40)
      scene.add(fill)

      // 地面 - 深色科技反光
      const floor = new THREE.Mesh(
        new THREE.PlaneGeometry(400, 400),
        new THREE.MeshStandardMaterial({ color: 0x070e1c, roughness: 0.65, metalness: 0.35 })
      )
      floor.rotation.x = -Math.PI / 2; floor.receiveShadow = true
      scene.add(floor)

      // 发光网格（蓝色系，更亮的中心轴线 + 较暗副网格）
      const grid = new THREE.GridHelper(160, 32, 0x00d4ff, 0x0a2a5a)
      grid.position.y = 0.012
      grid.material.transparent = true
      grid.material.opacity = 0.55
      scene.add(grid)

      // 圆心扫描环（地面装饰）
      const scanRing = new THREE.Mesh(
        new THREE.RingGeometry(6, 6.3, 64),
        new THREE.MeshBasicMaterial({
          color: 0x00d4ff, transparent: true, opacity: 0.7, side: THREE.DoubleSide,
          blending: THREE.AdditiveBlending, depthWrite: false
        })
      )
      scanRing.rotation.x = -Math.PI / 2
      scanRing.position.set(0, 0.02, -12)
      scene.add(scanRing)
      this._scanRing = scanRing

      // 圆心十字基准线
      const cross1 = new THREE.Mesh(new THREE.PlaneGeometry(0.06, 4),
        new THREE.MeshBasicMaterial({ color: 0x00d4ff, transparent: true, opacity: 0.5, side: THREE.DoubleSide }))
      cross1.rotation.x = -Math.PI / 2; cross1.position.set(0, 0.03, -12)
      scene.add(cross1)
      const cross2 = new THREE.Mesh(new THREE.PlaneGeometry(4, 0.06),
        new THREE.MeshBasicMaterial({ color: 0x00d4ff, transparent: true, opacity: 0.5, side: THREE.DoubleSide }))
      cross2.rotation.x = -Math.PI / 2; cross2.position.set(0, 0.03, -12)
      scene.add(cross2)

      // 构建 7 个节点
      LOOP_POS.forEach((pos, i) => {
        const g = new THREE.Group()
        g.position.copy(pos)
        scene.add(g)
        this._nodeGroups.push(g)

        const color = this.h2i(ALL_NODES[i].hex)
        const type = ALL_NODES[i].type

        if (type === 'factory') this.buildFactory(g, color)
        else if (type === 'warehouse') this.buildWarehouse(g, color)
        else if (type === 'servers') this.buildServerRacks(g, color)
        else if (type === 'cloud') this.buildCloudPlatform(g, color)
        else if (type === 'client') this.buildClient(g, color)
        else if (type === 'aibase') this.buildAiBase(g, color)
        else if (type === 'modellib') this.buildModelLib(g, color)

        // 地面发光圆环
        const ringR = ALL_NODES[i].isMain ? 5 : 3.8
        const ring = new THREE.Mesh(
          new THREE.TorusGeometry(ringR, 0.06, 6, 80),
          new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.9, transparent: true, opacity: 0.55 })
        )
        ring.rotation.x = -Math.PI / 2; ring.position.y = 0.04
        g.add(ring)

        // 地面发光圆盘（更柔光晕）
        const glowDisc = new THREE.Mesh(
          new THREE.CircleGeometry(ringR + 0.6, 48),
          new THREE.MeshBasicMaterial({
            color, transparent: true, opacity: 0.18, side: THREE.DoubleSide,
            blending: THREE.AdditiveBlending, depthWrite: false
          })
        )
        glowDisc.rotation.x = -Math.PI / 2; glowDisc.position.y = 0.02
        g.add(glowDisc)

        // 垂直光柱（cyber tower）
        const beam = new THREE.Mesh(
          new THREE.CylinderGeometry(0.18, 0.4, 22, 12, 1, true),
          new THREE.MeshBasicMaterial({
            color, transparent: true, opacity: 0.22, side: THREE.DoubleSide,
            blending: THREE.AdditiveBlending, depthWrite: false
          })
        )
        beam.position.y = 11
        g.add(beam)

        this._nodes.push({ ring, glowDisc, beam, phase: i * 0.9 })
      })

      // 连接管道（7 条，形成闭环）
      this._connTubes = []
      for (let i = 0; i < 7; i++) {
        this.buildConnTube(scene, i)
      }

      // 粒子流动曲线 = 7 段链路管道首尾相连，与可见管道形状完全一致
      const allPts = []
      for (let i = 0; i < 7; i++) {
        const curve = this._connCurves[i]
        const samples = 20
        for (let s = 0; s < samples; s++) {
          allPts.push(curve.getPoint(s / samples))
        }
      }
      this._loopCurve = new THREE.CatmullRomCurve3(allPts, true)

      this._packets = []
      for (let i = 0; i < 3; i++) this.buildPacket(scene, i)

      this.buildStars(scene)
    },

    // ── 客户（人像）──────────────────────────────────────────────
    buildClient(g, color) {
      const skinMat = new THREE.MeshStandardMaterial({ color: 0xf5deb3, roughness: 0.8 })
      const bodyMat = new THREE.MeshStandardMaterial({ color: 0xdce8f8, roughness: 0.6, metalness: 0.05 })
      const suitMat = new THREE.MeshStandardMaterial({ color: 0x2c4a7c, roughness: 0.6 })

      // 底座
      const base = new THREE.Mesh(new THREE.CylinderGeometry(2.2, 2.6, 0.4, 32),
        new THREE.MeshStandardMaterial({ color: 0x8090a8, metalness: 0.3 }))
      base.position.y = 0.2; g.add(base)

        // 腿
        ;[-0.5, 0.5].forEach(x => {
          const leg = new THREE.Mesh(new THREE.BoxGeometry(0.65, 2.6, 0.6), suitMat)
          leg.position.set(x, 1.7, 0); leg.castShadow = true; g.add(leg)
          const shoe = new THREE.Mesh(new THREE.BoxGeometry(0.66, 0.28, 0.95),
            new THREE.MeshStandardMaterial({ color: 0x1a1a2e }))
          shoe.position.set(x, 0.54, 0.18); g.add(shoe)
        })

      // 躯干（西装）
      const torso = new THREE.Mesh(new THREE.BoxGeometry(1.9, 2.6, 0.85), suitMat)
      torso.position.set(0, 3.9, 0); torso.castShadow = true; g.add(torso)

      // 衬衫/领带
      const shirt = new THREE.Mesh(new THREE.BoxGeometry(0.7, 2.0, 0.06),
        new THREE.MeshStandardMaterial({ color: 0xffffff }))
      shirt.position.set(0, 3.8, 0.44); g.add(shirt)
      const tie = new THREE.Mesh(new THREE.BoxGeometry(0.22, 1.5, 0.06),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.5 }))
      tie.position.set(0, 3.7, 0.45); g.add(tie)

        // 手臂
        ;[[-1.35, 0.12], [1.35, -0.12]].forEach(([x, rz]) => {
          const arm = new THREE.Mesh(new THREE.BoxGeometry(0.52, 2.3, 0.52), suitMat)
          arm.position.set(x, 3.7, 0); arm.rotation.z = rz
          arm.castShadow = true; g.add(arm)
          const hand = new THREE.Mesh(new THREE.SphereGeometry(0.28, 10, 8), skinMat)
          hand.position.set(x + rz * 2, 2.5, 0); g.add(hand)
        })

      // 颈部
      const neck = new THREE.Mesh(new THREE.CylinderGeometry(0.26, 0.3, 0.45, 12), skinMat)
      neck.position.y = 5.35; g.add(neck)

      // 头部
      const head = new THREE.Mesh(new THREE.SphereGeometry(0.82, 20, 16), skinMat)
      head.position.y = 6.35; head.castShadow = true; g.add(head)

      // 头发
      const hair = new THREE.Mesh(
        new THREE.SphereGeometry(0.84, 20, 16, 0, Math.PI * 2, 0, Math.PI * 0.52),
        new THREE.MeshStandardMaterial({ color: 0x1a1218, roughness: 0.9 }))
      hair.position.y = 6.35; g.add(hair)

        // 眼睛
        ;[-0.24, 0.24].forEach(ex => {
          const eye = new THREE.Mesh(new THREE.SphereGeometry(0.09, 8, 8),
            new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 2.5 }))
          eye.position.set(ex, 6.42, 0.76); g.add(eye)
        })

    },

    // ── AI基地 ──────────────────────────────────────────────────
    buildAiBase(g, color) {
      // 底座圆台
      const base = new THREE.Mesh(new THREE.CylinderGeometry(2.8, 3.2, 0.6, 32),
        new THREE.MeshStandardMaterial({ color: 0x0a2040, metalness: 0.8 }))
      base.position.y = 0.3; base.castShadow = true; g.add(base)

      const baseRing = new THREE.Mesh(new THREE.TorusGeometry(3.0, 0.07, 6, 64),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 1.2 }))
      baseRing.rotation.x = -Math.PI / 2; baseRing.position.y = 0.62; g.add(baseRing)

      // 半球穹顶
      const dome = new THREE.Mesh(
        new THREE.SphereGeometry(2.8, 32, 16, 0, Math.PI * 2, 0, Math.PI / 2),
        new THREE.MeshStandardMaterial({ color: 0x0d2045, metalness: 0.7, roughness: 0.3 })
      )
      dome.position.y = 0.62; dome.castShadow = true; g.add(dome)

      // 穹顶发光线框
      const domeEdge = new THREE.LineSegments(
        new THREE.EdgesGeometry(new THREE.SphereGeometry(2.82, 12, 6, 0, Math.PI * 2, 0, Math.PI / 2)),
        new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.45 }))
      domeEdge.position.y = 0.62; g.add(domeEdge)

      // 天线
      const antenna = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.08, 3.5, 8),
        new THREE.MeshStandardMaterial({ color: 0x4a90d0, metalness: 0.9 }))
      antenna.position.y = 4.4; g.add(antenna)
      const tip = new THREE.Mesh(new THREE.SphereGeometry(0.2, 8, 8),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 3.0 }))
      tip.position.y = 6.35; g.add(tip)

      // 旋转轨道环 × 2
      this._aiBaseRings = []
        ;[1.8, 2.6].forEach((r, ri) => {
          const ring = new THREE.Mesh(
            new THREE.TorusGeometry(r, 0.05, 6, 64),
            new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.8, transparent: true, opacity: 0.6 })
          )
          ring.position.y = 2.5 + ri * 0.5
          g.add(ring)
          this._aiBaseRings.push({ mesh: ring, speed: (ri === 0 ? 0.014 : -0.018) })
        })

      // 穹顶核心发光球
      const core = new THREE.Mesh(new THREE.SphereGeometry(0.9, 16, 12),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.6, transparent: true, opacity: 0.7 }))
      core.position.y = 2.5; g.add(core)
      this._aiBaseCore = core
    },

    // ── 模型库 ──────────────────────────────────────────────────
    buildModelLib(g, color) {
      // 经典数据库堆叠圆柱
      ;[0, 1, 2, 3, 4].forEach((_s, si) => {
        const h = 0.55, r = 2.8 - si * 0.08
        const disc = new THREE.Mesh(new THREE.CylinderGeometry(r, r, h, 32),
          new THREE.MeshStandardMaterial({ color: 0x0a2030, metalness: 0.8, roughness: 0.2 }))
        disc.position.y = 0.4 + si * 1.35; disc.castShadow = true; g.add(disc)

        // 层间发光环
        const gRing = new THREE.Mesh(new THREE.TorusGeometry(r + 0.04, 0.07, 6, 64),
          new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 1.0 }))
        gRing.rotation.x = -Math.PI / 2
        gRing.position.y = 0.68 + si * 1.35; g.add(gRing)
      })

      // 顶盖发光
      const top = new THREE.Mesh(new THREE.CylinderGeometry(2.85, 2.85, 0.15, 32),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.5, transparent: true, opacity: 0.8 }))
      top.position.y = 7.3; g.add(top)

        // 数据流竖条（正面）
        ;[-0.8, 0, 0.8].forEach(x => {
          const stripe = new THREE.Mesh(new THREE.BoxGeometry(0.06, 7.2, 0.06),
            new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.8 }))
          stripe.position.set(x, 3.7, 2.84); g.add(stripe)
        })

      // 外框
      const edge = new THREE.LineSegments(
        new THREE.EdgesGeometry(new THREE.CylinderGeometry(2.9, 2.9, 7.5, 8)),
        new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.3 }))
      edge.position.y = 3.8; g.add(edge)
    },

    // ── 工厂 (数据采集) ─────────────────────────────────────────
    buildFactory(g, color) {
      const body = new THREE.Mesh(new THREE.BoxGeometry(5.5, 5, 4.5),
        new THREE.MeshStandardMaterial({ color: 0xdde8f0, roughness: 0.7 }))
      body.position.set(0, 2.5, 0); body.castShadow = true; g.add(body)

      const roof = new THREE.Mesh(new THREE.BoxGeometry(5.8, 0.3, 4.8),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.5 }))
      roof.position.set(0, 5.15, 0); g.add(roof)

        ;[[-1.0, 0], [0.9, 0.3]].forEach(([x, z]) => {
          const ch = new THREE.Mesh(new THREE.CylinderGeometry(0.32, 0.42, 4.5, 10),
            new THREE.MeshStandardMaterial({ color: 0xc8d5e0, metalness: 0.3 }))
          ch.position.set(x, 7.4, z); ch.castShadow = true; g.add(ch)
          const sr = new THREE.Mesh(new THREE.TorusGeometry(0.42, 0.1, 6, 24),
            new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.7, transparent: true, opacity: 0.85 }))
          sr.position.set(x, 9.8, z); g.add(sr)
        })

      const beltBase = new THREE.Mesh(new THREE.BoxGeometry(4.2, 0.25, 1.6),
        new THREE.MeshStandardMaterial({ color: 0x0d1a30, metalness: 0.8 }))
      beltBase.position.set(0.3, 0.62, 3.2); g.add(beltBase)
      const beltGlow = new THREE.Mesh(new THREE.BoxGeometry(4.2, 0.06, 0.07),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 1.1 }))
      beltGlow.position.set(0.3, 0.78, 4.0); g.add(beltGlow)

      for (let i = 0; i < 4; i++) {
        const box = new THREE.Mesh(new THREE.BoxGeometry(0.55, 0.55, 0.55),
          new THREE.MeshStandardMaterial({ color: i % 2 === 0 ? 0xe8b44c : 0xc8c8c8, roughness: 0.6 }))
        box.position.set(-1.4 + i * 0.95, 0.99, 3.2); g.add(box)
        this._factoryBoxes.push({ mesh: box, baseX: -1.4 + i * 0.95, offset: i * 0.25 })
      }

      ;[1.4, 3.1].forEach(y => {
        const win = new THREE.Mesh(new THREE.BoxGeometry(4.0, 0.55, 0.06),
          new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.5, transparent: true, opacity: 0.9 }))
        win.position.set(0, y, 2.28); g.add(win)
      })

      const factoryEdge = new THREE.LineSegments(
        new THREE.EdgesGeometry(new THREE.BoxGeometry(5.52, 5.02, 4.52)),
        new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.35 }))
      factoryEdge.position.set(0, 2.5, 0); g.add(factoryEdge)
    },

    // ── 货架 (模型仓库) ─────────────────────────────────────────
    buildWarehouse(g, color) {
      ;[-2.2, 2.2].forEach(xOff => {
        ;[-1.4, 1.4].forEach(z => {
          const p = new THREE.Mesh(new THREE.BoxGeometry(0.2, 8, 0.2),
            new THREE.MeshStandardMaterial({ color: 0x1a6090, metalness: 0.8, roughness: 0.3 }))
          p.position.set(xOff, 4, z); p.castShadow = true; g.add(p)
        })
        for (let s = 0; s < 5; s++) {
          const shelf = new THREE.Mesh(new THREE.BoxGeometry(3, 0.12, 2.9),
            new THREE.MeshStandardMaterial({ color: 0x1e7aaa, metalness: 0.6, roughness: 0.4 }))
          shelf.position.set(xOff, 0.9 + s * 1.5, 0); g.add(shelf)
            ;[-0.75, 0, 0.75].forEach((bx, bi) => {
              const h = bi === 1 ? 1.0 : 0.72
              const box = new THREE.Mesh(new THREE.BoxGeometry(0.65, h, 0.85),
                new THREE.MeshStandardMaterial({ color: bi === 1 ? 0x9caab8 : (bi === 0 ? 0xe8b44c : 0xd4d0c8), roughness: 0.65 }))
              box.position.set(xOff + bx, 1.08 + s * 1.5 + h / 2, 0); g.add(box)
            })
        }
        const stripe = new THREE.Mesh(new THREE.BoxGeometry(0.06, 8, 0.06),
          new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 1 }))
        stripe.position.set(xOff + (xOff > 0 ? 1.5 : -1.5), 4, 0); g.add(stripe)
      })
      const frame = new THREE.LineSegments(
        new THREE.EdgesGeometry(new THREE.BoxGeometry(7, 8.2, 3.0)),
        new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0.4 }))
      frame.position.y = 4.1; g.add(frame)
    },

    // ── 服务器机柜 (训练工厂) ────────────────────────────────────
    buildServerRacks(g, color) {
      ;[-3.5, -1.2, 1.2, 3.5].forEach((x, ri) => {
        const rack = new THREE.Mesh(new THREE.BoxGeometry(1.6, 9, 2.2),
          new THREE.MeshStandardMaterial({ color: 0x0b1424, metalness: 0.88, roughness: 0.12 }))
        rack.position.set(x, 4.5, 0); rack.castShadow = true; g.add(rack)
        const edge = new THREE.LineSegments(
          new THREE.EdgesGeometry(new THREE.BoxGeometry(1.62, 9.02, 2.22)),
          new THREE.LineBasicMaterial({ color, transparent: true, opacity: ri % 2 === 0 ? 0.55 : 0.35 }))
        edge.position.set(x, 4.5, 0); g.add(edge)
        for (let row = 0; row < 7; row++) {
          const led = new THREE.Mesh(new THREE.BoxGeometry(1.1, 0.07, 0.04),
            new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 1.5 }))
          led.position.set(x, 0.9 + row * 1.1, 1.12); g.add(led)
        }
        const panel = new THREE.Mesh(new THREE.BoxGeometry(1.0, 7.5, 0.05),
          new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.15, transparent: true, opacity: 0.65 }))
        panel.position.set(x, 4.5, 1.13); g.add(panel)
        const topLight = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.15, 0.5, 8),
          new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 2.5 }))
        topLight.position.set(x, 9.25, 0); g.add(topLight)
      })
    },

    // ── 云平台 + 机器人 (推理验证) ──────────────────────────────
    // ── 云平台 + 机器人 (推理验证) ──────────────────────────────
    buildCloudPlatform(g, color) {
      const whiteMat = new THREE.MeshStandardMaterial({ color: 0xf4f8fc, metalness: 0.35, roughness: 0.45 })
      const darkMat = new THREE.MeshStandardMaterial({ color: 0x2a1f4a, metalness: 0.6, roughness: 0.35 })

      // 底部发光平台 + 光环
      const plat = new THREE.Mesh(new THREE.CylinderGeometry(4, 4.5, 0.55, 40),
        new THREE.MeshStandardMaterial({ color: 0x1a3a8a, metalness: 0.7, roughness: 0.3 }))
      plat.position.y = 0.27; plat.castShadow = true; g.add(plat)
      const pr = new THREE.Mesh(new THREE.TorusGeometry(4.1, 0.07, 6, 80),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 1.5, transparent: true, opacity: 0.9 }))
      pr.rotation.x = -Math.PI / 2; pr.position.y = 0.56; g.add(pr)

        // 腿
        ;[-0.5, 0.5].forEach(x => {
          const leg = new THREE.Mesh(new THREE.CylinderGeometry(0.32, 0.38, 1.4, 14), darkMat)
          leg.position.set(x, 1.35, 0); leg.castShadow = true; g.add(leg)
          // 膝盖关节
          const knee = new THREE.Mesh(new THREE.SphereGeometry(0.32, 12, 10), whiteMat)
          knee.position.set(x, 1.95, 0); g.add(knee)
          // 脚
          const foot = new THREE.Mesh(new THREE.BoxGeometry(0.78, 0.32, 1.0), whiteMat)
          foot.position.set(x, 0.78, 0.18); foot.castShadow = true; g.add(foot)
        })

      // 躯干 - 圆胖白色
      const torso = new THREE.Mesh(new THREE.BoxGeometry(1.7, 1.7, 1.15), whiteMat)
      torso.position.set(0, 3.05, 0); torso.castShadow = true; g.add(torso)
      // 圆顶 - 让躯干上方更圆润
      const torsoTop = new THREE.Mesh(new THREE.SphereGeometry(0.85, 16, 12, 0, Math.PI * 2, 0, Math.PI / 2), whiteMat)
      torsoTop.position.set(0, 3.9, 0); g.add(torsoTop)
      // 胸口装饰板
      const chest = new THREE.Mesh(new THREE.BoxGeometry(1.1, 1.1, 0.04), darkMat)
      chest.position.set(0, 3.05, 0.6); g.add(chest)
      // 胸口发光圆
      const chestCore = new THREE.Mesh(new THREE.CircleGeometry(0.24, 32),
        new THREE.MeshBasicMaterial({ color, transparent: true, opacity: 0.98, side: THREE.DoubleSide }))
      chestCore.position.set(0, 3.05, 0.625); g.add(chestCore)
      const chestRing = new THREE.Mesh(new THREE.RingGeometry(0.3, 0.38, 32),
        new THREE.MeshBasicMaterial({ color, transparent: true, opacity: 0.85, side: THREE.DoubleSide }))
      chestRing.position.set(0, 3.05, 0.625); g.add(chestRing)

        // 关节手臂
        ;[[-1.15, 1], [1.15, -1]].forEach(([ax, side]) => {
          const shoulder = new THREE.Mesh(new THREE.SphereGeometry(0.34, 14, 12), darkMat)
          shoulder.position.set(ax, 3.5, 0); g.add(shoulder)
          const upperArm = new THREE.Mesh(new THREE.CylinderGeometry(0.22, 0.25, 0.95, 12), whiteMat)
          upperArm.position.set(ax, 2.92, 0); upperArm.castShadow = true; g.add(upperArm)
          const elbow = new THREE.Mesh(new THREE.SphereGeometry(0.26, 12, 10), darkMat)
          elbow.position.set(ax, 2.4, 0); g.add(elbow)
          const forearm = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.22, 0.85, 12), whiteMat)
          forearm.position.set(ax + side * 0.08, 1.95, 0.05); forearm.rotation.z = side * 0.12
          forearm.castShadow = true; g.add(forearm)
          const hand = new THREE.Mesh(new THREE.SphereGeometry(0.28, 14, 12), darkMat)
          hand.position.set(ax + side * 0.18, 1.5, 0.1); g.add(hand)
        })

      // 颈部
      const neck = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.34, 0.32, 12), darkMat)
      neck.position.y = 4.32; g.add(neck)

      // 头 - 大圆球
      const head = new THREE.Mesh(new THREE.SphereGeometry(0.92, 24, 18), whiteMat)
      head.position.y = 5.25; head.castShadow = true; g.add(head)
      // 紫色面罩（下半球）
      const visor = new THREE.Mesh(
        new THREE.SphereGeometry(0.94, 24, 18, 0, Math.PI * 2, Math.PI / 3, Math.PI / 2.4),
        darkMat)
      visor.position.y = 5.25; g.add(visor)
      // 面罩前方一块发光横条作为"显示屏"装饰
      const visorGlow = new THREE.Mesh(
        new THREE.BoxGeometry(0.85, 0.18, 0.04),
        new THREE.MeshBasicMaterial({
          color, transparent: true, opacity: 0.35,
          blending: THREE.AdditiveBlending, depthWrite: false
        }))
      visorGlow.position.set(0, 5.05, 0.78); g.add(visorGlow)

        // 大眼睛
        ;[-0.32, 0.32].forEach(ex => {
          const eye = new THREE.Mesh(new THREE.SphereGeometry(0.18, 14, 12),
            new THREE.MeshBasicMaterial({ color, transparent: true, opacity: 1 }))
          eye.position.set(ex, 5.12, 0.78); g.add(eye)
          const eyeHalo = new THREE.Mesh(new THREE.SphereGeometry(0.32, 14, 12),
            new THREE.MeshBasicMaterial({
              color, transparent: true, opacity: 0.28,
              blending: THREE.AdditiveBlending, depthWrite: false
            }))
          eyeHalo.position.set(ex, 5.12, 0.78); g.add(eyeHalo)
        })

      // 头顶天线
      const antennaRod = new THREE.Mesh(new THREE.CylinderGeometry(0.045, 0.055, 0.55, 8), darkMat)
      antennaRod.position.y = 6.4; g.add(antennaRod)
      const antennaTip = new THREE.Mesh(new THREE.SphereGeometry(0.13, 12, 10),
        new THREE.MeshBasicMaterial({ color, transparent: true, opacity: 1 }))
      antennaTip.position.y = 6.78; g.add(antennaTip)

      // 头顶上方的云朵装饰（大号蓬松版）
      const cloudMat = new THREE.MeshStandardMaterial({ color: 0xeaf4ff, roughness: 0.78, metalness: 0.05 })
        ;[
          [0, 9.0, 0, 2.0],          // 中心
          [-2.4, 8.4, 0, 1.6],       // 左外
          [2.4, 8.4, 0, 1.6],        // 右外
          [-1.2, 8.7, 0, 1.4],       // 左中
          [1.2, 8.7, 0, 1.4],        // 右中
          [0, 10.0, 0, 1.3],         // 顶部蓬
          [-1.3, 9.7, 0, 1.1],       // 顶部左
          [1.3, 9.7, 0, 1.1],        // 顶部右
          [-3.3, 8.1, 0, 1.05],      // 左下尾
          [3.3, 8.1, 0, 1.05],       // 右下尾
        ].forEach(([x, y, z, r]) => {
          const s = new THREE.Mesh(new THREE.SphereGeometry(r, 16, 12), cloudMat)
          s.position.set(x, y, z); s.castShadow = true; g.add(s)
        })
      const cloudGlow = new THREE.Mesh(new THREE.SphereGeometry(1.3, 16, 12),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.5, transparent: true, opacity: 0.5 }))
      cloudGlow.position.set(0, 9.0, 0); g.add(cloudGlow)
      this._cloudGlow = cloudGlow

      // AI 悬浮球 + 轨道环
      const aiSphere = new THREE.Mesh(new THREE.SphereGeometry(0.6, 16, 12),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.7, transparent: true, opacity: 0.88 }))
      aiSphere.position.set(-2.8, 5.0, 0); g.add(aiSphere)
      this._aiSphere = aiSphere
      const aiOrbit = new THREE.Mesh(new THREE.TorusGeometry(0.95, 0.035, 6, 48),
        new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.9, transparent: true, opacity: 0.65 }))
      aiOrbit.position.set(-2.8, 5.0, 0); g.add(aiOrbit)
      this._aiOrbit = aiOrbit
    },

    // ── 闭环连接管道 (第 i 段：节点 i → 节点 (i+1)%7) ─────────
    buildConnTube(scene, i) {
      const next = (i + 1) % 7
      const from = LOOP_POS[i]
      const to = LOOP_POS[next]
      const colorA = this.h2i(ALL_NODES[i].hex)
      const colorB = this.h2i(ALL_NODES[next].hex)

      // 选段中间控制点，稍微上拱
      const mid = new THREE.Vector3().addVectors(from, to).multiplyScalar(0.5)
      mid.y = 6.5

      const segCurve = new THREE.CatmullRomCurve3([
        new THREE.Vector3(from.x, 3.5, from.z),
        new THREE.Vector3(mid.x, mid.y, mid.z),
        new THREE.Vector3(to.x, 3.5, to.z),
      ])

      this._connMidPts[i] = segCurve.getPoint(0.5)
      this._connCurves[i] = segCurve

      // 主管道，更粗更有质感
      const tubeGeo = new THREE.TubeGeometry(segCurve, 64, 0.35, 16, false)
      const tubeMat = new THREE.MeshStandardMaterial({
        color: colorB,
        metalness: 0.7,
        roughness: 0.2,
        emissive: colorB,
        emissiveIntensity: 0.3
      })
      const tube = new THREE.Mesh(tubeGeo, tubeMat)
      tube.castShadow = true
      tube.receiveShadow = true
      scene.add(tube)

      // 内层发光管道
      const innerTubeGeo = new THREE.TubeGeometry(segCurve, 64, 0.25, 12, false)
      const innerTubeMat = new THREE.MeshBasicMaterial({
        color: colorB,
        transparent: true,
        opacity: 0.5,
        blending: THREE.AdditiveBlending
      })
      const innerTube = new THREE.Mesh(innerTubeGeo, innerTubeMat)
      scene.add(innerTube)

      // 外层光晕
      const haloTubeGeo = new THREE.TubeGeometry(segCurve, 64, 0.6, 12, false)
      const haloTubeMat = new THREE.MeshBasicMaterial({
        color: colorB,
        transparent: true,
        opacity: 0.15,
        blending: THREE.AdditiveBlending,
        depthWrite: false
      })
      const haloTube = new THREE.Mesh(haloTubeGeo, haloTubeMat)
      scene.add(haloTube)

      // 管道分段装饰环
      const decoRings = []
      const ringCount = 8
      for (let r = 0; r < ringCount; r++) {
        const t = (r + 0.5) / ringCount
        const pos = segCurve.getPoint(t)
        const tangent = segCurve.getTangent(t).normalize()

        const ringGeo = new THREE.TorusGeometry(0.4, 0.06, 8, 32)
        const ringMat = new THREE.MeshStandardMaterial({
          color: colorB,
          metalness: 0.9,
          roughness: 0.1,
          emissive: colorB,
          emissiveIntensity: 0.5,
          transparent: true,
          opacity: 0.8
        })
        const ring = new THREE.Mesh(ringGeo, ringMat)
        ring.position.copy(pos)
        ring.lookAt(pos.clone().add(tangent))
        scene.add(ring)
        decoRings.push({ mesh: ring, material: ringMat, baseT: t })
      }

      this._connTubes.push({
        tube,
        innerTube,
        haloTube,
        tubeMat,
        innerTubeMat,
        haloTubeMat,
        decoRings,
        colorA,
        colorB,
        curve: segCurve
      })
    },

    buildPacket(scene, idx) {
      // 3 个机器人组队，间距 0.04（约一个机器人身位），一起绕圈
      const t = idx * 0.04
      const segIdx = Math.floor(t * 7) % 7
      const color = this.h2i(ALL_NODES[(segIdx + 1) % 7].hex)

      // 运输机器人组：更大更有质感
      const robot = new THREE.Group()

      // 主体（圆柱 + 球体组合）
      const bodyMat = new THREE.MeshStandardMaterial({
        color: 0xd0e5ff,
        metalness: 0.7,
        roughness: 0.2,
        emissive: 0x003080,
        emissiveIntensity: 0.2
      })

      // 圆柱体主体
      const bodyCylinder = new THREE.Mesh(
        new THREE.CylinderGeometry(0.45, 0.45, 0.6, 16),
        bodyMat
      )
      bodyCylinder.rotation.x = Math.PI / 2
      bodyCylinder.castShadow = true
      robot.add(bodyCylinder)

      // 两端球体
      const bodySphere1 = new THREE.Mesh(
        new THREE.SphereGeometry(0.45, 16, 12),
        bodyMat
      )
      bodySphere1.position.z = 0.3
      bodySphere1.castShadow = true
      robot.add(bodySphere1)

      const bodySphere2 = new THREE.Mesh(
        new THREE.SphereGeometry(0.45, 16, 12),
        bodyMat
      )
      bodySphere2.position.z = -0.3
      bodySphere2.castShadow = true
      robot.add(bodySphere2)

      // 舱体装饰环
      const trimMat = new THREE.MeshStandardMaterial({
        color: color,
        metalness: 0.9,
        roughness: 0.1,
        emissive: color,
        emissiveIntensity: 0.5
      })
      for (let tr = 0; tr < 3; tr++) {
        const trim = new THREE.Mesh(new THREE.TorusGeometry(0.48, 0.04, 8, 32), trimMat)
        trim.rotation.x = Math.PI / 2
        trim.position.y = -0.2 + tr * 0.2
        robot.add(trim)
      }

      // 顶部货箱（发光，更大）
      const cargoMat = new THREE.MeshStandardMaterial({
        color,
        emissive: color,
        emissiveIntensity: 1.2
      })
      const cargo = new THREE.Mesh(new THREE.BoxGeometry(0.55, 0.45, 0.55), cargoMat)
      cargo.position.y = 0.8
      cargo.castShadow = true
      robot.add(cargo)

      // 货箱装饰线
      const cargoWire = new THREE.LineSegments(
        new THREE.EdgesGeometry(new THREE.BoxGeometry(0.57, 0.47, 0.57)),
        new THREE.LineBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.5 })
      )
      cargoWire.position.copy(cargo.position)
      robot.add(cargoWire)

      // 底部推进器（多层光环）
      const thrusterMat1 = new THREE.MeshBasicMaterial({
        color,
        transparent: true,
        opacity: 0.8,
        blending: THREE.AdditiveBlending
      })
      const thruster1 = new THREE.Mesh(new THREE.TorusGeometry(0.55, 0.06, 8, 32), thrusterMat1)
      thruster1.rotation.x = Math.PI / 2
      thruster1.position.y = -0.5
      robot.add(thruster1)

      const thrusterMat2 = new THREE.MeshBasicMaterial({
        color: 0x00d4ff,
        transparent: true,
        opacity: 0.5,
        blending: THREE.AdditiveBlending
      })
      const thruster2 = new THREE.Mesh(new THREE.TorusGeometry(0.35, 0.04, 8, 24), thrusterMat2)
      thruster2.rotation.x = Math.PI / 2
      thruster2.position.y = -0.55
      robot.add(thruster2)

      // 底部喷射光锥
      const coneMat = new THREE.MeshBasicMaterial({
        color,
        transparent: true,
        opacity: 0.3,
        blending: THREE.AdditiveBlending,
        side: THREE.DoubleSide
      })
      const cone = new THREE.Mesh(new THREE.ConeGeometry(0.4, 0.8, 16), coneMat)
      cone.position.y = -1.0
      robot.add(cone)

      // 外层光晕（更大更亮）
      const haloMat = new THREE.MeshBasicMaterial({
        color, transparent: true, opacity: 0.25,
        blending: THREE.AdditiveBlending, depthWrite: false
      })
      const halo = new THREE.Mesh(new THREE.SphereGeometry(1.2, 16, 12), haloMat)
      robot.add(halo)

      scene.add(robot)
      this._packets.push({
        robot,
        bodyCylinder,
        bodySphere1,
        bodySphere2,
        cargo,
        cargoWire,
        cargoMat,
        thruster1,
        thruster2,
        cone,
        thrusterMat1,
        thrusterMat2,
        haloMat,
        t
      })
    },

    buildStars(scene) {
      const cnt = 2200
      const pos = new Float32Array(cnt * 3)
      const col = new Float32Array(cnt * 3)
      const pal = [new THREE.Color(0x00e5ff), new THREE.Color(0x00d4ff), new THREE.Color(0x0099ff), new THREE.Color(0x0077ff), new THREE.Color(0x0055ff), new THREE.Color(0x0040ff)]
      for (let i = 0; i < cnt; i++) {
        pos[i * 3] = (Math.random() - 0.5) * 360
        pos[i * 3 + 1] = Math.random() * 140 + 14
        pos[i * 3 + 2] = (Math.random() - 0.5) * 360
        const c = pal[Math.floor(Math.random() * pal.length)]
        col[i * 3] = c.r; col[i * 3 + 1] = c.g; col[i * 3 + 2] = c.b
      }
      const geo = new THREE.BufferGeometry()
      geo.setAttribute('position', new THREE.BufferAttribute(pos, 3))
      geo.setAttribute('color', new THREE.BufferAttribute(col, 3))
      scene.add(new THREE.Points(geo, new THREE.PointsMaterial({ size: 0.22, vertexColors: true, transparent: true, opacity: 0.6 })))
    },

    animate() {
      this._raf = requestAnimationFrame(this.animate)
      const t = Date.now() * 0.001

      // 运输机器人沿管道飞行，朝向跟随运动方向，并轻微上下浮动
      const aheadStep = 0.003
      this._packets.forEach(p => {
        p.t = (p.t + 0.0008) % 1
        const pt = this._loopCurve.getPoint(p.t)
        const ahead = this._loopCurve.getPoint((p.t + aheadStep) % 1)
        // 上浮 + 轻微 bob（更大幅度）
        const bob = Math.sin(t * 3 + p.t * 18) * 0.25
        p.robot.position.set(pt.x, pt.y + 0.8 + bob, pt.z)
        // 朝向运动方向（绕 y 轴）
        p.robot.rotation.y = Math.atan2(ahead.x - pt.x, ahead.z - pt.z)
        // 货箱缓慢自旋
        p.cargo.rotation.y += 0.03
        p.cargo.rotation.x = Math.sin(t * 2) * 0.1
        // 颜色 = 目标节点 / 管道色
        const seg = Math.floor(p.t * 7) % 7
        const c = this.h2i(ALL_NODES[(seg + 1) % 7].hex)
        p.cargoMat.color.setHex(c); p.cargoMat.emissive.setHex(c)
        p.thrusterMat1.color.setHex(c)
        p.haloMat.color.setHex(c)
        p.cone.material.color.setHex(c)
        // 推进器脉冲动画
        const pulse = 0.8 + Math.sin(t * 8) * 0.2
        p.thruster1.scale.setScalar(pulse)
        p.thruster2.scale.setScalar(pulse)
      })

      // 根据后台进度点亮对应管道
      this.updatePipeProgress(t)

      // 圆心扫描环呼吸 + 旋转
      if (this._scanRing) {
        const s = 1 + Math.sin(t * 1.6) * 0.08
        this._scanRing.scale.set(s, s, 1)
        this._scanRing.material.opacity = 0.55 + Math.sin(t * 1.6) * 0.2
      }

      // 节点光圈脉冲
      this._nodes.forEach((n, i) => {
        if (!n.ring) return
        const isHov = this.hoveredIdx === i
        const sc = isHov ? 1.12 + Math.sin(t * 5) * 0.06 : 1 + Math.sin(t * 1.4 + n.phase) * 0.03
        n.ring.scale.set(sc, 1, sc)
        n.ring.material.emissiveIntensity = isHov ? 1.7 : 0.85 + Math.sin(t * 1.4 + n.phase) * 0.22
      })

      // 传送带箱子
      this._factoryBoxes.forEach(b => {
        b.mesh.position.x = b.baseX + ((t * 0.35 + b.offset) % 1) * 0.7 - 0.35
      })



      // 推理云发光
      if (this._cloudGlow) {
        this._cloudGlow.material.emissiveIntensity = 0.4 + Math.sin(t * 2.2) * 0.3
        this._cloudGlow.position.y = 6.8 + Math.sin(t * 0.9) * 0.2
      }
      if (this._aiSphere) { this._aiSphere.position.y = 4.5 + Math.sin(t * 1.1) * 0.4; this._aiSphere.rotation.y += 0.018 }
      if (this._aiOrbit) {
        this._aiOrbit.position.y = 4.5 + Math.sin(t * 1.1) * 0.4
        this._aiOrbit.rotation.y += 0.02
        this._aiOrbit.rotation.x = Math.PI / 4 + Math.sin(t * 0.5) * 0.3
      }

      // 节点垂直光柱呼吸
      this._nodes.forEach((n, i) => {
        if (n.beam) n.beam.material.opacity = 0.18 + Math.sin(t * 1.6 + i * 0.7) * 0.08
        if (n.glowDisc) n.glowDisc.material.opacity = 0.15 + Math.sin(t * 1.6 + i * 0.7) * 0.06
      })

      // 鼠标 raycaster
      if (!this.popupHovered) {
        this._raycaster.setFromCamera(this._mouse, this._cam)
        let hit = -1
        for (let i = 0; i < this._nodeGroups.length; i++) {
          if (this._raycaster.intersectObjects(this._nodeGroups[i].children, true).length > 0) { hit = i; break }
        }
        this.hoveredIdx = hit
      }

      // 允许用户拖拽时接管相机，无操作时保持当前视角
      this._controls.update()

      this._renderer.render(this._scene, this._cam)
      this.updateLabels()
    },

    updateLabels() {
      const el = this.$refs.canvasRef
      const W = el.clientWidth, H = el.clientHeight
      this.labelPos = LOOP_POS.map(p => {
        const v = new THREE.Vector3(p.x, -1.8, p.z)
        v.project(this._cam)
        return { x: (v.x * 0.5 + 0.5) * W, y: (-v.y * 0.5 + 0.5) * H }
      })
      // desc 投影自模型顶部（Y=10），底部对齐投影点并额外留 10px 间隙
      const TOP_GUARD = 90, BTM_GUARD = H - 30
      this.descLabelPos = LOOP_POS.map(p => {
        const v = new THREE.Vector3(p.x, 10, p.z)
        v.project(this._cam)
        const sx = (v.x * 0.5 + 0.5) * W
        const sy = (-v.y * 0.5 + 0.5) * H - 10  // 标签底边距模型顶 10px
        if (sx < -100 || sx > W + 100 || sy < TOP_GUARD || sy > BTM_GUARD) return null
        return { x: sx, y: sy }
      })

      this.connLabelPos = this._connMidPts.map((pt, i) => {
        if (!pt || !this._connCurves[i]) return null
        const v = pt.clone()
        v.project(this._cam)
        const sx = (v.x * 0.5 + 0.5) * W
        const sy = (-v.y * 0.5 + 0.5) * H
        // 沿曲线切线方向取垂直，前后两半文字分置链路两侧
        const pA = this._connCurves[i].getPoint(0.44).clone().project(this._cam)
        const pB = this._connCurves[i].getPoint(0.56).clone().project(this._cam)
        const dsx = (pB.x - pA.x) * W * 0.5
        const dsy = -(pB.y - pA.y) * H * 0.5
        const len = Math.sqrt(dsx * dsx + dsy * dsy) || 1
        const perpX = -dsy / len
        const perpY = dsx / len
        const D = 12
        const left = { x: sx + perpX * D, y: sy + perpY * D }
        const right = { x: sx - perpX * D, y: sy - perpY * D }
        const inView = p => p.x >= -60 && p.x <= W + 60 && p.y >= TOP_GUARD && p.y <= BTM_GUARD
        if (!inView(left) || !inView(right)) return null
        return { left, right }
      })
    },

    onMouseMove(e) {
      const el = this.$refs.canvasRef
      const rect = el.getBoundingClientRect()
      this._mouse.x = ((e.clientX - rect.left) / el.clientWidth) * 2 - 1
      this._mouse.y = -((e.clientY - rect.top) / el.clientHeight) * 2 + 1
    },

    onResize() {
      const el = this.$refs.canvasRef
      if (!el) return
      const W = el.clientWidth, H = el.clientHeight
      if (!W || !H || !this._cam || !this._renderer) return
      this._cam.aspect = W / H; this._cam.updateProjectionMatrix()
      this._renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
      this._renderer.setSize(W, H)
    }
  }
}
</script>

<style scoped>
.pipeline-page {
  position: relative;
  width: 100%;
  height: calc(100vh - 84px);
  overflow: hidden;
  background: #050b1a;
}

/* 全屏按钮 */
.pipeline-fullscreen-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 100;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(0, 20, 50, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: #74c0fc;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.pipeline-fullscreen-btn:hover {
  background: rgba(0, 212, 255, 0.2);
  border-color: rgba(0, 212, 255, 0.6);
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.2);
  transform: scale(1.05);
  color: #00d4ff;
}

.pipeline-fullscreen-btn i {
  font-size: 18px;
}

/* 全屏模式 */
.pipeline-page:fullscreen {
  height: 100vh !important;
  width: 100vw !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  background: #050b1a;
}

.pipeline-page:-webkit-full-screen {
  height: 100vh !important;
  width: 100vw !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  background: #050b1a;
}

.pipeline-page:-moz-full-screen {
  height: 100vh !important;
  width: 100vw !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  background: #050b1a;
}

/* 四角科技感装饰边框 */
.pipeline-page::before,
.pipeline-page::after {
  content: '';
  position: absolute;
  width: 80px;
  height: 80px;
  z-index: 15;
  pointer-events: none;
}

.pipeline-page::before {
  top: 16px;
  left: 16px;
  border-top: 2px solid #00d4ff;
  border-left: 2px solid #00d4ff;
  box-shadow: -2px -2px 14px rgba(0, 212, 255, 0.35);
}

.pipeline-page::after {
  bottom: 16px;
  right: 16px;
  border-bottom: 2px solid #00d4ff;
  border-right: 2px solid #00d4ff;
  box-shadow: 2px 2px 14px rgba(0, 212, 255, 0.35);
}

.pipeline-page.embedded {
  height: 100%;
}

.three-canvas {
  width: 100%;
  height: 100%;
}

.hud-top-fade {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: linear-gradient(180deg, rgba(5, 11, 26, 0.95) 0%, transparent 100%);
  pointer-events: none;
  z-index: 10;
}

.hud-center {
  position: absolute;
  top: 6%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
  z-index: 20;
}

.hud-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.24em;
  color: #00d4ff;
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.35);
  border-radius: 3px;
  padding: 2px 10px;
  margin-bottom: 8px;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.6);
}

.hud-title {
  font-size: 22px;
  font-weight: 700;
  color: #e8f8ff;
  letter-spacing: 0.14em;
  text-shadow: 0 0 18px rgba(0, 212, 200, 0.7), 0 0 4px rgba(0, 212, 200, 0.85);
  margin-bottom: 10px;
}

/* 标题下方装饰线 */
.hud-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.hud-line span {
  display: block;
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 200, 0.55));
}

.hud-line span:last-child {
  background: linear-gradient(90deg, rgba(0, 212, 200, 0.55), transparent);
}

.hud-line i {
  display: block;
  width: 5px;
  height: 5px;
  background: #00d4c8;
  border-radius: 50%;
  box-shadow: 0 0 8px #00d4c8;
}

/* 节点标签 - 深色科技玻璃 */
.node-label {
  position: absolute;
  transform: translate(-50%, 0);
  text-align: center;
  cursor: default;
  padding: 7px 14px 9px;
  background: rgba(8, 18, 36, 0.78);
  border: 1px solid var(--c);
  border-radius: 9px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow:
    0 0 18px color-mix(in srgb, var(--c) 40%, transparent),
    inset 0 0 12px color-mix(in srgb, var(--c) 15%, transparent);
  transition: transform 0.16s, box-shadow 0.16s;
  pointer-events: auto;
  white-space: nowrap;
}

/* 核心链路模块稍大 */
.node-label.is-main {
  padding: 9px 18px 11px;
  border-width: 1.5px;
}

.node-label.active {
  box-shadow: 0 0 28px color-mix(in srgb, var(--c) 55%, transparent);
  transform: translate(-50%, -5px);
}

.nl-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--c);
  text-shadow: 0 0 8px color-mix(in srgb, var(--c) 70%, transparent);
}

.is-main .nl-name {
  font-size: 14px;
}

/* 悬停弹窗 - 深色科技 */
.node-popup {
  position: absolute;
  transform: translate(-50%, -100%);
  background: rgba(8, 18, 36, 0.92);
  border: 1.5px solid var(--c);
  border-radius: 10px;
  padding: 10px 16px 12px;
  pointer-events: auto;
  white-space: nowrap;
  z-index: 30;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow:
    0 6px 26px color-mix(in srgb, var(--c) 35%, rgba(0, 0, 0, 0.4)),
    inset 0 0 14px color-mix(in srgb, var(--c) 12%, transparent);
}

.node-popup::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  width: 13px;
  height: 13px;
  background: rgba(8, 18, 36, 0.92);
  border-right: 1.5px solid var(--c);
  border-bottom: 1.5px solid var(--c);
  transform: translateX(-50%) rotate(45deg);
}

.popup-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--c);
  margin-bottom: 5px;
  text-shadow: 0 0 8px color-mix(in srgb, var(--c) 60%, transparent);
}

.popup-desc {
  font-size: 12px;
  color: #b8c5d6;
  letter-spacing: 0.04em;
}

.node-popup.clickable {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.node-popup.clickable:hover {
  transform: translate(-50%, -103%);
  box-shadow: 0 10px 30px color-mix(in srgb, var(--c) 35%, rgba(0, 0, 0, 0.15));
}

.popup-fade-enter-active,
.popup-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.popup-fade-enter,
.popup-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -88%);
}

/* 链路注释：两字短标签，分置链路两侧 - 深色场景同色发光 */
.conn-label {
  position: absolute;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: 700;
  color: var(--c);
  letter-spacing: 0.14em;
  white-space: nowrap;
  pointer-events: none;
  padding: 1px 4px;
  text-shadow:
    0 0 4px color-mix(in srgb, var(--c) 90%, transparent),
    0 0 10px color-mix(in srgb, var(--c) 55%, transparent),
    0 1px 3px rgba(0, 0, 0, 0.8);
}

/* 全息弹窗面板 */
.hologram-panel {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 25;
  display: flex;
  flex-direction: column;
  gap: 16px;
  pointer-events: none;
}

.hologram-left {
  left: 24px;
}

.hologram-right {
  right: 24px;
}

/* 全息弹窗项 */
.hologram-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(8, 18, 36, 0.95) 0%, rgba(12, 26, 52, 0.92) 100%);
  border: 1.5px solid var(--c);
  border-radius: 12px;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow:
    0 0 30px color-mix(in srgb, var(--c) 25%, transparent),
    0 8px 32px rgba(0, 0, 0, 0.5),
    inset 0 0 20px color-mix(in srgb, var(--c) 10%, transparent);
  overflow: hidden;
  pointer-events: auto;
  transition: all 0.3s ease;
}

.hologram-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--c) 15%, transparent), transparent);
  animation: hologramScan 3s ease-in-out infinite;
}

.hologram-item::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, var(--c), transparent, var(--c), transparent);
  background-size: 200% 200%;
  border-radius: 14px;
  z-index: -1;
  opacity: 0.3;
  animation: hologramBorder 4s linear infinite;
}

.hologram-item:hover {
  transform: translateX(8px);
  box-shadow:
    0 0 50px color-mix(in srgb, var(--c) 40%, transparent),
    0 12px 40px rgba(0, 0, 0, 0.6),
    inset 0 0 30px color-mix(in srgb, var(--c) 15%, transparent);
}

.hologram-right .hologram-item:hover {
  transform: translateX(-8px);
}

/* 全息图标 */
.hologram-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle, color-mix(in srgb, var(--c) 20%, transparent) 0%, transparent 70%);
  border: 1.5px solid var(--c);
  border-radius: 10px;
  box-shadow:
    0 0 20px color-mix(in srgb, var(--c) 30%, transparent),
    inset 0 0 15px color-mix(in srgb, var(--c) 15%, transparent);
}

.hologram-icon svg {
  width: 24px;
  height: 24px;
  color: var(--c);
  filter: drop-shadow(0 0 8px var(--c));
  animation: hologramPulse 2s ease-in-out infinite;
}

/* 全息内容 */
.hologram-content {
  flex: 1;
  min-width: 0;
}

.hologram-title {
  font-size: 16px;
  font-weight: 800;
  color: var(--c);
  margin-bottom: 4px;
  text-shadow: 0 0 12px color-mix(in srgb, var(--c) 60%, transparent);
  letter-spacing: 0.05em;
}

.hologram-desc {
  font-size: 12px;
  color: #9fb3c8;
  letter-spacing: 0.03em;
  text-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
}

/* 动画关键帧 */
@keyframes hologramScan {
  0% {
    left: -100%;
  }

  50%,
  100% {
    left: 100%;
  }
}

@keyframes hologramBorder {
  0% {
    background-position: 0% 50%;
  }

  100% {
    background-position: 200% 50%;
  }
}

@keyframes hologramPulse {

  0%,
  100% {
    opacity: 0.8;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}
</style>
