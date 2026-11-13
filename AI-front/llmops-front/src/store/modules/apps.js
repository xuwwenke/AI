export default {
  namespaced: true,
  state: {
    list: [],                                   // 当前用户被授权的所有应用
    originKey: window.globalConfig.appKey,      // 原始应用 key
    currentKey: window.globalConfig.appKey,     // 当前展示的应用 key
    prodList: [],                               // 产线列表
    currentFirstLevelMenu: {},                  // 当前一级菜单（一级菜单在顶部时）
  },
  getters: {
    originApp: state => state.list.find(app => app.key === state.originKey),
    currentApp: state => state.list.find(app => app.key === state.currentKey)
  },
  mutations: {
    setList(state, payload) {
      state.list = payload || [];
    },
    // 设置原始应用
    setOriginKey(state, payload) {
      state.originKey = payload;
    },
    // 切换应用
    setCurrentKey(state, payload) {
      state.currentKey = payload;
    },
    setProdList(state, payload) {
      state.prodList = payload || [];
    },
    setCurrentFirstLevelMenu(state, payload) {
      state.currentFirstLevelMenu = payload || {};
    },
  }
};
