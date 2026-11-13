<template>
  <div v-if="list.length > 0">
    <div v-for="app in list" v-show="!noSubData.includes(app.key)" :key="app.key" class="app-block">
      <header class="header">
        <h1>
          <Icon :icon="app.icon" size="20" class="fl mr10" />
          <span>{{ app.name }}</span>
          <span :class="{ tag: true, off: app.deploy == 0 }">{{ app.deploy == 0 ? globalConfig.undeployedAlias : globalConfig.deployedAlias }}</span>
        </h1>
        <div :title="app.desc">{{ app.desc || '暂无描述内容' }}</div>
      </header>
      <section class="menu-container">
        <MenuList :item="app" @onNoData="onNoData($event, app.key)" />
      </section>
    </div>
    <NoData v-if="noSubData.length === list.length" />
  </div>
  <NoData v-else />
</template>

<script>
import noSubDataMixin from './noSubDataMixin'
import MenuList from './MenuList'
import NoData from './NoData'
import Icon from './Icon'

export default {
  mixins: [ noSubDataMixin ],
  props: ['list'],
  components: {
    MenuList,
    NoData,
    Icon,
  },
}
</script>

<style lang="scss" scoped>
@import '../var.scss';

.app-block {
  margin-bottom: 30px;
  border-bottom: 1px solid rgba(10, 45, 85, 0.1);
}

.header {
  height: 107px;
  margin-bottom: 15px;
  padding: 20px;
  border: 1px solid rgba(10, 45, 85, 0.2000);
  border-radius: 8px;
  background: url('../assets/app-title-left.png') no-repeat, url('../assets/app-title-right.png') no-repeat right;

  >h1 {
    margin-bottom: 12px;
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
  float: right;
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
  margin-right: -30px;
}
</style>
