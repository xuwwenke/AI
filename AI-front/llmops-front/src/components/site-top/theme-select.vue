<template>
  <el-select :value="theme.name" @change="onChangeTheme" size="mini" style="width: 100px;">
    <el-option
      v-for="item in globalConfig.themes"
      :key="item.name"
      :value="item.name"
      :label="item.label"
    ></el-option>
  </el-select>
</template>

<script>
import { dispatchResize } from '@/utils/screen';
import { mapState, mapMutations } from "vuex";

export default {
  data() {
    return {
      globalConfig: window.globalConfig
    };
  },
  computed: mapState(["theme"]),
  methods: {
    ...mapMutations("theme", ["setTheme"]),
    onChangeTheme(name) {
      this.setTheme(name);
      localStorage.setItem("theme", name);
      // 主题可能会改变某些参与计算的元素宽高（比如表格翻页器），触发 resize 来重新计算
      dispatchResize(); // css已经有缓存的情况
      setTimeout(dispatchResize, 500); // css无缓存的情况，等加载完触发
    }
  }
};
</script>
