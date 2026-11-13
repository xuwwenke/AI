export const fillLinkEle = {
  methods: {
    fillLinkEle(parentEleRef, subEle_Width) {
      this.$nextTick(function () {
        const appLinks = this.$refs[parentEleRef]
        const total_w = Number.parseFloat(window.getComputedStyle(appLinks).width)
        const count = Math.floor(total_w / subEle_Width)
        const remainder = this.data.length % count
        const resultCount = count - remainder
        for (let i = 0; i < resultCount; i++) {
          this.data.push({
            iconUrl: '',
            name: '',
            href: '',
            isFilled: true,
          })
        }
      })
    }
  }
};
