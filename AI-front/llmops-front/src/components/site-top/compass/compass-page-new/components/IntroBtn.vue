<template>
  <span :class="{ btn: true, disabled: !item.introUrl }" :title="item.introUrl ? '宣传门户' : '未配置宣传门户'"
    @click.stop="onShowIntro">
    <i class="fa fa-file-text-o"></i>
  </span>
</template>

<script>
import { getParsedUrl } from "@/utils/base"

export default {
  props: {
    item: {
      type: Object,
      default: () => ({}),
    },
  },
  methods: {
    onShowIntro() {
      if (!this.item.introUrl) return
      window.open(getParsedUrl(this.item.introUrl), "_blank")
    }
  }
}
</script>
