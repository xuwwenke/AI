<template>
  <div v-if="list.length > 0">
    <div v-for="item in list" v-show="!noSubData.includes(item.key)" :key="item.key" class="app-block">
      <header class="header">
        <h1>
          <Icon :icon="item.icon" size="20" class="fl mr10" />
          <span>{{ item.name }}</span>
          <span v-if="!isEverOne" :class="{ tag: true, off: item.deploy == 0 }">{{ item.deploy == 0 ? globalConfig.undeployedAlias : globalConfig.deployedAlias }}</span>
        </h1>
      </header>
      <section class="menu-container">
        <AppOrMenuList :item="item" @onNoData="onNoData($event, item.key)" />
      </section>
    </div>
    <NoData v-if="noSubData.length === list.length" />
  </div>
  <NoData v-else />
</template>

<script>
import noSubDataMixin from '../noSubDataMixin'
import AppOrMenuList from './AppOrMenuList'
import NoData from '../NoData'
import Icon from '../Icon'

export default {
  mixins: [ noSubDataMixin ],
  props: ['list'],
  components: {
    AppOrMenuList,
    NoData,
    Icon,
  },
  computed: {
    isEverOne() {
      return window.globalConfig.isEverOne
    },
  },
}
</script>

<style lang="scss" scoped>
@import '../../var.scss';

.app-block {
  margin-bottom: 30px;
  border-bottom: 1px solid rgba(10, 45, 85, 0.1);
}

.header {
  height: 54px;
  margin-bottom: 15px;
  padding: 12px 20px;
  border: 1px solid rgba(10, 45, 85, 0.2000);
  border-radius: 8px;
  background: url('../../assets/app-title-left.png') no-repeat, url('../../assets/app-title-right.png') no-repeat right;
  background-size: contain;

  >h1 {
    display: flex;
    align-items: center;
    width: 100%;
    font-size: 16px;
  }

  >div {
    height: 40px;
    color: $assist-color;
    overflow: hidden;
    // 多行溢出省略
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }
}

.tag {
  // float: right;
  margin-left: auto;
  padding: 4px 12px;
  border-radius: 4px;
  background: #6991fc;
  color: #fff;
  font-size: 14px;
  font-weight: normal;

  &.off {
    background: #858695;
  }
}

.menu-container {
  margin-right: -25px;
}
</style>
