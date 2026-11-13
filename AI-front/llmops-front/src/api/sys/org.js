import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/org";

export default {
  // 获取组织列表
  getList(o) {
    return httpRequest({
      url,
      params: o
    });
  },

  // 获取登录人的组织树
  getMyTree(o) {
    return httpRequest({
      url: url + "/my",
      params: o
    });
  },

  // 获取全部组织树
  getTree(o) {
    return httpRequest({
      url: url + "/tree",
      params: o
    });
  },

  // 获取单个组织信息
  getInfo(id) {
    return httpRequest({
      url: url + "/" + id
    });
  },

  insertOrUpdate(o) {
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
  },
  // 修改组织机构拥有的角色
  replaceRoles(id, roleArr) {
    return httpRequest({
      url: `${url}/role/replace/${id}`,
      method: "post",
      data: roleArr
    });
  }
};
