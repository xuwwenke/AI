from datetime import datetime

from models import BackupTask


class BackupTaskDAO:

    @staticmethod
    def create_task(
        db,
        namespace,
        pvc_name,
        job_name
    ):

        task = BackupTask(
            namespace_name=namespace,
            pvc_name=pvc_name,
            job_name=job_name,
            task_type="backup",
            status="Running",
            create_time=datetime.now()
        )

        db.add(task)
        db.commit()
        db.refresh(task)

        return task

    @staticmethod
    def get_running_task_by_pvc(
        db,
        namespace,
        pvc_name
    ):
        return (
            db.query(BackupTask)
            .filter(
                BackupTask.namespace_name == namespace,
                BackupTask.pvc_name == pvc_name,
                BackupTask.status == "Running"
            )
            .first()
        )

    @staticmethod
    def update_success(
        db,
        task
    ):
        task.status = "Succeeded"
        task.finish_time = datetime.now()
        db.commit()

    @staticmethod
    def update_failed(
        db,
        task
    ):
        task.status = "Failed"
        task.finish_time = datetime.now()
        db.commit()