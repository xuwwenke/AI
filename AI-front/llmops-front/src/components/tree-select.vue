<template>
  <el-select ref="select" v-model="fieldLabel" :placeholder="placeholder" :clearable="clearable"
    @clear="clearHandle" @visible-change="onVisibleChange">
    <el-option :value="field">
      <el-tree ref="tree" :accordion="accordion" :data="options" :props="props"
        :node-key="props.value" :default-expanded-keys="defaultExpandedKey"
        :expand-on-click-node="false" @node-click="handleNodeClick"></el-tree>
    </el-option>
  </el-select>
</template>

<script>
export default {
  name: 'tree-select',
  props: {
    // 配置项
    props: {
      type: Object,
      default: () => ({
        value: 'id', // ID字段名
        label: 'title', // 显示名称
        children: 'children' // 子级字段名
      })
    },

    // 选项列表数据(树形结构的对象数组)
    options: { type: Array, default: () => [] },

    // 初始值
    value: { type: Number, default: null },
    // label: { type: String },

    // 占位符
    placeholder: { type: String, default: '请选择' },

    // 可清空选项
    clearable: { type: Boolean, default: true },

    // 自动收起
    accordion: { type: Boolean, default: false }
  },
  data() {
    return {
      field: null,
      fieldLabel: null,
      defaultExpandedKey: []
    };
  },
  methods: {
    init() {
      if (this.field) {
        const node = this.$refs.tree.getNode(this.field);
        if (node) {
          this.fieldLabel = node.label;
          this.defaultExpandedKey = [node.parent.key];
        }
      } else {
        this.fieldLabel = null;
        this.defaultExpandedKey = [];
      }
    },
    onVisibleChange(visible) {
      if (visible) {
        this.$refs.tree.setCurrentKey(this.field);
      }
    },
    // 切换选项
    handleNodeClick(node) {
      this.field = node[this.props.value];
      this.fieldLabel = node[this.props.label];
      this.defaultExpandedKey = [];
      this.$refs.select.blur();
    },
    // 清除选中
    clearHandle() {
      this.field = null;
      this.init();
    }
  },
  watch: {
    value: {
      handler(val) {
        this.field = val;
        this.$nextTick(this.init);
      },
      immediate: true
    },
    field(val) {
      this.$emit('input', val);
    }
  }
};
</script>

<style scoped>
.el-scrollbar .el-scrollbar__view .el-select-dropdown__item {
  height: auto;
  padding: 0;
}
.el-select-dropdown__item.selected {
  font-weight: normal;
}
ul li >>> .el-tree .el-tree-node__content {
  height: auto;
}
.el-tree-node__label {
  font-weight: normal;
}
.el-tree >>> .is-current .el-tree-node__label {
  font-weight: 700;
}
.el-tree >>> .is-current > .el-tree-node__content {
  background: var(--pop-active-bg);
  color: var(--pop-active-txt);
}
.el-tree >>> .is-current .el-tree-node__children .el-tree-node__label {
  font-weight: normal;
}
</style>
