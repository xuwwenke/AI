const clone = value => JSON.parse(JSON.stringify(value))
const modelKey = (provider, model) => provider && model ? JSON.stringify([provider, model]) : ''
const memberIds = members => Array.isArray(members) ? [...new Set(members.map(member => typeof member === 'string' ? member : member && member.user_id).filter(Boolean))] : []
const sameMembers = (first, second) => JSON.stringify([...first].sort()) === JSON.stringify([...second].sort())

export function normalizeKnowledgeModels(result, type) {
  const payload = result && result.data !== undefined ? result.data : result
  const providers = Array.isArray(payload) ? payload : payload && payload.providers
  if (!Array.isArray(providers)) throw new Error('模型列表响应格式异常')
  const labelText = label => typeof label === 'string' ? label : label && (label.zh_Hans || label.en_US) || ''
  const models = []
  providers.forEach(provider => {
    if (!provider || !provider.provider || provider.status && provider.status !== 'active') return
    ;(Array.isArray(provider.models) ? provider.models : []).forEach(model => {
      if (!model || !model.model || model.model_type && model.model_type !== type || model.status && model.status !== 'active') return
      const key = modelKey(provider.provider, model.model)
      if (!models.some(item => item.key === key)) models.push({ key, provider: provider.provider, model: model.model, label: labelText(model.label) || model.model, providerLabel: labelText(provider.label) || provider.provider })
    })
  })
  return models
}

export function buildKnowledgeCreationPayload(settings, embeddingModels = [], rerankModels = []) {
  if (!['economy', 'high_quality'].includes(settings.indexingTechnique)) throw new Error('请选择有效的索引方式')
  const base = { indexing_technique: 'economy' }
  // Creation requires a full retrieval configuration, even when it matches the UI defaults.
  const initial = { ...normalizeKnowledgeSettings(base), topK: 0 }
  const form = { ...initial, ...settings, name: '', description: '' }
  const payload = buildKnowledgeSettingsPayload(form, initial, base, embeddingModels, rerankModels)
  return { ...payload, indexing_technique: settings.indexingTechnique }
}

export function normalizeKnowledgeSettings(base) {
  const retrieval = base.retrieval_model_dict || base.retrieval_model || {}
  const rerank = retrieval.reranking_model || {}
  const vector = retrieval.weights && retrieval.weights.vector_setting || {}
  return {
    name: base.name || '', description: base.description || '', permission: base.permission || 'only_me',
    partialMemberIds: memberIds(base.partial_member_list), docForm: base.doc_form || '',
    indexingTechnique: base.indexing_technique || 'economy', embeddingKey: modelKey(base.embedding_model_provider, base.embedding_model),
    keywordNumber: base.keyword_number == null ? 10 : Number(base.keyword_number),
    searchMethod: ['semantic_search', 'full_text_search', 'hybrid_search'].includes(retrieval.search_method) ? retrieval.search_method : 'semantic_search',
    rerankingEnable: !!retrieval.reranking_enable && retrieval.reranking_mode !== 'weighted_score', rerankKey: modelKey(rerank.reranking_provider_name, rerank.reranking_model_name),
    vectorWeight: vector.vector_weight == null ? 0.5 : Number(vector.vector_weight),
    topK: retrieval.top_k == null ? 3 : Number(retrieval.top_k), scoreThresholdEnabled: !!retrieval.score_threshold_enabled,
    scoreThreshold: retrieval.score_threshold == null ? 0.5 : Number(retrieval.score_threshold)
  }
}

function selectedModel(key, models, originalKey, label) {
  let parts
  try { parts = JSON.parse(key) } catch (e) { /* Report a field-specific validation error below. */ }
  if (!Array.isArray(parts) || parts.length !== 2 || parts.some(part => typeof part !== 'string' || !part.trim())) throw new Error(`请选择 ${label} 模型`)
  if (key !== originalKey && !models.some(model => model.key === key && !model.unavailable)) throw new Error(`${label} 模型不可用，请刷新模型列表后重新选择`)
  return { provider: parts[0], model: parts[1] }
}

export function buildKnowledgeSettingsPayload(form, initial, base, embeddingModels = [], rerankModels = []) {
  // Use the persisted configuration, not the editable form, to enforce the one-way upgrade.
  if (base.indexing_technique === 'high_quality' && form.indexingTechnique !== 'high_quality') throw new Error('知识库已启用高质量索引，不支持切换回经济模式')
  const payload = {}
  const changed = field => JSON.stringify(form[field]) !== JSON.stringify(initial[field])
  const highQuality = form.indexingTechnique === 'high_quality'
  const indexChanged = changed('indexingTechnique')
  if (form.name.trim() !== initial.name.trim()) {
    if (!form.name.trim() || form.name.trim().length > 40) throw new Error('知识库名称应为 1–40 个字符')
    payload.name = form.name.trim()
  }
  if (changed('description')) {
    if (form.description.length > 400) throw new Error('知识库描述不能超过 400 个字符')
    payload.description = form.description
  }
  if (changed('permission')) {
    if (!['only_me', 'all_team_members', 'partial_members'].includes(form.permission)) throw new Error('请选择有效的知识库权限')
    payload.permission = form.permission
  }
  if (form.permission === 'partial_members' && (changed('permission') || !sameMembers(form.partialMemberIds, initial.partialMemberIds))) {
    const ids = [...new Set(form.partialMemberIds.map(id => id.trim()).filter(Boolean))]
    if (!ids.length) throw new Error('指定成员权限至少需要一位成员，请输入成员 user_id')
    payload.partial_member_list = ids.map(user_id => ({ user_id }))
  }
  if (indexChanged) payload.indexing_technique = form.indexingTechnique
  if (highQuality && (indexChanged || changed('embeddingKey'))) {
    const embedding = selectedModel(form.embeddingKey, embeddingModels, initial.embeddingKey, 'Embedding')
    payload.embedding_model_provider = embedding.provider
    payload.embedding_model = embedding.model
  }

  const weighted = highQuality && form.searchMethod === 'hybrid_search' && !form.rerankingEnable
  const retrievalChanged = indexChanged || changed('topK') || highQuality && (
    changed('searchMethod') || changed('rerankingEnable') || changed('scoreThresholdEnabled') ||
    form.scoreThresholdEnabled && changed('scoreThreshold') || form.rerankingEnable && changed('rerankKey') ||
    weighted && (changed('vectorWeight') || changed('embeddingKey'))
  )
  if (retrievalChanged) {
    if (!Number.isInteger(form.topK) || form.topK < 1 || form.topK > 100) throw new Error('Top K 应为 1–100 的整数')
    if (highQuality && form.scoreThresholdEnabled && (!Number.isFinite(form.scoreThreshold) || form.scoreThreshold < 0 || form.scoreThreshold > 1)) throw new Error('Score 阈值应在 0–1 之间')
    if (highQuality && !['semantic_search', 'full_text_search', 'hybrid_search'].includes(form.searchMethod)) throw new Error('请选择有效的检索方式')
    // Keep server-side metadata filters and other documented retrieval options when editing visible controls.
    const previous = clone(base.retrieval_model_dict || base.retrieval_model || {})
    const retrieval = {}
    ;['metadata_filtering_conditions', 'weights'].forEach(key => {
      if (previous[key] !== undefined) retrieval[key] = previous[key]
    })
    // Dify returns null model names when reranking is off, but PATCH requires strings.
    if (previous.reranking_model && previous.reranking_model.reranking_provider_name && previous.reranking_model.reranking_model_name) retrieval.reranking_model = previous.reranking_model
    Object.assign(retrieval, {
      search_method: highQuality ? form.searchMethod : 'keyword_search', top_k: form.topK,
      reranking_enable: highQuality && form.rerankingEnable,
      reranking_mode: weighted ? 'weighted_score' : highQuality && form.rerankingEnable ? 'reranking_model' : null,
      score_threshold_enabled: highQuality && form.scoreThresholdEnabled,
      score_threshold: highQuality && form.scoreThresholdEnabled ? form.scoreThreshold : null
    })
    if (retrieval.reranking_enable) {
      const rerank = selectedModel(form.rerankKey, rerankModels, initial.rerankKey, 'Rerank')
      retrieval.reranking_model = { reranking_provider_name: rerank.provider, reranking_model_name: rerank.model }
    }
    if (weighted) {
      if (!Number.isFinite(form.vectorWeight) || form.vectorWeight < 0 || form.vectorWeight > 1) throw new Error('向量权重应在 0–1 之间')
      const embedding = selectedModel(form.embeddingKey, embeddingModels, initial.embeddingKey, 'Embedding')
      retrieval.weights = {
        weight_type: 'customized',
        vector_setting: { vector_weight: form.vectorWeight, embedding_provider_name: embedding.provider, embedding_model_name: embedding.model },
        keyword_setting: { keyword_weight: Number((1 - form.vectorWeight).toFixed(2)) }
      }
    }
    payload.retrieval_model = retrieval
  }
  return payload
}

export function verifyKnowledgeSettings(saved, payload) {
  const labels = { name: '名称', description: '描述', permission: '权限', indexing_technique: '索引方式', embedding_model: 'Embedding 模型', embedding_model_provider: 'Embedding 提供商' }
  const mismatches = Object.keys(labels).filter(key => key in payload && (key === 'description' ? saved[key] || '' : saved[key]) !== payload[key]).map(key => labels[key])
  if (payload.partial_member_list && (!Array.isArray(saved.partial_member_list) || !sameMembers(memberIds(saved.partial_member_list), memberIds(payload.partial_member_list)))) mismatches.push('指定成员')
  if (payload.retrieval_model) {
    const actual = saved.retrieval_model_dict || saved.retrieval_model || {}
    const expected = payload.retrieval_model
    const fields = ['search_method', 'top_k', 'reranking_enable', 'score_threshold_enabled']
    if (expected.score_threshold_enabled) fields.push('score_threshold')
    if (expected.reranking_enable) fields.push('reranking_mode')
    if (fields.some(key => actual[key] !== expected[key])) mismatches.push('检索设置')
    if (expected.reranking_enable && ['reranking_provider_name', 'reranking_model_name'].some(key => !actual.reranking_model || actual.reranking_model[key] !== expected.reranking_model[key])) mismatches.push('Rerank 模型')
    if (expected.reranking_mode === 'weighted_score') {
      const weights = actual.weights || {}
      if (actual.reranking_mode !== 'weighted_score' || !weights.vector_setting || !weights.keyword_setting ||
        ['vector_weight', 'embedding_provider_name', 'embedding_model_name'].some(key => weights.vector_setting[key] !== expected.weights.vector_setting[key]) ||
        weights.keyword_setting.keyword_weight !== expected.weights.keyword_setting.keyword_weight) mismatches.push('混合检索权重')
    }
  }
  return mismatches
}
