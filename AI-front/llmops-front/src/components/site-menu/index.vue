<template>
  <site-menu-custom v-if="globalConfig.menuStyle === 1 && mode === 'vertical'" ref="siteMenu"
    :collapse="collapse" :menuList="menuList" :currentMenu="currentMenu" @updateContent="updateContent" />
  <el-menu v-else ref="siteMenu" :mode="mode" :default-active="activeMenuName"
    :collapse="needCollapse" :collapse-transition="false" class="site-menu"
    :unique-opened="globalConfig.asideMenuUniqueOpened" @select="onSelect" :style="menuStyle">
    <sub-menu v-for="menu in menuList" :key="menu.id" :menu="menu" :collapse="needCollapse" @updateContent="updateContent">
    </sub-menu>
  </el-menu>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex';
import cloneDeep from 'lodash/cloneDeep';
import SubMenu from './sub-menu';
import SiteMenuCustom from '@/components/site-menu-custom';
import { QUERY_TAB } from '@/consts';
import { getRouteNameByMenu } from '@/router';

export default {
  name: 'site-menu',
  created() {
    this.setActive(this.$route);
  },
  props: ['mode', 'collapse'],
  components: {
    SubMenu,
    SiteMenuCustom,
  },
  data() {
    return {
      globalConfig: window.globalConfig,
      currentMenu: null,
    };
  },
  computed: {
    ...mapState(['mainTabs', 'apps']),
    ...mapGetters('apps', ['currentApp']),
    activeMenuName() {
      const metaMenu = this.$route.meta.menu
      if (metaMenu && metaMenu.hidden) {
        const lastVisibleParentMenu = metaMenu.breadcrumb.findLast(m => !m.hidden);
        return getRouteNameByMenu(lastVisibleParentMenu);
      }
      return this.$route.name || this.$route.meta.name;
    },
    needCollapse() {
      return this.mode == 'vertical' && this.collapse;
    },
    menuList() {
      let list

      if (window.globalConfig.menuStyle === 2) {
        list = this.apps.currentFirstLevelMenu.children || [];

      } else {
        const currentAppMenus = this.currentApp
          ? cloneDeep(this.currentApp.menus)
          : [];

        list =
          window.globalConfig.showAppSelect || window.globalConfig.useBiCompass
            ? currentAppMenus
            : JSON.parse(sessionStorage.getItem('menuList') || '[]');
      }

      // 过滤掉隐藏的菜单及其子菜单
      function recursionHide(list) {
        let tmp = [];
        list.forEach((item) => {
          if (item.hidden !== 1) {
            tmp.push(item);
            if (item.children) item.children = recursionHide(item.children);
          }
        });
        return tmp;
      }

      return recursionHide(list);
    },
    menuStyle() {
      return {
        // 这里又设宽度是为了防止菜单从折叠到展开时，文字挤在一起
        width:
          this.mode == 'vertical' && !this.collapse
            ? window.globalConfig.asideMenuWidth + 'px'
            : 'auto',
      };
    },
  },
  watch: {
    $route: 'setActive',
  },
  methods: {
    ...mapMutations('mainTabs', ['updateTabs', 'updateActiveName']),
    setActive(route, oldRoute) {
      const menu = route.meta.menu;
      this.currentMenu = (menu && menu.id) || -1;
      const newTab = route.query[QUERY_TAB];
      // 不用path，因为带参数的动态路由匹配时，当前路由path与路由表中的path不相等
      const originName = route.name || route.meta.name || '/'; // 这里的 route.meta.name 是针对嵌套路由
      const name = newTab ? originName + '-' + newTab : originName;
      let title = menu.name;
      title = newTab ? title + '-' + newTab : title;

      // 当两个不同app的菜单地址完全相同时，路由匹配的不一定是预期的app
      this.updateActiveName(name);

      if (window.globalConfig.useTabs) {
        // tab选中, 不存在先添加
        let tab = this.mainTabs.tabs.find((item) => item.name === name);

        if (!tab) {
          tab = {
            name: name,
            path: route.path,
            title: title,
            iframeUrl: route.meta.iframeUrl || '',
            params: route.params,
            query: route.query,
          };

          // 如果是临时tab页（即不与菜单一一对应）
          if (newTab) {
            let index_byPath = -1;
            // 如果存在旧路由，通过path比对，找到原tab页
            if (oldRoute) {
              index_byPath = this.mainTabs.tabs.findIndex(
                (item) =>
                  item.path === oldRoute.path &&
                  item.query[QUERY_TAB] === oldRoute.query[QUERY_TAB]
              );
            }
            // 通过name比对，找到相关tab页
            const index_byName = this.mainTabs.tabs.findIndex(
              (item) => item.name === originName
            );

            const index = index_byPath !== -1 ? index_byPath : index_byName;
            let arr = [...this.mainTabs.tabs];
            arr.splice(index + 1, 0, tab);
            this.updateTabs(arr);
          } else {
            this.updateTabs(this.mainTabs.tabs.concat(tab));
          }
        }
      }
    },
    onSelect(index, indePath) {
      // 用来应对路由跳转被拦截的情况，便于回退菜单和tab页选中项
      this.updateActiveName(index);
    },
    onMenuMouseOut() {
      this.$refs.siteMenu.onMenuMouseOut &&
        this.$refs.siteMenu.onMenuMouseOut();
    },
    updateContent() {
      this.$emit('updateContent')
    }
  },
};
</script>

<style lang="scss">
.site-menu,
.site-menu-pop {
  .menu-icon {
    width: 24px !important;
    text-align: center;
    font-size: 14px;
    color: inherit !important;
  }
  .text {
    margin: 0 5px;
    // 支持多行显示
    display: inline-block;
    line-height: 1;
    white-space: normal;
    max-height: 100%;
  }
  &.el-menu {
    /* .el-menu-item.is-active {
      background: #303030 !important;
    } */
    &:not(.el-menu--horizontal) {
      .el-menu-item.is-active {
        &:before {
          content: ' ';
          top: 0;
          left: 0;
          bottom: 0;
          width: 4px;
          background: #409eff;
          position: absolute;
        }
      }
    }
  }
}

.site-menu {
  &.el-menu {
    border-right: none;
    &.el-menu--collapse {
      .menu-icon {
        font-size: 20px;
      }
    }
  }
}
</style>
