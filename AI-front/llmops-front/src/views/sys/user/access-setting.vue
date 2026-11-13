<template>
  <el-dialog
    :title="dataForm.userName + '的访问控制列表'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="reset"
  >
    <div v-loading="loading" class="cb">
      <el-alert type="info" show-icon style="margin: -20px 0 20px">
        <ul>
          <li>1、此访问控制列表针对于当前用户</li>
          <li>2、优先检测登录IP是否存在于禁止名单中，未配置禁止名单时不进行禁止名单校验</li>
          <li>3、黑名单检测通过后，检测登录IP是否存在于允许名单中，未配置允许名单时不进行校验</li>
        </ul>
      </el-alert>
      <div v-for="(accessObj, index) in dataForm.access" :key="index">
        <el-button
          class="opt-button"
          type="danger"
          icon="fa fa-trash-o"
          @click="onRemoveItem(index)"
        ></el-button>
        <access-config ref="accessConfig" :accessObj="accessObj" />
      </div>
      <el-button class="opt-button" type="primary" icon="fa fa-plus" @click="onAddItem()"></el-button>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :disabled="loading"
        :loading="submitLoading"
        @click="dataFormSubmit"
      >确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_access from "@/api/sys/access";
import AccessConfig from "./access-config";

export default {
  data() {
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      dataForm: {
        userId: null,
        userName: null,
        access: [this.getDefaultAccessObj()],
      },
    };
  },
  components: {
    AccessConfig,
  },
  methods: {
    init(row) {
      this.visible = true;

      this.loading = true;
      this.dataForm.id = row.id;
      this.dataForm.userName = row.userName;

      api_access.getList(this.dataForm.id).then((data) => {
        this.loading = false;
        if (data && data.length > 0) {
          this.dataForm.access = data;
        }
      });
    },
    reset() {
      this.dataForm = {
        userId: null,
        userName: null,
        access: [this.getDefaultAccessObj()],
      };

      this.$nextTick(() => this.clearValidate());
    },
    // 清除表单校验状态
    clearValidate() {
      for (let item of this.$refs.accessConfig) {
        item.clearValidate();
      }
    },
    validate() {
      let result = true;
      for (let item of this.$refs.accessConfig) {
        if (!item.validate()) {
          // 没有直接 return 是为了让每一行都出现校验提示
          result = false;
        }
      }
      return result;
    },
    // 表单提交
    dataFormSubmit() {
      if (!this.validate()) return;

      this.submitLoading = true;

      api_access
        .setList(this.dataForm.id, this.dataForm.access)
        .then(() => {
          this.visible = false;
          this.submitLoading = false;
          this.$emit("onSubmit");
          this.$message({
            message: "操作成功",
            type: "success",
            duration: 1500,
          });
        })
        .catch(() => {
          this.submitLoading = false;
        });
    },
    getDefaultAccessObj() {
      return { accessType: "", startIpStr: "", endIpStr: "", expirationDate: "" };
    },
    onAddItem() {
      this.dataForm.access.push(this.getDefaultAccessObj());
    },
    onRemoveItem(index) {
      this.dataForm.access.splice(index, 1);
    },
  },
};
</script>

<style lang="scss" scoped>
.opt-button {
  float: right;
  margin-top: 5px;
  margin-left: 5px;
  padding: 5px 8px;
}
</style>
