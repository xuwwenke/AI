<template>
  <span>
    <el-tooltip content="异常日志" placement="bottom">
      <el-badge
        :is-dot="error.request.length > 0 || error.logic.length > 0"
      >
        <i class="fa fa-exclamation-triangle" @click="onShowErrorLog"></i>
      </el-badge>
    </el-tooltip>
    <!-- 弹窗, 异常日志 -->
    <log-info ref="logInfo" :error="error" />
  </span>
</template>

<script>
import { mapState } from "vuex";
import LogInfo from "./log-info";

export default {
  computed: mapState(["error"]),
  components: {
    LogInfo
  },
  methods: {
    onShowErrorLog() {
      this.$refs.logInfo.init();
    }
  }
};
</script>
