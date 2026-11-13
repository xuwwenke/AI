<template>
  <el-dialog
    title="批量导入字典"
    width="400px"
    :append-to-body="true"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="reset"
  >
    <el-form :model="dataForm" :rules="rules" ref="dataForm">
      <el-form-item label="字典文件" prop="file">
        <el-input
          type="file"
          ref="file"
          v-model="dataForm.file"
          accept="application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ></el-input>
      </el-form-item>
      <el-form-item label="导入策略" prop="policy">
        <el-radio-group v-model="dataForm.policy" class="policy-radio-group">
          <el-radio :label="0">冲突时中止导入</el-radio>
          <el-radio :label="1">先清除后插入</el-radio>
          <el-radio :label="2">增量（冲突部分不更新）</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="dataFormSubmit"
        >确定</el-button
      >
    </span>
  </el-dialog>
</template>

<script>
import api_dic from "@/api/sys/dic";

export default {
  data() {
    return {
      visible: false,
      submitLoading: false,
      uploadCancel: undefined, // 用于取消请求
      dataForm: {
        file: undefined,
        policy: 0,
      },
      rules: {
        file: [{ required: true, message: "请上传文件", trigger: "change" }],
        policy: [{ required: true }],
      },
    };
  },
  methods: {
    init() {
      this.visible = true;
    },
    reset() {
      this.$refs.dataForm.resetFields();
      this.uploadCancel && this.uploadCancel(); //取消上次请求
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          // console.log(this.$refs.file.$el.children[0].files[0], this.$refs.file.$refs.input.files[0]);
          const file = this.$refs.file.$refs.input.files[0];
          let formData = new FormData();
          formData.append("file", file);
          formData.append("policy", this.dataForm.policy);

          api_dic
            .dicImport(formData, (cancel) => (this.uploadCancel = cancel))
            .then(() => {
              this.$emit("onSubmit");
              this.$message({
                message: "操作成功",
                type: "success",
                duration: 1500,
              });
              this.visible = false;
              this.submitLoading = false;
            })
            .catch((e) => {
              this.submitLoading = false;
              this.dataForm.file = undefined; // 导入失败时清空文件，以免文件修改后无法再提交（ERR_UPLOAD_FILE_CHANGED）
            });
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.policy-radio-group {
  padding-left: 10px;
  > label {
    width: 100%;
    margin-bottom: 8px;
  }
}
</style>
