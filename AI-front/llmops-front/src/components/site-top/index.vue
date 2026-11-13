<template>
  <div class="site-top">
    <h1>
      <router-link to="/" class="home-link">
        <img v-if="globalConfig.headerLogo" :src="publicPath + globalConfig.headerLogo"
          class="logo" :height="globalConfig.headerLogoHeight || 100" />
        <span class="title">{{ globalConfig.headerTitle }}</span>
      </router-link>
    </h1>
    <first-level-menu v-if="globalConfig.menuStyle === 2" class="top-center" />
    <app-select v-else-if="globalConfig.showAppSelect || globalConfig.useBiCompass" class="top-center" />
    <span>
      <compass v-if="globalConfig.useBiCompass && globalConfig.compassStyle !== 1" class="top-btn" />
      <error-log v-if="globalConfig.showErrorLog" class="top-btn" />
      <theme-select v-if="!isIE && globalConfig.showChangeTheme" class="mr10" />
      <user-opt v-if="globalConfig.showUserOpt" @lockScreen="$emit('lockScreen')" />
    </span>
  </div>
</template>

<script>
import { isIE } from '@/utils/base';
import AppSelect from './app-select';
import ErrorLog from './error-log';
import ThemeSelect from './theme-select';
import UserOpt from './user-opt';
import Compass from './compass';
import FirstLevelMenu from './first-level-menu';

export default {
  data() {
    return {
      globalConfig: window.globalConfig,
      isIE: false,
      publicPath: process.env.BASE_URL,
    };
  },
  created() {
    this.isIE = isIE();
  },
  components: {
    AppSelect,
    ErrorLog,
    ThemeSelect,
    UserOpt,
    Compass,
    FirstLevelMenu,
  },
};
</script>

<style lang="scss">
.site-top {
  height: 100%;
  // vertical-align: middle;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  h1 {
    float: left;
    height: 100%;
    margin: 0;
    font-size: 0;
    user-select: none;
    .home-link {
      display: inline-block;
      height: 100%;
      color: inherit;
      .logo {
        max-height: 100%;
        margin-right: 5px;
        vertical-align: middle;
      }
      .title {
        display: inline-block;
        height: 100%;
        font-size: 20px;
        vertical-align: middle;
        color: #409EFF;
      }
    }
  }
  .top-center {
    flex: 1;
    margin: 0 20px;
    overflow: hidden;
  }
  .top-btn {
    margin-right: 10px;
    line-height: 1;
    font-size: 14px;
    cursor: pointer;
    &:hover {
      color: gray;
    }
  }
  .el-select {
    line-height: 1;
  }
  .el-dropdown {
    line-height: 1;
    cursor: pointer;
  }
}
</style>
