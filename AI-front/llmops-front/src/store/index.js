import Vue from "vue";
import Vuex from "vuex";
import cloneDeep from "lodash/cloneDeep";
import apps from "./modules/apps";
import error from "./modules/error";
import mainTabs from "./modules/mainTabs";
import theme from "./modules/theme";
import project from "./modules/project";
import aiChat from "./modules/aiChat";

Vue.use(Vuex);

export default new Vuex.Store({
  strict: process.env.NODE_ENV !== "production",
  modules: {
    apps,
    error,
    mainTabs,
    theme,
    project,
    aiChat
  },
  mutations: {
    // 重置vuex本地储存状态
    resetStore(state) {
      Object.keys(state).forEach(key => {
        if(key === "theme") return;     // 主题设置不重置
        state[key] = cloneDeep(window.initialStoreState[key]);
      });
    }
  },
  actions: {
    // 回退菜单和tab页选中项, 在页面组件中加入如下代码:
    /*
      import { mapActions } from "vuex";

      export default {
        // 注意这是生命周期函数，不能放在methods里
        beforeRouteLeave(to, from, next) {
          // 加延时防止浏览器后退时对话框一闪而过
          setTimeout(() => {
            this.$confirm('确定离开当前页面?', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              next();
            }).catch(() => {
              // 回退菜单和tab页选中项
              this.backMenu();
              next(false);
            });
          }, 0);
        },

        methods: {
          ...mapActions(["backMenu"])
        }
      }
    */
    backMenu(context) {
      context.commit("mainTabs/backActiveName");
    },
  }
});
