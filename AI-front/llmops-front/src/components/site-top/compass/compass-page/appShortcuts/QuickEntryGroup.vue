<template>
  <draggable
    v-model="sortArray"
    class="quickEntryGroup"
    :options="dragOptions"
    @end="dragEnd"
  >
    <transition-group type="transition" :name="'flip-list'">
      <a href="javascript:;" v-for="menu in sortArray" :key="menu.id">
        <el-tooltip
          class="item"
          popper-class="compass-pop"
          effect="light"
          :content="menu.crumb"
          placement="top"
        >
          <span class="name" @click="quickEntry(menu, root.app)">{{
            menu.name
          }}</span>
        </el-tooltip>
        <el-dropdown
          trigger="click"
          @command="handleCommand"
          placement="bottom-end"
        >
          <span class="el-dropdown-link">
            <i class="el-icon-more" title="更多"></i>
          </span>
          <el-dropdown-menu slot="dropdown" class="compass-pop">
            <el-dropdown-item
              icon="el-icon-delete"
              :command="composeValue(menu, root.app.id)"
              title="取消收藏"
            >
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </a>
    </transition-group>
  </draggable>
</template>

<script>
import draggable from "vuedraggable";
import api from "@/api/compass";
import { menuAction } from "@/router";

export default {
  components: {
    draggable,
  },
  props: {
    root: {
      type: Object,
      default: () => ({ data: [] }),
    },
  },
  data() {
    return {
      dragOptions: {
        animation: 500,
        ghostClass: "ghostClass",
      },
    };
  },
  computed: {
    sortArray: {
      get() {
        return this.root.data;
      },
      set(v) {
        console.log(v);
        this.root.data = v;
      },
    },
  },
  methods: {
    dragEnd(evt) {
      this.$emit('dragEnd');
    },
    toSave(str) {
      let params = {};
      params.menuIds = str;
      params.userName = localStorage.getItem("userName", true);
      api.seqFavorite(params).then((res) => {
        console.log("排序成功");
      });
    },
    quickEntry(menu, item) {
      menuAction(menu);
      this.$emit('setSystem', item);

      this.$emit("close");
    },
    handleCommand(pms) {
      let params = {};
      params.userName = localStorage.getItem("userName", true);
      params.menuId = pms.para1.id;
      api.cancelFavorite(params).then((res) => {
        pms.para1.favorite = 0;
        this.$emit("refreshFav", pms.para1, pms.para2);
      });
    },
    // command 参数扩展
    composeValue(item, parentId) {
      return {
        para1: item,
        para2: parentId,
      };
    },
  },
};
</script>

<style lang="scss" scoped>
@import "variable.scss";
.quickEntryGroup {
  a {
    display: inline-block;
    width: 12em;
    padding-right: 1em;
    box-sizing: border-box;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    line-height: 2;
    i {
      display: none;
      margin-left: 22px;
      transform: rotate(90deg);
      color: $color-theme;
    }
    .el-dropdown-link {
      display: inline-block;
      width: 4em;
    }
    &:hover {
      color: $color-theme;
      i {
        display: inline-block;
      }
    }
  }
}
</style>
