import Vue from 'vue';

/**
 * focus时，使用placeholder填充输入框
 * 用法示例：
 * <el-input v-model="xxx" v-fillWithPlaceholder placeholder="用户名"></el-input>
 */
Vue.directive("fillWithPlaceholder", {
  inserted(el) {
    el = el.querySelector('textarea') || el.querySelector('input');
    // let hasValue = false;

    el.onfocus = function (event) {
      const placeholder = el.getAttribute('placeholder');
      if (!this.value && placeholder) {
        this.value = placeholder;
        this.dispatchEvent(new Event('input'));
        // hasValue = false;
      }
    };
    /* el.onblur = function (event) {
      if (!hasValue) {
        this.value = '';
        this.dispatchEvent(new Event('input'));
      }
    }; */
    /* el.onchange = function (event) {
      hasValue = true;
    } */
  },
  unbind: function (el) {
    el.onfocus = el.onblur = el.oninput = null;
  }
});
