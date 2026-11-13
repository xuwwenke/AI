<!-- 数字输入正则限制框 -->
<template>
  <div class="menu-search">
    <div class="search-input">
      <el-input v-model="key" name="menu-search" :placeholder="placeholder"
        :class="{ hasValue: key }" clearable @input="searchByInput"
        @keyup.enter.native="searchEvent" size="medium"
        v-eventoutside="{ outCallback: onEventoutside }" />
      <!-- <span class="btn-close" @click="clearKey"><i class="cssicon cssicon-close"></i></span> -->
      <menu-popup-window v-show="key && popVisible" @setSystem="$emit('setSystem', $event)"
        @close="$emit('close')" />
    </div>
    <button @click="searchEvent" type="button">
      <i class="cssicon cssicon-search"></i>
    </button>
  </div>
</template>

<script>
import MenuPopupWindow from './MenuPopupWindow';
export default {
  name: 'menu-search',
  props: {
    placeholder: {
      type: String,
      default: '请输入搜索内容'
    },
    searchData: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    searchList() {
      return this.searchData;
    }
  },
  components: {
    MenuPopupWindow
  },
  data() {
    return {
      key: '',
      popVisible: false
    };
  },
  methods: {
    clearKey() {
      this.key = '';
    },
    searchByInput(val) {
      this.popVisible = true;
      this.key = val;
      var list = this.searchList.filter(
        res => res.crumb.indexOf(val.trim()) !== -1
      );
      if (this.key.trim() === '') {
        this.$emit('searchDataByEvent', '');
        this.$bus.emit('UPDATE_SEARCH', { param1: '', param2: [] });
      } else {
        this.$bus.emit('UPDATE_SEARCH', {
          param1: this.key.trim(),
          param2: list
        });
      }
    },
    searchEvent() {
      this.popVisible = false;
      this.$emit('searchDataByEvent', this.key.trim());
    },
    onEventoutside() {
      setTimeout(() => (this.popVisible = false), 0);
    }
  }
};
</script>

<style scoped lang='scss'>
@import 'variable.scss';
.menu-search {
  display: flex;
  margin: $distance-15 * 2 $distance-15 * 5;
  .search-input {
    flex: 1;
    position: relative;
    input {
      display: block;
      width: 100%;
      height: 36px;
      text-indent: 1em;
      border: 1px solid $color-gray1;
      outline: none;
      &:focus {
        border: 1px solid $color-theme;
      }
      &:hover {
        border: 1px solid $color-theme;
      }
      &.hasValue {
        border: 2px solid $color-theme;
        border-bottom: none;
      }
    }
    .btn-close {
      position: absolute;
      top: 9px;
      right: 10px;
      cursor: pointer;
      display: inline-flex;
      &:after {
        content: '';
        position: absolute;
        top: -6px;
        right: -8px;
        bottom: -4px;
        left: -10px;
        background-color: white;
      }
    }
  }
  button {
    width: 36px;
    height: 36px;
    border: none;
    background-color: $color-theme;
    color: white;
    outline: 0;
  }
}
</style>
