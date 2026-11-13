<template>
  <i class="el-icon-close" @click.stop="onDel"></i>
</template>

<script>
import api_compass from '@/api/compass'

export default {
  props: {
    item: Object,
    type: Number,
  },
  methods: {
    onDel() {
      api_compass.cancelFavorite({
        // userName: localStorage.getItem('userName', true),
        menuId: this.item.id,
        type: this.type,
      }).then(res => {
        this.$set(this.item.extendNode, 'favorite', false)
      })
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../../var.scss';
.el-icon-close {
  font-size: 12px;
  font-weight: bold;
  color: $assist-color;
  transform: scale(0.7);
  transition: all 0.3s;
  &:hover {
    color: $primary-color;
    transform: scale(1);
  }
}
</style>
