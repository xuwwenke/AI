<template>
  <el-tag :type="status.type" :disable-transitions="true">{{ status.label }}</el-tag>
</template>

<script>
export default {
  props: {
    statusList: Array,
    value: Number,
  },
  computed: {
    status() {
      const tmp = this.statusList.find(s => s.value === this.value);
      return tmp || { label: "未知", type: "info" };
    }
  }
}
</script>
