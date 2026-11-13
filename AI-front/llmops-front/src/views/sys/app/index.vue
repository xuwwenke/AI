<template>
  <div>
    <page-title />
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="submitQueryForm">
      <el-form-item>
        <el-input v-model="dataForm.name" v-iePlaceholder placeholder="应用名称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.key" v-iePlaceholder placeholder="应用关键字" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select v-model="dataForm.type" v-iePlaceholder placeholder="所属分类" clearable>
          <el-option v-for="appType in appTypeList" :key="appType.key" :value="appType.key" :label="appType.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="submitQueryForm">查询</el-button>
        <el-button v-if="isAuth('sys.app.add')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" stripe style="width: 100%">
      <el-table-column type="index" :index="getIndex" align="center" width="50" label="序号"></el-table-column>
      <el-table-column prop="name" align="center" label="应用名称" width="160" show-overflow-tooltip></el-table-column>
      <el-table-column prop="type" align="center" label="所属分类" width="100" :formatter="colFormatter" show-overflow-tooltip></el-table-column>
      <el-table-column prop="key" align="center" label="应用关键字" width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="icon" width="100" label="应用图标" show-overflow-tooltip>
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
      <el-table-column prop="url" label="应用地址" width="250" show-overflow-tooltip></el-table-column>
      <el-table-column prop="seq" align="center" label="显示序号" width="80"></el-table-column>
      <el-table-column prop="showInHome" align="center" label="呈现在导航" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.showInHome == 1" disable-transitions>是</el-tag>
          <el-tag v-else type="info" disable-transitions>否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deploy" align="center" label="已部署" width="70">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.deploy == 0" type="info" disable-transitions>否</el-tag>
          <el-tag v-else disable-transitions>是</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deploy" header-align="left" align="left" label="微服务名" width="160">
        <template slot-scope="scope">
          <div class="tag-div" v-if="scope.row.microServices && scope.row.microServices.length">
            <div class="tag-every" :key="tag" v-for="tag in scope.row.microServices.split(',')">{{tag}}</div>
          </div>
          <div v-else></div>
        </template>
      </el-table-column>
      <el-table-column prop="desc" align="center" label="应用描述" show-overflow-tooltip></el-table-column>
      <el-table-column prop="insertTime" align="center" width="140" label="创建时间" :formatter="timeFormatter">
      </el-table-column>
      <el-table-column fixed="right" align="center" width="100" label="操作">
        <template slot-scope="scope">
          <el-button v-if="isAuth('sys.app.edit')" type="text" @click="addOrUpdateHandle(scope.row)">修改</el-button>
          <el-button v-if="isAuth('sys.app.del')" type="text" @click="deleteHandle(scope.row.id, scope.row.key)">删除
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
import api_app from "@/api/sys/app";
import AddOrUpdate from "./add-or-update";
import { formatTime } from "@/utils/time";
import { getCfgDic, getValueByKey } from "@/utils/cfgDic";

export default {
  data() {
    return {
      appTypeList: [],
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

    getCfgDic("appType", (data) => {
      this.appTypeList = data;
    });
  },
  computed: {
    startPosition() {
      return this.limit * (this.paging - 1);
    },
  },
  methods: {
    colFormatter(row, column, cellValue, index) {
      const tmp = getValueByKey(cellValue, this.appTypeList) || cellValue;
      return tmp;
    },
    getIndex(index) {
      return index + 1 + this.startPosition;
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      api_app
        .getList({
          startPosition: this.startPosition,
          maxResult: this.limit,
          name: this.dataForm.name,
          key: this.dataForm.key,
          type: this.dataForm.type,
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
      this.$refs.addOrUpdate.init({ ...row }, this.appTypeList);
    },
    // 删除
    deleteHandle(id, key) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          api_app.delete(id, key).then(() => {
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
.tag-div {
  .tag-every{
    display: inline-block;
    margin: 4px 6px 0 0;
    color: #409EFF;
    background-color:#ecf5ff;
    font-size:12px;
    border: 1px solid #d9ecff;
    border-radius: 4px;
    padding: 0 8px;
  }
}
</style>
