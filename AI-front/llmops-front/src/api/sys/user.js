import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/user";
// 系统安全-用户管理
export default {
  // 分页复合查询用户
  getList(o) {
    return httpRequest({
      url,
      params: o
    });
  },
  // 增加用户
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
  // 修改用户
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
  // 用户解锁，主要用于用户错误的登录次数过多后，需管理员解锁
  unlock(id) {
    return httpRequest({
      url: url + "/unlock/" + id,
      method: "put"
    });
  },
  // 根据用户id重置密码，支持前端使用管理员帐号重置用户密码的功能
  resetPassword(id) {
    return httpRequest({
      url: url + "/password/" + id,
      method: "put"
    });
  },
  // 根据邮箱重置用户密码，提供给前端，做密码找回
  resetPasswordByEmail(email, code, pwd) {
    return httpRequest({
      url: url + "/password/email/rest",
      method: "put",
      params: { email, code, pwd }
    });
  },
  // 更新用户状态（含：禁用账号、注销账号、账号恢复可用）
  setStatus(id, status) {
    return httpRequest({
      url: url + "/status",
      method: "put",
      params: { id, status }
    });
  },
  // 删除用户
  delete(id) {
    return httpRequest({
      url: url + "/" + id,
      method: "delete"
    });
  },
	// 批量给多个用户授权菜单
  setMenus(userId, userName, userIds, menuIds) {
    return httpRequest({
      url: url + "/menus",
      method: "put",
			headers: { userId, userName },
      params: { userIds, menuIds }
    });
  },
  // 根据id查询用户个人菜单
  getMenus(id) {
    return httpRequest({
      url: url + "/menus/" + id,
      method: "get"
    });
  }
};
