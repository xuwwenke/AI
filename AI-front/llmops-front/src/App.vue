<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
import { mapState, mapMutations } from "vuex";
import { pubDicUpdated } from "@/utils/dic";

export default {
  computed: mapState(["theme"]),
  created() {
    this.setTheme(this.theme.name);

    // 监听消息
    if (self != top) window.addEventListener("message", this.messageHandler);

    if (
      window.globalConfig.showRecommendedBrowser &&
      navigator.userAgent.indexOf("Chrome") == -1
    ) {
      this.$notify({
        title: "提示",
        message: "推荐使用 Chrome 浏览器访问系统，以达到最佳效果",
        type: "warning"
      });
    }
  },
  methods: {
    ...mapMutations("theme", ["setTheme"]),
    messageHandler(event) {
      // 如果消息源不是父级（iframe 或调用 window.open）
      if (event.source !== parent && event.source !== window.opener) return;
      console.log("iframe 接收到消息：", event);
      if (event.data.msg === "theme") {
        this.setTheme(event.data.payload);
      }
      if (event.data.msg === "dicUpdated") {
        pubDicUpdated(event.data.payload);
      }
    }
  }
};
</script>

<style lang="scss">
#app {
  height: 100%;
  // font-family: "Avenir", Helvetica, Arial, sans-serif;
  font-family: PingFang SC,Hiragino Sans GB,Microsoft YaHei,SimSun,sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
