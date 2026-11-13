AILabX AI助手（openclaw-chat）新增功能与优化点汇总

一、前端（client）
1) 发送/停止共用按钮
   - 点击“发送”后进入生成状态，按钮变为“停止”
   - 点击“停止”会中断本轮请求，并在回复末尾标记“（已停止）”

2) 生成状态提示
   - 在 AI 标签旁显示“思考中...”/“生成中...”
   - “生成中...”使用不同颜色提示，区分更明显

3) 会话管理
   - 新增“清空会话”按钮
   - 自动保存最近会话到本地（localStorage），刷新页面仍可看到历史对话

4) 时间戳展示
   - 每条消息显示时间戳（分钟级去重：同一分钟内的连续消息只显示一次）
   - 今天：HH:mm；昨天：昨天 HH:mm；更早：YYYY-MM-DD HH:mm

5) 温度控制
   - 温度滑杆范围 0~2（前端会做范围保护）
   - 预设按钮：保守/平衡/发散（0.2/0.7/1.2）
   - 温度设置持久化（localStorage）

6) 错误展示与重试
   - 错误以红色“错误卡片”展示，避免混在正文里
   - 错误后提供“重试”按钮：自动取该轮上一条用户提问并重新发送
   - 请求进行中不会闪烁显示“模型没有返回内容”

二、后端（server）
1) SSE 流式代理
   - 通过 /api/chat/stream 将 OpenClaw 的 OpenAI 兼容流式输出转发给前端
   - 前端通过 event: token / event: error / event: done 进行增量渲染

2) 停止/中断链路
   - 前端 AbortController 中断请求
   - 后端将中断信号透传到上游 fetch，尽快终止本轮上游调用

3) 温度参数透传与校验
   - 接收 temperature 并 clamp 到 0~2 后再转发上游，避免异常值导致报错

4) 上游错误与“空输出”兜底
   - 尝试从上游流式 JSON 中提取错误信息并以 SSE error 事件输出
   - 若上游返回 200 但全程无 token，输出更明确的提示（常见原因：限流/配额不足）

5) 后端调试开关（默认关闭）
   - 环境变量：OPENCLAW_DEBUG=true/false（也兼容 BACKEND_DEBUG）
   - 开启后输出关键链路日志（请求开始、runId、首 token 延迟、token 进度、done/abort/error 等）

6) 配置化 OpenClaw 地址
   - OPENCLAW_BASE_URL 不再写死在代码中，统一通过 server/.env 配置
   - 未配置会在启动时给出明确错误提示

三、说明
1) 本项目当前默认保持“简洁模式”
   - 不展示 tools 工具调用过程（此前相关功能与代码已移除）

四、如何运行（本地开发）
1) 启动后端（server）
   - 进入目录：openclaw-chat/server
   - 复制配置：将 .env.example 复制为 .env，并按实际部署修改 OPENCLAW_BASE_URL / OPENCLAW_API_KEY
   - 安装依赖：npm install
   - 启动服务：npm run dev
   - 默认地址：http://0.0.0.0:3001

2) 启动前端（client）
   - 进入目录：openclaw-chat/client
   - 安装依赖：npm install
   - 启动开发服务：npm run dev
   - 默认地址：http://localhost:5173

3) 访问方式
   - 浏览器打开前端地址（通常是 http://localhost:5173）
   - 前端会通过 /api/chat/stream 调用本地后端代理，再由后端转发请求到 OpenClaw
