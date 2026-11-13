<template>
  <div>
    <AppItem v-for="app in filteredApps" :key="app.id" :app="app" />
  </div>
</template>

<script>
import AppItem from './AppItem'
export default {
  inject: ['getSearchStr'],
  props: ['list'],
  components: {
    AppItem,
  },
  computed: {
    filteredApps() {
      const reg = new RegExp(this.getSearchStr(), 'i')
      return this.list.filter(app => app.showInHome === 1 && (app.name.search(reg) !== -1 || (app.desc && app.desc.search(reg) !== -1)))
    }
  },
  watch: {
    'filteredApps.length': {
      handler(v) {
        this.$emit('onNoData', v === 0)
      },
      immediate: true,
    }
  }
}
</script>
