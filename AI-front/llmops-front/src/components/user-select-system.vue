<template>
  <el-popover
    popper-class="user-select"
    placement="bottom"
    :offset="15"
    :width="width"
    v-model="show"
    @show="onShow"
    @after-enter="onEnter"
  >
    <div slot="reference">
      <el-tooltip
        :disabled="text.length <= 20"
        class="item"
        effect="dark"
        :content="text"
        placement="top"
      >
        <el-input :size="size" placeholder="请选择" readonly :value="text">
          <el-link
            slot="suffix"
            :underline="false"
            :disabled="!text"
            @click.stop="clear"
          >
            <i class="el-icon-close el-input__icon"></i>
          </el-link>
        </el-input>
      </el-tooltip>
    </div>
    <div class="box row oh" :style="{ height }">
      <div class="f1 p10 col oh">
        <div>
          <el-input
            ref="search"
            placeholder="搜索成员"
            prefix-icon="el-icon-search"
            :validate-event="false"
            clearable
            v-model="filterText"
          />
        </div>
        <div class="f1 mt10" style="overflow-y: auto;">
          <el-tree
            v-if="show"
            ref="tree"
            :data="treeData"
            node-key="nodeKey"
            :filter-node-method="filterNode"
            @node-click="handleRowClick"
          >
            <template v-slot="{ data }">
              <div v-if="!data.isUser" class="row ac">
                <el-link
                  v-if="!single"
                  :underline="false"
                  class="mr10"
                  @click.stop="addFolder(data)"
                  ><i class="el-icon-plus" style="font-size: 14px;"></i
                ></el-link>
                <img
                  class="mr10"
                  style="width: 20px;"
                  src="@/assets/images/icon_file.png"
                />
                <span>{{ data.name }}</span>
              </div>
              <div v-else class="row ac">
                <img
                  class="mr10"
                  style="width: 20px;border-radius: 50%;"
                  :src="data.avatar || AvatarImg"
                />
                <span>{{ data.name }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </div>
      <div class="f1 p10 col oh">
        <div class="row ac">
          <span class="f1">已选（{{ selectedUsers.length }}）</span>
          <el-link :underline="false" @click="clearUser">清空</el-link>
          <el-link :underline="false" @click="confirm">确认</el-link>
        </div>
        <ul class="f1 mt10" style="overflow-y: auto;">
          <li
            v-for="(item, index) in selectedUsers"
            :key="item.id"
            class="selected-item row ac"
          >
            <img
              class="mr10"
              style="width: 20px;border-radius: 50%;"
              :src="item.avatar || AvatarImg"
            />
            <span class="f1">{{ item.realName }}</span>
            <el-link :underline="false"
              ><i class="el-icon-close" @click="removeEle(index)"></i
            ></el-link>
          </li>
        </ul>
      </div>
    </div>
  </el-popover>
</template>

<script>
import { debounce, cloneDeep, flatMap } from "lodash";
import api_user from "@/api/sys/user";
import api_org from "@/api/sys/org";
import AvatarImg from "@/assets/images/avatar.png";
export default {
  name: "user-select-system",
  props: {
    value: {
      type: [Array, String],
      default: () => []
    },
    single: {
      type: Boolean,
      default: false
    },
    width: {
      type: [String, Number],
      default: 600
    },
    height: {
      type: [String, Number],
      default: "400px"
    },
    size: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      AvatarImg,
      loading: false,
      show: false,
      filterText: "",
      userKeys: [],
      orgList: [],
      userList: [],
      treeData: []
    };
  },
  computed: {
    userMap({ userList }) {
      return userList.reduce((prev, val) => {
        if (val) {
          prev[val.userName] = val;
        }
        return prev;
      }, {});
    },
    text({ value, userMap }) {
      if (this.single) {
        if (!value || Array.isArray(value)) return "";
        else return (userMap[value] || {}).realName || value;
      } else {
        return value.map(v => (userMap[v] || {}).realName || "").toString();
      }
    },
    selectedUsers({ userKeys, userMap }) {
      return userKeys.map(v => {
        return userMap[v] || { id: v, userName: v, realName: v };
      });
    }
  },
  watch: {
    filterText: debounce(function(val) {
      this.$refs.tree.filter(val);
    }, 300)
  },
  created() {
    this.fetchData();
  },
  methods: {
    // 查询用户与部门数据
    fetchData() {
      this.loading = true;
      Promise.all([
        api_org.getTree().catch(() => null),
        api_user.getList({ maxResult: 9999 }).catch(() => null)
      ])
        .then(res => {
          this.orgList = res[0] || [];
          this.userList = (res[1] && res[1].resultData) || [];
        })
        .finally(() => {
          this.loading = false;
          this.updateTreeData();
        });
    },
    // 合并组织结构与用户列表，生成tree组件数据
    updateTreeData() {
      const treeData = cloneDeep(this.orgList);
      const tree2list = arr => {
        return flatMap(arr, item => {
          const children =
            item && item.children ? tree2list(item.children) : [];
          return [item, ...children];
        });
      };
      const flatOrgList = tree2list(treeData);
      flatOrgList.forEach(org => {
        org.nodeKey = `node-${org.id}`;
      });
      this.userList.forEach(user => {
        const org = user.org
          ? flatOrgList.find(item => item.id === user.org.id)
          : null;
        const treeUser = {
          ...user,
          nodeKey: `user-${user.id}`,
          name: user.realName,
          isUser: true
        };
        if (org && org.children) {
          org.children.push(treeUser);
        } else if (org) {
          org.children = [treeUser];
        }
      });
      this.treeData = treeData;
    },
    filterNode(value, data) {
      if (!value) return true;
      return (
        (data.name && data.name.includes(value)) ||
        (data.userName && data.userName.includes(value))
      );
    },
    clear() {
      if (this.single) this.$emit("input", "");
      else this.$emit("input", []);
      if (this.single) this.$emit("change", "");
      else this.$emit("change", []);
    },
    clearUser() {
      this.userKeys = [];
    },
    confirm() {
      this.show = false;
      if (this.single) this.$emit("input", this.userKeys[0] || "");
      else this.$emit("input", this.userKeys);
      if (this.single) this.$emit("change", this.userKeys[0] || "");
      else this.$emit("change", this.userKeys);
    },
    removeEle(index) {
      this.userKeys.splice(index, 1);
    },
    handleRowClick(data, node) {
      if (node.data.isUser) {
        if (this.single) {
          this.userKeys = [data.userName];
        } else if (!this.userKeys.includes(data.userName)) {
          this.userKeys.push(data.userName);
        }
      }
    },
    addFolder(data) {
      const user = this.getFolderUser(data.children || []);
      this.userKeys = Array.from(
        new Set([...this.userKeys, ...user.map(v => v.userName)])
      );
    },
    getFolderUser(data) {
      let user = [];
      data.forEach(d => {
        if (d.isUser) {
          user.push(d);
        } else {
          user = user.concat(this.getFolderUser(d.children || []));
        }
      });
      return user;
    },
    onShow() {
      this.filterText = "";
      if (this.single) {
        if (typeof this.value === "string" && this.value.length) {
          this.userKeys = [this.value];
        } else this.userKeys = [];
      } else this.userKeys = [...this.value];
      this.user = this.userKeys.map(v => this.userMap[v]);
    },
    onEnter() {
      if (this.$refs.search) {
        this.$refs.search.focus();
      }
    }
  }
};
</script>

<style lang="scss" scoped>
// *,
// :after,
// :before {
//   box-sizing: border-box;
// }
.row {
  display: flex;
  flex-direction: row;
}
.col {
  display: flex;
  flex-direction: column;
}
.ac {
  align-items: center;
}
.jc {
  justify-content: center;
}
.f1 {
  flex: 1;
}
.user-select {
  .box {
    > .f1 + .f1 {
      border-left: 1px solid #e1e1e1;
    }
  }
  .oh {
    overflow: hidden;
  }
  .selected-item {
    height: 32px;
    padding: 0 10px;
    &:hover {
      background-color: var(--sub-hover-bg);
    }
  }
}
</style>
