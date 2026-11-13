<!-- 数字输入正则限制框 -->
<template>
  <div class="menu-popup-windows">
    <div class="popup-windows">
      <ul v-if="!loading && dataList.length > 0">
        <li v-for="(item, index) in dataList" :key="index">
          <span>{{ item.name }}</span>
          <a
            href="javascript:;"
            @click="curmbClick(item)"
            v-html="hightLightCrumb(item.crumb)"
          ></a>
        </li>
      </ul>
      <div v-if="!loading && dataList.length === 0" class="noData">
        暂无数据，请修改搜索条件
      </div>
      <div v-if="loading" class="noData"><i class="el-icon-loading" /></div>
    </div>
  </div>
</template>

<script>
import { replaceAll } from "@/utils/base";
import { menuAction } from "@/router";

export default {
  name: "menu-popup-windows",
  data() {
    return {
      loading: true,
      inputStr: "",
      dataList: [],
    };
  },
  mounted() {
    this.loading = true;
    this.$bus.on("UPDATE_SEARCH", (target) => {
      this.inputStr = target.param1;
      this.dataList = target.param2;
      if (this.dataList) {
        this.loading = false;
      }
    });
  },
  methods: {
    curmbClick(item) {
      menuAction(item);
      this.$emit('setSystem', item.app);

      this.$emit("close");
    },
    hightLightCrumb(crumb) {
      var newCrumb = "";
      try {
        newCrumb = replaceAll(
          crumb,
          this.inputStr,
          '<span style="font-weight: bold;">' + this.inputStr + "</span>"
        );
      } catch (error) {
        newCrumb = crumb;
      }
      return newCrumb;
    },
  },
  beforeDestroy() {
    this.$bus.off("UPDATE_SEARCH");
  },
};
</script>

<style scoped lang="scss">
@import "variable.scss";
.menu-popup-windows {
  position: relative;
  &:before {
    content: "";
    position: absolute;
    z-index: 2;
    right: 1em;
    left: 1em;
    height: 1px;
    background-color: $color-gray1;
  }
  .popup-windows {
    position: absolute;
    z-index: 1;
    background-color: white;
    border: 2px solid $color-theme;
    border-top: none;
    box-sizing: border-box;
    width: 100%;
    padding-right: 1em;
    padding-left: 1em;
    max-height: 19em;
    overflow: auto;
    ul {
      list-style: none;
      margin: 0;
      padding: 0.5em 0;
      line-height: 1.8em;
      li span {
        // color: $color-gray1;
        color: #333333;
        font-weight: 700;
        margin-right: 1em;
      }
    }
    .noData {
      border-top: 1px solid $color-gray1;
      text-align: center;
      padding: 1em 0;
    }
  }
}
</style>
