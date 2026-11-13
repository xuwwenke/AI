// This URL belongs to the frontend gateway, not the raw knowledge service.
export function buildKnowledgeInvokeUrl({ knowledgeBaseId, namespace, origin, baseUrl }) {
  if (typeof knowledgeBaseId !== 'string' || !/^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i.test(knowledgeBaseId)) throw new Error('知识库 ID 无效，请刷新列表后重试')
  if (typeof namespace !== 'string' || !/^[A-Za-z0-9][A-Za-z0-9_.-]{0,127}$/.test(namespace)) throw new Error('缺少有效的当前项目 namespace，请重新选择项目')
  let base
  try { base = new URL(baseUrl || '/knowledge-invoke', origin) } catch (error) { throw new Error('知识库外部调用地址配置无效') }
  if (!['http:', 'https:'].includes(base.protocol) || base.username || base.password || base.search || base.hash) throw new Error('知识库外部调用地址配置无效')
  return `${base.href.replace(/\/+$/, '')}/${namespace}/knowledge-bases/${knowledgeBaseId}/retrieve/`
}

export function knowledgeInvokePythonExample(url) {
  return `import requests

url = ${JSON.stringify(url)}
response = requests.post(
    url,
    json={"query": "Ansible 如何安装？"},
    timeout=30,
)
response.raise_for_status()
result = response.json()
if result.get("code") != 0:
    raise RuntimeError(result.get("message") or str(result))
print(result["data"]["records"])`
}

export function isLocalKnowledgeInvokeUrl(url) {
  const hostname = new URL(url).hostname.toLowerCase()
  return hostname === 'localhost' || hostname.endsWith('.localhost') || hostname === '[::1]' || /^127\./.test(hostname)
}
