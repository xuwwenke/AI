<template>
  <div class="module-loading" :style="mainStyle">
    <div class="site-loading">
      <div class="spinner">
        <div class="rect1"></div>
        <div class="rect2"></div>
        <div class="rect3"></div>
        <div class="rect4"></div>
        <div class="rect5"></div>
      </div>
      <p class="tip">页面加载中...</p>
    </div>
  </div>
</template>

<script>
export default {
  computed: {
    mainStyle() {
      return {
        height: window.globalConfig.pageAdaptToWindow ? "100%" : "600px"
      };
    }
  }
};
</script>

<style lang="scss" scoped>
.module-loading {
  position: relative;
}
</style>
