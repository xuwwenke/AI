<template>
  <div class="login">
    <i class="fa fa-spinner fa-spin"></i>
    loading...
  </div>
</template>

<style>
.login {
  height: 600px;
  text-align: center;
  line-height: 600px;
}
</style>

<script>
import authApi from "@/api/auth";

export default {
  data() {
    return {
      //获取的登录认证码
      code: null,
    };
  },
  created() {
    this.login();
  },
  methods: {
    showMessage(msg) {
      this.$message({
        message: msg,
        type: "error",
        duration: 1500,
      });
    },

    // 提交表单
    login() {
      this.code = this.$route.query.code;

      if (!this.code) {
        this.showMessage("未发现授权码");
        this.$router
          .push({
            path: "/login",
          })
          .catch((e) => {});
        return;
      }

      authApi
        .loginBycode(this.code)
        .then((data) => {
          sessionStorage.setItem('sessionId', data.sessionId);

          const target = this.$route.query.target;
          if (target) {
            this.$router.replace({ path: target }).catch((e) => {});
          } else {
            this.$router.replace({ path: "/" }).catch((e) => {});
          }
        })
        .catch(() => {
          // this.showMessage("自动登录失败");
          this.$router
            .push({
              path: "/login",
            })
            .catch((e) => {});
        });
    },
  },
};
</script>

