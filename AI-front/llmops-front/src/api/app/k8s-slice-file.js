import httpRequest from "@/utils/httpRequest";
import store from "@/store";

const preURL = "/llmops-service/api/k8s-file";
const getNamespace = () => {
  return (store.state.project && store.state.project.currentProject && store.state.project.currentProject.name) || 'default'
};

const k8s_slice_file = {
  begin(podName, o, cancel) {
    return httpRequest({
      headers: { "Content-Type": "application/json" },
      method: "post",
      url: `${preURL}/apps/v1/namespaces/${getNamespace()}/pods/${podName}/chunk/begin`,
      data: o,
      cancel
    });
  },
  uploadSlice(podName, o, cancel) {
    return httpRequest({
      headers: { "Content-Type": "multipart/form-data" },
      method: "post",
      url: `${preURL}/apps/v1/namespaces/${getNamespace()}/pods/${podName}/chunk/upload`,
      data: o,
      cancel
    });
  },
  end(podName, o, cancel) {
    return httpRequest({
      headers: { "Content-Type": "application/json" },
      method: "post",
      url: `${preURL}/apps/v1/namespaces/${getNamespace()}/pods/${podName}/chunk/end`,
      data: o,
      cancel
    });
  }
};

export default k8s_slice_file;
