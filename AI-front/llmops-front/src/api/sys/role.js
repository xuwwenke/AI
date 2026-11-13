import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/role";

export default {
  // 获取角色列表
  getList(o) {
    return httpRequest({
      url,
      params: o
    });
  },

  // 获取登录人的角色树
  getMyTree(o) {
    return httpRequest({
      url: url + "/my",
      params: o
    });
  },

  // 获取单个角色信息
  getInfo(id) {
    return httpRequest({
      url: url + "/" + id
    });
  },

  insert(o) {
    return httpRequest({
      url,
      method: "post",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
      },
      data: o
    });
  },

  update(o) {
    return httpRequest({
      url,
      method: "put",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
      },
      data: o
    });
  },

  delete(id) {
    return httpRequest({
      url: url + "/" + id,
      method: "delete"
    });
  }
};
