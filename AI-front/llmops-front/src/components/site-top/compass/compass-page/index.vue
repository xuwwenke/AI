<template>
  <el-dialog
    custom-class="compass-page"
    :visible.sync="visible"
    append-to-body
    :fullscreen="true"
    @closed="reset"
  >
    <AppShortcuts @setSystem="setSystem" @close="visible = false" />
  </el-dialog>
</template>

<script>
import { switchApp } from '@/router';
import AppShortcuts from "./appShortcuts/App";
export default {
  components: {
    AppShortcuts,
  },
  data() {
    return {
      visible: false,
    };
  },
  methods: {
    init(id) {
      this.visible = true;
    },
    reset() {},
    setSystem(app) {
      switchApp(app)
      this.visible = false;
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep .compass-page.el-dialog {
  background-color: #f1f2f6;
  .el-dialog__header {
    padding: 0;
  }
  .el-dialog__body {
    padding: 20px;
    color: #333;
    .el-input__inner {
      background-color: #fff;
      color: #333;
    }
  }
}
</style>

<style lang="scss">
.el-tooltip__popper.compass-pop {
  background: #fff;
  color: #333;
}
.el-dropdown-menu.compass-pop {
  background: #fff;
  color: #333;
  .el-dropdown-menu__item {
    color: #333;
  }
}
</style>
