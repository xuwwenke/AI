<template>
  <el-dialog title="我的收藏" custom-class="compass-page-dialog" :visible.sync="visible" append-to-body
    width="722px">
    <div class="wrapper">
      <template v-if="!isEverOne">
        <h2>应用</h2>
        <CardList :list="list" />
      </template>
      <h2>{{ isEverOne ? '产线' : '能力' }}</h2>
      <List :list="list" />
    </div>
  </el-dialog>
</template>

<script>
import CardList from './CardList';
import List from './List';

export default {
  components: {
    CardList,
    List,
  },
  data() {
    return {
      visible: false,
      list: [],
    };
  },
  computed: {
    isEverOne() {
      return window.globalConfig.isEverOne
    },
  },
  methods: {
    init(list) {
      this.visible = true
      this.list = list
    },
  },
};
</script>

<style lang="scss" scoped>
.wrapper {
  > h2 {
    margin-bottom: 15px;
    padding-bottom: 20px;
    font-size: 16px;
    border-bottom: 1px solid #dcdfe6;
  }
}
</style>
