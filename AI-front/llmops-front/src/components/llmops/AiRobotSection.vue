<template>
  <div class="robot-section" :class="{ 'is-fullscreen': isFullScreen }">
    <div class="robot-header">
      <div class="robot-title-section">
        <div class="robot-title">{{ title }}</div>
        <div class="robot-subtitle">{{ subtitle }}</div>
      </div>
      <div class="robot-status-wrapper">
        <div class="robot-status">
          <span class="status-dot-online"></span>
          <span class="status-text-online">在线</span>
        </div>
        <div class="assistant-actions">
          <div class="ai-temp-control" v-if="showTemperature">
            <span class="ai-temp-label">温度</span>
            <input type="range" min="0" max="2" step="0.1" v-model.number="temperature" class="ai-temp-range" />
            <span class="ai-temp-value">{{ Number(temperature).toFixed(1) }}</span>
            <div class="ai-temp-presets">
              <button v-for="p in tempPresets" :key="p.label" type="button" class="ai-temp-preset-btn"
                :class="{ active: isTempPresetActive(p.value) }" @click="setTempPreset(p.value)">{{ p.label }}</button>
            </div>
          </div>
          <el-tooltip content="清空对话" placement="top">
            <el-button type="text" class="action-btn" @click="clearChat" :disabled="previewSending">
              <i class="el-icon-delete"></i>
            </el-button>
          </el-tooltip>
          <el-tooltip :content="isFullScreen ? '退出全屏' : '全屏展示'" placement="top">
            <el-button type="text" class="fullscreen-btn" @click="toggleFullScreen">
              <i :class="isFullScreen ? 'el-icon-copy-document' : 'el-icon-full-screen'"></i>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <div class="robot-content" ref="chatPreviewArea">
      <div v-for="(msg, idx) in chatMessages" :key="'msg-' + idx" class="chat-message"
        :class="msg.role === 'user' ? 'user-message' : 'robot-message'">
        <div class="robot-avatar" :class="msg.role === 'user' ? 'user-avatar' : ''">
          <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-chat-dot-round'"></i>
        </div>
        <div class="robot-bubble" :class="msg.role === 'user' ? 'user-bubble' : ''">
          <div class="message-text">
            <template v-if="msg.role === 'assistant' && msg.loading && !msg.content && !msg.error">
              <span class="message-loading">
                <span class="ai-status-dot" :class="{ generating: previewStreamStage === 'generating' }"></span>
                <span class="message-loading-text">{{ previewStreamStage === 'generating' ? 'AI 正在生成中...' : 'AI正在思考中...'
                }}</span>
              </span>
            </template>
            <template v-else-if="msg.role === 'assistant'">
              <div class="markdown-body" v-if="msg.content" v-html="renderMarkdown(msg.content)"></div>

              <ul v-if="msg.features" class="robot-features">
                <li v-for="(feature, fIdx) in msg.features" :key="fIdx">{{ feature }}</li>
              </ul>
              <div v-if="msg.question" class="robot-question">{{ msg.question }}</div>

              <div v-if="msg.loading && msg.content" class="ai-status-pill">
                <span class="ai-status-dot generating"></span>
                <span>生成中...</span>
              </div>
              <div v-if="msg.error" class="ai-error-card">
                <span class="ai-error-text">[错误] {{ msg.error }}</span>
                <el-button v-if="canRetry(idx)" size="mini" type="danger" plain class="ai-retry-btn"
                  :disabled="previewSending" @click="retryMessage(idx)">重试</el-button>
              </div>
              <!-- <div v-if="!msg.loading && !msg.error" class="message-actions"> -->
              <!-- <el-button v-if="canRetry(idx)" type="text" size="mini" @click="retryMessage(idx)">重试</el-button> -->
              <!-- </div> -->
            </template>
            <template v-else>{{ msg.content }}</template>
          </div>
          <div v-if="msg.ts && shouldShowTime(idx)" class="ai-message-time"
            :class="{ 'user-time': msg.role === 'user' }">{{
              formatTime(msg.ts) }}</div>
        </div>
      </div>
    </div>

    <div class="quick-questions" v-if="quickQuestions && quickQuestions.length">
      <el-button v-for="q in quickQuestions" :key="q" class="quick-question-btn" size="mini" round
        @click="handleQuickQuestion(q)">
        {{ q }}
      </el-button>
    </div>

    <div class="robot-input-section">
      <el-input v-model="robotInput" :placeholder="placeholder" class="robot-input" :disabled="previewSending"
        @keyup.enter.native="sendRobotMessage">
      </el-input>
      <el-button v-if="!previewSending" type="primary" class="send-btn" @click="sendRobotMessage">
        发送
      </el-button>
      <el-button v-else type="danger" class="send-btn stop-btn" @click="stopPreviewMessage">
        停止
      </el-button>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

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
  name: 'AiRobotSection',
  props: {
    title: { type: String, default: '智能助手' },
    subtitle: { type: String, default: '基于大模型的智能辅助系统' },
    welcomeMessage: { type: Object, default: () => ({ role: 'assistant', content: '你好！有什么我可以帮你的吗？' }) },
    quickQuestions: { type: Array, default: () => [] },
    placeholder: { type: String, default: '输入您的问题...' },
    apiBase: { type: String, default: '/api' },
    showTemperature: { type: Boolean, default: true },
    storageKey: { type: String, default: 'llmops_robot_default' }
  },
  data() {
    return {
      isFullScreen: false,
      robotInput: '',
      temperature: 0.7,
      tempPresets: [
        { label: '保守', value: 0.2 },
        { label: '平衡', value: 0.7 },
        { label: '发散', value: 1.2 }
      ]
    }
  },
  computed: {
    session() { return this.$store.getters['aiChat/getSession'](this.storageKey) },
    chatMessages() { return this.session ? this.session.chatMessages : [] },
    previewSending() { return this.session ? this.session.sending : false },
    previewStreamStage() { return this.session ? this.session.streamStage : null }
  },
  methods: {
    handleQuickQuestion(question) {
      this.robotInput = question
    },
    renderMarkdown(content) {
      if (!content) return ''
      return marked(content)
    },
    toggleFullScreen() {
      this.isFullScreen = !this.isFullScreen;
      if (this.isFullScreen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
      this.$nextTick(() => {
        this.scrollPreviewBottom();
      });
    },
    stopPreviewMessage() {
      this.$store.commit('aiChat/abortSession', { key: this.storageKey });
    },
    isAbortError(e) {
      if (!e) return false;
      return e.name === 'AbortError' || /aborted|abort/i.test(e.message || '');
    },
    async sendRobotMessage() {
      const text = (this.robotInput || '').trim()
      if (!text || this.previewSending) return

      this.$store.commit('aiChat/pushMessage', { key: this.storageKey, msg: { role: 'user', content: text, ts: Date.now() } })
      this.robotInput = ''
      this.$store.commit('aiChat/setSending', { key: this.storageKey, value: true })
      this.$store.commit('aiChat/setStreamStage', { key: this.storageKey, value: 'thinking' })
      this.$nextTick(() => this.scrollPreviewBottom())

      const history = this.chatMessages
        .slice(0, -1)
        .filter(m => !m.error && (m.content || '').trim())
        .map(m => ({ role: m.role, content: m.content }))
      this.$store.commit('aiChat/pushMessage', { key: this.storageKey, msg: { role: 'assistant', content: '', loading: true, ts: Date.now(), error: '' } })
      this.$nextTick(() => this.scrollPreviewBottom())

      const ctrl = new AbortController();
      this.$store.commit('aiChat/setAbortCtrl', { key: this.storageKey, ctrl });

      try {
        await this.streamChat(text, history, token => {
          if (this.previewStreamStage !== 'generating') this.$store.commit('aiChat/setStreamStage', { key: this.storageKey, value: 'generating' });
          this.$store.commit('aiChat/appendToken', { key: this.storageKey, token })
          this.$nextTick(() => this.scrollPreviewBottom())
        }, ctrl.signal)

        const lastContent = (this.chatMessages[this.chatMessages.length - 1] || {}).content || ''
        if (!lastContent.trim()) this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'content', value: '（机器人未返回内容）' })
      } catch (e) {
        if (this.isAbortError(e)) {
          const lastContent = (this.chatMessages[this.chatMessages.length - 1] || {}).content || ''
          if (lastContent.trim() && !lastContent.endsWith('（已停止生成）')) {
            this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'content', value: lastContent + '\n\n_（已停止生成）_' })
          } else if (!lastContent.trim()) {
            this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'content', value: '（已停止生成）' })
          }
        } else {
          this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'error', value: e && e.message ? e.message : String(e) })
        }
      } finally {
        this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'loading', value: false })
        this.$store.commit('aiChat/setSending', { key: this.storageKey, value: false })
        this.$store.commit('aiChat/setStreamStage', { key: this.storageKey, value: null })
        this.$store.commit('aiChat/setAbortCtrl', { key: this.storageKey, ctrl: null })
        this.persistChat()
        this.$nextTick(() => {
          this.scrollPreviewBottom()
          this.highlightCode()
        })
      }
    },
    async streamChat(message, history, onToken, signal) {
      const resp = await fetch(`${this.apiBase}/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          message,
          history,
          temperature: this.clampTemperature(this.temperature)
        }),
        signal
      })

      if (!resp.ok || !resp.body) {
        const t = await resp.text().catch(() => '')
        throw new Error(`HTTP ${resp.status} ${t}`)
      }

      const reader = resp.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      let done = false

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
          const { value, done: readDone } = await reader.read()
          done = readDone
          buffer += decoder.decode(value || new Uint8Array(), { stream: !done })

          const blocks = buffer.split('\n\n')
          buffer = blocks.pop() || ''

          for (const block of blocks) {
            const lines = block.split('\n')
            let event = 'message'
            let data = ''

            for (const line of lines) {
              if (line.startsWith('event:')) event = line.slice(6).trim()
              if (line.startsWith('data:')) data += line.slice(5).trim()
            }

            if (!data) continue

            let payload = {}
            try {
              payload = JSON.parse(data)
            } catch (e) {
              continue
            }

            if (event === 'token') {
              onToken(payload.token || '')
            } else if (event === 'error') {
              throw new Error(payload.message || '流式请求异常')
            } else if (event === 'done') {
              done = true
              break
            }
          }
        }
      } finally {
        if (signal) signal.removeEventListener('abort', onAbort);
      }
    },
    scrollPreviewBottom() {
      const el = this.$refs.chatPreviewArea
      if (el && el.scrollHeight !== undefined) {
        el.scrollTop = el.scrollHeight
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
    clearChat() {
      if (this.previewSending) return;
      this.$store.commit('aiChat/clearSession', { key: this.storageKey, welcomeMessage: this.welcomeMessage.content });
      try { localStorage.removeItem(this.storageKey); } catch (e) { }
    },
    canRetry(idx) {
      if (this.previewSending) return false;
      return idx > 0 && this.chatMessages[idx - 1].role === 'user';
    },
    retryMessage(idx) {
      if (this.previewSending) return;
      const question = this.chatMessages[idx - 1].content;
      const trimmed = this.chatMessages.slice(0, idx - 1);
      this.$store.commit('aiChat/loadMessages', { key: this.storageKey, messages: trimmed });
      this.robotInput = question;
      this.sendRobotMessage();
    },
    clampTemperature(v) { const n = Number(v); if (Number.isNaN(n)) return 0.7; return Math.min(2, Math.max(0, n)); },
    isTempPresetActive(v) { return Math.abs(Number(this.temperature) - v) < 0.05; },
    setTempPreset(v) { this.temperature = v; },
    formatTime(ts) {
      if (!ts) return '';
      const d = new Date(ts), now = new Date();
      const hh = String(d.getHours()).padStart(2, '0'), mm = String(d.getMinutes()).padStart(2, '0'), hm = `${hh}:${mm}`;
      const sT = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
      const sD = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
      const diff = Math.floor((sT - sD) / 86400000);
      if (diff === 0) return hm;
      if (diff === 1) return `昨天 ${hm}`;
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${hm}`;
    },
    minuteKey(ts) { if (!ts) return ''; const d = new Date(ts); return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}-${d.getHours()}-${d.getMinutes()}`; },
    shouldShowTime(idx) {
      if (idx <= 0) return true;
      const cur = this.chatMessages[idx], prev = this.chatMessages[idx - 1];
      if (!cur || !prev) return true;
      return this.minuteKey(cur.ts) !== this.minuteKey(prev.ts);
    },
    persistChat() {
      try {
        const safe = (this.chatMessages || []).filter(m => (m.content || '').trim() || m.error);
        const slim = safe.map(m => ({ role: m.role, content: m.content || '', ts: m.ts || Date.now(), error: m.error || '', features: m.features || null, question: m.question || null }));
        localStorage.setItem(this.storageKey, JSON.stringify(slim));
      } catch (e) { }
    },
    loadChat() {
      try {
        const raw = localStorage.getItem(this.storageKey);
        if (!raw) return null;
        const arr = JSON.parse(raw);
        if (!Array.isArray(arr) || !arr.length) return null;
        return arr.filter(m => m && (m.role === 'user' || m.role === 'assistant'))
          .map(m => ({ role: m.role, content: typeof m.content === 'string' ? m.content : '', ts: Number(m.ts) || Date.now(), error: typeof m.error === 'string' ? m.error : '', features: m.features || null, question: m.question || null }));
      } catch (e) { return null; }
    },
    persistTemperature() { try { localStorage.setItem(this.storageKey + '_temp', String(this.clampTemperature(this.temperature))); } catch (e) { } },
    loadTemperature() {
      try { const raw = localStorage.getItem(this.storageKey + '_temp'); if (raw == null) return; const v = Number(raw); if (!Number.isNaN(v)) this.temperature = this.clampTemperature(v); } catch (e) { }
    }
  },
  mounted() {
    this.$store.commit('aiChat/initSession', { key: this.storageKey, welcomeMessage: this.welcomeMessage.content });
    this.loadTemperature();
    if (!this.previewSending) {
      const saved = this.loadChat();
      if (saved && saved.length) this.$store.commit('aiChat/loadMessages', { key: this.storageKey, messages: saved });
    }
    this.$watch('temperature', (v) => {
      const c = this.clampTemperature(v);
      if (c !== v) this.temperature = c;
      this.persistTemperature();
    });
    this.$nextTick(() => {
      this.scrollPreviewBottom();
      this.highlightCode();
    });
  }
}
</script>

<style lang="scss" scoped>
.robot-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;

  &.is-fullscreen {
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

    .robot-header {
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f3f4f6;
    }

    .robot-content {
      flex: 1;
      max-height: none !important;
      margin-bottom: 24px;
    }

    .robot-input-section {
      margin-bottom: 24px;
    }

    .quick-questions {
      margin-top: auto;
      padding-top: 24px;
    }
  }
}

.robot-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.robot-title-section {
  flex: 1;
}

.robot-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.robot-subtitle {
  font-size: 13px;
  color: #6b7280;
}

.robot-status-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.robot-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background-color: #d1fae5;
  border-radius: 12px;
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

.assistant-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ===== AI 助手统一增强样式 (Header) ===== */
.ai-temp-control {
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

.ai-temp-control .ai-temp-label {
  color: #6b7280;
}

.ai-temp-control .ai-temp-range {
  width: 90px;
  accent-color: #3b82f6;
}

.ai-temp-control .ai-temp-value {
  min-width: 22px;
  text-align: right;
  color: #2563eb;
  font-weight: 600;
}

.ai-temp-presets {
  display: inline-flex;
  gap: 4px;
  margin-left: 2px;
}

.ai-temp-preset-btn {
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

.ai-temp-preset-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.ai-temp-preset-btn.active {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-color: #2563eb;
  color: #fff;
}

.robot-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;

  .chat-message {
    display: flex;
    align-items: flex-start;
    gap: 12px;

    &.user-message {
      flex-direction: row-reverse;
    }
  }

  .robot-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
    flex-shrink: 0;

    &.user-avatar {
      background: linear-gradient(135deg, #10b981, #059669);
    }
  }

  .robot-bubble {
    background: #f3f4f6;
    padding: 12px 16px;
    border-radius: 12px;
    border-top-left-radius: 0;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    max-width: 80%;
    width: fit-content;

    &.user-bubble {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: white;
      border-top-left-radius: 12px;
      border-top-right-radius: 0;
    }
  }

  .message-text {
    font-size: 14px;
    line-height: 1.6;
    word-break: break-word;
  }

  .message-loading {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #6b7280;
  }

  /* ===== AI 助手统一增强样式 ===== */
  .ai-status-dot {
    display: inline-block;
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: #3b82f6;
    margin-right: 4px;
    animation: aiRobotDotPulse 1s ease-in-out infinite;
  }

  .ai-status-dot.generating {
    background: #22c55e;
  }

  .ai-status-pill {
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

  .ai-error-card {
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

  .ai-error-text {
    flex: 1;
  }

  .ai-retry-btn {
    flex-shrink: 0;
  }

  .ai-message-time {
    margin-top: 4px;
    font-size: 11px;
    color: #9ca3af;
  }

  .ai-message-time.user-time {
    text-align: right;
  }

  @keyframes aiRobotDotPulse {
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

  .robot-features {
    margin: 10px 0 0 0;
    padding-left: 20px;

    li {
      font-size: 13px;
      color: #4b5563;
      margin-bottom: 6px;
      list-style-type: disc;
    }
  }

  .user-bubble .robot-features li {
    color: rgba(255, 255, 255, 0.9);
  }

  .robot-question {
    font-size: 14px;
    color: #1f2937;
    font-weight: 500;
    margin-top: 10px;
  }

  .user-bubble .robot-question {
    color: white;
  }

  .message-actions {
    margin-top: 8px;
    display: flex;
    gap: 8px;
  }
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.quick-question-btn {
  background-color: #f3f4f6 !important;
  border: none !important;
  color: #374151;
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 16px;

  &:hover {
    background-color: #e5e7eb;
    color: #1f2937;
  }
}

.action-btn {
  font-size: 18px;
  margin-left: 10px;
}

.fullscreen-btn {
  font-size: 18px;
}

.robot-input-section {
  display: flex;
  gap: 12px;
  align-items: center;

  .robot-input {
    flex: 1;
  }

  .send-btn {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    border: none;
    color: white;
    padding: 9px 15px;
    border-radius: 8px;

    &:hover {
      opacity: 0.9;
    }
  }

  .stop-btn {
    background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  }
}
</style>

<style>
/* Markdown 全局样式 */
.robot-section .markdown-body {
  word-wrap: break-word !important;
  font-size: 14px;
  line-height: 1.6;

  pre {
    background-color: #1f2937;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 8px 0;

    code {
      background-color: transparent;
      padding: 0;
      color: #e5e7eb;
    }
  }

  code {
    background-color: #f3f4f6;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 13px;
    color: #e11d48;
  }
}
</style>
