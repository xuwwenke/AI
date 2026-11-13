import express from 'express'
import cors from 'cors'
import dotenv from 'dotenv'

dotenv.config()

const app = express()
app.use(cors())
app.use(express.json({ limit: '2mb' }))

const OPENCLAW_BASE_URL = process.env.OPENCLAW_BASE_URL || 'http://127.0.0.1:18789'
const OPENCLAW_MODEL = process.env.OPENCLAW_MODEL || 'default'
const OPENCLAW_API_KEY = process.env.OPENCLAW_API_KEY || ''
const PORT = Number(process.env.PORT || 3001)

/** 健康检查 */
app.get('/api/health', (_, res) => {
  res.json({ ok: true, time: new Date().toISOString() })
})

/** 非流式（可选） */
app.post('/api/chat', async (req, res) => {
  try {
    const { message, history = [] } = req.body || {}
    if (!message?.trim()) return res.status(400).json({ message: 'message 不能为空' })

    const payload = {
      model: OPENCLAW_MODEL,
      stream: false,
      messages: [
        ...history
          .filter(m => m?.role && m?.content)
          .map(m => ({ role: m.role, content: m.content })),
        { role: 'user', content: message.trim() }
      ]
    }

    const r = await fetch(`${OPENCLAW_BASE_URL}/v1/chat/completions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(OPENCLAW_API_KEY ? { Authorization: `Bearer ${OPENCLAW_API_KEY}` } : {})
      },
      body: JSON.stringify(payload)
    })

    const text = await r.text()
    if (!r.ok) {
      return res.status(r.status).json({ message: '上游错误', detail: text })
    }

    const data = JSON.parse(text)
    const answer = data?.choices?.[0]?.message?.content ?? ''
    res.json({ answer, raw: data })
  } catch (e) {
    res.status(500).json({ message: '服务异常', detail: e?.message || String(e) })
  }
})

/** 流式 SSE */
app.post('/api/chat/stream', async (req, res) => {
  try {
    const { message, history = [] } = req.body || {}
    if (!message?.trim()) return res.status(400).json({ message: 'message 不能为空' })

    // SSE headers
    res.setHeader('Content-Type', 'text/event-stream; charset=utf-8')
    res.setHeader('Cache-Control', 'no-cache, no-transform')
    res.setHeader('Connection', 'keep-alive')
    res.flushHeaders?.()

    const payload = {
      model: OPENCLAW_MODEL,
      stream: true,
      messages: [
        ...history
          .filter(m => m?.role && m?.content)
          .map(m => ({ role: m.role, content: m.content })),
        { role: 'user', content: message.trim() }
      ]
    }

    const upstream = await fetch(`${OPENCLAW_BASE_URL}/v1/chat/completions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(OPENCLAW_API_KEY ? { Authorization: `Bearer ${OPENCLAW_API_KEY}` } : {})
      },
      body: JSON.stringify(payload)
    })

    if (!upstream.ok || !upstream.body) {
      const errText = await upstream.text().catch(() => '')
      res.write(`event: error\ndata: ${JSON.stringify({ message: '上游请求失败', detail: errText })}\n\n`)
      res.write(`event: done\ndata: {}\n\n`)
      return res.end()
    }

    const reader = upstream.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const rawLine of lines) {
        const line = rawLine.trim()
        if (!line.startsWith('data:')) continue

        const data = line.slice(5).trim()
        if (!data) continue

        if (data === '[DONE]') {
          res.write(`event: done\ndata: {}\n\n`)
          res.end()
          return
        }

        try {
          const json = JSON.parse(data)

          // 兼容 OpenAI 风格流式字段
          const token =
            json?.choices?.[0]?.delta?.content ??
            json?.choices?.[0]?.message?.content ??
            ''

          if (token) {
            res.write(`event: token\ndata: ${JSON.stringify({ token })}\n\n`)
          }
        } catch {
          // 忽略非 JSON 行
        }
      }
    }

    res.write(`event: done\ndata: {}\n\n`)
    res.end()
  } catch (e) {
    res.write?.(`event: error\ndata: ${JSON.stringify({ message: e?.message || '未知错误' })}\n\n`)
    res.write?.(`event: done\ndata: {}\n\n`)
    res.end?.()
  }
})

const HOST = process.env.HOST || '0.0.0.0'

app.listen(PORT, HOST, () => {
  console.log(`Server running at http://${HOST}:${PORT}`)
})
