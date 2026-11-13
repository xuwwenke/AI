export default {
  inject: ['getSearchStr'],
  data() {
    return {
      globalConfig: window.globalConfig,
      noSubData: [],
    }
  },
  computed: {
    searchStr() {
      return this.getSearchStr()
    },
  },
  methods: {
    onNoData(v, key) {
      let set = new Set(this.noSubData)
      if (v) {
        set.add(key)
      } else {
        set.delete(key)
      }
      this.noSubData = Array.from(set)
    }
  }
}
