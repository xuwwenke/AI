import httpRequest from "@/utils/httpRequest";
import qs from "qs";

const url = "/system/sys/security/log";

export default {
  // 获取登录日志
  getLoginLogs(o) {
    return httpRequest({
      url: url + "/login",
      params: o
    });
  },
  // 获取我的登录日志
  getLoginLogsMy(o) {
    return httpRequest({
      url: url + "/login/my",
      params: o
    });
  },
  // 导出登录日志
  exportLoginLogs(o) {
    return window.globalConfig.defaultProxyPath + url + "/login/export" + qs.stringify(o, { addQueryPrefix: true });
  },
  // 获取操作日志
  getOptLogs(o) {
    return httpRequest({
      url: url + "/opt",
      params: o
    });
  },
  // 获取我的操作日志
  getOptLogsMy(o) {
    return httpRequest({
      url: url + "/opt/my",
      params: o
    });
  },
  // 导出操作日志
  exportOptLogs(o) {
    return window.globalConfig.defaultProxyPath + url + "/export" + qs.stringify(o, { addQueryPrefix: true });
  }
};
