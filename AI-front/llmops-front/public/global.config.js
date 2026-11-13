// 全局配置
window.globalConfig = {
  /**
   * 环境配置
   */
  DOMAIN: "",                                   // 域配置，配合 evercloud 使用，将域名中需要变动的部分放在这里，例如：everbi-develop.wh.everark.com.cn，然后在配置地址时使用类似 http://everbi-front.${DOMAIN} 以便切换环境。当值含有 ip 地址时，将替换整个域名

  /**
   * 免密配置（详见 /guest/index.html，注意后台不能开启验证码）
   */
  guestUser: "",                                // 游客身份的用户名
  guestPwd: "",                                 // 游客身份的密码

  /**
   * 基础配置
   */
  defaultProxyPath: "./service",                // 默认代理路径，开发环境下需要与 vue.config.js 中的代理配置对应，生产环境下需要与 nginx 中的代理配置对应
  requestTimeout: undefined,                    // 请求超时时间（undefined 表示不做控制），单位毫秒，直接填入数字即可，示例：1000 * 30，表示 30 秒
  showRecommendedBrowser: true,                 // 显示推荐使用 Chrome 浏览器的提示
  forcePasswordChange: true,                    // 密码过期时，强制修改密码（无法跳过）
  lockScreenTime: undefined,                    // 长时间无交互自动锁屏（undefined 或 0 表示不自动锁屏），单位毫秒，直接填入数字即可，示例：1000 * 60 * 10，表示 10 分钟
  iframePreload: false,                         // 对 iframe 嵌入的第三方应用进行预载（IE除外），当 useTabs 为 false 时，还会进行预渲染
  iframePreloadDelay: 5000,                     // iframe 页面预载的延迟时间，单位毫秒（因预载开始时可能出现卡顿，请选择合适的时机）
  triggerParentLogout: true,                    // session 失效时，触发父系统（需基于 evervue）登出
  allowLogoutByIframe: false,                   // 允许 iframe 嵌入的第三方应用触发当前系统登出

  /**
   * 菜单与权限
   */
  prdKey: "",                                   // 产线关键字，不为空时仅获取该产线下的应用和菜单（可选）
  appKey: "llmops",                             // 应用关键字，登录时所需参数，将作为默认应用，以及应用切换时的原始应用（必填）
  showAppSelect: false,                         // 显示应用切换，开启后总是只显示当前应用下的菜单（无视 showFullMenu 和 showSystemMenu），呈现在引导页的应用才会显示
  appSelectStyle: 1,                            // 应用切换样式，0 下拉框 | 1 顶部平铺
  appMoreStyle: 1,                              // 更多应用的展开样式，0 下拉框 | 1 平铺列表（适合应用较多时）
  showFullMenu: false,                          // 显示完整菜单，true 将显示该用户在各app（包括系统管理）下所有菜单的组合，false 仅显示当前appKey对应的app下的菜单
  showSystemMenu: true,                         // 显示系统管理菜单，前提是用户拥有系统管理的菜单权限
  menuStyle: 1,                                 // 菜单样式，0 经典 | 1 右侧展开 | 2 一级菜单在顶部（此模式下应用切换不可用）
  homePageUrl: "",                   // 默认首页地址（留空时将自动采用第一个菜单页面），支持第三方页面、菜单页面、未配置成菜单的页面，支持 ${ip} 和 ${port} 的转义
  homePageIsGlobal: false,                      // 默认首页是否全局页面（即没有顶部栏、左侧菜单栏、底部栏），仅当 homePageUrl 非空时生效
  verifyAuth: false,                            // 是否开启 license 授权校验
  encryptEAuthAppKey: false,                    // E-AUTH-APP-KEY 是否需要加密，与网关配置 gateway.appDecrypt、gateway.appExcept 搭配
  everbiEAuthAppKey: "",                        // 特殊处理，请求 everbi 服务时，传入的 E-AUTH-APP-KEY（从而避免传入当前应用的 key）
  isEnableSMSLogin: false,                      // 是否启用短信登录

  /**
   * 显示与交互
   */
  useTabs: false,                               // 使用标签形式打开页面
  pageAdaptToWindow: true,                      // 页面适应窗口，true 页面高度与窗口一致，侧边栏和内容在div内滚动，false 页面自然增长高度
  headerHeight: 50,                             // 顶部栏高度，单位 px
  mainPadding: 20,                              // 正文内容内边距（iframe除外），单位 px
  showFooter: false,                            // 显示底部栏
  asideMenuWidth: 200,                          // 左侧菜单栏宽度，单位 px
  asideMenuUniqueOpened: true,                  // 左侧菜单只保持一个子菜单的展开
  asideMenuCollapse: false,                     // 左侧菜单默认折叠
  themes: [
    { name: "default", label: "默认主题" },
    { name: "orange", label: "醒目橙色" },
    { name: "blue", label: "蓝色海洋" },
    { name: "dark", label: "浩瀚星空" },
    { name: "dark-blue", label: "深蓝波纹" },
    { name: "light", label: "清新淡雅" },
    { name: "blue-nssa", label: "金御蓝色" },
    { name: "cyber", label: "赛博科技" },
    { name: "modern", label: "简洁现代" },

  ],                                            // 平台主题列表，对应 index.html 目录下的 themes 文件夹
  theme: "modern",                             // 初始平台主题（需从主题列表中选择），仅在 localStorage 中没有该值或不显示平台主题切换控件时生效
  siteMenuMode: "vertical",                     // 初始菜单显示位置 vertical（左侧纵向） / horizontal（顶部横向），仅在 localStorage 中没有该值时生效
  showErrorLog: false,                          // 显示顶部栏异常日志控件
  showChangeTheme: true,                        // 显示顶部栏平台主题切换控件
  showUserOpt: true,                            // 显示顶部栏用户操作控件
  showChangePassword: true,                     // 显示修改密码按钮（用户操作下拉列表中）
  showChangeMenuMode: true,                     // 显示菜单位置切换按钮（用户操作下拉列表中）
  showLockScreen: true,                         // 显示锁屏按钮（用户操作下拉列表中）
  showVersionInfo: true,                        // 显示版本信息（用户操作下拉列表中及登录页中）
  showMyLogs: true,                             // 显示我的日志（用户操作下拉列表中）
  showVpn: false,                               // 登录日志是否展示vpn登录的信息
  pageTitleBreadcrumb: true,                    // 页面标题栏显示面包屑
  useMainPageTitle: true,                       // 使用主框架的统一页面标题（此时各页面内的标题不会显示）
  useMainPageTitleInIframe: false,              // 当前系统被 iframe 嵌入时，使用主框架的统一页面标题（此时各页面内的标题不会显示）
  showContentOnlyInIframe: true,                // 当前系统被 iframe 嵌入时，仅显示页面内容
  showContentPageTitleInIframe: true,           // 当前系统被 iframe 嵌入时，显示页面内容中的标题
  showPageTitleInIframe: false,                 // 当前系统通过 iframe 加载第三方页面时，显示本系统生成的标题
  useIconfont: false,                           // 使用 iconfont 图标，需在 index.html 同目录下存在相应的 iconfont 文件夹（从官网下载资源包后解压得到）
  uiSize: "small",                              // Element UI 组件的默认尺寸 large / medium / small / mini
  refreshContentByMenuAction: false,            // 点击菜单或面包屑最后一级，会刷新当前页面内容（常用于将页面重置到初始状态）
  loginNoAnimation: false,                      // 登录页不显示动画

  /**
   * logo、标题和文字
   * 登录页主标题和副标题都为空时，显示默认图片标题
   */
  loginTitle: "AILabX",                               // 登录页主标题
  loginSubTitle: "AI中台",                            // 登录页副标题
  loginPageUrl: "",                             // 自定义登录页地址
  headerLogo: "favicon.ico",                               // 顶部栏 logo（相对于 index.html 的路径，示例：logo.png，没有 logo 时留空，注意不要用中文）
  headerLogoHeight: 25,                          // 顶部栏 logo 高度，单位 px（可选，为 0 时自适应）
  headerTitle: "AILabX",                       // 顶部栏标题（同时会替换网页 title）
  footerInfo: "恒安嘉新（北京）科技股份公司-技术支持",                  // 底部栏文字

  /**
   * 使用 BI 增强
   */
  useBiDic: false,                              // 使用 bi 增强数据字典

  useBiCompass: false,                          // 是否使用应用导航功能（需要bi后台接口支持）
  compassBtnName: "应用导航",                    // 导航按钮的名字
  compassMode: 1,                               // 0 仅显示叶子节点菜单 1 仅显示第一级菜单
  compassStyle: 1,                              // 0 原版样式 1 everone样式
  // 仅当 compassStyle 为 0 时，以下设置才能看到效果
  showCompassPageAfterLogin: true,              // 登录成功后进入默认页时，是否自动弹出旧版导航页
  // 仅当 compassStyle 为 1 时，以下设置才能看到效果
  isEverOne: false,                             // 是否产品能力展示中心
  showCustomApp: false,                         // 应用导航中显示自定义应用
  compassSubBtn: {                              // 应用导航副按钮
    name: "",                                   // 按钮名称
    url: "",                                    // 跳转地址，例如：/#/sys/app?_app_=system
    style: { color: "orange", fontSize: "16px" }, // 按钮 css 样式
  },
  deployedAlias: "已部署",                       // 应用导航中“已部署”标签的别名
  undeployedAlias: "未部署",                     // 应用导航中“未部署”标签的别名

  /**
   * 应用级别整体个性化设置，其中 xxx 为 appkey，而页面级别的设置可通过 this.$emit("onUpdateMain", {}) 单独抛出，详见 main.vue
   */
  appSettings: {
    // xxx: {
    //   siteMenu: {
    //     hidden: true,
    //   },
    //   pageTitle: {
    //     hidden: true,
    //   },
    //   pageContent: {
    //     padding: 0,
    //     background: "transparent",
    //   },
    // }
  },

  /**
   * LLM 大模型（旧版已弃用）
   */
  /* useLLMBox: false,                             // 使用大模型会话小窗口，页面形式集成无需配置（在菜单中配置 http://xxx/#/page-root/client 即可）
  everllmUrl: "",                               // 大模型服务前端地址（可选，若为空，将自动从系统应用列表中获取，例：http://everllm-front.${DOMAIN}）
  showLLMBoxInIframe: false,                    // 当前系统被 iframe 嵌入时，是否显示 LLM 小窗
  llmBoxOption: {                               // 小窗初始化参数（按需设置）
    // bigscreen: false,
    // docked: false,
    // width: 580,
    // height: 600,
    // maxHeight: '100vh',
    // bottom: '0px',
    // right: '0px',
    // zIndex: 1000,
  }, */


  /**************************************************************************************
   * Docker 用来设置环境变量的埋点（为注释状态），必须在结尾，不要删
   **************************************************************************************/
  // ${GLOBAL_CONFIG_EXPAND}
}
