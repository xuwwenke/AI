<template>
  <div>
    <el-image v-if="/\/|\./.test(icon)" :src="icon" :style="{ width: size + 'px', height: size + 'px' }">
      <div slot="error" class="image-slot">
        <img :src="isMenu ? menuIconHolder : appIconHolder" :width="isMenu ? 15 : size" :height="size" />
      </div>
    </el-image>
    <Icon v-else-if="icon" :icon="icon" :style="{
      width: size + 'px',
      height: size + 'px',
      'font-size': fontSize ? fontSize + 'px' : undefined,
      'line-height': size + 'px'
    }" />
    <img v-else :src="isMenu ? menuIconHolder : appIconHolder" :width="isMenu ? 15 : size" :height="size" />
  </div>
</template>

<script>
import appIconHolder from '../assets/app-icon-holder.svg';
import menuIconHolder from '../assets/menu-icon-holder.png';

export default {
  props: {
    icon: String,
    size: {
      type: [Number, String],
      default: 18,
    },
    fontSize: [Number, String],
    isMenu: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      appIconHolder,
      menuIconHolder,
    };
  },
};
</script>

<style lang="scss" scoped>
.fa {
  display: inline-block;
  text-align: center;
}
</style>
