export default {
  namespaced: true,
  state: {
    tabs: [],            // 主框架已打开的tab页
    activeName: '',      // 主框架当前显示的tab页, 也是菜单选中项
    oldActiveName: ''    // 旧的选择项
  },
  mutations: {
    updateTabs(state, payload) {
      state.tabs = payload;
    },
    updateCurrentTab(state, payload) {
      const currentIndex = state.tabs.findIndex(item => item.name === state.activeName);
      state.tabs[currentIndex] = payload;
    },
    // 点菜单时会被调用两次: 1 点击时（site-menu中onSelect）, 2 路由改变时
    updateActiveName(state, payload) {
      if (state.activeName == payload) return;
      state.oldActiveName = state.activeName;
      state.activeName = payload;
    },
    backActiveName(state) {
      state.activeName = state.oldActiveName;
    }
  }
};
