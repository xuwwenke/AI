<template>
  <div>
    <page-title />
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="submitQueryForm">
      <el-form-item>
        <el-input v-model="dataForm.name" v-iePlaceholder placeholder="产线名称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.key" v-iePlaceholder placeholder="产线关键字" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="submitQueryForm">查询</el-button>
        <el-button v-if="isAuth('sys.prod.add')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" stripe style="width: 100%">
      <el-table-column type="index" :index="getIndex" align="center" width="50" label="序号"></el-table-column>
      <el-table-column prop="name" align="center" label="产线名称"></el-table-column>
      <el-table-column prop="key" align="center" label="产线关键字"></el-table-column>
      <el-table-column prop="icon" width="100" label="产线图标">
        <template slot-scope="scope">
          <el-image v-if="/\/|\./.test(scope.row.icon)" :src="scope.row.icon" :preview-src-list="[scope.row.icon]"
            class="cell-image">
            <div slot="placeholder" class="image-slot">
              <i class="el-icon-loading"></i>
            </div>
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
              <!-- 加载失败 -->
            </div>
          </el-image>
          <template v-else>
            <i :class="'fa fa-' + scope.row.icon"></i>
            {{ scope.row.icon }}
          </template>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="产线地址"></el-table-column>
      <el-table-column prop="seq" align="center" label="显示序号" width="80"></el-table-column>
      <el-table-column prop="showInHome" align="center" label="呈现在导览页" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.showInHome == 1" disable-transitions>是</el-tag>
          <el-tag v-else type="info" disable-transitions>否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deploy" align="center" label="已部署" width="60">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.deploy == 0" type="info" disable-transitions>否</el-tag>
          <el-tag v-else disable-transitions>是</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="desc" align="center" label="产线描述" show-overflow-tooltip></el-table-column>
      <el-table-column prop="insertTime" align="center" width="180" label="创建时间" :formatter="timeFormatter">
      </el-table-column>
      <el-table-column fixed="right" align="center" width="150" label="操作">
        <template slot-scope="scope">
          <el-button v-if="isAuth('sys.prod.edit')" type="text" @click="addOrUpdateHandle(scope.row)">修改</el-button>
          <el-button v-if="isAuth('sys.prod.del')" type="text" @click="deleteHandle(scope.row.id, scope.row.key)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :disabled="dataListLoading" class="mt10" @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle" :current-page="paging" :page-sizes="[10, 20, 50, 100]" :page-size="limit"
      :total="total" layout="total, sizes, prev, pager, next, jumper"></el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdate" @onSubmit="getDataList"></add-or-update>
  </div>
</template>

<script>
import api_prod from "@/api/sys/prod";
import AddOrUpdate from "./add-or-update";
import { formatTime } from "@/utils/time";

export default {
  data() {
    return {
      dataForm: {},
      dataList: [],
      paging: 1,
      limit: 10,
      total: 0,
      dataListLoading: false,
    };
  },
  components: {
    AddOrUpdate,
  },
  created() {
    this.getDataList();
  },
  computed: {
    startPosition() {
      return this.limit * (this.paging - 1);
    },
  },
  methods: {
    getIndex(index) {
      return index + 1 + this.startPosition;
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      api_prod
        .getList({
          startPosition: this.startPosition,
          maxResult: this.limit,
          name: this.dataForm.name,
          key: this.dataForm.key,
        })
        .then((data) => {
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
    timeFormatter(row, column, cellValue, index) {
      return formatTime(cellValue);
    },
    // 新增 / 修改
    addOrUpdateHandle(row) {
      this.$refs.addOrUpdate.init({ ...row });
    },
    // 删除
    deleteHandle(id, key) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          api_prod.delete(id, key).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
        })
        .catch(() => { });
    },
  },
};
</script>

<style lang="scss" scoped>
.cell-image ::v-deep {
  width: 40px;
  height: 40px;
  vertical-align: middle;

  .image-slot {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>
