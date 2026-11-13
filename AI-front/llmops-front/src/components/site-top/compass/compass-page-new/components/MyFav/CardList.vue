<template>
  <div v-if="favList.length > 0" class="container">
    <Card v-for="item in favList" :key="item.key" :item="item" class="card" />
    <!-- <div v-for="(blank, index) in 4" :key="index" class="blank"></div> -->
  </div>
  <NoData v-else />
</template>

<script>
import Card from './Card'
import NoData from '../NoData'

export default {
  props: ['list'],
  components: {
    Card,
    NoData,
  },
  computed: {
    favList() {
      return this.list.filter(item => item.showInHome === 1 && item.extendNode && item.extendNode.favorite)
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  margin-right: -50px;
  // display: flex;
  // flex-wrap: wrap;
  // justify-content: space-evenly;
}
.card {
  margin: 0 41px 22px 0;
}
// .blank {
//   width: 260px;
//   height: 0;
// }
</style>
