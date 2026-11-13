import api_cfg_dic from "@/api/sys/cfg/dic";

/*
 * 获取属性信息方法
 * type 需要获取的属性type
 * callback 回调函数
 * 由于需要刷新页面时能更新，所以不用 sessionStorage
 */
export function getCfgDic(type, callback, onError) {
  let globalDic = window.globalCache.cfgDic;

  if (globalDic[type] != null) {
    callback(globalDic[type]);
    return;
  }

  api_cfg_dic
    .getDicList(type)
    .then(data => {
      // 缓存对象
      globalDic[type] = data;

      callback(data || []);
    })
    .catch(err => {
      console.log("属性数据加载失败");
      if (typeof onError === "function") {
        onError(err);
      }
    });
}

/**
 * 获取属性信息方法
 * @param {*} type 需要获取的属性type
 * @returns {Promise}
 */
export function getCfgDicPromise(type) {
  return new Promise((resolve, reject) => {
    getCfgDic(type, resolve, reject);
  });
}

// 通过字典项的值获取名称，对于可用过滤器的场合，可采用 commonFilter 中的 codeFilter
export function getValueByKey(k, dic) {
  const tmp = dic.find(item => item.key == k);
  if (tmp) {
    return tmp.value;
  }
  return null;
}
