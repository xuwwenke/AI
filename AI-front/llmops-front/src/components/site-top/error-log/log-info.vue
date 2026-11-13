<template>
  <el-dialog title="异常日志（最近10条）" :visible.sync="visible" width="60%" append-to-body>
    <el-tabs class="tabs">
      <el-tab-pane>
        <span slot="label">
          <el-badge :value="error.request.length || undefined" :max="99">通讯异常</el-badge>
        </span>
        <el-table :data="error.request" stripe border style="width: 100%">
          <el-table-column
            prop="timestamp"
            align="center"
            label="时间"
            width="150"
            :formatter="timeFormatter"
          ></el-table-column>
          <el-table-column prop="code" align="center" label="异常码" width="60"></el-table-column>
          <el-table-column prop="message" align="center" label="消息" show-overflow-tooltip></el-table-column>
          <el-table-column prop="body" align="center" label="详细信息" show-overflow-tooltip></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane>
        <span slot="label">
          <el-badge :value="error.logic.length || undefined" :max="99">脚本异常</el-badge>
        </span>
        <el-table :data="error.logic" stripe border style="width: 100%">
          <el-table-column
            prop="timestamp"
            align="center"
            label="时间"
            width="150"
            :formatter="timeFormatter"
          ></el-table-column>
          <el-table-column prop="message" align="center" label="异常信息" show-overflow-tooltip></el-table-column>
          <el-table-column prop="scriptURI" align="center" label="异常路径" show-overflow-tooltip></el-table-column>
          <el-table-column
            prop="component"
            align="center"
            label="异常组件"
            width="150"
            show-overflow-tooltip
          ></el-table-column>
          <!-- <el-table-column prop="lineNumber" align="center" label="出错行号"></el-table-column> -->
          <!-- <el-table-column prop="columnNumber" align="center" label="出错列号"></el-table-column> -->
          <!-- <el-table-column prop="errorObj" align="center" label="错误详情"></el-table-column> -->
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <span slot="footer" class="dialog-footer">
      <el-button type="primary" @click="visible = false">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { formatTime } from "@/utils/time";

export default {
  data() {
    return {
      visible: false
    };
  },
  props: ["error"],
  methods: {
    init() {
      this.visible = true;
    },
    timeFormatter(row, column, cellValue, index) {
      return formatTime(cellValue);
    }
  }
};
</script>

<style lang="scss" scoped>
.tabs {
  margin-top: -25px;
  ::v-deep .el-badge__content.is-fixed {
    top: 10px;
    right: 0;
  }
}
</style>
