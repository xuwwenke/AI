<template>
  <el-dialog
    title="自定义导出"
    :close-on-click-modal="false"
    :visible.sync="visible"
  >
    <el-tree
      ref="exportTree"
      class="menu-tree"
      show-checkbox
      :data="list"
      node-key="id"
      :empty-text="appKey ? undefined : '请先选择系统'"
      highlight-current
    >
      <span slot-scope="{ data }" :class="'type-' + data.type">
        <i v-if="data.icon" :class="'mr5 fa fa-' + data.icon"></i>
        <span>{{ data.name }}</span>
      </span>
    </el-tree>

    <div slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="confirm">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { downloadFile } from "@/utils/download";
import api_menu from "@/api/sys/menu";
export default {
  data() {
    return {
      visible: false
    };
  },
  props: {
    list: {
      required: true
    },
    appKey: {}
  },
  methods: {
    init() {
      this.visible = true;
    },
    getCheckedKeys() {
      console.log(this.$refs.exportTree.getCheckedKeys());
    },
    confirm() {
      let result = this.$refs.exportTree
        .getCheckedKeys()
        .concat(this.$refs.exportTree.getHalfCheckedKeys());
      if (!result.length) {
        this.$message({
          type: "error",
          message: "请选择要导出的数据"
        });
        return;
      }
      let menuIds = result.join(",");
      if (!this.appKey || !menuIds) {
        return false;
      }
      downloadFile(api_menu.download({ appKey: this.appKey, menuIds }));
    }
  }
};
</script>
