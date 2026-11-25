from app.tools.diagnosis_tools import get_diagnosis_service


def main():
    service = get_diagnosis_service()

    report = service.diagnose_deployment(
        namespace="ailabx",
        deployment_name="kubeflow-dashboard-schedule",
    )

    print("\n========== Deployment ==========")

    print(
        report.deployment.model_dump_json(
            indent=2
        )
    )

    print("\n========== Pod Diagnosis ==========")

    for evidence in report.pods:
        print(f"\nPod: {evidence.pod.name}")
        print(f"Status: {evidence.pod.status}")
        print(f"Restart Count: {evidence.pod.restart_count}")

        print(f"Events: {len(evidence.events)}")

        for event in evidence.events:
            print(
                f"  [{event.get('type')}] "
                f"{event.get('reason')}: "
                f"{event.get('message')}"
            )

        print(
            f"Previous Logs: "
            f"{len(evidence.previous_logs)} containers"
        )

        for container, logs in evidence.previous_logs.items():
            print(f"\n--- Container: {container} ---")
            print(logs[:3000])

        if evidence.collection_errors:
            print("\nCollection Errors:")

            for error in evidence.collection_errors:
                print(f"  - {error}")


if __name__ == "__main__":
    main()