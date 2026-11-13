<template>
  <el-dialog
    title="验证码认证"
    :visible.sync="smsVisible"
    width="26%"
    :close-on-click-modal="false"
    center
  >
    <h1>
      您好，{{ smsParam.userName }}
    </h1>
    <p>
      为保障您的账号安全性，需要完成以下二次认证
    </p>
    <p>
      验证码已发送到您的手机：{{ desensitizedPhone }}，请查收！
    </p>
    <div class="verify-code-input">
      <el-input v-model="verifyCode" type="text" placeholder="请输入验证码" />
      <el-button :disabled="isCounting || reuse" @click="getSMSData()">{{
        buttonText
      }}</el-button>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button type="primary" :disabled="!verifyCode || loading" @click="submit"
        >确 定</el-button
      >
    </span>
  </el-dialog>
</template>
<script>
import api_auth from "@/api/auth";
import { clearLoginInfo } from "@/utils/base";
export default {
  data() {
    return {
      smsVisible: false,
      loading: false,
      smsData: {},
      smsParam: {},
      verifyCode: "",
      isCounting: true,
      countDown: 0, // 倒计时时间，单位为秒
      intervalId: null,
      reuse: false
    };
  },
  computed: {
    desensitizedPhone() {
      return this.desensitizePhoneNumber(this.smsData.destination);;
    },
    desensitizedMessage() {
      return this.smsData.destination && this.smsData.destination.replace(/\[(\d{11})\]/, (match, p1) => `[${p1.slice(0, 3)}****${p1.slice(7)}]`);
    },
    buttonText() {
      return this.reuse ? '验证码还在有效期' : this.isCounting ? `${this.countDown}秒后重新发送` : "发送验证码";
    },
  },
  methods: {
    init(data, param) {
      this.reuse = data.reuse;
      clearInterval(this.intervalId);
      this.verifyCode = '';
      this.smsData = data;
      this.smsParam = param;
      this.countDown = Math.floor(
        (data.validationTime - data.sendTime) / 1000
      );
      clearInterval(this.intervalId);
      if (!this.reuse) this.sendVerifyCode();
      this.smsVisible = true;
    },
    desensitizePhoneNumber(phoneNumber) {
      if (!phoneNumber || !this.isValidPhoneNumber(phoneNumber)) {
        return phoneNumber;
      }

      const prefix = phoneNumber.substring(0, 3);
      const suffix = phoneNumber.substring(7);
      const maskedPart = '****';
      return `${prefix}${maskedPart}${suffix}`;
    },
    isValidPhoneNumber(phoneNumber) {
      const regex = /^1[3-9]\d{9}$/;
      return regex.test(phoneNumber);
    },
    sendVerifyCode() {
      clearInterval(this.intervalId);
      this.isCounting = true;
      this.intervalId = setInterval(() => {
        this.countDown--;
        if (this.countDown <= 0) {
          clearInterval(this.intervalId);
          this.isCounting = false;
          this.countDown = ''; // 重置倒计时
        }
      }, 1000);
    },
    getSMSData() {
      api_auth
        .smsLogin(this.smsParam)
        .then((res) => {
          this.smsData = res;
          this.reuse = res.reuse;
          this.countDown = Math.floor(
            (res.validationTime - res.sendTime) / 1000
          );
          this.sendVerifyCode();
        })
        .catch(() => {});
    },
    submit() {
      this.loading = true;
      const o = {
        appKey: window.globalConfig.appKey,
        authCode: this.verifyCode,
        loginType: "SMS",
        userName: this.smsData.destination,
      };
      api_auth.login(o).then(async (data) => {
        this.loading = false;
        this.smsVisible = false;
        clearLoginInfo();

        // 初始化项目信息
         await this.$store.dispatch('project/initProject', this.smsData.destination);

        const target = this.$route.params.target;

        if (target) {
          this.$router.replace(target).catch((e) => {});
        } else {
          window.globalCache.autoShowCompass = true;
          this.$router.replace({ path: "/" }).catch((e) => {});
        }

        window.globalCache.checkPasswordExpire = true;
      }).catch(() => { 
        this.loading = false; 
        this.getSMSData();
        this.verifyCode = '';
      });
    },
  },
};
</script>
<style lang="scss" scoped>
::v-deep .el-input__inner,
.el-button {
  border: 0;
  border-bottom: 1px solid #d7d5dc;
  border-radius: 0;
  width: 100%;
}
.verify-code-input {
  display: flex;
}
</style>
