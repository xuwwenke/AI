import httpRequest from "@/utils/httpRequest";

const product = "/system/sys/senior/license/";

const auth = "/system-adapter/sys/license/";

// 授权管理
export default {
  // 分页查询授权信息
  getList(o) {
    return httpRequest({
      url: auth + "page",
      method: "post",
      data: o
    });
  },
  // 增加授权信息
  insert(o) {
    return httpRequest({
      url: auth + "add",
      method: "post",
      data: o
    });
  },
  // 修改授权信息
  update(o) {
    return httpRequest({
      url: auth + "update",
      method: "put",
      data: o
    });
  },
  // 下载授权证书
  getDownloadLic(id) {
    return window.globalConfig.defaultProxyPath + auth + "downloadLic/" + id;
  },
  // 获取产品信息，校验产品授权
  getProductInfo() {
    return httpRequest({
      url: product + "verify/product",
      method: "get"
    });
  },
  // 获取设备信息
  getServerInfos() {
    return httpRequest({
      url: product + "getServerInfos",
      method: "get"
    });
  },
  // 上传授权码
  uploadFile(o) {
    return httpRequest({
      url: product + "activation",
      method: "post",
      data: o
    });
  },
};
