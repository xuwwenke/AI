<template>
  <el-dialog title="详情" :visible.sync="visible" @closed="reset" append-to-body>
    <!-- 用 v-if 是为了避免在两次弹窗中出现相同的 el-tag 且位置不同时，出现跳动 -->
    <el-form v-if="dataForm" label-width="20%" label-position="left" style="margin: 0 5%">
      <el-form-item
        label="用户名"
      >{{ dataForm.userName + " ( session id: " + dataForm.sessionId + " )" }}</el-form-item>
      <el-form-item label="操作IP">{{ dataForm.optIp }}</el-form-item>
      <el-form-item label="操作时间">{{ formatTime(dataForm.optTime) }}</el-form-item>
      <el-form-item label="操作的应用">{{ dataForm.appKey }}</el-form-item>
      <el-form-item label="操作的模块">{{ dataForm.modules + " ( " + dataForm.permission + " )" }}</el-form-item>
      <el-form-item label="请求URL">{{ dataForm.url }}</el-form-item>
      <el-form-item label="请求协议">{{ dataForm.reqScheme }}</el-form-item>
      <el-form-item label="请求协议类型">{{ dataForm.reqProtocol }}</el-form-item>
      <el-form-item label="请求方式">{{ dataForm.reqMethod }}</el-form-item>
      <!-- <el-form-item label="请求参数" class="jsonArea">
        <pre>{{ formatJson(dataForm.request) }}</pre>
      </el-form-item> -->
      <el-form-item label="响应状态码">{{ dataForm.rspStatus }}</el-form-item>
      <el-form-item label="响应消息">{{ dataForm.rspMsg }}</el-form-item>
      <!-- <el-form-item label="响应参数" class="jsonArea">
        <pre>{{ formatJson(dataForm.response) }}</pre>
      </el-form-item> -->
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">关闭</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { formatTime } from "@/utils/time";
// import { formatJson } from "@/utils/JsonUtil";

export default {
  data() {
    return {
      nothing: "——",
      visible: false,
      dataForm: null
    };
  },
  methods: {
    formatTime,
    // formatJson,
    init(row) {
      this.visible = true;
      this.dataForm = row;
    },
    reset() {
      this.dataForm = null;
    }
  }
};
</script>

<style lang="scss" scoped>
.jsonArea {
  ::v-deep .el-form-item__content {
    max-height: 300px;
    overflow: auto;
  }
}
</style>
