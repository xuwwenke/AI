import httpRequest from "@/utils/httpRequest";
import store from "@/store";

const preURL = "/llmops-service/api/k8s-file";
const getNamespace = () => {
  return (store.state.project && store.state.project.currentProject && store.state.project.currentProject.name) || 'default'
};
const getClusterName = () => {
  return (store.state.project && store.state.project.currentCluster && store.state.project.currentCluster.name) || ''
};

export default {
  executeCommand(podName, cmd) {
    return httpRequest({
      method: "get",
      url: `${preURL}/apps/v1/namespaces/${getNamespace()}/pods/${podName}/exec`,
      params: { cmd }
    });
  },
  getFiles(podName, dirPath, showHidden = false) {
    return httpRequest({
      method: "get",
      url: `${preURL}/apps/v1/namespaces/${getNamespace()}/pods/${podName}/list`,
      params: { dirPath, showHidden }
    });
  },
  getFileContent(podName, filePath) {
    return httpRequest({
      method: "get",
      url: `${preURL}/apps/v1/namespaces/${getNamespace()}/pods/${podName}/file`,
      params: { filePath }
    });
  },
  downloadFile(podName, filePath) {
    return httpRequest({
      method: "get",
      url: `${preURL}/apps/v1/namespaces/${getNamespace()}/pods/${podName}/download`,
      params: { path: filePath, clusterName: getClusterName() },
      responseType: "blob",
      _fullResData: true
    });
  },
  downloadFromUrl(podName, url, targetPath) {
    return httpRequest({
      method: "post",
      url: `/llmops-service/api/k8s-pod-download/namespaces/${getNamespace()}/pods/${podName}/download-from-url`,
      data: { url, targetPath },
      headers: { clusterName: getClusterName() }
    });
  }
};
