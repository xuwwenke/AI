<template>
  <div :class="{ 'app-card': true, 'disabled': item.deploy == 0 }">
    <div class="wrapper" @click="onClick">
      <div class="badge" :style="badgeStyle"></div>
      <div class="icon">
        <div class="outter"></div>
        <div class="inner">
          <Icon :icon="item.icon" size="52" fontSize="42" />
        </div>
      </div>
      <h1 class="ellip" :title="item.name">{{ item.name }}</h1>
      <div class="desc" :title="item.desc">{{ item.desc || '暂无描述内容' }}</div>
    </div>
    <div class="btns">
      <IntroBtn :item="item" />
      <FavSwitch :item="item" :type="isEverOne ? 2 : 3" />
    </div>
  </div>
</template>

<script>
import { getParsedUrl } from "@/utils/base"
import IntroBtn from './IntroBtn'
import FavSwitch from './FavSwitch'
import Icon from './Icon'

export default {
  inject: ['setSystem'],
  props: ['item'],
  components: {
    IntroBtn,
    FavSwitch,
    Icon,
  },
  computed: {
    isEverOne() {
      return window.globalConfig.isEverOne
    },
    badgeStyle() {
      const imgVar = this.item.deploy == 0 ? '0' : '1';
      return {
        background: `url(${require('../assets/deploy-' + imgVar + '.png')}) no-repeat`,
      }
    }
  },
  methods: {
    onClick() {
      let tmp = this.item

      if (tmp.deploy == 0) {
        // window.open(getParsedUrl(tmp.introUrl), "_blank")

      } else {
        if (window.globalConfig.isEverOne) {
          if (tmp.apps.length > 0) {
            tmp = tmp.apps[0]
          } else {
            window.open(getParsedUrl(tmp.url), "_blank")
            return
          }
        }

        this.setSystem(tmp)
      }
    },
  }
}
</script>

<style lang="scss" scoped>
@import '../var.scss';

.app-card {
  position: relative;
  display: inline-block;
  width: 252px;
  height: 260px;
  border: 1px solid rgba(52, 143, 234, 0.2);
  background: linear-gradient(224deg, #FFFDF7 0%, #F1FBFD 57%, #EDF0FD 100%);
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    .btns {
      display: block;
    }
  }

  &:not(.disabled) {
    &:hover {
      border-color: $primary-color;
      box-shadow: 0px 5px 6px rgba(67, 107, 217, 0.2);
    }

    .wrapper {
      cursor: pointer;
    }
  }

  .wrapper {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    height: 100%;
    padding: 20px;

    >h1 {
      width: 100%;
      margin-bottom: 12px;
      font-size: 16px;
      text-align: center;
    }
  }
}

.badge {
  position: absolute;
  top: -3px;
  left: -3px;
  width: 62px;
  height: 64px;
}

.btns {
  position: absolute;
  top: -2px;
  right: 0;
  padding: 10px;
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
  position: relative;
  width: 102px;
  height: 102px;
  margin-bottom: 15px;
}

.outter {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(181deg, #FFFFFF 0%, $primary-color 100%);
  border-radius: 50%;
  opacity: 0.15;
}

.inner {
  position: absolute;
  top: 9px;
  left: 9px;
  width: 84px;
  height: 84px;
  padding: 15px;
  background: #fff;
  border: 1px solid rgba(10, 45, 85, 0.2);
  box-shadow: 0px 2px 1px #FFFFFF;
  border-radius: 50%;
  text-align: center;
  overflow: hidden;
}

.desc {
  height: 70px;
  line-height: 24px;
  color: $assist-color;
  overflow: hidden;
  // 多行溢出省略
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}
</style>
