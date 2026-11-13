<template>
  <div class="module-missing" :style="mainStyle">
    <div class="site-content">
      <p class="not-found-desc">
        抱歉！您访问的页面<em>出错</em>啦 ...
      </p>
      <p style="color: gray">请尝试刷新页面</p>
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
.module-missing {
  display: flex;
  justify-content:center;
  align-items: center;
  overflow: hidden;
  .site-content {
    text-align: center;
  }
  .not-found-desc {
    margin: 0 0 30px;
    font-size: 26px;
    text-transform: uppercase;
    color: rgb(118, 131, 143);
    > em {
      font-style: normal;
      color: #ee8145;
    }
  }
}
</style>
