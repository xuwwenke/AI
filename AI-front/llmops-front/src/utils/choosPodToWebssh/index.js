import router from "@/router";
import store from "@/store";
import Vue from "vue";
import * as k8s_api from "@/api/app/k8s";
import Main from "./main";
import moment from "moment";
import "./index.scss";

let PodsConstructor = Vue.extend(Main);
const v = new Vue();

class ChoosePodDialog {
  constructor({ pods, namespace, openMethod }) {
    this.pods = pods;
    this.namespace = namespace;
    this.openMethod = openMethod;
    this.instance = null;
  }

  showPodListDialog() {
    const that = this;
    this.instance = new PodsConstructor({
      data: {
        list: that.pods,
        namespace: that.namespace,
        openMethod: that.openMethod,
        visible: true
      },
      methods: {
        close() {
          document.body.removeChild(that.instance.$el);
          that.instance = null;
        },
        openWebssh(...arg) {
          const fn = that.openMethod || that.openWebssh;
          fn(...arg);
          this.visible = false;
          this.close.bind(that.instance);
        }
      }
    });

    this.instance.$mount();
    document.body.appendChild(this.instance.$el);
  }

  openWebssh(type, namespace, podName) {
    const clusterName = (store.state.project.currentCluster && store.state.project.currentCluster.name) || 'default';
    const param = {
      path: `/webssh/${type}/${namespace}/${podName}`,
      query: {
        proName: namespace,
        clusterName
      }
    };

    const { href } = router.resolve(param);
    window.open(href, "_blank");
  }
}

function formatPod(list) {
  return list.map(i => {
    const { metadata, status } = i;
    const deleting =
      metadata.deletionGracePeriodSeconds || metadata.deletionTimestamp;
    const phase = deleting ? "Deleting" : status.phase;
    console.log(phase)
    return {
      name: metadata.name,
      phase,
      creationTimestamp: moment(metadata.creationTimestamp).fromNow(),
      podIP: status.podIP,
      hostIP: status.hostIP,
      reason: status.reason
    };
  });
}

export default async function (
  { serviceName, namespace, type, openMethod, labelSelector },
  showDialog
) {
  const loading = v.$loading({ target: "body" });
  try {
    const selector = labelSelector || `app=debug-${serviceName}`;
    const res = await k8s_api.getContainerGroup(namespace, undefined, selector);
    const pods = formatPod(res.items);

    const choosePodDialog = new ChoosePodDialog({
      pods,
      namespace,
      openMethod
    });

    if (pods.length === 0) {
      // 提示用户无可用 Pod
      v.$message.warning(`命名空间 ${namespace} 中没有找到可用的 Pod`);
      return;
    }

    if (pods.length === 1 && !showDialog) {
      const fn = choosePodDialog.openMethod || choosePodDialog.openWebssh.bind(choosePodDialog);
      return fn(type, namespace, pods[0].name);
    }

    choosePodDialog.showPodListDialog();
  } catch (error) {
    console.log(error);
  } finally {
    loading.close();
  }
}
