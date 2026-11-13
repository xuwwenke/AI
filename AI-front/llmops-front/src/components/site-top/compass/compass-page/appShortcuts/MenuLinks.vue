<!-- 数字输入正则限制框 -->
<template>
  <div class="menu-links">
    <template v-for="(item, index) in appList">
      <div :key="index" v-if="item.temp.length > 0" class="links-classify">
        <h6 class="links-title">
          <span :title="item.name">{{ item.name }}</span>
        </h6>
        <div ref="linksName" class="links-name">
          <a href="javascript:;" v-for="(menu, index2) in item.temp" :key="index2"
            @click="pageLink(menu, item)" @mouseover="over(menu)" @mouseleave="leave(menu)">
            <el-tooltip class="item" popper-class="compass-pop" effect="light" placement="top">
              <div slot="content">
                <span v-html="hightLightCrumb(menu.crumb, item.inputVal)"></span>
              </div>
              <span class="name">{{ menu.name }}</span>
            </el-tooltip>
            <span class="icons">
              <span class="header-span">
                <span v-show="menu.pName === 'new'" class="header-tag">NEW</span>
                <span v-show="menu.pName === 'hot'" class="header-tag">HOT</span>
              </span>

              <span class="header-img">
                <img v-if="menu.isShowFav === true && menu.favorite === 1" :src="getImgUrl('fav')"
                  title="取消收藏" @click.stop="cancel(menu, item.id)" />
                <img v-if="menu.isShowFav === true && menu.favorite === 0"
                  class="icon-favarite-hover" :src="getImgUrl('unFav')" title="收藏"
                  @click.stop="fav(menu, item.id)" />
              </span>
            </span>
          </a>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import api from '@/api/compass';
import { replaceAll } from '@/utils/base';
import { menuAction } from '@/router';

export default {
  name: 'menu-links',
  props: {
    appData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {};
  },
  computed: {
    appList() {
      return this.appData;
    }
  },
  created() {},
  mounted() {
    try {
      const linksName = this.$refs.linksName;
      const total_w = Number.parseFloat(
        window.getComputedStyle(linksName[0]).width
      );
      const a_w = 168;
      const rowNumber = Math.floor(total_w / a_w);
      for (let i = 0; i < this.appList.length; i++) {
        const menus = this.appList[i].menus;
        const remainder = menus.length % rowNumber;
        if (remainder === 0) {
          continue;
        }
        const resultCount = rowNumber - remainder;
        for (let j = 0; j < resultCount; j++) {
          menus.push({
            name: '',
            iconUrl: '',
            isFilled: true
          });
        }
      }
    } catch (error) {}
  },
  methods: {
    fav(row, id) {
      let params = {};
      params.userName = localStorage.getItem('userName', true);
      params.menuId = row.id;
      if (row.favoriteSeq !== null) {
        params.favoriteSeq = row.favoriteSeq;
      }
      api.handeFavorite(params).then(res => {
        this.$set(row, 'favorite', 1);
        this.$emit('refreshFav', row, id);
      });
    },
    cancel(row, id) {
      let params = {};
      params.userName = localStorage.getItem('userName', true);
      params.menuId = row.id;
      api.cancelFavorite(params).then(res => {
        this.$set(row, 'favorite', 0);
        this.$emit('refreshFav', row, id);
      });
    },
    over(row) {
      this.$set(row, 'isShowFav', true);
    },
    leave(row) {
      this.$set(row, 'isShowFav', false);
      this.clearToolTip();
    },
    // 清除tooltip的dom实例
    clearToolTip() {
      var domList = document.getElementsByClassName('el-tooltip__popper');
      var list = Array.from(domList);
      if (list.length) {
        list.forEach(dom => {
          dom.remove();
        });
      }
    },
    pageLink(menu, app) {
      menuAction(menu);
      this.$emit('setSystem', app);
    },
    getImgUrl(type) {
      if (type === 'fav') {
        return require('./public/img/u158.png');
      } else if (type === 'unFav') {
        return require('./public/img/u157.png');
      }
    },
    hightLightCrumb(crumb, inputV) {
      var newCrumb = '';
      try {
        if (inputV) {
          newCrumb = replaceAll(
            crumb,
            inputV,
            '<span style="font-weight: bold;">' + inputV + '</span>'
          );
        } else {
          newCrumb = crumb;
        }
      } catch (error) {
        newCrumb = crumb;
      }
      return newCrumb;
    }
  }
};
</script>

<style scoped lang="scss">
@import 'variable.scss';
.menu-links {
  padding-left: $distance-15;
  .links-classify {
    line-height: 1;
    margin: 2em 0;
    .links-title {
      position: absolute;
      width: 140px;
      // font-size: 1.2em;
      font-size: 16px;
      font-weight: bold;
      color: $color-theme;
      margin-top: 6px;
      // white-space: nowrap;
      overflow: hidden;
      // text-overflow: ellipsis;
      line-height: 1.2;
      padding-right: 1em;
      box-sizing: border-box;
    }
    .links-name {
      //display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      margin-left: 140px;
      a {
        display: inline-block;
        // width: 15em;
        width: 16em;
        padding: 0 1em;
        box-sizing: border-box;
        .name {
          display: inline-block;
          // width: 7.5em;
          max-width: 7.5em;
          margin-right: $distance-15;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          padding: 8px 0;
          border-bottom: 1px solid transparent;
          &:hover {
            border-bottom: 1px solid $color-theme;
            color: $color-theme;
          }
        }
        .icons {
          position: absolute;
          display: inline-flex;
          img {
            height: 20px;
            margin-left: 4px;
            margin-top: 6px;
          }
          .icon-favarite-hover {
            opacity: 1;
            transition: opacity 0.2s ease-in;
          }
          .header-span {
            padding-top: 8px;
            .header-tag {
              background-color: #fa4642;
              padding: 2px 4px 2px 4px;
              color: #fff;
              font-size: 12px;
              border-radius: 2px;
            }
          }
        }
        &:hover {
          // background-color: #eee;
          color: #333;
          .icons {
            .icon-favarite-hover {
              opacity: 1;
            }
          }
        }
      }
    }
  }
}
</style>
