// One bounded request, never silently drop invalid rows or split writes into partial batches.
export const MAX_SEGMENT_IMPORT_ROWS = 100
export const MAX_SEGMENT_IMPORT_BYTES = 5 * 1024 * 1024

export function parseSegmentCsv(text, qa = false) {
  if (typeof text !== 'string') throw new Error('无法读取 CSV 文件')
  const source = text.replace(/^\uFEFF/, '')
  if (source.includes('\uFFFD')) throw new Error('文件编码无效，请使用 UTF-8 编码的 CSV')
  const rows = []
  let row = [], field = '', quoted = false, closed = false
  const finishField = () => { row.push(field); field = ''; closed = false }
  const finishRow = () => {
    finishField()
    if (row.some(value => value.trim())) rows.push(row)
    row = []
    if (rows.length > MAX_SEGMENT_IMPORT_ROWS + 1) throw new Error(`每次最多导入 ${MAX_SEGMENT_IMPORT_ROWS} 个分段`)
  }
  for (let index = 0; index < source.length; index++) {
    const char = source[index]
    if (quoted) {
      if (char === '"') {
        if (source[index + 1] === '"') { field += '"'; index++ }
        else { quoted = false; closed = true }
      } else field += char
    } else if (char === ',') finishField()
    else if (char === '\n' || char === '\r') {
      if (char === '\r' && source[index + 1] === '\n') index++
      finishRow()
    } else if (char === '"') {
      if (field || closed) throw new Error('CSV 引号格式错误，请下载模板后填写')
      quoted = true
    } else {
      if (closed) throw new Error('CSV 引号后存在多余内容')
      field += char
    }
  }
  if (quoted) throw new Error('CSV 引号未闭合')
  if (field || row.length || closed) finishRow()
  const header = qa ? ['问题', '答案'] : ['分段内容']
  if (!rows.length || rows[0].length !== header.length || !rows[0].every((cell, index) => cell.trim() === header[index])) throw new Error(`表头必须为：${header.join('、')}`)
  if (rows.length < 2) throw new Error('CSV 中没有可导入的分段')
  return rows.slice(1).map((cells, index) => {
    if (cells.length !== header.length) throw new Error(`第 ${index + 2} 条记录列数不正确`)
    if (cells.some(cell => !cell.trim())) throw new Error(`第 ${index + 2} 条记录内容不能为空`)
    return qa ? { content: cells[0], answer: cells[1] } : { content: cells[0] }
  })
}

export function segmentCsvTemplate(qa = false) {
  return '\uFEFF' + (qa ? '问题,答案\r\n示例问题,示例答案\r\n' : '分段内容\r\n这里是第一条分段内容\r\n这里是第二条分段内容\r\n')
}

export function uncertainKnowledgeWrite(error) {
  const status = error && error.response && error.response.status
  return !status || status >= 500 || status === 408
}
