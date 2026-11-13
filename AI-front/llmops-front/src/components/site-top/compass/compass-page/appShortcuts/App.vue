<template>
  <div class="app-shortcuts" v-loading="loading">
    <div class="app-left">
      <div class="my-app">
        <h2 class="app-title">
          <img :src="myAppPic" width="20" height="20" alt="" />我的应用
        </h2>
        <app-links
          :appData="appLinksData"
          @setSystem="$emit('setSystem', $event)"
        />
      </div>
      <div class="app-menus">
        <menu-search
          placeholder="请输入菜单名称或关键字"
          :searchData="searchList"
          @setSystem="$emit('setSystem', $event)"
          @searchDataByEvent="handSearch"
          @close="$emit('close')"
        />
        <menu-classify :list="tagsList" @filterMebuByTag="filterByTag" />
        <menu-links
          :appData="appLinksData"
          @setSystem="$emit('setSystem', $event)"
          @refreshFav="setRefreshFav"
          @close="$emit('close')"
        />
      </div>
    </div>
    <div class="app-right">
      <quick-entry
        :appData="favDataList"
        @setSystem="$emit('setSystem', $event)"
        @refreshFav="setRefreshFav"
        @close="$emit('close')"
      />
      <data-screen @close="$emit('close')" />
    </div>
  </div>
</template>

<script>
import AppLinks from "./AppLinks";
import MenuSearch from "./MenuSearch";
import MenuLinks from "./MenuLinks";
import MenuClassify from "./MenuClassify";
import QuickEntry from "./QuickEntry";
import DataScreen from "./DataScreen";
import { mapState } from "vuex";
import cloneDeep from "lodash/cloneDeep";
export default {
  components: {
    AppLinks,
    MenuSearch,
    MenuLinks,
    MenuClassify,
    QuickEntry,
    DataScreen,
  },
  data() {
    return {
      loading: false,
      appLinksData: [],
      appsData: [],
      favDataList: [],
      searchList: [],
      tagsList: [],
      currentTagObj: {
        id: "",
      },
      myAppPic: require("./public/img/u159.png"),
      originApps: [],
      curInput: "",
    };
  },
  watch: {
    apps: {
      handler(v) {
        this.init();
      },
      immediate: true,
      deep: true,
    }
  },
  // created() {
  //   this.loading = true;
  //   this.init();
  // },
  computed: {
    ...mapState(["apps"]),
  },
  methods: {
    async init() {
      const appList = cloneDeep(this.apps.list);
      this.originApps = this.setAppCrumb(
        appList.filter((app) => app.showInHome !== 0)
      );

      // 先根据罗盘中拖拽的顺序排序，再根据应用管理中的序号排序
      this.originApps = this.originApps.sort((a, b) => {
        if (a.compSeq && b.compSeq) return a.compSeq - b.compSeq
        else if (a.compSeq && !b.compSeq) return -1
        else if (!a.compSeq && b.compSeq) return 1
        else return a.seq - b.seq
      })

      let apps = cloneDeep(this.originApps);

      this.appLinksData = apps;
      // console.log('this.appLinksData', this.appLinksData)
      this.searchList = [];
      this.appLinksData.forEach((res) => {
        this.searchList = this.searchList.concat(res.temp);
      });
      var allMenus = this.getAllMenus(this.appLinksData);
      var tagsList = this.getAllTags(allMenus);
      this.tagsList = tagsList;
      var groupMenus = this.groupMenuByRoot(allMenus);
      groupMenus.forEach((item, i) => {
        this.appLinksData.forEach((item2) => {
          if (item.rootName === item2.name) {
            item.rootId = item2.id;
            item.icon = item2.icon;
            item.crumb = item2.crumb;
            item.app = item2;
          }
        });
        item.data = item.data.filter((row) => row.favorite === 1);
        item.data.sort(this.compare("favoriteSeq")); // 对已收藏的菜单进行排序
      });
      this.favDataList = groupMenus;
      this.loading = false;
    },
    compare(prop) {
      return function (obj1, obj2) {
        var val1 = obj1[prop];
        var val2 = obj2[prop];
        if (!isNaN(Number(val1)) && !isNaN(Number(val2))) {
          val1 = Number(val1);
          val2 = Number(val2);
        }
        if (val1 < val2) {
          return -1;
        } else if (val1 > val2) {
          return 1;
        } else {
          return 0;
        }
      };
    },
    setAppCrumb(list) {
      if (list === null) {
        return [];
      }
      list.forEach((node) => {
        node.temp = [];
        node.menus.forEach((menu) => {
          let tmp;
          if (window.globalConfig.compassMode === 1) {
            var newObj = this.setMenuObject(node, menu);
            newObj.crumb = node.name + " > " + menu.name;
            tmp = [newObj];
          } else {
            tmp = this.calcCrumb(node, menu);
          }
          node.temp = node.temp.concat(tmp);
        });
      });
      return list;
    },
    // 面包屑拼接
    calcCrumb(node, menu) {
      var arr = [];
      var num = 0;
      var self = this;
      function fn(menu) {
        if (num === 0) {
          menu.tempCrumb = node.name;
        }

        // 过滤掉隐藏的菜单及其子菜单
        if (menu.hidden === 1) return arr;

        num++;
        if (menu.children && menu.children.length > 0) {
          if (num === 1) {
            menu.tempCrumb = node.name + " > " + menu.name;
          } else {
            menu.tempCrumb += " > " + menu.name;
          }
          menu.children.forEach((child) => {
            if (child.children && child.children.length > 0) {
              child.tempCrumb = menu.tempCrumb;
              fn(child);
            } else {
              var newObj = self.setMenuObject(node, child);
              newObj.crumb = menu.tempCrumb + " > " + child.name;
              arr.push(newObj);
            }
          });
        } else {
          var newObj = self.setMenuObject(node, menu);
          newObj.crumb = node.name + " > " + menu.name;
          arr.push(newObj);
        }
        return arr;
      }
      var aaa = fn(menu);
      return aaa;
    },
    // 封装menu对象
    setMenuObject(node, item) {
      var obj = {};
      obj.name = item.name;
      obj.url = item.url;
      obj.openMode = item.openMode;
      obj.id = item.id;
      obj.rootName = node.name;
      var propt = item.extendNode && item.extendNode.properties;
      obj.pName = propt && propt[0] && propt[0].pName ? propt[0].pName : "";
      obj.favorite = item.extendNode && item.extendNode.favorite ? item.extendNode.favorite : 0;
      obj.favoriteSeq = item.extendNode && item.extendNode.favoriteSeq
        ? item.extendNode.favoriteSeq
        : null;
      obj.tags = item.extendNode && item.extendNode.tags || [];
      obj.isShowFav = false;
      obj.app = node;
      obj.children = item.children;
      return obj;
    },
    getAllMenus(list) {
      var all = [];
      list.forEach((ele) => {
        if (ele.temp.length > 0) {
          all = all.concat(ele.temp);
        }
      });
      return all;
    },
    // 获取所有的标签
    getAllTags(list) {
      var tmpList = [];
      list.forEach((item1) => {
        if (item1.tags && item1.tags.length > 0) {
          item1.tags.forEach((item2) => {
            var tmp = {};
            tmp.name = item1.name;
            tmp.id = item1.id;
            tmp.tag = item2;
            tmp.isSelected = false;
            tmpList.push(tmp);
          });
        }
      });
      return tmpList;
    },
    // 根据标签过滤数据
    async filterByTag(tagObj) {
      this.loading = true;
      this.currentTagObj = tagObj;
      this.loading = false;
      this.appLinksData = cloneDeep(this.originApps);
      if (tagObj.id !== "") {
        if (this.curInput === "") {
          this.appLinksData.forEach((item) => {
            item.temp = item.temp.filter(
              (row) => row.tags && row.tags.join("").indexOf(tagObj.tag) !== -1
            );
          });
        } else {
          this.appLinksData.forEach((item) => {
            item.inputVal = this.curInput;
            item.temp = item.temp.filter(
              (row) =>
                row.tags &&
                row.tags.join("").indexOf(tagObj.tag) !== -1 &&
                row.crumb.indexOf(this.curInput) !== -1
            );
          });
        }
      } else {
        if (this.curInput !== "") {
          this.appLinksData.forEach((item) => {
            item.inputVal = this.curInput;
            item.temp = item.temp.filter(
              (row) => row.crumb.indexOf(this.curInput) !== -1
            );
          });
        }
      }
    },
    // 根据相同的根节点进行分组
    groupMenuByRoot(menus) {
      var map = {};
      for (let i = 0; i < menus.length; i++) {
        let ai = menus[i];
        if (!map[ai.rootName]) {
          map[ai.rootName] = [ai];
        } else {
          map[ai.rootName].push(ai);
        }
      }
      let res = [];
      Object.keys(map).forEach((key) => {
        res.push({
          rootName: key,
          data: map[key],
        });
      });
      return res;
    },
    setRefreshFav(val, id) {
      var flag = true;
      this.favDataList.forEach((item1) => {
        item1.data.forEach((item2, index2) => {
          if (val.id === item2.id && val.favorite === 0) {
            flag = false;
            item1.data.splice(index2, 1);
          }
        });
      });
      this.favDataList.forEach((item) => {
        if (flag && item.rootId === id) {
          item.data.push(val);
        }
      });
    },
    async handSearch(input) {
      this.curInput = input;
      this.appLinksData = cloneDeep(this.originApps);
      if (this.currentTagObj.id === "") {
        if (input !== "") {
          this.appLinksData.forEach((item) => {
            item.inputVal = input;
            item.temp = item.temp.filter(
              (row) => row.crumb.indexOf(input) !== -1
            );
          });
        }
      } else {
        if (input === "") {
          this.appLinksData.forEach((item) => {
            item.temp = item.temp.filter(
              (row) =>
                row.tags &&
                row.tags.join("").indexOf(this.currentTagObj.tag) !== -1
            );
          });
        } else {
          this.appLinksData.forEach((item) => {
            item.inputVal = input;
            item.temp = item.temp.filter(
              (row) =>
                row.crumb.indexOf(input) !== -1 &&
                row.tags &&
                row.tags.join("").indexOf(this.currentTagObj.tag) !== -1
            );
          });
        }
      }
    },
  },
};
</script>

<style lang="scss">
@import "variable.scss";
@import "cssIcon.css";
@import "tool.css";
.app-shortcuts {
  display: flex;
  padding: $distance-15;
  font-size: 14px;
  line-height: 1;
  a {
    text-decoration: none;
    outline: none;
    color: #333;
  }
  button {
    cursor: pointer;
    outline: 0;
    background-color: transparent;
    border: none;
  }
  dl,
  dd {
    margin: 0;
  }
  .app-left {
    flex: 2;
  }
  .app-right {
    flex: 1;
    margin-left: $distance-15;
    border-radius: 10px;
  }
  .my-app {
    padding-bottom: $distance-15;
    border: 1px solid $color-gray1;
    background-color: #ffffff;
    border-radius: 5px;
    .app-title {
      font-size: 18px;
      color: #333333;
      margin-bottom: 10px;
      background: $color-gray1;
      // padding: 10px $distance-15;
      padding: 7px 5px 13px;
      border-bottom: 1px solid #ebebeb;
      img {
        margin: 0 $distance-15;
        position: relative;
        top: 2px;
      }
    }
  }
  .app-menus {
    padding: $distance-15;
    border: 1px solid $color-gray1;
    background-color: #ffffff;
    margin-top: 15px;
    border-radius: 10px;
  }
}
</style>
