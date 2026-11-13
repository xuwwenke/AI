<script>
export default {
  name: 'MenuPannel',
  props: {
    menu: {
      type: Array,
      default: function() {
        return [];
      }
    },
    currentMenu: {
      type: [Number, String],
      default: ''
    },
    hotSearch: {
      type: Array,
      default() {
        return [];
      }
    },
    showNewFunc: Function,
    showHotFunc: Function
  },
  data() {
    return {
      searchKey: '',
      menuList: []
    };
  },
  computed: {
    menus: function() {
      let newMenus = [];
      let _self = this;
      this.menu.forEach(ele => {
        if (ele.children) {
          let children = [];
          ele.children.forEach(element => {
            if (element.name.indexOf(_self.searchKey) > -1) {
              children.push(element);
            }
          });
          if (children.length > 0) {
            newMenus.push(Object.assign({}, ele, { children: children }));
          }
        } else {
          if (ele.name.indexOf(_self.searchKey) > -1) {
            newMenus.push(Object.assign({}, ele, { children: null }));
          }
        }
      });
      return newMenus;
    }
  },
  mounted() {},
  methods: {
    click(evt, el) {
      this.$emit('click', { evt, el });
    },
    menuMouseEnter(el) {
      this.$emit('menuMouseEnter', el);
    },
    showNew(el) {
      let flag = this.showNewFunc(el);
      return flag ? 'display:inline;' : 'display:none;';
    },
    showHot(el) {
      let flag = this.showHotFunc(el);
      return flag ? 'display:inline;' : 'display:none;';
    }
  },
  render(h) {
    if (this.menu.length === 0) {
      return null;
    }

    const renderExtra = el => {
      return (
        <span style="display: inherit">
          <span style={this.showHot(el)} class="header-tag">
            HOT
          </span>
          <span style={this.showNew(el)} class="header-tag">
            NEW
          </span>
        </span>
      );
    };

    let menuList = [];
    let _self = this;
    const renderMenus = function(h, list) {
      list.forEach(el => {
        if (el.children) {
          const li = [];
          el.children.forEach(c => {
            let liClazz = 'menu-li';
            if (c.children) {
              liClazz += ' normal-li';
            } else if (c.ids.indexOf(_self.currentMenu) > -1) {
              liClazz += ' active';
            }
            let cChild = [];

            if (c.children) {
              c.children &&
                c.children.forEach(cc => {
                  cChild.push(
                    <li
                      onclick={e => {
                        e.stopPropagation();
                        _self.click(e, cc);
                      }}
                      class={cc.id === _self.currentMenu ? 'active' : ''}
                    >
                      <span title={cc.name} class="title">
                        {cc.name}
                      </span>
                      {renderExtra(cc)}
                    </li>
                  );
                });
            }
            li.push(
              <div class="li-wrapper">
                <div
                  class={liClazz}
                  onclick={e => {
                    e.stopPropagation();
                    _self.click(e, c);
                  }}
                  onmouseenter={e => {
                    _self.menuMouseEnter(el);
                  }}
                >
                  <span title={c.name} class="title">
                    {c.name}
                  </span>
                  {renderExtra(c)}
                </div>
                {cChild.length ? (
                  <div>
                    <div class="triangle"></div>
                    <div class="triangle-border"></div>
                    <ul class="li-box">{cChild}</ul>
                  </div>
                ) : (
                  ''
                )}
              </div>
            );
          });
          menuList.push(
            <ul>
              <h2 style="">
                <Icon class="menu-icon mr5" icon={el.icon} />
                <span class="third-menu" title={el.name}>
                  {el.name}
                </span>
                {renderExtra(el)}
              </h2>

              {li}
            </ul>
          );
        } else {
          let liClazz = 'menu-li';
          if (el.ids.indexOf(_self.currentMenu) > -1) {
            liClazz += ' active';
          }
          menuList.push(
            <ul>
              <li
                class={liClazz}
                onclick={e => {
                  e.stopPropagation();
                  _self.click(e, el);
                }}
                onmouseenter={() => _self.menuMouseEnter(el)}
              >
                <span>{el.name}</span>
                {renderExtra(el)}
              </li>
            </ul>
          );
        }
      });
    };
    renderMenus(h, this.menus);

    let quickTagsList = [];
    const renderTags = function(h, list) {
      list &&
        list.forEach(el => {
          quickTagsList.push(
            <el-tag
              type=""
              effect="plain"
              size="medium"
              class="tag"
              disable-transitions
              onclick={e => {
                e.stopPropagation();
                _self.click(e, el);
              }}
            >
              {el.name}
            </el-tag>
          );
        });
    };
    renderTags(h, this.hotSearch);

    let main = (
      <div class="search-menu">
        <div class="search-menu-header">
          <el-input
            placeholder="搜索菜单"
            v-model={this.searchKey}
            style="width: 300px"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              type="primary"
            ></el-button>
          </el-input>
          <span class="search-menu-tag">{quickTagsList}</span>
        </div>
        <div class="search-menu-body">{menuList}</div>
      </div>
    );
    return main;
  }
};
</script>

<style lang="scss" scoped>
.hot-new-img {
  margin-left: 5px;
  vertical-align: super;
}
</style>
<style lang="scss">
.third-menu {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;

  vertical-align: bottom;
}
.search-menu {
  background-color: var(--site-sub-menu2-bg);
  padding: 10px;
  width: 750px;
  height: 100%;
  box-sizing: border-box;
}
.search-menu-header {
  box-sizing: border-box;
  height: 32px;
  margin: 10px;
  overflow: hidden;
}
.search-menu-tag {
  .tag {
    margin-left: 10px;
    border-radius: 20px;
    cursor: pointer;
  }
}
.search-menu-body {
  display: inline-flex;
  flex-wrap: wrap;
  overflow: auto;
  width: 100%;
  max-height: calc(100% - 64px);
  ul {
    padding-top: 10px;
    box-sizing: border-box;
    width: 240px;
    flex: none;

    & > h2 {
      margin: 0 16px 10px;
      font-size: 14px;
      line-height: 32px;
      border-bottom: 1px solid #c7c7c7;
      display: inline-flex;
      align-items: center;
      width: calc(100% - 30px);
    }
    .li-wrapper {
      position: relative;
      margin: 0 16px;
      .menu-li {
        padding: 8px 0;
        &.normal-li {
          cursor: auto !important;
          &:hover {
            color: inherit !important;
          }
        }
      }
      .title {
        max-width: 100%;
        display: inline-block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .triangle {
        width: 0px;
        height: 0px;
        border-top: 8px solid transparent;
        border-left: 8px solid transparent;
        border-right: 8px solid transparent;
        border-bottom: 8px solid #fcfcfc;
        position: absolute;
        top: 22px;
        left: calc(15% + 0px);
        z-index: 999;
      }
      .triangle-border {
        width: 0px;
        height: 0px;
        border-top: 8px solid transparent;
        border-left: 8px solid transparent;
        border-right: 8px solid transparent;
        border-bottom: 8px solid #d8d8d8;
        position: absolute;
        top: 21px;
        left: 15%;
        z-index: 99;
      }
      .li-box {
        width: 100%;
        margin: 2px 0 5px;
        padding: 10px 15px;
        background-color: var(--pop-bg);
        color: var(--pop-txt);
        box-shadow: 0 5px 8px 0 rgba(209, 212, 221, 0.42);
        border: solid 1px #d8d8d8;
        font-size: 14px;
        cursor: pointer;
        > li {
          width: 100%;
          line-height: 25px;
          display: inline-flex;
          align-items: center;
          transition: color 0.3s;
          &:hover,
          &.hover {
            // background: var(--site-menu-hover-bg) !important;
            color: var(--default-hover-txt) !important;
          }
          &.active {
            color: var(--pop-active-txt) !important;
          }
        }
      }
    }
  }
}
</style>
