<template>
  <el-dialog
    :title="'修改用户(' + selectedCount + ')'"
    :close-on-click-modal="false"
    :visible.sync="visible"
  >
    <div v-loading="loading || submitLoading">
      <el-input
        class="mb10"
        placeholder="输入姓名进行过滤"
        v-model="filterText"
        clearable
        @clear="searchTree()"
      >
      </el-input>
      <el-tree
        ref="tree"
        node-key="nodeKey"
        :data="treeData"
        :props="defaultProps"
        :filter-node-method="filterNode"
        show-checkbox
        @check-change="onSelectionChange"
      >
        <template v-slot="{ data }">
          <div class="row ac">
            <img
              style="width: 20px;border-radius: 50%;"
              :src="
                data.avatar ? data.avatar : data.isUser ? ImgAvatar : ImgFolder
              "
            />
            <span>{{ data.name }}</span
            ><el-tag v-if="data.isAdmin" class="ml5" size="mini">管理员</el-tag>
          </div>
        </template>
      </el-tree>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :disabled="loading"
        :loading="submitLoading"
        @click="dataFormSubmit"
        >确定</el-button
      >
    </span>
  </el-dialog>
</template>

<script>
import { cloneDeep, flatMap, debounce } from "lodash";
import api_group from "@/api/sys/group";
import api_user from "@/api/sys/user";
import api_org from "@/api/sys/org";
import ImgAvatar from "@/assets/images/avatar.png";
import ImgFolder from "@/assets/images/icon_file.png";
export default {
  data() {
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      dataForm: {},
      defaultProps: {
        children: "children",
        label: "name"
      },
      ImgAvatar,
      ImgFolder,
      orgList: [],
      userList: [],
      adminList: [],
      treeData: [],
      filterText: "",
      selectedCount: 0
    };
  },
  watch: {
    filterText(val) {
      this.searchTree(val);
    }
  },
  methods: {
    init(id) {
      this.visible = true;
      this.loading = true;
      this.dataForm = {};
      this.orgList = [];
      this.userList = [];
      this.adminList = [];
      this.treeData = [];
      Promise.all([
        api_group.getDetail(id).catch(() => null),
        api_org.getTree().catch(() => null),
        api_user.getList({ maxResult: 9999 }).catch(() => null)
      ])

        .then(res => {
          this.dataForm = Object.assign({}, res[0]);
          this.orgList = res[1] || [];
          this.userList = (res[2] && res[2].resultData) || [];
          if (this.dataForm) {
            [
              this.dataForm.administrator1,
              this.dataForm.administrator2,
              this.dataForm.administrator3
            ].forEach(item => {
              if (item) {
                this.adminList.push(item);
              }
            });
          }
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
        const isAdmin = this.adminList.includes(user.userName);
        const treeUser = {
          ...user,
          nodeKey: `user-${user.id}`,
          name: user.realName,
          isUser: true,
          isAdmin: isAdmin,
          disabled: isAdmin
        };
        if (org && org.children) {
          org.children.push(treeUser);
        } else if (org) {
          org.children = [treeUser];
        }
      });
      this.treeData = treeData;
      // 回填选中状态
      if (this.dataForm && this.dataForm.users) {
        this.$nextTick(() => {
          const keys = [];
          if (this.dataForm.users) {
            keys.push(...this.dataForm.users.map(item => `user-${item.id}`));
          }
          // 默认勾选管理员
          if (this.adminList && this.adminList.length) {
            keys.push(
              ...this.userList
                .filter(item => this.adminList.includes(item.userName))
                .map(item => `user-${item.id}`)
            );
          }
          this.$refs.tree.setCheckedKeys(keys);
        });
      }
      this.selectedCount =
        (this.dataForm && this.dataForm.users && this.dataForm.users.length) ||
        0;
    },
    searchTree: debounce(function(val) {
      this.$refs.tree.filter(val);
    }, 500),
    filterNode(value, data) {
      if (!value) return true;
      const str = value.toLowerCase();
      return (
        (data.realName && data.realName.includes(str)) ||
        (data.userName && data.userName.includes(str))
      );
    },
    onSelectionChange(data, checked, indeterminate) {
      this.selectedCount = this.$refs.tree
        .getCheckedNodes()
        .filter(node => node.isUser).length;
    },
    // 表单提交
    dataFormSubmit() {
      const users = this.$refs.tree
        .getCheckedNodes()
        .filter(node => node.isUser);
      this.submitLoading = true;
      api_group
        .replaceUser(
          this.dataForm.id,
          users.map(user => user.id)
        )
        .then(() => {
          this.$message.success("操作成功");
          this.visible = false;
        })
        .finally(() => {
          this.submitLoading = false;
        });
    }
  }
};
</script>
<style lang="scss" scoped>
.el-tree {
  max-height: calc(70vh - 54px - 60px - 62px);
  overflow: auto;
}
</style>
