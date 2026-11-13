<template>
  <el-dialog
    title="修改密码"
    width="30%"
    top="30vh"
    class="change-password"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :visible.sync="visible"
    append-to-body
    :show-close="closable"
    @closed="reset"
  >
    <el-form
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @keyup.enter.native="dataFormSubmit"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPwd">
        <el-input type="password" v-model.trim="dataForm.oldPwd" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPwd">
        <el-input type="password" v-model.trim="dataForm.newPwd" auto-complete="off"></el-input>
      </el-form-item>
      <el-form-item label="确认新密码" prop="comfirmPwd">
        <el-input type="password" v-model.trim="dataForm.comfirmPwd" auto-complete="off"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <span v-if="!closable" class="fl" style="font-size: 12px; color: gray">
        <span>
          <i class="el-icon-info"></i> 无法跳过，可
        </span>
        <el-button type="text" @click="$emit('onLogout')">退出登录</el-button>
      </span>
      <el-button v-if="closable" @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_auth from "@/api/auth";
import RSAUtils from "@/utils/rsaUtil";
import { password } from "@/utils/validate";

export default {
  props: {
    closable: {
      type: Boolean,
      default: true
    }
  },
  data() {
    var validatePass = (rule, value, callback) => {
      if (!password(value)) {
        callback(new Error("密码长度10~32位，必须包含“大写字母、小写字母、数字、特殊字符”中的三类"));
      } else {
        if (this.dataForm.comfirmPwd !== "") {
          this.$refs.dataForm.validateField("comfirmPwd");
        }
        callback();
      }
    };
    var validatePass2 = (rule, value, callback) => {
      if (value !== this.dataForm.newPwd) {
        callback(new Error("两次输入密码不一致"));
      } else {
        callback();
      }
    };
    return {
      visible: false,
      submitLoading: false,
      dataForm: {
        oldPwd: null,
        newPwd: null,
        comfirmPwd: null
      },
      dataRule: {
        oldPwd: [{ required: true, message: "请输入原密码", trigger: "blur" }],
        newPwd: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { validator: validatePass, trigger: "blur" }
        ],
        comfirmPwd: [
          { required: true, message: "请再次输入新密码", trigger: "blur" },
          { validator: validatePass2, trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    init(id) {
      this.visible = true;
      this.dataForm.id = id;
    },
    reset() {
      this.$refs.dataForm.resetFields();
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate(valid => {
        if (valid) {
          this.submitLoading = true;

          api_auth
            .getKeyPair()
            .then(resData => {
              const publicKey = RSAUtils.getKeyPair(
                resData.exponent,
                "",
                resData.modulus
              );

              const o = {
                id: this.dataForm.id,
                oldPwd: RSAUtils.encryptedString(
                  publicKey,
                  this.dataForm.oldPwd
                ),
                newPwd: RSAUtils.encryptedString(
                  publicKey,
                  this.dataForm.newPwd
                )
              };

              api_auth
                .changePassword(o)
                .then(() => {
                  this.visible = false;
                  this.submitLoading = false;
                  this.$message({
                    message: "操作成功",
                    type: "success",
                    duration: 1500
                  });
                })
                .catch(() => {
                  this.submitLoading = false;
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

<style lang="scss" scoped>
.change-password {
  ::v-deep .el-dialog__body {
    padding: 20px 20px 10px;
  }
}
</style>
