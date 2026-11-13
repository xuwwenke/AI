import httpRequest from "@/utils/httpRequest";

const url = "/everbi/customapp";

export default {
  // 获取自定义app列表
  getList(o) {
    return httpRequest({
      url: url + '/list',
      params: o
    });
  },

  insert(o) {
    return httpRequest({
      url,
      method: "post",
      data: o
    });
  },

  update(o) {
    return httpRequest({
      url,
      method: "put",
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
