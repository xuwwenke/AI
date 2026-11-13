const install = (Vue) => {
  let eventHistory = {};
  const Bus = new Vue({
    methods: {
      emit(event, ...args) {
        eventHistory[event] = args;
        this.$emit(event, ...args);
      },
      on(event, callback) {
        if (typeof callback !== 'function') {
          console.warn(event + ' 事件监听需要传 callback(Function) [EventBus]');
          return '';
        }
        if (eventHistory[event]) {
          callback.apply(this, eventHistory[event]);
        }
        this.$on(event, callback);
      },
      off(event, callback) {
        this.$off(event, callback);
      },
      clear(event) {
        if (event) {
          this.$off(event);
          eventHistory[event] = undefined;
        } else {
          this.$off();
          eventHistory = {};
        }
      }
    },
  });
  Vue.prototype.$bus = Bus;
}

export default install;
