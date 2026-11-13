import Vue from "vue";
import App from "./App";
import router from "./router";
import store from "./store";
// import VueCookie from "vue-cookie";
import VueClipboard from "vue-clipboard2";
// import ElementUI from "element-ui";
// import "element-ui/lib/theme-chalk/index.css";
import { isAuth, initGlobalCache, isIE } from "@/utils/base";
import cloneDeep from "lodash/cloneDeep";
import "./element-ui-config.js";
import "./error-log.js";
import "./common.scss";
import "./el-custom.scss";
import "@/utils/commonFilter";
import "@/utils/directives";
import VueBus from "@/utils/vue-bus";
import PageTitle from "@/components/page-title";
import Icon from "@/components/icon";

// Vue.use(VueCookie);
Vue.use(VueClipboard);
Vue.use(VueBus);
Vue.component("PageTitle", PageTitle);
Vue.component("Icon", Icon);
// Vue.use(ElementUI, { size: window.globalConfig.uiSize || "small" });

Vue.config.productionTip = false;

// 非生产环境, 适配mockjs模拟数据
if (process.env.NODE_ENV !== "production") {
  require("@/mock");
}

// 挂载全局
Vue.prototype.isAuth = isAuth; // 权限方法

// 保存整站vuex本地储存初始状态
window.initialStoreState = cloneDeep(store.state);

// 全局缓存，刷新或退出登录时清空
initGlobalCache();

if (isIE()) {
  require("@/el-ie-fix.scss");
}

// IE路由补丁：使得url中的hash值变化能够触发路由跳转
const IERouterFix = {
  methods: {
    hashChangeHandler() { this.$router.push(window.location.hash.substring(1, window.location.hash.length)); },
  },
  mounted() { if ( isIE() ) { window.addEventListener('hashchange', this.hashChangeHandler); } },
  destroyed() { if ( isIE() ) { window.removeEventListener('hashchange', this.hashChangeHandler); } }
};

new Vue({
  mixins: [IERouterFix],
  router,
  store,
  render: h => h(App)
}).$mount("#app");
