import httpRequest from "@/utils/httpRequest";
import httpRequestApi from "@/utils/httpRequestApi";
import store from "@/store";

export default {
  // 是否开启验证码
  getNeedCode() {
    return httpRequest({
      url: "/sso/code"
    });
  },
  getKeyPair() {
    return httpRequest({
      url: "/sso/keyPair"
    });
  },
  login(o) {
    return httpRequest({
      url: "/sso/login",
      method: "post",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
      },
      data: o
    });
  },
  // 短信验证码登录
  smsLogin(o) {
    return httpRequest({
      url: "/sso/code/again",
      method: "post",
      params: o
    });
  },
  // 通过授权码进行免登录
  loginBycode(code) {
    return httpRequest({
      url: "/sso/login/code",
      method: "post",
      params: {
        code: code,
        appKey: window.globalConfig.appKey,
        all: true
      }
    })
  },
  // license 校验产品授权
  verifyAuth(){
    return httpRequest({
      url: '/system/sys/senior/license/verifyAuth'
    })
  },
  // 密码是否已过期
  passwordExpire(o) {
    return httpRequest({
      url: "/system/sys/security/user/expire/password",
      method: "put",
      params: o
    });
  },
  changePassword(o) {
    return httpRequest({
      url: "/system/sys/security/user/password",
      method: "put",
      params: o
    });
  },
  validatePassword(o) {
    return httpRequestApi({
      url: "/system/sys/security/user/password/validate",
      method: "post",
      params: o
    });
  },
  logout() {
    return httpRequest({
      url: "/sso/logout",
      params: {
        // 登出时的应用
        appKey: store.state.apps && store.state.apps.currentKey
      }
    });
  },
  // /sso/my 接口拿到的菜单有seq字段，打开方式等，但是需要手动排序，并且需要退出再进才会更新 (子节点为 children)
  getMyInfo() {
    return httpRequest({
      url: "/sso/my",
      params: {
        prdKey: window.globalConfig.prdKey,
        appKey: window.globalConfig.appKey,
        all: true
      }
    });
  },
};
