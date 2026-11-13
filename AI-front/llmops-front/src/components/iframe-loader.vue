<template>
  <div class="fw fh" v-loading="loading">
    <page-title
      v-if="needPageTitle"
      class="title"
      :style="{
        padding: `${globalConfig.mainPadding}px ${globalConfig.mainPadding}px 0`,
      }"
    />
    <div
      :style="{
        height: needPageTitle
          ? `calc(100% - ${globalConfig.mainPadding + 22}px)`
          : '100%',
      }"
    >
      <iframe
        ref="iframe"
        :key="fixedSrc"
        :src="fixedSrc"
        width="100%"
        height="100%"
        style="display: block"
        frameborder="0"
        allowfullscreen="true"
      ></iframe>
    </div>
  </div>
</template>

<script>
import api_auth from "@/api/auth";
import { clearLoginInfo, getParsedUrl, getfixedUrl } from "@/utils/base";
import { mapState } from "vuex";
import { pubDicUpdated, subDicUpdated } from "@/utils/dic";
import { merge, isEqual } from 'lodash'

export default {
  props: {
    src: {
      type: String,
      default: "",
    },
    // 需要加载动画
    needLoading: {
      type: Boolean,
      default: true,
    },
    // 是否页面级别，而不是对话框级别
    isPage: {
      type: Boolean,
      default: false,
    },
    // 处于仪表盘时，会接收到查询条件过滤
    filters: {
      type: Object,
    },
    // 是否进行消息通讯，预载时可设为false
    needMsg: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      globalConfig: window.globalConfig,
      loading: false,
      fixedSrc: null,
    };
  },
  computed: {
    ...mapState(["theme"]),
    needPageTitle() {
      return this.isPage && window.globalConfig.showPageTitleInIframe;
    },
  },
  watch: {
    $route: {
      handler(v) {
        this.fixedSrc = this.getFixedSrc(this.src, v);
      },
    },
    src: {
      handler(v) {
        this.fixedSrc = this.getFixedSrc(v, this.$route);

        if (this.needLoading && v != null && v != "") {
          this.loading = true;

          // 一段时间后，不再显示loading，避免长页面加载了大部分，还剩小部分卡住，造成loading遮挡
          setTimeout(() => {
            this.loading = false;
          }, 3000);
        }

        this.$nextTick(() => {
          if (this.$refs.iframe) {
            this.$refs.iframe.onload = () => {
              this.loading = false;
              this.setIframeTheme(this.theme.name);
              this.setFilters(this.filters);
              this.$emit("onload");
            };
          }
        });
      },
      immediate: true,
    },
    "theme.name"(v) {
      this.setIframeTheme(v);
    },
    filters(v, oldV) {
      if (!isEqual(v, oldV)) this.setFilters(v)
    },
  },
  created() {
    // 关注任何字典更新，并传递给 iframe
    subDicUpdated(undefined, (data) => {
      this.sendDicUpdated(data);
    });
  },
  mounted() {
    if (this.needMsg) window.addEventListener("message", this.messageHandler);
  },
  destroyed() {
    if (this.needMsg)
      window.removeEventListener("message", this.messageHandler);
  },
  methods: {
    getFixedSrc(src, route) {
      if (!src) return null;
      const parsedUrl = getParsedUrl(src);
      const fixedUrl = getfixedUrl(parsedUrl);

      /* 提供为iframe动态追加url参数的一种途径，可以与菜单地址中的参数并存
      将iframe所需的多个参数，通过 encodeURIComponent 编码后，作为外层 _iframeQuerys_ 参数的值
      例如：_iframeQuerys_=test1%3D3%26test2%3D4 将会传递给 iframe 参数 test1=3&test2=4 */
      let dynamicIframeQuery = "";
      const iframeQuerys = route.query._iframeQuerys_;
      if (iframeQuerys) {
        const sign = fixedUrl.includes("?") ? "&" : "?";
        dynamicIframeQuery = sign + iframeQuerys;
      }

      let dynamicIframeParam = "";
      const iframeParams = route.query._iframeParams_;
      if (iframeParams) {
        dynamicIframeParam = "/" + iframeParams;
      }

      return fixedUrl + dynamicIframeParam + dynamicIframeQuery;
    },
    // 往 iframe 传递 theme
    setIframeTheme(data) {
      this.sendMsg({ msg: "theme", payload: data });
    },
    // 往 iframe 传递 查询条件
    setFilters(data) {
      if (data) this.sendMsg({ msg: "filters", payload: data });
    },
    // 往 iframe 传递 字典更新，给预渲染的页面用
    sendDicUpdated(data) {
      this.sendMsg({ msg: "dicUpdated", payload: data });
    },
    // 往 iframe 发消息
    sendMsg(msgObject) {
      this.needMsg &&
        this.$refs.iframe &&
        this.fixedSrc &&
        this.$refs.iframe.contentWindow.postMessage(msgObject, this.fixedSrc);
    },
    // 来自 iframe 的消息
    messageHandler(event) {
      // 如果消息源不是 iframe 页
      if (
        !this.$refs.iframe ||
        event.source !== this.$refs.iframe.contentWindow
      )
        return;

      console.log("iframe-loader 接收到消息：", event);

      this.$emit("messageHandler", event);

      // 来自 iframe 的登出消息
      if (
        event.data.msg == "onLogout" &&
        window.globalConfig.allowLogoutByIframe
      ) {
        // evercloud 使用了域名，再要求 ip 和 hostname 相同就不合适了
        // const event_host = event.origin.split("//")[1];
        // const event_hostname = event_host && event_host.split(":")[0];
        // // 忽略来自不同 ip/hostname 的消息
        // if (event_hostname !== location.hostname) return;

        const payload = event.data.payload;

        if (payload && payload.message) {
          this.$message({
            message: payload.message,
            type: "error",
            duration: 1500,
          });
        }

        if (payload && payload.needLogout) {
          api_auth.logout().then(() => {
            clearLoginInfo();
            this.$router.push({
              name: "login",
            });
          });
        } else {
          clearLoginInfo();
          this.$router.push({
            name: "login",
            params: { target: this.$router.currentRoute.path },
          });
        }
      }

      // 来自 iframe 的路由跳转消息
      if (event.data.msg == "onRouterPush") {
        const payload = event.data.payload;
        if (payload) {
          this.$router.push(payload);
        }
      }

      /* 来自 iframe 的路由合并消息，深度合并，可实现 iframe 地址的更新，使用方式如下：
      if(self != top) {
        document.referrer &&
          parent.postMessage(
            {
              msg: "onRouterMerge",
              payload: {
                query: {
                  _iframeParams_: 123,                  // 路由的动态参数
                  _iframeQuerys_: 'test1=3&test2=4',    // url参数，这里不需要编码
                }
              }
            },
            document.referrer
          );
      }
      这样iframe的url就变化了，比如原来是 /some，现在就是 /some/123?test1=3&test2=4 对应的是 id 为 123 的详情页，且携带 test1 和 test2 参数。
      本来iframe内路由变化，外层系统是感知不到的。通过这个消息，将iframe的路由变化通知到外层，外层自动生成返回按钮
      */
      if (event.data.msg == "onRouterMerge") {
        const payload = event.data.payload;
        if (payload) {
          const oldRoute = { ...this.$route };
          this.$router.push(merge({}, this.$route, payload)).catch((e) => {});

          this.$nextTick(() => {
            this.$emit("onUpdateMain", {
              pageTitle: {
                back: () => this.$router.push(oldRoute),
              },
            });
          });
        }
      }

      // 来自 iframe 的更新字典缓存消息
      if (event.data.msg == "onDicUpdated") {
        const payload = event.data.payload;
        if (payload) {
          pubDicUpdated(payload);
        }
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.title {
  margin-bottom: -10px;
}
</style>
