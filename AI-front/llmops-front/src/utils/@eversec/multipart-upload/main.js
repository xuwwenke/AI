import MultipartUpload from "./src/main";

/* istanbul ignore next */
MultipartUpload.install = function (Vue) {
  Vue.component(MultipartUpload.name, MultipartUpload);
};

export default MultipartUpload;
