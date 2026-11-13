from rest_framework.renderers import JSONRenderer


class ImagePreviewRenderer(JSONRenderer):
  """允许浏览器发送 image/* Accept；预览失败时仍返回 JSON 错误。"""
  media_type = "image/*"
  format = "image"

  def render(self, data, accepted_media_type=None, renderer_context=None):
    if renderer_context and renderer_context.get("response") is not None:
      renderer_context["response"]["Content-Type"] = "application/json"
    return super().render(data, accepted_media_type, renderer_context)
