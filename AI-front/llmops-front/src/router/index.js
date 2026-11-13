/**
 * 全站路由配置
 *
 * 对于自动生成页（可配到菜单中）/page/:id, 虽然菜单信息中有 id 和 name, 但路由信息中没有, 所以采用 path 来匹配
 * 菜单中不能配通配符id，否则所有页都会匹配这个菜单
 * 菜单中每次只能配单个页地址，虽可以通过解析找到page模块，但如果事先路由中不存在/page/:id，会造成只有菜单配了才可以查看该页效果
 */
import Vue from "vue";
import Router from "vue-router";
// import api_menu from "@/api/sys/menu";
import api_auth from "@/api/auth";
import api_prod from '@/api/sys/prod';
import api_compass from "@/api/compass";
import store from "@/store";
import { clearLoginInfo, getParsedUrl, getfixedUrl, getAppUrl, sortTreeData, isIE, getUUID } from "@/utils/base";
import { COMPASS_KEY, QUERY_APPKEY, QUERY_TAB } from "@/consts";
import cloneDeep from "lodash/cloneDeep";
import { Message } from "element-ui";
import { ng_url_mapping, ng_permission_mapping } from "./url-mapping";
import { inner_route_mapping } from "./url-route-mapping";
import qs from "qs";
import NProgress from "nprogress";
import "nprogress/nprogress.css";

NProgress.configure({ showSpinner: false });

Vue.use(Router);

// 开发环境不使用懒加载, 懒加载页面太多会造成webpack热更新慢, 而生产环境使用懒加载
const _import = require("./import-" + process.env.NODE_ENV).default;

// 模拟生产环境（但network中不会体现懒加载效果）
// const _import = require("./import-production").default;

// 全局路由(无需嵌套上左右整体布局)
let globalRoutes = [
  /* {
    path: "/404",
    component: _import("404"),
    name: "404"
  }, */
  {
    path: "/no-menu",
    component: _import("no-menu"),
    name: "noMenu"
  },
  {
    path: "/login",
    component: _import("login"),
    name: "login"
  },
  {
    path: "/login.code",  // 通过授权码登录（授权码在后台获取）, 例 http://localhost:8080/#/login.code?code=123&target=/chart
    component: _import("login-code"),
    name: "loginCode"
  },
  // 没有自动生成页root需求时，注释掉这里
  {
    path: "/page-root/:type/:id", // 仅显示单页，用于嵌入到其它系统
    component: _import("page/page-root")
  },
  {
    path: "/webssh/:type/:namespace/:pod_name", // 容器终端
    component: _import("webssh")
  },
  {
    path: "/webssh/:type",
    component: _import("webssh")
  },
  {
    path: "/llmops/large-screen",
    component: _import("llmops/largeScreen/index"),
    name: "large-screen",
    meta: { menu: { name: "AI模型迭代指挥平台" } }
  }
];

// 主入口路由(需嵌套上左右整体布局)
const mainRoutes = {
  path: "/",
  component: _import("main"),
  name: "main"
};
const otherRouters = [];

let homePageRoute;
let compassRoute;
let firstRoutes = {};
const queryReg = /\?(?!\/|\?)/; // ?, 排除 ?/ 和 ??
const router = createRouter();

// 取消现有 cancel token 下所有未完成的请求
export function cancelRequest() {
  if (window.globalCache.requestCancels) {
    window.globalCache.requestCancels.forEach(cancel => {
      cancel();
    });
    window.globalCache.requestCancels = [];
  }
}

export function menuAction(menu, keepTab = false) {
  if (menu.url || menu.children) {
    // 支持通过父级打开（找到第一个叶子节点作为菜单）
    const loop = m => {
      if (m.children && m.children.length > 0) return loop(m.children[0]);
      else return m;
    }
    menu = loop(menu);

    if (["_blank", "blank", "dialog"].includes(menu.openMode)) {
      window.open(menu.url);
      return;
    }

    let query = {};
    const qStr = menu.url.split(queryReg)[1];
    if (qStr) {
      query = qs.parse(qStr);
    }

    if (keepTab) {
      const query_tab = router.currentRoute.query[QUERY_TAB];
      if (query_tab) query[QUERY_TAB] = query_tab;
    }

    gotoRoute(
      { name: getRouteNameByMenu(menu), query },
      false,
      menu.openMode === 'multiple'
    );
  }
}

export function getRouteNameByMenu(menu) {
  return menu.name + '_' + menu.id;   // 如果用id，需加上前缀，防止与静态 name（如 "404"）冲突
}

/**
 * 路由跳转处理
 * @param {*} route 路由对象
 * @param {*} tabClick 是否由标签点击触发（标签形式下）
 * @param {*} multiple 是否可重复打开多次（标签形式下）
 */
export function gotoRoute(route, tabClick = false, multiple = false) {
  const name = route.name;

  if (window.globalConfig.useTabs) {
    const vName = route.query && route.query[QUERY_TAB]
      ? name + "-" + route.query[QUERY_TAB]
      : name;

    let tab = store.state.mainTabs.tabs.find((item) => item.name === vName);
    if (tab) {
      // 使用tab信息，避免左侧菜单激活tab后，query和params丢失
      let o;
      // 点击tab时，保持当前子路由状态
      // !tab.iframeUrl 修复问题：第一个tab是iframe时，其它tab关闭可能会弹出另一个相同iframe的tab
      // 问题分析：当iframe内嵌的地址是另一个应用下的菜单时，会出现两个path相近的路由，导致path匹配混乱，所以tab事件中iframe不用path定向
      if (tabClick && tab.path && !tab.iframeUrl) {
        o = {
          path: tab.path, // path中已经包含了params信息
          query: tab.query,
        };
      } else {
        // 点击左侧菜单时不保持状态
        // let query = {};
        // const query_appkey = tab.query[QUERY_APPKEY];
        // const query_tab = tab.query[QUERY_TAB];
        // if (query_appkey) query[QUERY_APPKEY] = query_appkey;
        // if (query_tab) query[QUERY_TAB] = query_tab;

        // 改：点击左侧菜单时保持 query 参数，否则不太方便，例如需要通过 url 参数来保持平台的项目切换状态
        let query = { ...tab.query }
        if (multiple) query[QUERY_TAB] = getUUID(5);

        o = {
          name,
          // params: tab.params,  // 不使用params，以免点上级面包屑无法返回
          query,
        };
      }
      router.push(o).catch((e) => { });
    } else {
      router.push(route).catch((e) => { });
    }
  } else {
    router.push(route).catch((e) => { });
  }
}

// 重置路由
export function resetRouter() {
  const newRouter = createRouter();
  router.matcher = newRouter.matcher;

  firstRoutes = {};
}

// 切换到应用
export function switchApp(app) {
  const storedApps = store.state.apps;
  const queryAppKey = router.currentRoute.query[QUERY_APPKEY];
  if (!app || (app.key === storedApps.currentKey && queryAppKey !== COMPASS_KEY)) return;

  const appKey = app.key;

  // 已配置通过新窗口打开的应用
  if (app.urlTarget === '_blank') {
    window.open(getParsedUrl(app.url), "_blank");
    return;
  }

  let path = `/?${QUERY_APPKEY}=${appKey}`;

  // 当为自定义应用时（当前通过 app.appName 是否存在来判断）
  if (app.appName) {
    let tmp = `everbiAppKey=everbi&projectid=${app.id}&customApp=1`;
    path += `&_iframeQuerys_=${encodeURIComponent(tmp)}`;
  }

  router.replace(path).catch(e => { });
}

// 获取第一个可用的菜单 name
export function getFirstMenu(menuList) {
  for (const item of menuList) {
    if (item.children && item.children.length >= 1 && item.children.some(child => !child.hidden)) {
      const result = getFirstMenu(item.children);
      if (result) return result;
    } else {
      if (!item.url || item.url === "/") continue;
      switch (item.openMode) {
        // 打开新窗口模式不作为默认首页
        // case "_blank":
        // 兼容ng版配置
        case "_blank":
        case "blank":
        case "dialog":
          break;
        default: {
          let query = {};
          const qObj = qs.parse(item.url.split(queryReg)[1]);
          if (/^(http[s]?:)?\/\/.*/.test(item.url)) {
            // 绝对地址的参数在 iframe 处理时已带上，这里只带 _app_ 即可
            query[QUERY_APPKEY] = qObj[QUERY_APPKEY];
          } else {
            query = qObj;
          }
          return { name: getRouteNameByMenu(item), query };
        }
      }
    }
  }
  return null;
}

router.beforeEach((to, from, next) => {
  let queryAppKey = to.query[QUERY_APPKEY];
  const storedApps = store.state.apps;
  const routeType = getRouteType(to, globalRoutes);

  if (queryAppKey && queryAppKey !== COMPASS_KEY && (window.globalConfig.showAppSelect || window.globalConfig.useBiCompass)) {
    // 应用 url 参数中的 appKey
    if (storedApps.currentKey !== queryAppKey) store.commit("apps/setCurrentKey", queryAppKey);
  } else {
    if (storedApps.currentKey !== storedApps.originKey) store.commit("apps/setCurrentKey", storedApps.originKey);
  }

  let currentKey = storedApps.currentKey;

  // 取消旧页面的请求（Tabs形式除外）
  if (!window.globalConfig.useTabs) cancelRequest();

  // 添加动态(菜单)路由
  // 1. 已经添加 or 全局路由, 直接访问
  // 2. 获取菜单列表, 添加并保存本地存储
  if (router.options.hasDynamicRoutes || routeType === "global") {
    const firstRoute = firstRoutes[currentKey];
    let route_tmp = {};

    if (to.path === "/") {
      const homePageUrl = window.globalConfig.homePageUrl;
      // 跳到首页
      if (!queryAppKey && homePageUrl) {
        route_tmp = homePageRoute;
      } else if ((!queryAppKey || queryAppKey === COMPASS_KEY) && window.globalConfig.useBiCompass && window.globalConfig.compassStyle === 1) {
        route_tmp = compassRoute;
      } else if (firstRoute && firstRoute.name !== null) {
        firstRoute.query = to.query;
        route_tmp = firstRoute;
      }
    } else {
      if (self == top) NProgress.start(); // 只在这种情况下加进度条，避免在默认页时，点平台标题，进度条不消失
    }

    // 如果从登录页跳过来，浏览器回退不再回到登录页
    next({ ...route_tmp, replace: from.path === "/login" });
  } else {
    /* const token = Vue.cookie.get("token");
    const id = localStorage.getItem("id");
    // 如果没有登录
    if (!token || !/\S/.test(token) || !id) {
      clearLoginInfo();
      next({ name: "login", params: { target: to } });
    } else {
      // 重置路由
      resetRouter(); */
    // api_menu.getMenuNav(id)

    //
    api_auth
      .getMyInfo()
      .then(data => {
        localStorage.setItem("id", data.user.id);
        localStorage.setItem("userName", data.user.userName);
        sessionStorage.setItem('sessionId', data.sessionId);
        window.globalCache.userInfo = data.user;

        // 重置路由
        resetRouter();

        // 保存到 vuex
        let origin_app = data.apps.find(app => app.key === storedApps.originKey);
        let origin_app_url;

        if (origin_app) {
          origin_app_url = getAppUrl(origin_app.url);
        }

        // const menuList = data.list;
        let list = [];
        let permissions = [];
        data.apps.forEach(app => {
          if (
            app.key === window.globalConfig.appKey ||
            window.globalConfig.showAppSelect ||
            window.globalConfig.useBiCompass ||
            window.globalConfig.showFullMenu ||
            (window.globalConfig.showSystemMenu && app.key === "system")
          ) {
            app.menus = sortTreeData(app.menus);

            // 当没有原始应用时，使用符合条件的第一个应用作为原始应用
            if (!origin_app) {
              origin_app = app;
              origin_app_url = getAppUrl(origin_app.url);

              currentKey = origin_app.key;
              store.commit("apps/setOriginKey", currentKey);
              store.commit("apps/setCurrentKey", currentKey);

              // my 接口新增能力，响应中加了 currentApp 属性
              // 不存到 vuex 中，因为 currentApp 不包含菜单数据
              let login_app = data.currentApp;

              if (login_app) {
                origin_app = login_app;
                origin_app_url = getAppUrl(login_app.url);
              }
            }

            // 当开启应用切换，或开启罗盘，或显示所有菜单时，为非当前应用的菜单 url 追加 app 参数
            if (window.globalConfig.showAppSelect || window.globalConfig.useBiCompass || window.globalConfig.showFullMenu) {
              if (app.key !== origin_app.key) {
                let appUrl;

                const app_url = getAppUrl(app.url);
                // 当 app 的 url 与原始应用的 url 以及当前 url 不同时，需拼接 app 的 url
                if (app_url !== origin_app_url && app_url !== window.location.protocol + "//" + window.location.host) {
                  appUrl = app_url;
                }

                transMenus(app.menus, app.key, appUrl);
              }
            }

            firstRoutes[app.key] = getFirstMenu(app.menus);

            list = list.concat(app.menus);
          }

          // permissions = permissions.concat(Object.keys(app.functions));

          // 兼容ng版配置
          let keys = [];
          Object.keys(app.functions).map(key => {
            const mappedPermission = ng_permission_mapping[key];
            if (mappedPermission) {
              keys = keys.concat(mappedPermission);
            } else {
              keys = keys.concat(key);
            }
          });
          permissions = permissions.concat(keys);
        });

        if (list.length == 0 && !window.globalConfig.homePageUrl) {
          // Message.error("没有权限登录当前应用（没有任何菜单）");
          next({ name: "noMenu" });
          return;
        }

        // 后台好像已经改成排序的了，这里的排序暂时没去掉，兼容旧后台，以防万一
        const menuList = sortTreeData(list);

        // console.log("菜单列表：", menuList);
        dynamicRoutesBuilder(menuList);
        // console.log(menuList, JSON.stringify(menuList));
        sessionStorage.setItem("menuList", JSON.stringify(menuList || "[]"));
        sessionStorage.setItem("permissions", JSON.stringify(permissions || "[]"));
        store.commit("apps/setList", cloneDeep(data.apps));
        // store.commit("apps/setProdList", cloneDeep(data.prd));
        // firstRoute = getFirstMenu(menuList);
        // console.log("第一个Url：", firstRoute);

        next({ ...to, replace: true });

        if (window.globalConfig.isEverOne) {
          // 由于不再从 bi 的 my 接口取产线列表，而三件套的 my 又没返回产线，所以需要专门取
          // 这里注意不能调分页查询的接口，因为系统管理里面配了权限，如果角色未授权，会 401
          api_prod
            .getAll()
            .then(prdData => {
              const allProdList = prdData || [];
              store.commit("apps/setProdList", cloneDeep(allProdList));
              data.prd = allProdList

              if (window.globalConfig.useBiCompass) {
                useBiCompassConfig(data)  // 异步追加应用导航/功能罗盘配置
              }
            })
        } else if (window.globalConfig.useBiCompass) {
          useBiCompassConfig(data)  // 异步追加应用导航/功能罗盘配置
        }
      })
      .catch(e => {
        clearLoginInfo();
        // sessionStorage.setItem("menuList", "[]");
        console.error(e);
        console.log("请求菜单列表和权限失败，跳转至登录页");

        next({ name: "login", params: { target: to } });
        // 应对场景：退出到登录页后，点浏览器后退，进度条不消失
        NProgress.done();
      });
    // }
  }
});

router.afterEach(to => {
  NProgress.done();
  const el = document.querySelector('.site-main-content');
  if (el) el.scrollTop = 0;
});

// 创建路由
function createRouter() {
  pushHomePageRoute(globalRoutes, true);

  return new Router({
    mode: "hash",
    scrollBehavior: () => ({ y: 0 }),
    hasDynamicRoutes: false, // 是否已经添加动态(菜单)路由
    routes: globalRoutes
  });
}

/**
 * 获取路由类型, global: 全局路由, main: 主入口路由
 */
function getRouteType(route, globalRoutes = []) {
  let tmp = [];
  for (let i = 0; i < globalRoutes.length; i++) {
    // console.log(route, globalRoutes[i]);
    // 带参数的动态路由匹配时，当前路由path与路由表中的path不相等，所以加入额外匹配判断
    if (
      (route.name != undefined && route.name === globalRoutes[i].name)
      || route.path === globalRoutes[i].path
      || (route.matched[0] && route.matched[0].path === globalRoutes[i].path)
    ) {
      return "global";
    } else if (
      globalRoutes[i].children &&
      globalRoutes[i].children.length >= 1
    ) {
      tmp = tmp.concat(globalRoutes[i].children);
    }
  }
  return tmp.length >= 1 ? getRouteType(route, tmp) : "main";
}

// 全局配置 homePageUrl 对应的路由
function pushHomePageRoute(routes, isGlobal = false) {
  const homePageUrl = window.globalConfig.homePageUrl;
  const noQueryUrl = homePageUrl.split("?")[0];

  if (!window.globalConfig.homePageUrl || window.globalConfig.homePageIsGlobal != isGlobal) return;

  const existRoute = routes.find(route => route.path == noQueryUrl);
  if (existRoute) {
    homePageRoute = existRoute;
    return;
  }

  const parsedUrl = getParsedUrl(homePageUrl);
  const tmpUrl = homePageUrl.startsWith("/") ? homePageUrl.substr(1) : homePageUrl
  let path;
  let comp;
  let meta;

  if (/^(http[s]?:)?\/\/.*/.test(parsedUrl)) {
    // 测试用例：http://${ip}:8888/aaa/bbb（第三方页面）
    // 测试用例：http://${ip}:${port}/custom-html/demo2.html
    path = "/iframe/" + encodeURIComponent(parsedUrl);
    comp = "iframe";
    meta = { iframeUrl: parsedUrl };
  } else if (/\.html$/.test(noQueryUrl)) {
    // 测试用例：/custom-html/demo2.html（本地html）
    // 使用 iframe 嵌入本地 html，可避免跳转导致系统切换
    path = "/iframe/" + tmpUrl;
    comp = "iframe";
    meta = { iframeUrl: homePageUrl };
  } else {
    // 测试用例：/aaa/bbb（未配置成菜单的页面）
    // 测试用例：/sys/log/login（菜单页面）
    path = homePageUrl;
    comp = tmpUrl;
  }

  homePageRoute = {
    name: "homePage",
    path,
    component: _import(comp) || null,
    meta
  };

  routes.push(homePageRoute);
}

/**
 * 转换菜单（针对非当前应用），使得页面刷新后的应用切换状态及菜单高亮保持，且部署地址不同时菜单地址自动转为绝对路径
 * @param {*} menuList 菜单列表
 * @param {*} appKey 菜单列表所属的 app 的 key
 * @param {*} appUrl 菜单列表所属的 app 的 url
 */
function transMenus(menuList, appKey, appUrl) {
  for (const ml of menuList) {
    if (!ml.children) {
      if (!ml.url) continue;
      ml.originUrl = ml.url;
      // 兼容ng版配置，非当前应用只转换ng菜单地址，不用匹配当前应用的内置路由
      const mappedUrl = ng_url_mapping[ml.url];
      if (mappedUrl) ml.url = mappedUrl;

      // 如果需要拼接 app 的 url
      if (appUrl) {
        let menu_url = getfixedUrl(ml.url);
        if (/^(http[s]?:)?\/\/.*/.test(menu_url)) {
          // 绝对路径不拼接应用地址
          ml.url = menu_url;
        } else {
          ml.url = appUrl + menu_url;
        }
      }

      if (window.globalConfig.showAppSelect || window.globalConfig.useBiCompass) {
        if (!["_blank", "blank", "dialog"].includes(ml.openMode)) {
          // 追加 app 的 key
          const sign = queryReg.test(ml.url) ? '&' : '?';
          ml.url += sign + QUERY_APPKEY + '=' + appKey;
        }
      }
    } else {
      transMenus(ml.children, appKey, appUrl);
    }
  }
}

/**
 * 动态路由生成器
 * @param {*} menuList 菜单列表
 * @param {*} routes 动态路由
 */
function dynamicRoutesBuilder(menuList = [], routes = []) {
  let nextList = [];    // 下一次递归需处理的列表

  for (let i = 0; i < menuList.length; i++) {
    const ml = menuList[i];

    // 如果含有子菜单，且子菜单不是全部隐藏的（隐藏菜单可用于生成下钻页路由），则删除其 url（不生成路由，也不支持面包屑点击），作为父级菜单
    if (ml.children && ml.children.length >= 1 && ml.children.some(child => !child.hidden)) {
      delete ml.url;
    }

    // 存入面包屑中的信息尽量简化，以免转存到 localStorage 时，体积超出上限
    let ml_simple = {
      id: ml.id,
      name: ml.name,
      icon: ml.icon,
      url: ml.url,
      hidden: ml.hidden,
    };

    // 计算菜单的面包屑
    if (ml.parentBreadcrumb) {
      ml.breadcrumb = [...ml.parentBreadcrumb, ml_simple];
    } else {
      ml.breadcrumb = [ml_simple];  // 第一层级
    }

    // 删掉菜单信息中的冗余信息
    delete ml.parentBreadcrumb;     // 临时的传递
    delete ml.parent;               // 后台返回的，仅能看到上级，被面包屑取代

    if (ml.children && ml.children.length >= 1) {
      ml.children.forEach(child => child.parentBreadcrumb = ml.breadcrumb);   // 将父级的面包屑传递下去

      nextList = nextList.concat(ml.children);  // 广度遍历，拼接下一层

      if (ml.url) delete ml.children;   // 如果 url 未被删除，说明其不作为父级菜单，需删除 children 以免菜单组件生成子菜单
    }

    // 生成路由和菜单
    if (ml.url && /\S/.test(ml.url)) {
      // 匹配内置路由
      const mappedRoute = cloneDeep(inner_route_mapping[ml.originUrl || ml.url]);
      if (mappedRoute) {
        let url = mappedRoute.path;
        const queryStr = ml.url.split(queryReg)[1];
        // 给自定义路由加上_app_参数
        if (queryStr) {
          const sign = queryReg.test(url) ? "&" : "?";
          url += sign + queryStr;
        }
        ml.url = url; // 替换原有url
        // 如果是嵌套路由
        if (mappedRoute.children) {
          parseNestedRoute(mappedRoute, getRouteNameByMenu(ml), ml);
        }
        routes.push(mappedRoute);   // 加入内置路由
        continue;
      }
      // 兼容ng版配置
      const mappedUrl = ng_url_mapping[ml.url];
      if (mappedUrl) ml.url = mappedUrl;
      // ml.url = ml.url.replace(/^\//, "");

      let parsedUrl = getParsedUrl(ml.url);
      let fixedUrl = getfixedUrl(parsedUrl);

      // if (ml.openMode == "_blank") {
      // 兼容ng版配置
      if (["_blank", "blank", "dialog"].includes(ml.openMode)) {
        ml.url = fixedUrl;
        continue;
      }

      // 外链转内链，即同应用的地址避免用iframe，而是用路由，暂时只对 /page-root 生效
      if (/^(http[s]?:)?\/\/.*/.test(ml.url) && ml.url.includes("/page-root")) {
        const app_url = getAppUrl(ml.url);
        if (app_url === window.location.protocol + "//" + window.location.host) {
          ml.url = parsedUrl.replace(app_url, "");
          ml.url = ml.url.replace("/#", "");
          ml.url = ml.url.replace("/page-root", "/page");

          parsedUrl = getParsedUrl(ml.url);
          fixedUrl = getfixedUrl(parsedUrl);
        }
      }

      // 作为 main 的 children 时，vue-router 在 matcher 会自动加上 "/"
      // if (!/^(http[s]?:)?\/\/.*/.test(ml.url) && ml.url.charAt(0) != "/") {
      //   ml.url = "/" + ml.url;
      // }

      let route = {
        path: ml.url,
        component: null,
        name: getRouteNameByMenu(ml),
        meta: {
          menu: ml
        }
      };

      let tmp = ml.url;

      // 处理因动态路由结尾处的问号，引起的参数拼接符号问题
      // 例如针对 /dashboard/:id?&app=everbi 进行处理
      if (ml.url.includes("?&" + QUERY_APPKEY)) {
        tmp = ml.url.split("&" + QUERY_APPKEY)[0];
        route.path = tmp;
      }

      // 处理 query 参数，这里要排除动态路由中的问号，仅考虑常规问号
      // 例如针对 /dashboard/:id?/:name?t=123 ，仅按后一个问号进行分割
      const arr_tmp = tmp.split(queryReg);
      let queryInfo;
      // 如果常规问号后还有字符
      if (arr_tmp[1]) {
        queryInfo = qs.parse(arr_tmp[1]);
        route.path = arr_tmp[0];
        route.meta = {
          ...route.meta,
          queryInfo,
        };
      }

      // 去掉动态路由参数部分，从第一个 /: 分割
      let url_tmp = getParsedUrl(arr_tmp[0].split("/:")[0]);

      // 如果 url 是绝对路径，采用 iframe 打开，这里可理解为 /^(http[s]?:)?\/\/.*/.test(ml.url)，因 fixedUrl 已处理过，所以简写了
      if (!/^\/#\/.*/.test(fixedUrl)) {
        let urlWithQuery = url_tmp;

        if (queryInfo) {
          const query_tmp = cloneDeep(queryInfo);
          // 当不同应用下菜单地址生成的路由相同时，url就会匹配靠前的路由。为了使路由不同，iframe 地址还是需带上 _app_
          // delete query_tmp[QUERY_APPKEY];      // 外层系统已带上 _app_，iframe 地址不再带
          if (Object.keys(query_tmp).length > 0) {
            urlWithQuery += "?" + qs.stringify(query_tmp, { encode: false });
          }
        }

        // 因 iframe-loader 中会对 url 进行转义，所以这里传入原始 url 即可
        route.path = "iframe/" + encodeURIComponent(urlWithQuery);
        // route.component = _import("iframe") || null;
        route.meta = {
          ...route.meta,
          // 这里使用 urlWithQuery，是避免 iframe 的参数丢失，虽然可以把参数单独放在 meta 中，在需要时取，但是很多第三方系统并不会有该逻辑
          iframeUrl: urlWithQuery,
        };

        // 如果开启了预加载，且不是 IE 浏览器
        if (window.globalConfig.iframePreload && !isIE()) {
          // 预加载
          if (!window.globalCache.preload_urls) window.globalCache.preload_urls = {};
          let app_url = getAppUrl(url_tmp);
          if (!window.globalCache.preload_urls[app_url]) window.globalCache.preload_urls[app_url] = url_tmp;
        } else {
          route.component = _import("iframe") || null;
        }
      } else {
        // 去掉开头的 /
        let componentUrl = url_tmp.replace(/^\//, "");

        // bi 的动态页配成菜单时，不能都配同一个 path（/page 或者 /page/:type/:id），所以用 /page/1/xx 作为 path，同时将参数解析后存入 meta 中
        if (/^\/page\//.test(ml.url)) {
          const arr_tmp2 = componentUrl.split("/");
          arr_tmp2.shift();
          route.meta = {
            ...route.meta,
            paramsInfo: arr_tmp2
          };
          componentUrl = "page";
        }

        // 通过特殊分隔符来指定url左侧为组件路径，右侧为路由参数的值，示例：/some/_/a1/b2
        const SIGN = "/_/";
        if (ml.url.includes(SIGN)) {
          const urlWithParams = componentUrl.split(SIGN);
          const arr_tmp2 = urlWithParams[1].split("/");
          route.meta = {
            ...route.meta,
            paramsInfo: arr_tmp2
          };
          componentUrl = urlWithParams[0];
        }

        try {
          route.component = _import(componentUrl) || null;
        } catch (e) {
          console.log(e);
        }
      }
      // console.log(route);
      routes.push(route);
      // }
    }
  }

  if (nextList.length >= 1) {
    dynamicRoutesBuilder(nextList, routes);
  } else {
    pushHomePageRoute(routes);

    if (window.globalConfig.useBiCompass && window.globalConfig.compassStyle === 1) {
      compassRoute = {
        name: COMPASS_KEY,
        path: `/${COMPASS_KEY}`,
        component: () => import('@/components/site-top/compass/compass-page-new/index.vue'),
        query: {
          [QUERY_APPKEY]: COMPASS_KEY,
        },
        meta: {
          menu: {
            name: window.globalConfig.compassBtnName,
          }
        }
      };

      routes.push(compassRoute)
    }

    const dynamicRoutes = cloneDeep(mainRoutes);
    // dynamicRoutes.name = "main-dynamic";

    let tmp = routes;
    // 在动态路由前追加路由，不要在 mainRoutes 处加（matcher在""前），会和这里加效果不一样（matcher在""后）
    tmp = [
      {
        path: "/llmops/knowledge",
        component: _import("llmops/knowledge/index"),
        name: "knowledge",
        meta: { menu: { name: "知识库" } }
      },
      {
        path: "/llmops/knowledge/create",
        component: _import("llmops/knowledge/create"),
        name: "knowledge-create",
        meta: { menu: { name: "创建知识库" } }
      },
      {
        path: "/llmops/knowledge/:id/documents/create",
        component: _import("llmops/knowledge/create"),
        name: "knowledge-document-create",
        meta: { menu: { name: "添加知识库文档" } }
      },
      {
        path: "/llmops/knowledge/:id/documents/:documentId",
        component: _import("llmops/knowledge/document"),
        name: "knowledge-document-detail",
        meta: { menu: { name: "文档详情" } }
      },
      {
        path: "/llmops/knowledge/:id/retrieve",
        component: _import("llmops/knowledge/retrieve"),
        name: "knowledge-retrieve",
        meta: { menu: { name: "召回测试" } }
      },
      {
        path: "/llmops/knowledge/:id/settings",
        component: _import("llmops/knowledge/settings"),
        name: "knowledge-settings",
        meta: { menu: { name: "知识库设置" } }
      },
      {
        path: "/llmops/knowledge/:id",
        component: _import("llmops/knowledge/detail"),
        name: "knowledge-detail",
        meta: { menu: { name: "知识库文档" } }
      },
      {
        path: "/llmops/pipeline",
        component: _import("llmops/pipeline/index"),
        name: "pipeline",
        meta: { menu: { name: "Pipeline 流水线" } }
      },
      {
        path: "/llmops/datasets/annotation",
        component: _import("llmops/datasets/annotation/index"),
        name: "dataset-annotation",
        meta: { menu: { name: "数据标注" } }
      },
      {
        path: "/llmops/datasets/annotation/projects/:id/settings",
        component: _import("llmops/datasets/annotation/settings"),
        name: "dataset-annotation-settings",
        meta: { menu: { name: "项目设置" } }
      },
      {
        path: "/llmops/datasets/annotation/projects/:id/tasks",
        component: _import("llmops/datasets/annotation/tasks"),
        name: "dataset-annotation-tasks",
        meta: { menu: { name: "数据标注任务" } }
      },
      ...tmp
    ];

    dynamicRoutes.children = tmp;

    // 现有路由信息不能修改和删除，只能 addRoutes 来改变
    router.addRoutes([dynamicRoutes, { path: "*", name: "404", component: _import("404") }]);

    router.options.hasDynamicRoutes = true;
    // console.log(router);
  }
}

/**
 * 解析嵌套路由
 * @param {*} route 路由
 * @param {*} name 路由名，对应菜单名
 * @param {*} menu 菜单信息
 * @param {*} findDefaultChildRoute 查找默认子路由
 */
function parseNestedRoute(route, name, menu, findDefaultChildRoute = true) {
  // 当前层级路由存在默认子路由
  let hasDefaultChildRoute = false;

  route.meta = { ...route.meta, name, menu };
  route.children.forEach(item => {
    item.meta = { ...item.meta, name, menu };

    // 找到默认子路由
    if (findDefaultChildRoute && (item.path === "" || item.path === "/")) {
      hasDefaultChildRoute = true;

      if (item.children) {
        parseNestedRoute(item, name, menu);
      } else {
        // 给最终的默认子路由加上 name
        item.name = name;
      }
    } else if (item.children) {
      // 为每一层子路由加上 meta.name
      parseNestedRoute(item, name, menu, false);
    }
  });

  // 找不到下级默认子路由，给当前层级路由（可能是父路由或某层默认子路由）加上 name
  if (findDefaultChildRoute && !hasDefaultChildRoute) route.name = name;
}

/**
 * 获取应用导航/功能罗盘的配置，追加到已有数据上（依赖 BI 后台服务）
 * @param {*} data  三件套 my 接口返回的数据
 */
function useBiCompassConfig(data) {
  api_compass.getMyConfig().then(extendData => {
    // console.log(extendData)
    function setMenuExtendNode(menus) {
      menus.forEach(menu => {
        const menuExtendNode = extendData.menu[menu.id]
        if (menuExtendNode) menu.extendNode = menuExtendNode
        if (menu.children) setMenuExtendNode(menu.children)
      })
    }

    let prds = cloneDeep(data.prd)
    if (prds) {
      prds.forEach(prd => {
        const prdExtendNode = extendData.prd[prd.id]
        if (prdExtendNode) prd.extendNode = prdExtendNode
      })
    }

    let apps = cloneDeep(data.apps)
    apps.forEach(app => {
      const appExtendNode = extendData.app[app.id]
      if (appExtendNode) app.extendNode = appExtendNode
      const appSeq = extendData.appSeq[app.key]
      if (appSeq) app.compSeq = appSeq
      setMenuExtendNode(app.menus)
    })

    store.commit("apps/setList", apps);
    store.commit("apps/setProdList", prds);
  })
}

export default router;
