// 邮箱
export function email(s) {
  return /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(
    s
  );
}

// 手机号码
export function mobile(s) {
  return /^1[0-9]{10}$/.test(s);
}

// 座机电话号码
export function phone(s) {
  return /^([0-9]{3,4}-)?[0-9]{7,8}$/.test(s);
}

// URL地址
export function url(s) {
  return /^(http[s]?:)?\/\/.*/.test(s);
}

// IP地址
export function ip(s) {
  return /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/.test(
    s
  );
}

// 数字
export function number(s) {
  return /^-?\d*\.?\d+$/.test(s);
}

// 整数
export function integer(s) {
  return /^-?\d+$/.test(s);
}

// 密码强度校验，密码最少8位，必须包含"大写字母、小写字母、数字、特殊字符"中的三类
export function password(s) {
  return /^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\W_]+$)(?![a-z0-9]+$)(?![a-z\W_]+$)(?![0-9\W_]+$)[a-zA-Z0-9\W_]{10,32}$/.test(s);
}

// 身份证，15位和18位身份证号码，18位最后一位可能是数字或字母
export function idCard(s) {
  return /(^\d{15}$)|(^\d{17}([0-9]|X|x)$)/.test(s);
}

// 护照，至少8位的字母和数字组合
export function passport(s) {
  return /^[A-Za-z0-9]{8,}$/.test(s);
}

// IP地址列表（可以带http或https协议头和端口），各项之间只允许有英文逗号、空格、换行
export function ipList(s) {
  return /^(?:(?:https?:\/\/)?(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(?::\d{1,5})?)(?:[, \n]+(?:(?:https?:\/\/)?(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(?::\d{1,5})?))*$/.test(s);
}

// 域名列表（可以带http或https协议头和端口），各项之间只允许有英文逗号、空格、换行
export function domainList(s) {
  return /^^(?:(?:https?:\/\/)?(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(?::\d{1,5})?)(?:[, \n]+(?:(?:https?:\/\/)?(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(?::\d{1,5})?))*$/.test(s);
}

// MAC地址列表，各项之间只允许有英文逗号、空格、换行
export function macList(s) {
  return /^(?:[0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}(?:[, \n]+(?:[0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2})*$/.test(s);
}
