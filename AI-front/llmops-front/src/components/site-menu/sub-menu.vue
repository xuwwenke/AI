<template>
  <el-submenu
    v-if="menu.children && menu.children.length >= 1"
    :index="getRouteNameByMenu(menu)"
    popper-class="site-menu-pop"
    :show-timeout="100"
    :hide-timeout="100"
  >
    <template slot="title">
      <Icon class="menu-icon" :icon="menu.icon" :placeholder="collapse ? 'bars' : ''" />
      <span slot="title" class="text" :title="menu.name">{{ menu.name }}</span>
    </template>
    <sub-menu
      v-for="item in menu.children"
      :key="item.id"
      :menu="item"
    ></sub-menu>
  </el-submenu>
  <el-menu-item
    v-else
    :index="getRouteNameByMenu(menu)"
    @click="menuClick(menu)"
  >
    <Icon class="menu-icon" :icon="menu.icon" :placeholder="collapse ? 'bars' : ''" />
    <span slot="title" class="text" :title="menu.name">{{ menu.name }}</span>
  </el-menu-item>
</template>

<script>
import { getRouteNameByMenu, menuAction } from "@/router";

export default {
  name: "sub-menu",
  props: {
    menu: {
      // type: Object,
      required: true,
    },
    collapse: {
      type: Boolean,
    },
  },
  methods: {
    getRouteNameByMenu,
    menuAction,
    menuClick(menu) {
      this.menuAction(menu);
      this.$emit('updateContent')
    }
  },
};
</script>
