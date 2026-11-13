<template>
  <div class="site-root fh">
    <div
      v-if="showContentOnly"
      class="fh"
      :style="{
        boxSizing: 'border-box',
        padding: mainSetting.pageContent.padding,
        background: mainSetting.pageContent.background,
      }"
    >
      <page-title v-if="hasMainPageTitleInIframe" isMainInIframe :setting="mainSetting.pageTitle" />
      <div :style="{ height: hasMainPageTitleInIframe ? 'calc(100% - 33px)' : '100%' }">
        <transition v-if="!isIE" name="fade" mode="out-in">
          <router-view @onUpdateMain="onUpdateMain" />
        </transition>
        <router-view v-else @onUpdateMain="onUpdateMain" />
      </div>
    </div>
    <el-container
      v-else
      :class="{ fh: globalConfig.pageAdaptToWindow }"
      style="min-height: 100%"
    >
      <el-header
        ref="siteHeader"
        class="site-header"
        :height="globalConfig.headerHeight + 'px'"
        :style="{
          zIndex: 1000,
          padding: '0 15px',
          lineHeight: globalConfig.headerHeight + 'px',
        }"
      >
        <!-- <site-top @onCollapse="onCollapse" /> -->
        <site-top @lockScreen="lockScreen" />
      </el-header>
      <el-container direction="vertical" class="site-container" :style="outerStyle">
        <el-header
          ref="horizontalSiteMenu"
          v-if="theme.siteMenuMode == 'horizontal'"
          v-show="showMenu"
          style="height: auto; padding: 0"
        >
          <site-menu mode="horizontal" :collapse="collapse" @updateContent="updateContent" />
        </el-header>
        <el-container :style="innerStyle">
          <el-aside
            v-if="theme.siteMenuMode == 'vertical'"
            v-show="showMenu"
            :class="{'site-aside': true, collapse}"
            :style="siteAsideStyle"
            :width="asideWidth"
            v-eventoutside="{ mode: 'move', outCallback: onMenuMouseOut }"
          >
            <div class="scroll-wrapper">
              <div class="collapse-btn-wrapper">
                <i
                  :class="[
                    'collapse-btn',
                    collapse ? 'fa fa-angle-right' : 'fa fa-angle-left',
                  ]"
                  @click="onCollapse"
                ></i>
              </div>
              <site-menu ref="siteMenu" mode="vertical" :collapse="collapse" @updateContent="updateContent" />
            </div>
          </el-aside>
          <el-main :class="{'site-main': true, 'has-title': hasMainPageTitle}" :style="siteMainStyle">
            <page-title v-if="hasMainPageTitle" isMain :setting="mainSetting.pageTitle" @updateContent="updateContent" />
            <section class="site-main-content" :style="siteMainContentStyle">
              <site-tabs v-if="globalConfig.useTabs && contentRefresh" @onUpdateMain="onUpdateMain" />
              <transition v-else-if="!isIE" name="fade" mode="out-in">
                <keep-alive :include="aiPageNames">
                  <router-view v-if="contentRefresh" @onUpdateMain="onUpdateMain" />
                </keep-alive>
              </transition>
              <keep-alive v-else-if="contentRefresh" :include="aiPageNames">
                <router-view @onUpdateMain="onUpdateMain" />
              </keep-alive>
              <iframe-preloader v-if="globalConfig.iframePreload && !isIE && contentRefresh" />
            </section>
          </el-main>
        </el-container>
        <el-footer
          v-if="globalConfig.showFooter"
          ref="siteFooter"
          class="site-footer"
          height="40px"
          >{{ globalConfig.footerInfo }}</el-footer
        >
      </el-container>
    </el-container>
    <screen-lock
      ref="screenLock"
      :id="id"
      :userName="userName"
      @onClose="unlockScreen"
    ></screen-lock>
  </div>
</template>

<script>
import SiteTop from "@/components/site-top";
import SiteMenu from "@/components/site-menu";
import SiteTabs from "@/components/site-tabs";
import ScreenLock from "@/components/screen-lock";
import { mapState, mapGetters } from "vuex";
import { dispatchResize } from "@/utils/screen";
import { NoEvent } from "@/utils/interaction";
import IframePreloader from "@/components/iframe-preloader";
import { isIE, getParsedUrl } from "@/utils/base";
import merge from 'lodash/merge';
import cloneDeep from 'lodash/cloneDeep';
import { COMPASS_KEY } from '@/consts';
import api_app from '@/api/sys/app'

export default {
  data() {
    return {
      globalConfig: window.globalConfig,
      collapse: window.globalConfig.asideMenuCollapse,
      outerStyle: undefined,
      innerStyle: undefined,
      inIframe: self != top,
      noEvent: null,
      id: null,
      userName: null,
      isIE: false,
      mainSetting: null,
      llmBox: null,
      contentRefresh: true,
    };
  },
  computed: {
    ...mapState(["theme", 'apps']),
    ...mapGetters('apps', ['currentApp']),
    aiPageNames() {
      return ['AILabX', 'Dashboard', 'DataExplorer', 'Datasets', 'Notebook', 'Inference', 'ModelManagement', 'TrainingInstance', 'Orchestration']
    },
    showLLMBox() {
      return window.globalConfig.useLLMBox && (!this.inIframe || window.globalConfig.showLLMBoxInIframe)
    },
    showContentOnly() {
      return this.inIframe && window.globalConfig.showContentOnlyInIframe;
    },
    siteAsideStyle() {
      return {
        minHeight: window.globalConfig.pageAdaptToWindow ? 0 : "600px",
      }
    },
    siteMainStyle() {
      return {
        width: this.isIE ? `calc(100% - ${this.asideWidth})` : undefined,
        height: window.globalConfig.pageAdaptToWindow ? "100%" : "auto",
        minHeight: window.globalConfig.pageAdaptToWindow ? 0 : "600px",
        padding: 0,
      };
    },
    siteMainContentStyle() {
      return {
        height: window.globalConfig.pageAdaptToWindow ? (this.hasMainPageTitle ? 'calc(100% - 40px)' : '100%') : 'auto',
        padding: this.mainSetting.pageContent.padding,
        background: this.mainSetting.pageContent.background,
      };
    },
    // showMenu 不能用 v-if，否则菜单中逻辑不执行，会导致 tabs 不能正常工作
    showMenu() {
      if (this.mainSetting.siteMenu.hidden) return false;
      if (this.$route.name === COMPASS_KEY) return false;
      if (window.globalConfig.menuStyle === 2 && !this.apps.currentFirstLevelMenu.children) return false;
      return true;
    },
    asideWidth() {
      let w = window.globalConfig.asideMenuWidth;
      if (this.theme.name === 'blue-nssa') w += 40;
      return this.collapse ? "64px" : w + "px";
    },
    hasMainPageTitle() {
      return window.globalConfig.useMainPageTitle && !window.globalConfig.useTabs && !this.mainSetting.pageTitle.hidden && this.$route.name !== COMPASS_KEY;
    },
    hasMainPageTitleInIframe() {
      return window.globalConfig.useMainPageTitleInIframe && !this.mainSetting.pageTitle.hidden && this.$route.name !== COMPASS_KEY;
    },
  },
  components: {
    SiteTop,
    SiteMenu,
    SiteTabs,
    ScreenLock,
    IframePreloader,
  },
  watch: {
    $route: {
      handler(v) {
        this.mainSetting = {
          siteMenu: {
            hidden: false
          },
          pageTitle: {
            hidden: false,
            back: null,
            slotItems: [],
            slotTools: []
          },
          pageContent: {
            padding:
              this.$route.meta.iframeUrl || window.globalConfig.useTabs || this.$route.name === COMPASS_KEY
                ? 0
                : window.globalConfig.mainPadding + 'px',
            // background: '#fff',
          }
        };
        // 使用全局配置中的应用设置进行合并，后续可进一步使用页面抛出的设置进行合并
        merge(
          this.mainSetting,
          window.globalConfig.appSettings[this.apps.currentKey]
        );
      },
      immediate: true
    },
    "theme.siteMenuMode": function (v) {
      this.$nextTick(this.updateStyle);
    },
  },
  methods: {
    updateContent () {
      if (!window.globalConfig.refreshContentByMenuAction) return
      this.contentRefresh= false;
      this.$nextTick(() => {
          this.contentRefresh= true;
      });
    },
    /* onUpdateMain 可在特定页面中触发外层容器的调整，使用方式如下：（而应用或子系统级别的统一调整请使用全局配置 appSettings）
    this.$emit("onUpdateMain", {
      siteMenu: {
        hidden: true,
      },
      pageTitle: {
        hidden: true,
        back: () => {},   // 当设置 back: true, 默认使用浏览器的后退
        slotItems: [
          {
            icon: "info-circle",
            name: "追加1-详情",
            click: () => {},
          },
          {
            icon: "edit",
            name: "追加2-编辑",
          }
        ],
        slotTools: [
          {
            icon: "plus-square",
            name: "自定义操作1",
            click: () => {},
          },
          {
            name: "自定义操作2",
            click: () => {},
          },
        ],
      },
      pageContent: {
        padding: 0,
        background: "transparent",
      },
    });
    */
    onUpdateMain(v) {
      // cloneDeep 后再赋值，从而触发视图更新
      this.mainSetting = merge(cloneDeep(this.mainSetting), v);
    },
    onCollapse() {
      this.collapse = !this.collapse;
      // 等待折叠动画完毕后触发 resize
      setTimeout(dispatchResize, 300);
    },
    updateStyle() {
      if (this.showContentOnly) return;
      if (window.globalConfig.pageAdaptToWindow) {
        const h = this.$refs.siteHeader.$el.offsetHeight;

        this.outerStyle = { height: "calc(100% - " + h + "px)" };

        let h2 = 0;
        if (this.theme.siteMenuMode == "horizontal") {
          h2 = this.$refs.horizontalSiteMenu.$el.offsetHeight;
        }

        const h3 = window.globalConfig.showFooter
          ? this.$refs.siteFooter.$el.offsetHeight
          : 0;

        this.innerStyle = {
          height: "calc(100% - " + h2 + "px - " + h3 + "px)",
        };
      }
    },
    lockScreen() {
      // console.log("lockScreen")
      if (window.globalConfig.lockScreenTime) {
        this.noEvent.off();
      }
      this.$refs.screenLock.init();
      localStorage.setItem("screenLocked", "1");
    },
    unlockScreen() {
      // console.log("unlockScreen")
      if (window.globalConfig.lockScreenTime) {
        this.noEvent.on();
      }
      localStorage.setItem("screenLocked", "0");
    },
    onMenuMouseOut() {
      this.$refs.siteMenu.onMenuMouseOut && this.$refs.siteMenu.onMenuMouseOut();
    },
    useLLMBox() {
      if (window.globalConfig.everllmUrl) {
        this.loadScript(window.globalConfig.everllmUrl)
      } else {
        api_app
          .getList({
            startPosition: 1,
            maxResult: 10000,
          })
          .then(res => {
            const appList = res.resultData || []

            let app = appList.find(app => app.key === 'everllm')
            if (app && app.url) {
              let url = getParsedUrl(app.url)
              const urlObj = new URL(url)
              url = urlObj.origin + urlObj.pathname

              this.loadScript(url)
            }
          })
      }
    },
    loadScript(url) {
      if (url.at(-1) !== '/') url += '/'
      url += 'llm-box.js'

      const scriptId = 'llm-box-script'
      if (!document.getElementById(scriptId)) {
        let script = document.createElement('script')
        script.id = scriptId
        script.src = url
        script.onload = () => {
          this.initLLMBox()
        }
        document.body.appendChild(script)
      }
    },
    initLLMBox() {
      this.llmBox = new LLMBox(window.globalConfig.llmBoxOption)
      this.llmBox.create()
      this.$watch('currentApp', v => {
        this.llmBox.setApp(v)
      }, { immediate: true })
    }
  },
  created() {
    this.id = localStorage.getItem("id");
    this.userName = localStorage.getItem("userName");
    this.isIE = isIE();
  },
  mounted() {
    this.updateStyle();
    // 为 body 设样式
    document.body.style.overflow =
      window.globalConfig.pageAdaptToWindow && !this.inIframe
        ? "hidden"
        : "visible";

    // 添加锁屏事件
    if (window.globalConfig.lockScreenTime) {
      this.noEvent = new NoEvent(() => {
        this.lockScreen();
      }, window.globalConfig.lockScreenTime);
      this.noEvent.on();
    }

    // 检查锁屏状态，防止刷新造成解锁
    if (localStorage.getItem("screenLocked") == "1") {
      this.lockScreen();
    }

    if (this.showLLMBox) this.useLLMBox()
  },
  destroyed() {
    // 避免登录页无法滚动
    document.body.style.overflow = "visible";
    // 移除锁屏事件
    if (window.globalConfig.lockScreenTime) {
      this.noEvent.off();
    }

    if (this.llmBox) {
      this.llmBox.destroy()
      this.llmBox = null
    }
  },
};
</script>

<style lang="scss">
.site-aside {
  position: relative;
  /* background: #454545; */
  /* color: #333; */
  transition: width 0.3s;
  // overflow-x: hidden !important;
  // width: auto !important;
  overflow: visible !important;
  // height: 100%; /* 兼容 ie9 */
  float: left; /* 兼容 ie9 */
  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  // 避免折叠按钮随菜单滚动
  .scroll-wrapper {
    width: 100%;
    height: 100%;
    background: inherit;
    overflow-x: hidden;
    overflow-y: hidden;
    &:hover {
      overflow-y: auto;
    }
  }
  .collapse-btn-wrapper {
    position: absolute;
    // z-index: 10000;
    top: 0;
    right: 0;
    width: 25px;
    height: 25px;
    overflow: hidden;
    pointer-events: none;
  }
  .collapse-btn {
    // display: none;
    position: absolute;
    z-index: 1;
    top: 1px;
    right: -20px;
    padding: 3px 7px;
    font-size: 14px;
    cursor: pointer;
    background: #383838; /* IE 兼容 */
    color: white; /* IE 兼容 */
    background: var(--site-menu-hover-bg);
    color: var(--site-menu-hover-txt);
    border-radius: 5px 0 0 5px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
    transition: right 0.3s;
    pointer-events: auto;
  }
  &:hover {
    .collapse-btn {
      // display: block;
      right: 0;
    }
  }
}
.site-main {
  .site-main-content {
    overflow: auto;
  }
}
.fade-enter-active,
.fade-leave-active {
  overflow-x: hidden;
  transition: all 0.1s ease;
}
.fade-enter,
.fade-leave-to {
  transform: translateX(10px);
  opacity: 0;
}
</style>
