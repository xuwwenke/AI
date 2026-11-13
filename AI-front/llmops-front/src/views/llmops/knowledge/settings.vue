<template>
  <div class="knowledge-settings-page">
    <aside class="settings-sidebar">
      <button type="button" class="back-link" @click="navigate('knowledge')"><i class="el-icon-arrow-left"></i> 知识库</button>
      <div class="base-summary"><span class="base-icon"><i class="el-icon-collection"></i></span><div><strong :title="knowledgeName">{{ knowledgeName }}</strong><small>知识库</small></div></div>
      <nav aria-label="知识库导航">
        <button type="button" @click="navigate('knowledge-detail')"><i class="el-icon-document"></i> 文档</button>
        <button type="button" disabled><i class="el-icon-share"></i> 流水线</button>
        <button type="button" @click="navigate('knowledge-retrieve')"><i class="el-icon-aim"></i> 召回测试</button>
        <button type="button" class="is-active" aria-current="page"><i class="el-icon-setting"></i> 设置</button>
      </nav>
    </aside>

    <main v-loading="loading" class="settings-main">
      <header class="page-header"><h1>设置</h1><p>管理知识库的基础信息、索引方式和检索设置。</p></header>
      <div v-if="loadError" class="load-error"><i class="el-icon-warning-outline"></i><p>{{ loadError }}</p><el-button size="small" @click="loadSettings">重新加载</el-button></div>
      <el-form v-else-if="loaded" ref="settingsForm" :model="form" :rules="rules" label-position="top" class="settings-form" :disabled="saving" @submit.native.prevent="saveSettings">
        <el-alert class="capability-notice" title="配置保存后将同步至知识库服务" description="修改索引方式或 Embedding 模型可能触发文档重新索引。分段模式、关键词数量及摘要配置暂不支持在此修改。" type="info" :closable="false" show-icon />
        <section class="form-section">
          <h2>基础信息</h2>
          <el-form-item label="知识库名称" prop="name">
            <div class="name-field"><span class="name-icon"><i class="el-icon-collection"></i></span><el-input v-model="form.name" maxlength="40" show-word-limit placeholder="请输入知识库名称" /></div>
          </el-form-item>
          <el-form-item label="知识库描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" maxlength="400" show-word-limit placeholder="描述知识库的内容与用途" resize="vertical" /></el-form-item>
          <el-form-item label="标签">
            <div class="knowledge-tags-field"><el-tag v-for="tag in knowledgeTags" :key="tag.id" size="small" type="info">{{ tag.name }}</el-tag><span v-if="tagsLoading" class="tags-state"><i class="el-icon-loading"></i> 加载中</span><span v-else-if="tagsError" class="field-error">{{ tagsError }}</span><span v-else-if="!knowledgeTags.length" class="tags-state">暂无标签</span><el-button size="small" icon="el-icon-price-tag" @click="tagManagerVisible = true">管理标签</el-button></div>
            <p class="field-hint">标签通过独立接口即时保存，不受下方“保存设置”按钮影响。</p>
          </el-form-item>
          <el-form-item label="权限">
            <el-select v-model="form.permission" class="field-select" aria-label="知识库权限"><el-option label="只有我" value="only_me" /><el-option label="所有团队成员" value="all_team_members" /><el-option label="指定成员" value="partial_members" /></el-select>
            <p class="field-hint">控制哪些 Dify 工作区成员可以访问此知识库。</p>
          </el-form-item>
          <el-form-item v-if="form.permission === 'partial_members'" label="指定成员 user_id">
            <el-select v-model="form.partialMemberIds" multiple filterable allow-create default-first-option class="field-select" placeholder="输入成员 user_id 后按回车添加" aria-label="指定成员 user_id"><el-option v-for="id in form.partialMemberIds" :key="id" :value="id" :label="id" /></el-select>
            <p class="field-hint">当前接口未提供成员列表，请填写 Dify 工作区成员的 user_id；至少保留一位成员。</p>
          </el-form-item>
        </section>

        <section class="form-section">
          <h2>分段设置</h2><p class="section-hint">分段模式由上传文档时的处理方式决定，此处展示当前知识库配置。</p>
          <div class="segment-options">
            <div v-for="mode in segmentModes" :key="mode.value" class="segment-card" :class="{ 'is-selected': form.docForm === mode.value }">
              <i :class="mode.icon"></i><strong>{{ mode.label }}</strong><p>{{ mode.description }}</p><i v-if="form.docForm === mode.value" class="el-icon-success selected-mark"></i>
            </div>
          </div>
          <p v-if="!form.docForm" class="field-hint">暂无分段配置，请在添加文档时选择。</p>
        </section>

        <section class="form-section">
          <h2>索引方式</h2><p class="section-hint">高质量索引保存生效后，不支持切换回经济模式。</p>
          <div class="index-options" role="group" aria-label="索引方式">
            <button type="button" class="choice-card" :class="{ 'is-selected': isHighQuality }" :aria-pressed="isHighQuality" :disabled="saving" @click="setIndexingTechnique('high_quality')">
              <span class="choice-heading"><i class="el-icon-magic-stick"></i><strong>高质量</strong><em>推荐</em><span class="radio-mark"></span></span>
              <span class="choice-description">使用 Embedding 模型将文档转为向量，提供更精准的语义检索。</span>
            </button>
            <button type="button" class="choice-card" :class="{ 'is-selected': !isHighQuality }" :aria-pressed="!isHighQuality" :disabled="saving || isHighQualityLocked" :title="isHighQualityLocked ? '知识库已启用高质量索引，不支持切换回经济模式' : ''" @click="setIndexingTechnique('economy')">
              <span class="choice-heading"><i class="el-icon-coin"></i><strong>经济</strong><span class="radio-mark"></span></span>
              <span class="choice-description">使用关键词索引进行检索，无需调用 Embedding 模型。</span>
            </button>
          </div>
          <p v-if="isHighQualityLocked" class="field-hint"><i class="el-icon-lock"></i> 已启用高质量索引，经济模式不可选。文档索引失败时需处理失败原因，不会解除此限制。</p>
          <div v-if="isHighQuality" class="index-configuration">
            <el-form-item label="Embedding 模型" prop="embeddingKey">
              <div class="model-field">
                <el-select v-model="form.embeddingKey" filterable :loading="embeddingLoading" :disabled="embeddingLoading || !!embeddingError" placeholder="请选择 Embedding 模型" class="field-select" aria-label="Embedding 模型">
                  <el-option v-for="model in embeddingOptions" :key="model.key" :label="model.label + ' · ' + model.providerLabel" :value="model.key" :disabled="model.unavailable"><span>{{ model.label }}</span><small class="model-provider">{{ model.providerLabel }}{{ model.unavailable ? '（当前模型不可用）' : '' }}</small></el-option>
                </el-select>
                <el-button type="text" :loading="embeddingLoading" :disabled="saving" @click="loadModels('text-embedding')">刷新</el-button>
              </div>
              <p v-if="embeddingError" class="field-error">{{ embeddingError }}</p>
              <p v-else-if="!embeddingLoading && !embeddingOptions.some(item => !item.unavailable)" class="field-hint">暂无可用的 Embedding 模型，请先在模型服务中配置。</p>
              <p v-else class="field-hint">模型列表来自当前项目的模型服务，保存后将同步 Embedding 模型配置。</p>
            </el-form-item>
          </div>
          <div v-else class="index-configuration">
            <div class="control-label">关键词数量 <span>{{ form.keywordNumber }}</span></div>
            <el-slider v-model="form.keywordNumber" :min="1" :max="50" show-input disabled />
            <p class="field-hint">当前更新接口不支持修改关键词数量；未返回配置时显示参考值 10。</p>
          </div>
        </section>

        <section class="form-section">
          <div class="section-heading"><h2>摘要自动生成</h2><el-switch :value="summaryEnabled" disabled /></div>
          <p class="section-hint">为文档分段生成摘要，提升检索效果。当前接口暂不支持在此修改摘要配置。</p>
        </section>

        <section class="form-section retrieval-section">
          <div class="retrieval-section-label">
            <h2>检索设置</h2>
            <el-popover placement="bottom-start" width="300" trigger="click">
              <div class="retrieval-help"><strong>如何选择检索方式？</strong><p>向量检索：根据语义相似度查找相关分段。</p><p>全文检索：根据查询关键词匹配文档。</p><p>混合检索：结合两种检索结果，按权重或 Rerank 模型排序。</p><p>经济模式仅支持关键词检索。</p></div>
              <button slot="reference" type="button" class="retrieval-help-button">了解更多关于检索方法。</button>
            </el-popover>
          </div>
          <div class="retrieval-options" role="group" aria-label="检索方式">
            <div v-for="method in visibleRetrievalMethods" :key="method.value" class="retrieval-card" :class="{ 'is-selected': !isHighQuality || form.searchMethod === method.value }">
              <button :id="'retrieval-heading-' + method.value" type="button" class="retrieval-heading" :aria-expanded="!isHighQuality || form.searchMethod === method.value" :aria-controls="'retrieval-body-' + method.value" :disabled="saving" @click="setSearchMethod(method.value)">
                <i :class="method.icon"></i><span><strong>{{ method.label }}</strong><em v-if="method.value === 'hybrid_search'">推荐</em><small>{{ method.description }}</small></span>
              </button>
              <div v-if="!isHighQuality || form.searchMethod === method.value" :id="'retrieval-body-' + method.value" class="retrieval-body" role="region" :aria-labelledby="'retrieval-heading-' + method.value">
                <template v-if="isHighQuality">
                  <div class="rerank-control"><el-switch :id="'rerank-' + method.value" v-model="form.rerankingEnable" :disabled="saving" /><label :for="'rerank-' + method.value">Rerank 模型</label><el-tooltip content="使用 Rerank 模型对召回结果二次排序，启用后需选择可用模型。" placement="top"><i class="el-icon-question help-icon" tabindex="0" aria-label="Rerank 模型说明"></i></el-tooltip></div>
                  <div v-if="form.rerankingEnable" class="rerank-model-field">
                    <div class="model-field"><el-select v-model="form.rerankKey" size="small" filterable :loading="rerankLoading" :disabled="saving || rerankLoading || !!rerankError" placeholder="请选择 Rerank 模型" class="field-select" aria-label="Rerank 模型"><el-option v-for="model in rerankOptions" :key="model.key" :value="model.key" :label="model.label + ' · ' + model.providerLabel" :disabled="model.unavailable" /></el-select><el-button type="text" size="small" :loading="rerankLoading" :disabled="saving" @click="loadModels('rerank')">刷新</el-button></div>
                    <p v-if="rerankError" class="field-error">{{ rerankError }}</p><p v-else-if="!rerankLoading && !rerankOptions.some(item => !item.unavailable)" class="field-hint">暂无可用的 Rerank 模型，请配置后刷新，或关闭此选项。</p>
                  </div>
                  <div v-if="method.value === 'hybrid_search' && !form.rerankingEnable" class="retrieval-weights">
                    <div class="control-label">权重设置 <span>语义 {{ form.vectorWeight.toFixed(2) }} / 关键词 {{ (1 - form.vectorWeight).toFixed(2) }}</span></div><el-slider v-model="form.vectorWeight" :min="0" :max="1" :step="0.01" :disabled="saving" /><p class="field-hint">未启用 Rerank 模型时，按向量与关键词权重融合检索结果。</p>
                  </div>
                </template>
                <div class="retrieval-controls">
                  <div class="retrieval-parameter"><div class="control-label"><label :for="'retrieval-top-k-' + method.value">Top K</label><el-tooltip content="返回的文档分段数量，范围为 1–100。" placement="top"><i class="el-icon-question help-icon" tabindex="0" aria-label="Top K 说明"></i></el-tooltip></div><div class="parameter-input"><el-input-number :id="'retrieval-top-k-' + method.value" v-model="form.topK" size="small" :min="1" :max="100" controls-position="right" :disabled="saving" /><el-slider v-model="form.topK" :min="1" :max="100" :disabled="saving" /></div></div>
                  <div v-if="isHighQuality" class="retrieval-parameter"><div class="control-label"><el-switch :id="'retrieval-score-enabled-' + method.value" v-model="form.scoreThresholdEnabled" :disabled="saving" /><label :for="'retrieval-score-enabled-' + method.value">Score 阈值</label><el-tooltip content="只返回相关性得分不低于阈值的分段，范围为 0–1。" placement="top"><i class="el-icon-question help-icon" tabindex="0" aria-label="Score 阈值说明"></i></el-tooltip></div><div class="parameter-input"><el-input-number v-model="form.scoreThreshold" size="small" :min="0" :max="1" :step="0.01" :precision="2" controls-position="right" :disabled="saving || !form.scoreThresholdEnabled" aria-label="Score 阈值" /><el-slider v-model="form.scoreThreshold" :min="0" :max="1" :step="0.01" :disabled="saving || !form.scoreThresholdEnabled" /></div></div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <footer class="save-bar"><el-button type="primary" native-type="submit" :loading="saving">保存设置</el-button><el-button :disabled="saving" @click="resetForm">重置</el-button><span>仅提交已修改的配置</span></footer>
      </el-form>
    </main>
    <knowledge-tags-manager :visible.sync="tagManagerVisible" :knowledge-base-id="$route.params.id" :knowledge-name="knowledgeName" @change="loadTags" />
  </div>
</template>

<script>
import { getKnowledgeBase, getKnowledgeModels, updateKnowledgeBase, getKnowledgeBaseTags } from '@/api/llmops/knowledge'
import { parseKnowledgeTags } from '@/utils/knowledgeTags'
import { normalizeKnowledgeSettings, buildKnowledgeSettingsPayload, verifyKnowledgeSettings } from '@/utils/knowledgeSettings'
import KnowledgeTagsManager from './tags-manager.vue'

const unwrap = result => result && result.data !== undefined ? result.data : result
const modelKey = (provider, model) => provider && model ? JSON.stringify([provider, model]) : ''
const labelText = label => typeof label === 'string' ? label : label && (label.zh_Hans || label.en_US) || ''

export default {
  name: 'KnowledgeSettings',
  components: { KnowledgeTagsManager },
  data() {
    return {
      loading: false, loaded: false, saving: false, loadError: '', base: {}, initialForm: {}, settingsRequestVersion: 0,
      knowledgeTags: [], tagsLoading: false, tagsError: '', tagManagerVisible: false, tagsRequestVersion: 0,
      embeddingModels: [], rerankModels: [], embeddingLoading: false, rerankLoading: false, embeddingError: '', rerankError: '',
      form: normalizeKnowledgeSettings({}),
      rules: { name: [{ required: true, whitespace: true, message: '请输入知识库名称', trigger: 'blur' }, { max: 40, message: '知识库名称不能超过 40 个字符', trigger: 'blur' }], description: [{ max: 400, message: '描述不能超过 400 个字符', trigger: 'blur' }] },
      segmentModes: [
        { value: 'text_model', label: '通用', icon: 'el-icon-document', description: '将文档拆分为独立的文本分段。' },
        { value: 'hierarchical_model', label: '父子', icon: 'el-icon-connection', description: '使用子分段检索，返回父分段上下文。' },
        { value: 'qa_model', label: 'Q&A', icon: 'el-icon-chat-dot-round', description: '以问题与答案的形式组织分段。' }
      ],
      retrievalMethods: [
        { value: 'semantic_search', label: '向量检索', icon: 'el-icon-menu', description: '通过生成查询嵌入并查询与其向量表示最相似的文本分段。' },
        { value: 'full_text_search', label: '全文检索', icon: 'el-icon-document', description: '索引文档中的所有词汇，查询关键词并返回包含这些词汇的文本分段。' },
        { value: 'hybrid_search', label: '混合检索', icon: 'el-icon-s-grid', description: '同时执行全文检索和向量检索，从两类查询结果中选取最佳结果，可设置权重或配置重排序模型。' }
      ]
    }
  },
  computed: {
    knowledgeName() { return this.base.name || this.$route.query.name || '知识库' },
    isHighQuality() { return this.form.indexingTechnique === 'high_quality' },
    isHighQualityLocked() { return this.base.indexing_technique === 'high_quality' },
    visibleRetrievalMethods() { return this.isHighQuality ? this.retrievalMethods : [{ value: 'keyword_search', label: '关键词检索', icon: 'el-icon-search', description: '经济模式根据关键词索引匹配文档分段。' }] },
    summaryEnabled() { return !!(this.base.summary_index_setting && this.base.summary_index_setting.enable) },
    embeddingOptions() { return this.withCurrentModel(this.embeddingModels, this.form.embeddingKey) },
    rerankOptions() { return this.withCurrentModel(this.rerankModels, this.form.rerankKey) }
  },
  created() { this.loadSettings(); this.loadModels('text-embedding'); this.loadModels('rerank') },
  activated() { if (this.loaded && this.$route.name === 'knowledge-settings') this.loadTags() },
  watch: { '$route.params.id'() { ++this.settingsRequestVersion; if (this.$route.name === 'knowledge-settings') this.loadSettings() } },
  methods: {
    setSearchMethod(value) {
      if (this.saving || !this.isHighQuality || !this.retrievalMethods.some(method => method.value === value)) return
      this.form.searchMethod = value
    },
    navigate(name) { this.$router.push(name === 'knowledge' ? { name } : { name, params: { id: this.$route.params.id }, query: { ...this.$route.query, name: this.knowledgeName } }) },
    withCurrentModel(models, key) {
      if (!key || models.some(model => model.key === key)) return models
      try {
        const [provider, model] = JSON.parse(key)
        return [{ key, provider, model, label: model, providerLabel: provider, unavailable: true }, ...models]
      } catch (e) { return models }
    },
    normalizeModels(result, type) {
      const payload = unwrap(result)
      // The Dify-backed API returns data as a provider array; accept the legacy wrapper as well.
      const providers = Array.isArray(payload) ? payload : payload && payload.providers
      if (!Array.isArray(providers)) throw new Error('模型列表响应格式异常')
      const models = []
      providers.forEach(provider => {
        if (!provider || !provider.provider || provider.status && provider.status !== 'active') return
        const providerLabel = labelText(provider.label) || provider.provider
        const providerModels = Array.isArray(provider.models) ? provider.models : []
        providerModels.forEach(model => {
          if (!model || !model.model || model.model_type && model.model_type !== type || model.status && model.status !== 'active') return
          const key = modelKey(provider.provider, model.model)
          if (!models.some(item => item.key === key)) models.push({ key, provider: provider.provider, model: model.model, label: labelText(model.label) || model.model, providerLabel })
        })
      })
      return models
    },
    async loadModels(type) {
      const prefix = type === 'text-embedding' ? 'embedding' : 'rerank'
      if (this[`${prefix}Loading`]) return
      this[`${prefix}Loading`] = true
      this[`${prefix}Error`] = ''
      try { this[`${prefix}Models`] = this.normalizeModels(await getKnowledgeModels(type), type) }
      catch (e) { this[`${prefix}Models`] = []; this[`${prefix}Error`] = e.message || '模型列表加载失败，请点击刷新重试' }
      finally { this[`${prefix}Loading`] = false }
    },
    async loadSettings() {
      const id = this.$route.params.id
      const version = ++this.settingsRequestVersion
      this.loading = true
      this.loaded = false
      this.loadError = ''
      try {
        const base = unwrap(await getKnowledgeBase(id))
        if (id !== this.$route.params.id || version !== this.settingsRequestVersion) return
        if (!base || !base.id || typeof base.name !== 'string') throw new Error('知识库配置响应格式异常')
        this.base = base
        this.resetForm()
        this.loaded = true
        this.loadTags()
      } catch (e) { if (id === this.$route.params.id && version === this.settingsRequestVersion) this.loadError = e.message || '知识库配置加载失败' }
      finally { if (version === this.settingsRequestVersion) this.loading = false }
    },
    async loadTags() {
      const id = this.$route.params.id
      const version = ++this.tagsRequestVersion
      this.knowledgeTags = []
      this.tagsLoading = true
      this.tagsError = ''
      try {
        const tags = parseKnowledgeTags(await getKnowledgeBaseTags(id))
        if (id === this.$route.params.id && version === this.tagsRequestVersion) this.knowledgeTags = tags
      } catch (e) { if (id === this.$route.params.id && version === this.tagsRequestVersion) this.tagsError = e.message || '标签加载失败' }
      finally { if (version === this.tagsRequestVersion) this.tagsLoading = false }
    },
    resetForm() {
      this.form = normalizeKnowledgeSettings(this.base)
      this.initialForm = JSON.parse(JSON.stringify(this.form))
      this.$nextTick(() => { if (this.$refs.settingsForm) this.$refs.settingsForm.clearValidate() })
    },
    setIndexingTechnique(value) {
      if (value === 'economy' && this.isHighQualityLocked) {
        this.$message.warning('知识库已启用高质量索引，不支持切换回经济模式')
        return
      }
      this.form.indexingTechnique = value
    },
    async saveSettings() {
      if (this.saving || !this.loaded || !this.$refs.settingsForm) return
      const id = this.$route.params.id
      const version = this.settingsRequestVersion
      this.saving = true
      let submitted = false
      try {
        const valid = await new Promise(resolve => this.$refs.settingsForm.validate(resolve))
        if (!valid || id !== this.$route.params.id || version !== this.settingsRequestVersion) return
        const payload = buildKnowledgeSettingsPayload(this.form, this.initialForm, this.base, this.embeddingModels, this.rerankModels)
        if (!Object.keys(payload).length) { this.$message.info('设置未修改'); return }
        await updateKnowledgeBase(id, payload)
        submitted = true
        if (id !== this.$route.params.id || version !== this.settingsRequestVersion) return
        const saved = unwrap(await getKnowledgeBase(id))
        if (id !== this.$route.params.id || version !== this.settingsRequestVersion) return
        if (!saved || saved.id !== id) throw new Error('无法读取保存后的知识库配置，请重新加载确认')
        const mismatches = verifyKnowledgeSettings(saved, payload)
        this.base = saved
        this.resetForm()
        if (mismatches.length) this.$message.warning(`请求已提交，但无法确认${mismatches.join('、')}已生效，已展示服务端当前配置`)
        else this.$message.success(payload.indexing_technique || payload.embedding_model ? '知识库设置已保存；文档索引重建结果请在文档页查看' : '知识库设置已保存')
      } catch (e) {
        if (id !== this.$route.params.id || version !== this.settingsRequestVersion) return
        const data = e.response && e.response.data
        const message = data && (data.message || data.detail) || e.message || '保存失败，请稍后重试'
        if (submitted) this.$message.warning(`保存请求已提交，但配置回读失败：${message}。请重新加载确认`)
        else this.$message.error(typeof message === 'string' ? message : '保存失败，请检查接口返回的字段错误')
      }
      finally { this.saving = false }
    }
  }
}
</script>

<style scoped>
.knowledge-settings-page { display: flex; min-height: calc(100vh - 100px); color: #17233d; background: #fff; }
.settings-sidebar { flex: 0 0 250px; padding: 20px 16px; border-right: 1px solid #edf0f4; box-sizing: border-box; }
.back-link, .settings-sidebar nav button { background: none; border: 0; font: inherit; cursor: pointer; text-align: left; }
.back-link { color: #344054; font-weight: 600; }.back-link i { margin-right: 8px; }
.base-summary { display: flex; align-items: center; gap: 10px; margin: 36px 0 26px; }.base-summary > div { min-width: 0; }
.base-icon, .name-icon { display: grid; flex-shrink: 0; place-items: center; width: 42px; height: 42px; color: #1649f5; background: #edf2ff; border: 1px solid #e4eaff; border-radius: 10px; font-size: 21px; }
.base-summary strong { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.base-summary small { display: block; margin-top: 4px; color: #98a2b3; font-size: 11px; }
.settings-sidebar nav { display: grid; gap: 5px; }.settings-sidebar nav button { padding: 11px 13px; color: #526079; border-radius: 8px; }.settings-sidebar nav button i { margin-right: 11px; font-size: 16px; }.settings-sidebar nav .is-active { color: #1649f5; background: #edf2ff; font-weight: 600; }.settings-sidebar nav button:disabled { opacity: .6; cursor: default; }
.settings-main { flex: 1; min-width: 0; padding: 25px 40px 40px; box-sizing: border-box; }.page-header h1 { margin: 0 0 8px; font-size: 20px; color: #101828; }.page-header p { margin: 0; color: #667085; font-size: 13px; }
.settings-form { max-width: 850px; margin-top: 26px; }.capability-notice { margin-bottom: 24px; }.form-section { padding-bottom: 25px; margin-bottom: 25px; border-bottom: 1px solid #edf0f4; }.form-section h2 { margin: 0 0 18px; color: #101828; font-size: 15px; font-weight: 600; }
.settings-form ::v-deep .el-form-item { margin-bottom: 20px; }.settings-form ::v-deep .el-form-item__label { padding: 0 0 7px; color: #344054; font-size: 13px; line-height: 22px; }.settings-form ::v-deep .el-input__inner, .settings-form ::v-deep .el-textarea__inner { border-radius: 7px; }.settings-form ::v-deep .el-textarea__inner { padding: 10px 12px 20px; line-height: 1.7; }.settings-form ::v-deep .el-input__count { color: #98a2b3; }
.name-field, .model-field { display: flex; align-items: center; gap: 12px; }.name-icon { width: 40px; height: 40px; }.field-select { width: 100%; }.model-field .field-select { flex: 1; min-width: 0; }.model-provider { margin-left: 16px; color: #98a2b3; }
.knowledge-tags-field { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }.knowledge-tags-field .el-tag { max-width: 100%; overflow: hidden; text-overflow: ellipsis; }.tags-state { color: #98a2b3; font-size: 12px; }
.field-hint, .section-hint { margin: 8px 0 0; color: #98a2b3; font-size: 12px; line-height: 1.6; }.section-hint { margin: -8px 0 16px; color: #667085; }.field-error { margin: 8px 0 0; color: #f56c6c; font-size: 12px; line-height: 1.6; }
.segment-options { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }.segment-card { position: relative; padding: 16px; background: #fafbfc; border: 1px solid #e4e7ed; border-radius: 9px; }.segment-card > i:first-child { margin-right: 8px; color: #667085; font-size: 17px; }.segment-card strong { font-size: 13px; }.segment-card p { margin: 10px 0 0; color: #98a2b3; font-size: 12px; line-height: 1.6; }.segment-card .selected-mark { position: absolute; top: 16px; right: 12px; color: #1649f5; }
.index-options { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }.choice-card { padding: 17px; color: #344054; background: #fff; border: 1px solid #dcdfe6; border-radius: 9px; text-align: left; font: inherit; cursor: pointer; }.choice-heading { display: flex; align-items: center; gap: 8px; }.choice-heading > i { font-size: 18px; }.choice-heading strong { font-size: 14px; }.choice-heading em { padding: 2px 5px; color: #1649f5; background: #e7edff; border-radius: 4px; font-size: 10px; font-style: normal; }.choice-description { display: block; margin-top: 10px; color: #667085; font-size: 12px; line-height: 1.7; }
.radio-mark { display: block; flex-shrink: 0; width: 14px; height: 14px; margin-left: auto; background: #fff; border: 1px solid #d0d5dd; border-radius: 50%; box-sizing: border-box; }.is-selected { background: #f5f8ff; border-color: #1649f5; }.is-selected .radio-mark { border: 4px solid #1649f5; }.index-configuration { margin-top: 20px; padding: 18px 20px; background: #f8fafc; border: 1px solid #edf0f4; border-radius: 8px; }.index-configuration ::v-deep .el-form-item { margin-bottom: 0; }
.section-heading { display: flex; justify-content: space-between; align-items: center; gap: 15px; margin-bottom: 14px; }.section-heading h2 { margin: 0; }.section-heading strong { font-size: 13px; }
.retrieval-section { display: grid; grid-template-columns: 160px minmax(0, 1fr); gap: 22px; align-items: start; }
.retrieval-section-label h2 { margin: 3px 0 5px; font-size: 14px; }.retrieval-help-button { padding: 0; border: 0; color: #1649f5; background: none; font: inherit; font-size: 12px; line-height: 1.7; text-align: left; cursor: pointer; }.retrieval-help { color: #667085; font-size: 12px; line-height: 1.7; }.retrieval-help strong { color: #344054; }.retrieval-help p { margin: 8px 0 0; }
.retrieval-options { display: grid; min-width: 0; gap: 10px; }.retrieval-card { overflow: hidden; min-width: 0; color: #344054; background: #fafbfe; border: 1px solid #e4e8ef; border-radius: 12px; }.retrieval-card.is-selected { background: #fff; border-color: #1649f5; box-shadow: 0 0 0 1px #1649f5; }
.retrieval-heading { position: relative; display: flex; align-items: flex-start; gap: 12px; width: 100%; padding: 12px 16px; color: #475467; background: transparent; border: 0; text-align: left; font: inherit; cursor: pointer; }.retrieval-heading > i { flex: none; margin-top: 2px; color: #7c839f; font-size: 19px; }.retrieval-heading > span { min-width: 0; }.retrieval-heading strong { font-size: 13px; font-weight: 600; line-height: 20px; }.retrieval-heading small { display: block; margin-top: 2px; color: #7c859c; font-size: 12px; line-height: 1.6; }.retrieval-heading em { display: inline-block; margin-left: 6px; padding: 0 4px; color: #1649f5; border: 1px solid #1649f5; border-radius: 4px; font-size: 10px; line-height: 16px; font-style: normal; vertical-align: 1px; }.retrieval-heading:disabled { cursor: not-allowed; }.retrieval-card.is-selected > .retrieval-heading { background: #f9faff; }.retrieval-card.is-selected > .retrieval-heading > i { color: #1649f5; }.retrieval-card.is-selected > .retrieval-heading::after { position: absolute; bottom: 0; left: 20px; width: 0; height: 0; border-right: 8px solid transparent; border-bottom: 8px solid #fff; border-left: 8px solid transparent; content: ''; }
.retrieval-body { padding: 16px 18px 14px; }.rerank-control { display: flex; align-items: center; gap: 8px; min-height: 20px; color: #344054; font-size: 13px; }.rerank-control label, .retrieval-controls .control-label label { cursor: pointer; }.help-icon { color: #a8afbd; font-size: 13px; cursor: help; }.rerank-model-field { margin-top: 14px; }.retrieval-weights { margin-top: 18px; }.retrieval-weights .field-hint { margin-top: 0; }
.retrieval-controls { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px; margin-top: 20px; }.control-label { display: flex; align-items: center; gap: 7px; color: #344054; font-size: 13px; line-height: 24px; }.control-label > span { color: #98a2b3; font-size: 12px; }.parameter-input { display: flex; align-items: center; gap: 14px; margin-top: 6px; }.parameter-input > .el-input-number { flex: none; width: 98px; }.parameter-input > .el-slider { flex: 1; min-width: 0; padding: 0 4px; }.parameter-input ::v-deep .el-input__inner { padding-left: 12px; background: #f3f4f7; border-color: transparent; text-align: left; }.parameter-input ::v-deep .el-input-number__decrease, .parameter-input ::v-deep .el-input-number__increase { width: 24px; background: transparent; border-color: #e8ebf1; }.parameter-input ::v-deep .el-slider__runway, .retrieval-weights ::v-deep .el-slider__runway { height: 4px; background: #e8ebf2; }.parameter-input ::v-deep .el-slider__bar, .retrieval-weights ::v-deep .el-slider__bar { height: 4px; background: #1649f5; }.parameter-input ::v-deep .el-slider__button-wrapper, .retrieval-weights ::v-deep .el-slider__button-wrapper { top: -16px; }.parameter-input ::v-deep .el-slider__button, .retrieval-weights ::v-deep .el-slider__button { width: 7px; height: 18px; border: 1px solid #d8deea; border-radius: 3px; background: #fff; box-shadow: 0 1px 3px rgba(16,24,40,.12); }.parameter-input ::v-deep .el-slider.is-disabled .el-slider__bar { background: #b9c7ff; }.parameter-input ::v-deep .el-input.is-disabled .el-input__inner { color: #98a2b3; background: #f8f9fb; }.parameter-input ::v-deep .el-slider.is-disabled .el-slider__button { border-color: #edf0f5; box-shadow: none; }
.index-configuration ::v-deep .el-slider__input { width: 112px; }.index-configuration ::v-deep .el-slider__runway.show-input { margin-right: 130px; }
.save-bar { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; padding-top: 22px; }.save-bar .el-button + .el-button { margin-left: 0; }.save-bar span { color: #98a2b3; font-size: 12px; }.save-bar .el-button--primary { background: #1649f5; border-color: #1649f5; }.load-error { margin: 80px 0; color: #667085; text-align: center; }.load-error > i { color: #e6a23c; font-size: 38px; }
.choice-card:focus-visible, .retrieval-heading:focus-visible, .retrieval-help-button:focus-visible, .settings-sidebar button:focus-visible { outline: 2px solid #1649f5; outline-offset: 3px; }.retrieval-heading:focus-visible { outline-offset: -3px; }button:disabled { cursor: not-allowed; }
.choice-card:disabled:not(.is-selected) { color: #98a2b3; background: #f8fafc; border-color: #edf0f4; opacity: .65; }
@media (max-width: 1100px) { .settings-sidebar { flex-basis: 190px; }.settings-main { padding: 25px; }.retrieval-section { grid-template-columns: 1fr; gap: 14px; } }
@media (max-width: 850px) { .retrieval-controls { grid-template-columns: 1fr; gap: 15px; } }
@media (max-width: 700px) { .knowledge-settings-page { flex-direction: column; }.settings-sidebar { flex: auto; padding: 14px 16px; border-right: 0; border-bottom: 1px solid #edf0f4; }.base-summary { margin: 18px 0; }.settings-sidebar nav { grid-template-columns: repeat(4, 1fr); }.settings-sidebar nav button { padding: 10px 4px; text-align: center; font-size: 12px; }.settings-sidebar nav button i { margin-right: 3px; }.settings-main { padding: 22px 16px; }.segment-options, .index-options { grid-template-columns: 1fr; } }
</style>
