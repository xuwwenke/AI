// PubSub 在Tab页模式下会造成干扰，不建议使用。举例如下：
// 修复问题：tab形式打开页面模式下，前一个tab里的查询组件没加载完毕，然后切到另一个tab，之后前一个tab中的查询组件加载完成触发查询，影响了当前的tab，而前一个tab中的图表却未被过滤。
// 先尝试通过在PubSub消息中加入pageId，来限制消息的适用范围，发现发布者不方便直接获取正确的pageId。
// 后把所有PubSub改为父子事件传递实现。

// 仪表盘中筛选器条件更新
// export const DASH_FILTERS_UPDATE = "dash.filters.update";

// 仪表盘或图表页下钻或跳转
// export const DASH_CHART_JUMP = "dash.chart.jump";

// 字典更新
export const DIC_UPDATED = "dic.updated";
