<template>
  <el-dialog
    title="找回密码 / 设置密码"
    :visible.sync="visible"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-steps :active="currentStep" finish-status="success" align-center>
      <el-step title="邮箱验证" />
      <el-step title="密码重置" />
    </el-steps>
    <el-form
      class="mt20"
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      label-width="100px"
    >
      <template v-if="currentStep === 0">
        <el-form-item class="captcha-row" label="邮箱" prop="email" key="email">
          <el-input
            class="f1"
            v-model.trim="dataForm.email"
            placeholder=""
          />
          <el-button
            class="ml10"
            :loading="captchaLading"
            :disabled="captchaCooldown > 0"
            @click.stop="sendCaptcha()"
            >{{
              captchaCooldown > 0 ? captchaCooldown : "发送验证码"
            }}</el-button
          >
        </el-form-item>
        <el-form-item label="邮箱验证码" prop="code" key="code">
          <el-input
            ref="codeInput"
            v-model.trim="dataForm.code"
            placeholder=""
          ></el-input>
        </el-form-item>
      </template>
      <template v-if="currentStep === 1">
        <el-form-item label="设置密码" prop="password" key="password">
          <el-input v-model.trim="dataForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm" key="confirm">
          <el-input v-model.trim="dataForm.confirm" type="password"></el-input>
        </el-form-item>
      </template>
      <template v-if="currentStep > 1">
        <el-form-item class="form-success" key="success">
          <p><i class="el-icon-success"></i>密码设置成功</p>
        </el-form-item>
      </template>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button v-if="currentStep <= 0" :loading="loading" @click="cancel"
        >取消</el-button
      ><el-button
        v-else-if="currentStep <= 1"
        :loading="loading"
        @click="prevStep"
        >上一步</el-button
      ><el-button type="primary" :loading="loading" @click="nextStep">{{
        currentStep >= 1 ? "完成" : "下一步"
      }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { email } from "@/utils/validate";
import codeApi from "@/api/sys/code";
import api_auth from "@/api/auth";
import api_user from "@/api/sys/user";
import RSAUtils from "@/utils/rsaUtil";
export default {
  data() {
    const validator = (rule, value, callback) => {
      if (email(value)) {
        callback();
      } else {
        callback(new Error("请输入有效的邮箱"));
      }
    };
    const strongValidator = (rule, value, callback) => {
      let level = 0;
      level += /\d/.test(value) ? 1 : 0;
      level += /[a-z]/.test(value) ? 1 : 0;
      level += /[A-Z]/.test(value) ? 1 : 0;
      level += /[~!@#$%^&*]/.test(value) ? 1 : 0;
      if (!/.{8,16}/.test(value)) {
        callback(new Error("密码长度为8~16位"));
      } else if (level < 3) {
        callback(
          new Error("密码必须包含三类（大写字母、小写字母、数字、特殊字符）")
        );
      } else {
        callback();
      }
    };
    const pwdValidator = (rule, value, callback) => {
      if (value === this.dataForm.password) {
        callback();
      } else {
        callback(new Error("两次密码输入不一致"));
      }
    };
    return {
      visible: false,
      loading: false,
      captchaLading: false,
      captchaCooldown: false,
      currentStep: 0,
      dataForm: {
        email: "",
        code: "",
        password: "",
        confirm: ""
      },
      dataRule: {
        email: [
          { required: true, message: "请填写邮箱", trigger: "blur" },
          { validator, trigger: "blur" }
        ],
        code: [{ required: true, message: "请填写验证码", trigger: "blur" }],
        password: [
          { required: true, message: "请填写密码", trigger: "blur" },
          { validator: strongValidator, trigger: "blur" }
        ],
        confirm: [
          { required: true, message: "请再次输入", trigger: "blur" },
          { validator: pwdValidator, trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    init() {
      this.currentStep = 0;
      this.dataForm = { email: "", code: "", password: "", confirm: "" };
      this.visible = true;
    },
    cancel() {
      this.visible = false;
    },
    sendCaptcha() {
      if (!this.dataForm.email) {
        this.$refs.dataForm.validateField("email");
        return;
      }
      this.captchaLading = true;
      codeApi
        .sendEmailCode(this.dataForm.email)
        .then(() => {
          // customClass: "globalError"
          this.$message.success("验证码已发送");
          this.startTimer();
          if (this.$refs.codeInput) {
            this.$refs.codeInput.focus();
          }
        })
        .finally(() => {
          this.captchaLading = false;
        });
    },
    startTimer() {
      this.captchaCooldown = 60;
      this.captchaTimer = setInterval(() => {
        if (this.captchaCooldown > 0) {
          this.captchaCooldown--;
        } else {
          clearInterval(this.captchaTimer);
        }
      }, 1000);
    },
    prevStep() {
      this.currentStep--;
    },
    nextStep() {
      switch (this.currentStep) {
        case 0:
          this.verifyCode();
          break;
        case 1:
          this.resetPwd();
          break;
        case 2:
          this.finish();
          break;
      }
    },
    finish() {
      this.visible = false;
      this.$emit("success", this.dataForm.email);
    },
    // 输入验证码即可进入下一步
    verifyCode() {
      this.$refs.dataForm.validate().then(
        () => {
          this.currentStep++;
        },
        () => {}
      );
    },
    // 提交验证码和新密码
    resetPwd() {
      this.$refs.dataForm.validate().then(
        () => {
          this.loading = true;
          api_auth
            .getKeyPair()
            .then(resData => {
              const publicKey = RSAUtils.getKeyPair(
                resData.exponent,
                "",
                resData.modulus
              );
              const { email, code, password } = this.dataForm;
              return api_user.resetPasswordByEmail(
                email,
                code,
                RSAUtils.encryptedString(publicKey, password)
              );
            })
            .then(data => {
              this.currentStep++;
            })
            .finally(() => {
              this.loading = false;
            });
        },
        () => {}
      );
    }
  }
};
</script>

<style lang="scss" scoped>
.el-steps {
  padding: 10px 5px 5px;
  border-radius: 5px;
  background-color: #f5f7fa;
}
.el-form {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 200px;
  margin-left: 10%;
  margin-right: 10%;
  .captcha-row::v-deep {
    .el-form-item__content {
      display: flex;
    }
    .el-button {
      width: 120px;
      text-align: center;
    }
  }
}
.form-success {
  display: block;
  margin: 0 100px 0 0;
  text-align: center;
  .el-icon-success {
    display: block;
    width: 68px;
    margin: 0 auto;
    font-size: 68px;
    color: #5cb87a;
  }
  p {
    margin: 50px 0;
    font-size: 14px;
    font-weight: 700;
  }
}
</style>
