<template>
  <div class="site-page--not-found">
    <div class="site-content">
      <h2 class="not-found-title"><i class="el-icon-error"></i> 配置出错</h2>
      <p class="not-found-desc">没有可显示的菜单和页面，请联系管理员调整配置</p>
      <details>
        <summary>查看详情</summary>
        <dl>
          <dt>请检查下列配置：</dt>
          <dt>1. 当前登录用户所属的角色是否被正确分配了菜单</dt>
          <dt>2. 全局配置 global.config.js 中的 appKey 是否正确</dt>
          <dt>3. 结合业务需求，尝试调整全局配置中以下属性：</dt>
          <dd>showAppSelect、showFullMenu、showSystemMenu、useBiCompass</dd>
        </dl>
      </details>
      <el-button @click="$router.push({ name: 'login' })">返回登录页</el-button>
      <el-button type="primary" class="not-found-btn-gohome" @click="onLogout"
        >退出登录</el-button
      >
    </div>
  </div>
</template>

<script>
import api_auth from "@/api/auth";
import { clearLoginInfo } from "@/utils/base";

export default {
  methods: {
    onLogout() {
      api_auth.logout().then(() => {
        clearLoginInfo();
        this.$router.push({
          name: "login",
        });
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.site-page--not-found {
  height: 100%;
  display: flex;
  justify-content: center;
  // align-items: center;
  overflow: hidden;
  .site-content {
    margin-top: 30vh;
    text-align: center;
  }
  .not-found-title {
    margin: 0 0 15px;
    font-size: 34px;
    font-weight: 400;
    color: #ee8145;
  }
  .not-found-desc {
    margin: 0 0 20px;
    font-size: 24px;
    text-transform: uppercase;
    color: rgb(118, 131, 143);
  }
  .not-found-btn-gohome {
    margin-left: 30px;
  }
  details {
    margin-bottom: 30px;
    font-size: 14px;
    text-align: left;
    color: rgb(118, 131, 143);
    summary {
      font-size: 16px;
      text-align: center;
      cursor: pointer;
      &:focus {
        outline: none;
      }
    }
  }
}
</style>
