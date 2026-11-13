<template>
  <el-tabs type="card" v-model="mainTabsActiveName" class="site-tabs"
    :closable="mainTabs.tabs.length > 1" @tab-click="onTabClick" @tab-remove="onTabRemove">
    <el-dropdown class="tabs-tools" split-button @click="onRefresh">
      <i class="fa fa-refresh"></i>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item @click.native="onCloseLeft">
          <i class="fa fa-arrow-circle-left"></i> 关闭左侧
        </el-dropdown-item>
        <el-dropdown-item @click.native="onCloseRight">
          <i class="fa fa-arrow-circle-right"></i> 关闭右侧
        </el-dropdown-item>
        <el-dropdown-item @click.native="onCloseOther">
          <i class="fa fa-times-circle"></i> 关闭其它
        </el-dropdown-item>
        <el-dropdown-item @click.native="onCloseAll">
          <i class="fa fa-window-close"></i> 关闭全部
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <el-tab-pane v-for="item in mainTabs.tabs" :key="item.name" :label="item.title"
      :name="item.name" :style="tabPaneStyle">
      <keep-alive v-if="!item.iframeUrl && !needRefresh[item.name]">
        <router-view v-if="item.name === mainTabsActiveName" @onUpdateMain="$emit('onUpdateMain', $event)" />
      </keep-alive>
      <!-- 防止激活tab时, 其中的iframe页总是刷新 -->
      <template v-else-if="item.iframeUrl && !needRefresh[item.name]">
        <iframe-loader :src="item.iframeUrl" :needLoading="false" :isPage="true" />
      </template>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import { mapState, mapMutations } from 'vuex';
import { gotoRoute } from '@/router';
import { COMPASS_KEY, QUERY_TAB } from '@/consts';
import IframeLoader from '@/components/iframe-loader';

export default {
  data() {
    return {
      globalConfig: window.globalConfig,
      needRefresh: {},
    };
  },
  components: {
    IframeLoader,
  },
  computed: {
    ...mapState(['mainTabs']),
    mainTabsActiveName: {
      get() {
        return this.mainTabs.activeName;
      },
      set(val) {
        this.updateActiveName(val);
      },
    },
    tabPaneStyle() {
      return {
        padding:
          this.$route.meta.iframeUrl || this.$route.name === COMPASS_KEY
            ? 0
            : window.globalConfig.mainPadding + 'px',
      };
    },
  },
  watch: {
    $route(v) {
      const vName = v.query[QUERY_TAB]
        ? v.name + '-' + v.query[QUERY_TAB]
        : v.name;

      const tab = this.mainTabs.tabs.find(
        (item) => item.name === this.mainTabs.activeName && item.name === vName
      );

      if (tab) {
        this.updateCurrentTab({
          ...tab,
          params: v.params,
          query: v.query,
          path: v.path,
        });
      }
    },
  },
  methods: {
    ...mapMutations('mainTabs', [
      'updateTabs',
      'updateCurrentTab',
      'updateActiveName',
    ]),
    // tabs, 选中tab
    onTabClick(_tab) {
      gotoRoute({ name: _tab.name }, true);
    },
    // tabs, 删除tab
    onTabRemove(tabName) {
      this.updateTabs(
        this.mainTabs.tabs.filter((item) => item.name !== tabName)
      );
      if (this.mainTabs.tabs.length >= 1) {
        // 当前选中tab被删除
        if (tabName === this.mainTabs.activeName) {
          var tab = this.mainTabs.tabs[this.mainTabs.tabs.length - 1];
          // gotoRoute({name: tab.name});
          this.onTabClick(tab);
          // this.updateActiveName(tab.name);
        }
      } else {
        this.updateActiveName('');
        this.$router.push({ path: '/' });
      }
    },
    // 刷新
    onRefresh() {
      this.$set(this.needRefresh, this.mainTabsActiveName, true);
      this.$nextTick(() => {
        this.$set(this.needRefresh, this.mainTabsActiveName, false);
      });
    },
    // 关闭左侧
    onCloseLeft() {
      const index = this.mainTabs.tabs.findIndex(
        (item) => item.name === this.mainTabs.activeName
      );
      this.updateTabs(this.mainTabs.tabs.slice(index));
    },
    // 关闭右侧
    onCloseRight() {
      const index = this.mainTabs.tabs.findIndex(
        (item) => item.name === this.mainTabs.activeName
      );
      this.updateTabs(this.mainTabs.tabs.slice(0, index + 1));
    },
    // 关闭其它
    onCloseOther() {
      this.updateTabs(
        this.mainTabs.tabs.filter(
          (item) => item.name === this.mainTabs.activeName
        )
      );
    },
    // 关闭全部
    onCloseAll() {
      this.updateTabs([]);
      this.updateActiveName('');
      this.$router.push({ path: '/' });
    },
  },
};
</script>

<style lang="scss">
.site-tabs.el-tabs {
  height: 100%;
  > .el-tabs__header {
    margin-right: 67px;
    margin-bottom: 0;
    border-bottom: none;
    .el-tabs__nav-wrap {
      margin-bottom: 0;
      .el-tabs__nav {
        border: none;
        .el-tabs__item {
          border-bottom: none;
          border-left: none;
          transition: padding 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
        }
      }
    }
  }
  > .el-tabs__content {
    height: calc(100% - 40px);
    overflow: visible;
    > .el-tab-pane {
      box-sizing: border-box;
      height: 100%;
      overflow-x: hidden;
      overflow-y: auto;
    }
    > .tabs-tools {
      position: absolute;
      right: 0;
      top: -40px;
      box-sizing: border-box;
      height: 40px;
      .el-button {
        height: 40px;
        border: none;
        border-radius: 0;
      }
    }
  }
}
</style>
