<template>
  <div v-show="iframe_app && !globalConfig.useTabs" class="wrapper" :style="wrapperStyle">
    <!-- 将 v-for 和 v-if 分开，以免渲染机制紊乱，造成直接打开某个iframe，经指定延迟后又重复加载 -->
    <div
      v-for="(preload_url, app_url) in globalCache.preload_urls"
      :key="app_url"
      class="inner"
    >
      <iframe-loader
        v-if="iframeUrls[app_url]"
        v-show="iframe_app == app_url"
        :src="iframeUrls[app_url]"
        :needLoading="false"
        :isPage="true"
        :needMsg="!globalConfig.useTabs"
      />
    </div>
  </div>
</template>

<script>
import { getAppUrl } from "@/utils/base";
import IframeLoader from "@/components/iframe-loader";

export default {
  components: {
    IframeLoader,
  },
  data() {
    return {
      globalConfig: window.globalConfig,
      globalCache: window.globalCache,
      iframe_app: null,
      iframeUrls: {},
    };
  },
  computed: {
    wrapperStyle() {
      return {
        minHeight: window.globalConfig.pageAdaptToWindow ? 0 : "600px",
      };
    },
  },
  created() {
    // 注意：会在 $route 的 watch 之后执行
    setTimeout(() => {
      for (let app_url in this.globalCache.preload_urls) {
        const preload_url = this.globalCache.preload_urls[app_url];
        if (!this.iframeUrls[app_url]) {
          this.$set(this.iframeUrls, app_url, preload_url);
        }
      }
    }, window.globalConfig.iframePreloadDelay);
  },
  watch: {
    $route: {
      handler(v) {
        if (window.globalConfig.useTabs) return;

        const meta = v.meta;
        if (meta && meta.iframeUrl) {
          this.iframe_app = getAppUrl(meta.iframeUrl);

          this.$set(this.iframeUrls, this.iframe_app, meta.iframeUrl);
        } else {
          this.iframe_app = null;
        }
      },
      immediate: true,
    },
  },
};
</script>

<style lang="scss" scoped>
.wrapper {
  position: relative;
  height: 100%;
}
.inner {
  position: absolute;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  > * {
    pointer-events: auto;
  }
}
</style>
