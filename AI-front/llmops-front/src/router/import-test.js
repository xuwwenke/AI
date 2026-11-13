// import ModuleLoading from "@/components/module-loading";
import ModuleMissing from "@/components/module-missing";

// 此写法不支持路由守卫
// export default file => () => {
//   const AsyncComponent = () => ({
//     // 需要加载的组件 (应该是一个 `Promise` 对象)
//     component: import(/* webpackChunkName: "[request]" */ "@/views/" + file),
//     // 异步组件加载时使用的组件
//     loading: ModuleLoading,
//     // 加载失败时使用的组件
//     error: ModuleMissing,
//     // 展示加载时组件的延时时间。默认值是 200 (毫秒)
//     delay: 300/* ,
//     // 如果提供了超时时间且组件加载也超时了，
//     // 则使用加载失败时使用的组件。默认值是：`Infinity`
//     timeout: 3000 */
//   });

//   // 包装并返回一个函数式组件
//   return Promise.resolve({
//     functional: true,
//     render(createElement, context) {
//       return createElement(AsyncComponent, context.data, context.children);
//     }
//   });
// };


// module.exports 与 import 同时用会报错
// module.exports = file => () => import("@/views/" + file + ".vue");
// export default file => () => import("@/views/" + file);

export default file => () => {
  return import(/* webpackChunkName: "[request]" */ "@/views/" + file)
    .then(module => {
      return module.default;
    })
    .catch(e => {
      console.log("加载模块出错：", e);
      return ModuleMissing;
    });
};
