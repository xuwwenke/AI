<template>
  <div class="app-card" @click="onClick">
    <div class="badge" :style="badgeStyle"></div>
    <FavDel :item="item" :type="isEverOne ? 2 : 3" class="del" />
    <div class="icon">
      <div class="outter"></div>
      <div class="inner">
        <Icon :icon="item.icon" />
      </div>
    </div>
    <h1 class="ellip" :title="item.name">{{ item.name }}</h1>
  </div>
</template>

<script>
import { getParsedUrl } from "@/utils/base"
import FavDel from './FavDel'
import Icon from '../Icon'

export default {
  inject: ['setSystem'],
  props: ['item'],
  components: {
    FavDel,
    Icon,
  },
  computed: {
    isEverOne() {
      return window.globalConfig.isEverOne
    },
    badgeStyle() {
      const imgVar = this.item.deploy == 0 ? '0' : '1';
      return {
        background: `url(${require('../../assets/deploy-' + imgVar + '-small.png')}) no-repeat`,
      }
    }
  },
  methods: {
    onClick() {
      let tmp = this.item

      if (tmp.deploy == 0) {
        window.open(getParsedUrl(tmp.introUrl), "_blank")

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
@import '../../var.scss';

.app-card {
  position: relative;
  display: inline-block;
  width: 200px;
  height: 60px;
  padding: 10px 10px 10px 30px;
  border: 1px solid rgba(52, 143, 234, 0.2);
  background: linear-gradient(224deg, #fffaf7 0%, #F1FBFD 57%, #EDF0FD 100%);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  >h1 {
    margin-top: 10px;
  }

  &:hover {
    border-color: $primary-color;
    box-shadow: 0px 5px 6px rgba(67, 107, 217, 0.2);

    .del {
      display: block;
    }
  }
}

.badge {
  position: absolute;
  top: -4px;
  left: -3px;
  width: 53px;
  height: 51px;
}

.del {
  position: absolute;
  top: 0;
  right: 0;
  padding: 5px;
  display: none;
}

.icon {
  position: relative;
  float: left;
  width: 40px;
  height: 40px;
  margin-right: 10px;
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
  top: 5px;
  left: 5px;
  width: 30px;
  height: 30px;
  padding: 5px;
  background: #fff;
  border: 1px solid rgba(10, 45, 85, 0.2);
  box-shadow: 0px 2px 1px #FFFFFF;
  border-radius: 50%;
  overflow: hidden;
}
</style>
