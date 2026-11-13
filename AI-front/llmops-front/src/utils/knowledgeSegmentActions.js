export function parseSegmentBatchResult(response, action, requestedIds) {
  const data = response && response.data !== undefined ? response.data : response
  const expected = [...new Set(requestedIds)]
  if (!data || data.action !== action || !Array.isArray(data.results) || data.results.length !== expected.length) throw new Error('批量操作结果不完整，请刷新列表确认后再操作')
  const seen = new Set(), succeededIds = [], failedItems = []
  data.results.forEach(item => {
    if (!item || !expected.includes(item.segment_id) || seen.has(item.segment_id) || typeof item.success !== 'boolean') throw new Error('批量操作结果异常，请刷新列表确认')
    seen.add(item.segment_id)
    if (item.success) succeededIds.push(item.segment_id)
    else failedItems.push({ id: item.segment_id, message: item.message || item.code || '操作失败' })
  })
  if (data.total !== expected.length || data.success_count !== succeededIds.length || data.failure_count !== failedItems.length) throw new Error('批量操作统计与逐项结果不一致，请刷新列表确认')
  return { succeededIds, failedItems }
}
