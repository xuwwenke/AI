<template>
  <div class="app-card">
    <div class="wrapper" @click="onClick">
      <div class="icon">
        <div class="outter"></div>
        <div class="inner">
          <Icon :icon="item.appIcon" size="52" fontSize="42" />
        </div>
      </div>
      <h1 class="ellip" :title="item.appName">{{ item.appName }}</h1>
      <div class="desc" :title="item.appDesc">{{ item.appDesc || '暂无描述内容' }}</div>
    </div>
    <div class="btns">
      <!-- <FavSwitch :item="item" :type="3" /> -->
      <span class="btn" title="编辑" @click.stop="addOrUpdateHandle(item)">
        <i class="fa fa-pencil"></i>
      </span>
      <span class="btn" title="删除" @click.stop="deleteHandle(item.id)">
        <i class="fa fa-times"></i>
      </span>
    </div>
  </div>
</template>

<script>
import { getParsedUrl } from "@/utils/base"
// import FavSwitch from './FavSwitch'
import Icon from './Icon'

export default {
  inject: ['setSystem'],
  props: ['item', 'addOrUpdateHandle', 'deleteHandle'],
  components: {
    // FavSwitch,
    Icon,
  },
  methods: {
    onClick() {
      let tmp = this.item

      // 如果设置了应用地址，则直接跳转
      if (tmp.appUrl) {
        window.location.href = tmp.appUrl
        return
      }

      tmp.key = 'everbi'
      this.setSystem(tmp)
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
