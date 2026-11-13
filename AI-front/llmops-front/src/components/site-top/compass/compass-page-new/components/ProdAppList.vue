<template>
  <div v-if="list.length > 0">
    <div v-for="prod in list" v-show="!noSubData.includes(prod.key)" :key="prod.key" class="prod-block">
      <header class="header">
        <h1>
          <Icon :icon="prod.icon" size="20" class="fl mr10" style="margin-top: 4px" />
          <span>{{ prod.name }}</span>
          <!-- <span :class="{ tag: true, off: prod.deploy == 0 }">{{ prod.deploy == 0 ? globalConfig.undeployedAlias : globalConfig.deployedAlias }}</span> -->
        </h1>
      </header>
      <section class="app-container">
        <AppList :list="prod.apps" @onNoData="onNoData($event, prod.key)" />
      </section>
    </div>
    <NoData v-if="noSubData.length === list.length" />
  </div>
  <NoData v-else />
</template>

<script>
import noSubDataMixin from './noSubDataMixin'
import AppList from './AppList'
import NoData from './NoData'
import Icon from './Icon'

export default {
  mixins: [ noSubDataMixin ],
  props: ['list'],
  components: {
    AppList,
    NoData,
    Icon,
  },
}
</script>

<style lang="scss" scoped>
@import '../var.scss';

.prod-block {
  margin-bottom: 30px;
  border-bottom: 1px solid rgba(10, 45, 85, 0.1);
}

.header {
  height: 50px;
  margin-bottom: 15px;
  padding: 0 20px;
  border-radius: 8px;
  line-height: 50px;
  background: url('../assets/prod-title-bg.png') no-repeat;

  >h1 {
    font-size: 16px;
  }
}

.tag {
  margin-left: 20px;
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

.app-container {
  margin-right: -30px;
}
</style>
