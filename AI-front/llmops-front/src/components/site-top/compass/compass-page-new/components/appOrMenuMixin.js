export default {
  inject: ['getSearchStr'],
  props: ['item'],
  computed: {
    searchStr() {
      return this.getSearchStr()
    },
    appsOrMenus() {
      let result = []

      if (window.globalConfig.isEverOne) {
        result = this.item.apps
      } else {
        const tmp = this.item.menus

        if (window.globalConfig.compassMode === 1) {
          result = tmp
        } else {
          function loop(menus) {
            menus.forEach(menu => {
              if (menu.children) {
                loop(menu.children)
              } else {
                result.push(menu)
              }
            })
          }

          loop(tmp)
        }
      }


      return result
    },
  },
}
