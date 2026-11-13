<template>
  <div class="retrieve-page">
    <aside class="retrieve-sidebar">
      <button type="button" class="breadcrumb" @click="goToKnowledgeList"><i class="el-icon-arrow-left"></i> 知识库</button>
      <div class="base-summary"><span class="base-icon"><i class="el-icon-collection"></i></span><div><strong>{{ knowledgeName }}</strong><small>知识库</small></div></div>
      <nav aria-label="知识库导航">
        <button type="button" @click="goToDocuments"><i class="el-icon-document"></i> 文档</button>
        <button type="button" disabled><i class="el-icon-share"></i> 流水线</button>
        <button type="button" class="is-active"><i class="el-icon-aim"></i> 召回测试</button>
        <button type="button" @click="goToSettings"><i class="el-icon-setting"></i> 设置</button>
      </nav>
      <div class="sidebar-bottom"><span>访问 API <i class="el-icon-success"></i></span></div>
    </aside>

    <main class="retrieve-main">
      <section class="query-panel">
        <header><h1>召回测试</h1><p>根据给定的查询文本测试知识的召回效果。</p></header>
        <div class="query-box">
          <div class="query-box-header"><strong>源文本</strong><el-select v-model="searchMethod" size="small" class="method-select" aria-label="检索方式"><el-option label="向量检索" value="semantic_search" /><el-option label="倒排索引" value="full_text_search" /><el-option label="混合检索" value="hybrid_search" /></el-select></div>
          <textarea v-model.trim="query" maxlength="200" placeholder="请输入文本，建议使用简短的陈述句。" aria-label="查询文本" @keydown.ctrl.enter.prevent="runTest" />
          <span class="character-count">{{ query.length }}/200</span>
          <el-button type="primary" size="small" class="test-button" :loading="loading" :disabled="!query.trim()" @click="runTest"><i v-if="!loading" class="el-icon-video-play"></i> 测试</el-button>
        </div>

        <section class="history-section"><h2>记录</h2><div v-if="!records.length" class="history-empty"><span><i class="el-icon-time"></i></span><p>最近无查询结果</p></div><div v-else class="history-list"><button v-for="record in records" :key="record.id" type="button" :class="{ 'is-active': activeRecordId === record.id }" @click="showRecord(record)"><span>{{ record.query }}</span><small>{{ record.time }} · {{ record.results.length }} 条结果</small></button></div></section>
      </section>

      <section class="results-panel" aria-live="polite">
        <div v-if="loading" class="result-placeholder"><i class="el-icon-loading"></i><p>正在检索知识库...</p></div>
        <div v-else-if="!activeRecord" class="result-placeholder"><i :class="retrievalError ? 'el-icon-warning-outline' : 'el-icon-aim'"></i><p>{{ retrievalError || '召回测试结果将展示在这里' }}</p></div>
        <div v-else class="result-content"><div v-if="retrievalError" class="result-error">{{ retrievalError }}</div><div class="result-heading"><h2>召回结果</h2><span>{{ activeRecord.results.length }} 条</span></div><p class="query-caption">查询：{{ activeRecord.query }}</p><div v-if="!activeRecord.results.length" class="no-result">接口返回 0 条匹配分段。可检查检索方式及知识库索引状态。</div><article v-for="(item, index) in activeRecord.results" :key="item.id || index" class="result-card"><div class="result-card-head"><strong>{{ index + 1 }}. {{ resultTitle(item) }}</strong><span v-if="resultScore(item) !== null">相似度 {{ resultScore(item) }}</span></div><p>{{ resultText(item) }}</p><div v-if="resultKeywords(item).length" class="result-keywords"><span v-for="(word, wordIndex) in resultKeywords(item)" :key="wordIndex"># {{ word }}</span></div></article></div>
      </section>
    </main>
  </div>
</template>

<script>
import { getKnowledgeBase, retrieveKnowledgeBase } from '@/api/llmops/knowledge'
import { buildKnowledgeRetrievalModel, knowledgeRetrievalError } from '@/utils/knowledgeRetrieval'

const HISTORY_PREFIX = 'llmops_knowledge_retrieve_history_v1_'
const SEARCH_METHODS = ['semantic_search', 'full_text_search', 'hybrid_search']

export default {
  name: 'KnowledgeRetrieve',
  data() {
    return { query: '', searchMethod: 'full_text_search', retrievalModel: {}, loading: false, records: [], activeRecordId: null, retrievalError: '' }
  },
  computed: {
    knowledgeName() { return this.$route.query.name || '知识库' },
    activeRecord() { return this.records.find(record => record.id === this.activeRecordId) || null }
  },
  created() { this.loadHistory(); this.loadKnowledgeSettings() },
  methods: {
    goToKnowledgeList() { this.$router.push({ name: 'knowledge' }) },
    goToDocuments() { this.$router.push({ name: 'knowledge-detail', params: { id: this.$route.params.id }, query: this.$route.query }) },
    goToSettings() { this.$router.push({ name: 'knowledge-settings', params: { id: this.$route.params.id }, query: this.$route.query }) },
    historyKey() { return `${HISTORY_PREFIX}${this.$route.params.id}` },
    loadHistory() {
      try {
        const saved = JSON.parse(localStorage.getItem(this.historyKey()) || '[]')
        this.records = Array.isArray(saved) ? saved.filter(item => item && typeof item.query === 'string' && Array.isArray(item.results)).slice(0, 20) : []
        if (this.records.length) this.showRecord(this.records[0])
      } catch (e) { this.records = [] }
    },
    saveHistory() {
      try { localStorage.setItem(this.historyKey(), JSON.stringify(this.records)) }
      catch (e) { this.$message.warning('浏览器存储空间不足，查询记录仅保留在当前页面') }
    },
    async loadKnowledgeSettings() {
      try {
        const result = await getKnowledgeBase(this.$route.params.id)
        const base = result && result.data && typeof result.data === 'object' ? result.data : result
        const model = base && (base.retrieval_model_dict || base.retrieval_model || base.dataset && base.dataset.retrieval_model)
        if (model && typeof model === 'object') {
          this.retrievalModel = model
          if (!this.records.length && SEARCH_METHODS.includes(model.search_method)) this.searchMethod = model.search_method
        }
      } catch (e) { /* 知识库配置不可用时仍允许使用页面选定的检索方式 */ }
    },
    unwrap(result) {
      let data = result
      for (let depth = 0; depth < 3 && data && typeof data === 'object' && !Array.isArray(data); depth += 1) {
        if (Array.isArray(data.records) || Array.isArray(data.results) || Array.isArray(data.items) || Array.isArray(data.segments) || Array.isArray(data.hits) || Array.isArray(data.nodes) || Array.isArray(data.chunks)) break
        if (data.data && typeof data.data === 'object') data = data.data
        else if (data.result && typeof data.result === 'object') data = data.result
        else if (data.payload && typeof data.payload === 'object') data = data.payload
        else break
      }
      return data
    },
    resultList(result) {
      const data = this.unwrap(result)
      if (Array.isArray(data)) return data
      if (!data || typeof data !== 'object') return null
      const list = data.records || data.results || data.items || data.segments || data.hits || data.nodes || data.chunks || data.data
      return Array.isArray(list) ? list : null
    },
    async runTest() {
      const query = this.query.trim()
      if (!query || this.loading) return
      this.loading = true
      this.retrievalError = ''
      try {
        const retrievalModel = buildKnowledgeRetrievalModel(this.retrievalModel, this.searchMethod)
        const result = await retrieveKnowledgeBase(this.$route.params.id, { query, retrieval_model: retrievalModel })
        const results = this.resultList(result)
        if (!results) throw new Error('检索接口已响应，但结果格式无法识别，请检查接口返回数据')
        const record = { id: Date.now(), query, searchMethod: this.searchMethod, results, time: new Date().toLocaleString('zh-CN', { hour12: false }) }
        this.records.unshift(record)
        this.records = this.records.slice(0, 20)
        this.activeRecordId = record.id
        this.saveHistory()
      } catch (e) {
        this.retrievalError = knowledgeRetrievalError(e)
        this.$message.error(this.retrievalError)
      }
      finally { this.loading = false }
    },
    showRecord(record) { this.activeRecordId = record.id; this.query = record.query; if (SEARCH_METHODS.includes(record.searchMethod)) this.searchMethod = record.searchMethod; this.retrievalError = '' },
    resultSegment(item) { return item.segment || item.document_segment || item },
    resultTitle(item) {
      const segment = this.resultSegment(item)
      return item.document && item.document.name || segment.document_name || segment.name || `分段 ${segment.position || segment.index || ''}`
    },
    resultText(item) { const segment = this.resultSegment(item); return segment.content || segment.text || '暂无文本内容' },
    resultKeywords(item) { const words = this.resultSegment(item).keywords; return Array.isArray(words) ? words : [] },
    resultScore(item) { const value = item.score !== undefined ? item.score : this.resultSegment(item).score; return value === undefined || value === null ? null : `${(Number(value) * 100).toFixed(1)}%` }
  }
}
</script>

<style scoped>
.retrieve-page { display: flex; min-height: calc(100vh - 100px); background: #fff; color: #17233d; box-sizing: border-box; }
.retrieve-sidebar { display: flex; flex: 0 0 250px; flex-direction: column; padding: 20px 16px; border-right: 1px solid #edf0f4; box-sizing: border-box; }.breadcrumb, .retrieve-sidebar nav button { background: none; border: 0; cursor: pointer; font: inherit; }.breadcrumb { align-self: flex-start; color: #344054; font-weight: 600; }.breadcrumb i { margin-right: 8px; }.base-summary { display: flex; align-items: center; gap: 10px; margin: 36px 0 26px; }.base-icon { display: grid; place-items: center; width: 42px; height: 42px; color: #1649f5; background: #edf2ff; border-radius: 10px; font-size: 21px; }.base-summary strong, .base-summary small { display: block; }.base-summary small { margin-top: 4px; color: #98a2b3; font-size: 11px; }.retrieve-sidebar nav { display: grid; gap: 5px; }.retrieve-sidebar nav button { padding: 11px 13px; color: #526079; border-radius: 8px; text-align: left; }.retrieve-sidebar nav button i { margin-right: 11px; font-size: 16px; }.retrieve-sidebar nav .is-active { color: #1649f5; background: #edf2ff; font-weight: 600; }.retrieve-sidebar nav button:disabled { cursor: default; opacity: .6; }.sidebar-bottom { margin-top: auto; padding: 10px; color: #667085; border: 1px solid #e9edf3; border-radius: 8px; font-size: 12px; }.sidebar-bottom i { float: right; color: #28bd80; }
.retrieve-main { display: grid; flex: 1; grid-template-columns: minmax(400px, 1fr) minmax(400px, 1fr); min-width: 0; }.query-panel { padding: 20px 28px; min-width: 0; }.query-panel header h1 { margin: 0 0 5px; color: #101828; font-size: 20px; }.query-panel header p { margin: 0 0 18px; color: #667085; font-size: 14px; }
.query-box { position: relative; height: 360px; border: 2px solid #1649f5; border-radius: 13px; overflow: hidden; box-sizing: border-box; }.query-box-header { display: flex; align-items: center; justify-content: space-between; height: 44px; padding: 0 10px 0 15px; background: #f3f5f8; }.query-box-header strong { font-size: 13px; }.method-select { width: 140px; }.query-box textarea { display: block; width: 100%; height: calc(100% - 44px); padding: 17px 18px 58px; color: #344054; background: #fff; border: 0; resize: none; outline: none; box-sizing: border-box; font: inherit; line-height: 1.6; }.query-box textarea::placeholder { color: #98a2b3; }.character-count { position: absolute; top: 49px; right: 12px; color: #98a2b3; font-size: 11px; }.test-button { position: absolute; right: 18px; bottom: 10px; min-width: 100px; background: #1649f5; border-color: #1649f5; border-radius: 8px; }.test-button.is-disabled { background: #dce4ff; border-color: #dce4ff; }
.history-section h2 { margin: 28px 0 16px; font-size: 17px; }.history-empty { padding: 20px 22px; background: linear-gradient(105deg, #f4f5f7, #fafbfc); border-radius: 15px; color: #667085; }.history-empty span { display: grid; place-items: center; width: 44px; height: 44px; background: #fff; border-radius: 12px; font-size: 20px; }.history-empty p { margin: 12px 0 8px; font-size: 13px; }.history-list { display: grid; gap: 8px; }.history-list button { display: flex; flex-direction: column; gap: 7px; padding: 14px 16px; background: #f8f9fb; border: 1px solid #edf0f4; border-radius: 9px; color: #344054; cursor: pointer; text-align: left; }.history-list button.is-active { border-color: #b7c6ff; background: #f4f6ff; }.history-list button span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%; }.history-list button small { color: #98a2b3; }
.results-panel { min-height: 700px; background: #f4f6f9; border-radius: 18px 0 0 0; }.result-placeholder { display: flex; align-items: center; justify-content: center; flex-direction: column; min-height: 700px; color: #aeb6c5; }.result-placeholder i { font-size: 57px; }.result-placeholder p { margin-top: 18px; font-size: 13px; }.result-content { padding: 25px 28px; }.result-heading { display: flex; align-items: center; justify-content: space-between; }.result-heading h2 { margin: 0; font-size: 17px; }.result-heading span, .query-caption { color: #667085; font-size: 13px; }.query-caption { margin: 12px 0 20px; }.no-result { padding: 90px 0; text-align: center; color: #98a2b3; }.result-card { padding: 18px; margin-bottom: 12px; background: #fff; border: 1px solid #e9edf3; border-radius: 12px; }.result-card-head { display: flex; justify-content: space-between; gap: 12px; font-size: 13px; }.result-card-head span { color: #1649f5; white-space: nowrap; }.result-card p { margin: 12px 0; color: #344054; line-height: 1.7; white-space: pre-wrap; overflow-wrap: anywhere; }.result-keywords { display: flex; flex-wrap: wrap; gap: 8px; color: #98a2b3; font-size: 12px; }
.result-error { margin-bottom: 18px; padding: 10px 12px; color: #b54708; background: #fffaeb; border: 1px solid #fedf89; border-radius: 8px; font-size: 12px; }
@media (max-width: 1100px) { .retrieve-sidebar { flex-basis: 190px; }.retrieve-main { grid-template-columns: 1fr; }.results-panel { min-height: 350px; border-radius: 0; }.result-placeholder { min-height: 350px; } }
@media (max-width: 650px) { .retrieve-page { flex-wrap: wrap; }.retrieve-sidebar { flex: 1 0 100%; padding: 12px 16px; border-right: 0; border-bottom: 1px solid #edf0f4; }.base-summary, .retrieve-sidebar nav, .sidebar-bottom { display: none !important; }.retrieve-main { flex: 1 0 100%; }.query-panel { padding: 18px 16px; } }
</style>
