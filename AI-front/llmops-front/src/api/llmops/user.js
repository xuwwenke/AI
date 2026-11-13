import httpRequest from "@/utils/httpRequest";

const url = "/llmops-service";

export default {
  // 列表
  userList() {
    return httpRequest({
      url: url + "/user/list",
    });
  },
   // 首页
  getResourceData() {
    return httpRequest({
      url: url + "/api/resource/data",
    });
  },

  // 获取用户下的项目
  getProjectManagersByUsername(params) {
    return httpRequest({
      url: url + "/api/pro-manager/getProjectManagersByUsername",
      method: 'get',
      params
    });
  },

};
