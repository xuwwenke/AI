<template>
  <div class="quick-entry">
    <h3 class="qe-title">
      <em class="qet-title"><img :src="entryPic" width="20" height="20" alt="" />快捷入口</em>
      <!-- <button class="qet-edit" type="button">
				<i class="cssicon cssicon-edit"></i>
				<span>编辑</span>
			</button> -->
    </h3>
    <div v-if="noFav" class="tac p20">
      暂无快捷入口，请点击左侧菜单的收藏按钮进行添加
    </div>
    <template v-else v-for="(root, index) in appList">
      <dl :key="index" v-if="root.data.length > 0">
        <dt>
          <!-- <i class="cssicon cssicon-edit"></i> -->
          <img v-if="root.icon && /\/|\./.test(root.icon)" :src="root.icon" alt=""
            class="cssicon" />
          <i v-else :class="'fa fa-' + root.icon"></i>
          <span>{{ root.rootName }}（{{ root.data.length }}）</span>
        </dt>
        <dd>
          <quick-entry-group :root="root" @dragEnd="dragEnd" @setSystem="$emit('setSystem', $event)"
            @refreshFav="$emit('refreshFav', $event)" @close="$emit('close')" />
        </dd>
      </dl>
    </template>
  </div>
</template>

<script>
import debounce from 'lodash/debounce';
import api from '@/api/compass';
import QuickEntryGroup from './QuickEntryGroup';

export default {
  name: 'quick-entry',
  components: {
    QuickEntryGroup
  },
  props: {
    appData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      entryPic: require('./public/img/u160.png')
    };
  },
  computed: {
    appList() {
      return this.appData;
    },
    noFav() {
      return !this.appList.find(app => app.data.length > 0);
    }
  },
  methods: {
    dragEnd: debounce(function(evt) {
      var tmp = [];
      this.appList.forEach(app => {
        app.data.forEach(dt => {
          tmp.push(dt.id);
        });
      });
      var idStr = tmp.join(',');
      this.toSave(idStr);
    }, 2000),
    toSave(str) {
      let params = {};
      params.menuIds = str;
      params.userName = localStorage.getItem('userName', true);
      api.seqFavorite(params).then(res => {
        console.log('排序成功');
      });
    }
  }
};
</script>

<style scoped lang="scss">
@import 'variable.scss';
.quick-entry {
  border: 1px solid $color-gray1;
  background-color: #ffffff;
  border-radius: 5px;
  .qe-title {
    display: flex;
    justify-content: space-between;
    background: $color-gray1;
    // padding: 10px 5px;
    padding: 7px 5px 13px;
    color: #333333;
    border-bottom: 1px solid $boder-color1;
    .qet-title {
      font-size: 18px;
      font-style: normal;
      img {
        margin: 0 $distance-15;
        position: relative;
        top: 3px;
      }
    }
    .qet-edit {
      display: inline-flex;
      align-items: center;
      cursor: pointer;
      .cssicon-edit {
        margin-right: 4px;
      }
    }
  }
  dl {
    margin-left: $distance-15 * 2;
    margin-right: $distance-15 * 2;
    border-bottom: 1px solid $boder-color1;
    &:last-child {
      border: none;
    }
    dt {
      font-size: 18px;
      font-weight: 400;
      margin: $distance-15 * 2 0 $distance-15;
      .cssicon {
        // position: absolute;
        // margin-left: - $distance-15 * 2;
        margin-right: 10px;
        position: relative;
        bottom: 2px;
      }
      i {
        margin-right: 10px;
      }
    }
    dd {
      margin: $distance-15 0 $distance-15 $distance-15 * 2;
    }
  }
}
</style>
