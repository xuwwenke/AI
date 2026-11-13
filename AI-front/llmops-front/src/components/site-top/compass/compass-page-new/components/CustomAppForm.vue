<template>
  <el-dialog :title="dataForm.id == null ? '创建自定义应用' : '编辑自定义应用'" custom-class="compass-page-dialog" :close-on-click-modal="false" :visible.sync="visible"
    @closed="reset">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="auto">
      <el-form-item label="应用名称" prop="appName">
        <el-input v-model.trim="dataForm.appName" placeholder="应用名称"></el-input>
      </el-form-item>
      <el-form-item label="图标" prop="appIcon">
        <icon-select v-model="dataForm.appIcon" placeholder="点击选择图标，或直接输入图片地址，也可点击下方按钮上传图片（将转为 base64）" />
        <icon-upload-base64 v-model="dataForm.appIcon" class="mt10" />
      </el-form-item>
      <el-form-item label="显示序号" prop="seq">
        <el-input v-model="dataForm.seq" placeholder="请输入数字"></el-input>
      </el-form-item>
      <el-form-item label="应用描述" prop="appDesc">
        <el-input type="textarea" :rows="3" v-model="dataForm.appDesc" placeholder="请输入描述"></el-input>
      </el-form-item>
      <el-form-item label="应用地址" prop="appUrl">
        <el-input v-model.trim="dataForm.appUrl" placeholder="可选，设置后会直接跳转到该地址"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_customApp from "@/api/customApp";
import api_auth from "@/api/auth";
import IconSelect from "@/components/icon-select";
import IconUploadBase64 from '@/components/icon-upload-base64';

export default {
  components: {
    IconSelect,
    IconUploadBase64,
  },
  data() {
    return {
      visible: false,
      submitLoading: false,
      dataForm: {
        appName: null,
        // appKey: null,
        appIcon: null,
        // appUrl: null,
        seq: null,
        appDesc: null,
        appUrl: null,
      },
      dataRule: {
        appName: [
          { required: true, message: "应用名称不能为空", trigger: "blur" },
        ],
      },
    };
  },
  methods: {
    init(row) {
      this.visible = true;
      this.$nextTick(() => {
        // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
        // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
        this.$refs.dataForm.clearValidate();

        // 对dataForm的赋值，需要滞后，以免变成默认值，reset时出问题
        this.dataForm.id = row.id;
        if (this.dataForm.id != null) {
          if(row.deploy == undefined) row.deploy = 1;
          this.dataForm = {
            id: row.id,
            appName: row.appName,
            // appKey: row.appKey,
            appIcon: row.appIcon,
            // appUrl: row.appUrl,
            seq: row.seq,
            appDesc: row.appDesc,
            appUrl: row.appUrl,
          };
        }
      });
    },
    reset() {
      this.$refs.dataForm.resetFields();
    },
    // 表单提交
    async dataFormSubmit() {
      if (window.globalConfig.verifyAuth) {
        //加授权权限校验
        try {
          let data = await api_auth.verifyAuth();
          if (!data) return this.$message.warning('license未授权或已过期，请联系系统管理员');
        } catch (err) {
          return false;
        }
      }
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          const tmp = { ...this.dataForm };

          const opt = this.dataForm.id == null ? "insert" : "update";

          api_customApp[opt](tmp)
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
        }
      });
    },
  },
};
</script>
