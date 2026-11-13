import request from '@/utils/httpRequest'
import store from '@/store'

const preURL = '/llmops-service/k8s-api'

function getNs() {
  return (store.state.project && store.state.project.currentProject && store.state.project.currentProject.name) || 'default'
}

export function getStorageList(params) {
  return request({
    url: `${preURL}/api/v1/namespaces/${getNs()}/persistentvolumeclaims`,
    method: 'get',
    params
  })
}

export function getStorageDetail(name) {
  return request({
    url: `${preURL}/api/v1/namespaces/${getNs()}/persistentvolumeclaims/${name}`,
    method: 'get'
  })
}

export function createStorageVolume(data) {
  return request({
    url: `${preURL}/api/v1/namespaces/${getNs()}/persistentvolumeclaims`,
    method: 'post',
    data
  })
}

export function deleteStorageVolume(name) {
  return request({
    url: `${preURL}/api/v1/namespaces/${getNs()}/persistentvolumeclaims/${name}`,
    method: 'delete'
  })
}

export function getStorageOverview() {
  return request({
    url: '/llmops-service/api/project/storage-overview',
    method: 'get',
    params: { namespace: getNs() }
  })
}

export function getSnapshotList(pvcName) {
  return request({
    baseURL: '',
    url: '/pvcviewer/snapshot/list',
    method: 'get',
    params: { namespace: getNs(), pvc_name: pvcName }
  })
}

export function deleteSnapshot(data) {
  return request({
    baseURL: '',
    url: '/pvcviewer/snapshot',
    method: 'delete',
    data: { namespace: getNs(), pvc_name: data.pvc_name, snapshot_id: data.snapshot_id }
  })
}

export function restoreSnapshot(data) {
  return request({
    baseURL: '',
    url: '/pvcviewer/restore',
    method: 'post',
    data: { namespace: getNs(), pvc_name: data.pvc_name, snapshot_id: data.snapshot_id, source_pvc_name: data.source_pvc_name }
  })
}
