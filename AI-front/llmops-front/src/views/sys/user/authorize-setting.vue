<template>
  <el-dialog title="授权用户的访问资源"
    :close-on-click-modal="false" :visible.sync="visible" @closed="reset">
    <el-form v-loading="loading" label-width="80px">
      <el-form-item label="授权资源">
        <el-card shadow="never">
          <el-row :gutter="10">
            <el-col v-for="(list, index) in menuList" :key="list.key"
              :md="Math.max(8, Math.floor(24 / menuList.length))">
              <h1 class="treeTitle">
                <span class="mr20">{{ list.value.name }}</span>
                <el-button type="text" @click="selectAll(list, index)">全选</el-button>
                <el-button type="text" @click="selectAll(list, index, false)">全不选</el-button>
              </h1>
              <el-tree :data="list.value.menus" node-key="id" ref="menuListTree" show-checkbox
                class="menu-tree">
                <span slot-scope="{ data }" :class="'type-' + data.type">
                  <!-- <i v-if="data.icon" :class="'fa fa-' + data.icon"></i> -->
                  <span>{{ data.name }}</span>
                </span>
              </el-tree>
            </el-col>
          </el-row>
        </el-card>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :disabled="loading" :loading="submitLoading"
        @click="submit">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import api_menu from '@/api/sys/menu';
import api_user from '@/api/sys/user';

export default {
  data() {
    return {
      visible: false,
      loading: false,
      submitLoading: false,
      menuList: [],
      id: null
    };
  },
  computed: {
    
  },
  methods: {
    async init(id) {
      this.id = id
      this.visible = true;
      this.loading = true;
      const menuArr = (await api_user.getMenus(id)) || [];
      console.log(menuArr)
			const menuIds = menuArr.map(it => it.id)
      api_menu.getMenuList().then(data => {
        this.menuList = [];
        for (let key in data) {
          this.menuList.push({
            key,
            value: data[key]
          });
        }
        this.loading = false;
				this.$nextTick(() => {
					this.$refs.menuListTree.map(tree => {
						const leafKeys = menuIds.filter(key => {
							const node = tree.getNode(key);
							return node && node.isLeaf;
						});
						tree.setCheckedKeys(leafKeys);
					});
				})
				
      });
    },
    async getUserMenu() {

    },
    reset() {
      this.$refs.menuListTree.map(item => item.setCheckedKeys([]));
    },
    selectAll(list, index, isSelect = true) {
      this.$refs.menuListTree[index].setCheckedKeys(
        isSelect ? list.value.menus.map(item => item.id) : []
      );
    },
    // 表单提交
    submit() {
      this.submitLoading = true;
      let menuIds = '';
      let arr = [];
      this.$refs.menuListTree.map(item => {
        arr = arr.concat(item.getCheckedKeys(), item.getHalfCheckedKeys());
      });
      menuIds = arr.join(',');
      console.log(menuIds);
			const userId = localStorage.getItem('id')
			const userName = localStorage.getItem('userName')
      api_user
        .setMenus(userId, userName, this.id, menuIds)
        .then(() => {
          this.visible = false;
          this.submitLoading = false;
          this.$emit("onSubmit");
          this.$message({
            message: "操作成功",
            type: "success",
            duration: 1500,
          });
        })
        .catch(() => {
          this.submitLoading = false;
        });
    }
  }
};
</script>

<style lang="scss" scoped>
.treeTitle {
  margin: 0;
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
    i.fa {
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
