const NUM = 10;

export default {
  namespaced: true,
  state: {
    request: [], // 网络请求异常
    logic: []    // js等逻辑异常
  },
  mutations: {
    pushRequestError(state, payload) {
      state.request.unshift(payload);
      if(state.request.length > NUM) state.request.pop();
    },
    pushLogicError(state, payload) {
      state.logic.unshift(payload);
      if(state.logic.length > NUM) state.logic.pop();
    }
  }
};
