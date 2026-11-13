
from abc import ABC, abstractmethod


class SegmentAttachmentError(RuntimeError):
  """分段已经创建，但部分图片关联失败，保留创建结果以便更新重试。"""

  def __init__(self, result, failed_segments):
    super().__init__("分段已创建，但图片关联失败，请使用更新分段接口重试")
    self.result = result
    self.failed_segments = failed_segments


class SegmentImageNotFoundError(RuntimeError):
  """请求的图片不在目标分段的附件中。"""


class BaseKnowledgeBackend(ABC):
  @abstractmethod
  def create_dataset(self, name: str, description: str = "", **kwargs) -> dict:
    """创建知识库，返回后端知识库信息（至少包含 id 字段）"""

  @abstractmethod
  def get_dataset(self, dataset_id: str) -> dict:
    """获取知识库详情"""

  @abstractmethod
  def update_dataset(self, dataset_id: str, name: str = None, description: str = None,
                     permission: str = None, **kwargs) -> dict:
    """部分更新知识库名称、描述、模型、权限和检索配置。"""

  @abstractmethod
  def delete_dataset(self, dataset_id: str) -> dict:
    """删除知识库"""

  @abstractmethod
  def upload_document(self, dataset_id: str, uploaded_file, indexing_technique: str = "economy",
                      embedding_model: str = None, embedding_model_provider: str = None, **kwargs) -> dict:
    """上传文档到知识库（通过文件）。额外参数通过 kwargs 透传（doc_form、doc_language、process_rule、retrieval_model）"""

  @abstractmethod
  def create_document_from_text(self, dataset_id: str, name: str, text: str, **kwargs) -> dict:
    """通过文本创建文档。额外参数通过 kwargs 透传（indexing_technique、doc_form、doc_language、process_rule、retrieval_model、embedding_model、embedding_model_provider）"""

  @abstractmethod
  def update_document_by_file(self, dataset_id: str, document_id: str, uploaded_file, **kwargs) -> dict:
    """通过文件更新文档（重新上传文件覆盖原文档）。额外参数通过 kwargs 透传（doc_form、doc_language、process_rule、retrieval_model、embedding_model、embedding_model_provider）"""

  @abstractmethod
  def retrieve(self, dataset_id: str, query: str, retrieval_model: dict = None) -> dict:
    """从知识库检索分段"""

  @abstractmethod
  def list_documents(self, dataset_id: str, page: int = 1, limit: int = 20) -> dict:
    """获取知识库下的文档列表"""

  @abstractmethod
  def get_document(self, dataset_id: str, document_id: str) -> dict:
    """获取文档详情"""

  @abstractmethod
  def get_document_indexing_status(self, dataset_id: str, batch: str) -> dict:
    """获取创建或更新文档批次的索引进度"""

  @abstractmethod
  def delete_document(self, dataset_id: str, document_id: str) -> dict:
    """删除文档"""

  @abstractmethod
  def download_document(self, dataset_id: str, document_id: str) -> dict:
    """获取文档的下载 URL，返回包含 url 字段的字典"""

  @abstractmethod
  def update_documents_status(self, dataset_id: str, action: str, document_ids: list) -> dict:
    """批量更新文档状态。action: enable/disable/archive/un_archive"""

  @abstractmethod
  def get_document_segments(self, dataset_id: str, document_id: str, page: int = 1,
                            limit: int = 20, status: list = None, keyword: str = None) -> dict:
    """获取文档下的分段列表"""

  @abstractmethod
  def get_segment_detail(self, dataset_id: str, document_id: str, segment_id: str) -> dict:
    """获取单个分段详情"""

  @abstractmethod
  def update_segment(self, dataset_id: str, document_id: str, segment_id: str, segment: dict) -> dict:
    """更新单个分段"""

  @abstractmethod
  def delete_segment(self, dataset_id: str, document_id: str, segment_id: str) -> dict:
    """删除单个分段"""

  @abstractmethod
  def add_segments(self, dataset_id: str, document_id: str, segments: list) -> dict:
    """向文档添加分段"""

  @abstractmethod
  def upload_segment_image(self, uploaded_file) -> dict:
    """上传分段图片，返回后端文件信息（至少包含 id 字段）。"""

  @abstractmethod
  def get_segment_image(self, dataset_id: str, document_id: str, segment_id: str, file_id: str) -> dict:
    """校验分段附件归属并读取图片，返回 content、content_type 和 name。"""

  @abstractmethod
  def create_metadata(self, dataset_id: str, name: str, type: str) -> dict:
    """创建元数据字段。type: string/number/time"""

  @abstractmethod
  def update_metadata(self, dataset_id: str, metadata_id: str, name: str) -> dict:
    """更新（重命名）元数据字段"""

  @abstractmethod
  def delete_metadata(self, dataset_id: str, metadata_id: str) -> dict:
    """删除元数据字段"""

  @abstractmethod
  def get_metadata(self, dataset_id: str) -> dict:
    """获取知识库下的元数据字段列表"""
    
  @abstractmethod
  def get_built_in_metadata(self, dataset_id: str) -> dict:
    """获取内置的元数据字段列表"""

  @abstractmethod
  def update_built_in_metadata_status(self, dataset_id: str, action: str) -> dict:
    """启用/停用内置元数据字段。action: enable/disable"""

  @abstractmethod
  def update_documents_metadata(self, dataset_id: str, operation_data: list) -> dict:
    """批量更新文档元数据。operation_data 每个元素包含 document_id、metadata_list、partial_update"""

  @abstractmethod
  def create_tag(self, name: str) -> dict:
    """创建知识库标签"""

  @abstractmethod
  def list_tags(self) -> list:
    """获取知识库标签列表"""

  @abstractmethod
  def update_tag(self, tag_id: str, name: str) -> dict:
    """重命名知识库标签"""

  @abstractmethod
  def delete_tag(self, tag_id: str) -> dict:
    """删除知识库标签"""

  @abstractmethod
  def bind_tags(self, tag_ids: list, target_id: str) -> dict:
    """绑定标签到知识库"""

  @abstractmethod
  def unbind_tags(self, target_id: str, tag_ids: list = None, tag_id: str = None) -> dict:
    """解除标签与知识库的绑定"""

  @abstractmethod
  def get_dataset_tags(self, dataset_id: str) -> dict:
    """获取绑定到知识库的标签列表"""

  @abstractmethod
  def get_models(self, model_type: str) -> dict:
    """获取指定类型的可用模型列表。model_type: text-embedding/rerank/llm/tts/speech2text/moderation"""
