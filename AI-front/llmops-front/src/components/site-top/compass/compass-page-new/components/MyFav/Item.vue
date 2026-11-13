<template>
  <span class="it" @click="onClick()">
    <FavDel :item="it" :type="isEverOne ? 3 : 1" class="del" />
    <Icon :icon="it.icon" size="20" isMenu class="fl mr10" />
    <span :title="it.name">{{ it.name }}</span>
  </span>
</template>

<script>
import { menuAction } from '@/router'
import FavDel from './FavDel'
import Icon from '../Icon'

export default {
  inject: ['setSystem'],
  props: ['it', 'item'],
  components: {
    FavDel,
    Icon,
  },
  computed: {
    isEverOne() {
      return window.globalConfig.isEverOne
    },
  },
  methods: {
    onClick() {
      let tmp = this.it

      if (window.globalConfig.isEverOne) {
        if (tmp.deploy == 0) {
          window.open(getParsedUrl(tmp.introUrl), "_blank");

        } else {
          this.setSystem(tmp)
        }
      } else {
        menuAction(tmp)
        this.setSystem(this.item)
      }
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../../var.scss';

.it {
  position: relative;
  display: inline-block;
  width: 155px;
  height: 46px;
  padding: 13px;
  margin-right: 21px;
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

    .del {
      display: block;
    }
  }
}

.del {
  position: absolute;
  top: -1px;
  right: 0;
  padding: 6px;
  display: none;
}
</style>
