<template>
  <ul class="app-select">
    <li v-for="menu in firstLevelMenus" :key="menu.id"
      :class="['app-btn', { selected: checkSelected(menu) }]"
      @click="onClick(menu)">
      {{ menu.name }}
    </li>
  </ul>
</template>

<script>
import { mapMutations } from 'vuex';
import { getFirstMenu } from '@/router';

export default {
  computed: {
    firstLevelMenus() {
      let list = JSON.parse(sessionStorage.getItem('menuList') || '[]')

      list = list.filter(item => item.hidden !== 1);

      return list;
    },
    routeFirstLevelMenu() {
      return this.$route.meta.menu.breadcrumb[0];
    }
  },
  watch: {
    $route: {
      handler(v) {
        // 通过路由找到一级菜单 id，再通过 firstLevelMenus 找到一级菜单对象（含 children 二级菜单）
        if (this.firstLevelMenus.length > 0 && this.routeFirstLevelMenu) {
          const firstMenu = this.firstLevelMenus.find(item => item.id === this.routeFirstLevelMenu.id);
          if (firstMenu) this.setCurrentFirstLevelMenu(firstMenu);
        }
      },
      immediate: true
    }
  },
  methods: {
    ...mapMutations('apps', ['setCurrentFirstLevelMenu']),
    checkSelected(menu) {
      return this.routeFirstLevelMenu.id === menu.id;
    },
    onClick(menu) {
      this.setCurrentFirstLevelMenu(menu);

      const firstMenu = getFirstMenu([menu]);
      if (firstMenu) this.$router.push(firstMenu);
    }
  },
}
</script>

<style lang="scss" scoped>
.app-btn {
  display: inline-block;
  padding: 0 10px;
  line-height: inherit;
  cursor: pointer;
  &:hover {
    background: var(--pop-hover-bg);
    color: var(--pop-hover-txt);
  }
}

.selected {
  background: var(--pop-active-bg);
  color: var(--pop-active-txt);
  font-weight: 700;
}
</style>
