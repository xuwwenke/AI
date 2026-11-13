<template>
  <div v-if="!globalConfig.loginPageUrl" class="loginBox">
    <div :class="{'loginMain': true, 'loginNoAnimation': globalConfig.loginNoAnimation}">
      <div class="login_halo"></div>
      <div class="login_global"></div>
      <div class="arrow_bg"></div>
      <div class="bling_1"></div>
      <div class="bling_2"></div>
      <div class="bling_3"></div>
      <div class="arrow_1"></div>
      <div class="arrow_2"></div>
      <div class="arrow_3"></div>
      <div class="arrow_4"></div>
      <div class="arrow_5"></div>
      <div class="cold_1"></div>
      <div class="cold_2"></div>
      <div class="cold_3"></div>
      <div class="cold_4"></div>
      <div class="cold_5"></div>
      <div class="loginContent">
        <template v-if="globalConfig.loginTitle || globalConfig.loginSubTitle">
          <!-- 文字标题 -->
          <h1
            class="login_title_txt login_title_1"
            :style="{'text-align': globalConfig.loginSubTitle ? 'left' : 'center'}"
          >{{ globalConfig.loginTitle }}</h1>
          <h1
            class="login_title_txt login_title_2"
            :style="{'text-align': globalConfig.loginTitle ? 'left' : 'center', 'text-indent': globalConfig.loginTitle ? '2em' : '0'}"
          >{{ globalConfig.loginSubTitle }}</h1>
        </template>
        <template v-else>
          <!-- 图片标题 -->
          <h1 class="login_title_img login_title_1"></h1>
          <h1 class="login_title_img login_title_2"></h1>
        </template>
        <div @keyup.enter="formDataSubmit" style="overflow: hidden;">
          <ul>
            <li>
              <input
                type="text"
                name="username"
                v-model.trim="formData.username"
                placeholder="用户名"
                autocomplete="off"
              />
            </li>
            <li>
              <input
                ref="pwdInput"
                type="password"
                name="password"
                v-model.trim="formData.password"
                placeholder="密码"
                autocomplete="off"
              />
            </li>
            <li v-if="needLoginCode">
              <input
                type="text"
                name="code"
                v-model.trim="formData.code"
                placeholder="验证码"
                autocomplete="off"
              />
              <img :src="codeImg" alt="验证码" title="点击刷新" @click="getImgCode" />
            </li>
          </ul>
          <!-- <a href="javascript:;" class="loginBtn" @click="formDataSubmit">登录</a> -->
          <el-button
            type="primary"
            class="loginBtn"
            :loading="loading"
            @click="formDataSubmit"
            :style="{height: needLoginCode ? '155px' : '100px'}"
          >登 录</el-button>
          <!-- <div class="link-reset">
            <a @click="openModal()">找回密码 / 设置密码</a>
          </div> -->
        </div>
        <div v-if="version" class="login_version">
           {{ version }}
        </div>
      </div>
    </div>
    <!-- <modal-reset ref="modal" @success="onPwdReset" /> -->
    <SMSVerify ref="sMSVerifyRef" />
  </div>
</template>

<script>
import api_auth from "@/api/auth";
import RSAUtils from "@/utils/rsaUtil";
import { clearLoginInfo, getParsedUrl } from "@/utils/base";
import nativeAxios from "axios";
// import ModalReset from "./login-reset-modal";
import SMSVerify from "./sms-verify";
export default {
  name: "login",
  components: {
    // ModalReset,
    SMSVerify
  },
  data() {
    return {
      globalConfig: window.globalConfig,
      loading: false,
      needLoginCode: false,
      codeImg: null,
      formData: {
        username: "",
        password: "",
        code: null
      },
      version: null
    };
  },
  created() {
    if (window.globalConfig.loginPageUrl) {
      window.location.href = getParsedUrl(window.globalConfig.loginPageUrl);
      return;
    }

    this.getNeedCode();
    if (window.globalConfig.showVersionInfo) this.getVersion();
  },
  methods: {
    getVersion() {
      nativeAxios
        .get("./version.json?t=" + new Date().getTime())
        .then(res => {
          if (res.data) {
            let str = res.data.version;
            let version = str
              .split(".")
              .filter((it, idx) => idx < 3)
              .join(".");
            this.version = version.charAt(0).toUpperCase() === "V" ? version : `V ${version}`;
            localStorage.setItem("currentVersion", JSON.stringify(res.data));
          }
        })
        .catch(err => {
          localStorage.removeItem("currentVersion");
        });
    },
    getNeedCode() {
      api_auth
        .getNeedCode()
        .then(resData => {
          this.needLoginCode = resData;
          if (this.needLoginCode) {
            this.getImgCode();
          }
        });
    },
    getImgCode() {
      // api_auth.getImgCode().then(data => this.codeImg = data);
      this.codeImg =
        window.globalConfig.defaultProxyPath +
        "/sso/code/80/37/10/25/20?t=" +
        new Date().getTime();
    },
    showMessage(msg) {
      this.$message({
        message: msg,
        type: "error",
        duration: 1500
      });
    },
    // 提交表单
    formDataSubmit() {
      if (this.loading) return;
      if (!this.formData.username) {
        this.showMessage("请输入用户名");
        return;
      }
      if (!this.formData.password) {
        this.showMessage("请输入密码");
        return;
      }
      // 后台可以对验证码进行开关，所以不一定必填验证码
      /* if (!this.formData.code) {
        this.showMessage("请输入验证码");
        return;
      } */

      this.loading = true;

      api_auth
        .getKeyPair()
        .then(resData => {
          const publicKey = RSAUtils.getKeyPair(
            resData.exponent,
            "",
            resData.modulus
          );
          // console.log(publicKey, formData.password)
          const o = {
            userName: this.formData.username,
            password: RSAUtils.encryptedString(
              publicKey,
              this.formData.password
            ),
            code: this.formData.code,
            appKey: window.globalConfig.appKey
          };
           if (window.globalConfig.isEnableSMSLogin) {
            this.loginBySMS(o);
          } else {
            api_auth
            .login(o)
            .then(async data => {
              // 登录成功后，清空原来的身份信息
              clearLoginInfo(false);

              sessionStorage.setItem('sessionId', data.sessionId);

              // 初始化项目信息
              await this.$store.dispatch('project/initProject', this.formData.username);

              this.loading = false;

              const target = this.$route.params.target;

              if (target && target.path !== "/") {
                this.$router.replace(target).catch(e => {});
              } else {
                if(window.globalConfig.showCompassPageAfterLogin) window.globalCache.autoShowCompass = true;
                this.$router.replace({ path: "/" }).catch(e => {});
              }

              window.globalCache.checkPasswordExpire = true;
            })
            .catch(() => {
              this.loading = false;

              this.getImgCode();
            });
          }
        })
        .catch(() => {
          this.loading = false;
        });
    },
    loginBySMS(o) {
      const param = {...o, type: 'SMS'}
      api_auth.smsLogin(param).then(res => {
        this.loading = false;
        this.$refs.sMSVerifyRef.init(res, param);
      }).catch(() => {
        this.loading = false;
        this.getImgCode();
      })
    },
    // 打开忘记密码弹窗
    openModal() {
      this.$refs.modal.init();
    },
    onPwdReset(email) {
      this.formData.username = email;
      this.formData.password = "";
      this.$refs.pwdInput.focus();
    }
  }
};
</script>

<style lang="scss">
.loginNoAnimation {
  * {
    animation: none !important;
  }
}

.loginBox {
  min-width: 1200px;
  min-height: 700px;
  height: 100%;
  background: url("./assets/login_bg.png") no-repeat center;
  overflow: hidden;
}

.loginMain {
  width: 1200px;
  height: 100%;
  margin: auto;
  position: relative;
}

.login_halo {
  position: absolute;
  left: -360px;
  top: 0;
  width: 1920px;
  height: 1080px;
  background: url("./assets/halo.png") no-repeat;
  animation: halo 10s ease-in-out infinite alternate;
  transform-origin: 774px 491px;
}

.login_global {
  position: absolute;
  left: 118px;
  top: 211px;
  width: 593px;
  height: 561px;
  background: url("./assets/global.png") no-repeat;
  animation: global 10s ease-in-out infinite alternate;
}

.arrow_bg {
  background-image: url("./assets/arrow_bg_2.png");
  position: absolute;
  right: -360px;
  top: 0;
  width: 966px;
  height: 1080px;
}

.arrow_1 {
  background: url("./assets/arrow_1.png") no-repeat;
  position: absolute;
  width: 75px;
  height: 138px;
  right: -360px;
  animation: move_down 19s linear infinite backwards,
    bling 2s linear infinite alternate;
}

.arrow_2 {
  background: url("./assets/arrow_2.png") no-repeat;
  position: absolute;
  width: 135px;
  height: 124px;
  right: -300px;
  animation: move_up 21s 1s linear infinite backwards,
    bling 2.5s linear infinite alternate;
}

.arrow_3 {
  background: url("./assets/arrow_3.png") no-repeat;
  position: absolute;
  width: 122px;
  height: 112px;
  right: 16px;
  animation: move_down 23s 2s linear infinite backwards,
    bling 3s linear infinite alternate;
}

.arrow_4 {
  background: url("./assets/arrow_4.png") no-repeat;
  position: absolute;
  width: 176px;
  height: 162px;
  right: 266px;
  animation: move_down 25s 3s linear infinite backwards,
    bling 3.5s linear infinite alternate;
}

.arrow_5 {
  background: url("./assets/arrow_5.png") no-repeat;
  position: absolute;
  width: 134px;
  height: 123px;
  right: 153px;
  animation: move_up 27s 4s linear infinite backwards,
    bling 4s linear infinite alternate;
}

.arrow_6 {
  background: url("./assets/arrow_6.png") no-repeat;
  position: absolute;
  width: 176px;
  height: 162px;
  right: -176px;
  animation: move_down 29s 5s linear infinite backwards,
    bling 4.5s linear infinite alternate;
}

.cold_1 {
  background: url("./assets/cold_1.png") no-repeat;
  position: absolute;
  width: 38px;
  height: 388px;
  right: 501px;
  animation: move_down 20s 6s linear infinite backwards,
    bling 2s linear infinite alternate;
}

.cold_2 {
  background: url("./assets/cold_2.png") no-repeat;
  position: absolute;
  width: 24px;
  height: 244px;
  right: -237px;
  animation: move_up 22s 7s linear infinite backwards,
    bling 2.5s linear infinite alternate;
}

.cold_3 {
  background: url("./assets/cold_3.png") no-repeat;
  position: absolute;
  width: 38px;
  height: 228px;
  right: -38px;
  animation: move_down 24s 8s linear infinite backwards,
    bling 3s linear infinite alternate;
}

.cold_4 {
  background: url("./assets/cold_4.png") no-repeat;
  position: absolute;
  width: 42px;
  height: 434px;
  right: 130px;
  animation: move_up 26s 9s linear infinite backwards,
    bling 3.5s linear infinite alternate;
}

.cold_5 {
  background: url("./assets/cold_5.png") no-repeat;
  position: absolute;
  width: 83px;
  height: 329px;
  right: -99px;
  animation: move_down 28s 10s linear infinite backwards,
    bling 4s linear infinite alternate;
}

.binary {
  position: absolute;
  top: 0;
}

.bling_1 {
  position: absolute;
  top: 0;
  right: -360px;
  width: 391px;
  height: 392px;
  background: url("./assets/bling_1.png") no-repeat;
  animation: bling 2s alternate infinite;
}

.bling_2 {
  position: absolute;
  top: 711px;
  right: 440px;
  width: 366px;
  height: 369px;
  background: url("./assets/bling_2.png") no-repeat;
  animation: bling 3s alternate infinite;
}

.bling_3 {
  position: absolute;
  top: 582px;
  right: -360px;
  width: 492px;
  height: 498px;
  background: url("./assets/bling_3.png") no-repeat;
  animation: bling 4s alternate infinite;
}

.loginContent {
  position: absolute;
  top: 270px;
  right: 100px;
}

// 文字标题样式
.loginContent .login_title_txt {
  color: white;
}

.loginContent .login_title_txt.login_title_1 {
  font-size: 38px;
  animation: login_title_1 1s;
}

.loginContent .login_title_txt.login_title_2 {
  font-size: 38px;
  margin-bottom: 30px;
  animation: login_title_2 1s;
}

// 图片标题样式
.loginContent .login_title_img {
  width: 442px;
  height: 68px;
}

.loginContent .login_title_img.login_title_1 {
  background: url("./assets/logintitle_1.png") no-repeat;
  animation: login_title_1 1s;
}

.loginContent .login_title_img.login_title_2 {
  background: url("./assets/logintitle_2.png") no-repeat;
  margin-bottom: 30px;
  animation: login_title_2 1s;
}

.loginContent > div {
  width: 442px;
  margin: 0 auto;
}
.loginContent ul {
  width: 300px;
  float: left;
}
.loginMain ul li input {
  width: 300px;
  height: 42px;
  border-radius: 5px;
  padding-left: 40px;
  border: 0;
}
.loginMain ul li input {
  display: inline-block;
  box-sizing: border-box;
}

.loginMain ul li:first-child input {
  background: url("./assets/admin.png") 12px/16px 18px no-repeat #e1e9f5;
}
.loginMain ul li:nth-child(2) input {
  background: url("./assets/pad.png") 14px/13px 14px no-repeat #e1e9f5;
  margin-top: 16px;
}
.loginMain ul li:nth-child(3) input {
  background: url("./assets/verification.png") 14px/15px 12px no-repeat
    #e1e9f5;
  width: 200px;
  margin-top: 16px;
  vertical-align: middle;
}
.loginMain ul li:nth-child(3) img {
  margin-top: 16px;
  vertical-align: middle;
  margin-left: 20px;
}
/* .loginMain .loginBtn {
  display: inline-block;
  width: 124px;
  height: 155px;
  background: #1871e1;
  border-radius: 5px;
  color: #fff;
  text-align: center;
  line-height: 155px;
  float: right;
  transition: 0.3s;
} */
.loginMain .loginBtn {
  // display: inline-block;
  float: right;
  width: 124px;
  // height: 155px;
  // background: #1871e1;
  border-radius: 5px;
  // color: #fff;
  text-align: center;
  // line-height: 155px;
  transition: 0.3s;
  font-size: 16px;
  font-weight: bold;
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: #fff !important;
}
.loginMain .loginBtn:hover {
  background: #66b1ff !important;
  border-color: #66b1ff !important;
  color: #fff !important;
}
.loginMain .loginBtn:active {
  background: #3a8ee6 !important;
  border-color: #3a8ee6 !important;
  color: #fff !important;
}
.loginMain .loginBtn.is-disabled {
  background-color: #a0cfff !important;
  border-color: #a0cfff !important;
  color: #fff !important;
}
.loginMain .link-reset {
  display: block;
  clear: both;
  padding-top: 10px;
  a {
    cursor: pointer;
  }
}
.loginMain {
  .login_version {
    color: #eee;
    margin-top: 20px;
    font-size: 12px;
  }
}
/**谷歌input黄色背景问题**/
.loginMain ul li input:-webkit-autofill {
  -webkit-box-shadow: 0 0 0 1000px #e1e9f5 inset !important;
}

@keyframes global {
  from {
    transform: rotate(-30deg);
  }
  to {
    transform: rotate(30deg);
  }
}

@keyframes halo {
  from {
    transform: rotate(30deg);
  }
  to {
    transform: rotate(-30deg);
  }
}

@keyframes bling {
  from {
    opacity: 1;
  }
  to {
    opacity: 0.1;
  }
}

@keyframes login_title_1 {
  from {
    transform: translate(-200px);
    opacity: 0;
  }
  to {
    transform: translate(0);
    opacity: 1;
  }
}

@keyframes login_title_2 {
  from {
    transform: translateX(200px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes move_down {
  from {
    top: 0;
    transform: translateY(-100%);
  }
  to {
    top: 100%;
    transform: translateY(0);
  }
}

@keyframes move_up {
  from {
    bottom: 0;
    transform: translateY(100%);
  }
  to {
    bottom: 100%;
    transform: translateY(0);
  }
}
</style>
