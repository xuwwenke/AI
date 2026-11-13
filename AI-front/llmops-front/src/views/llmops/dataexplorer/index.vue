<template>
  <div class="dataexplorer-container">
    <div class="dataexplorer-content">
      <!-- 头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">数据探索</h1>
          <p class="page-subtitle">数据查询和数据探索工具</p>
        </div>
        <div class="header-right">
          <el-button type="primary" class="create-btn" @click="handleNewQuery">
            <i class="el-icon-plus"></i> 新增查询
          </el-button>
        </div>
      </div>

      <!-- 查询区域 -->
      <div class="section">
        <div class="query-card card-hover">
          <div class="query-header">
            <div class="query-controls">
              <div class="control-item">
                <el-select v-model="queryForm.database" size="small" placeholder="引擎" style="width: 150px;">
                  <el-option label="mysql" value="mysql"></el-option>
                  <el-option label="postgresql" value="postgresql"></el-option>
                  <el-option label="clickhouse" value="clickhouse"></el-option>
                </el-select>
              </div>
              <div class="control-item">
                <el-input v-model="queryForm.table" size="small" placeholder="数据库名称" style="width: 200px;"></el-input>
              </div>
              <div class="control-item">
                <el-button type="primary" size="small" @click="handleRunQuery">
                  <i class="el-icon-video-play"></i> 运行
                </el-button>
              </div>
            </div>
            <div class="close-btn" @click="handleCloseQuery">
              <i class="el-icon-close"></i>
            </div>
          </div>
          <div class="sql-editor">
            <el-input
              v-model="queryForm.sql"
              type="textarea"
              :rows="6"
              placeholder="SELECT * FROM table LIMIT 10;"
              class="sql-textarea"
            ></el-input>
          </div>
        </div>
      </div>

      <!-- 查询结果 -->
      <div class="section">
        <div class="results-card card-hover">
          <div class="results-title">查询结果</div>
          <div class="table-wrapper">
            <el-table
              :data="queryResults"
              style="width: 100%"
              stripe
              border
              size="small"
              :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }"
            >
              <el-table-column prop="id" label="id" width="80"></el-table-column>
              <el-table-column prop="name" label="name" min-width="150"></el-table-column>
              <el-table-column prop="value" label="value" min-width="120"></el-table-column>
              <el-table-column prop="created_at" label="created_at" min-width="150"></el-table-column>
            </el-table>
          </div>
        </div>
      </div>

      <!-- AI助手 -->
      <!-- <div class="section">
        <div class="ai-assistant-card card-hover">
          <div class="assistant-header">
            <div class="assistant-info">
              <div class="assistant-avatar">
                   <i class="fa fa-android"></i>
              </div>
              <div class="assistant-name-wrap">
                <span class="assistant-name">AI助手</span>
                <span class="assistant-status">在线</span>
              </div>
            </div>
          </div>
          
          <div class="chat-area" ref="chatPreviewArea">
            <div
              class="chat-message"
              :class="msg.role === 'assistant' ? 'ai-message' : 'user-message'"
              v-for="(msg, idx) in chatPreviewMessages"
              :key="'preview-' + idx"
            >
              <template v-if="msg.role === 'assistant'">
                <div class="message-avatar">
                  AI
                </div>
                <div class="message-content">
                  <template v-if="msg.loading && !msg.content">
                    <span class="message-loading">
                      <i class="el-icon-loading"></i>
                      <span class="message-loading-text">AI 正在思考中...</span>
                    </span>
                  </template>
                  <template v-else>{{ msg.content }}</template>
                </div>
              </template>

              <template v-else>
                <div class="message-content user-preview-content">
                  {{ msg.content }}
                </div>
                <div class="message-avatar user-avatar">
                  我
                </div>
              </template>
            </div>
          </div>
          
          <div class="chat-input-area">
            <el-input 
              v-model="chatInput" 
              placeholder="输入你的数据探索需求..." 
              class="chat-input"
              :suffix-icon="'el-icon-share'"
              @keyup.enter.native="sendMessage"
            ></el-input>
            <el-button
              type="primary"
              class="send-btn"
              :loading="previewSending"
              @click="sendMessage"
            >
              发送
            </el-button>
          </div>
          
          <div class="quick-suggestions">
            <span class="suggestion-label">快捷建议</span>
            <div class="suggestion-buttons">
              <el-button size="mini" class="suggestion-btn" @click="quickSuggestion('编写SQL查询')">编写SQL查询</el-button>
              <el-button size="mini" class="suggestion-btn" @click="quickSuggestion('分析数据统计')">分析数据统计</el-button>
              <el-button size="mini" class="suggestion-btn" @click="quickSuggestion('查找所有数据表')">查找所有数据表</el-button>
              <el-button size="mini" class="suggestion-btn" @click="quickSuggestion('优化查询性能')">优化查询性能</el-button>
            </div>
          </div>
        </div>
      </div> -->

      <!-- 数据库连接配置 -->
      <div class="section">
        <div class="config-card card-hover">
          <div class="config-title">数据库连接配置</div>
          <div class="config-form">
            <div class="form-row">
              <div class="form-col">
                <label class="form-label">主机</label>
                <el-input v-model="configForm.host" size="small" placeholder="localhost"></el-input>
              </div>
              <div class="form-col">
                <label class="form-label">端口</label>
                <el-input v-model="configForm.port" size="small" placeholder="3306"></el-input>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <label class="form-label">用户名</label>
                <el-input v-model="configForm.username" size="small" placeholder="root"></el-input>
              </div>
              <div class="form-col">
                <label class="form-label">密码</label>
                <el-input v-model="configForm.password" size="small" type="password" placeholder="********"></el-input>
              </div>
            </div>
            <div class="form-actions">
              <el-button type="primary" size="small" @click="handleSaveConfig">保存配置</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataExplorer',
  data() {
    return {
      apiBase: "/api",
      chatInput: "",
      previewSending: false,
      chatPreviewMessages: [
        {
          role: "assistant",
          content:
            '你好！我是数据探索 AI 助手，可以帮你编写 SQL、分析结果以及优化查询。试着问我：“帮我写一条查询最近 7 天订单的 SQL” 或 “解释一下当前结果的含义”。'
        }
      ],
      queryForm: {
        database: 'mysql',
        table: '',
        sql: 'SELECT * FROM table LIMIT 10;'
      },
      queryResults: [
        { id: 1, name: '测试数据1', value: '100', created_at: '2024-01-01' },
        { id: 2, name: '测试数据2', value: '200', created_at: '2024-01-02' }
      ],
      configForm: {
        host: 'localhost',
        port: '3306',
        username: 'root',
        password: ''
      }
    }
  },
  methods: {
    handleNewQuery() {
      this.$message.info('新增查询功能')
    },
    handleRunQuery() {
      this.$message.success('查询执行成功')
    },
    handleCloseQuery() {
      this.$message.info('关闭查询')
    },
    handleSaveConfig() {
      this.$message.success('配置保存成功')
    },
    async sendMessage() {
      const text = (this.chatInput || "").trim();
      if (!text || this.previewSending) return;

      this.chatPreviewMessages.push({ role: "user", content: text });
      this.chatInput = "";
      this.previewSending = true;
      this.$nextTick(() => this.scrollPreviewBottom());

      const history = this.chatPreviewMessages.slice(0, -1);
      const aiMsg = { role: "assistant", content: "", loading: true };
      this.chatPreviewMessages.push(aiMsg);
      this.$nextTick(() => this.scrollPreviewBottom());

      try {
        await this.streamChat(text, history, token => {
          aiMsg.content += token;
          this.$nextTick(() => this.scrollPreviewBottom());
        });

        if (!aiMsg.content.trim()) aiMsg.content = "（模型未返回内容）";
      } catch (e) {
        const msg = e && e.message ? e.message : e;
        aiMsg.content = `请求失败：${msg}`;
      } finally {
        aiMsg.loading = false;
        this.previewSending = false;
        this.$nextTick(() => this.scrollPreviewBottom());
      }
    },
    async streamChat(message, history, onToken) {
      const resp = await fetch(`${this.apiBase}/chat/stream`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          message,
          history
        })
      });

      if (!resp.ok || !resp.body) {
        const t = await resp.text().catch(() => "");
        throw new Error(`HTTP ${resp.status} ${t}`);
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder("utf-8");
      let buffer = "";
      let done = false;

      while (!done) {
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

          if (event === "token" && payload.token) {
            onToken(payload.token);
          } else if (event === "error") {
            throw new Error(payload.message || "流式响应错误");
          }
        }
      }
    },
    scrollPreviewBottom() {
      const el = this.$refs.chatPreviewArea;
      if (el && el.scrollHeight !== undefined) {
        el.scrollTop = el.scrollHeight;
      }
    },
    quickSuggestion(text) {
      this.chatInput = text
    }
  }
}
</script>

<style lang="scss" scoped>
.dataexplorer-container {
  background-color: #f9fafb;
  min-height: calc(100vh - 60px);
}

.dataexplorer-content {
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
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.section {
  margin-bottom: 24px;
}

.query-card,
.results-card,
.ai-assistant-card,
.config-card {
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 查询区域 */
.query-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.query-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.control-item {
  display: flex;
  align-items: center;
}

.close-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: #f3f4f6;
  color: #1f2937;
}

.sql-editor {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 12px;
}

.sql-textarea {
  ::v-deep textarea {
    font-family: 'Courier New', monospace;
    background-color: transparent;
    border: none;
    resize: vertical;
  }
}

/* 查询结果 */
.results-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
}

.table-wrapper {
  overflow-x: auto;
}

/* AI助手 */
.assistant-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.assistant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.assistant-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assistant-name {
  font-size: 15px;
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
}

.chat-message {
  display: flex;
  gap: 12px;
}

.user-message {
  justify-content: flex-end;
}

.chat-area .chat-message + .chat-message {
  margin-top: 12px;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
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
  gap: 4px;
  color: #6b7280;
  line-height: 1;
  padding: 0;
  font-size: 0;
}

.message-loading i.el-icon-loading {
  font-size: 13px;
  line-height: 1;
  font-style: normal;
  display: inline-block;
  margin: 0 !important;
  padding: 0 !important;
  white-space: nowrap;
}

.message-loading-text {
  font-size: 13px;
  line-height: 1;
  white-space: nowrap;
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
  white-space: nowrap;
}

.quick-suggestions {
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.suggestion-label {
  font-size: 12px;
  color: #6b7280;
  display: block;
  margin-bottom: 8px;
}

.suggestion-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.suggestion-btn {
  background-color: #f3f4f6 !important;
  color: #374151;
  border: none !important;
}

.suggestion-btn:hover {
  background-color: #e5e7eb;
  color: #1f2937;
}

/* 数据库配置 */
.config-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .query-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .query-controls {
    flex-wrap: wrap;
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
</style>
