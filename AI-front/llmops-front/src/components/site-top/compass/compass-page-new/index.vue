<template>
  <div class="compass-page">
    <div class="my-fav-btn" @click="showMyFav">
      <i class="fa fa-star"></i>
      <span>我的收藏</span>
      <div :class="['bubble', bubbleAminAdd !== undefined ? (bubbleAminAdd ? 'anim-add' : 'anim-del') : '']"
        @animationend="bubbleAminAdd = undefined">
        <i class="fa fa-star"></i>&nbsp;{{ bubbleAminAdd ? '+1' : '-1' }}
      </div>
    </div>
    <div class="wrapper">
      <h1>
        {{ globalConfig.compassBtnName }}
        <el-link v-if="globalConfig.compassSubBtn && globalConfig.compassSubBtn.name"
          :style="{'vertical-align': 'baseline', ...globalConfig.compassSubBtn.style}"
          :href="globalConfig.compassSubBtn.url">{{ globalConfig.compassSubBtn.name }}</el-link>
      </h1>
      <div class="search">
        <el-input v-model="searchStr" prefix-icon="fa fa-search" placeholder="请输入关键字查找" clearable>
          <!-- <template slot="append">
            <el-button type="primary" icon="el-icon-search" @click="onSearch">查询</el-button>
          </template> -->
        </el-input>
      </div>
      <template v-if="globalConfig.showCustomApp">
        <h2>自定义应用</h2>
        <CustomAppCardList />
      </template>
      <template v-if="!isEverOne">
        <h2>应用</h2>
        <ProdOrAppCardList :list="filteredProdOrAppList" />
      </template>
      <h2>{{ isEverOne ? '产线' : '能力' }}</h2>
      <ProdAppList v-if="isEverOne" :list="prodList" />
      <AppMenuList v-else :list="appList" />
    </div>
    <el-backtop target=".site-main-content"></el-backtop>
    <MyFav ref="myFav" />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { switchApp } from '@/router'
import cloneDeep from 'lodash/cloneDeep'
// import api_prod from '@/api/sys/prod'
import CustomAppCardList from './components/CustomAppCardList'
import ProdOrAppCardList from './components/ProdOrAppCardList'
import ProdAppList from './components/ProdAppList'
import AppMenuList from './components/AppMenuList'
import MyFav from './components/MyFav'

export default {
  provide() {
    return {
      getSearchStr: () => this.searchStr,
      setBubbleAnim: (fav) => {
        this.bubbleAminAdd = fav
      },
      setSystem: this.setSystem,
    }
  },
  components: {
    CustomAppCardList,
    ProdOrAppCardList,
    ProdAppList,
    AppMenuList,
    MyFav,
  },
  data() {
    return {
      globalConfig: window.globalConfig,
      searchStr: '',
      clonedAppsStore: {},
      // allProdList: [],
      bubbleAminAdd: undefined,
    };
  },
  watch: {
    apps: {
      handler(v) {
        this.clonedAppsStore = cloneDeep(v)
      },
      immediate: true,
      deep: true,
    }
  },
  computed: {
    ...mapState(['apps']),
    isEverOne() {
      return window.globalConfig.isEverOne
    },
    prodList() {
      if (!this.isEverOne) return []

      let tmp = []

      this.clonedAppsStore.prodList.forEach(prod => {
        prod.apps = []
        if (prod.appKeyArray) {
          prod.appKeyArray.forEach(appKey => {
            const app = this.clonedAppsStore.list.find(app => app.key === appKey)
            if (app) {
              prod.apps.push(app)
            }
          })

          // 排序
          prod.apps = prod.apps.sort((a, b) => {
            // hot 的往前放
            if (a.extendNode && a.extendNode.hot && (!b.extendNode || !b.extendNode.hot)) return -1
            if ((!a.extendNode || !a.extendNode.hot) && b.extendNode && b.extendNode.hot) return 1
            return a.seq - b.seq
          });
        }
        if (prod.showInHome === 1/*  && prod.apps.length > 0 */) {
          tmp.push(prod)
        }
      })

      // 由于目前未做排序功能，所以强制以三件套中排序为准
      tmp = tmp.sort((a, b) => a.seq - b.seq);

      return tmp
    },
    appList() {
      let tmp = this.clonedAppsStore.list.filter(app => app.showInHome === 1)

      // 由于目前未做排序功能，所以强制以三件套中排序为准
      tmp = tmp.sort((a, b) => a.seq - b.seq);

      return tmp
    },
    filteredProdOrAppList() {
      const list = this.isEverOne ? this.prodList : this.appList
      const reg = new RegExp(this.searchStr, 'i')
      return list.filter(item => item.name.search(reg) !== -1 || (item.desc && item.desc.search(reg) !== -1))
    },
  },
  // created() {
  //   if (this.isEverOne) {
  //     api_prod
  //       .getList({
  //         maxResult: 1000
  //       })
  //       .then(data => {
  //         this.allProdList = data ? data.resultData : [];
  //       })
  //   }
  // },
  methods: {
    showMyFav() {
      this.$refs.myFav.init(this.isEverOne ? this.prodList : this.appList)
    },
    setSystem(app) {
      switchApp(app)
      this.$refs.myFav.visible = false
    },
  },
};
</script>

<style lang="scss" scoped>
@import './var.scss';

::v-deep .compass-page-dialog.el-dialog {
  .el-dialog__header {
    height: 50px;
    padding: 14px 20px;
    background: $primary-color;

    .el-dialog__title {
      color: #fff;
      font-size: 16px;
    }

    .el-dialog__headerbtn {
      top: 16px;

      .el-dialog__close {
        color: #fff;
      }
    }
  }

  .el-dialog__body {
    padding-top: 20px;
  }
}

.compass-page {
  position: relative;
  min-width: 1500px;
  min-height: 1000px;
  background: url(./assets/bg-top.png) no-repeat top, url(./assets/bg-bottom.png) no-repeat bottom, linear-gradient(to right, #f6fafc, #fff, #f6fafc);
  color: $color;
  font-size: 14px;
  overflow: hidden; // 触发 BFC
}

.my-fav-btn {
  position: fixed;
  top: 120px;
  right: 70px;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 46px;
  border: 1px solid $primary-color;
  border-radius: 8px;
  background-color: rgba(67, 107, 217, 0.1);
  line-height: 46px;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    border-color: $primary-color-hover;
    background-color: rgba(21, 83, 255, 0.2);
    color: $primary-color-hover;
  }

  >i {
    margin-right: 5px;
    font-size: 16px;
    color: $primary-color;
  }
}

.bubble {
  position: absolute;
  color: $primary-color;
  font-size: 12px;
  font-weight: bolder;
  opacity: 0;
  user-select: none;
  pointer-events: none;

  &.anim-add {
    animation: bubble-anim-add .7s;
  }

  &.anim-del {
    color: $assist-color;
    animation: bubble-anim-del .7s;
  }
}

@keyframes bubble-anim-add {
  0% {
    top: -40px;
    transform: scale(1.2);
    opacity: 0;
  }

  50% {
    top: -40px;
    transform: scale(1.6);
    opacity: 1;
  }

  100% {
    top: 0;
    opacity: 0;
  }
}

@keyframes bubble-anim-del {
  0% {
    top: 0;
    opacity: 0;
  }

  50% {
    top: -40px;
    transform: scale(1.6);
    opacity: 1;
  }

  100% {
    top: -40px;
    transform: scale(1.2);
    opacity: 0;
  }
}

.wrapper {
  width: 1400px;
  margin: 150px auto 170px;

  >h1 {
    margin-bottom: 50px;
    font-size: 48px;
    text-align: center;
    color: $primary-color;
  }

  >h2 {
    margin-bottom: 15px;
    padding-bottom: 20px;
    font-size: 22px;
    border-bottom: 1px solid #DCDFE6;
  }
}

.search {
  // position: sticky;
  // top: 10px;
  // z-index: 1000;
  width: 70%;
  margin: 0 auto 60px;

  ::v-deep .el-input {
    .el-input__inner {
      height: 50px;
      padding-left: 40px;
      line-height: 50px;
      background-color: #FFF;
      border: 1px solid #DCDFE6;
      // border-radius: 10px 0 0 10px;
      border-radius: 10px;
      color: $color;
      font-size: 16px;
    }

    .el-input__prefix .el-input__icon {
      padding-left: 10px;
      line-height: 50px;
      font-size: 16px;
    }

    /* .el-input-group__append {
      background: $primary-color;
      border-color: $primary-color;
      border-radius: 0 10px 10px 0;
      color: #fff;
      font-size: 20px;
      transition: background-color 0.3s;

      &:hover {
        background: $primary-color-hover;
      }
    } */
  }
}
</style>
