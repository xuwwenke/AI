import axios from "axios";
import router from "@/router";
import qs from "qs";
import { Message } from "element-ui";
import { clearLoginInfo } from "./base";
import store from "@/store";
import debounce from "lodash/debounce";
import { encryptAES } from './aes'

const showErrorMsg = debounce(msg => Message.error({ message: msg, customClass: "globalError" }), 300);
const errorMessages = res => `${res.status} ${res.statusText}`;

const http = axios.create({
  timeout: window.globalConfig.requestTimeout,
  withCredentials: true
  /* headers: {
    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
  } */
});

// 写入通讯异常日志
const pushRequestError = info => {
  store.commit("error/pushRequestError", info);
}

// 写入业务异常日志
const pushCodeError = res => pushRequestError({ ...res.data, body: res.data.body !== undefined ? JSON.stringify(res.data.body).substr(0, 500) : undefined });

/**
 * 请求拦截
 */
http.interceptors.request.use(
  conf => {
    // 生成取消请求的 token/cancel
    conf.cancelToken = new axios.CancelToken(cancel => {
      /**
       * conf.cancel 是自定义回调函数，必要时页面可以通过回调拿到单个token，取消单个请求
       * 示例：请求时传入与url同级的参数 cancel：cancel => (this.getListCancel = cancel)
       * 调用 this.getListCancel && this.getListCancel(); 取消上次请求
       * 注意 getListCancel 需要定义在实例上（如组件的data中），而不是api中，避免一个页面请求两个相同的api时，前一个请求被取消
       */
      if (conf.cancel) conf.cancel(cancel);
      /**
       * 之前为每个页面的所有请求生成一个token，取消所有请求时一次取消，但不能满足对单个请求取消的场景
       * 所以改成为每个请求生成一个token，用数组来保存，取消所有请求时遍历取消
       */
      window.globalCache.requestCancels = window.globalCache.requestCancels || [];
      window.globalCache.requestCancels.push(cancel);
    });

    // get 请求加上随机数
    if (conf.method == "get") {
      conf.params = {
        t: new Date().getTime(),
        ...conf.params
      };
    }

    // url 转换
    /* conf.url = (process.env.NODE_ENV !== "production" && process.env.OPEN_PROXY
      ? "/proxyApi"
      : "") + conf.url; */
    if (conf.baseURL == undefined) {
      conf.baseURL = window.globalConfig.defaultProxyPath;
    }

    // 请求头带上token
    // conf.headers['Authorization'] = Vue.cookie.get("token");

    // application/x-www-form-urlencoded 请求的数据进行序列化
    // console.log(conf.headers['Content-Type'])
    if (
      conf.headers["Content-Type"] &&
      conf.headers["Content-Type"].indexOf(
        "application/x-www-form-urlencoded"
      ) != -1
    ) {
      conf.data = qs.stringify(conf.data);
    }

    // for E-AUTH-APP-KEY
    const ignoreList = ['/sso/login', '/compass/my'];
    const ignore = ignoreList.some(el => conf.url.includes(el));

    if (!ignore) {
      let auth_key = store.state.apps.currentKey;

      if (conf.url.startsWith('/everbi')) auth_key = window.globalConfig.everbiEAuthAppKey;

      if (window.globalConfig.encryptEAuthAppKey) {
        const sessionId = sessionStorage.getItem('sessionId')
        const aesKey = sessionId.replace(/-/g, "").slice(0, 16); // 截取16位
        auth_key = encryptAES(auth_key, aesKey);
      }

      conf.headers["E-AUTH-APP-KEY"] = auth_key;
      conf.headers['E-Auth-Token'] = sessionStorage.getItem('sessionId');
    }

    // 为所有请求添加 namespace (当前项目名称)
    if (store.state.project && store.state.project.currentProject) {
      conf.headers["namespace"] = store.state.project.currentProject.name;
    }

    // 为所有请求添加 clusterName (当前集群名称)
    if (store.state.project && store.state.project.currentCluster) {
      conf.headers["clusterName"] = store.state.project.currentCluster.name;
    }

    // console.log(conf)
    return conf;
  },
  error => {
    return Promise.reject(error);
  }
);

/**
 * 响应拦截
 */
http.interceptors.response.use(
  res => {
    // 401, 用户session失效
    if (res.data && (res.data.code === 401 || res.data.code === -999)) {
      // 只有已打开的页面（获取过菜单）才出现提示，避免新打开的页面出现
      // 新打开的页面也不使用此处的跳转到login，因为 router.currentRoute.path 不符合预期
      // console.log(router.currentRoute)
      // if (router.options.hasDynamicRoutes) {
      if (router.currentRoute.path !== '/') { // 20210201 调整，为了使全局路由中的请求也能触发
        showErrorMsg(res.data.message);

        clearLoginInfo();
        router.push({
          name: "login",
          params: { target: router.currentRoute }
        }).catch(e => { });
      }

      pushCodeError(res);

      return Promise.reject(errorMessages(res));
    }
    const successCodes = res.config.successCodes || [200];
    const acceptedBusinessCodes = res.config.acceptedBusinessCodes || [];
    if (res.data && res.data.code != undefined && !successCodes.includes(Number(res.data.code)) && !acceptedBusinessCodes.includes(res.data.code)) {
      if (res.data.message) {
        showErrorMsg(res.data.message);
      } else {
        showErrorMsg("数据获取失败");
      }

      pushCodeError(res);

      return Promise.reject(errorMessages(res));
    }

    if (res.config._fullResData) return res.data;       // 如果请求配置中有 _fullResData 属性，则返回完整的响应数据
    if (res.data.body !== undefined) return res.data.body;
    return res.data;
  },
  err => {
    if (err && err.response) {
      switch (err.response.status) {
        case 400:
          err.message = "请求错误(400)";
          break;
        case 401:
          err.message = "未授权，请重新登录(401)";
          break;
        case 403:
          err.message = "拒绝访问(403)";
          break;
        case 404:
          err.message = "请求出错(404)";
          break;
        case 408:
          err.message = "请求超时(408)";
          break;
        case 500: {
          // 仅 500 错误优先获取后端返回的错误消息
          const backendMessage = err.response.data && (err.response.data.message || err.response.data.msg);
          err.message = backendMessage || "服务器错误(500)";
          break;
        }
        case 501:
          err.message = "服务未实现(501)";
          break;
        case 502: {
          const backendMessage = err.response.data && err.response.data.message;
          err.message = typeof backendMessage === "string" && backendMessage ? backendMessage : "网络错误(502)";
          break;
        }
        case 503:
          err.message = "服务不可用(503)";
          break;
        case 504:
          err.message = "网络超时(504)";
          break;
        case 505:
          err.message = "HTTP版本不受支持(505)";
          break;
        default:
          err.message = `连接出错(${err.response.status})!`;
      }

      showErrorMsg(err.message);
      const data = err.response.data;
      pushRequestError({ timestamp: data.timestamp, code: data.status, message: err.message, body: data.path });

    } else {
      console.log(err);
      // 可能是请求被取消，这时不需要进行提示
      // err.message = "连接服务器失败!";
    }

    return Promise.reject(err);
  }
);

export default http;
