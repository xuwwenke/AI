<template>
  <el-dialog :title="disabled ? '详情' : dataForm.id == null ? '新增' : '修改'"
    :close-on-click-modal="disabled" :visible.sync="visible" @closed="reset">
    <el-form v-loading="loading" :model="dataForm" :rules="dataRule" ref="dataForm"
      label-width="90px" :disabled="disabled">
      <el-form-item label="上级角色" prop="parentId">
        <el-select v-model="fixedParentId" filterable clearable @change="getParentMenus"
          :disabled="dataForm.id != null && (dataForm.parentId == null || !parentMatching)">
          <el-option v-for="role in roleList" :key="role.id" :value="role.id" :label="role.name">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="角色名称" prop="name">
        <el-input v-model.trim="dataForm.name" placeholder="角色名称"></el-input>
      </el-form-item>
      <el-form-item label="角色描述" prop="description">
        <el-input type="textarea" :rows="3" v-model="dataForm.description" placeholder="请输入描述">
        </el-input>
      </el-form-item>
      <el-form-item>
        <span slot="label">
          授权资源
          <el-tooltip
            placement="right"
            content="只能从上级角色拥有的资源（菜单、按钮权限等）中勾选"
          >
            <i class="el-icon-question"></i>
          </el-tooltip>
        </span>
        <div v-if="!disabled" class="mb10">
          <el-input v-model="searchStr" prefix-icon="el-icon-search" placeholder="搜索资源" clearable style="margin-right: 10px; width: 300px;"></el-input>
        </div>
        <div v-loading="loadingMenus" style="min-height: 100px; max-height: 740px; overflow: auto;">
          <!-- <el-row :gutter="10"> -->
          <div class="masonry-container">
            <!-- <el-col v-for="(list, index) in treesData" :key="list.key" class="mb20"
              :md="Math.max(8, Math.floor(24 / treesData.length))" v-show="isTreeVisible(list)"> -->
            <div v-for="(list, index) in treesData" :key="list.key" v-show="isTreeVisible(list)" class="masonry-item">
              <h1 class="treeTitle">
                <span class="mr20">{{ list.value.name }}</span>
                <span class="tools" v-if="!disabled">
                  <el-button type="text" @click="selectAll(list, index)">全勾选</el-button>
                  <el-button type="text" @click="selectAll(list, index, false)">全不选</el-button>
                </span>
              </h1>
              <el-tree :data="list.value.menus"
                node-key="id" ref="menuListTree" show-checkbox
                class="menu-tree">
                <span slot-scope="{ data }" :class="'type-' + data.type">
                  <Icon class="menu-icon" :icon="data.icon" />
                  <span>{{ data.name }}</span>
                </span>
              </el-tree>
            </div>
          </div>
            <!-- </el-col> -->
          <!-- </el-row> -->
        </div>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{ disabled ? '关闭' : '取消'}}</el-button>
      <el-button v-if="!disabled" type="primary" :disabled="loading" :loading="submitLoading"
        @click="dataFormSubmit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_menu from '@/api/sys/menu';
import api_role from '@/api/sys/role';

export default {
  data() {
    return {
      visible: false,
      disabled: false,
      loading: false,
      loadingMenus: false,
      submitLoading: false,
      searchStr: '',
      roleList: [],
      treesData: [],
      currentRoleData: {},
      parentName: null,
      dataForm: {
        parentId: null,
        name: null
      },
      dataRule: {
        name: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }]
      }
    };
  },
  computed: {
    parentMatching() {
      const parentId = this.dataForm.parentId;
      if (parentId != null && parentId != '') {
        return Boolean(this.roleList.find(item => item.id == parentId));
      }
      return false;
    },
    // 上级角色可能处于当前操作人可见的角色列表之外，这时直接显示 parent.name
    fixedParentId: {
      get() {
        if (!this.parentMatching) return this.parentName;
        return this.dataForm.parentId;
      },
      set(v) {
        this.dataForm.parentId = v;
      }
    },
  },
  methods: {
    init(id, disabled) {
      this.visible = true;
      this.disabled = disabled;
      // 修复焦点在带校验的项按回车提交后，再次打开对话框，该项会带有校验提示的问题
      // 而 resetFields 仍然放在关闭对话框时执行，以避免下次打开时，带动画的控件（如checkbox）会闪现之前的结果
      this.$nextTick(() => {
        this.$refs.dataForm.clearValidate();
      });

      this.treesData = []
      this.loading = true;
      this.dataForm.id = id;

      if (this.dataForm.id != null) {
        api_role.getInfo(this.dataForm.id).then(data => {
          this.loading = false;

          if (data && data.role) {
            this.parentName = data.role.parent && data.role.parent.name;

            this.dataForm = {
              id,
              parentId: data.role.parent && data.role.parent.id,
              name: data.role.name,
              description: data.role.description
            };

            this.currentRoleData = data

            this.getParentMenus()
          }
        });
      } else {
        this.getParentMenus()
      }

      api_role
        .getList({
          maxResult: 1000
        })
        .then(data => {
          this.loading = false;
          this.roleList = data ? data.resultData : [];
        });
    },
    reset() {
      this.$refs.dataForm.resetFields();
      this.$refs.menuListTree.map(item => item.setCheckedKeys([]));
    },
    async getParentMenus() {
      this.loadingMenus = true

      this.treesData = []
      const parentId = this.dataForm.parentId

      let data
      if (parentId) {
        const tmp = await api_role.getInfo(parentId)
        data = tmp && tmp.apps
      } else {
        const tmp = await api_menu.getMenuList()
        data = tmp
      }

      for (let key in data) {
        this.treesData.push({
          key,
          value: data[key]
        });
      }

      this.$nextTick(() => {
        this.loadingMenus = false

        if (this.currentRoleData.menuMap) {
          this.$refs.menuListTree && this.$refs.menuListTree.map(tree => {
            const leafKeys = Object.keys(this.currentRoleData.menuMap).filter(key => {
              const node = tree.getNode(key);
              return node && node.isLeaf;
            });

            tree.setCheckedKeys(leafKeys);
          });
        }
      })
    },

    // 判断树是否应该显示
    isTreeVisible(list) {
      // 如果是详情模式(disabled=true)，只显示有勾选节点的树
      if (this.disabled) {
        // 获取当前树对应的树组件索引
        const index = this.treesData.findIndex(item => item.key === list.key);
        // 如果找到对应的树组件，检查是否有选中节点
        if (index !== -1 && this.$refs.menuListTree && this.$refs.menuListTree[index]) {
          const checkedKeys = this.$refs.menuListTree[index].getCheckedKeys();
          // 如果没有选中节点，则不显示
          return checkedKeys.length > 0;
        }
        return false;
      }

      // 非详情模式，使用搜索过滤
      if (!this.searchStr) return true;

      // 检查list.value.name是否匹配searchStr
      if (list.value.name && list.value.name.toLowerCase().includes(this.searchStr.toLowerCase())) {
        return true;
      }

      // 检查list.value.menus中任一级的name属性是否匹配searchStr
      if (list.value.menus && list.value.menus.length) {
        // 递归检查菜单树中的name属性
        const hasMatchingMenu = (menus) => {
          for (const menu of menus) {
            if (menu.name && menu.name.toLowerCase().includes(this.searchStr.toLowerCase())) {
              return true;
            }
            // 如果有子菜单，递归检查
            if (menu.children && menu.children.length) {
              if (hasMatchingMenu(menu.children)) {
                return true;
              }
            }
          }
          return false;
        };

        return hasMatchingMenu(list.value.menus);
      }

      return false;
    },

    selectAll(list, index, isSelect = true) {
      this.$refs.menuListTree[index].setCheckedKeys(
        isSelect ? list.value.menus.map(item => item.id) : []
      );
    },
    // 表单提交
    dataFormSubmit() {
      this.$refs.dataForm.validate(valid => {
        if (valid) {
          this.submitLoading = true;

          const tmp = { ...this.dataForm };
          let arr = [];
          this.$refs.menuListTree.map(item => {
            arr = arr.concat(item.getCheckedKeys(), item.getHalfCheckedKeys());
          });
          tmp.menuIds = arr.join(',');
          // console.log(tmp.menuIds);

          const opt = this.dataForm.id == null ? 'insert' : 'update';

          api_role[opt](tmp)
            .then(() => {
              this.visible = false;
              this.submitLoading = false;
              this.$emit('onSubmit');
              this.$message({
                message: '操作成功',
                type: 'success',
                duration: 1500
              });
            })
            .catch(() => {
              this.submitLoading = false;
            });
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
::v-deep .el-dialog__body {
  padding-bottom: 0;
}
.masonry-container {
  column-count: 3; /* 分为3列 */
  column-gap: 20px; /* 列间距 */
  width: 100%; /* 容器宽度 */
}

.masonry-item {
  position: relative;
  display: inline-block;
  width: 100%; /* 每个item的宽度 */
  box-sizing: border-box;
  break-inside: avoid; /* 防止item在列中被拆分 */
  margin-bottom: 20px; /* item之间的间距 */
  padding: 10px; /* 内边距 */
  border: 1px solid #ddd; /* 边框 */
  border-radius: 4px; /* 边框圆角 */

  .tools {
    position: absolute;
    top: 1px;
    right: 0;
    padding: 0 10px;
    background: rgba(255, 255, 255, 0.9);
    display: none;
  }

  &:hover {
    .tools {
      display: block;
    }
  }
}
.treeTitle {
  margin: 0;
  color: var(--primary-bg);
  line-height: 1;
}
.menu-tree {
  max-height: 500px;
  overflow: auto;
  line-height: normal;
  .el-tree-node {
    font-size: 14px;
    &.is-hidden {
      display: block !important;
      color: #e4e4e4;
    }
    .menu-icon {
      width: 20px;
      margin-right: 5px;
      text-align: center;
    }
    .type-2 {
      font-size: 12px;
      > span {
        border-bottom: 1px dashed;
      }
    }
  }
}
</style>
