<template>
  <el-dialog
    title="我的日志"
    top="5vh"
    :visible.sync="visible"
    :close-on-click-modal="false"
    append-to-body
    width="75%"
    custom-class="saveAsDialog"
  >
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="登录日志" name="first">
        <login-log></login-log>
      </el-tab-pane>
      <el-tab-pane label="操作日志" name="second">
        <opt-log></opt-log>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>
<script>
import loginLog from "./login-log.vue";
import optLog from "./opt/index.vue";
export default {
  components: {
    loginLog,
    optLog,
  },
  data() {
    return {
      visible: false,
      activeName: "first",
    };
  },
  mounted() {},
  methods: {
    init() {
      this.visible = true;
    },
    handleClick(tab, event) {},
  },
};
</script>
<style lang="scss" scoped>
::v-deep .el-dialog__body{
  min-height:680px;
  padding: 0 20px 30px !important;
}

::v-deep .saveAsDialog {
  min-width: 1400px;
}
</style>
