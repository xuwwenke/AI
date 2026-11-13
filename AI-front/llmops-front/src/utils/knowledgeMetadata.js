const TYPES = ['string', 'number', 'time']
const unwrap = result => {
  let data = result
  for (let depth = 0; depth < 4 && data && typeof data === 'object'; depth++) {
    if (Array.isArray(data.doc_metadata) || Array.isArray(data.fields) || Array.isArray(data)) break
    if (data.data && typeof data.data === 'object') data = data.data
    else break
  }
  return data
}

export function parseKnowledgeMetadata(result, builtInResult) {
  const payload = unwrap(result)
  const builtInPayload = unwrap(builtInResult)
  if (!payload || !Array.isArray(payload.doc_metadata) || typeof payload.built_in_field_enabled !== 'boolean') throw new Error('元数据列表或内置启用状态响应异常，请刷新重试')
  const builtIn = Array.isArray(builtInPayload) ? builtInPayload : builtInPayload && builtInPayload.fields
  if (!Array.isArray(builtIn)) throw new Error('内置元数据列表响应异常，请刷新重试')
  const validField = field => field && typeof field.name === 'string' && !!field.name.trim() && TYPES.includes(field.type)
  if (!builtIn.every(validField)) throw new Error('内置元数据字段格式异常')
  const builtInNames = new Set(builtIn.map(field => field.name))
  const fields = payload.doc_metadata.filter(field => field && field.id !== 'built-in' && field.type !== 'built-in' && !builtInNames.has(field.name))
  if (!fields.every(field => validField(field) && typeof field.id === 'string' && !!field.id)) throw new Error('自定义元数据字段格式异常')
  return { fields, builtInFields: builtIn, builtInEnabled: payload.built_in_field_enabled }
}

export function validateKnowledgeMetadataName(name, fields, builtInFields, exceptId = '') {
  const value = typeof name === 'string' ? name.trim() : ''
  if (!value) throw new Error('请输入元数据名称')
  const equalName = field => field.name.toLowerCase() === value.toLowerCase()
  if (builtInFields.some(equalName)) throw new Error('不能使用内置元数据字段名称')
  if (fields.some(field => field.id !== exceptId && equalName(field))) throw new Error('元数据名称已存在')
  return value
}
