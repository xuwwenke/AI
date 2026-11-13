<template>
  <el-dialog
    title="屏幕已锁定"
    width="30%"
    top="30vh"
    class="screen-lock"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :visible.sync="visible"
    append-to-body
    :show-close="false"
    @closed="reset"
  >
    <el-form
      :model="dataForm"
      :rules="dataRule"
      ref="dataForm"
      @submit.native.prevent
      @keyup.enter.native="dataFormSubmit"
      label-width="100px"
    >
      <el-form-item label="密码" prop="password">
        <el-input
          type="password"
          v-model.trim="dataForm.password"
          auto-complete="off"
          placeholder="输入密码解锁"
          clearable
        ></el-input>
      </el-form-item>
      <el-form-item v-if="sessionExpired && needLoginCode" label="验证码" prop="code">
        <el-input
          v-model.trim="dataForm.code"
          auto-complete="off"
          class="mr10 vam"
          style="width: 200px"
          clearable
        ></el-input>
        <img :src="codeImg" alt="验证码" title="点击刷新" class="vam" @click="getImgCode" />
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <span class="fl" style="font-size: 12px; color: gray">
        <span>
          <i class="el-icon-info"></i> 无法跳过，可
        </span>
        <el-button type="text" @click="onLogout">退出登录</el-button>
      </span>
      <el-button type="primary" :loading="submitLoading" @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_auth from "@/api/auth";
import RSAUtils from "@/utils/rsaUtil";
import { clearLoginInfo } from "@/utils/base";

export default {
  props: {
    id: String || Number,
    userName: String
  },
  data() {
    return {
      visible: false,
      submitLoading: false,
      needLoginCode: false,
      sessionExpired: false,
      codeImg: null,
      dataForm: {
        password: null,
        code: null
      },
      dataRule: {
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        code: [{ required: true, message: "请输入验证码", trigger: "blur" }]
      }
    };
  },
  methods: {
    init() {
      this.visible = true;
    },
    reset() {
      this.$refs.dataForm.resetFields();
      this.sessionExpired = false;
      this.$emit("onClose");
    },
    onLogout() {
      api_auth.logout().then(() => {
        clearLoginInfo();
        this.$router.push({
          name: "login"
        });
      });
    },
    getNeedCode() {
      return api_auth.getNeedCode().then(resData => {
        this.needLoginCode = resData;
        if (this.needLoginCode) {
          this.getImgCode();
        }
      });
    },
    getImgCode() {
      this.codeImg =
        window.globalConfig.defaultProxyPath +
        "/sso/code/80/30/10/23/20?t=" +
        new Date().getTime();
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
              const password = RSAUtils.encryptedString(
                publicKey,
                this.dataForm.password
              );

              if (!this.sessionExpired) {
                // 调用校验密码接口
                const o = {
                  id: this.id,
                  password
                };

                api_auth
                  .validatePassword(o)
                  .then(data => {
                    this.submitLoading = false;

                    // 用户session失效
                    if (data.code === 401 || data.code === -999) {
                      this.sessionExpired = true;
                      this.getNeedCode().then(() => {
                        // 如果不需要验证码，直接触发登录
                        if (!this.needLoginCode) {
                          this.dataFormSubmit();
                        }
                      });
                      return;
                    }

                    // 其它后台异常
                    if (data.code != undefined && data.code !== 200) {
                      this.$message({
                        message: data.message || "操作失败",
                        type: "error",
                        duration: 1500
                      });
                      return;
                    }

                    // 解锁
                    this.visible = false;
                  })
                  .catch(error => {
                    this.submitLoading = false;
                    this.$message({
                      message: error.message || "操作失败",
                      type: "error",
                      duration: 1500
                    });
                  });
              } else {
                // 调用登录接口
                const o = {
                  userName: this.userName,
                  password,
                  code: this.dataForm.code,
                  appKey: window.globalConfig.appKey
                };

                api_auth
                  .login(o)
                  .then(data => {
                    this.submitLoading = false;

                    // 解锁
                    this.visible = false;
                  })
                  .catch(() => {
                    this.submitLoading = false;

                    this.getImgCode();
                  });
              }
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

<style lang="scss">
.screen-lock {
  ::v-deep .el-dialog__body {
    padding: 20px 20px 10px;
  }
}
</style>
