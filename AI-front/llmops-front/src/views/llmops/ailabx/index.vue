<template>
  <div class="ailabx-container">
    <div class="ailabx-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">AILabX</h1>
          <p class="page-subtitle">探索各类实验智能体验和良好应用</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="add-btn" size="small">
            <i class="el-icon-plus"></i> 发布应用
          </el-button>
        </div>
      </div>

      <!-- AI 闭环架构 Pipeline -->
      <div class="section pipeline-section">
        <pipeline-flow :embedded="true" :progress-data="pipelineProgress" />
      </div>

      <!-- 分类导航 -->
      <div class="section">
        <div class="section-title">应用分类</div>
        <div class="category-nav">
          <el-button v-for="(category, index) in categories" :key="index"
            :type="activeCategory === index ? 'primary' : ''" class="category-btn" size="small"
            @click="activeCategory = index">
            {{ category }}
          </el-button>
        </div>
      </div>

      <!-- 模型应用 -->
      <div class="section">
        <div class="section-title">模型应用</div>
        <div class="app-cards">
          <div class="app-card card-hover" v-for="(app, index) in modelApps" :key="index">
            <div class="app-card-header">
              <div class="app-icon" :style="{ backgroundColor: app.iconBg }">
                <i :class="app.icon"></i>
              </div>
              <div class="app-info">
                <div class="app-name">{{ app.name }}</div>
                <div class="app-desc">{{ app.desc }}</div>
              </div>
            </div>
            <div class="app-details">
              <div class="app-detail-item">
                <span class="detail-label">模型</span>
                <span class="detail-value">{{ app.model }}</span>
              </div>
              <div class="app-detail-item">
                <span class="detail-label">版本</span>
                <span class="detail-value">{{ app.version }}</span>
              </div>
              <div class="app-detail-item">
                <span class="detail-label">评分</span>
                <span class="detail-value">{{ app.rating }}</span>
              </div>
            </div>
            <div class="app-actions">
              <el-button type="primary" size="small" class="action-btn" @click="handleOpenChat(app)">{{ app.primaryBtn
              }}</el-button>
              <el-button size="small" class="action-btn-outline">详情</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 龙虾应用 先隐藏 -->
      <div class="section">
        <div class="section-title">技能应用</div>
        <div class="app-cards">
          <div class="app-card card-hover" v-for="(app, index) in longxdApps" :key="index">
            <div class="app-card-header">
              <div class="app-icon" :style="{ backgroundColor: app.iconBg }">
                <i :class="app.icon"></i>
              </div>
              <div class="app-info">
                <div class="app-name">{{ app.name }}</div>
                <div class="app-desc">{{ app.desc }}</div>
              </div>
            </div>
            <div class="app-details">
              <div class="app-detail-item">
                <span class="detail-label">类型</span>
                <span class="detail-value">{{ app.type }}</span>
              </div>
              <div class="app-detail-item">
                <span class="detail-label">版本</span>
                <span class="detail-value">{{ app.version }}</span>
              </div>
              <div class="app-detail-item">
                <span class="detail-label">评分</span>
                <span class="detail-value">{{ app.rating }}</span>
              </div>
            </div>
            <div class="app-actions">
              <el-button type="primary" size="small" class="action-btn" @click="handleOpenChat(app)">{{ app.primaryBtn
              }}</el-button>
              <el-button size="small" class="action-btn-outline">详情</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 大模型应用 -->
      <div class="section">
        <div class="section-title">大模型应用</div>
        <div class="app-cards">
          <div class="app-card card-hover" v-for="(app, index) in llmApps" :key="'llm-' + index">
            <div class="app-card-header">
              <div class="app-icon" :style="{ backgroundColor: app.iconBg }">
                <i :class="app.icon"></i>
              </div>
              <div class="app-info">
                <div class="app-name">{{ app.name }}</div>
                <div class="app-desc">{{ app.desc }}</div>
              </div>
            </div>
            <div class="app-details">
              <div class="app-detail-item">
                <span class="detail-label">类型</span>
                <span class="detail-value">{{ app.type }}</span>
              </div>
              <div class="app-detail-item">
                <span class="detail-label">版本</span>
                <span class="detail-value">{{ app.version }}</span>
              </div>
              <div class="app-detail-item">
                <span class="detail-label">评分</span>
                <span class="detail-value">{{ app.rating }}</span>
              </div>
            </div>
            <div class="app-actions">
              <el-button type="primary" size="small" class="action-btn" @click="handleOpenChat(app)">{{ app.primaryBtn
              }}</el-button>
              <el-button size="small" class="action-btn-outline">详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 对话弹框 -->
    <el-dialog :visible.sync="chatDialogVisible" width="700px" :close-on-click-modal="false" class="chat-dialog"
      custom-class="chat-dialog">
      <div slot="title" class="chat-dialog-title">
        <div class="chat-header">
          <div class="chat-icon" :style="{ backgroundColor: currentChatApp.iconBg }">
            <i :class="currentChatApp.icon"></i>
          </div>
          <div class="chat-info">
            <div class="chat-name">{{ currentChatApp.name }} 对话</div>
            <div class="chat-desc">与{{ currentChatApp.name }}大模型进行实时对话</div>
          </div>
          <!-- <div class="dialog-header-actions">  
            <div class="temp-control">
              <span class="temp-label">温度</span>
              <input type="range" min="0" max="2" step="0.1" v-model.number="temperature" class="temp-range" />
              <span class="temp-value">{{ Number(temperature).toFixed(1) }}</span>
              <div class="temp-presets">
                <button v-for="p in tempPresets" :key="'d-' + p.label" type="button" class="temp-preset-btn"
                  :class="{ active: isTempPresetActive(p.value) }" @click="setTempPreset(p.value)">{{ p.label
                  }}</button>
              </div>
            </div>
            <el-tooltip content="清空会话" placement="top">
              <el-button type="text" class="clear-btn" :disabled="isDialogSending" @click="clearDialogChat">
                <i class="el-icon-delete"></i>
              </el-button>
            </el-tooltip>
          </div> -->
        </div>
      </div>

      <div class="chat-dialog-content">
        <div class="chat-messages" ref="chatMessages">
          <div class="chat-message" :class="m.role === 'assistant' ? 'ai-message' : 'user-message'"
            v-for="(m, index) in dialogMessages" :key="'dialog-' + index">
            <template v-if="m.role === 'assistant'">
              <div class="message-avatar">AI</div>
              <div class="message-content-wrap">
                <div class="message-content" v-if="m.loading && !m.content && !m.error">
                  <span class="message-loading">
                    <span class="status-dot" :class="{ generating: dialogStreamStage === 'generating' }"></span>
                    <span class="message-loading-text">{{ dialogStreamStage === 'generating' ?
                      'AI 正在生成中...' : 'AI正在思考中...' }}</span>
                  </span>
                </div>
                <div class="message-content markdown-body" v-else-if="m.content" v-html="renderMarkdown(m.content)">
                </div>
                <div class="message-status-pill" v-if="m.loading && m.content">
                  <span class="status-dot generating"></span>
                  <span>生成中...</span>
                </div>
                <div v-if="m.error" class="message-error-card">
                  <span class="message-error-text">[错误] {{ m.error }}</span>
                  <el-button v-if="canRetryDialog(index)" size="mini" type="danger" plain class="message-retry-btn"
                    :disabled="isDialogSending" @click="retryDialog(index)">重试</el-button>
                </div>
                <div v-if="m.ts && shouldShowDialogTime(index)" class="message-time">{{ formatTime(m.ts) }}</div>
              </div>
            </template>
            <template v-else>
              <div class="message-content-wrap user-wrap">
                <div class="message-content">{{ m.content }}</div>
                <div v-if="m.ts && shouldShowDialogTime(index)" class="message-time user-time">{{ formatTime(m.ts) }}
                </div>
              </div>
              <div class="message-avatar">我</div>
            </template>
          </div>
        </div>

        <div class="chat-input-section">
          <el-input v-model="chatDialogInput" placeholder="输入你的问题..." class="chat-dialog-input"
            :disabled="isDialogSending" @keyup.enter.native="sendChatMessage"></el-input>
          <el-button v-if="!isDialogSending" type="primary" class="send-chat-btn" @click="sendChatMessage">
            <i class="el-icon-s-promotion"></i> 发送
          </el-button>
          <el-button v-else type="danger" class="send-chat-btn stop-chat-btn" @click="stopChatMessage">
            <i class="el-icon-video-pause"></i> 停止
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- AI 助手悬浮按钮 -->
    <button class="ai-fab" :class="{ active: aiDrawerVisible }" @click="aiDrawerVisible = true" title="AI 助手">
      <img :src="aiRobot" alt="AI 助手" class="ai-fab-image" />
      <span class="ai-fab-label">AI助手</span>
    </button>

    <!-- AI 助手抽屉 -->
    <el-drawer :visible.sync="aiDrawerVisible" direction="rtl" size="700px" :with-header="false" append-to-body
      custom-class="ai-assistant-drawer">
      <div class="ai-drawer-wrap">
        <div class="ai-drawer-header">
          <span class="ai-drawer-title">
            <i class="el-icon-chat-dot-round"></i> AILabX 助手
          </span>
          <i class="el-icon-close ai-drawer-close" @click="aiDrawerVisible = false"></i>
        </div>
        <div class="ai-drawer-body">
          <ai-assistant-card v-if="aiDrawerVisible" storageKey="ailabx_preview_messages_v1" assistantName="AILabX助手"
            apiBase="/api"
            welcomeMessage="你好！我是AILabX助手，可以帮你探索和部署AI应用，请问有什么可以帮助你的？例如：&quot;推荐一个图像识别模型&quot;、&quot;部署一个对话机器人&quot;、&quot;查找我相关应用&quot;"
            :quickSuggestions="['训练新版本yolov8模型，并完成模型注册和模型推理部署', '训练新版本决策树模型，并完成模型注册和模型推理部署', '列出平台当前流水线列表', '规划一个多种分类模型的对比训练任务']" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import AiAssistantCard from '@/components/llmops/AiAssistantCard.vue'
import PipelineFlow from '@/views/llmops/pipeline/index.vue'
import aiRobot from './ai-robot.jpg'
import { getPipelineProgress } from '@/api/llmops/ailabx'

const renderer = new marked.Renderer()

// 自定义链接渲染，让所有链接在新窗口打开
renderer.link = function (href, title, text) {
  const titleAttr = title ? ` title="${title}"` : ''
  return `<a href="${href}"${titleAttr} target="_blank" rel="noopener noreferrer">${text}</a>`
}

marked.setOptions({
  renderer: renderer,
  breaks: true,
  gfm: true,
  highlight: function (code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    try {
      return hljs.highlight(code, { language }).value
    } catch (e) {
      return hljs.highlightAuto(code).value
    }
  }
})

export default {
  name: "AILabX",
  components: { AiAssistantCard, PipelineFlow },
  data() {
    return {
      apiBase: "/api",
      aiDrawerVisible: false,
      aiRobot,

      currentChatApp: {
        name: "",
        desc: "",
        icon: "",
        iconBg: "",
        isOllama: false,
        ollamaModel: ""
      },
      activeCategory: 0,
      categories: ["全部", "模型智能体", "技能应用", "图像处理", "自然语言处理", "数据分析", "推荐系统"],
      // 大屏进度 mock 数据，后续替换为后端接口返回
      pipelineProgress: {
        overall: 68,
        statusText: "训练工厂任务执行中",
        stages: [
          { name: "客户", percent: 100, status: "done" },
          { name: "AI基地", percent: 100, status: "done" },
          { name: "数据采集", percent: 100, status: "done" },
          { name: "模型仓库", percent: 100, status: "done" },
          { name: "训练工厂", percent: 68, status: "running" },
          { name: "推理验证", percent: 0, status: "pending" },
          { name: "模型库", percent: 0, status: "pending" }
        ]
      },
      ollamaBaseUrl: '/ollama',
      modelApps: [
        {
          name: "对话应用",
          desc: "基于GPT-4的智能对话助手",
          icon: "el-icon-chat-dot-round",
          iconBg: "#dbeafe",
          model: "GPT-4",
          version: "v1.0",
          rating: "4.8/5.0",
          primaryBtn: "部署"
        },
        {
          name: "图像识别应用",
          desc: "基于ResNet的图像分类系统",
          icon: "el-icon-picture",
          iconBg: "#d1fae5",
          model: "ResNet-50",
          version: "v2.1",
          rating: "4.6/5.0",
          primaryBtn: "部署"
        },
        {
          name: "文本分析应用",
          desc: "基于BERT的文本分析系统",
          icon: "el-icon-document",
          iconBg: "#fef3c7",
          model: "BERT-base",
          version: "v1.5",
          rating: "4.7/5.0",
          primaryBtn: "部署"
        }
      ],
      longxdApps: [
        {
          name: "minimax-docx",
          desc: "智能解析编辑DOCX文档，AI辅助文档处理",
          icon: "el-icon-s-tools",
          iconBg: "#fce7f3",
          type: "文档编辑",
          version: "v1.2",
          rating: "4.9/5.0",
          primaryBtn: "部署"
        },
        // {
        //   name: "龙虾市场分析",
        //   desc: "分析龙虾市场价格趋势",
        //   icon: "el-icon-s-marketing",
        //   iconBg: "#fef3c7",
        //   type: "数据分析",
        //   version: "v2.0",
        //   rating: "4.7/5.0",
        //   primaryBtn: "部署"
        // },
        // {
        //   name: "龙虾食谱推荐",
        //   desc: "基于龙虾种类推荐食谱",
        //   icon: "el-icon-s-goods",
        //   iconBg: "#d1fae5",
        //   type: "推荐系统",
        //   version: "v1.1",
        //   rating: "4.8/5.0",
        //   primaryBtn: "部署"
        // }
      ],
      llmApps: [
        {
          name: "Qwen",
          desc: "阿里云千问大模型",
          icon: "el-icon-s-opportunity",
          iconBg: "#dbeafe",
          type: "通用大模型",
          version: "2.5",
          rating: "4.8/5.0",
          primaryBtn: "对话",
          isOllama: true,
          ollamaModel: "qwen2.5:7b"
        },
        {
          name: "DeepSeek",
          desc: "深度求索大模型",
          icon: "el-icon-s-data",
          iconBg: "#ede9fe",
          type: "通用大模型",
          version: "R1",
          rating: "4.7/5.0",
          primaryBtn: "对话",
          isOllama: true,
          ollamaModel: "deepseek-r1"
        },
        {
          name: "GPT",
          desc: "OpenAI GPT大模型",
          icon: "el-icon-s-platform",
          iconBg: "#ecfdf5",
          type: "通用大模型",
          version: "OSS",
          rating: "4.9/5.0",
          primaryBtn: "对话",
          isOllama: true,
          ollamaModel: "gpt-oss"
        }
      ],
      temperature: 0.7,
      dialogMessages: [],
      chatDialogVisible: false,
      chatDialogInput: "",
      isDialogSending: false,
      dialogStreamStage: null,
      dialogAbortCtrl: null
    };
  },
  computed: {
    renderMarkdown() {
      return (content) => {
        if (!content) return ''
        return marked(content)
      }
    }
  },
  watch: {
    $route: {
      immediate: true,
      handler() {
        this.loadPipelineProgress()
      }
    }
  },
  methods: {
    async loadPipelineProgress() {
      console.log('[AILabX] request pipeline progress')
      try {
        const progress = await getPipelineProgress()
        const payload = progress && progress.body
          ? progress.body
          : progress && progress.data
            ? progress.data
            : progress
        console.log('[AILabX] pipeline progress loaded', payload)
        if (payload && Array.isArray(payload.stages) && payload.stages.length) {
          this.pipelineProgress = payload
        }
      } catch (e) {
        // 接口未启动时继续使用本地 mock 数据，方便本地调试大屏效果
        console.warn('[AILabX] pipeline progress request failed', e)
      }
    },
    handleOpenChat(app) {
      if (app.primaryBtn === "对话") {
        this.currentChatApp = {
          name: app.name,
          desc: app.desc,
          icon: app.icon,
          iconBg: app.iconBg,
          isOllama: app.isOllama || false,
          ollamaModel: app.ollamaModel || ''
        };

        this.dialogMessages = [
          {
            role: "assistant",
            content: `你好！我是${app.name}，有什么可以帮助你的？`,
            ts: Date.now()
          }
        ];
        this.chatDialogInput = "";
        this.chatDialogVisible = true;

        this.$nextTick(() => {
          this.scrollDialogBottom();
          this.highlightCode();
        });
      } else if (app.primaryBtn === "部署") {
        this.$message.info(`正在部署: ${app.name}`);
      }
    },

    async sendChatMessage() {
      const text = (this.chatDialogInput || "").trim();
      if (!text || this.isDialogSending) return;

      this.dialogMessages.push({ role: "user", content: text, ts: Date.now() });
      this.chatDialogInput = "";

      const history = this.dialogMessages
        .slice(0, -1)
        .filter(m => !m.error && (m.content || '').trim())
        .map(m => ({ role: m.role, content: m.content }));

      const aiMsg = { role: "assistant", content: "", loading: true, ts: Date.now(), error: "" };
      this.dialogMessages.push(aiMsg);
      this.isDialogSending = true;
      this.dialogStreamStage = 'thinking';
      this.$nextTick(() => this.scrollDialogBottom());

      const ctrl = new AbortController();
      this.dialogAbortCtrl = ctrl;

      try {
        const onToken = token => {
          if (this.dialogStreamStage !== 'generating') this.dialogStreamStage = 'generating';
          aiMsg.content += token;
          this.$nextTick(() => this.scrollDialogBottom());
        };
        if (this.currentChatApp.isOllama) {
          await this.streamOllamaChat(this.currentChatApp.ollamaModel, text, history, onToken, ctrl.signal);
        } else {
          await this.streamChat(text, history, onToken, ctrl.signal);
        }
      } catch (e) {
        if (this.isAbortError(e)) {
          if (aiMsg.content.trim() && !aiMsg.content.endsWith('（已停止）')) {
            aiMsg.content += '（已停止）';
          } else if (!aiMsg.content.trim()) {
            aiMsg.content = '（已停止）';
          }
        } else {
          aiMsg.error = e && e.message ? e.message : String(e);
        }
      } finally {
        aiMsg.loading = false;
        this.isDialogSending = false;
        this.dialogStreamStage = null;
        this.dialogAbortCtrl = null;
        this.$nextTick(() => {
          this.scrollDialogBottom();
          this.highlightCode();
        });
      }
    },
    stopChatMessage() {
      if (this.dialogAbortCtrl) {
        this.dialogAbortCtrl.abort();
      }
    },
    isAbortError(e) {
      if (!e) return false;
      return e.name === 'AbortError' || /aborted|abort/i.test(e.message || '');
    },

    async streamOllamaChat(model, message, history, onToken, signal) {
      const resp = await fetch(`${this.ollamaBaseUrl}/api/chat`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: model,
          messages: [
            ...history.map(m => ({ role: m.role === 'assistant' ? 'assistant' : 'user', content: m.content })),
            { role: "user", content: message }
          ],
          stream: true,
          options: { temperature: this.clampTemperature(this.temperature) }
        }),
        signal
      });

      if (!resp.ok || !resp.body) {
        const t = await resp.text().catch(() => "");
        throw new Error(`HTTP ${resp.status} ${t}`);
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder("utf-8");
      let buffer = "";
      let done = false;

      const onAbort = () => {
        try { reader.cancel(); } catch (e) { }
      };
      if (signal) {
        if (signal.aborted) onAbort();
        signal.addEventListener('abort', onAbort, { once: true });
      }

      try {
        while (!done) {
          if (signal && signal.aborted) {
            const err = new Error('aborted');
            err.name = 'AbortError';
            throw err;
          }
          const { value, done: readDone } = await reader.read();
          done = readDone;
          buffer += decoder.decode(value || new Uint8Array(), { stream: !done });

          const lines = buffer.split("\n");
          buffer = lines.pop() || "";

          for (const line of lines) {
            if (!line.trim()) continue;
            try {
              const payload = JSON.parse(line);
              if (payload.message && payload.message.content) {
                onToken(payload.message.content);
              }
              if (payload.done) {
                done = true;
                break;
              }
            } catch (e) {
              continue;
            }
          }
        }
      } finally {
        if (signal) signal.removeEventListener('abort', onAbort);
      }
    },

    async streamChat(message, history, onToken, signal) {
      const resp = await fetch(`${this.apiBase}/chat/stream`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          message,
          history,
          temperature: this.clampTemperature(this.temperature)
        }),
        signal
      });

      if (!resp.ok || !resp.body) {
        const t = await resp.text().catch(() => "");
        throw new Error(`HTTP ${resp.status} ${t}`);
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder("utf-8");
      let buffer = "";
      let done = false;

      const onAbort = () => {
        try { reader.cancel(); } catch (e) { }
      };
      if (signal) {
        if (signal.aborted) onAbort();
        signal.addEventListener('abort', onAbort, { once: true });
      }

      try {
        while (!done) {
          if (signal && signal.aborted) {
            const err = new Error('aborted');
            err.name = 'AbortError';
            throw err;
          }
          const { value, done: readDone } = await reader.read();
          done = readDone;
          buffer += decoder.decode(value || new Uint8Array(), { stream: !done });

          const blocks = buffer.split("\n\n");
          buffer = blocks.pop() || "";

          for (const block of blocks) {
            const lines = block.split("\n");
            let event = "message";
            let data = "";

            for (const line of lines) {
              if (line.startsWith("event:")) event = line.slice(6).trim();
              if (line.startsWith("data:")) data += line.slice(5).trim();
            }

            if (!data) continue;

            let payload = {};
            try {
              payload = JSON.parse(data);
            } catch (e) {
              continue;
            }

            if (event === "token") {
              onToken(payload.token || "");
            } else if (event === "error") {
              throw new Error(payload.message || "流式请求异常");
            } else if (event === "done") {
              done = true;
              break;
            }
          }
        }
      } finally {
        if (signal) signal.removeEventListener('abort', onAbort);
      }
    },

    scrollDialogBottom() {
      const el = this.$refs.chatMessages;
      if (el && el.scrollHeight !== undefined) {
        el.scrollTop = el.scrollHeight;
      }
    },
    highlightCode() {
      this.$nextTick(() => {
        const blocks = document.querySelectorAll('.markdown-body pre code');
        blocks.forEach((block) => {
          try {
            hljs.highlightElement(block);
          } catch (e) {
            console.warn('Code highlight failed:', e);
          }
        });
      });
    },

    // ===== 温度控制 =====
    clampTemperature(v) {
      const n = Number(v);
      if (Number.isNaN(n)) return 0.7;
      return Math.min(2, Math.max(0, n));
    },
    isTempPresetActive(v) {
      return Math.abs(Number(this.temperature) - v) < 0.05;
    },
    setTempPreset(v) {
      this.temperature = v;
    },

    // ===== 时间戳 =====
    formatTime(ts) {
      if (!ts) return '';
      const d = new Date(ts);
      const now = new Date();
      const hh = String(d.getHours()).padStart(2, '0');
      const mm = String(d.getMinutes()).padStart(2, '0');
      const hm = `${hh}:${mm}`;
      const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
      const startOfTarget = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
      const dayDiff = Math.floor((startOfToday - startOfTarget) / 86400000);
      if (dayDiff === 0) return hm;
      if (dayDiff === 1) return `昨天 ${hm}`;
      const y = d.getFullYear();
      const mon = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${y}-${mon}-${day} ${hm}`;
    },
    minuteKey(ts) {
      if (!ts) return '';
      const d = new Date(ts);
      return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}-${d.getHours()}-${d.getMinutes()}`;
    },
    shouldShowDialogTime(idx) {
      if (idx <= 0) return true;
      const cur = this.dialogMessages[idx];
      const prev = this.dialogMessages[idx - 1];
      if (!cur || !prev) return true;
      return this.minuteKey(cur.ts) !== this.minuteKey(prev.ts);
    },

    // ===== 错误重试 =====
    findRetryQuestion(list, assistantIndex) {
      for (let i = assistantIndex - 1; i >= 0; i -= 1) {
        const m = list[i];
        if (m && m.role === 'user' && (m.content || '').trim()) return m.content.trim();
      }
      return '';
    },
    canRetryDialog(idx) {
      if (this.isDialogSending) return false;
      return !!this.findRetryQuestion(this.dialogMessages, idx);
    },
    retryDialog(idx) {
      if (this.isDialogSending) return;
      const q = this.findRetryQuestion(this.dialogMessages, idx);
      if (!q) return;
      this.chatDialogInput = q;
      this.sendChatMessage();
    },

    // ===== 清空会话 =====
    clearDialogChat() {
      if (this.isDialogSending) return;
      const name = this.currentChatApp && this.currentChatApp.name ? this.currentChatApp.name : 'AI';
      this.dialogMessages = [
        { role: 'assistant', content: `你好！我是${name}，有什么可以帮助你的？`, ts: Date.now() }
      ];
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.highlightCode();
    });
  }
};
</script>

<style lang="scss" scoped>
.ailabx-container {
  min-height: calc(100vh - 60px);
}

/* Pipeline 嵌入容器 */
.pipeline-section {
  margin-bottom: 24px;
}

.pipeline-section ::v-deep .pipeline-page {
  height: 540px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow:
    0 6px 28px rgba(0, 212, 200, 0.18),
    0 2px 12px rgba(0, 0, 0, 0.35),
    inset 0 0 0 1px rgba(0, 212, 200, 0.25);
}

/* AI 助手悬浮按钮 */
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 28px;
  z-index: 1000;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  gap: 3px;
  width: 78px;
  padding: 10px 8px 7px;
  overflow: hidden;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.22);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.ai-fab-image {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: 8px;
  object-fit: cover;
}

.ai-fab-label {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.ai-fab:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.30);
}

.ai-fab.active {
  transform: scale(0.95);
}

/* 抽屉内部 */
.ai-drawer-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.ai-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #eef0f4;
  flex-shrink: 0;
}

.ai-drawer-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.ai-drawer-title i {
  color: #2563eb;
  font-size: 18px;
}

.ai-drawer-close {
  font-size: 20px;
  color: #64748b;
  cursor: pointer;
  transition: color 0.15s;
}

.ai-drawer-close:hover {
  color: #1e293b;
}

.ai-drawer-body {
  flex: 1;
  overflow: hidden;
  padding: 12px;
}

.ailabx-content {
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
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
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
  border-radius: 8px;
}

.section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.category-nav {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  background: white;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.category-btn {
  border-radius: 20px;
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  background: white;
  color: #374151;
  transition: all 0.3s;

  &:hover {
    border-color: #3b82f6;
    color: #3b82f6;
  }
}

.category-btn.is-active {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  color: white;
}

.app-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.app-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.app-card-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.app-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #1f2937;
  flex-shrink: 0;
}

.app-info {
  flex: 1;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.app-desc {
  font-size: 13px;
  color: #6b7280;
}

.app-details {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.app-detail-item {
  flex: 1;
}

.detail-label {
  font-size: 12px;
  color: #9ca3af;
  display: block;
  margin-bottom: 2px;
}

.detail-value {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.app-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  height: 36px;
}

.action-btn-outline {
  flex: 1;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: white;
  height: 36px;
}

@media (max-width: 1200px) {
  .app-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .app-cards {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-right {
    width: 100%;
  }
}

/* 对话弹框样式 */
.chat-dialog {
  ::v-deep .el-dialog {
    border-radius: 16px;
  }

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
    padding: 0;
  }
}

.chat-dialog-title {
  margin: 0;
}

.chat-header {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #1f2937;
}

.chat-info {
  flex: 1;
}

.chat-name {
  font-size: 22px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 4px;
}

.chat-desc {
  font-size: 14px;
  color: #6b7280;
}

.chat-dialog-content {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #f9fafb;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-dialog-content .chat-message {
  display: flex;
  gap: 12px;
  width: 100%;
}

.chat-dialog-content .ai-message {
  justify-content: flex-start;
}

.chat-dialog-content .user-message {
  justify-content: flex-end;
}

.chat-dialog-content .message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.chat-dialog-content .user-message .message-avatar {
  background: linear-gradient(135deg, #10b981, #059669);
}

.chat-dialog-content .message-content {
  background-color: white;
  padding: 12px 16px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  font-size: 14px;
  color: #1f2937;
  line-height: 1.6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  word-break: break-word;
}

.chat-dialog-content .markdown-body {
  word-wrap: break-word;

  h1,
  h2,
  h3,
  h4,
  h5,
  h6 {
    margin-top: 16px;
    margin-bottom: 8px;
    font-weight: 600;
    line-height: 1.25;
  }

  h1 {
    font-size: 20px;
    border-bottom: 1px solid #e5e7eb;
    padding-bottom: 8px;
  }

  h2 {
    font-size: 18px;
  }

  h3 {
    font-size: 16px;
  }

  h4 {
    font-size: 15px;
  }

  p {
    margin: 8px 0;
  }

  ul,
  ol {
    padding-left: 20px;
    margin: 8px 0;
  }

  li {
    margin: 4px 0;
  }

  code {
    background-color: #f3f4f6;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 13px;
    color: #e11d48;
  }

  pre {
    background-color: #1f2937;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 8px 0;

    code {
      background-color: transparent;
      padding: 0;
      color: #f9fafb;
      font-size: 13px;
      line-height: 1.5;
    }
  }

  blockquote {
    border-left: 4px solid #3b82f6;
    padding-left: 12px;
    margin: 8px 0;
    color: #6b7280;
  }

  table {
    border-collapse: collapse;
    width: 100%;
    margin: 8px 0;
    font-size: 13px;
  }

  th,
  td {
    border: 1px solid #e5e7eb;
    padding: 8px 12px;
    text-align: left;
  }

  th {
    background-color: #f9fafb;
    font-weight: 600;
  }

  tr:nth-child(even) {
    background-color: #f9fafb;
  }

  a {
    color: #3b82f6;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  hr {
    border: none;
    border-top: 1px solid #e5e7eb;
    margin: 16px 0;
  }
}

.chat-dialog-content .user-message .message-content {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.chat-input-section {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background-color: white;
}

.chat-dialog-input {
  flex: 1;

  ::v-deep .el-input__inner {
    border-radius: 12px;
    border: 1px solid #d1d5db;
    padding: 12px 16px;
    height: 48px;
    font-size: 15px;
    transition: all 0.3s;
  }

  ::v-deep .el-input__inner:focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }
}

.send-chat-btn {
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  height: 48px;
  padding: 0 24px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;

  &:hover {
    background: linear-gradient(135deg, #2563eb, #1d4ed8);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  }
}

.send-chat-btn.stop-chat-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626);

  &:hover {
    background: linear-gradient(135deg, #dc2626, #b91c1c);
    box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
  }
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

.markdown-body h2 {
  font-size: 18px !important;
}

.markdown-body h3 {
  font-size: 16px !important;
  color: #374151 !important;
}

.markdown-body h4 {
  font-size: 15px !important;
}

.markdown-body p {
  margin: 8px 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-body li {
  margin: 4px 0;
}

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
  color: #f9fafb;
  font-size: 13px;
  line-height: 1.5;
}

.markdown-body blockquote {
  border-left: 4px solid #3b82f6;
  padding-left: 12px;
  margin: 8px 0;
  color: #6b7280;
}

.markdown-body table {
  border-collapse: collapse !important;
  width: 100% !important;
  margin: 8px 0 !important;
  font-size: 13px !important;
}

.markdown-body th,
.markdown-body td {
  border: 1px solid #e5e7eb !important;
  padding: 8px 12px !important;
  text-align: left !important;
}

.markdown-body th {
  background-color: #f9fafb !important;
  font-weight: 600 !important;
}

.markdown-body tr:nth-child(even) {
  background-color: #f9fafb !important;
}

.markdown-body a {
  color: #3b82f6;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body hr {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 16px 0;
}

/* ===== 温度控制 ===== */
.temp-control {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 12px;
  color: #374151;
}

.temp-control .temp-label {
  color: #6b7280;
}

.temp-control .temp-range {
  width: 90px;
  accent-color: #3b82f6;
}

.temp-control .temp-value {
  min-width: 22px;
  text-align: right;
  color: #2563eb;
  font-weight: 600;
}

.temp-presets {
  display: inline-flex;
  gap: 4px;
  margin-left: 2px;
}

.temp-preset-btn {
  border: 1px solid #d1d5db;
  background: #fff;
  color: #4b5563;
  border-radius: 10px;
  font-size: 11px;
  line-height: 1;
  padding: 3px 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.temp-preset-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.temp-preset-btn.active {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-color: #2563eb;
  color: #fff;
}

.clear-btn {
  color: #6b7280 !important;
  padding: 4px 6px !important;
  font-size: 18px !important;
  margin-left: 10px !important;
}

.clear-btn:hover {
  color: #ef4444 !important;
}

.dialog-header-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

/* ===== 消息内容包裹（让 时间戳/错误卡片 跟随气泡） ===== */
.message-content-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 100%;
  min-width: 0;
}

.message-content-wrap.user-wrap {
  align-items: flex-end;
}

/* ===== 思考中/生成中 状态点 ===== */
.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #3b82f6;
  margin-right: 4px;
  animation: ailabx-dot-pulse 1s ease-in-out infinite;
}

.status-dot.generating {
  background: #22c55e;
}

.message-loading {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.message-status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  padding: 2px 8px;
  font-size: 11px;
  color: #16a34a;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  line-height: 1.4;
}

/* ===== 错误卡片 + 重试 ===== */
.message-error-card {
  margin-top: 8px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  word-break: break-word;
}

.message-error-text {
  flex: 1;
}

.message-retry-btn {
  flex-shrink: 0;
}

/* ===== 时间戳 ===== */
.message-time {
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}

.message-time.user-time {
  text-align: right;
}

@keyframes ailabx-dot-pulse {
  0% {
    transform: scale(0.85);
    opacity: 0.65;
  }

  50% {
    transform: scale(1.15);
    opacity: 1;
  }

  100% {
    transform: scale(0.85);
    opacity: 0.65;
  }
}
</style>
