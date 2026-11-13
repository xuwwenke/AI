const BUILT_IN_NAMES = ['document_name', 'uploader', 'upload_date', 'last_update_date', 'source']

export function isBuiltInDocumentMetadata(field) {
  return field.id === 'built-in' || field.type === 'built-in' || BUILT_IN_NAMES.includes(field.name)
}

export function documentMetadataRows(document, fields) {
  if (!document || !Object.prototype.hasOwnProperty.call(document, 'doc_metadata')) throw new Error('文档未返回元数据，不能安全保存，请刷新重试')
  const values = document.doc_metadata == null ? [] : document.doc_metadata
  if (!Array.isArray(values)) throw new Error('文档元数据格式异常，不能安全保存')
  return values.filter(field => !isBuiltInDocumentMetadata(field)).map(field => {
    if (!field.id || !field.name || !Object.prototype.hasOwnProperty.call(field, 'value')) throw new Error('文档元数据字段不完整')
    const definition = fields.find(item => item.id === field.id)
    const type = definition ? definition.type : field.type
    return { id: field.id, name: definition ? definition.name : field.name, type, value: field.value, readonly: !definition }
  })
}

export function serializeDocumentMetadata(rows) {
  const ids = new Set()
  return rows.map(field => {
    if (!field.id || !field.name || isBuiltInDocumentMetadata(field) || ids.has(field.id)) throw new Error('元数据字段无效或重复，请重新打开标注')
    ids.add(field.id)
    let value = field.value
    if (!field.readonly) {
      if (field.type === 'number' || field.type === 'time') {
        if (value == null || typeof value === 'string' && !value.trim()) value = null
        else {
          value = Number(value)
          if (!Number.isFinite(value)) throw new Error(`${field.name} 必须是有效数字${field.type === 'time' ? '时间戳' : ''}`)
        }
      } else if (field.type === 'string') value = value == null ? null : String(value)
      else throw new Error(`${field.name} 的字段类型不受支持`)
    }
    return { id: field.id, name: field.name, value }
  })
}
