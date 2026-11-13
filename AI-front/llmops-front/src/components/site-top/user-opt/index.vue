<template>
  <span>
    <el-dropdown @command="handleCommand" trigger="click">
      <span class="el-dropdown-link">
        <span>{{ userName }}</span>
        <i class="el-icon-arrow-down el-icon--right"></i>
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item v-if="globalConfig.showChangePassword" command="changePassword">修改密码</el-dropdown-item>
        <el-dropdown-item v-if="globalConfig.showChangeMenuMode" command="switchSiteMenu">
          {{theme.siteMenuMode == "vertical" ? "顶部菜单" : "左侧菜单"}}
        </el-dropdown-item>
        <el-dropdown-item v-if="globalConfig.showLockScreen" command="lockScreen">锁屏</el-dropdown-item>
        <el-dropdown-item v-if="globalConfig.showMyLogs" command="myLogs">我的日志</el-dropdown-item>
        <el-dropdown-item command="about" v-if="globalConfig.showVersionInfo && version">版本说明</el-dropdown-item>
        <el-dropdown-item command="logout" divided style="color: red">退出</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <!-- 弹窗, 修改密码 -->
    <change-password ref="changePasswordForce" :closable="false" @onLogout="handleCommand('logout')"></change-password>
    <change-password ref="changePassword"></change-password>
    <about ref="about"></about>
    <my-logs ref="myLogs"></my-logs>
  </span>
</template>

<script>
import { mapState, mapMutations } from "vuex";
import api_auth from "@/api/auth";
import { clearLoginInfo } from "@/utils/base";
import { dispatchResize } from "@/utils/screen";
import ChangePassword from "./change-password";
import About from "./../about";
import MyLogs from './../mylogs';

export default {
  data() {
    return {
      globalConfig: window.globalConfig,
      id: null,
      userName: null,
      alertShowing: false,
      alertMessage: null,
      version: localStorage.getItem("currentVersion")
    };
  },
  computed: mapState(["theme"]),
  components: {
    ChangePassword,
    About,
    MyLogs
  },
  created() {
    this.id = localStorage.getItem("id");
    this.userName = localStorage.getItem("userName");

    if (
      window.globalCache.checkPasswordExpire ||
      window.globalConfig.forcePasswordChange
    ) {
      window.globalCache.checkPasswordExpire = false;
      this.checkPasswordExpire();
    }
  },
  watch: {
    $route() {
      if (this.alertShowing) {
        setTimeout(this.showAlert, 0);
      }
    }
  },
  methods: {
    ...mapMutations("theme", ["switchSiteMenuMode"]),
    handleCommand(command) {
      switch (command) {
        case "switchSiteMenu":
          this.switchSiteMenuMode();
          dispatchResize();
          break;
        case "changePassword":
          this.$refs.changePassword.init(this.id);
          break;
        case "lockScreen":
          this.$emit("lockScreen");
          break;
        case "myLogs":
        this.$refs.myLogs.init();
          break;
        case "about":
          this.$refs.about.init();
          break;
        case "logout":
          api_auth.logout().then(() => {
            clearLoginInfo();
            this.$router.push({
              name: "login"
            });
          });
          break;
      }
    },
    checkPasswordExpire() {
      api_auth.passwordExpire({ id: this.id }).then(data => {
        if (data.needModify && window.globalConfig.forcePasswordChange) {
          this.alertShowing = true;
          this.alertMessage = data.description;
          this.showAlert();
        } else if (data.needRemind || data.needModify) {
          this.$confirm(data.description, "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
            closeOnClickModal: false
          })
            .then(() => {
              setTimeout(() => {
                this.$refs.changePassword.init(this.id);
              }, 200);
            })
            .catch(() => { });
        }
      });
    },
    showAlert() {
      this.$alert(this.alertMessage, "提示", {
        confirmButtonText: "立即修改",
        type: "warning",
        showClose: false
      })
        .then(() => {
          this.alertShowing = false;
          setTimeout(() => {
            this.$refs.changePasswordForce.init(this.id);
          }, 200);
        })
        .catch(() => { });
    }
  }
};
</script>
