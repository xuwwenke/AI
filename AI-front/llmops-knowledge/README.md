Django 的知识库 UUID 是平台自己的 ID，而 Dify 的知识库 ID 是 backend_dataset_id。上传文档时，应该先根据平台 UUID 查询数据库，再使用对应的 Dify ID 调用接口。

知识库配置保存成功后，若文档重建遇到 `unable to store logs within log store`
或 `input/output error`，参见 [Weaviate 存储故障恢复](docs/weaviate-storage-recovery.md)。

## 更新知识库

```http
PATCH /api/knowledge-bases/{平台知识库ID}/
Content-Type: application/json
namespace: project-a

{
  "name": "Product Documentation",
  "description": "产品 API 技术文档",
  "indexing_technique": "high_quality",
  "embedding_model": "text-embedding-3-small",
  "embedding_model_provider": "langgenius/openai/openai",
  "permission": "partial_members",
  "partial_member_list": [{"user_id": "团队成员ID"}],
  "retrieval_model": {
    "search_method": "semantic_search",
    "top_k": 3,
    "reranking_enable": false,
    "score_threshold_enabled": false
  }
}
```

所有顶层字段均可省略，至少提交一个可更新字段。另支持
`external_knowledge_api_id`、`external_knowledge_id` 和
`external_retrieval_model`（`score_threshold`、`score_threshold_enabled`、`top_k`）。
名称为 1–40 个字符，描述最多 400 个字符。检索配置支持重排序、混合搜索权重及元数据过滤；
提交 `retrieval_model` 时必须包含示例中的四个字段，启用重排序时还必须指定 `reranking_mode`。

仅向 Dify 转发显式提交的字段，不注入默认配置；保留空描述、空成员列表、
`false`、`0` 及 `indexing_technique: null`。后端更新成功后才保存本地名称和描述。
尚未关联后端的知识库只能更新本地名称和描述，不能更新后端配置。

响应沿用 `{"code": 0, "message": "知识库更新成功", "data": {...}}`，
`data` 合并 Dify PATCH 返回的完整知识库信息（包括 `retrieval_model_dict`、
`embedding_available`、`partial_member_list` 等），无需额外请求 Dify 详情接口。
`id` 保留平台知识库 ID，`backend_dataset_id` 为 Dify 知识库 ID；
项目归属和创建者快照保持本地值。接口文档中已包含更新请求的嵌套配置字段。

## 文档状态更新错误

`PATCH /api/knowledge-bases/{knowledge_base_id}/documents/status/{action}/`
请求中的 `document_ids` 使用平台文档 ID，服务会转换成 Dify 文档 ID。

Dify 返回业务错误（HTTP 400、404、409、422、429）时，接口保留对应状态，
响应中的 `detail` 返回 Dify 的具体错误原因，`backend_status` 和 `backend_code`
分别记录后端 HTTP 状态及错误码（后端未提供错误码时省略）。例如：

```json
{
  "code": "BACKEND_UPDATE_STATUS_FAILED",
  "message": "批量更新文档状态失败",
  "detail": "Document is being indexed, please try again later",
  "backend_status": 400,
  "backend_code": "invalid_action"
}
```

应根据 `detail` 处理具体原因；Dify 可能在文档索引任务执行期间拒绝状态变更。
连接失败、超时、后端鉴权失败或后端服务异常仍返回 HTTP 502。

## 添加模型供应商

模型供应商由本项目自行管理，不依赖 Dify。生产环境建议配置独立且稳定的 Fernet 密钥：

```env
MODEL_CREDENTIAL_ENCRYPTION_KEY=<Fernet key>
```

可使用 `python -c "from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())"` 生成。未配置时会根据 Django `SECRET_KEY` 派生密钥。

调用本项目接口：

```http
POST /api/model-providers/langgenius/openai/openai/credentials/
Content-Type: application/json
namespace: project-a

{
  "name": "production",
  "supported_model_types": ["llm"],
  "models": ["gpt-4.1", "gpt-4.1-mini"],
  "credentials": {
    "api_key": "sk-...",
    "base_url": "https://api.openai.com/v1"
  }
}
```

`provider` 是平台内的供应商标识；`credentials` 内可保存该供应商所需的 API Key、服务地址等配置。凭据使用 Fernet 加密落库，响应只返回凭据字段名，不返回具体值。`models` 可省略；对于 `llm` 类型，省略后会通过 OpenAI 兼容的 `base_url/models` 接口动态发现。

按类型查询当前 namespace 下本平台自管的供应商和模型：

```http
GET /api/local-models/llm/
namespace: project-a
```

预定义供应商会返回 `features`、`model_properties` 和用于界面展示的 `tags`。例如 DeepSeek 模型会包含 `LLM`、`CHAT`、`1048K`、`VISION` 等标签；OpenAI 兼容接口动态发现的未知模型至少返回模型类型和模式标签。

## 获取 Dify 可用模型

```http
GET /api/models/text-embedding/
namespace: project-a
```

此接口调用 `GET /v1/workspaces/current/models/model-types/{model_type}`，
使用服务配置的 `DIFY_BASE_URL` 和 `DIFY_API_KEY`。
支持 `text-embedding`、`rerank`、`llm`、`tts`、`speech2text`、`moderation`。
`namespace` 请求头仍必填，返回的 Dify 工作空间由 `DIFY_API_KEY` 决定。

响应为 `{"code": 0, "message": "success", "data": [...]}`。
`data` 是 Dify 返回的供应商数组，保留供应商的 `provider`、`label`、
`icon_small`、`icon_small_dark`、`status`、`tenant_id` 和完整的 `models` 字段，
包括模型能力、属性、来源及负载均衡状态；不再包裹 `data.providers`。

配置知识库时，嵌入模型使用 `text-embedding`：将供应商的 `provider` 作为
`embedding_model_provider`，模型的 `model` 作为 `embedding_model`。
重排序模型使用 `rerank`：分别映射到 `reranking_provider_name` 和
`reranking_model_name`。Dify 请求失败时返回 HTTP 502，并保留后端错误详情。

原本平台自管的模型查询已迁移至 `/api/local-models/{model_type}/`，
继续返回 `data.providers`；调用原本地查询的客户端需要更新路径。

## 模型运行网关

Dify Provider 插件通过内部模型网关执行 Embedding 和 Rerank。启动服务前设置独立的服务令牌：

```env
MODEL_GATEWAY_SERVICE_TOKEN=<long-random-token>
```

所有运行接口都需要以下请求头：

```http
Authorization: Bearer <MODEL_GATEWAY_SERVICE_TOKEN>
namespace: project-a
```

Embedding：

```http
POST /api/internal/models/embeddings/
Content-Type: application/json

{
  "provider": "openai-compatible",
  "credential_name": "production",
  "model": "bge-m3",
  "texts": ["第一段", "第二段"],
  "input_type": "document"
}
```

Token Count：

```http
POST /api/internal/models/embeddings/token-count/

{
  "provider": "openai-compatible",
  "credential_name": "production",
  "model": "bge-m3",
  "texts": ["第一段", "第二段"]
}
```

如果供应商凭据包含 `token_count_url`，网关会调用该地址，要求返回
`{"token_counts": [3, 4]}`。未配置时使用本地估算，并在响应中返回
`"method": "estimated"`。

Rerank：

```http
POST /api/internal/models/rerank/

{
  "provider": "openai-compatible",
  "credential_name": "production",
  "model": "bge-reranker-v2-m3",
  "query": "报销流程是什么？",
  "documents": ["文档一", "文档二"],
  "top_n": 2,
  "score_threshold": 0.3
}
```

默认调用供应商 `base_url` 下的 `/embeddings` 和 `/rerank`。非标准服务可在凭据中分别配置完整的 `embeddings_url`、`rerank_url` 和额外 `headers`。

## 创建知识库时保存用户信息

平台调用方应从当前登录会话获取用户名和邮箱，传入创建接口：

```http
POST /api/knowledge-bases/
Content-Type: application/json
namespace: project-a

{
  "name": "团队知识库",
  "description": "项目文档",
  "creator_username": "zhangsan",
  "creator_email": "zhangsan@example.com"
}
```

两个字段仅保存在本地知识库记录中，不传给 Dify；创建、列表和详情响应均包含这两个字段。邮箱会进行格式校验。为兼容旧调用，字段可省略，默认空字符串；历史记录也默认空字符串。创建者信息作为创建时的快照，更新知识库接口不修改它们。本服务目前不解析 LLMOps 登录态，调用方负责传入当前登录用户的真实信息。

部署更新后执行数据库迁移：

```bash
python manage.py migrate
```

## 给分段添加图片

采用先上传图片、再关联分段的方式，两个请求均需传 `namespace` 请求头。
URL 中的知识库 ID 和文档 ID 均为本平台 UUID。

先上传一张图片：

```bash
curl -X POST 'http://127.0.0.1:8000/api/knowledge-bases/{知识库ID}/documents/{文档ID}/segments/images/upload/' \
  -H 'namespace: project-a' \
  -F 'file=@/path/to/example.png'
```

接口接收 `multipart/form-data`，每次仅支持一个 `file`；多张图片需分别上传。
允许 jpg、jpeg、png、gif，并检查扩展名、MIME 类型和文件头，默认每张最多 2MiB。
成功返回 HTTP 201，例如：

```json
{
  "code": 0,
  "message": "图片上传成功",
  "data": {
    "id": "Dify文件ID",
    "name": "example.png",
    "size": 1024,
    "extension": "png",
    "mime_type": "image/png"
  }
}
```

取 `data.id`，调用原添加分段接口：

```http
POST /api/knowledge-bases/{知识库ID}/documents/{文档ID}/segments/
Content-Type: application/json
namespace: project-a

{
  "segments": [
    {
      "content": "图片说明文字",
      "attachment_ids": ["Dify文件ID"]
    }
  ]
}
```

支持批量添加分段，每个分段仍需提供非空 `content`。`attachment_ids` 为文件 ID
字符串数组，默认每段最多 10 个；不能传图片 URL 或本地路径。未传此字段的纯文本请求
保持原有行为。

也可以给已有分段添加、替换或清空图片：

```http
POST /api/knowledge-bases/{知识库ID}/documents/{文档ID}/segments/{分段ID}/
Content-Type: application/json
namespace: project-a

{
  "segment": {
    "attachment_ids": ["Dify文件ID"]
  }
}
```

更新时传入完整的附件 ID 列表；省略或传 `null` 表示保留原有图片，传 `[]` 表示清空。

服务使用现有 `DIFY_BASE_URL`、`DIFY_API_KEY`，调用 Dify 的
`POST /v1/datasets/pipeline/file-upload` 上传图片，不需要额外配置控制台登录凭据。
Dify 需提供该接口，并允许当前知识库 API Key 访问；绑定了特定知识库、不能访问此上传
接口的受限 Key 不适用。图片上传返回的 ID 必须与目标知识库属于同一 Dify 工作空间。
附件功能请使用 Dify 的高质量索引知识库；图片参与向量检索还需要 Dify 配置支持图片的
多模态嵌入模型。

部分 Dify 版本在批量创建分段时会遗漏附件关联，客户端会检查返回的附件列表并通过
更新分段接口补齐。如果分段已创建但图片关联失败，返回 HTTP 502，
`code` 为 `BACKEND_ATTACH_SEGMENT_IMAGES_FAILED`；`data.data` 保留已创建的分段，
`failed_segments` 包含失败分段的 `index`（从 0 开始）、`segment_id`、`attachment_ids`
和错误说明。请用其中的分段 ID 调用更新接口重试，避免再次创建重复分段。
若 Dify 的创建响应缺少可匹配的分段 ID，失败信息会要求先核对创建结果。

可通过环境变量调整限制，并与 Dify 的同名设置保持一致：

```env
SINGLE_CHUNK_ATTACHMENT_LIMIT=10
ATTACHMENT_IMAGE_FILE_SIZE_LIMIT=2
```

`ATTACHMENT_IMAGE_FILE_SIZE_LIMIT` 的单位为 MiB。该功能不需要数据库迁移。

## 预览分段中的图片

```http
GET /api/knowledge-bases/{知识库ID}/documents/{文档ID}/segments/{分段ID}/images/{Dify文件ID}/preview/?namespace=project-a
```

成功时直接返回图片二进制及对应的 `Content-Type`（如 `image/png`），
以 `Content-Disposition: inline` 展示图片；不返回 JSON，也不重定向到 Dify。
接口文档位于 `/api/docs/` 的 `chunks` 分组。

`namespace` 可以通过请求头传入，也可以使用同名查询参数；至少提供一种，同时提供时
请求头优先。查询参数方便浏览器的 `<img src>` 调用，不需要为图片额外构造自定义请求头。
服务会校验知识库与文档归属，并查询分段详情，只有目标分段中已关联的附件才能读取。
尚未关联到分段的上传文件不适用此接口；上传后、保存前可使用浏览器本地文件预览。

分段列表、单个分段详情、新增和更新响应中的 `attachments` 均新增 `preview_url`：

```json
{
  "id": "Dify文件ID",
  "name": "example.png",
  "source_url": "Dify原始签名地址",
  "preview_url": "/api/knowledge-bases/知识库ID/documents/文档ID/segments/分段ID/images/Dify文件ID/preview/?namespace=project-a"
}
```

响应层级保持原样：列表和新增结果在 `data.data[].attachments[]`，
详情和更新结果在 `data.data.attachments[]`。其余 Dify 字段仍保留。
前端应使用 `preview_url` 展示图片，例如 Vue：

```html
<img :src="attachment.preview_url" :alt="attachment.name" />
```

`preview_url` 是本站相对地址，已包含编码后的 `namespace`。
前后端不同域名部署时，请使用 API 服务域名补齐地址，如
`new URL(attachment.preview_url, apiOrigin).href`。

每次读取时，服务都会查询 Dify 获取当前附件和新的签名地址，再从配置的 Dify 文件
服务读取图片。默认使用 `DIFY_BASE_URL`；如果文件接口使用不同的内部可达地址，可配置：

```env
DIFY_FILES_BASE_URL=http://dify-api:5001
```

该地址为文件服务基地址，不包含 `/files` 路径；带网关前缀时可包含该前缀。
预览时不依赖浏览器访问 Dify 内网地址，也不需要把 Dify API Key 交给前端。
服务仅读取配置的文件服务，不跟随重定向，仅接受常用光栅图片类型，预览大小上限为 20MiB。

缺少 `namespace`、知识库或文档尚未同步时返回 HTTP 400；资源不存在、图片不属于当前
分段或已被删除时返回 HTTP 404；Dify 读取失败时返回 HTTP 502。错误响应为 JSON。
图片响应禁用缓存，删除附件后不会继续通过本接口返回旧图片。此功能无需数据库迁移。

## 批量启用、禁用和删除分段

```http
POST /api/knowledge-bases/{知识库ID}/documents/{文档ID}/segments/batch/
Content-Type: application/json
namespace: project-a

{
  "action": "disable",
  "segment_ids": [
    "01234567-89ab-cdef-0123-456789abcdef",
    "11234567-89ab-cdef-0123-456789abcdef"
  ]
}
```

`action` 支持 `enable`（启用）、`disable`（禁用）、`delete`（删除）。
三种操作均使用上述 POST 接口；知识库 ID 和文档 ID 是本平台 UUID，
`segment_ids` 是当前文档的 Dify 分段 UUID 字符串数组。
请求必须提供 `namespace` 请求头，且 `segment_ids` 非空、最多 100 个。
所有参数校验通过后才会开始操作；重复 UUID（含大小写差异）按首次出现顺序去重。

服务同步逐个处理分段，单个失败后继续处理后续分段，已成功的操作不会回滚。
启用、禁用前会查询当前状态；若已处于目标状态，返回成功并标记 `unchanged: true`，
避免重复更新。删除不存在或已被移除的分段会记录为失败。

- 全部成功：HTTP 200，`code` 为 `0`。
- 部分失败：HTTP 207，`code` 为 `BATCH_PARTIALLY_FAILED`。
- 全部失败：HTTP 502，`code` 为 `BATCH_FAILED`。

例如部分禁用失败：

```json
{
  "code": "BATCH_PARTIALLY_FAILED",
  "message": "部分分段操作失败",
  "data": {
    "action": "disable",
    "total": 2,
    "success_count": 1,
    "failure_count": 1,
    "results": [
      {
        "segment_id": "01234567-89ab-cdef-0123-456789abcdef",
        "success": true
      },
      {
        "segment_id": "11234567-89ab-cdef-0123-456789abcdef",
        "success": false,
        "code": "BACKEND_SEGMENT_OPERATION_FAILED",
        "message": "Segment not found",
        "backend_status": 404,
        "backend_code": "not_found"
      }
    ]
  }
}
```

`total`、成功和失败计数均按去重后的分段计算，`results` 保持对应顺序。
前端需检查 `failure_count` 或逐项的 `success`，不能仅凭 HTTP 为 2xx 判断全部成功；
重试时仅提交失败的分段 ID。后端 HTTP 错误会保留 `backend_status` 和可用的
`backend_code`；连接失败或超时则在 `message` 中说明。
请求参数错误返回 HTTP 400，知识库或文档不属于当前 namespace 时返回 HTTP 404。
该接口已加入 `/api/docs/` 的 `chunks` 分组，无需数据库迁移。

## 外部程序通过平台检索并调用 Ollama

完整示例：[examples/llmops_ollama_rag.py](examples/llmops_ollama_rag.py)。
外部程序调用 LLMOps Knowledge 检索，再调用 Ollama 生成回答；Dify 地址和知识库 API Key
由本服务端的 `DIFY_BASE_URL`、`DIFY_API_KEY` 管理，示例不需要这两个配置。
底层检索仍由本服务转发到 Dify。

已有接口可以直接使用：

```http
POST /api/knowledge-bases/{平台知识库ID}/retrieve/
Content-Type: application/json
namespace: project-a

{
  "query": "Ansible 如何安装？",
  "retrieval_model": {
    "search_method": "semantic_search",
    "reranking_enable": false,
    "top_k": 5,
    "score_threshold_enabled": false
  }
}
```

响应结构为 `{"code": 0, "message": "检索成功", "data": {"records": [...]}}`，
外部程序需从 `data.records` 读取命中片段，其余片段内容和来源结构沿用 Dify。
该接口目前使用 `namespace` 请求头区分项目；如果部署入口另有网关鉴权，调用时还需
按网关要求提供相应凭据，Dify API Key 始终保留在本服务端。

这里的 ID 是创建知识库或列表接口返回的 `id`，不是 Dify 的 `backend_dataset_id`。
如果目前只知道 Dify 知识库 ID，可先调用 `GET /api/knowledge-bases/`（同样提供
`namespace` 请求头），找到 `backend_dataset_id` 对应的记录，取它的 `id`。
直接在 Dify 创建、尚未在本服务登记的知识库，不能直接使用 Dify ID 调用此路由。

在仓库根目录配置环境变量并运行：

```bash
export LLMOPS_KNOWLEDGE_URL='http://127.0.0.1:8000'
export LLMOPS_NAMESPACE='project-a'
export LLMOPS_KNOWLEDGE_BASE_ID='替换为平台知识库UUID'
export OLLAMA_URL='http://127.0.0.1:11434'
export OLLAMA_MODEL='qwen2.5:0.5b'

.venv/bin/python examples/llmops_ollama_rag.py 'Ansible 如何安装？' --top-k 5
```

`LLMOPS_KNOWLEDGE_URL` 是平台服务根地址，不包含 `/api` 或 `/v1`；如果部署在网关路径下，
可包含该路径前缀。`OLLAMA_URL` 按实际可达地址配置，并确保模型已在 Ollama 中可用。
示例保留“检索 → 构建文本上下文 → Ollama 生成”的流程；无有效文本时直接返回提示，
不会调用 Ollama。打印的来源是检索命中文档及分数。

运行示例测试：

```bash
.venv/bin/python -m unittest discover -s examples -p 'test_*.py'
```

## 下一版本
1. 增加连接外部知识库功能；
2. 支持创建自定义知识库。
