import router from "@/router";
import store from "@/store";

/**
 * 获取uuid
 */
 export function getUUID(num = 30) {
  // return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, c => {
  // return "xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx".replace(/[xy]/g, c => {
  //   return (c === "x" ? (Math.random() * 16) | 0 : "r&0x3" | "0x8").toString(
  //     16
  //   );
  // });
  // uuid修改为30长度
  /* return "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".replace(/[xy]/g, c => {
    return (c === "x" ? (Math.random() * 16) | 0 : "r&0x3" | "0x8").toString(
      16
    );
  }); */
  let str = '';
  for (let i = 0; i < num; i++) {
    str += ((Math.random() * 16) | 0).toString(16);
  }
  return str;
}

/**
 * 是否有权限
 * @param {*} key
 */
export function isAuth(key) {
  // key 为空字符串或不存在时
  if (!key) return true;
  return (
    JSON.parse(sessionStorage.getItem("permissions") || "[]").indexOf(key) !==
    -1 || false
  );
}

// 是否 IE 浏览器
export function isIE() {
  if (!!window.ActiveXObject || "ActiveXObject" in window) return true;
  else return false;
}

/**
 * 清除登录信息
 */
export function clearLoginInfo(needTriggerParentLogout = true) {
  // Vue.cookie.delete("token");
  // localStorage.clear();
  localStorage.removeItem("id");
  localStorage.removeItem("userName");
  localStorage.removeItem("screenLocked");
  sessionStorage.clear();
  store.commit("resetStore");
  router.options.hasDynamicRoutes = false;
  initGlobalCache();

  // session 失效时，触发父系统（需基于 evervue）登出
  if (needTriggerParentLogout && window.globalConfig.triggerParentLogout) {
    document.referrer && parent.postMessage({ msg: "onLogout", payload: { needLogout: false, message: "会话已过期，请重新登录" } }, document.referrer);
  }
}

// 初始化内存全局缓存
export function initGlobalCache() {
  // dic 数据字典 cfgDic 属性（旧字典）
  window.globalCache = { dic: {}, cfgDic: {} };
}

// 对 url 中的变量进行解析
export function getParsedUrl(url) {
  if (!url) return url;
  let tmp = url;
  tmp = tmp.replace("${ip}", window.location.hostname);
  if (window.location.port) {
    tmp = tmp.replace("${port}", window.location.port);
  } else {
    tmp = tmp.replace(":${port}", "");
  }

  let DOMAIN = window.globalConfig.DOMAIN;

  const isIpPort = (domain) => /((?:(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(?:25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d))))/.test(domain)
  // 如果含有 ip 地址或者 localhost
  const isNotDomain = (domain) => domain.includes('localhost') || isIpPort(domain)

  if (DOMAIN) {
    if (isNotDomain(DOMAIN)) {
      // 替换整个域名
      tmp = tmp.replace(/[\w.\-]*\${DOMAIN}[\w.\-]*/, DOMAIN);
    } else {
      // 仅替换变量
      tmp = tmp.replace("${DOMAIN}", DOMAIN);
    }
  } else {
    // DOMAIN 未配置时，尝试自动生成
    DOMAIN = window.location.host
    if (isNotDomain(DOMAIN)) {
    } else {
      // 仅替换变量
      DOMAIN = DOMAIN.slice(DOMAIN.indexOf('.') + 1);
      tmp = tmp.replace("${DOMAIN}", DOMAIN);
    }
  }

  return tmp;
}

/**
 * 对原始 url 进行修正
 * @param {*} url 原始url
 * @param {*} absolute 是否转换成绝对路径
 */
export function getfixedUrl(url, absolute = false) {
  if (!url) return url;
  if (/^(http[s]?:)?\/\/.*/.test(url)) {
    // 绝对路径不转换
    return url;
  } else {
    let tmp = url;
    if (tmp.charAt(0) != "/") {
      tmp = "/" + tmp;
    }
    // 相对路径转成前端路由可识别的地址, 直接访问某些文件时除外
    if (!/\.(html|jpg|png|gif|svg|mp4)$/.test(url.split("?")[0])) {
      tmp = "/#" + tmp;
    }
    if (absolute) {
      tmp = window.location.protocol + "//" + window.location.host + tmp;
    }
    return tmp;
  }
}

/**
 * 截取应用地址，去掉结尾的 "?"、"/"、"/#...." 部分
 * url 用例："http://${ip}:${port}" "http://aaa.com/" "http://aaa.com?a=1&b=2" "http://aaa.com/#/index" "192.168.0.1" "192.168.0.1/index"
 * parse 是否调用 getParsedUrl 对其中的变量进行转换
 * 当 parse 为 false 时的结果："http://${ip}:${port}" "http://aaa.com" "http://aaa.com" "http://aaa.com" "http://192.168.0.1" "http://192.168.0.1/index"
 */
export function getAppUrl(url, parse = true) {
  if (!url) return url;
  let tmp = url.split('?')[0];
  if (!/^(http[s]?:)?\/\/.*/.test(tmp)) {
    console.warn(`应用地址 ${tmp} 可能存在错误，请检查！`);
    tmp = "http://" + tmp;
  }
  tmp = tmp.replace(
    // /(?<!http[s]?:[\/]?)(\/$|\/#.*)/,  // FireFox/Edge 等不支持反向预查
    /(?!http[s]?:[\/]?)(\/$|\/#.*)/, // 改为正向预查
    ""
  );
  if (parse) tmp = getParsedUrl(tmp);
  return tmp;
}

// 树形数据排序 seq 序号 children 子节点
export function sortTreeData(treeData) {
  treeData = treeData.sort((a, b) => {
    // 某个元素的 seq 不存在时，与前后元素比较时返回 NaN，排序会受影响
    return a.seq - b.seq;
  });

  treeData.map(item => {
    if (item.children) {
      sortTreeData(item.children);
    }
  });

  return treeData;
}

// 截取字符串并显示省略号
export function omitStr(str, len = 10, ellipsis = "...") {
  if (str && str.length && str.length > len) {
    return str.substring(0, len) + ellipsis;
  }
  return str;
}

// 元素相对于 body 的 offsetTop
export function getOffsetTopByBody(el) {
  let offsetTop = 0;
  while (el && el.tagName !== "body") {
    offsetTop += el.offsetTop;
    el = el.offsetParent;
  }
  return offsetTop;
}

// 获取字符串显示时的宽高
export function textSize(text, fontSize = "12px", fontWeight = "bold", fontFamily = "Avenir, Helvetica, Arial, sans-serif") {
  var span = document.createElement("span");
  var result = {};
  result.width = span.offsetWidth;
  result.height = span.offsetHeight;
  span.style.visibility = "hidden";
  span.style.fontSize = fontSize;
  span.style.fontWeight = fontWeight;
  span.style.fontFamily = fontFamily;
  span.style.display = "inline-block";
  document.body.appendChild(span);
  if (typeof span.textContent != "undefined") {
    span.textContent = text;
  } else {
    span.innerText = text;
  }
  result.width = parseInt(window.getComputedStyle(span).width) - result.width;
  result.height = parseInt(window.getComputedStyle(span).height) - result.height;
  // 移除span
  document.body.removeChild(span);
  return result;
}

// 将字符串 str 中全部 substr 替换为 replacement
export function replaceAll(str, substr, replacement) {
  // let reg = new RegExp(substr, "g");
  // return str.replace(reg, replacement);
  // 正则会遇到特殊字符未转义的问题，改用普通字符串进行全部替换
  return str.split(substr).join(replacement);
}
