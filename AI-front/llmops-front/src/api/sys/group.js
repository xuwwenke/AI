import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/group";
// 系统安全-用户组
export default {
  // 分页查询分组
  getList(o) {
    return httpRequest({
      url,
      params: o
    });
  },
  // 增加用户组
  insert(o) {
    return httpRequest({
      url,
      method: "post",
      params: o               // 这里三件套还有点 bug，暂时先不改成 data
    });
  },
  // 修改用户组
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
  // 删除用户组
  delete(id) {
    return httpRequest({
      url: url + "/" + id,
      method: "delete"
    });
  },
  // 查询用户组详情（含用户列表）
  getDetail(id) {
    return httpRequest({
      url: url + "/" + id,
      method: "get"
    });
  },
  // 追加用户到用户组
  pushUser(id, o) {
    return httpRequest({
      url: url + "/user/push/" + id,
      method: "post",
      data: o
    });
  },
  // 替换用户列表
  replaceUser(id, o) {
    return httpRequest({
      url: url + "/user/replace/" + id,
      method: "post",
      data: o
    });
  },
  // 修改用户组拥有的角色
  replaceRoles(id, roleArr) {
    return httpRequest({
      url: `${url}/role/replace/${id}`,
      method: "post",
      data: roleArr
    });
  }
};
