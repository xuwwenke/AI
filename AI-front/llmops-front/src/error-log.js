import store from "./store";
import Vue from "vue";

// 监听和写入脚本异常日志
// 普通js错误
window.onerror = function (
  errorMessage,
  scriptURI,
  lineNumber,
  columnNumber,
  errorObj
) {
  // 用到 el-table 的页面在 resize 的时候总是会报这个错, 所以忽略
  if (errorMessage === "ResizeObserver loop limit exceeded") return;

  store.commit("error/pushLogicError", {
    timestamp: new Date().getTime(),
    message: errorMessage,
    scriptURI,
    lineNumber,
    columnNumber,
    errorObj
  })
};

// vue组件渲染时错误
Vue.config.errorHandler = function (err, vm, info) {
  Vue.nextTick(() => {
    const vm_name = formatComponentName(vm);

    store.commit("error/pushLogicError", {
      timestamp: new Date().getTime(),
      message: err.message,
      scriptURI: window.location.href,
      component: vm_name
    })

    // 在开发模式下打印 log
    if (process.env.NODE_ENV === 'development') {
      console.error(vm_name, info, err);
    }
  });
};

function formatComponentName(vm, includeFile) {
  if (vm.$root === vm) {
    return '<Root>'
  }
  const options = typeof vm === 'function' && vm.cid != null
    ? vm.options
    : vm._isVue
      ? vm.$options || vm.constructor.options
      : vm
  let name = options.name || options._componentTag
  const file = options.__file
  if (!name && file) {
    const match = file.match(/([^/\\]+)\.vue$/)
    name = match && match[1]
  }

  const classifyRE = /(?:^|[-_])(\w)/g
  const classify = str => str
    .replace(classifyRE, c => c.toUpperCase())
    .replace(/[-_]/g, '')

  return (
    (name ? `<${classify(name)}>` : `<Anonymous>`) +
    (file && includeFile !== false ? ` at ${file}` : '')
  )
}
