# from app.tools.diagnosis_tools import get_pod_logs


# def main():
#   result = get_pod_logs.invoke({
#     "namespace": "ailabx",
#     "pod_name": "kubeflow-dashboard-schedule-798d586644-pqm8f",
#     "previous": True,
#     "tail_lines": 100,
#   })

#   print(result)


# if __name__ == "__main__":
#   main()


import json

from app.tools.diagnosis_tools import diagnose_deployment


def main():
    # StructuredTool.invoke() 是 LangChain 工具的标准调用方式
    result = diagnose_deployment.invoke(
        {
            "namespace": "ailabx",
            "deployment_name": "kubeflow-dashboard-schedule",
        }
    )

    print("\n========== Tool Result ==========")

    # Tool 返回的是 JSON 字符串
    data = json.loads(result)

    print(json.dumps(
        data,
        ensure_ascii=False,
        indent=2,
    ))

    if "error" in data:
        print("\nTool execution failed!")
        return

    print("\n========== Summary ==========")
    print("Namespace:", data["namespace"])
    print("Workload:", data["workload_name"])
    print("Health:", data["health_status"])
    print("Risk:", data["risk_status"])
    print("Findings:", len(data["findings"]))


if __name__ == "__main__":
    main()
