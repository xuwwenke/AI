<template>
  <el-dialog
    :title="dataForm.id == null ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="reset"
  >
    <el-form
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      label-width="120px"
    >
      <el-form-item label="所属字典" prop="name">
        <el-input
          v-model="dataForm.name"
          :disabled="true"
          placeholder="所属字典"
        ></el-input>
      </el-form-item>
      <el-form-item label="上级字典项" prop="pName">
        <el-input
          v-model="dataForm.pName"
          :disabled="true"
          placeholder="上级字典项"
        ></el-input>
      </el-form-item>
      <el-form-item label="字典项级别" prop="itemLevel">
        <el-input
          v-model="dataForm.itemLevel"
          :disabled="true"
          placeholder="字典项级别"
        ></el-input>
      </el-form-item>
      <el-form-item label="字典项值" prop="itemValue">
        <el-input
          v-model="dataForm.itemValue"
          placeholder="字典项值"
        ></el-input>
      </el-form-item>
      <el-form-item label="字典项名称" prop="itemName">
        <el-input
          v-model="dataForm.itemName"
          placeholder="字典项名称"
        ></el-input>
      </el-form-item>
      <el-form-item label="字典项排序值" prop="itemSort">
        <el-input
          placeholder="字典项排序值"
          v-model="dataForm.itemSort"
        ></el-input>
      </el-form-item>
      <el-form-item label="字典项备注" prop="itemSort">
        <el-input
          placeholder="字典项备注"
          v-model="dataForm.remark5"
        ></el-input>
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
      dataForm: {},
      dataRule: {
        itemName: [
          { required: true, message: "字典项名称不能为空", trigger: "blur" }
        ],
        itemValue: [{ required: true, message: "字典项值", trigger: "blur" }]
      }
    };
  },
  methods: {
    init(data) {
      this.visible = true;
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate();
        this.dataForm = { ...data };
      });
    },
    reset() {
      this.$refs.dataForm.resetFields();
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate(valid => {
        if (valid) {
          this.submitLoading = true;
          // this.$set(this.dataForm, this.dataForm);
          const type = this.dataForm.id == null ? "addCode" : "updateCode";
          const tmp = {...this.dataForm};
          delete tmp.childs;
          api_dic[type](tmp)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
              //子组件触发父组件事件
              this.$emit("onSubmit", this.dataForm);
              this.$message({
                message: "操作成功",
                type: "success",
                duration: 1500
              });
            })
            .catch(() => {
              this.submitLoading = false;
            });
        }
      });
    }
  }
};
</script>
