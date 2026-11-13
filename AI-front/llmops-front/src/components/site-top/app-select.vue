<template>
  <!-- el-select 不能自适应宽度 -->
  <!-- <el-select :value="currentApp.key" @change="onChangeApp" size="mini">
    <el-option v-for="app in appList" :key="app.key" :value="app.key" :label="app.name"></el-option>
  </el-select>-->
  <div ref="topBtns" class="app-select">
    <template v-if="globalConfig.appSelectStyle === 1">
      <ul class="top-btns">
        <li v-for="app in appTopList" :key="app.key"
          :class="['app-btn', { selected: checkSelected(app) }]"
          @click="handleCommandLeft(app)">
          {{ app.name }}
        </li>
        <li v-if="extraApp" class="app-btn selected">
          {{ extraApp.name }}
        </li>
      </ul>
      <template v-if="appMoreList.length > 0">
        <el-popover v-if="globalConfig.appMoreStyle === 1" v-model="popVisible" trigger="hover">
          <div style="display: flex; flex-direction: column; width: calc(100vw - 40px); max-height: 400px;">
            <div style="margin-bottom: 20px; text-align: center;">
              <el-input v-model="searchStr" style="width: 800px; max-width: 100%;" placeholder="搜索应用名称或key" prefix-icon="el-icon-search" clearable></el-input>
            </div>
            <div style="flex: 1; overflow: auto;">
              <div v-if="appSearchedList.length === 0" style="text-align: center; color: lightgray">没有匹配的应用</div>
              <div class="grid-container">
                <div v-for="app in appSearchedList" :key="app.key" :title="app.name + ' (' + app.key + ')'"
                  :class="{ 'grid-item': true, 'ellip': true, selected: checkSelected(app) }"
                  @click="handleCommandRight(app)">
                  {{ app.name }}
                  <div v-if="searchStr" style="color: lightgray">{{ app.key }}</div>
                </div>
              </div>
            </div>
          </div>
          <i slot="reference" class="app-btn el-icon-more"></i>
        </el-popover>
        <el-dropdown v-else size="default" style="line-height: inherit"
          @command="handleCommandRight" trigger="hover" placement="bottom">
          <i class="app-btn el-icon-more"></i>
          <el-dropdown-menu slot="dropdown"
            :class="{ 'dropdown-max-height': appMoreList.length > 10 }">
            <div style="position: sticky; top: -10px; background: #fff; padding: 0 10px;">
              <el-input v-model="searchStr" class="fw" placeholder="搜索应用名称或key" prefix-icon="el-icon-search" clearable></el-input>
            </div>
            <el-dropdown-item v-if="appSearchedList.length === 0" disabled>没有匹配的应用</el-dropdown-item>
            <el-dropdown-item v-for="app in appSearchedList" :key="app.key" :command="app"
              :class="{ selected: checkSelected(app) }">
              {{ app.name }}<span v-if="searchStr" class="fr ml20" style="color: lightgray">{{ app.key }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
    </template>
    <el-dropdown v-else @command="onSelect" trigger="hover" size="default" placement="bottom">
      <span class="el-dropdown-link">
        <span>{{ currentApp ? currentApp.name : compassApp.name }}</span>
        <i class="el-icon-arrow-down el-icon--right"></i>
      </span>
      <el-dropdown-menu slot="dropdown" :class="{ 'dropdown-max-height': appList.length > 10 }">
        <el-dropdown-item v-for="app in appList" :key="app.key" :command="app"
          :class="{ selected: checkSelected(app) }">
          {{ app.name }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex';
import { switchApp } from '@/router';
import cloneDeep from 'lodash/cloneDeep';
import { textSize } from '@/utils/base';
import { COMPASS_KEY, QUERY_APPKEY } from '@/consts';

export default {
  data() {
    return {
      globalConfig: window.globalConfig,
      topBtnNum: 0,
      topBtnMarginPadding: 20,
      extraApp: null,
      searchStr: '',
      popVisible: false,
      compassApp: {
        key: COMPASS_KEY,
        name: window.globalConfig.compassBtnName,
      }
    };
  },
  computed: {
    ...mapState(['apps', 'theme']),
    ...mapGetters('apps', ['currentApp']),
    appList() {
      let tmp = this.apps.list.filter(app => app.showInHome == 1 && app.deploy != 0);
      if (window.globalConfig.useBiCompass) {
        if (window.globalConfig.compassStyle === 1) {
          // 新版应用导航未做排序功能，以三件套中排序为准
          tmp = tmp.sort((a, b) => a.seq - b.seq);
          tmp.unshift(this.compassApp);
        } else {
          // 旧版功能罗盘有做排序功能，先根据罗盘中拖拽的顺序排序，再根据应用管理中的序号排序
          tmp = tmp.sort((a, b) => {
            if (a.compSeq && b.compSeq) return a.compSeq - b.compSeq
            else if (a.compSeq && !b.compSeq) return -1
            else if (!a.compSeq && b.compSeq) return 1
            else return a.seq - b.seq
          })
        }
      }
      return tmp;
    },
    appTopList() {
      return this.appList.slice(0, this.topBtnNum);
    },
    appMoreList() {
      return this.appList.slice(this.topBtnNum);
    },
    appSearchedList() {
      if (this.searchStr)
        return this.appList.filter(app => app.name.toLowerCase().includes(this.searchStr.toLowerCase()) || app.key.toLowerCase().includes(this.searchStr.toLowerCase()))
      return this.appMoreList
    },
    topBtnWidthArr() {
      let fs = "12px";
      let mp = this.topBtnMarginPadding;
      if (this.theme.name === 'blue-nssa') {
        fs = "14px";
        mp += 4;
      } else if (this.theme.name === 'blue-light') {
        fs = "14px";
        mp += 10;
      }
      return this.appList.map(
        app => textSize(app.name, fs).width + mp
      );
    },
    isCompassApp() {
      return this.$route.query[QUERY_APPKEY] === COMPASS_KEY;
    }
  },
  watch: {
    extraApp(v) {
      this.resize();
    },
    appMoreList(v) {
      this.getExtraApp(v);
    },
    isCompassApp: function() {
      this.getExtraApp(this.appMoreList);
    },
    'apps.currentKey': function(v) {
      this.getExtraApp(this.appMoreList);
    },
  },
  mounted() {
    this.resize();
    window.addEventListener('resize', this.resize);
  },
  destroyed() {
    window.removeEventListener('resize', this.resize);
  },
  methods: {
    resize() {
      this.topBtnNum = this.getTopBtnNum();
    },
    getTopBtnNum() {
      // 处理顶部菜单显示个数
      let num = 0;
      let width = 0;
      let tmpList = cloneDeep(this.topBtnWidthArr);

      // 将右侧下拉列表中的选中项，直接显示在顶部
      const extraW = this.extraApp
        ? textSize(this.extraApp.name).width + this.topBtnMarginPadding
        : 0;

      const containerW = this.$refs.topBtns.clientWidth - 50 - extraW;

      if (containerW <= 0) return 0;

      while (width < containerW) {
        num++;
        const addW = tmpList.shift();
        width += addW;
        // console.log(addW, width, containerW)
      }

      return Math.max(0, num - 1);
    },
    handleCommandLeft(app) {
      this.extraApp = null;
      this.onSelect(app);
    },
    handleCommandRight(app) {
      this.extraApp = app;
      this.popVisible = false;
      this.onSelect(app);
    },
    onSelect(app) {
      switchApp(app);
    },
    checkSelected(app) {
      if (this.isCompassApp) return app.key === COMPASS_KEY;
      return app.key === this.apps.currentKey
    },
    getExtraApp(v) {
      if (v.find(app => app.key === this.apps.currentKey && !this.isCompassApp)) {
        this.extraApp = this.currentApp;
      } else {
        this.extraApp = null;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.el-dropdown-link {
  font-size: 12px;
}
// 加样式会造成小箭头不可见，所以只在必要时才加
.dropdown-max-height {
  max-height: 80vh;
  overflow-x: hidden;
  overflow-y: auto;
  &::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
}
.app-select {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  overflow: hidden;
}
.top-btns {
  display: inline-block;
  // flex: 1;
  // white-space: nowrap;
  // overflow: hidden;
}

.app-btn {
  display: inline-block;
  padding: 0 10px;
  line-height: inherit;
  cursor: pointer;
  &:hover {
    background: var(--pop-hover-bg);
    color: var(--pop-hover-txt);
  }
}

.selected {
  background: var(--pop-active-bg);
  color: var(--pop-active-txt);
  font-weight: 700;
}

.grid-container {
  display: grid;
  grid-gap: 10px;
  grid-template-columns: repeat(6, 1fr);

  .grid-item {
    border-radius: 5px;
    padding: 10px;
    cursor: pointer;
    &:hover {
      color: var(--pop-hover-txt);
    }
  }
}

@media screen and (max-width: 1400px) {
  .grid-container {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
