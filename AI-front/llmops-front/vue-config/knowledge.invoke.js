/* eslint-env node */
// Fixed-upstream retrieval adapter: the copied URL carries namespace + platform ID.
const INVOKE_PATH = /^\/knowledge-invoke\/([A-Za-z0-9][A-Za-z0-9_.-]{0,127})\/knowledge-bases\/([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})\/retrieve\/(\?[^#]*)?$/

function parseKnowledgeInvokePath(value) {
  const match = typeof value === 'string' && value.match(INVOKE_PATH)
  return match ? { namespace: match[1], path: `/api/knowledge-bases/${match[2]}/retrieve/${match[3] || ''}` } : null
}

function guardKnowledgeInvoke(req, res, next) {
  const route = parseKnowledgeInvokePath(req.originalUrl || req.url)
  if (!route) return res.status(404).json({ code: 'INVALID_KNOWLEDGE_INVOKE_URL', message: '知识库调用地址无效' })
  if (req.method !== 'POST') return res.set('Allow', 'POST').status(405).json({ code: 'METHOD_NOT_ALLOWED', message: '请使用 POST，并以 JSON 传入 query' })
  req.knowledgeInvoke = route
  next()
}

function rewriteKnowledgeInvoke(path, req) {
  const route = req.knowledgeInvoke || parseKnowledgeInvokePath(path)
  if (!route) throw new Error('Invalid knowledge invoke URL')
  return route.path
}

function setKnowledgeInvokeHeaders(proxyReq, req) {
  // The namespace in the copied URL is authoritative; callers cannot override it.
  proxyReq.setHeader('namespace', req.knowledgeInvoke.namespace)
}

module.exports = { parseKnowledgeInvokePath, guardKnowledgeInvoke, rewriteKnowledgeInvoke, setKnowledgeInvokeHeaders }
