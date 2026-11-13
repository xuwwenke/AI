<template>
  <el-popover v-model="visible" placement="bottom" trigger="click" :disabled="disabled"
    :popper-class="'icon-select-popover' + (type === 'icon' ? ' iconfont' : '')" @after-leave="searchStr = ''">
    <el-input v-if="type === 'icon'" v-model="searchStr" placeholder="关键字搜索" clearable class="mb10"></el-input>
    <label v-if="finalIconList.length === 0">未搜索到图标</label>
    <div v-else class="icon-select-list">
      <el-button v-for="(item, index) in finalIconList" :key="index" :class="{ 'is-active': getValue(item) === field }"
        :title="getTitle(item)" @click="onSelect(getValue(item))">
        <Icon :icon="getValue(item)" />
      </el-button>
    </div>
    <div v-if="type !== 'icon'" class="mt10 tar">
      <a :href="moreUrl" target="_blank">查看更多</a>
    </div>
    <el-input v-model="field" slot="reference" :placeholder="placeholder" clearable></el-input>
  </el-popover>
</template>

<script>
import MENU_ICON_LIST from "@/consts/menuIcons";
import BTN_ICON_LIST from "@/consts/btnIcons";
import httpRequestApi from '@/utils/httpRequestApi';

export default {
  props: {
    type: {
      type: String,
      default: window.globalConfig.useIconfont ? "icon" : "fa"   // fa: fontawesome, el: el-icon, icon: iconfont
    },
    iconList: {
      type: Array
    },
    value: {
      type: String
    },
    disabled: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      visible: false,
      field: null,
      useIconfont: window.globalConfig.useIconfont,
      iconfontOption: [],
      searchStr: '',
      faIconsUrl: "https://fontawesome.com.cn/v4/icons",
      elIconsUrl: "https://element.eleme.cn/#/zh-CN/component/icon",
      iconfontUrl: "iconfont/demo_index.html",
    };
  },
  computed: {
    finalIconList() {
      if (this.iconList) return this.iconList;
      switch (this.type) {
        case "fa":
          return MENU_ICON_LIST;
        case "el":
          return BTN_ICON_LIST;
        case "icon":
          const reg = new RegExp(this.searchStr, 'i')
          return this.iconfontOption.filter(item => this.getTitle(item).search(reg) !== -1);
      }
    },
    moreUrl() {
      switch (this.type) {
        case "fa":
          return this.faIconsUrl;
        case "el":
          return this.elIconsUrl;
        case "icon":
          return this.iconfontUrl;
      }
    },
  },
  watch: {
    value: {
      handler(val) {
        this.field = val;
      },
      immediate: true
    },
    field(val) {
      this.$emit("input", val);
    }
  },
  methods: {
    onSelect(item) {
      this.visible = false;
      this.field = item;
    },
    getValue(item) {
      return this.type === 'icon' ? 'icon-' + item.font_class : item;
    },
    getTitle(item) {
      return this.type === 'icon' ? item.name + '\n' + this.getValue(item) : item;
    }
  },
  created() {
    if (this.useIconfont && this.type === 'icon') {
      httpRequestApi({
        url: "iconfont/iconfont.json",
        baseURL: "",
      }).then(json => {
        this.iconfontOption = json.glyphs;
      })
    }
  }
};
</script>

<style lang="scss">
.icon-select-popover {
  max-width: 330px;

  &.iconfont {
    max-width: 45%;
  }
}

.icon-select-list {
  max-height: 310px;
  padding: 0;
  margin: -8px 0 0 -8px;
  overflow: auto;

  >.el-button {
    padding: 8px;
    margin: 8px 0 0 8px !important;

    &.is-active {
      border-color: var(--primary-active-border) !important;
    }

    i,
    svg {
      display: inline-block;
      vertical-align: middle;
      width: 18px;
      height: 18px;
      font-size: 18px;
    }
  }
}
</style>
