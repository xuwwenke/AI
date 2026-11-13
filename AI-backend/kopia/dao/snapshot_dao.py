from datetime import datetime

from models import Snapshot


class SnapshotDAO:

    @staticmethod
    def create_snapshot(
        db,
        task_id,
        namespace,
        pvc_name,
        snapshot_id
    ):
        snapshot = Snapshot(
            task_id=task_id,
            namespace_name=namespace,
            pvc_name=pvc_name,
            snapshot_id=snapshot_id,
            create_time=datetime.now()
        )

        db.add(snapshot)

        db.commit()

    @staticmethod
    def count_by_namespace(
        db,
        namespace
    ):

        return (
            db.query(Snapshot)
            .filter(
                Snapshot.namespace_name == namespace
            )
            .count()
        )

    @staticmethod
    def list_by_pvc(
        db,
        namespace,
        pvc_name
    ):

        return (
            db.query(Snapshot)
            .filter(
                Snapshot.namespace_name == namespace,
                Snapshot.pvc_name == pvc_name
            )
            .all()
        )
    
    @staticmethod
    def list_by_namespace(
        db,
        namespace
    ):

        return (
            db.query(Snapshot)
            .filter(
                Snapshot.namespace_name == namespace
            )
            .all()
        )

    @staticmethod
    def list_by_namespace_with_task(
        db,
        namespace
    ):
        from models import BackupTask
        
        return (
            db.query(Snapshot, BackupTask)
            .outerjoin(
                BackupTask,
                Snapshot.task_id == BackupTask.id
            )
            .filter(
                Snapshot.namespace_name == namespace
            )
            .all()
        )

    @staticmethod
    def find_by_snapshot_id(
        db,
        snapshot_id
    ):
        return (
            db.query(Snapshot)
            .filter(
                Snapshot.snapshot_id == snapshot_id
            )
            .first()
        )

    @staticmethod
    def list_all(
        db
    ):
        return (
            db.query(Snapshot)
            .all()
        )

    @staticmethod
    def delete_by_snapshot_id(
        db,
        snapshot_id
    ):
        count = (
            db.query(Snapshot)
            .filter(
                Snapshot.snapshot_id == snapshot_id
            )
            .delete()
        )
        db.commit()
        return count