<template>
  <div class="page">
    <div class="card">
      <header class="header">
        <h1>本地 AI 助手（OpenClaw）</h1>
        <button class="ghost" @click="clearChat" :disabled="loading">清空会话</button>
      </header>

      <main class="chat" ref="chatRef">
        <div v-for="(m, i) in messages" :key="i" :class="['row', m.role]">
          <div class="bubble">
            <div class="meta">{{ m.role === 'user' ? '你' : 'AI' }}</div>
            <div class="text">{{ m.content }}</div>
          </div>
        </div>
      </main>

      <footer class="footer">
        <textarea
          v-model="input"
          class="input"
          placeholder="输入你的问题，Enter 发送，Shift+Enter 换行"
          @keydown.enter.exact.prevent="send"
        />
        <button class="send" :disabled="loading || !input.trim()" @click="send">
          {{ loading ? '生成中...' : '发送' }}
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'

type Role = 'user' | 'assistant'
interface Msg { role: Role; content: string }

const input = ref('')
const loading = ref(false)
const chatRef = ref<HTMLElement | null>(null)
const messages = ref<Msg[]>([
  { role: 'assistant', content: '你好，我是你的本地 OpenClaw 助手。' }
])

const scrollBottom = async () => {
  await nextTick()
  const el = chatRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const clearChat = () => {
  messages.value = [{ role: 'assistant', content: '会话已清空，我们重新开始吧。' }]
}

const send = async () => {
  const content = input.value.trim()
  if (!content || loading.value) return

  messages.value.push({ role: 'user', content })
  input.value = ''

  // AI 占位，后续流式追加
  const ai = { role: 'assistant' as const, content: '' }
  messages.value.push(ai)
  loading.value = true
  await scrollBottom()

  try {
    const history = messages.value.slice(0, -1) // 不包含当前 ai 占位
    const resp = await fetch('/api/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: content, history })
    })

    if (!resp.ok || !resp.body) throw new Error(`HTTP ${resp.status}`)

    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let done = false

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
          ai.content += payload.token || ''
          await scrollBottom()
        } else if (event === 'error') {
          ai.content += `\n[错误] ${payload.message || '未知错误'}`
          await scrollBottom()
        } else if (event === 'done') {
          done = true
          break
        }
      }
    }

    if (!ai.content.trim()) ai.content = '(模型没有返回内容)'
  } catch (e: any) {
    ai.content += `\n请求失败：${e?.message || '未知错误'}`
  } finally {
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
.text { white-space: pre-wrap; word-break: break-word; line-height: 1.65; }
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
</style>
