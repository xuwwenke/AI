from dao.snapshot_dao import SnapshotDAO


class SnapshotService:

    @staticmethod
    def get_snapshot_count(
        db,
        namespace
    ):
        return SnapshotDAO.count_by_namespace(
            db,
            namespace
        )

    @staticmethod
    def get_snapshot_list_by_pvc(
        db,
        namespace,
        pvc_name
    ):
        return SnapshotDAO.list_by_pvc(
            db,
            namespace,
            pvc_name
        )

    @staticmethod
    def get_snapshot_list_by_namespace(
        db,
        namespace
    ):
        return SnapshotDAO.list_by_namespace(
            db,
            namespace
        )

    @staticmethod
    def get_snapshot_list_by_namespace_with_task(
        db,
        namespace
    ):
        return SnapshotDAO.list_by_namespace_with_task(
            db,
            namespace
        )

    @staticmethod
    def find_snapshot_by_id(
        db,
        snapshot_id
    ):
        return SnapshotDAO.find_by_snapshot_id(
            db,
            snapshot_id
        )

    @staticmethod
    def get_snapshot_list(
        db
    ):
        return SnapshotDAO.list_all(
            db
        )

    @staticmethod
    def delete_snapshot(
        db,
        snapshot_id
    ):
        return SnapshotDAO.delete_by_snapshot_id(
            db,
            snapshot_id
        )