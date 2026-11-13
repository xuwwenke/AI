import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/cfg/dic";

export default {
  getDicList(type) {
    return httpRequest({
      url: url + "/" + type
    });
  },
  // 获取旧字典列表
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

  delete(id) {
    return httpRequest({
      url: url + "/" + id,
      method: "delete"
    });
  },
};
