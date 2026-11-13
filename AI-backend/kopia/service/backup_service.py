from dao.backup_task_dao import BackupTaskDAO
from dao.snapshot_dao import SnapshotDAO


class BackupService:

    @staticmethod
    def create_backup_task(
        db,
        namespace,
        pvc_name,
        job_name
    ):
        return BackupTaskDAO.create_task(
            db,
            namespace,
            pvc_name,
            job_name
        )

    @staticmethod
    def check_running_backup(
        db,
        namespace,
        pvc_name
    ):
        return BackupTaskDAO.get_running_task_by_pvc(
            db,
            namespace,
            pvc_name
        )

    @staticmethod
    def restore_success(
        db,
        task,
        snapshot_id
    ):

        BackupTaskDAO.update_success(
            db,
            task
        )

    @staticmethod
    def backup_success(
        db,
        task,
        snapshot_id
    ):

        SnapshotDAO.create_snapshot(
            db,
            task.id,
            task.namespace_name,
            task.pvc_name,
            snapshot_id
        )

        BackupTaskDAO.update_success(
            db,
            task
        )

    @staticmethod
    def backup_failed(
        db,
        task
    ):

        BackupTaskDAO.update_failed(
            db,
            task
        )

    @staticmethod
    def task_success(
        db,
        task
    ):
        BackupTaskDAO.update_success(
            db,
            task
        )