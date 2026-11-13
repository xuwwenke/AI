<template>
  <div v-if="list.length > 0" class="container">
    <ProdOrAppCard v-for="item in list" :key="item.key" :item="item" class="card" />
    <!-- <div v-for="(blank, index) in 4" :key="index" class="blank"></div> -->
  </div>
  <NoData v-else />
</template>

<script>
import ProdOrAppCard from './ProdOrAppCard'
import NoData from './NoData'

export default {
  props: ['list'],
  components: {
    ProdOrAppCard,
    NoData,
  },
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
  margin: 0 35px 26px 0;
}
// .blank {
//   width: 260px;
//   height: 0;
// }
</style>
