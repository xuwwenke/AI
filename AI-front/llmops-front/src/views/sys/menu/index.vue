<template>
  <div>
    <page-title />
    <el-form :inline="true">
      <el-form-item>
        <el-select
          v-model="appKey"
          placeholder="请选择系统"
          :loading="appListLoading"
          filterable
          @change="onChangeApp"
        >
          <el-option v-for="app in appList" :key="app.key" :value="app.key" :label="app.name"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="filterText" v-iePlaceholder placeholder="输入关键字进行过滤" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <!-- <el-button
          v-if="isAuth('sys.menu.addMenu')"
          type="success"
          @click="onAdd(1, true)"
          :disabled="appKey == null"
        >新增一级菜单</el-button>-->
        <!-- <el-button
          v-if="isAuth('sys.menu.addMenu')"
          type="primary"
          @click="onAdd(1)"
          :disabled="currentNode.id == null || currentNode.type == 2"
        >新增子菜单</el-button>-->
        <el-button
          v-if="isAuth('sys.menu.addMenu')"
          type="primary"
          @click="onAdd(1)"
          :disabled="appKey == null || dataListLoading || currentNode.type == 2"
        >新增菜单</el-button>
        <el-button
          v-if="isAuth('sys.menu.addPermission')"
          type="primary"
          @click="onAdd(2)"
          :disabled="currentNode.id == null"
        >新增资源</el-button>
        <el-button
          v-if="isAuth('sys.menu.edit')"
          type="warning"
          @click="onEdit()"
          :disabled="currentNode.id == null"
        >修改</el-button>
        <el-button
          v-if="isAuth('sys.menu.del')"
          type="danger"
          @click="onDelete()"
          :disabled="currentNode.id == null"
        >删除</el-button>
        <el-upload
          v-if="isAuth('sys.menu.import')"
          :show-file-list="false"
          accept=".xlsx"
          action="#"
          :on-change="handleUploadChange"
          :http-request="handleHttpRequest"
          class="btn"
        >
          <el-button type="primary">全量导入</el-button>
        </el-upload>
        <el-button
          v-if="isAuth('sys.menu.export')"
          type="primary"
          :disabled="appKey == null"
          @click="onDownload(appKey)"
          >全量导出</el-button
        >
        <el-button
          v-if="isAuth('sys.menu.export')"
          type="primary"
          :disabled="appKey == null"
          @click="onDefineDownload(appKey)"
          >自定义导出</el-button
        >
      </el-form-item>
    </el-form>
    <el-row :gutter="10">
      <el-col :md="9">
        <el-tree
          ref="menuTree"
          class="menu-tree"
          v-loading="dataListLoading"
          :data="dataList"
          node-key="id"
          :empty-text="appKey ? undefined : '请先选择系统'"
          :default-expanded-keys="expandedKeys"
          :filter-node-method="filterNode"
          highlight-current
          @current-change="setCurrentNode"
          @node-click="onNodeClick"
        >
          <span slot-scope="{ data }" :class="'type-' + data.type">
            <Icon class="menu-icon" :icon="data.icon" />
            <span>{{ data.name }}</span>
          </span>
        </el-tree>
      </el-col>
      <el-col :md="15">
        <add-or-update
          ref="addOrUpdate"
          :appKey="appKey"
          :optionData="optionData"
          @onSubmit="getDataList"
          @onConfirmCancel="onConfirmCancel"
        />
      </el-col>
    </el-row>
    <MenuTree ref="chooseTreeRef" :appKey="appKey" :list="dataList"></MenuTree>
  </div>
</template>

<script>
import api_app from "@/api/sys/app";
import api_menu from "@/api/sys/menu";
// import { sortTreeData } from "@/utils/base";
import AddOrUpdate from "./add-or-update";
import cloneDeep from "lodash/cloneDeep";
import { mapState } from "vuex";
import { downloadFile } from "@/utils/download";
import MenuTree from "./menu-tree";

export default {
  components: {
    AddOrUpdate,
    MenuTree
  },
  data() {
    return {
      appListLoading: false,
      dataListLoading: false,
      appKey: window.globalConfig.appKey,
      appList: [],
      filterText: "",
      menuObj: {},
      expandedKeys: [],
      oldNode: {},
      currentNode: {},
      dataList: [],
      optionData: [],
      file: null,
    };
  },
  computed: {
    ...mapState("apps", ["currentKey"])
  },
  created() {
    this.getAppList();
    this.getDataList();
  },
  watch: {
    filterText(val) {
      this.$refs.menuTree.filter(val);
    }
  },
  methods: {
    // 获取app列表
    getAppList() {
      this.appListLoading = true;
      api_app
        .getList({ maxResult: 9999 })
        .then(data => {
          this.appListLoading = false;
          this.appList = data.resultData;
        })
        .catch(() => {
          this.appListLoading = false;
        });
    },

    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      api_menu
        .getMenuList()
        .then(data => {
          this.menuObj = data;
          if (this.appKey) {
            this.setTreeData(this.appKey);
          }
          this.dataListLoading = false;
        })
        .then(() => {
          if (this.currentNode.id) {
            this.expandedKeys = [this.currentNode.id];
            this.$refs.menuTree.setCurrentKey(this.currentNode.id);
            this.setCurrentNode(this.$refs.menuTree.getCurrentNode());
          }
        })
        .catch(() => {
          this.dataListLoading = false;
        });
    },
    // 选择系统
    onChangeApp(appKey) {
      this.oldNode = {};
      this.currentNode = {};
      this.$refs.addOrUpdate.reset();
      this.$refs.addOrUpdate.onCancel();
      this.setTreeData(appKey);
    },
    // 过滤节点
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    // 设置当前app的菜单树数据
    setTreeData(appKey) {
      try {
        // this.dataList = sortTreeData(this.menuObj[appKey].menus);
        this.dataList = this.menuObj[appKey].menus;
        this.$nextTick(() => {
          this.$refs.menuTree.filter(this.filterText);
        });
      } catch (e) {
        this.dataList = [];
      }

      this.updateOptionData();
    },
    // 更新右侧上级菜单下拉框数据
    updateOptionData(noSelf = false) {
      this.optionData =
        this.getOptionData(cloneDeep(this.dataList), noSelf) || [];
    },
    getOptionData(data, noSelf = false) {
      let tmp = [];
      for (let item of data) {
        if (noSelf && item.id == this.currentNode.id) continue;
        if (item.type == 1 || this.currentNode.type !== 1) tmp.push(item);
        if (item.children)
          item.children = this.getOptionData(item.children, noSelf);
      }
      return tmp;
    },
    // 选择节点
    setCurrentNode(nodeData) {
      if (nodeData == null) return;
      this.oldNode = this.currentNode;
      this.currentNode = nodeData;
      this.$refs.addOrUpdate.updateFormData(this.currentNode);
    },
    onConfirmCancel() {
      this.$refs.menuTree.setCurrentKey(this.oldNode.id);
    },
    onNodeClick(data, node, vm) {
      vm.$el.blur();
    },
    /**
     * 新增菜单或权限
     * type 类型  1 菜单 2权限
     * root 是否一级菜单
     */
    onAdd(type, root = false) {
      this.$refs.addOrUpdate.onAdd(type, root, this.currentNode);
      this.updateOptionData();
    },
    onEdit() {
      this.$refs.addOrUpdate.onEdit();
      this.updateOptionData(true);
    },
    // 删除
    onDelete() {
      this.$confirm(`确定对[${this.currentNode.name}]进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          api_menu.delete(this.currentNode.id).then(() => {
            if (this.currentNode.parent) {
              this.expandedKeys = [this.currentNode.parent.id];
            }
            this.oldNode = {};
            this.currentNode = {};
            this.$refs.addOrUpdate.reset();
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500
            });
          });
        })
        .catch(() => {});
    },
    // onUpload() {
    //   this.$refs.uploadRef.init();
    // },
    onDownload(appKey) {
      if (!appKey) return;
      downloadFile(api_menu.download({ appKey }));
      return false;
    },
    async handleUploadChange(file, fileList){
      this.file = file;
    },
    async handleHttpRequest(){
      try {
        let res = await api_menu.upload(this.toFormData({ file: this.file.raw }));
        if (res) {
          this.$message({
            type: "success",
            message: "导入成功"
          });
          await this.getDataList();
        } else {
          this.$message({
            type: "error",
            message: "导入失败，请稍候重试"
          });
        }
      } catch (err) {
        console.log(err);
      }
    },
    onDefineDownload() {
      this.$refs.chooseTreeRef.init();
    },
    toFormData(obj) {
      if (!obj || !Object.keys(obj).length) return obj;
      return Object.keys(obj).reduce((acc, key) => {
        acc.append(key, obj[key]);
        return acc;
      }, new FormData());
    }
  }
};
</script>

<style lang="scss" scoped>
.menu-tree {
  max-height: 780px;
  overflow: auto;
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
.btn {
  display: inline-block;
  margin: 0 10px;
}
</style>
