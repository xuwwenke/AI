// everone样式新版罗盘/应用导航关键字
export const COMPASS_KEY = "compass";

// url中用来记录appkey的参数
export const QUERY_APPKEY = "_app_";

// 多tab的标识，用于支持打开任意的多tab
export const QUERY_TAB = "_tab_";
// 参考代码：
// 基于同一菜单打开多tab，只需在系统管理->菜单管理中，设置打开方式为“重复打开”即可
// 基于动态路由打开多tab
/* goto(id, name) {
  let route = { name: this.$route.name, query: { ...this.$route.query } };
  route.params = { id };

  if (window.globalConfig.useTabs) {
    let tmp;
    switch (window.globalConfig.detailsOpenMode) {
      case 1:
        tmp = name;
        break;
      case 2:
        tmp = name + "-" + getUUID(5);
        break;
      default:
        tmp = undefined;
    }
    if (tmp) route.query[QUERY_TAB] = tmp;
  }

  this.$router.push(route).catch(() => { });
}
// 基于嵌套路由打开多tab
goto(path, name) {
  let route = { path, query: { ...this.$route.query } };
  if (window.globalConfig.useTabs) route.query[QUERY_TAB] = name;
  this.$router.push(route).catch(() => { });
} */

// 一般类型
export const GENERAL_TYPE = {
  string: "string",
  number: "number",
  integer: "integer",
  date: "date",
  boolean: "boolean",
  blob: "blob"
};

// 目前涉及到的后台返回类型映射
export const JAVA_TYPE_MAPPING = {
  // java类型映射
  "java.lang.String": GENERAL_TYPE.string, //字符串
  "java.util.Clob": GENERAL_TYPE.string, //大字符串
  "java.math.BigDecimal": GENERAL_TYPE.number, //数字，含小数
  "java.lang.Byte": GENERAL_TYPE.integer, //整数
  "java.lang.Long": GENERAL_TYPE.integer, //整数
  "java.lang.Integer": GENERAL_TYPE.integer, //整数
  "java.lang.Short": GENERAL_TYPE.integer, //整数
  "java.util.Date": GENERAL_TYPE.date, //日期时间戳
  "java.lang.Boolean": GENERAL_TYPE.boolean, //布尔
  "java.sql.Blob": GENERAL_TYPE.blob, //二进制
  // 表达式返回结果类型
  string: GENERAL_TYPE.string,
  number: GENERAL_TYPE.number,
  integer: GENERAL_TYPE.integer,
  date: GENERAL_TYPE.date,
  boolean: GENERAL_TYPE.boolean,
  blob: GENERAL_TYPE.blob
};

// 表单控件类型
export const FORM_COMP_TYPE = {
  text: "text",
  input: "input",
  select: "select",
  cascader: "cascader",
  date: "date",
  datetime: "datetime",
  dategrain: "dategrain",
  datetimerange: "datetimerange",
  compare: "compare",
  file: "file",
  expression: "expression"
};

// 校验规则
export const VALIDATE_LIST = [
  {
    value: "email",
    label: "邮箱",
    help: "who@demo.com"
  },
  {
    value: "mobile",
    label: "手机号码",
    help: "130xxxxxxxx"
  },
  {
    value: "phone",
    label: "电话号码",
    help: "000-12345678 或 12345678"
  },
  {
    value: "url",
    label: "URL地址",
    help: "http(s)://demo.com"
  },
  {
    value: "ip",
    label: "IP地址",
    help: "255.255.255.255"
  }
];
