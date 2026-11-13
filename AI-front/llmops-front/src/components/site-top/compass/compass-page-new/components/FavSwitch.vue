<template>
  <i :class="classObj" :title="isFav ? '取消收藏' : '收藏'" @click.stop="onSwitch"></i>
</template>

<script>
import api_compass from '@/api/compass'

export default {
  inject: ['setBubbleAnim'],
  props: {
    item: {
      type: Object,
      default: () => ({}),
    },
    type: Number,
    size: {
      type: String,
      default: 'normal',
    }
  },
  computed: {
    isFav() {
      return this.item.extendNode && this.item.extendNode.favorite
    },
    classObj() {
      return {
        'fa fa-star': this.isFav,
        'fa fa-star-o': !this.isFav,
        mini: this.size === 'mini',
      }
    }
  },
  methods: {
    onSwitch() {
      if (!this.item.extendNode) this.$set(this.item, 'extendNode', {})
      const opt = this.isFav ? 'cancelFavorite' : 'handeFavorite'
      api_compass[opt]({
        // userName: localStorage.getItem('userName', true),
        menuId: this.item.id,
        type: this.type,
      }).then(res => {
        const bool = !this.isFav
        this.$set(this.item.extendNode, 'favorite', bool)
        this.setBubbleAnim(bool)
      })
    },
  }
}
</script>

<style lang="scss" scoped>
.fa {
  font-size: 16px;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    transform: scale(1.3);
  }
  &.fa-star {
    color: #F59E3B;
  }
  &.fa-star-o {
    color: #b6bfc7;
  }
}
.mini {
  font-size: 12px;
  transform: scale(0.8);
  &:hover {
    transform: scale(1);
  }
}
</style>
