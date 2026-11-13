import httpRequest from "@/utils/httpRequest";

const url = "/everbi/compass";

export default {
  // 查询当前登录的用户信息
  getMyInfo(key = window.globalConfig.appKey, flag = true) {
    return httpRequest({
      url: url + "/my",
      params: {
        appKey: key,
        all: flag,
        prd: window.globalConfig.isEverOne,
      }
    });
  },
  // 罗盘查询接口不再覆盖my接口，而是改为增量
  getMyConfig() {
    return httpRequest({
      url: url + "/myConfig",
      params: {
        prd: window.globalConfig.isEverOne,
      }
    });
  },
  // 应用排序
  seqApps(o) {
    return httpRequest({
        url: url + "/user/seqApps",
        params: o
    });
  },
  // 收藏排序
  seqFavorite(o) {
    return httpRequest({
        url: url + "/user/seqFavorite",
        params: o
    });
  },
  // 收藏
  handeFavorite(o) {
    return httpRequest({
      url: url + "/user/favorite",
      method: "post",
      params: o
    });
  },
  // 取消收藏
  cancelFavorite(o) {
    return httpRequest({
      url: url + "/user/cancelFavorite",
      method: "post",
      params: o
    });
  },
  // 获取数据字典和内容
  // 应该用 utils/dic
  /* getDicList(dicCodeArr) {
    let codeArray = [];
    dicCodeArr.forEach( item=> {
      codeArray.push({ dicCode: item, pId: "-9", isQueryAllSon: "0" });
    });
    let data = {
      dicParam: JSON.stringify(codeArray),
      operator: -9,
      province: -9,
      service: -9
    };
    return httpRequest({
      url: url2 + "/baseDicItem/selectSon",
      params: data,
      method: "get"
    });
  } */
};
