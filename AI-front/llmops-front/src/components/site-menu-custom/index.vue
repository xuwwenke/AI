<template>
  <div
    :class="{
      'site-menu-custom': true,
      collapse,
    }"
  >
    <!-- <transition-group name="list" tag="ul"> -->
    <ul :style="menuStyle">
      <li
        v-for="el in menuList"
        :key="el.id"
        @click="selectMenu({evt: $event, el})"
        :class="{
          active: el.ids.indexOf(currentMenu) > -1,
          hover: el.ids.indexOf(hoverMenu) > -1,
        }"
        :title="el.name"
        @mouseenter="menuMouseEnter(el, 'first')"
      >
        <Icon class="menu-icon" :icon="el.icon" :placeholder="collapse ? 'bars' : ''" />
        <span v-if="!collapse" class="title">{{ el.name }}</span>
        <span v-if="showHot(el) && !collapse" class="header-tag">HOT</span>
        <span v-if="showNew(el) && !collapse" class="header-tag">NEW</span>
        <i v-if="el.children && !collapse" class="el-icon-arrow-right"></i>
      </li>
    </ul>
    <!-- </transition-group> -->
    <div class="s-sider-bar" v-if="isShowExpand && expandMenu">
      <div key="sider-bar-body" class="s-sider-bar-body">
        <h2 class="ellip" :title="expandMenu.title">{{ expandMenu.title }}</h2>
        <ul>
          <li
            v-for="el in expandMenu.list"
            :key="el.id"
            @click="selectMenu({evt: $event, el})"
            :class="{
              active: el.ids.indexOf(currentMenu) > -1,
              hover: el.ids.indexOf(hoverMenu) > -1,
            }"
            :title="el.name"
            @mouseenter="menuMouseEnter(el, 'second')"
            class="menu-li"
          >
            <!-- <i class="icon" :class="'fa fa-' + el.icon"></i> -->
            <span class="ellip">{{ el.name }}</span>
            <span v-if="showHot(el)" class="header-tag">HOT</span>
            <span v-if="showNew(el)" class="header-tag">NEW</span>
            <i v-if="el.children" class="el-icon-arrow-right"></i>
          </li>
        </ul>
      </div>
      <MenuPannel
        key="sider-bar-menu"
        :menu="expandSecondMenu"
        :currentMenu="currentMenu"
        :hotSearch="hotSearch"
        :showNewFunc="showNew"
        :showHotFunc="showHot"
        @click="selectMenu($event)"
        @menuMouseEnter="menuMouseEnter($event, 'third')"
      />
    </div>
  </div>
</template>

<script>
import MenuPannel from "./menu-pannel";
import { menuAction } from "@/router";

export default {
  props: ["collapse", "menuList", "currentMenu"],
  components: {
    MenuPannel,
  },
  data() {
    return {
      globalConfig: window.globalConfig,
      isShowExpand: false,
      apps: [],
      hoverMenu: "",
      expandMenu: null,
      expandSecondMenu: [],
      hotSearch: [],
    };
  },
  watch: {
    menuList: {
      handler(v) {
        this.formatMenus(v);
      },
      immediate: true,
    },
  },
  computed: {
    menuStyle() {
      return {
        width: !this.collapse
          ? window.globalConfig.asideMenuWidth + "px"
          : "64px",
      };
    },
  },
  methods: {
    // mouseout 由外层触发调用，因为 scroll 层是在外层
    // 这里根节点的高度是可能超出侧边栏高度的，用来触发 mouseout 会存在以下问题：
    // 1. 从已展开的菜单往上或往下移出侧边栏边界时，无法触发
    // 2. 从已展开的菜单移到滚动条时，展开层会收回
    onMenuMouseOut() {
      this.isShowExpand = false;
      this.expandMenu = null;
      this.expandSecondMenu = [];
      this.hoverMenu = "";
    },
    formatMenus(menus = []) {
      let ids = [];
      for (let i = 0; i < menus.length; i++) {
        let menu = menus[i];
        ids.push(menu.id);
        menu.ids = [menu.id];
        if (menu.children) {
          let tids = this.formatMenus(menu.children);
          tids.forEach((id) => {
            menu.ids.push(id);
          });
          ids = ids.concat(tids);
        }
      }
      return ids;
    },
    selectMenu(o) {
      const evt = o.evt;
      const menu = o.el;
      if (!menu.children) {
        if (!evt.ctrlKey) this.isShowExpand = false;   // 按住ctrl时，点击不关闭

        menuAction(menu);
        this.$emit('updateContent')
      }
    },
    showNew(el) {
      return (
        el.extendNode &&
        el.extendNode.properties &&
        el.extendNode.properties.length &&
        el.extendNode.properties.some((item) => {
          return item.pName === "new";
        })
      );
    },
    showHot(el) {
      return (
        el.extendNode &&
        el.extendNode.properties &&
        el.extendNode.properties.length &&
        el.extendNode.properties.some((item) => {
          return item.pName === "hot";
        })
      );
    },
    getHotMenu(menuChild, list = []) {
      if (menuChild.length) {
        menuChild.forEach((child) => {
          if (this.showHot(child)) {
            if (!(child.children && child.children.length)) {
              list.push(child);
            }
          }
          if (child.children) {
            this.getHotMenu(child.children, list);
          }
        });
      }
    },
    menuMouseEnter(el, flag) {
      this.hoverMenu = el.id;
      if (flag === "first") {
        this.hotSearch = [];
        if (el.children) {
          this.isShowExpand = true;
          this.expandMenu = {
            title: el.name,
            list: el.children,
          };
        } else {
          this.isShowExpand = false;
        }
        this.expandSecondMenu = [];
      } else if (flag === "second") {
        this.hotSearch = [];
        if (el.children) {
          this.getHotMenu(el.children, this.hotSearch);
          if (!el.childrenMenu) {
            el.childrenMenu = [];
            el.children.forEach((c) => {
              if (c.children) {
                c.children.forEach((item) => {
                  if (item.children) {
                    let list = [];
                    this.formatExpandMenu(item, list);
                    item.children = list;
                  }
                });
              }
              el.childrenMenu.push(c);
            });
          }
          this.expandSecondMenu = el.childrenMenu;
        } else {
          this.expandSecondMenu = [];
        }
      }
    },
    formatExpandMenu(menu, list = []) {
      if (menu.children) {
        menu.children.forEach((el) => {
          this.formatExpandMenu(el, list);
        });
      } else {
        list.push(menu);
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.site-menu-custom {
  .menu-icon {
    flex: 0 0 24px;
    text-align: center;
  }
  &.collapse {
    .menu-icon {
      font-size: 20px;
    }

    & > ul {
      // width: 64px;
      overflow-x: hidden;

      & > li {
        height: 50px;
      }
    }
  }

  & > ul {
    // width: 220px;
    height: 100%;
    user-select: none;
    overflow: auto;
    // padding: 10px 0;
    box-sizing: border-box;
    // transition: all 200ms linear;
    background: var(--site-menu-bg) !important;
    color: var(--site-menu-txt) !important;

    & > li {
      height: 50px;
      line-height: 50px;
      // margin: 8px 0;
      padding: 0 20px;
      box-sizing: border-box;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.1s;
      display: inline-flex;
      align-items: center;
      width: 100%;
      position: relative;
      white-space: nowrap;

      &:hover,
      &.hover {
        background: var(--site-menu-hover-bg) !important;
        color: var(--site-menu-hover-txt) !important;

        & > i {
          &.el-icon-arrow-right {
            right: 10px;
          }
        }
      }

      &.active {
        background: var(--site-menu-active-bg) !important;
        color: var(--site-menu-active-txt) !important;
      }

      > * {
        vertical-align: middle;
      }

      & > i {
        // position: absolute;
        transition: all 100ms linear;

        /* &.fa {
          left: 20px;
        } */

        &.el-icon-arrow-right {
          position: absolute;
          right: 15px;
          top: 50%;
          margin-top: -7px;
        }
      }

      & > .title {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        // padding-right: 20px;
        // width: calc(100% - 40px);
        margin-left: 5px;
        display: inline-block;
      }
    }
  }

  .s-sider-bar {
    position: absolute;
    top: 0;
    right: 0;
    transform: translateX(100%);
    // background-color: #fff;
    // background-color: var(--pop-bg);
    color: var(--pop-txt);
    height: 100%;
    overflow: auto;
    box-sizing: border-box;
    box-shadow: 0 2px 5px 0 rgba(0, 10, 30, 0.3);
    display: inline-flex;
    transition: all 200ms linear;
    // z-index: 9999;
    z-index: 3000;

    .s-sider-bar-body {
      padding: 10px 0;
      height: 100%;
      box-sizing: border-box;
      width: 240px;
      flex: none;
      background: var(--site-sub-menu-bg);

      & > h2 {
        margin: 0 16px 10px;
        font-size: 14px;
        line-height: 32px;
        border-bottom: 1px solid #c7c7c7;
      }

      & > ul {
        overflow: auto;
        height: calc(100% - 40px);
      }
    }
  }

  ::v-deep .menu-li {
    font-size: 14px;
    position: relative;
    line-height: 1.4;
    padding: 8px 30px 8px 16px;
    text-align: left;
    // color: #515666;
    color: var(--pop-txt);
    cursor: pointer;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    display: inline-flex;
    align-items: center;
    width: 100%;
    box-sizing: border-box;
    transition: color 0.3s;

    &:hover,
    &.hover {
      // background: var(--site-menu-hover-bg) !important;
      // color: var(--site-menu-hover-txt) !important;
      // background: var(--pop-hover-bg) !important;
      color: var(--pop-hover-txt) !important;
    }

    &.active {
      // background: var(--site-menu-active-bg) !important;
      // color: var(--site-menu-active-txt) !important;
      // background: var(--pop-active-bg) !important;
      color: var(--pop-active-txt) !important;
      // border-left: 2px solid #000063;
      // font-weight: 700;
    }

    i:not(.icon) {
      position: absolute;
      right: 15px;
    }
  }

  ::v-deep .header-tag {
    display: inline-block;
    // float: right;
    background-color: #fa4642;
    padding: 2px 4px 2px 4px;
    color: #fff;
    font-size: 12px;
    border-radius: 2px;
    margin-left: 5px;
    line-height: 1;
    //
    margin-bottom: 1px;
    transform: scale(0.8, 0.8);
  }
}
</style>
