
from app.infrastructure.prometheus_client import (
  get_prometheus_client,
)

def main():
  prometheus = get_prometheus_client()
  # 检查 Prometheus 版本
  info = prometheus.health_check()
  print("Prometheus 连接成功")
  print(info)
  # 测试 PromQL
  result = prometheus.query("up")
  print("\n监控目标状态：")
  for item in result["result"]:
    print(
      item["metric"].get("job"),
      item["value"][1],
    )


if __name__ == "__main__":
  main()
