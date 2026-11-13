<template>
  <div>
    <MenuItem v-for="menu in filteredMenus" :key="menu.id" :menu="menu" :app="item" />
  </div>
</template>

<script>
import appOrMenuMixin from './appOrMenuMixin'
import MenuItem from './MenuItem'
export default {
  mixins: [ appOrMenuMixin ],
  components: {
    MenuItem,
  },
  computed: {
    filteredMenus() {
      const reg = new RegExp(this.searchStr, 'i')
      return this.appsOrMenus.filter(menu => !menu.hidden && menu.name.search(reg) !== -1)
    },
  },
  watch: {
    'filteredMenus.length': {
      handler(v) {
        this.$emit('onNoData', v === 0)
      },
      immediate: true,
    }
  }
}
</script>
