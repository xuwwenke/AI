import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/senior";
// 版本管理
export default {
  // 获取版本列表
  getVersionList(o) {
    return httpRequest({
      url: url + "/version/list",
      method: "get",
      params: o
    });
  }
};
