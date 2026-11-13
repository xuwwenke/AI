<template>
  <div>
    <page-title />
    <el-form :inline="true" :model="dataForm" ref="queryForm" @keyup.enter.native="submitQueryForm">
      <el-form-item prop="dateRange">
        <el-date-picker
          ref="datePicker"
          v-model="dataForm.dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          @change="onDateChange"
        ></el-date-picker>
      </el-form-item>
      <el-form-item prop="ip">
        <el-input v-model="dataForm.ip" v-iePlaceholder placeholder="操作IP" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitQueryForm">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" stripe style="width: 100%;">
      <el-table-column type="index" :index="getIndex" align="center" width="50" label="序号"></el-table-column>
      <el-table-column prop="userName" align="center" width="150" label="用户名"></el-table-column>
      <el-table-column prop="optIp" align="center" width="150" label="操作IP"></el-table-column>
      <el-table-column
        prop="optTime"
        align="center"
        width="200"
        label="操作时间"
        :formatter="timeFormatter"
      ></el-table-column>
      <el-table-column prop="url" align="left" label="请求URL" show-overflow-tooltip></el-table-column>
      <el-table-column prop="execTime" align="center" width="100" label="执行时长(ms)"></el-table-column>
      <el-table-column prop="remark" align="center" label="URL描述"></el-table-column>
      <!-- <el-table-column prop="request" align="center" label="请求参数" show-overflow-tooltip></el-table-column> -->
      <!-- <el-table-column prop="response" align="center" label="响应参数" show-overflow-tooltip></el-table-column> -->
      <el-table-column fixed="right" align="center" width="150" label="操作">
        <template slot-scope="scope">
          <el-button type="text" @click="showDetail(scope.row)">详情</el-button>
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
    <!-- 详情 -->
    <detail ref="detail"></detail>
  </div>
</template>

<script>
import api_log from "@/api/sys/log";
import { formatTime, getStartTime } from "@/utils/time";
import Detail from "./detail";
import { downloadFile } from "@/utils/download";

export default {
  data() {
    return {
      dataForm: {
        dateRange: [new Date(getStartTime()), new Date()],
        userName:"",
        ip:""
      },
      dataList: [],
      paging: 1,
      limit: 10,
      total: 0,
      dataListLoading: false,
      exportFilter: null
    };
  },
  components: {
    Detail
  },
  created() {
    this.dataForm.userName = localStorage.userName;
    this.getDataList();
  },
  computed: {
    startPosition() {
      return this.limit * (this.paging - 1);
    }
  },
  methods: {
    onDateChange(v) {
      if(!v) this.$refs.datePicker.userInput = null; // ie兼容性，避免第一次清空默认值后，仍然显示有值
    },
    getIndex(index) {
      return index + 1 + this.startPosition;
    },
    // 获取数据列表
    getDataList() {
      // console.log(this.dataForm.dateRange);
      this.dataListLoading = true;
      const dateRange = this.dataForm.dateRange;
      const startDate = dateRange && dateRange[0].getTime();
      const endDate = dateRange && dateRange[1].getTime();

      // 查询条件
      const queryFilter = {
        startPosition: this.startPosition,
        maxResult: this.limit,
        startDate,
        endDate,
        userName: this.dataForm.userName,
        ip: this.dataForm.ip
      };

      // 触发过查询，才作为导出的条件
      this.exportFilter = {
        ...queryFilter,
        startPosition: 0,
        maxResult: 1000000
      };

      api_log
        .getOptLogsMy(queryFilter)
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
    onExport() {
      downloadFile(api_log.exportOptLogs(this.exportFilter));
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
    showDetail(row) {
      this.$refs.detail.init({ ...row });
    }
  }
};
</script>
