import httpRequest from "@/utils/httpRequest";

const url = window.globalConfig.useBiDic ? "/everbi/dic" : "/everbase/dic";

export default {
  // 获取数据字典和内容
  getDicList(dicCodeArr) {
    let codeArray = [];
    dicCodeArr.forEach(function (item) {
      codeArray.push({ dicCode: item, pId: "-9", isQueryAllSon: "0" });
    });
    let data = {
      dicParam: JSON.stringify(codeArray),
      operator: -9,
      province: -9,
      service: -9
    };
    return httpRequest({
      url: url + "/baseDicItem/selectSon",
      params: data,
      method: "get"
    });
  },

  // 获取字典列表
  getList(o) {
    return httpRequest({
      url: url + "/baseDic",
      params: {
        ...o
      }
    });
  },

  dicImport(o, cancel) {
    return httpRequest({
      headers: { "Content-Type": "multipart/form-data" },
      url: url + "/baseDic/import",
      method: "post",
      data: o,
      cancel
    });
  },

  add(o) {
    return httpRequest({
      url: url + "/baseDic/add",
      method: "post",
      params: o
    });
  },

  update(o) {
    return httpRequest({
      url: url + "/baseDic/update",
      method: "put",
      params: o
    });
  },

  delete(id) {
    return httpRequest({
      url: url + "/baseDic/delete",
      params: { id: id },
      method: "put"
    });
  },

  getCode(id) {
    return httpRequest({
      url: url + "/baseDicItem/listTree",
      params: { dicId: id }
    });
  },
  // 添加字典项
  addCode(o) {
    return httpRequest({
      url: url + "/baseDicItem/add",
      method: "post",
      params: o
    });
  },
  // 更改字典项
  updateCode(o) {
    return httpRequest({
      url: url + "/baseDicItem/update",
      method: "put",
      params: o
    });
  },
  // 删除字典项
  deleteCode(id) {
    return httpRequest({
      url: url + "/baseDicItem/delete",
      params: { id: id },
      method: "put"
    });
  },
  // 清除字典缓存
  clearCache(params) {
    return httpRequest({
      url: url + "/baseDic/clearCache",
      params,
      method: "put"
    });
  }
};
