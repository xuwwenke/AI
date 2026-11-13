<template>
  <div>
    <page-title />
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="submitQueryForm">
      <el-form-item>
        <el-input v-model="dataForm.type" v-iePlaceholder placeholder="属性类型" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select v-model="dataForm.appId" v-iePlaceholder placeholder="所属应用" clearable>
          <el-option v-for="app in appList" :key="app.id" :value="app.id" :label="app.name"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="submitQueryForm">查询</el-button>
        <el-button v-if="isAuth('sys.cfg.dic.add')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" stripe style="width: 100%;">
      <el-table-column type="index" :index="getIndex" align="center" width="50" label="序号"></el-table-column>
      <el-table-column prop="key" align="center" label="键"></el-table-column>
      <el-table-column prop="value" align="center" label="值"></el-table-column>
      <el-table-column prop="type" align="center" label="类型"></el-table-column>
      <el-table-column prop="app.name" align="center" label="所属应用"></el-table-column>
      <el-table-column prop="remark" align="center" label="描述"></el-table-column>
      <el-table-column fixed="right" align="center" width="150" label="操作">
        <template slot-scope="scope">
          <el-button
            v-if="isAuth('sys.cfg.dic.edit')"
            type="text"
            @click="addOrUpdateHandle(scope.row)"
          >修改</el-button>
          <el-button
            v-if="isAuth('sys.cfg.dic.del')"
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
import api_cfg_dic from "@/api/sys/cfg/dic";
import api_app from "@/api/sys/app";
import AddOrUpdate from "./add-or-update";

export default {
  data() {
    return {
      appList: [],
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

    api_app
      .getList({ /* bindCurrentUser: false, */ maxResult: 1000 })
      .then(data => {
        this.appList = data ? data.resultData : [];
      });
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
      api_cfg_dic
        .getList({
          startPosition: this.startPosition,
          maxResult: this.limit,
          type: this.dataForm.type,
          appId: this.dataForm.appId
        })
        .then(data => {
          this.total = data.totalRecord;
          // 查询不存在的页时，回到第一页
          if (this.total > 0 && this.total <= this.startPosition) {
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
    addOrUpdateHandle(row) {
      this.$refs.addOrUpdate.init({ ...row }, this.appList);
    },
    // 删除
    deleteHandle(id) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          api_cfg_dic.delete(id).then(() => {
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
