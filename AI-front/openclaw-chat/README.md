一、这个项目整体在做什么？
你这个项目本质上是一个“本地 OpenClaw 的聊天壳”：

前端（Vue）：负责聊天界面（输入框、消息列表、流式显示）

后端（Node/Express）：负责安全地调用 OpenClaw API（带 token），并把结果转发给前端

OpenClaw 服务：真正生成 AI 回复的模型服务

可以理解成：

浏览器（UI） ⇄ 你的后端代理（网关） ⇄ OpenClaw（模型）

二、前端是干啥的？
前端只做三类事：

收集用户输入

用户输入“你好”

前端把消息加入聊天列表（你发出的气泡）

调用你自己的后端接口

请求 /api/chat/stream（不是直接请求 OpenClaw）

把 message 和 history 发过去

接收流式结果并实时渲染

后端用 SSE 推 token（event: token）

前端一段段追加到当前 AI 气泡里

最后收到 event: done 结束这轮回复

三、后端是干啥的？
后端是“安全代理层 + 协议转换层”：

接收前端请求

POST /api/chat/stream

参数：当前消息 + 历史对话

拼 OpenClaw 请求体

加上模型名（model: default）

开启流式（stream: true）

拼 messages 数组

带鉴权调用 OpenClaw

Authorization: Bearer <OPENCLAW_API_KEY>

目标地址：OPENCLAW_BASE_URL/v1/chat/completions

把 OpenClaw 流结果转发给前端

读取上游流数据

提取 token（如 choices[0].delta.content）

以 SSE 的 event: token 推给浏览器

结束时发 event: done