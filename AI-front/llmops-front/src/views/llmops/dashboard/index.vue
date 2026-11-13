<template>
  <div class="dashboard-container">
    <div class="dashboard-content">
      <!-- 欢迎横幅 -->
      <div class="welcome-banner">
        <div class="banner-left">
          <h1 class="welcome-title">欢迎回来，{{ userName }}</h1>
          <p class="welcome-subtitle">今天有 {{ projectIndex.task || 0 }} 个训练任务正在运行，{{ projectIndex.serviceUnreadyCount || 0 }} 个服务需要关注</p>
          <div class="banner-buttons">
            <!-- <el-button class="btn-white" size="small" @click="showCreateDialog">
              <i class="el-icon-plus"></i> 创建项目
            </el-button>
            <el-button class="btn-outline" size="small">
              <i class="el-icon-s-data"></i> 启动环境
            </el-button> -->
          </div>
        </div>
        <div class="banner-right">
          <div class="stat-number">{{ projectIndex.aiExperimentCount || 0 }}</div>
          <div class="stat-label">当前实验</div>
        </div>
      </div>

      <!-- 资源概览 -->
      <div class="section">
        <h2 class="section-title">资源概览</h2>
        <div class="resource-grid">
          <!-- GPU卡片 -->
          <div class="resource-card card-hover">
            <div class="card-header">
              <div class="card-icon-wrap">
                <div class="card-icon purple-icon">
                  <i class="fa fa-microchip"></i>
                </div>
                <div class="card-info">
                  <div class="card-label">GPU使用率</div>
                  <div class="card-value">{{ resourceOverview.gpuUsed }}/{{ resourceOverview.gpuLimit }} 卡</div>
                </div>
              </div>
              <div class="progress-ring-container">
                <svg class="progress-ring" viewBox="0 0 64 64">
                  <circle class="progress-ring-bg" cx="32" cy="32" r="28" stroke-width="4" fill="none"></circle>
                  <circle
                    class="progress-ring-fill purple-fill"
                    cx="32" cy="32" r="28"
                    stroke-width="4" fill="none"
                    stroke-dasharray="175.9"
                    :stroke-dashoffset="getDashOffset(resourceOverview.gpuUsed, resourceOverview.gpuLimit)"
                  ></circle>
                </svg>
                <span class="progress-text">{{ calculateProgress(resourceOverview.gpuUsed, resourceOverview.gpuLimit) }}%</span>
              </div>
            </div>
            <div class="status-tags">
              <span class="status-tag tag-green">空闲 {{ Math.max(0, parseFloat(resourceOverview.gpuLimit || 0) - parseFloat(resourceOverview.gpuUsed || 0)) }}卡</span>
              <span class="status-tag tag-blue">运行 {{ resourceOverview.gpuUsed }}卡</span>
            </div>
          </div>

          <!-- CPU卡片 -->
          <div class="resource-card card-hover">
            <div class="card-header">
              <div class="card-icon-wrap">
                <div class="card-icon orange-icon">
                  <i class="el-icon-cpu"></i>
                </div>
                <div class="card-info">
                  <div class="card-label">CPU使用率</div>
                  <div class="card-value">{{ resourceOverview.cpuUsed }}/{{ resourceOverview.cpuLimit }} 核</div>
                </div>
              </div>
              <div class="progress-ring-container">
                <svg class="progress-ring" viewBox="0 0 64 64">
                  <circle class="progress-ring-bg" cx="32" cy="32" r="28" stroke-width="4" fill="none"></circle>
                  <circle
                    class="progress-ring-fill orange-fill"
                    cx="32" cy="32" r="28"
                    stroke-width="4" fill="none"
                    stroke-dasharray="175.9"
                    :stroke-dashoffset="getDashOffset(resourceOverview.cpuUsed, resourceOverview.cpuLimit)"
                  ></circle>
                </svg>
                <span class="progress-text">{{ calculateProgress(resourceOverview.cpuUsed, resourceOverview.cpuLimit) }}%</span>
              </div>
            </div>
            <div class="progress-bar-container">
              <div class="progress-bar">
                <div
                  class="progress-bar-fill orange-bg"
                  :style="{ width: calculateProgress(resourceOverview.cpuUsed, resourceOverview.cpuLimit) + '%' }"
                ></div>
              </div>
            </div>
          </div>

          <!-- 内存卡片 -->
          <div class="resource-card card-hover">
            <div class="card-header">
              <div class="card-icon-wrap">
                <div class="card-icon green-icon">
                  <i class="fa fa-database"></i>
                </div>
                <div class="card-info">
                  <div class="card-label">内存使用</div>
                  <div class="card-value">{{ resourceOverview.memoryUsed }}/{{ resourceOverview.memoryLimit }} GB</div>
                </div>
              </div>
              <div class="progress-ring-container">
                <svg class="progress-ring" viewBox="0 0 64 64">
                  <circle class="progress-ring-bg" cx="32" cy="32" r="28" stroke-width="4" fill="none"></circle>
                  <circle
                    class="progress-ring-fill green-fill"
                    cx="32" cy="32" r="28"
                    stroke-width="4" fill="none"
                    stroke-dasharray="175.9"
                    :stroke-dashoffset="getDashOffset(resourceOverview.memoryUsed, resourceOverview.memoryLimit)"
                  ></circle>
                </svg>
                <span class="progress-text">{{ calculateProgress(resourceOverview.memoryUsed, resourceOverview.memoryLimit) }}%</span>
              </div>
            </div>
            <div class="progress-bar-container">
              <div class="progress-bar">
                <div
                  class="progress-bar-fill green-bg"
                  :style="{ width: calculateProgress(resourceOverview.memoryUsed, resourceOverview.memoryLimit) + '%' }"
                ></div>
              </div>
            </div>
          </div>

          <!-- 存储卡片 -->
          <div class="resource-card card-hover">
            <div class="card-header">
              <div class="card-icon-wrap">
                <div class="card-icon blue-icon">
                  <i class="el-icon-coin"></i>
                </div>
                <div class="card-info">
                  <div class="card-label">存储使用</div>
                  <div class="card-value">{{ resourceOverview.storageUsed }}/{{ resourceOverview.storageLimit }} GB</div>
                </div>
              </div>
              <div class="progress-ring-container">
                <svg class="progress-ring" viewBox="0 0 64 64">
                  <circle class="progress-ring-bg" cx="32" cy="32" r="28" stroke-width="4" fill="none"></circle>
                  <circle
                    class="progress-ring-fill blue-fill"
                    cx="32" cy="32" r="28"
                    stroke-width="4" fill="none"
                    stroke-dasharray="175.9"
                    :stroke-dashoffset="getDashOffset(resourceOverview.storageUsed, resourceOverview.storageLimit)"
                  ></circle>
                </svg>
                <span class="progress-text">{{ calculateProgress(resourceOverview.storageUsed, resourceOverview.storageLimit) }}%</span>
              </div>
            </div>
            <div class="progress-bar-container">
              <div class="progress-bar">
                <div
                  class="progress-bar-fill blue-bg"
                  :style="{ width: calculateProgress(resourceOverview.storageUsed, resourceOverview.storageLimit) + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- AI管家 -->
      <div class="section">
        <h2 class="section-title">AI管家</h2>
        <ai-assistant-card
          storage-key="llmops_dashboard_chat_v1"
          assistant-name="智能助手"
          welcome-message="你好！我是AI管家，可以帮你完成各种平台操作。请问有什么可以帮助你的？"
          :quick-suggestions="['平台功能介绍','智能助手使用手册', '平台整体项目及资源使用信息', '平台k8s集群整体状态']"
        />
      </div>

      <!-- 平台指引 -->
      <div class="section guide-video-section">
        <h2 class="section-title">新手指引视频</h2>
        <div class="guide-card card-hover">
          <!-- 主视频预览区 -->
          <div class="main-video-preview" @click="playVideo(tutorialVideos[0])">
            <div class="play-button-large">
              <i class="el-icon-caret-right"></i>
            </div>
            <div class="main-video-info">
              <h3 class="main-video-title">平台快速入门</h3>
              <p class="main-video-subtitle">点击播放按钮开始观看</p>
            </div>
          </div>

          <!-- 视频列表标题 -->
          <div class="video-list-title">视频教程列表</div>

          <!-- 视频教程网格 -->
          <div class="video-tutorial-grid">
            <div class="tutorial-card" v-for="(item, index) in tutorialVideos.slice(1)" :key="index" @click="playVideo(item)">
              <div class="tutorial-play-btn">
                <i class="el-icon-caret-right"></i>
              </div>
              <div class="tutorial-info">
                <div class="tutorial-title">{{ item.title }}</div>
                <div class="tutorial-duration">{{ item.duration }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="section">
        <div class="quick-entries">
          <div class="quick-entry card-hover" @click="navigateTo('/llmops/datasets')">
            <div class="quick-icon-wrapper">
              <div class="quick-icon purple-quick">
                <i class="el-icon-s-data"></i>
              </div>
            </div>
            <div class="quick-title">数据管理</div>
            <div class="quick-desc">{{ dataInfo.datasetCount || 0 }}个数据集</div>
          </div>
          <div class="quick-entry card-hover" @click="navigateTo('/llmops/training/instance')">
            <div class="quick-icon-wrapper">
              <div class="quick-icon green-quick">
                <i class="el-icon-s-claim"></i>
              </div>
            </div>
            <div class="quick-title">实验管理</div>
            <div class="quick-desc">{{ dataInfo.aiExperimentCount || 0 }}个实验</div>
          </div>
          <div class="quick-entry card-hover" @click="navigateTo('/llmops/serving/model')">
            <div class="quick-icon-wrapper">
              <div class="quick-icon orange-quick">
                <i class="el-icon-s-promotion"></i>
              </div>
            </div>
            <div class="quick-title">模型服务</div>
            <div class="quick-desc">{{ dataInfo.inferenceServiceCount || 0 }}个在线服务</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建项目弹窗 -->
    <el-dialog
      title="创建项目"
      :visible.sync="createDialogVisible"
      width="600px"
      :close-on-click-modal="false"
      class="create-project-dialog"
    >
      <el-form :model="projectForm" label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="projectForm.department" placeholder="请选择部门" style="width: 100%;">
            <el-option label="AI Lab" value="AI Lab"></el-option>
            <el-option label="数据科学部" value="数据科学部"></el-option>
            <el-option label="研发部" value="研发部"></el-option>
            <el-option label="产品部" value="产品部"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateProject">创建</el-button>
      </span>
    </el-dialog>

    <!-- 视频播放弹窗 -->
    <el-dialog
      :title="currentVideo.title"
      :visible.sync="videoDialogVisible"
      width="800px"
      @close="handleVideoClose"
      class="video-play-dialog"
      :close-on-click-modal="false"
    >
      <div class="video-container" v-if="videoDialogVisible">
        <video
          ref="videoPlayer"
          controls
          autoplay
          preload="metadata"
          playsinline
          webkit-playsinline
          style="width: 100%; border-radius: 8px;"
        >
          <source :src="currentVideo.url" type="video/mp4">
          您的浏览器不支持视频播放。
        </video>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProjectResourceOverview, getProjectDataInfo, getProjectIndex } from '@/api/llmops/project'
import AiAssistantCard from '@/components/llmops/AiAssistantCard.vue'

export default {
  name: 'Dashboard',
  components: {
    AiAssistantCard
  },
  data() {
    return {
      userName: localStorage.getItem('userName') || '用户',
      projectIndex: {
        task: 0,
        serviceUnreadyCount: 0,
        serviceReadyCount: 0,
        aiExperimentCount: 0
      },
      dataInfo: {
        aiExperimentCount: 0,
        datasetCount: 0,
        inferenceServiceCount: 0
      },
      createDialogVisible: false,
      videoDialogVisible: false,
      currentVideo: {},
      projectForm: {
        name: '',
        department: 'AI Lab',
        description: ''
      },
      tutorialVideos: [
        { title: '平台快速入门', duration: '8:43', url: '/video/decision_tree_train_register_infer.mp4' },
        { title: '通过AI智能IDE开发教程', duration: '6:20', url: '/video/ai_ide_tutorial.mp4' },
        { title: '多种分类模型对比训练', duration: '12:27', url: '/video/multiple_classification_models.mp4' },
        { title: 'yolov8模型训练、注册、推理部署', duration: '16.44', url: '/video/yolov8_train_register_infer.mp4' },
        { title: '查看当前的模型列表', duration: '10:24', url: '/video/view_current_models.mp4' },
        { title: '获取当前任务编排列表', duration: '7:23', url: '/video/get_current_task_list.mp4' },
        { title: '列出数据集列表', duration: '3:49', url: '/video/list_datasets.mp4' },
      ],
      resourceOverview: {
        cpuUsed: '0',
        cpuLimit: '0',
        memoryUsed: '0',
        memoryLimit: '0',
        gpuUsed: '0',
        gpuLimit: '0',
        storageUsed: '0',
        storageLimit: '0'
      }
    }
  },
  mounted() {
    this.fetchResourceOverview()
    this.fetchProjectDataInfo()
    this.fetchProjectIndex()
    // this.initVideoDurations()
  },
  methods: {
    async fetchResourceOverview() {
      try {
        const res = await getProjectResourceOverview()
        if (res) {
          this.resourceOverview = res
        }
      } catch (error) {
        console.error('获取资源概览失败:', error)
      }
    },
    async fetchProjectDataInfo() {
      try {
        const res = await getProjectDataInfo()
        if (res) {
          this.dataInfo = {
            aiExperimentCount: res.aiExperimentCount || 0,
            datasetCount: res.datasetCount || 0,
            inferenceServiceCount: res.inferenceServiceCount || 0
          }
        }
      } catch (error) {
        console.error('获取项目数据信息失败:', error)
      }
    },
    async fetchProjectIndex() {
      try {
        const res = await getProjectIndex()
        if (res) {
          this.projectIndex = res
        }
      } catch (error) {
        console.error('获取首页统计失败:', error)
      }
    },
    calculateProgress(used, total) {
      const u = parseFloat(used) || 0
      const t = parseFloat(total) || 1
      const progress = Math.min(Math.round((u / t) * 100), 100)
      return progress
    },
    getDashOffset(used, total) {
      const progress = this.calculateProgress(used, total)
      // 圆周长 = 2 * π * r = 2 * 3.14159 * 28 ≈ 175.9
      const circumference = 175.9
      return circumference * (1 - progress / 100)
    },
    showCreateDialog() {
      this.createDialogVisible = true
      this.projectForm = {
        name: '',
        department: 'AI Lab',
        description: ''
      }
    },
    handleCreateProject() {
      if (!this.projectForm.name.trim()) {
        this.$message.warning('请输入项目名称')
        return
      }
      this.$message.success('项目创建成功')
      this.createDialogVisible = false
    },
    handleQuickQuestion(question) {
      // 通过 ref 调用子组件方法
    },
    initVideoDurations() {
      this.tutorialVideos.forEach(async (video, index) => {
        if (video.url) {
          try {
            const seconds = await this.getVideoDuration(video.url)
            this.$set(this.tutorialVideos[index], 'duration', this.formatSeconds(seconds))
          } catch (error) {
            console.error(`无法获取视频时长: ${video.url}`, error)
          }
        }
      })
    },
    getVideoDuration(url) {
      return new Promise((resolve, reject) => {
        const video = document.createElement('video')
        video.preload = 'metadata'
        video.src = url
        video.onloadedmetadata = () => {
          resolve(video.duration)
        }
        video.onerror = () => {
          reject(new Error('视频加载失败'))
        }
      })
    },
    formatSeconds(seconds) {
      if (!seconds) return '0:00'
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    quickSuggestion(text) {
      this.chatInput = text
    },
    playVideo(item) {
      if (!item.url) {
        this.$message.info('该视频正在录制中，敬请期待...')
        return
      }
      this.currentVideo = item
      this.videoDialogVisible = true
    },
    handleVideoClose() {
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.pause()
      }
    },
    navigateTo(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  min-height: calc(100vh - 60px);
}

.dashboard-content {
  // max-width: 1400px;
  margin: 0 auto;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  border-radius: 12px;
  padding: 24px;
  color: white;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.welcome-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 16px 0;
}

.banner-buttons {
  display: flex;
  gap: 12px;
}

.btn-white {
  background-color: white !important;
  color: #2563eb !important;
  border: none !important;
}

.btn-white:hover {
  background-color: #eff6ff !important;
}

.btn-outline {
  background-color: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  color: white !important;
}

.btn-outline:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.6) !important;
}

.banner-right {
  text-align: right;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

/* 资源卡片网格 */
.resource-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.resource-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-icon-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.purple-icon {
  background-color: #f3e8ff;
  color: #9333ea;
}

.orange-icon {
  background-color: #ffedd5;
  color: #ea580c;
}

.blue-icon {
  background-color: #dbeafe;
  color: #2563eb;
}

.green-icon {
  background-color: #dcfce7;
  color: #16a34a;
}

.card-info {
  flex: 1;
}

.card-label {
  font-size: 14px;
  color: #6b7280;
}

.card-value {
  font-size: 18px;
  font-weight: bold;
  color: #1f2937;
}

/* 进度环 */
.progress-ring-container {
  position: relative;
  width: 64px;
  height: 64px;
}

.progress-ring {
  width: 64px;
  height: 64px;
  transform: rotate(-90deg);
}

.progress-ring-bg {
  stroke: #e5e7eb;
}

.progress-ring-fill {
  transition: stroke-dashoffset 0.5s ease;
}

.purple-fill {
  stroke: #9333ea;
}

.orange-fill {
  stroke: #ea580c;
}

.blue-fill {
  stroke: #2563eb;
}

.green-fill {
  stroke: #16a34a;
}

.progress-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

/* 状态标签 */
.status-tags {
  display: flex;
  gap: 8px;
}

.status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.tag-green {
  background-color: #dcfce7;
  color: #16a34a;
}

.tag-blue {
  background-color: #dbeafe;
  color: #2563eb;
}

.tag-red {
  background-color: #fee2e2;
  color: #dc2626;
}

/* 进度条 */
.progress-bar-container {
  width: 100%;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background-color: #2563eb;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.blue-bg {
  background-color: #2563eb;
}

.orange-bg {
  background-color: #ea580c;
}

.green-bg {
  background-color: #16a34a;
}

/* 任务列表 (已移除) */

/* 平台指引 */
.guide-video-section {
  .guide-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid #e5e7eb;
  }

  .main-video-preview {
    background-color: #f9fafb;
    border-radius: 12px;
    height: 180px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f3f4f6;
    }

    .play-button-large {
      width: 48px;
      height: 48px;
      background-color: #2563eb;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 24px;
      margin-bottom: 12px;
      box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.2);
    }

    .main-video-title {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 4px 0;
      text-align: center;
    }

    .main-video-subtitle {
      font-size: 12px;
      color: #6b7280;
      margin: 0;
    }
  }

  .video-list-title {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 16px;
  }

  .video-tutorial-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }

  .tutorial-card {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background-color: white;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #2563eb;
      background-color: #f9fafb;
      transform: translateY(-2px);
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    }

    .tutorial-play-btn {
      width: 32px;
      height: 32px;
      background-color: #f3f4f6;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #2563eb;
      font-size: 16px;
      margin-right: 12px;
    }

    .tutorial-info {
      text-align: left;

      .tutorial-title {
        font-size: 14px;
        font-weight: 500;
        color: #1f2937;
        margin-bottom: 2px;
      }

      .tutorial-duration {
        font-size: 12px;
        color: #9ca3af;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 快捷入口 */
.quick-entries {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-entry {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  text-align: left;
  cursor: pointer;
}

.quick-entry:hover {
  border-color: #3b82f6;
}

.quick-icon-wrapper {
  margin-bottom: 12px;
}

.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: transform 0.3s ease;
}

.quick-entry:hover .quick-icon {
  transform: scale(1.1);
}

.blue-quick {
  background-color: #dbeafe;
  color: #2563eb;
}

.purple-quick {
  background-color: #f3e8ff;
  color: #9333ea;
}

.green-quick {
  background-color: #dcfce7;
  color: #16a34a;
}

.orange-quick {
  background-color: #ffedd5;
  color: #ea580c;
}

.quick-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.quick-desc {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

@media (max-width: 1024px) {
  .resource-grid {
    grid-template-columns: 1fr;
  }

  .tasks-grid {
    grid-template-columns: 1fr;
  }

  .quick-entries {
    grid-template-columns: repeat(2, 1fr);
  }

  .welcome-banner {
    flex-direction: column;
  }

  .banner-right {
    display: none;
  }
}

@media (max-width: 768px) {
  .quick-entries {
    grid-template-columns: 1fr;
  }

  .suggestion-buttons {
    flex-direction: column;
    align-items: flex-start;
  }

  .suggestion-btn {
    width: 100%;
    justify-content: flex-start;
  }
}

/* 全屏模式样式 */
.ai-assistant-card.is-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 2000;
  margin: 0;
  border-radius: 0;
  display: flex;
  flex-direction: column;
  padding: 32px;
  background-color: white;

  .assistant-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f3f4f6;
  }

  .chat-area {
    flex: 1;
    max-height: none;
    margin-bottom: 24px;
  }

  .chat-input-area {
    margin-bottom: 24px;
  }

  .quick-suggestions {
    margin-top: auto;
    padding-top: 24px;
  }
}

.assistant-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fullscreen-btn {
  font-size: 18px;
  color: #6b7280;
  padding: 4px;
}

.fullscreen-btn:hover {
  color: #2563eb;
}

.stop-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  border: none !important;
  color: white !important;
}

.stop-btn:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c) !important;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3) !important;
}
</style>

<!-- 非 scoped 样式，用于 Markdown 渲染 -->
<style>
/* Markdown 全局样式 */
.markdown-body {
  word-wrap: break-word !important;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  margin-top: 16px !important;
  margin-bottom: 8px !important;
  font-weight: 600 !important;
  line-height: 1.25 !important;
  color: #1f2937 !important;
  display: block !important;
}

.markdown-body h1 {
  font-size: 20px !important;
  border-bottom: 1px solid #e5e7eb !important;
  padding-bottom: 8px !important;
}
.markdown-body h2 { font-size: 18px !important; }
.markdown-body h3 {
  font-size: 16px !important;
  color: #374151 !important;
}
.markdown-body h4 { font-size: 15px !important; }

.markdown-body p { margin: 8px 0; }

.markdown-body ul,
.markdown-body ol {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-body li { margin: 4px 0; }

.markdown-body code {
  background-color: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  color: #e11d48;
}

.markdown-body pre {
  background-color: #1f2937;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-body pre code {
  background-color: transparent;
  padding: 0;
  color: #e5e7eb;
}

.markdown-body a {
  color: #2563eb;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}
</style>
