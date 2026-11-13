<template>
  <el-dialog
    :title="dataForm.id == null ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible"
    @closed="reset"
  >
    <el-form
      v-loading="loading"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      label-width="120px"
    >
      <el-form-item label="所属应用" prop="appId">
        <el-select v-model="dataForm.appId" placeholder="所属应用" clearable>
          <el-option v-for="app in appList" :key="app.id" :value="app.id" :label="app.name"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="键" prop="key">
        <el-input v-model.trim="dataForm.key" placeholder="键"></el-input>
      </el-form-item>
      <el-form-item label="值" prop="value">
        <el-input v-model.trim="dataForm.value" placeholder="值"></el-input>
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-input v-model.trim="dataForm.type" placeholder="类型"></el-input>
      </el-form-item>
      <el-form-item label="描述" prop="remark">
        <el-input type="textarea" :rows="3" v-model="dataForm.remark" placeholder="请输入描述"></el-input>
      </el-form-item>
    </el-form>
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
import api_cfg_dic from "@/api/sys/cfg/dic";

export default {
  data() {
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      appList: [],
      dataForm: {
        appId: null,
        key: null,
        value: null,
        type: null,
        remark: null,
      },
      dataRule: {
        appId: [
          { required: true, message: "所属应用不能为空", trigger: "blur" }
        ],
        key: [{ required: true, message: "键不能为空", trigger: "blur" }],
        value: [{ required: true, message: "值不能为空", trigger: "blur" }],
        type: [{ required: true, message: "类型不能为空", trigger: "blur" }]
      }
    };
  },
  methods: {
    init(row, appList) {
      this.visible = true;
      this.appList = appList;
      // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
      // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate();
      });

      // this.loading = true;

      this.dataForm.id = row.id;

      if (this.dataForm.id != null) {
        // 加入延迟，以免直接进行 “修改” 操作后，resetFields 无法重置到清空状态
        this.$nextTick(() => {
          this.dataForm = {
            id: row.id,
            appId: row.app ? row.app.id : null,
            key: row.key,
            value: row.value,
            type: row.type,
            remark: row.remark
          };
        });
      }
    },
    reset() {
      this.$refs.dataForm.resetFields();
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate(valid => {
        if (valid) {
          this.submitLoading = true;

          this.dataForm["app.id"] = this.dataForm.appId;

          api_cfg_dic
            .insertOrUpdate(this.dataForm)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
              this.$emit("onSubmit");
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
