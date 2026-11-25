from app.tools.diagnosis_tools import get_deployment_status


def main():
  result = get_deployment_status.invoke({
    "namespace": "ailabx",
    "deployment_name": "kubeflow-dashboard-schedule",
  })

  print(result)


if __name__ == "__main__":
  main()