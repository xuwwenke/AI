<template>
  <div>
    <page-title />
    <el-form
      :inline="true"
      :model="dataForm"
      ref="queryForm"
      @keyup.enter.native="submitQueryForm"
    >
      <el-row :gutter="50">
        <el-col :sm="12" :lg="8">
          <el-form-item label="登录时间" prop="dateRange">
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
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="用户名" prop="userName">
            <el-input
              v-model="dataForm.userName"
              v-iePlaceholder
              placeholder="用户名"
              clearable
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="登录 IP" prop="loginIp">
            <el-input
              v-model="dataForm.loginIp"
              v-iePlaceholder
              placeholder="登录 IP"
              clearable
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="登录应用" prop="loginAppName">
            <el-input
              v-model="dataForm.loginAppName"
              v-iePlaceholder
              placeholder="登录应用"
              clearable
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="登出应用" prop="logoutAppName">
            <el-input
              v-model="dataForm.logoutAppName"
              v-iePlaceholder
              placeholder="登出应用"
              clearable
            ></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="日志类型" prop="logType">
            <el-select v-model="dataForm.logType" placeholder="请选择">
              <el-option value="ALL" label="登录 + 登出"></el-option>
              <el-option value="LOGIN" label="登录"></el-option>
              <el-option value="LOGOUT" label="登出"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8" class="fr">
          <el-form-item label=" ">
            <el-button type="primary" @click="submitQueryForm">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>
            <el-button v-if="isAuth('sys.log.login.export')" @click="onExport"
              >导出</el-button
            >
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-table
      v-loading="dataListLoading"
      :data="dataList"
      stripe
      style="width: 100%"
    >
      <el-table-column
        type="index"
        :index="getIndex"
        align="center"
        width="50"
        label="序号"
      ></el-table-column>
      <el-table-column
        prop="sessionId"
        align="center"
        width="140"
        label="SESSION ID"
      ></el-table-column>
      <el-table-column
        prop="userId"
        align="center"
        label="用户 ID"
      ></el-table-column>
      <el-table-column
        prop="userName"
        align="center"
        label="用户名"
      ></el-table-column>
      <el-table-column
        prop="realName"
        align="center"
        label="真实姓名"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGOUT'"
        prop="loginIp"
        align="center"
        width="120"
        label="登录 IP"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGOUT'"
        prop="loginAppName"
        align="center"
        width="100"
        label="登录应用"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGOUT'"
        prop="loginTime"
        align="center"
        width="160"
        label="登录时间"
        :formatter="timeFormatter"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGIN'"
        prop="logoutAppName"
        align="center"
        width="100"
        label="登出时的应用"
      ></el-table-column>
      <el-table-column
        v-if="showVpn"
        prop="hostname"
        align="center"
        width="100"
        label="HOSTNAME"
      ></el-table-column>
      <el-table-column
        v-if="showVpn"
        prop="netIp"
        align="center"
        width="120"
        label="公网IP"
      ></el-table-column>
      <el-table-column
        v-if="showVpn"
        prop="macAddr"
        align="center"
        width="160"
        label="MAC地址"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGIN'"
        prop="logoutTime"
        align="center"
        width="160"
        label="登出时间"
        :formatter="timeFormatter"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGOUT'"
        prop="loginStatusExplain"
        align="center"
        width="100"
        label="登录状态"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGOUT'"
        prop="loginTypeExplain"
        align="center"
        width="100"
        label="登录类型"
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGOUT'"
        prop="loginDesc"
        align="left"
        width="180"
        label="登录描述"
        show-overflow-tooltip
      ></el-table-column>
      <el-table-column
        v-if="queryLogType !== 'LOGOUT'"
        prop="ua"
        align="left"
        width="200"
        label="浏览器user-agent"
        show-overflow-tooltip
      ></el-table-column>
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
  </div>
</template>

<script>
import api_log from "@/api/sys/log";
import { formatTime, getStartTime } from "@/utils/time";
import { downloadFile } from "@/utils/download";

export default {
  data() {
    return {
      showVpn:window.globalConfig.showVpn,
      queryLogType: "ALL", // 点查询时，才将 logType 传入
      dataForm: {
        dateRange: [new Date(getStartTime()), new Date()],
        userName: "",
        loginIp: "",
        loginAppName: "",
        logoutAppName: "",
        logType: "ALL",
      },
      dataList: [],
      paging: 1,
      limit: 10,
      total: 0,
      dataListLoading: false,
      exportFilter: null,
    };
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
    onDateChange(v) {
      if (!v) this.$refs.datePicker.userInput = null; // ie兼容性，避免第一次清空默认值后，仍然显示有值
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
        loginIp: this.dataForm.loginIp,
        loginAppName: this.dataForm.loginAppName,
        logoutAppName: this.dataForm.logoutAppName,
        isOnlyLogout: this.dataForm.logType === "LOGOUT",
      };

      // 触发过查询，才作为导出的条件
      this.exportFilter = {
        ...queryFilter,
        startPosition: 0,
        maxResult: 1000000,
      };

      api_log
        .getLoginLogs(queryFilter)
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
      this.queryLogType = this.dataForm.logType;
      this.paging = 1;
      this.getDataList();
    },
    resetQueryForm() {
      this.$refs.queryForm.resetFields();
      this.submitQueryForm();
    },
    onExport() {
      downloadFile(api_log.exportLoginLogs(this.exportFilter));
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
  },
};
</script>
