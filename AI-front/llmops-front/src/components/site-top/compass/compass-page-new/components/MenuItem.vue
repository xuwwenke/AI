<template>
  <span class="menu" @click="onMenuClick()">
    <FavSwitch :item="menu" :type="1" size="mini" class="fav" />
    <Icon :icon="menu.icon" class="fl mr10" isMenu />
    <span :title="menu.name">{{ menu.name }}</span>
  </span>
</template>

<script>
import { menuAction } from '@/router'
import FavSwitch from './FavSwitch'
import Icon from './Icon'

export default {
  inject: ['setSystem'],
  props: ['menu', 'app'],
  components: {
    FavSwitch,
    Icon,
  },
  methods: {
    onMenuClick() {
      menuAction(this.menu);
      this.setSystem(this.app);
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../var.scss';

.menu {
  position: relative;
  display: inline-block;
  width: 155px;
  height: 46px;
  padding: 13px;
  margin-right: 23px;
  margin-bottom: 15px;
  background: #ebf5fe;
  border: 1px solid rgba(0, 0, 0, 0);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0px 3px 6px rgba(67, 107, 217, 0.2);

    .fav {
      display: block;
    }
  }
}

.fav {
  position: absolute;
  top: -1px;
  right: 0;
  padding: 6px;
  display: none;
}
</style>
