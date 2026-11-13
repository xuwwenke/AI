<template>
  <el-dialog
    title="版本说明"
    top="30vh"
    :visible.sync="visible"
    :close-on-click-modal="false"
    append-to-body
  >
    <div class="container">
      <div class="title">{{ currentVersion.name }}</div>
      <div class="desc mb20">
        {{ currentVersion.description }}
      </div>
      <div class="title">版本名称：</div>
      <div class="desc mb30">
        <span>{{ currentVersion.version }}</span>
        <el-button type="text" @click="handleMore" class="link"
          >更多 >></el-button
        >
      </div>
      <div class="title">
        {{ globalConfig.footerInfo }}
      </div>
    </div>
    <version-list ref="versionRef"></version-list>
  </el-dialog>
</template>

<script>
import VersionList from "./versionList.vue";

export default {
  components: { VersionList },
  data() {
    return {
      globalConfig: window.globalConfig,
      visible: false,
    };
  },
  computed: {
    currentVersion(){
      let version = localStorage.getItem("currentVersion");
      return version ? JSON.parse(version) : {}
    }
  },
  created() {
  },
  methods: {
    init() {
      this.visible = true;
    },
    handleMore() {
      this.$refs.versionRef.init();
    },

  }
};
</script>
<style lang="scss" scoped>
.container {
  padding-left: 34px;
  color: #666;
  position: relative;
  &:before {
    content: "";
    width: 24px;
    height: 24px;
    background: url("./version.png") no-repeat center / contain;
    position: absolute;
    left: 0px;
    top: 0;
  }
  .title {
    font-size: 16px;
    font-weight: 600;
  }
  .desc {
    font-size: 14px;
    line-height: 24px;
    .link {
      margin-left: 16px;
      font-size: 14px;
      text-decoration: underline;
    }
  }
  .mb20 {
    margin-bottom: 20px;
  }
  .mb30 {
    margin-bottom: 30px;
  }
}
</style>
