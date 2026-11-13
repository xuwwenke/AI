<script>
import { menuAction } from "@/router";
import ProjectSelect from "./site-top/project-select";

export default {
  props: {
    isMain: {
      type: Boolean,
      default: false,
    },
    isMainInIframe: {
      type: Boolean,
      default: false,
    },
    // 参数具体用法参考 src/main.vue 中的代码和注释
    setting: {
      type: Object,
      default: () => ({})
    },
  },
  data() {
    return {
      globalConfig: window.globalConfig,
      menuAction,
      inIframe: self != top,
    };
  },
  components: {
    ProjectSelect
  },
  computed: {
    menu() {
      return this.$route.meta.menu;
    },
  },
  mounted() {
    // console.log("面包屑", this.$route.meta.menu);
  },
  methods: {
    hasAction(m, index) {
      // 若面包屑节点 url 存在，且与当前 fullPath 不同，则可点击
      if (m.url && !/^(http[s]?:)?\/\/.*/.test(m.url) && m.url != this.$route.fullPath) return true;

      // 全局控制，开启后面包屑最后一个节点总是可点击
      if (window.globalConfig.refreshContentByMenuAction && index == this.menu.breadcrumb.length - 1) return true

      return false;
    },
    goBack() {
      if (this.setting.back === true) {
        this.$router.back();
      } else {
        this.setting.back();
      }

      this.setting.back = null;
    },
    breadcrumbClick(m) {
      if (!m.hidden) this.menuAction(m, true);  // 如果是隐藏菜单（下钻页），则只刷新不跳转，以免url参数被清空（详情页经常用url参数来获取数据）
      this.$emit('updateContent');
    }
  },
  render() {
    if (window.globalConfig.useMainPageTitle && !this.isMain && !this.isMainInIframe) return null;
    if (this.setting && this.setting.hidden) return null;

    const getIconName = (m) => (
      <span>
        <Icon class="mr5" icon={m.icon} />
        {m.name}
      </span>
    );

    const slotItems =
      this.setting && this.setting.slotItems && this.setting.slotItems.length > 0
        ? this.setting.slotItems.map((item) => (
          <el-breadcrumb-item>
            {item.click ? (
              <a style="cursor: pointer" onClick={item.click}>{getIconName(item)}</a>
            ) : (
              getIconName(item)
            )}
          </el-breadcrumb-item>
        ))
        : null;

    const slotTools =
      this.setting && this.setting.slotTools
        ? this.setting.slotTools.map((item) => (
          <span>
            {item.click ? (
              <a onClick={item.click} class="mr10">{getIconName(item)}</a>
            ) : (
              getIconName(item)
            )}
          </span>
        ))
        : null;

    return !this.inIframe || this.globalConfig.showContentPageTitleInIframe ? (
      <h1 class={{ "page-title": true, 'is-main': this.isMain }}
        style={this.isMain ? `padding: 0 ${window.globalConfig.mainPadding}px` : undefined}>
        <el-breadcrumb separator="/">
          {this.menu ? (
            this.globalConfig.pageTitleBreadcrumb ? (
              this.menu.breadcrumb.map((m, index) => {
                return (
                  <el-breadcrumb-item>
                    {this.hasAction(m, index) || (slotItems && index == this.menu.breadcrumb.length - 1) ? (
                      <a style="cursor: pointer" onClick={() => this.breadcrumbClick(m)}>
                        {getIconName(m)}
                      </a>
                    ) : (
                      getIconName(m)
                    )}
                  </el-breadcrumb-item>
                );
              })
            ) : (
              <el-breadcrumb-item>{getIconName(this.menu)}</el-breadcrumb-item>
            )
          ) : null}
          {slotItems}
        </el-breadcrumb>
        <span class="tools">
          <ProjectSelect />
          {slotTools}
          {this.setting.back ? (
            <a class="btn-goback" onClick={this.goBack}>
              <i class="fa fa-arrow-circle-left"></i> 返回
            </a>
          ) : null}
        </span>
        {!this.isMain && <hr class="mt5" />}
      </h1>
    ) : null;
  },
};
</script>

<style lang="scss" scoped>
.page-title {
  line-height: 1;
  font-size: 14px;
  background-color: #fff;
  &.is-main {
    height: 40px;
    line-height: 39px;
    border-bottom: 1px solid #d9dfe6;
    .el-breadcrumb {
      line-height: 39px;
    }
  }

  .el-breadcrumb {
    display: inline-block;
    line-height: 1;
    font-weight: normal;
  }

  .tools {
    height: 40px;
    float: right;
    display: flex;
    align-items: center;
    cursor: pointer;
  }
}
</style>
