<template>
  <div>
    <Item v-for="it in favAppsOrMenus" :key="it.id" :it="it" :item="item" />
  </div>
</template>

<script>
import appOrMenuMixin from '../appOrMenuMixin'
import Item from './Item'
export default {
  mixins: [ appOrMenuMixin ],
  components: {
    Item,
  },
  computed: {
    favAppsOrMenus() {
      function isShow(item) {
        return window.globalConfig.isEverOne ? item.showInHome === 1 : !item.hidden
      }
      return this.appsOrMenus.filter(item => isShow(item) && item.extendNode && item.extendNode.favorite)
    },
  },
  watch: {
    'favAppsOrMenus.length': {
      handler(v) {
        this.$emit('onNoData', v === 0)
      },
      immediate: true,
    }
  }
}
</script>
