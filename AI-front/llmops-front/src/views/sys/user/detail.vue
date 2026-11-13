<template>
  <el-dialog title="详情" :visible.sync="visible" width="40%" @closed="reset">
    <!-- 用 v-if 是为了避免在两次弹窗中出现相同的 el-tag 且位置不同时，出现跳动 -->
    <el-form v-if="dataForm" label-width="30%" label-position="left" style="margin: 0 5%">
      <el-form-item label="用户名">{{ dataForm.userName }}</el-form-item>
      <el-form-item label="真实姓名">{{ dataForm.realName || nothing }}</el-form-item>
      <el-form-item label="手机号">{{ dataForm.tel || nothing }}</el-form-item>
      <el-form-item label="电子邮件">{{ dataForm.email || nothing }}</el-form-item>
      <el-form-item label="状态">
        <status-tag :statusList="statusList" :value="dataForm.isValid" />
      </el-form-item>
      <el-form-item label="所属组织">
        <el-tag v-for="org in dataForm.orgs" :key="org.id" class="mr5 mb5">{{ org.name }}</el-tag>
      </el-form-item>
      <el-form-item label="角色">
        <el-tag v-for="role in dataForm.roles" :key="role.id" class="mr5 mb5">{{ role.name }}</el-tag>
      </el-form-item>
      <el-form-item label="用户归属">{{ dataForm.comcodeLabel || nothing }}</el-form-item>
      <el-form-item label="登录失败次数">{{ dataForm.loginFailureNum }}</el-form-item>
      <el-form-item label="登录失败时间">{{ formatTime(dataForm.loginFailureTime) || nothing }}</el-form-item>
      <el-form-item label="创建时间">{{ formatTime(dataForm.insertTime) }}</el-form-item>
      <el-form-item label="更新时间">{{ formatTime(dataForm.lastUpdateTime) || nothing }}</el-form-item>
      <el-form-item label="修改密码时间">{{ formatTime(dataForm.resetPasswordTime) || nothing }}</el-form-item>
      <el-form-item label="用户描述">{{ dataForm.userDesc || nothing }}</el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">关闭</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { formatTime } from "@/utils/time";
import StatusTag from "./status-tag";

export default {
  components: {
    StatusTag,
  },
  props: {
    statusList: Array,
  },
  data() {
    return {
      nothing: "——",
      visible: false,
      dataForm: null
    };
  },
  methods: {
    formatTime,
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
