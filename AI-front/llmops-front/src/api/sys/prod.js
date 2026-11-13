import httpRequest from "@/utils/httpRequest";

const url = "/system/sys/security/prd";

export default {
  // 获取产线列表
  getList(o) {
    return httpRequest({
      url,
      params: o
    });
  },
  // 获取所有产线
  getAll() {
    return httpRequest({
      url: url + "/all",
    });
  },
  // 增加产线
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
  // 修改产线
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
  // 删除产线
  delete(id, key) {
    return httpRequest({
      url: url + "/" + id + "/" + encodeURIComponent(key),
      method: "delete"
    });
  },
};
