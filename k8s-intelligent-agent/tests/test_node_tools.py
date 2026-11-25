
from pprint import pprint

from app.tools.node_tools import (
  get_cluster_resource_summary,
  list_node_resource_details,
)


def test_cluster_summary_tool():
  print("\n========== 集群资源汇总 ==========")
  result = get_cluster_resource_summary.invoke({})
  pprint(result)


def test_node_details_tool():
  print("\n========== 节点资源明细 ==========")
  result = list_node_resource_details.invoke({})
  pprint(result)

if __name__ == "__main__":
  test_cluster_summary_tool()
  test_node_details_tool()
