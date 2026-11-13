import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/app";

export default {
  // 获取app列表
  getList(o) {
    return httpRequest({
      url,
      params: o
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

  delete(id, key) {
    return httpRequest({
      url: url + "/" + id + "/" + encodeURIComponent(key),
      method: "delete"
    });
  },
};
