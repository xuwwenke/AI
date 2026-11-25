
from app.infrastructure.k8s_client import get_k8s_client


def main():
  k8s = get_k8s_client()
  nodes = k8s.core_v1.list_node()
  print(f"集群节点数量: {len(nodes.items)}")
  for node in nodes.items:
    print(
      f"节点名称: {node.metadata.name}, "
      f"IP: {node.status.addresses[0].address if node.status.addresses else 'N/A'}"
    )
    conditions = node.status.conditions or []
    for condition in conditions:
      if condition.type == "Ready":
        print(f"节点状态: {condition.status}")


if __name__ == "__main__":
  main()
