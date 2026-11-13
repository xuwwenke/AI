<template>
  <span>
    <el-tooltip :content="globalConfig.compassBtnName" placement="bottom">
      <i class="fa fa-paper-plane" @click="showCompassPage"></i>
    </el-tooltip>
    <!-- <compass-page-new v-if="globalConfig.compassStyle === 1" ref="compassPage" /> -->
    <compass-page ref="compassPage" />
  </span>
</template>

<script>
import CompassPage from "./compass-page";
// import CompassPageNew from "./compass-page-new";
export default {
  components: {
    CompassPage,
    // CompassPageNew,
  },
  data() {
    return {
      globalConfig: window.globalConfig,
    };
  },
  mounted() {
    this.autoShowCompass();
  },
  methods: {
    autoShowCompass() {
      if (window.globalCache.autoShowCompass || this.$route.query.autoShowCompass) {
        window.globalCache.autoShowCompass = false;
        this.showCompassPage();
      }
    },
    showCompassPage() {
      this.$refs.compassPage && this.$refs.compassPage.init();
    },
  },
};
</script>
