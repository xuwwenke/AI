
import json

from app.services.node_resource_service import (
  NodeResourceService,
)


def main():
  service = NodeResourceService()
  # 1. 获取节点资源
  nodes = service.list_node_resources()
  print("\n========== 节点资源 ==========\n")
  for node in nodes:
    print(
      json.dumps(
        node.model_dump(),
        ensure_ascii=False,
        indent=2,
      )
    )
  # 2. 获取集群汇总
  summary = service.get_cluster_summary()
  print("\n========== 集群资源汇总 ==========\n")
  print(
    json.dumps(
      summary.model_dump(),
      ensure_ascii=False,
      indent=2,
    )
  )


if __name__ == "__main__":
  main()
