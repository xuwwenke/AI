
from functools import lru_cache

from langchain_core.tools import tool

from app.services.node_resource_service import NodeResourceService


@lru_cache(maxsize=1)
def get_node_resource_service() -> NodeResourceService:
    return NodeResourceService()


@tool
def get_cluster_resource_summary() -> dict:
    """
    获取 Kubernetes 集群整体资源汇总。

    返回节点总数、Ready 节点数、未就绪节点数、
    CPU 总量、已使用 CPU、CPU 使用率、
    内存总量、已使用内存、内存使用率。

    注意：CPU 和内存使用率只统计有 Prometheus
    监控指标的节点，不代表所有节点的完整使用率。

    用户询问集群整体资源情况时调用。
    """
    service = get_node_resource_service()

    summary = service.get_cluster_summary()
    result = summary.model_dump(mode="json")

    # 明确区分 Ready 和 NotReady 数量
    result["not_ready_nodes"] = (
        result["total_nodes"] - result["ready_nodes"]
    )

    return result


@tool
def list_node_resource_details() -> dict:
    """
    获取 Kubernetes 集群所有节点的资源使用明细。

    返回所有节点的 CPU、内存使用率，
    同时在 Python 中计算监控覆盖率和最高 CPU 使用率节点。

    没有监控指标的节点，其使用率为 null，
    不能把 null 当成 0。

    用户询问节点资源明细、CPU 使用率排名、
    哪个节点负载最高时调用。
    """
    service = get_node_resource_service()

    nodes = service.list_node_resources()

    # Pydantic 对象转换成字典
    node_data = [
        node.model_dump(mode="json")
        for node in nodes
    ]

    # 只统计有有效 CPU 指标的节点
    monitored_cpu_nodes = [
        node
        for node in node_data
        if (
            node["cpu_metric_available"]
            and node["cpu_usage_percent"] is not None
        )
    ]

    # 只统计有有效内存指标的节点
    monitored_memory_nodes = [
        node
        for node in node_data
        if (
            node["memory_metric_available"]
            and node["memory_usage_percent"] is not None
        )
    ]

    # Python 负责排序，不让大模型计算
    cpu_rank = sorted(
        monitored_cpu_nodes,
        key=lambda node: node["cpu_usage_percent"],
        reverse=True,
    )

    memory_rank = sorted(
        monitored_memory_nodes,
        key=lambda node: node["memory_usage_percent"],
        reverse=True,
    )

    return {
        "total_nodes": len(node_data),

        "monitored_cpu_nodes": len(monitored_cpu_nodes),
        "unmonitored_cpu_nodes": (
            len(node_data) - len(monitored_cpu_nodes)
        ),

        "monitored_memory_nodes": len(monitored_memory_nodes),
        "unmonitored_memory_nodes": (
            len(node_data) - len(monitored_memory_nodes)
        ),

        "highest_cpu_node": cpu_rank[0] if cpu_rank else None,
        "highest_memory_node": memory_rank[0] if memory_rank else None,

        "cpu_rank": [
            {
                "name": node["name"],
                "cpu_usage_percent": node["cpu_usage_percent"],
            }
            for node in cpu_rank
        ],

        "memory_rank": [
            {
                "name": node["name"],
                "memory_usage_percent": node["memory_usage_percent"],
            }
            for node in memory_rank
        ],

        "nodes": node_data,
    }


node_tools = [
    get_cluster_resource_summary,
    list_node_resource_details,
]
