import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/menu";

export default {
  // 获取完整菜单
  getMenuList() {
    return httpRequest({
      url: url + "/app"
    });
  },

  // 获取当前用户菜单 已经按seq排好序了，但信息不够完整 (子节点为 childs)
  /* getMenuNav(id) {
    return httpRequest({
      url: "/system/sys/security/permission/getMenuPermission",
      params: {
        id: id
      }
    });
  }, */

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
  },
  upload(o) {
    return httpRequest({
      headers: {
        "Content-type": "multipart/form-data"
      },
      url: url + "/import",
      method: "post",
      data: o
    });
  },

  download({ appKey, menuIds }) {
    let params = menuIds ? `?menuIds=${menuIds}` : "";
    return  window.globalConfig.defaultProxyPath +
     url +
    `/export/${appKey}`
    + params;
  }
};
