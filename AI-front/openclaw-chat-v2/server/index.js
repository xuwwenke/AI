import express from 'express'
import cors from 'cors'
import dotenv from 'dotenv'

dotenv.config()

const app = express()
app.use(cors())
app.use(express.json({ limit: '2mb' }))

// OpenClaw 网关地址：通过 server/.env 配置，避免写死在代码里
const OPENCLAW_BASE_URL = process.env.OPENCLAW_BASE_URL
if (!OPENCLAW_BASE_URL || !String(OPENCLAW_BASE_URL).trim()) {
  console.error('缺少环境变量 OPENCLAW_BASE_URL，请在 server/.env 中配置，例如：OPENCLAW_BASE_URL=http://127.0.0.1:18789')
  process.exit(1)
}
// 模型名称：由 OpenClaw 侧定义（这里默认值为 default）
const OPENCLAW_MODEL = process.env.OPENCLAW_MODEL || 'default'
// OpenAI 兼容鉴权：如果 OpenClaw 开启了 API Key 校验，则通过 Bearer Token 传递
const OPENCLAW_API_KEY = process.env.OPENCLAW_API_KEY || ''
const PORT = Number(process.env.PORT || 3001)

// 后端调试开关：用于输出关键路径日志（默认关闭）
const isTruthy = (v) => /^(1|true|yes|on)$/i.test(String(v || '').trim())
const DEBUG_ENABLED = isTruthy(process.env.OPENCLAW_DEBUG || process.env.BACKEND_DEBUG)
const debugLog = (event, meta = {}) => {
  if (!DEBUG_ENABLED) return
  const ts = new Date().toISOString()
  try {
    console.log(`[debug][${ts}] ${event} ${JSON.stringify(meta)}`)
  } catch {
    console.log(`[debug][${ts}] ${event}`)
  }
}
const pickFirstString = (...vals) => {
  for (const v of vals) {
    if (typeof v === 'string' && v.trim()) return v
  }
  return ''
}

/** 健康检查 */
app.get('/api/health', (_, res) => {
  res.json({ ok: true, time: new Date().toISOString() })
})

/** 非流式（可选） */
app.post('/api/chat', async (req, res) => {
  const reqStart = Date.now()
  try {
    const { message, history = [], temperature } = req.body || {}
    if (!message?.trim()) return res.status(400).json({ message: 'message 不能为空' })
    // 前后端都做 clamp，避免传入异常值导致上游报错
    const parsedTemp = Number(temperature)
    const safeTemperature = Number.isFinite(parsedTemp)
      ? Math.min(2, Math.max(0, parsedTemp))
      : undefined

    const payload = {
      model: OPENCLAW_MODEL,
      stream: false,
      ...(safeTemperature !== undefined ? { temperature: safeTemperature } : {}),
      // messages：按 OpenAI 兼容格式拼接历史 + 本轮用户输入
      messages: [
        ...history
          .filter(m => m?.role && m?.content)
          .map(m => ({ role: m.role, content: m.content })),
        { role: 'user', content: message.trim() }
      ]
    }
    debugLog('chat.start', {
      endpoint: '/api/chat',
      model: OPENCLAW_MODEL,
      historyCount: Array.isArray(history) ? history.length : 0,
      messageLength: message.trim().length,
      temperature: safeTemperature
    })

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
    debugLog('chat.done', {
      endpoint: '/api/chat',
      ok: true,
      elapsedMs: Date.now() - reqStart,
      answerLength: answer.length
    })
    res.json({ answer, raw: data })
  } catch (e) {
    debugLog('chat.error', {
      endpoint: '/api/chat',
      elapsedMs: Date.now() - reqStart,
      error: e?.message || String(e)
    })
    res.status(500).json({ message: '服务异常', detail: e?.message || String(e) })
  }
})

/** 流式 SSE */
app.post('/api/chat/stream', async (req, res) => {
  // debugId：用于把同一次请求的日志串起来，排查更方便
  const reqStart = Date.now()
  const debugId = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
  // 写 SSE 时需要避免对已关闭的 response 写入
  const safeWrite = (chunk) => {
    if (!res.writableEnded) res.write(chunk)
  }
  // 统一封装 SSE 事件：event + JSON data
  const safeWriteEvent = (eventName, payload) => {
    try {
      safeWrite(`event: ${eventName}\ndata: ${JSON.stringify(payload)}\n\n`)
    } catch {
      safeWrite(`event: ${eventName}\ndata: {}\n\n`)
    }
  }
  try {
    const { message, history = [], temperature } = req.body || {}
    if (!message?.trim()) return res.status(400).json({ message: 'message 不能为空' })
    const parsedTemp = Number(temperature)
    const safeTemperature = Number.isFinite(parsedTemp)
      ? Math.min(2, Math.max(0, parsedTemp))
      : undefined
    debugLog('chat_stream.start', {
      debugId,
      endpoint: '/api/chat/stream',
      model: OPENCLAW_MODEL,
      historyCount: Array.isArray(history) ? history.length : 0,
      messageLength: message.trim().length,
      temperature: safeTemperature
    })

    // SSE headers：浏览器端使用 fetch + ReadableStream 读取
    res.setHeader('Content-Type', 'text/event-stream; charset=utf-8')
    res.setHeader('Cache-Control', 'no-cache, no-transform')
    res.setHeader('Connection', 'keep-alive')
    res.flushHeaders?.()
    const controller = new AbortController()
    let runId = ''
    let tokenChunkCount = 0
    let sentErrorEvent = false
    let firstTokenLatencyMs = -1
    let clientClosed = false
    const handleClientDisconnect = () => {
      clientClosed = true
      controller.abort()
      debugLog('chat_stream.client_disconnect', { debugId, runId, elapsedMs: Date.now() - reqStart })
    }
    // 注意：不能用 req.close 判断中断，它在请求体读取完成后也可能触发
    // 这里使用 req.aborted / res.close 来准确识别“客户端提前断开”
    req.on('aborted', handleClientDisconnect)
    res.on('close', () => {
      if (!res.writableEnded) handleClientDisconnect()
    })

    const payload = {
      model: OPENCLAW_MODEL,
      stream: true,
      ...(safeTemperature !== undefined ? { temperature: safeTemperature } : {}),
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
      body: JSON.stringify(payload),
      signal: controller.signal
    })
    debugLog('chat_stream.upstream_connected', {
      debugId,
      ok: upstream.ok,
      status: upstream.status
    })

    if (!upstream.ok || !upstream.body) {
      const errText = await upstream.text().catch(() => '')
      safeWrite(`event: error\ndata: ${JSON.stringify({ message: '上游请求失败', detail: errText })}\n\n`)
      safeWrite(`event: done\ndata: {}\n\n`)
      debugLog('chat_stream.upstream_error', {
        debugId,
        status: upstream.status,
        detail: errText?.slice?.(0, 400) || ''
      })
      return res.end()
    }

    const reader = upstream.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    // 上游是 OpenAI 兼容的 chunked 数据流：逐行解析 data: ... JSON / [DONE]
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      if (clientClosed || res.writableEnded) {
        controller.abort()
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const rawLine of lines) {
        const line = rawLine.trim()
        if (!line.startsWith('data:')) continue

        const data = line.slice(5).trim()
        if (!data) continue

        if (data === '[DONE]') {
          // 如果全程没有任何 token，给出更明确的错误提示（常见原因：上游限流/配额不足）
          if (tokenChunkCount === 0 && !sentErrorEvent) {
            const msg = '上游没有返回可展示内容，可能是模型限流或配额不足，请稍后重试。'
            safeWriteEvent('error', { message: msg })
            sentErrorEvent = true
            debugLog('chat_stream.empty_output', { debugId, runId, message: msg })
          }
          safeWriteEvent('done', {})
          debugLog('chat_stream.done', {
            debugId,
            runId,
            tokenChunkCount,
            firstTokenLatencyMs,
            elapsedMs: Date.now() - reqStart
          })
          res.end()
          return
        }

        try {
          const json = JSON.parse(data)
          // 兼容部分后端把错误写在流式 JSON 内部的情况
          const upstreamErrorMsg = pickFirstString(
            json?.error?.message,
            json?.message,
            json?.detail?.message
          )
          if (upstreamErrorMsg) {
            safeWriteEvent('error', { message: upstreamErrorMsg })
            sentErrorEvent = true
            debugLog('chat_stream.upstream_error_event', {
              debugId,
              runId,
              message: upstreamErrorMsg.slice(0, 300)
            })
          }
          const maybeRunId = json?.id ?? json?.run_id ?? json?.runId ?? ''
          if (!runId && typeof maybeRunId === 'string' && maybeRunId) {
            runId = maybeRunId
            safeWriteEvent('run_id', { runId })
            debugLog('chat_stream.run_id', { debugId, runId })
          }

          // 兼容 OpenAI 风格流式字段
          const token =
            json?.choices?.[0]?.delta?.content ??
            json?.choices?.[0]?.message?.content ??
            ''

          if (token) {
            // event: token 由前端持续拼接到当前 AI 气泡内容中
            safeWriteEvent('token', { token })
            tokenChunkCount += 1
            if (firstTokenLatencyMs < 0) {
              firstTokenLatencyMs = Date.now() - reqStart
              debugLog('chat_stream.first_token', { debugId, runId, firstTokenLatencyMs })
            } else if (tokenChunkCount % 50 === 0) {
              debugLog('chat_stream.token_progress', { debugId, runId, tokenChunkCount })
            }
          }
        } catch {
          // 忽略非 JSON 行
        }
      }
    }

    if (!clientClosed && !res.writableEnded) {
      safeWriteEvent('done', {})
      debugLog('chat_stream.done_fallback', {
        debugId,
        runId,
        tokenChunkCount,
        firstTokenLatencyMs,
        elapsedMs: Date.now() - reqStart
      })
      res.end()
    }
  } catch (e) {
    if (e?.name === 'AbortError') {
      debugLog('chat_stream.abort', { debugId, elapsedMs: Date.now() - reqStart })
      if (!res.writableEnded) res.end()
      return
    }
    debugLog('chat_stream.error', {
      debugId,
      elapsedMs: Date.now() - reqStart,
      error: e?.message || '未知错误'
    })
    safeWrite(`event: error\ndata: ${JSON.stringify({ message: e?.message || '未知错误' })}\n\n`)
    safeWrite(`event: done\ndata: {}\n\n`)
    if (!res.writableEnded) res.end()
  }
})

const HOST = process.env.HOST || '0.0.0.0'

app.listen(PORT, HOST, () => {
  console.log(`Server running at http://${HOST}:${PORT}`)
})
