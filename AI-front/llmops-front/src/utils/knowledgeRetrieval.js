const SEARCH_METHODS = ['semantic_search', 'full_text_search', 'hybrid_search']
const validWeight = value => typeof value === 'number' && Number.isFinite(value) && value >= 0 && value <= 1

// GET knowledge-base uses DatasetRetrievalModel; POST retrieve uses a different RetrievalModel.
// Do not copy the GET object verbatim: inactive optional fields may be null, and weights are nested.
export function buildKnowledgeRetrievalModel(saved, searchMethod) {
  const source = saved && typeof saved === 'object' && !Array.isArray(saved) ? saved : {}
  if (!SEARCH_METHODS.includes(searchMethod)) throw new Error('请选择有效的检索方式')
  const topK = source.top_k == null ? 3 : Number(source.top_k)
  if (!Number.isInteger(topK) || topK < 1 || topK > 100) throw new Error('知识库 Top K 配置应为 1–100 的整数，请前往设置页修改')
  const usesRerankModel = !!source.reranking_enable && source.reranking_mode !== 'weighted_score'
  const result = {
    search_method: searchMethod,
    reranking_enable: usesRerankModel,
    top_k: topK,
    score_threshold_enabled: !!source.score_threshold_enabled
  }
  if (result.score_threshold_enabled) {
    if (!validWeight(source.score_threshold)) throw new Error('知识库 Score 阈值应在 0–1 之间，请前往设置页修改')
    result.score_threshold = source.score_threshold
  }
  if (usesRerankModel) {
    const model = source.reranking_model || {}
    const provider = model.reranking_provider_name
    const name = model.reranking_model_name
    if (typeof provider !== 'string' || !provider.trim() || typeof name !== 'string' || !name.trim()) throw new Error('知识库已启用 Rerank，但模型配置不完整，请在设置页选择模型或关闭 Rerank')
    result.reranking_mode = 'reranking_model'
    result.reranking_model = { reranking_provider_name: provider, reranking_model_name: name }
  } else if (searchMethod === 'hybrid_search' && (source.reranking_mode === 'weighted_score' || source.weights != null)) {
    const weights = source.weights || {}
    const vector = typeof weights.vector_setting === 'number' ? weights.vector_setting : weights.vector_setting && weights.vector_setting.vector_weight
    const keyword = typeof weights.keyword_setting === 'number' ? weights.keyword_setting : weights.keyword_setting && weights.keyword_setting.keyword_weight
    if (!validWeight(vector) || !validWeight(keyword)) throw new Error('知识库混合检索权重配置不完整，请在设置页重新保存权重设置')
    result.reranking_mode = 'weighted_score'
    result.reranking_enable = !!source.reranking_enable
    result.weights = { vector_setting: vector, keyword_setting: keyword }
  }
  return result
}

export function knowledgeRetrievalError(error) {
  const data = error && error.response && error.response.data
  if (typeof data === 'string' && data.trim() && !/<(?:html|!doctype)/i.test(data)) return data
  if (data && typeof data === 'object') {
    if (typeof data.message === 'string' && data.message) return data.message
    if (typeof data.detail === 'string' && data.detail) return data.detail
    const messages = []
    const collect = (value, path, depth) => {
      if (messages.length >= 6 || depth > 6) return
      if (typeof value === 'string') messages.push(path ? `${path}：${value}` : value)
      else if (Array.isArray(value)) value.forEach(item => collect(item, path, depth + 1))
      else if (value && typeof value === 'object') Object.keys(value).forEach(key => collect(value[key], path ? `${path}.${key}` : key, depth + 1))
    }
    collect(data, '', 0)
    if (messages.length) return messages.join('；')
  }
  return error && error.message || '召回测试失败'
}
