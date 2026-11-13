<template>
  <div>
    <div v-for="(urlObj, index) in urlObjs" :key="index">
      <el-button
        v-show="!disabled && urlObjs.length > 1"
        class="opt-button"
        type="danger"
        icon="fa fa-trash-o"
        @click="onRemoveItem(index)"
      ></el-button>
      <url-config ref="urlConfig" :urlObj="urlObj" :disabled="disabled" :needRules="needRules" />
    </div>
    <el-button
      v-show="!disabled"
      class="opt-button"
      type="primary"
      icon="fa fa-plus"
      @click="onAddItem()"
    ></el-button>
  </div>
</template>

<script>
import urlConfig from "./url-config";

export default {
  props: ["dataForm", "disabled", "needRules"],
  components: {
    urlConfig
  },
  data() {
    return {
      urlObjs: []
    };
  },
  watch: {
    dataForm: {
      handler(v, oldV) {
        if (
          v &&
          oldV &&
          v.url === oldV.url &&
          v.httpMethod === oldV.httpMethod &&
          v.remark === oldV.remark
        ) {
          return;
        }

        const obj = this.getDefaultUrlObj();
        const urlArr = v.url ? v.url.split(",") : [obj.url];
        const httpMethodArr = v.httpMethod
          ? v.httpMethod.split(",")
          : [obj.httpMethod];
        const remarkArr = v.remark ? v.remark.split(",") : [obj.remark];
        this.urlObjs = urlArr.map((url, index) => {
          return {
            url,
            httpMethod: httpMethodArr[index],
            remark: remarkArr[index]
          };
        });

        this.$nextTick(this.clearValidate);
      },
      immediate: true
    }
  },
  methods: {
    validate() {
      let result = true;
      for (let item of this.$refs.urlConfig) {
        if (!item.validate()) {
          // 没有直接 return 是为了让每一行都出现校验提示
          result = false;
        }
      }
      return result;
    },
    getUrlInfo() {
      const urlArr = [];
      const httpMethodArr = [];
      const remarkArr = [];
      this.urlObjs.forEach(urlObj => {
        urlArr.push(this.replaceStr(urlObj.url));
        httpMethodArr.push(this.replaceStr(urlObj.httpMethod));
        remarkArr.push(this.replaceStr(urlObj.remark));
      });

      return {
        url: urlArr.join(","),
        httpMethod: httpMethodArr.join(","),
        remark: remarkArr.join(",")
      };
    },
    getDefaultUrlObj() {
      return { url: "", httpMethod: "GET", remark: "" };
    },
    replaceStr(str) {
      return str.replace(",", "，");
    },
    onAddItem() {
      this.urlObjs.push(this.getDefaultUrlObj());
    },
    onRemoveItem(index) {
      this.urlObjs.splice(index, 1);
    },
    // 清除表单校验状态
    clearValidate() {
      if (this.$refs.urlConfig) {
        for (let item of this.$refs.urlConfig) {
          item.clearValidate();
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.opt-button {
  float: right;
  margin-top: 5px;
  margin-left: 5px;
  padding: 5px 8px;
}
</style>
