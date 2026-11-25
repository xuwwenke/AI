from app.services.kubernetes_service import KubernetesService

def main():
  k8s_service = KubernetesService()
  nodes = k8s_service.list_node()
  print(nodes)
if __name__ == "__main__":
  main()