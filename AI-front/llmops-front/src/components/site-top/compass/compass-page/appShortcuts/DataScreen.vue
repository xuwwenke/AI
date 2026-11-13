<template>
  <div class="data-screen">
    <img
      v-for="(pic, index) in dataList"
      :key="index"
      :src="pic.newObj.img"
      @click="linkUrl(pic.newObj)"
    />
  </div>
</template>

<script>
import { getDic } from "@/utils/dic";

export default {
  name: "data-screen",
  data() {
    return {
      dataList: [],
    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      getDic(["compass_quick_access"], (data) => {
        var list = data.compass_quick_access;
        if (list.length > 0) {
          list.forEach((ele) => {
            ele.newObj = JSON.parse(ele.itemValue);
          });
        }
        list.sort(this.compare("itemSort"));
        this.dataList = list;
      });
    },
    compare(prop) {
      return function (obj1, obj2) {
        var val1 = obj1[prop];
        var val2 = obj2[prop];
        if (!isNaN(Number(val1)) && !isNaN(Number(val2))) {
          val1 = Number(val1);
          val2 = Number(val2);
        }
        if (val1 < val2) {
          return -1;
        } else if (val1 > val2) {
          return 1;
        } else {
          return 0;
        }
      };
    },
    linkUrl(obj) {
      if (obj.action === "self") {
        window.location.href = obj.url;
      } else {
        window.open(obj.url);
      }
      this.$emit("close");
    },
  },
};
</script>

<style scoped lang="scss">
@import "variable.scss";
.data-screen {
  width: 100%;
  border-radius: 5px;
  margin-top: $distance-15;
  img {
    width: 100%;
    cursor: pointer;
    margin-bottom: $distance-15;
  }
}
</style>
