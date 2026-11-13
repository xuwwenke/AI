// The global list returns data: [], whereas bound tags return data: { data: [], total }.
export function parseKnowledgeTags(result) {
  let list = result
  for (let depth = 0; depth < 4 && list && !Array.isArray(list); depth += 1) {
    list = list.data !== undefined ? list.data : list.items !== undefined ? list.items : list.results
  }
  if (!Array.isArray(list)) throw new Error('标签列表响应格式异常')
  const seen = new Set()
  return list.map(tag => {
    if (!tag || !tag.id || typeof tag.name !== 'string') throw new Error('标签数据缺少 ID 或名称')
    return { ...tag, id: String(tag.id) }
  }).filter(tag => {
    if (seen.has(tag.id)) return false
    seen.add(tag.id)
    return true
  })
}
