<template>
  <div class="dic">
    <page-title />
    <el-row :gutter="10">
      <el-col :md="12">
        <el-form
          :inline="true"
          :model="dataForm"
          @keyup.enter.native="submitQueryForm"
        >
          <el-form-item>
            <el-input
              v-model="dataForm.code"
              v-iePlaceholder
              placeholder="字典编码"
              clearable
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="dataForm.name"
              v-iePlaceholder
              placeholder="字典名称"
              clearable
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-select
              v-model="dataForm.type"
              v-iePlaceholder
              placeholder="字典类型"
              clearable
            >
              <el-option
                v-for="item in typeList"
                :key="item.itemValue"
                :value="item.itemValue"
                :label="item.itemName"
              ></el-option>
            </el-select>
          </el-form-item>
          <!-- <el-form-item>
            <el-select v-model="dataForm.service" placeholder="服务标识" clearable>
              <el-option
                v-for="item in serviceList"
                :key="item.key"
                :value="item.key"
                :label="item.name"
              ></el-option>
            </el-select>
          </el-form-item>-->
          <el-form-item>
            <el-button @click="submitQueryForm">查询</el-button>
            <el-button type="primary" @click="addOrUpdateHandle()"
              >新增</el-button
            >
            <el-button type="primary" @click="refresh()">刷新</el-button>
          </el-form-item>
          <el-form-item v-if="globalConfig.useBiDic">
            <el-button @click="downloadTemplate">模板下载</el-button>
            <el-button
              type="primary"
              :loading="fileImporting"
              @click="dicImport"
              >批量导入</el-button
            >
          </el-form-item>
        </el-form>
        <el-table
          v-loading="dataListLoading"
          ref="table"
          :data="dataList"
          stripe
          style="width: 100%"
          @row-click="rowClick"
          :highlight-current-row="true"
        >
          <el-table-column type="expand">
            <template slot-scope="props">
              <el-form label-position="left" inline class="demo-table-expand">
                <!-- <el-form-item label="运营商">
                  <span>{{ props.row.operator }}</span>
                </el-form-item>
                <el-form-item label="归属省">
                  <span>{{ props.row.province }}</span>
                </el-form-item>
                <el-form-item label="归属市">
                  <span>{{ props.row.city }}</span>
                </el-form-item>
                <el-form-item label="服务标识">
                  <span>{{ props.row.service }}</span>
                </el-form-item>-->
                <el-form-item label="创建时间">
                  <span>{{ props.row.createtime | timeFilter }}</span>
                </el-form-item>
                <el-form-item label="操作时间">
                  <span>{{ props.row.modifytime | timeFilter }}</span>
                </el-form-item>
                <el-form-item label="操作者">
                  <span>{{ props.row.modifier }}</span>
                </el-form-item>
                <el-form-item label="备注">
                  <span>{{ props.row.comment }}</span>
                </el-form-item>
              </el-form>
            </template>
          </el-table-column>
          <el-table-column
            type="index"
            :index="getIndex"
            header-align="center"
            align="center"
            width="50"
            label="序号"
          ></el-table-column>
          <el-table-column
            prop="code"
            header-align="center"
            align="center"
            label="字典编码"
          ></el-table-column>
          <el-table-column
            prop="name"
            header-align="center"
            align="center"
            label="字典名称"
          ></el-table-column>
          <el-table-column
            prop="hierarchy"
            header-align="center"
            align="center"
            label="字典层级"
          ></el-table-column>
          <el-table-column
            prop="type"
            header-align="center"
            align="center"
            label="字典类型"
          >
            <template slot-scope="scope">
              {{ scope.row.type | codeFilter(typeList) }}
            </template>
          </el-table-column>
          <el-table-column
            header-align="center"
            align="center"
            width="150"
            label="操作"
          >
            <template slot-scope="scope">
              <el-button type="text" @click="refresh(scope.row)"
                >刷新</el-button
              >
              <el-button type="text" @click="addOrUpdateHandle(scope.row)"
                >修改</el-button
              >
              <el-button type="text" @click="deleteHandle(scope.row.id)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          :disabled="dataListLoading"
          class="mt10"
          @size-change="sizeChangeHandle"
          @current-change="currentChangeHandle"
          :current-page="paging"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="limit"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
        ></el-pagination>
      </el-col>
      <el-col :md="12">
        <el-form :inline="true" class="dicItem">
          <el-form-item>
            <el-input
              placeholder="输入关键字进行过滤"
              v-model.trim="filterText"
              clearable
            ></el-input>
          </el-form-item>
          <el-form-item v-if="selectRow.type != 111" class="treeList">
            <el-button
              type="success"
              @click="onAdd(true)"
              :disabled="selectRow.id == null"
              >新增一级字典项</el-button
            >
            <el-button
              type="primary"
              @click="onAdd(false)"
              :disabled="
                !(
                  currentNode.id != null &&
                  currentNode.itemLevel < selectRow.hierarchy
                )
              "
              >新增子字典项</el-button
            >
            <el-button
              type="warning"
              @click="editCode"
              :disabled="currentNode.id == null"
              >编辑</el-button
            >
            <el-button
              type="danger"
              @click="deleteCode"
              :disabled="currentNode.id == null"
              >删除</el-button
            >
          </el-form-item>
        </el-form>
        <el-card class="box-card" style="margin-left: 25px">
          <div slot="header" class="clearfix">
            <span>{{ selectRow.code }} ( {{ selectRow.name }} ) 字典项</span>
          </div>
          <el-tree
            ref="dicTree"
            class="dic-tree"
            :data="treeData"
            :props="{ label: getName, children: 'childs' }"
            node-key="id"
            :default-expanded-keys="expandedKeys"
            @current-change="setCurrentNode"
            highlight-current
            :filter-node-method="filterNode"
            :render-content="renderContent"
          ></el-tree>
        </el-card>
      </el-col>
    </el-row>

    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdate" @onSubmit="getDataList"></add-or-update>
    <add-or-update-tree
      ref="addOrUpdateTree"
      @onSubmit="onDicItemSubmit"
    ></add-or-update-tree>

    <!-- 弹窗, 批量导入 -->
    <dic-import v-if="globalConfig.useBiDic" ref="dicImport" @onSubmit="getDataList" />
  </div>
</template>
<script>
import api_dic from "@/api/sys/dic";
import AddOrUpdate from "./add-or-update";
import AddOrUpdateTree from "./add-or-update-tree";
import { formatTime } from "@/utils/time";
import { getDic, pubDicUpdated } from "@/utils/dic";
import { downloadFile } from "@/utils/download";
import DicImport from "./dic-import.vue";

export default {
  data() {
    return {
      globalConfig: window.globalConfig,
      dataForm: {},
      dataList: [],
      treeData: [],
      typeList: [],
      selectRow: {},
      serviceList: [],
      currentNode: {},
      expandedKeys: [],
      filterText: "",
      paging: 1,
      limit: 10,
      total: 0,
      dataListLoading: false,
      fileImporting: false,
    };
  },
  components: {
    AddOrUpdate,
    AddOrUpdateTree,
    DicImport,
  },
  watch: {
    filterText(val) {
      this.$refs.dicTree.filter(val);
    },
  },
  created() {
    this.getTypeList();
    this.getDataList();
  },
  methods: {
    refresh(row = {}) {
      let params = row.id ? {id: row.id} : {}
      api_dic.clearCache(params).then(data => {
        if (data) {
          this.$message.success("刷新成功");
        }
      });
    },
    getIndex(index) {
      return index + 1 + this.limit * (this.paging - 1);
    },
    //获取树形结构字典项名称
    getName(data, node) {
      return data.itemName + "-" + data.itemValue;
    },
    // 过滤节点
    filterNode(value, data) {
      if (!value) return true;
      return data.itemName.indexOf(value) !== -1;
    },
    //获取字典类型列表
    getTypeList() {
      getDic(["code"], (data) => {
        this.typeList = data.code;
      });
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      this.filterText = "";
      api_dic
        .getList({
          paging: this.paging,
          limit: this.limit,
          service: this.dataForm.service,
          name: this.dataForm.name,
          code: this.dataForm.code,
          type: this.dataForm.type,
        })
        .then((data) => {
          this.dataList = data.list;
          if (this.dataList.length > 0) {
            this.rowClick(this.dataList[0]);
            this.$nextTick(() => {
              // document
              //   .getElementsByClassName("el-table__body-wrapper")[0]
              //   .getElementsByTagName("tr")[0]
              //   .classList.add("current-row");
              this.$refs.table.setCurrentRow(this.dataList[0]);
            });

            //document.querySelectorAll("tr")[0].classList.add("current-row");
          }
          this.total = data.total;
          this.dataListLoading = false;
          if (this.currentNode.id) {
            this.expandedKeys = [this.currentNode.id];
            this.$refs.dicTree.setCurrentKey(this.currentNode.id);
            this.setCurrentNode(this.$refs.dicTree.getCurrentNode());
          }
        })
        .catch(() => {
          this.dataListLoading = false;
        });
    },
    //行点击事件
    rowClick(row) {
      // if (row.id == this.selectRow.id) {
      //   return;
      // }
      this.filterText = "";
      if (row && !row.itemName) {
        this.selectRow = { ...row };
        this.currentNode = {};
      } else {
        //this.setCurrentNode(row);
        this.currentNode = {};
      }
      api_dic
        .getCode(this.selectRow.id)
        .then((data) => {
          this.treeData = data.list.childs;
        })
        .catch(() => {
          this.dataListLoading = false;
        });
    },
    // 选择节点
    setCurrentNode(nodeData) {
      if (nodeData == null) return;
      this.currentNode = nodeData;
      let tmp = { ...nodeData };
      this.dataForm = tmp;
    },
    submitQueryForm() {
      this.paging = 1;
      this.getDataList();
    },
    // 每页数
    sizeChangeHandle(val) {
      this.limit = val;
      this.paging = 1;
      this.getDataList();
    },
    // 当前页
    currentChangeHandle(val) {
      this.paging = val;
      this.getDataList();
    },
    timeFormatter(row, column, cellValue, index) {
      return formatTime(cellValue);
    },
    // 新增 / 修改
    addOrUpdateHandle(row) {
      this.$refs.addOrUpdate.init({ ...row });
    },
    downloadTemplate() {
      downloadFile(
        window.globalConfig.defaultProxyPath +
          "/everbi/dic/baseDic/downLoadTemplate"
      );
    },
    dicImport() {
      this.$refs.dicImport.init();
    },
    //添加或删除字典项type:是否为一级字典项
    onAdd(type) {
      if (type) {
        this.$refs.addOrUpdateTree.init({
          name: this.selectRow.code,
          itemLevel: 1,
          dicId: this.selectRow.id,
        });
      } else {
        let currentNode = { ...this.currentNode };
        let data = {};
        data.name = this.selectRow.code;
        data.pName = currentNode.itemName;
        data.itemLevel = currentNode.itemLevel + 1;
        data.dicId = this.selectRow.id;
        data.pId = currentNode.id;
        this.$refs.addOrUpdateTree.init(data);
      }
    },
    //编辑字典项
    editCode() {
      //console.log(this.currentNode);
      let currentNode = { ...this.currentNode };
      let pName = "";
      currentNode.name = this.selectRow.code;
      if (currentNode.pId != "-9") {
        let pNode = this.$refs.dicTree.getNode(currentNode.pId);
        pName = pNode.data.itemName;
        currentNode.pName = pName;
      }
      this.$refs.addOrUpdateTree.init(currentNode);
    },
    //删除字典项
    deleteCode() {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          api_dic.deleteCode(this.currentNode.id).then(() => {
            api_dic
              .getCode(this.selectRow.id)
              .then((data) => {
                this.treeData = data.list.childs;
                this.$message({
                  message: "操作成功",
                  type: "success",
                  duration: 1500,
                });
                this.currentNode = {};

                pubDicUpdated(this.selectRow.code);
              })
              .catch(() => {
                this.dataListLoading = false;
              });
          });
        })
        .catch(() => {});
    },
    // 删除
    deleteHandle(id) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          api_dic.delete(id).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
        })
        .catch(() => {});
    },
    onDicItemSubmit(data) {
      this.rowClick(data);

      pubDicUpdated(this.selectRow.code);
    },
    renderContent(h, { node, data, store }) {
      return (
        <span title={data.remark5}>
          <span>{data.itemName}</span>
          <span> - </span>
          <span class="dic-item-value">{data.itemValue}</span>
        </span>
      );
    },
  },
};
</script>
<style type="text/css" scoped>
.demo-table-expand .el-form-item {
  width: 50%;
  margin: 0;
}
</style>
<style lang="scss">
.dic {
  .dicItem {
    .el-input {
      position: relative;
      left: 25px;
      width: 200px;
    }

    .treeList {
      float: right;
    }
  }
}
</style>
<style lang="scss" scoped>
.dic-tree {
  max-height: 700px;
  overflow: auto;
}
.dic {
  ::v-deep .dic-item-value {
    color: #409eff; // ie兼容性
    color: var(--primary-bg);
  }
}
</style>
