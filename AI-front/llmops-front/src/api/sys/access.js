import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/access";

export default {
  // 获取用户的访问控制列表
  getList(userId) {
    return httpRequest({
      url: url + "/" + userId
    });
  },

  // 设置用户的访问控制列表
  setList(userId, o) {
    return httpRequest({
      url: url + "/" + userId,
      method: "post",
      data: o
    });
  }
};
