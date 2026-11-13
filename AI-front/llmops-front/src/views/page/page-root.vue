<template>
  <div>{{ $route.params }}</div>
</template>

<script>
// 用于显示单页（不嵌套上左右整体布局）
import { mapMutations } from "vuex";

export default {
  watch: {
    $route: {
      handler(v) {
        this.setTheme(v.query.theme || "default");
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    ...mapMutations("theme", ["setTheme"])
  }
};
</script>
