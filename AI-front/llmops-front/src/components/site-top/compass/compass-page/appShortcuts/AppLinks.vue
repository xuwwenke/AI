<template>
  <div>
    <draggable tag="div" :list="appList" :options="dragOptions" ref="appLinks" class="app-links"
      :class="{ unfolded: isUnfolded }" @end="dragEnd">
      <transition-group type="transition" :name="'flip-list'">
        <a ref="oneLink" href="javascript:;" draggable="true" v-for="item in appList" :key="item.id"
          class="draggable" :class="{ isFilled: item.isFilled }" :title="item.name"
          @click="$emit('setSystem', item)">
          <img v-if="item.icon && /\/|\./.test(item.icon)" :src="item.icon" alt="" />
          <i v-else :class="'fa fa-' + item.icon"></i>
          <span>{{ item.name }}</span>
        </a>
      </transition-group>
    </draggable>
    <div class="app-more" @click="toggleMore" v-if="isShowMore">
      <span>{{ isUnfolded ? "收起" : "更多"
        }}<i class="cssicon"
          :class="isUnfolded ? 'cssicon-arrow-up' : 'cssicon-arrow-down'"></i></span>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable';
import debounce from 'lodash/debounce';
import api from '@/api/compass';
import { mapMutations } from 'vuex';
import cloneDeep from "lodash/cloneDeep";

export default {
  name: 'app-links',
  components: {
    draggable
  },
  props: {
    appData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dragOptions: {
        animation: 500,
        ghostClass: 'ghostClass'
      },
      isShowMore: false,
      isUnfolded: false
    };
  },
  computed: {
    appList() {
      return this.appData;
    }
  },
  mounted() {
    this.checkNeedMoreBtn();
    window.addEventListener('resize', this.checkNeedMoreBtn);
  },
  methods: {
    ...mapMutations('apps', ['setList']),
    checkNeedMoreBtn() {
      const oneW = this.$refs.oneLink[0] && this.$refs.oneLink[0].clientWidth;
      this.isShowMore =
        this.appData.length > Math.floor(this.$el.clientWidth / oneW) * 2;
    },
    toggleMore() {
      this.isUnfolded = !this.isUnfolded;
    },
    dragEnd: debounce(function(evt) {
      if (evt.newIndex === evt.oldIndex) {
        return;
      }
      // 获取排序后的数据
      console.log('dragEnd', this.appList);
      var tmp = [];
      this.appList.forEach(app => {
        tmp.push(app.key);
      });
      var idStr = tmp.join(',');
      this.toSave(idStr);

      let list = cloneDeep(this.appList)
      list.forEach((app, index) => {
        app.compSeq = index + 1
        delete app.temp
      })
      this.setList(list);
    }, 2000),
    toSave(str) {
      let params = {};
      params.keys = str;
      params.userName = localStorage.getItem('userName', true);
      api.seqApps(params).then(res => {
        console.log('排序成功');
        // this.$emit('refreshApp', true)
      });
    }
  }
};
</script>

<style scoped lang="scss">
@import 'variable.scss';
.app-links {
  max-height: 180px;
  overflow-y: hidden;
  // transition: all 0.5s;
  &.unfolded {
    max-height: none;
  }
  > span {
    // display: inline-flex;
    display: table-cell;
    flex-wrap: wrap;
    justify-content: space-between;
  }
  a {
    // display: inline-flex;
    display: inline-block;
    flex-direction: column;
    align-items: center;
    padding: $distance-15 22px;
    text-align: center;
    &:hover {
      background-color: #eee;
      color: #333;
    }
    &.isFilled {
      cursor: text;
      opacity: 0;
      pointer-events: none;
    }
    img {
      display: inline-block;
      height: 40px;
      margin-bottom: 6px;
    }
    i {
      display: block;
      height: 40px;
      margin-top: 6px;
      font-size: 30px;
    }
    span {
      display: block;
      width: 9em;
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap;
    }
  }
}
// .flip-list-move {
//   transition: transform 0.5s;
// }
.app-more {
  cursor: pointer;
  // line-height: 1;
  color: $color-theme;
  width: 80px;
  margin: $distance-15 auto 0;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 4px;
  background-color: #e8f2fe;
  .cssicon-arrow-down {
    font-size: 12px;
    margin-left: 10px;
  }
  .cssicon-arrow-up {
    font-size: 12px;
    margin-left: 10px;
  }
}
</style>
