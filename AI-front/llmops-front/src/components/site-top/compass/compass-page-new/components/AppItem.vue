<template>
  <span :class="{'app': true, 'disabled': app.deploy == 0, 'hot': app.extendNode && app.extendNode.hot}">
    <div class="wrapper" @click="onClick">
      <span class="tags">
        <span :class="{ tag: true, off: app.deploy == 0 }">{{ app.deploy == 0 ? globalConfig.undeployedAlias : globalConfig.deployedAlias }}</span>
        <span v-if="app.extendNode && app.extendNode.hot" class="tag hot">HOT</span>
      </span>
      <div class="icon">
        <Icon :icon="app.icon" size="32" />
      </div>
      <h1 :title="app.name">{{ app.name }}</h1>
      <div class="desc" :title="app.desc">{{ app.desc || '暂无描述内容' }}</div>
    </div>
    <div class="btns">
      <IntroBtn :item="app" />
      <FavSwitch :item="app" :type="3" />
    </div>
  </span>
</template>

<script>
import { getParsedUrl } from "@/utils/base"
import IntroBtn from './IntroBtn'
import FavSwitch from './FavSwitch'
import Icon from './Icon'

export default {
  inject: ['setSystem'],
  props: ['app'],
  components: {
    IntroBtn,
    FavSwitch,
    Icon,
  },
  data() {
    return {
      globalConfig: window.globalConfig,
    };
  },
  methods: {
    onClick() {
      let tmp = this.app

      if (tmp.deploy == 0) {
        // window.open(getParsedUrl(tmp.introUrl), "_blank");

      } else {
        this.setSystem(tmp)
      }
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../var.scss';

.app {
  position: relative;
  display: inline-block;
  width: 260px;
  height: 130px;
  margin-right: 25px;
  margin-bottom: 22px;
  border: 1px solid rgba(52, 143, 234, 0.2);
  border-radius: 8px;
  transition: all 0.3s;
  vertical-align: middle;

  &.hot {
    border: 1px solid #A2B4E3;
    background: linear-gradient(rgba(255,255,255,0), rgba(149, 181, 255, 0.2));
  }

  &:hover {
    .btns {
      display: block;
    }
  }

  &:not(.disabled) {
    &:hover {
      border-color: $primary-color;
      box-shadow: 0px 3px 6px rgba(67, 107, 217, 0.2);
    }

    .wrapper {
      cursor: pointer;
    }
  }

  .wrapper {
    display: inline-block;
    width: 100%;
    height: 100%;
    padding: 30px 15px 10px;

    >h1 {
      margin-bottom: 5px;
      // font-size: 16px;
      // overflow: hidden;
      // white-space: nowrap;
      // text-overflow: ellipsis;
      font-size: 14px;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
    }
  }
}

.tags {
  position: absolute;
  top: 4px;
  left: 4px;
  .tag + .tag {
    margin-left: 4px;
  }
}

.tag {
  padding: 2px 6px;
  border-radius: 4px;
  background: #6991fc;
  color: #fff;
  font-size: 12px;
  font-weight: normal;

  &.off {
    background: #858695;
  }
  &.hot {
    background: #ed5858;
  }
}

.btns {
  position: absolute;
  top: 0;
  right: 0;
  padding: 6px;
  display: none;

  >* {
    margin-left: 5px;
  }

  .btn {
    font-size: 16px;
    color: #b6bfc7;

    &.disabled {
      color: #d7dbdf;
    }

    &:not(.disabled) {
      cursor: pointer;
    }

    &:not(.disabled):hover {
      color: #436ad9;
    }
  }
}

.icon {
  float: left;
  width: 54px;
  height: 54px;
  margin-right: 12px;
  padding: 10px;
  border: 1px solid rgba(10, 45, 85, 0.2);
  border-radius: 50%;
  overflow: hidden;
}

.desc {
  height: 50px;
  line-height: 24px;
  color: $assist-color;
  overflow: hidden;
  // 多行溢出省略
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
