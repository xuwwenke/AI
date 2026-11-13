<template>
  <div>
    <page-title />
    <el-alert title="只能查看您拥有的角色，以及下级角色" type="info" show-icon class="mb10"></el-alert>
    <el-form :inline="true" :model="dataForm" @submit.native.prevent @keyup.enter.native="submitQueryForm">
      <el-form-item>
        <el-input v-model="dataForm.name" v-iePlaceholder placeholder="角色名称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="submitQueryForm">查询</el-button>
        <el-button v-if="isAuth('sys.role.add')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" stripe style="width: 100%;">
      <el-table-column type="index" :index="getIndex" align="center" width="50" label="序号"></el-table-column>
      <el-table-column prop="name" align="center" label="角色名称">
        <template slot-scope="scope">
          <el-button type="text" @click="addOrUpdateHandle(scope.row.id, true)">{{ scope.row.name }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="parent.alias" align="center" label="上级角色"></el-table-column>
      <el-table-column prop="description" align="center" label="角色描述"></el-table-column>
      <el-table-column fixed="right" align="center" width="150" label="操作">
        <template slot-scope="scope">
          <el-button
            v-if="isAuth('sys.role.edit')"
            type="text"
            @click="addOrUpdateHandle(scope.row.id)"
          >修改</el-button>
          <el-button
            v-if="isAuth('sys.role.del')"
            type="text"
            @click="deleteHandle(scope.row.id)"
          >删除</el-button>
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
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdate" @onSubmit="getDataList"></add-or-update>
  </div>
</template>

<script>
import api_role from "@/api/sys/role";
import AddOrUpdate from "./add-or-update";
export default {
  data() {
    return {
      dataForm: {},
      dataList: [],
      paging: 1,
      limit: 10,
      total: 0,
      dataListLoading: false
    };
  },
  components: {
    AddOrUpdate
  },
  created() {
    this.getDataList();
  },
  computed: {
    startPosition() {
      return this.limit * (this.paging - 1);
    }
  },
  methods: {
    getIndex(index) {
      return index + 1 + this.startPosition;
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      api_role
        .getList({
          startPosition: this.startPosition,
          maxResult: this.limit,
          name: this.dataForm.name
        })
        .then(data => {
          this.total = data.totalRecord;
          // 查询不存在的页时，回到第一页
          if(this.total > 0 && this.total <= this.startPosition) {
            this.paging = 1;
            this.getDataList();
            return;
          }
          this.dataList = data.resultData;
          this.dataListLoading = false;
        })
        .catch(() => {
          this.dataListLoading = false;
        });
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
    // 新增 / 修改
    addOrUpdateHandle(id, disabled = false) {
      this.$refs.addOrUpdate.init(id, disabled);
    },
    // 删除
    deleteHandle(id) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          api_role.delete(id).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500
            });
          });
        })
        .catch(() => {});
    }
  }
};
</script>
