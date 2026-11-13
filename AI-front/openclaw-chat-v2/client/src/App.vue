<template>
  <div class="page">
    <div class="card">
      <header class="header">
        <h1>AILabX AI助手</h1>
        <div class="header-right">
          <div class="temp-setting">
            <label class="temp-label" for="tempRange">温度</label>
            <input
              id="tempRange"
              type="range"
              min="0"
              max="2"
              step="0.1"
              v-model.number="temperature"
              class="temp-range"
            />
            <span class="temp-value">{{ temperature.toFixed(1) }}</span>
            <div class="temp-presets">
              <button
                v-for="p in tempPresets"
                :key="p.label"
                class="preset-btn"
                :class="{ active: isPresetActive(p.value) }"
                @click="setPreset(p.value)"
                type="button"
              >
                {{ p.label }}
              </button>
            </div>
          </div>
          <button class="ghost" @click="clearChat" :disabled="loading">清空会话</button>
        </div>
      </header>

      <main class="chat" ref="chatRef">
        <div v-for="(m, i) in messages" :key="i" :class="['row', m.role]">
          <div class="bubble">
            <div class="meta">
              {{ m.role === 'user' ? '你' : 'AI' }}
              <span
                v-if="m.role === 'assistant' && loading && i === activeAiIndex"
                class="thinking"
              >
                <span class="status-dot" :class="{ generating: streamStage === 'generating' }"></span>
                <span class="status-text">{{ streamStage === 'generating' ? '生成中...' : '思考中...' }}</span>
              </span>
            </div>
            <div class="text">{{ getDisplayContent(m, i) }}</div>
            <div v-if="m.error" class="error-box">
              <div class="error-text">[错误] {{ m.error }}</div>
              <button
                v-if="canRetry(i)"
                class="error-retry"
                type="button"
                :disabled="loading"
                @click="retryByIndex(i)"
              >
                重试
              </button>
            </div>
            <div v-if="shouldShowTime(i)" class="time">{{ formatTime(m.ts) }}</div>
          </div>
        </div>
      </main>

      <footer class="footer">
        <textarea
          v-model="input"
          class="input"
          placeholder="输入你的问题，Enter 发送，Shift+Enter 换行"
          @keydown.enter.exact.prevent="handleEnter"
        />
        <button class="send" :disabled="!loading && !input.trim()" @click="sendOrStop">
          {{ loading ? '停止' : '发送' }}
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'

type Role = 'user' | 'assistant'
interface Msg { role: Role; content: string; ts: number; error?: string }

// localStorage 持久化 key：用于刷新后保留最近会话与温度设置
const STORAGE_KEY = 'openclaw_chat_messages_v1'
const TEMP_STORAGE_KEY = 'openclaw_chat_temperature_v1'

const input = ref('')
const loading = ref(false)

// temperature：控制模型“发散程度”，越低越稳定，越高越随机
const temperature = ref(0.7)
const tempPresets = [
  { label: '保守', value: 0.2 },
  { label: '平衡', value: 0.7 },
  { label: '发散', value: 1.2 }
]
const chatRef = ref<HTMLElement | null>(null)

// 用于中断本轮 SSE 流（点击“停止”）
const streamController = ref<AbortController | null>(null)

// 当前正在流式输出的 AI 消息索引（用于在 UI 中显示“思考中/生成中”状态）
const activeAiIndex = ref<number | null>(null)

// streamStage：用于区分“未产出 token（思考中）”与“已开始输出 token（生成中）”
const streamStage = ref<'thinking' | 'generating' | null>(null)

// 页面首次加载时的默认欢迎语
const getDefaultMessages = (): Msg[] => ([
  { role: 'assistant', content: '你好，我是你的AILabX AI助手。', ts: Date.now() }
])

// 从 localStorage 载入历史会话；只保留必要字段，并做基本容错
const loadMessages = (): Msg[] => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return getDefaultMessages()
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return getDefaultMessages()
    const valid = parsed
      .filter((m: any) => (m?.role === 'user' || m?.role === 'assistant') && typeof m?.content === 'string')
      .map((m: any) => ({
        role: m.role as Role,
        content: m.content,
        ts: Number(m?.ts) || Date.now(),
        error: typeof m?.error === 'string' ? m.error : ''
      }))
      .filter((m: Msg) => m.content.trim().length > 0 || !!m.error)
    return valid.length ? valid : getDefaultMessages()
  } catch {
    return getDefaultMessages()
  }
}
const messages = ref<Msg[]>(loadMessages())

// 从 localStorage 载入温度设置（并在前端做一次 0~2 的范围保护）
const loadTemperature = () => {
  try {
    const raw = localStorage.getItem(TEMP_STORAGE_KEY)
    if (!raw) return
    const val = Number(raw)
    if (!Number.isNaN(val)) {
      temperature.value = Math.min(2, Math.max(0, val))
    }
  } catch {
    // ignore
  }
}
loadTemperature()

// 会话/温度变化后自动持久化到 localStorage
watch(messages, (val) => {
  const safeMessages = val.filter(m => m.content.trim().length > 0 || !!m.error)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(safeMessages))
}, { deep: true })

watch(temperature, (val) => {
  const clamped = Math.min(2, Math.max(0, Number(val) || 0))
  if (clamped !== val) temperature.value = clamped
  localStorage.setItem(TEMP_STORAGE_KEY, String(clamped))
})

const isPresetActive = (v: number) => Math.abs(temperature.value - v) < 0.05
const setPreset = (v: number) => {
  temperature.value = v
}

// 用于统一控制“空内容”时的显示：避免流式进行中出现“模型没有返回内容”的闪烁
const getDisplayContent = (m: Msg, idx: number) => {
  if (m.content.trim()) return m.content
  if (m.error) return ''
  const isActiveEmptyAssistant =
    m.role === 'assistant' &&
    loading.value &&
    activeAiIndex.value === idx
  if (isActiveEmptyAssistant) return ''
  return '(模型没有返回内容)'
}

// 将后端 error 事件内容写到当前 AI 气泡（展示为错误卡片）
const appendErrorText = (msg: Msg, text: string) => {
  const clean = (text || '').trim() || '未知错误'
  msg.error = clean
}

// “重试”会自动取该条 AI 回复之前最近的一条用户提问
const findRetryQuestion = (assistantIndex: number) => {
  for (let i = assistantIndex - 1; i >= 0; i -= 1) {
    const m = messages.value[i]
    if (m?.role === 'user' && m.content.trim()) return m.content.trim()
  }
  return ''
}

const canRetry = (assistantIndex: number) => {
  if (loading.value) return false
  const question = findRetryQuestion(assistantIndex)
  return !!question
}

const retryByIndex = (assistantIndex: number) => {
  if (loading.value) return
  const question = findRetryQuestion(assistantIndex)
  if (!question) return
  input.value = question
  send()
}

// 时间戳显示规则：今天显示 HH:mm；昨天显示“昨天 HH:mm”；更早显示“YYYY-MM-DD HH:mm”
const formatTime = (ts: number) => {
  const d = new Date(ts)
  const now = new Date()
  const hh = `${d.getHours()}`.padStart(2, '0')
  const mm = `${d.getMinutes()}`.padStart(2, '0')
  const hm = `${hh}:${mm}`

  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const startOfTargetDay = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const dayDiff = Math.floor((startOfToday.getTime() - startOfTargetDay.getTime()) / (24 * 60 * 60 * 1000))

  if (dayDiff === 0) return hm
  if (dayDiff === 1) return `昨天 ${hm}`

  const y = d.getFullYear()
  const mon = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${y}-${mon}-${day} ${hm}`
}

const minuteKey = (ts: number) => {
  const d = new Date(ts)
  const y = d.getFullYear()
  const mon = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  const hh = `${d.getHours()}`.padStart(2, '0')
  const mm = `${d.getMinutes()}`.padStart(2, '0')
  return `${y}-${mon}-${day}-${hh}-${mm}`
}

const shouldShowTime = (idx: number) => {
  if (idx <= 0) return true
  const curr = messages.value[idx]
  const prev = messages.value[idx - 1]
  if (!curr || !prev) return true
  return minuteKey(curr.ts) !== minuteKey(prev.ts)
}

// 保持聊天窗口自动滚动到底部
const scrollBottom = async () => {
  await nextTick()
  const el = chatRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const clearChat = () => {
  messages.value = [{ role: 'assistant', content: '会话已清空，我们重新开始吧。', ts: Date.now() }]
  localStorage.removeItem(STORAGE_KEY)
}

// 中断本轮流式请求：会触发 fetch 的 AbortError
const stop = () => {
  if (!loading.value) return
  streamController.value?.abort()
}

// 同一个按钮承载“发送/停止”
const sendOrStop = () => {
  if (loading.value) {
    stop()
  } else {
    send()
  }
}

const handleEnter = () => {
  if (!loading.value) send()
}

// 核心发送逻辑：
// 1) 追加用户消息 + AI 占位
// 2) POST /api/chat/stream 获取 SSE 流
// 3) 解析 event: token / event: error / event: done 并更新 UI
const send = async () => {
  const content = input.value.trim()
  if (!content || loading.value) return

  messages.value.push({ role: 'user', content, ts: Date.now() })
  input.value = ''

  // AI 占位，后续流式追加
  const ai = { role: 'assistant' as const, content: '', ts: Date.now() }
  messages.value.push(ai)
  const aiIndex = messages.value.length - 1
  activeAiIndex.value = aiIndex
  streamStage.value = 'thinking'
  loading.value = true
  await scrollBottom()

  try {
    const controller = new AbortController()
    streamController.value = controller
    const history = messages.value.slice(0, -1) // 不包含当前 ai 占位
    const resp = await fetch('/api/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: content, history, temperature: temperature.value }),
      signal: controller.signal
    })

    if (!resp.ok || !resp.body) throw new Error(`HTTP ${resp.status}`)

    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let done = false

    // SSE 解析策略：按空行分块，每个块包含 event/data 行
    while (!done) {
      const { value, done: rDone } = await reader.read()
      done = rDone
      buffer += decoder.decode(value || new Uint8Array(), { stream: !done })

      const blocks = buffer.split('\n\n')
      buffer = blocks.pop() || ''

      for (const block of blocks) {
        const lines = block.split('\n')
        let event = 'message'
        let data = ''

        for (const l of lines) {
          if (l.startsWith('event:')) event = l.slice(6).trim()
          if (l.startsWith('data:')) data += l.slice(5).trim()
        }

        if (!data) continue
        const payload = JSON.parse(data)

        if (event === 'token') {
          if (streamStage.value !== 'generating') streamStage.value = 'generating'
          const token = payload.token || ''
          const msg = messages.value[aiIndex]
          if (msg && msg.role === 'assistant') {
            msg.content += token
          }
          await scrollBottom()
        } else if (event === 'error') {
          const msg = messages.value[aiIndex]
          if (msg && msg.role === 'assistant') {
            appendErrorText(msg, payload.message || '未知错误')
          }
          await scrollBottom()
        } else if (event === 'done') {
          done = true
          break
        }
      }
    }
  } catch (e: any) {
    const msg = messages.value[aiIndex]
    if (e?.name === 'AbortError') {
      if (msg && msg.role === 'assistant') {
        if (msg.content.trim()) {
          if (!msg.content.endsWith('（已停止）')) msg.content += '（已停止）'
        } else {
          msg.content = '（已停止）'
        }
      }
      return
    }
    if (msg && msg.role === 'assistant') {
      appendErrorText(msg, `请求失败：${e?.message || '未知错误'}`)
    }
  } finally {
    streamController.value = null
    activeAiIndex.value = null
    streamStage.value = null
    loading.value = false
    scrollBottom()
  }
}
</script>

<style scoped>
.page { height: 100%; padding: 20px; }
.card {
  max-width: 900px; height: calc(100vh - 40px); margin: 0 auto;
  background: #fff; border: 1px solid #e8e8e8; border-radius: 14px;
  display: flex; flex-direction: column; overflow: hidden;
}
.header {
  height: 60px; padding: 0 16px; border-bottom: 1px solid #eee;
  display: flex; align-items: center; justify-content: space-between;
}
.header h1 { font-size: 18px; margin: 0; }
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.temp-setting {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.temp-label {
  font-size: 12px;
  color: #666;
}
.temp-range {
  width: 120px;
}
.temp-value {
  font-size: 12px;
  min-width: 30px;
  text-align: right;
  color: #1677ff;
}
.temp-presets {
  display: inline-flex;
  gap: 6px;
  margin-left: 2px;
}
.preset-btn {
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  border-radius: 12px;
  font-size: 12px;
  line-height: 1;
  padding: 4px 8px;
  cursor: pointer;
}
.preset-btn:hover {
  border-color: #1677ff;
  color: #1677ff;
}
.preset-btn.active {
  background: #1677ff;
  border-color: #1677ff;
  color: #fff;
}
.ghost {
  border: 1px solid #ddd; background: #fff; border-radius: 8px;
  padding: 8px 12px; cursor: pointer;
}
.chat { flex: 1; overflow-y: auto; padding: 16px; background: #fafbff; }
.row { display: flex; margin-bottom: 12px; }
.row.user { justify-content: flex-end; }
.row.assistant { justify-content: flex-start; }
.bubble {
  max-width: 75%; border: 1px solid #ececec; background: #fff;
  border-radius: 12px; padding: 10px 12px;
}
.row.user .bubble { background: #1677ff; color: #fff; border-color: #1677ff; }
.meta { font-size: 12px; opacity: 0.75; margin-bottom: 4px; }
.thinking {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1677ff;
  animation: pulse 1.2s ease-in-out infinite;
}
.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #1677ff;
  display: inline-block;
  animation: dotPulse 1s ease-in-out infinite;
}
.status-dot.generating {
  background: #22c55e;
}
.status-text {
  letter-spacing: 0.2px;
}
.text { white-space: pre-wrap; word-break: break-word; line-height: 1.65; }
.error-box {
  margin-top: 8px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #b91c1c;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.error-text {
  flex: 1;
}
.error-retry {
  border: 1px solid #fca5a5;
  background: #fff;
  color: #b91c1c;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}
.error-retry:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.time {
  margin-top: 6px;
  font-size: 11px;
  opacity: 0.55;
}
.footer {
  border-top: 1px solid #eee; padding: 12px;
  display: flex; gap: 10px;
}
.input {
  flex: 1; min-height: 70px; resize: vertical;
  border: 1px solid #ddd; border-radius: 10px; padding: 10px;
}
.send {
  width: 90px; border: none; border-radius: 10px;
  background: #1677ff; color: #fff; cursor: pointer;
}
.send:disabled, .ghost:disabled { opacity: 0.55; cursor: not-allowed; }

@keyframes pulse {
  0% { opacity: 0.4; }
  50% { opacity: 1; }
  100% { opacity: 0.4; }
}

@keyframes dotPulse {
  0% { transform: scale(0.8); opacity: 0.65; }
  50% { transform: scale(1.15); opacity: 1; }
  100% { transform: scale(0.8); opacity: 0.65; }
}
</style>
