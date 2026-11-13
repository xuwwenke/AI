import request from '@/utils/httpRequest'
import store from "@/store";

const preURL = "/llmops-service/k8s-api";


const getClusterName = () => {
  return store.state.project.currentCluster.name;
};

/**
 * 查询容器组
 * @param {string} namespaces 命名空间
 * @param {string} clusterName 集群名称（可选，默认从 store 获取）
 * @param {string} labelSelector 标签选择器
 */
export function getContainerGroup(namespaces, clusterName, labelSelector) {
  return request({
    url: `${preURL}/api/v1/namespaces/${namespaces}/pods?labelSelector=${labelSelector}`,
    method: 'get',
    headers: {
      clusterName: clusterName || getClusterName()
    }
  });
}

export function createPod(data) {
  return request({
    baseURL: '',
    url: `/pvcviewer/create`,
    method: 'post',
    data
  });
}

export function deletePod(data) {
  return request({
    baseURL: '',
    url: `/pvcviewer/delete`,
    method: 'post',
    data
  });
}

export function getPvcViewerOverview() {
  return request({
    baseURL: '',
    url: `/pvcviewer/overview`,
    method: 'get'
  });
}

export function backupPvc(data) {
  return request({
    baseURL: '',
    url: `/pvcviewer/backup`,
    method: 'post',
    data
  });
}

