
import json

from app.infrastructure.k8s_client import get_k8s_client
from app.services.diagnosis_service import DiagnosisService
from app.services.diagnosis_rules import DiagnosisRuleEngine


def main():
    clients = get_k8s_client()

    service = DiagnosisService(
        core_v1_api=clients.core_v1,
        app_v1_api=clients.app_v1,
    )

    # 第一步：采集 Kubernetes 诊断证据
    report = service.diagnose_deployment(
        namespace="ailabx",
        deployment_name="kubeflow-dashboard-schedule",
    )

    # 第二步：执行规则诊断
    engine = DiagnosisRuleEngine()
    result = engine.diagnose(report)

    # 第三步：输出诊断结果
    print("\n========== Diagnosis Result ==========")
    print(json.dumps(
        result.model_dump(mode="json"),
        ensure_ascii=False,
        indent=2,
    ))

    print("\n========== Findings Summary ==========")
    print(f"Workload: {result.workload_name}")
    print(f"Health Status: {result.health_status}")
    print(f"Risk Status: {result.risk_status}")
    print(f"Total Findings: {len(result.findings)}")

    for index, finding in enumerate(result.findings, 1):
        print(f"\n[{index}] {finding.title}")
        print(f"Code: {finding.code}")
        print(f"Severity: {finding.severity}")
        print(f"Category: {finding.category}")
        print(f"Message: {finding.message}")

        for evidence in finding.evidence:
            print(f"  Evidence: {evidence}")

        for recommendation in finding.recommendations:
            print(f"  Recommendation: {recommendation}")


if __name__ == "__main__":
    main()
