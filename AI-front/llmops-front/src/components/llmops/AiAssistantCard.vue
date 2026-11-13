<template>
  <div class="ai-assistant-card card-hover" :class="{ 'is-fullscreen': isFullScreen }">
    <div class="assistant-header">
      <div class="assistant-info">
        <div class="assistant-avatar">
          <i class="fa fa-android"></i>
        </div>
        <div class="assistant-name-wrap">
          <span class="assistant-name">{{ assistantName }}</span>
          <span class="assistant-status">在线</span>
        </div>
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

    <div class="chat-area" ref="chatPreviewArea">
      <div class="chat-message" :class="msg.role === 'assistant' ? 'ai-message' : 'user-message'"
        v-for="(msg, idx) in chatMessages" :key="'preview-' + idx">
        <template v-if="msg.role === 'assistant'">
          <div class="message-avatar">AI</div>
          <div class="message-content-wrap">
            <div class="message-content" v-if="msg.loading && !msg.content && !msg.error">
              <span class="message-loading">
                <span class="ai-status-dot" :class="{ generating: previewStreamStage === 'generating' }"></span>
                <span class="message-loading-text">{{ previewStreamStage === 'generating' ? 'AI 正在生成中...' : 'AI正在思考中...'
                }}</span>
              </span>
            </div>
            <div class="message-content" v-else-if="msg.content || msg.error">
              <div class="markdown-body" v-if="msg.content" v-html="renderMarkdown(msg.content)"></div>
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
            </div>
            <div v-if="msg.ts && shouldShowTime(idx)" class="ai-message-time">{{ formatTime(msg.ts) }}</div>
          </div>
        </template>

        <template v-else>
          <div class="message-content-wrap user-wrap">
            <div class="message-content user-preview-content">{{ msg.content }}</div>
            <div v-if="msg.ts && shouldShowTime(idx)" class="ai-message-time user-time">{{ formatTime(msg.ts) }}</div>
          </div>
          <div class="message-avatar user-avatar">我</div>
        </template>
      </div>
    </div>

    <div class="chat-input-area">
      <el-input v-model="robotInput" :placeholder="placeholder" class="chat-input" :disabled="previewSending"
        @keyup.enter.native="sendRobotMessage"></el-input>
      <el-button v-if="!previewSending" type="primary" class="send-btn" @click="sendRobotMessage">
        <i class="el-icon-s-promotion"></i> 发送
      </el-button>
      <el-button v-else type="danger" class="send-btn stop-btn" @click="stopPreviewMessage">
        <i class="el-icon-video-pause"></i> 停止
      </el-button>
    </div>

    <div class="quick-suggestions" v-if="quickSuggestions && quickSuggestions.length">
      <span class="suggestion-label">快捷建议</span>
      <div class="suggestion-buttons">
        <el-button v-for="suggestion in quickSuggestions" :key="suggestion" size="mini" class="suggestion-btn"
          @click="handleQuickQuestion(suggestion)">{{ suggestion }}</el-button>
      </div>
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
  name: 'AiAssistantCard',
  props: {
    assistantName: {
      type: String,
      default: 'AI助手'
    },
    welcomeMessage: {
      type: String,
      default: '你好！我是智能助手，有什么可以帮助你的？'
    },
    quickSuggestions: {
      type: Array,
      default: () => []
    },
    placeholder: {
      type: String,
      default: '输入你的需求...'
    },
    apiBase: {
      type: String,
      default: '/api'
    },
    showTemperature: {
      type: Boolean,
      default: true
    },
    storageKey: {
      type: String,
      default: 'llmops_assistant_default'
    }
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
    session() {
      return this.$store.getters['aiChat/getSession'](this.storageKey)
    },
    chatMessages() {
      return this.session ? this.session.chatMessages : []
    },
    previewSending() {
      return this.session ? this.session.sending : false
    },
    previewStreamStage() {
      return this.session ? this.session.streamStage : null
    }
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
      this.$store.commit('aiChat/abortSession', { key: this.storageKey })
    },
    isAbortError(e) {
      if (!e) return false;
      return e.name === 'AbortError' || /aborted|abort/i.test(e.message || '');
    },
    async sendRobotMessage() {
      const text = (this.robotInput || '').trim()
      if (!text || this.previewSending) return

      this.$store.commit('aiChat/pushMessage', {
        key: this.storageKey,
        msg: { role: 'user', content: text, ts: Date.now() }
      })
      this.robotInput = ''
      this.$store.commit('aiChat/setSending', { key: this.storageKey, value: true })
      this.$store.commit('aiChat/setStreamStage', { key: this.storageKey, value: 'thinking' })
      this.$nextTick(() => this.scrollPreviewBottom())

      const session = this.$store.getters['aiChat/getSession'](this.storageKey)
      const history = (session.chatMessages)
        .slice(0, -1)
        .filter(m => !m.error && (m.content || '').trim())
        .map(m => ({ role: m.role, content: m.content }))

      const aiMsg = { role: 'assistant', content: '', loading: true, ts: Date.now(), error: '' }
      this.$store.commit('aiChat/pushMessage', { key: this.storageKey, msg: aiMsg })
      this.$nextTick(() => this.scrollPreviewBottom())

      const ctrl = new AbortController();
      this.$store.commit('aiChat/setAbortCtrl', { key: this.storageKey, ctrl })

      try {
        await this.streamChat(text, history, token => {
          if (this.previewStreamStage !== 'generating') {
            this.$store.commit('aiChat/setStreamStage', { key: this.storageKey, value: 'generating' })
          }
          this.$store.commit('aiChat/appendToken', { key: this.storageKey, token })
          this.$nextTick(() => this.scrollPreviewBottom())
        }, ctrl.signal)

        const currentSession = this.$store.getters['aiChat/getSession'](this.storageKey)
        const msgs = currentSession.chatMessages
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant' && !last.content.trim()) {
          this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'content', value: '（机器人未返回内容）' })
        }
      } catch (e) {
        if (this.isAbortError(e)) {
          const currentSession = this.$store.getters['aiChat/getSession'](this.storageKey)
          const msgs = currentSession.chatMessages
          const last = msgs[msgs.length - 1]
          if (last && last.role === 'assistant') {
            const existing = last.content || ''
            if (existing.trim() && !existing.endsWith('（已停止生成）')) {
              this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'content', value: existing + '\n\n_（已停止生成）_' })
            } else if (!existing.trim()) {
              this.$store.commit('aiChat/setLastAiMsgField', { key: this.storageKey, field: 'content', value: '（已停止生成）' })
            }
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
      this.$store.commit('aiChat/clearSession', { key: this.storageKey, welcomeMessage: this.welcomeMessage })
      try { localStorage.removeItem(this.storageKey); } catch (e) { }
    },
    canRetry(idx) {
      if (this.previewSending) return false;
      return idx > 0 && this.chatMessages[idx - 1].role === 'user';
    },
    retryMessage(idx) {
      if (this.previewSending) return;
      const question = this.chatMessages[idx - 1].content;
      const msgs = this.chatMessages.slice(0, idx - 1)
      this.$store.commit('aiChat/loadMessages', { key: this.storageKey, messages: msgs })
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
        const slim = safe.map(m => ({ role: m.role, content: m.content || '', ts: m.ts || Date.now(), error: m.error || '' }));
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
          .map(m => ({ role: m.role, content: typeof m.content === 'string' ? m.content : '', ts: Number(m.ts) || Date.now(), error: typeof m.error === 'string' ? m.error : '' }));
      } catch (e) { return null; }
    },
    persistTemperature() { try { localStorage.setItem(this.storageKey + '_temp', String(this.clampTemperature(this.temperature))); } catch (e) { } },
    loadTemperature() {
      try { const raw = localStorage.getItem(this.storageKey + '_temp'); if (raw == null) return; const v = Number(raw); if (!Number.isNaN(v)) this.temperature = this.clampTemperature(v); } catch (e) { }
    }
  },
  mounted() {
    this.$store.commit('aiChat/initSession', { key: this.storageKey, welcomeMessage: this.welcomeMessage })

    this.loadTemperature();
    if (!this.previewSending) {
      const saved = this.loadChat();
      if (saved && saved.length) {
        this.$store.commit('aiChat/loadMessages', { key: this.storageKey, messages: saved })
      }
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
.ai-assistant-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.assistant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.assistant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.assistant-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assistant-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.assistant-status {
  font-size: 12px;
  padding: 2px 8px;
  background-color: #dcfce7;
  color: #16a34a;
  border-radius: 12px;
}

.chat-area {
  min-height: 80px;
  max-height: 220px;
  overflow-y: auto;
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message {
  display: flex;
  gap: 12px;
}

.ai-message {
  justify-content: flex-start;
}

.user-message {
  justify-content: flex-end;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.message-content {
  background-color: white;
  padding: 12px 16px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  font-size: 14px;
  color: #1f2937;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  word-break: break-word;
}

.user-preview-content {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
}

.user-avatar {
  background: linear-gradient(135deg, #10b981, #059669);
}

.message-loading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
}

.chat-input-area {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.chat-input {
  flex: 1;
}

.send-btn {
  border-radius: 8px;
  padding: 0 20px;
}

.stop-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;

  &:hover {
    background: linear-gradient(135deg, #dc2626, #b91c1c);
  }
}

.quick-suggestions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.suggestion-label {
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
}

.suggestion-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  font-size: 18px;
  margin-left: 10px;
}

.fullscreen-btn {
  font-size: 18px;
}

.suggestion-btn {
  border-radius: 12px;
  background-color: #f3f4f6 !important;
  border: none;
  color: #374151;

  &:hover {
    background-color: #e5e7eb;
    color: #1f2937;
  }
}

.assistant-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

/* ===== AI 助手统一增强样式 ===== */
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

.message-content-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-content-wrap.user-wrap {
  align-items: flex-end;
}

.ai-status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #3b82f6;
  margin-right: 4px;
  animation: aiCardDotPulse 1s ease-in-out infinite;
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

@keyframes aiCardDotPulse {
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
</style>

<style>
/* 非 scoped 样式用于 Markdown */
.ai-assistant-card .markdown-body {
  font-size: 14px;
  line-height: 1.6;
  color: inherit;

  pre {
    background: #f6f8fa;
    padding: 12px;
    border-radius: 6px;
    overflow: auto;
    margin: 8px 0;
  }

  code {
    background: rgba(175, 184, 193, 0.2);
    padding: 0.2em 0.4em;
    border-radius: 6px;
    font-size: 85%;
  }
}
</style>
