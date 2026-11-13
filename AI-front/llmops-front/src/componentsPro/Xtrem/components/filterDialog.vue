<template>
  <el-dialog
    width="1000px"
    :visible.sync="visible"
    :close-on-click-modal="false"
    title="过滤文本"
    append-to-body
  >
    <div class="filter-context">
      <div v-html="context" v-if="context"></div>
      <div v-else class="text-center fz24 mt200">未匹配到对应信息</div>
    </div>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      context: "",
      visible: false
    };
  },
  methods: {
    show(data) {
      this.context = data;
      this.visible = true;
    }
  }
};
</script>

<style lang="scss" scoped>
.filter-context {
  background: #000;
  color: #fff;
  padding: 10px;
  height: 500px;
  overflow-y: auto;
}
</style>
