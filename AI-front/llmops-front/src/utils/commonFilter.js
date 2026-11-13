import Vue from "vue";

//时间戳转换为"yyyy-MM-dd HH:mm:ss"格式
Vue.filter("timeFilter", function(val) {
  var unixtimestamp = new Date(val);
  var year = 1900 + unixtimestamp.getYear();
  var month = "0" + (unixtimestamp.getMonth() + 1);
  var date = "0" + unixtimestamp.getDate();
  var hour = "0" + unixtimestamp.getHours();
  var minute = "0" + unixtimestamp.getMinutes();
  var second = "0" + unixtimestamp.getSeconds();
  return (
    year +
    "-" +
    month.substring(month.length - 2, month.length) +
    "-" +
    date.substring(date.length - 2, date.length) +
    " " +
    hour.substring(hour.length - 2, hour.length) +
    ":" +
    minute.substring(minute.length - 2, minute.length) +
    ":" +
    second.substring(second.length - 2, second.length)
  );
});
//字典表中获取的下拉选项过滤
Vue.filter("codeFilter", function(val, list) {
  list && list.forEach(item => {
    if (item.itemValue == val) {
      val = item.itemName;
    }
  });
  return val;
});

//AccessKey-hidden
Vue.filter("accessKeyHiddenFilter", function(val) {
  if (val && val.length == 32) {
    val = val.substring(0, 8) + "****************" + val.substring(25);
  }
  return val;
});
