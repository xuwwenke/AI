import { Message } from "element-ui";

/**
 * 文件下载, 对于下载链接可直接用 window.open(url, "_blank");
 * @param {*} data Blob、String，二进制数据、url、base64编码
 * @param {*} fileName 下载的文件命名，可带扩展名，跨域下无效
 */
export function downloadFile(data, fileName) {
  let url = "";
  let isBlob = false;
  const errMsg = "下载出错，文件数据无法识别！";

  if (data instanceof Blob) {
    isBlob = true;
    url = window.URL.createObjectURL(data);
  } else if (typeof data == "string") {
    url = data;
  } else {
    Message.error(errMsg);
    return;
  }

  // 将浏览器默认的打开行为改为下载（绝对路径下容易出现）
  if (url && /\.(html|jpg|png|gif|mp4)$/.test(url)) {
    const fileName = url.split("/").pop().split("?")[0];
    const defaultMime = "application/octet-stream";

    const xhr = new XMLHttpRequest();
    xhr.open("GET", url, true);
    xhr.responseType = 'blob';
    xhr.onload = function (e) {
      // console.log(xhr.response);
      if (xhr.response instanceof Blob) {
        downloadFile(xhr.response, fileName, defaultMime);
      } else {
        // 注意：mockjs会将原生的 XMLHttpRequest 覆写为 MockXMLHttpRequest，响应中Blob会被转为字符串，导致文件下载异常的问题
        console.error("下载出错，改为使用 window.open 打开，请关闭 Mock 后再试！");
        // 使用默认行为保底
        window.open(url, "_blank");
      }
    };
    setTimeout(function () { xhr.send(); }, 0);

    return;
  }

  if ("download" in document.createElement("a")) {
    // 非IE下载
    const tmpLink = document.createElement("a");
    tmpLink.download = fileName || "";
    tmpLink.style.display = "none";
    tmpLink.href = url;
    document.body.appendChild(tmpLink);
    tmpLink.click();
    window.URL.revokeObjectURL(tmpLink.href); // 释放URL 对象
    document.body.removeChild(tmpLink);
  } else {
    // IE10+下载
    if (isBlob) {
      window.navigator.msSaveBlob(data, fileName);
    } else if (url) {
      window.open(url, "_blank");
    } else {
      Message.error(errMsg);
      return;
    }
  }
}
