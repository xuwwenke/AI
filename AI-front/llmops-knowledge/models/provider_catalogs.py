"""供应商预定义模型目录。

远端 /models 接口通常只返回模型 ID。上下文窗口、视觉、工具调用等能力
需要像 Dify 插件一样在本地维护元数据。
"""


DEEPSEEK_MODELS = [
  {
    "model": "deepseek-flash",
    "label": "deepseek-flash",
    "model_type": "llm",
    "features": ["agent-thought", "tool-call", "multi-tool-call", "vision"],
    "model_properties": {"mode": "chat", "context_size": 1048576},
  },
  {
    "model": "deepseek-v4-flash",
    "label": "deepseek-v4-flash",
    "model_type": "llm",
    "features": ["agent-thought", "tool-call", "multi-tool-call", "vision"],
    "model_properties": {"mode": "chat", "context_size": 1048576},
  },
  {
    "model": "deepseek-v4-flash-vision-exp",
    "label": "deepseek-v4-flash-vision-exp",
    "model_type": "llm",
    "features": ["agent-thought", "tool-call", "multi-tool-call", "vision"],
    "model_properties": {"mode": "chat", "context_size": 1048576},
  },
  {
    "model": "deepseek-v4-pro",
    "label": "deepseek-v4-pro",
    "model_type": "llm",
    "features": ["agent-thought", "tool-call", "multi-tool-call"],
    "model_properties": {"mode": "chat", "context_size": 1048576},
  },
]


PROVIDER_CATALOGS = {
  "deepseek": DEEPSEEK_MODELS,
}

PROVIDER_METADATA = {
  "deepseek": {
    "label": {"zh_Hans": "深度求索", "en_US": "DeepSeek"},
    "supported_model_types": ["llm"],
  },
}


def normalize_provider_name(provider):
  """兼容 deepseek 和 langgenius/deepseek/deepseek 两类标识。"""
  return provider.strip("/").split("/")[-1].lower().replace("_", "-")


def get_provider_catalog(provider, model_type):
  catalog = PROVIDER_CATALOGS.get(normalize_provider_name(provider), [])
  return [item.copy() for item in catalog if item["model_type"] == model_type]


def get_provider_metadata(provider):
  normalized_name = normalize_provider_name(provider)
  return PROVIDER_METADATA.get(normalized_name, {
    "label": {"zh_Hans": normalized_name, "en_US": normalized_name},
  })


def build_model_tags(model):
  tags = [model["model_type"].upper()]
  properties = model.get("model_properties") or {}
  mode = properties.get("mode")
  if mode:
    tags.append(str(mode).upper())

  context_size = properties.get("context_size")
  if isinstance(context_size, int) and context_size > 0:
    if context_size >= 1000:
      tags.append(f"{context_size // 1000}K")
    else:
      tags.append(str(context_size))

  feature_tags = {
    "vision": "VISION",
    "tool-call": "TOOL CALL",
    "agent-thought": "REASONING",
  }
  for feature in model.get("features") or []:
    tag = feature_tags.get(feature)
    if tag and tag not in tags:
      tags.append(tag)
  return tags
