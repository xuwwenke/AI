<template>
  <div>
    <page-title />
    <el-alert title="只能查看您所属组织机构的用户，以及下级组织机构的用户" type="info" show-icon class="mb10"></el-alert>
    <el-form :inline="true" :model="dataForm" ref="queryForm" @keyup.enter.native="submitQueryForm">
      <el-row :gutter="50">
        <el-col :sm="12" :lg="8">
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="dataForm.userName" v-iePlaceholder placeholder="用户名" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="dataForm.realName" v-iePlaceholder placeholder="真实姓名" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="手机号" prop="tel">
            <el-input v-model="dataForm.tel" v-iePlaceholder placeholder="手机号" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="dataForm.email" v-iePlaceholder placeholder="邮箱" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="用户状态" prop="isValid">
            <el-select v-model="dataForm.isValid" placeholder="请选择" clearable>
              <el-option v-for="s in statusList" :key="s.value" :value="s.value" :label="s.label"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8">
          <el-form-item label="组织机构" prop="orgName">
            <el-input v-model="dataForm.orgName" v-iePlaceholder placeholder="组织机构" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :lg="8" class="fr">
          <el-form-item label=" ">
            <el-button type="primary" @click="submitQueryForm">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>
            <el-button v-if="isAuth('sys.user.add')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" stripe style="width: 100%;">
      <el-table-column type="index" :index="getIndex" align="center" width="50" label="序号"></el-table-column>
      <el-table-column prop="userName" align="center" label="用户名">
        <template slot-scope="scope">
          <el-button type="text" @click="showDetail(scope.row)">{{ scope.row.userName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="realName" align="center" label="真实姓名"></el-table-column>
      <el-table-column prop="tel" align="center" width="120" label="手机号"></el-table-column>
      <el-table-column prop="email" align="center" label="邮箱"></el-table-column>
      <el-table-column prop="isValid" align="center" width="100" label="状态">
        <template slot-scope="scope">
          <status-tag :statusList="statusList" :value="scope.row.isValid" />
        </template>
      </el-table-column>
      <!-- <el-table-column prop="org.name" align="center" label="所属组织"></el-table-column> -->
      <el-table-column prop="loginFailureNum" align="center" width="100" label="登录失败次数"></el-table-column>
      <el-table-column
        prop="insertTime"
        align="center"
        width="160"
        label="创建时间"
        :formatter="timeFormatter"
      ></el-table-column>
      <el-table-column
        prop="resetPasswordTime"
        align="center"
        width="160"
        label="修改密码时间"
        :formatter="timeFormatter"
      ></el-table-column>
      <el-table-column
        prop="expirationDate"
        align="center"
        width="160"
        label="失效时间"
        :formatter="timeFormatter"
      ></el-table-column>
      <el-table-column prop="comcode" align="center" label="用户归属" :formatter="colFormatter"></el-table-column>
      <el-table-column fixed="right" align="center" width="150" label="操作">
        <template slot-scope="scope">
          <el-button
            v-if="isAuth('sys.user.edit')"
            type="text"
            @click="addOrUpdateHandle(scope.row)"
            :disabled="btnDisabled(scope.row)"
          >修改</el-button>
          <el-button
            v-if="isAuth('sys.user.del')"
            type="text"
            @click="deleteHandle(scope.row.id)"
            :disabled="btnDisabled(scope.row)"
          >删除</el-button>
          <el-dropdown
            v-if="isAuth('sys.user.resetPassword') || isAuth('sys.user.unlock') || isAuth('sys.user.access')"
            class="ml10"
            @command="handleCommand($event, scope.row)"
          >
            <span class="el-dropdown-link">
              <span>更多</span>
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-if="isAuth('sys.user.resetPassword')"
                command="resetPassword"
                :disabled="btnDisabled(scope.row)"
              >重置密码</el-dropdown-item>
              <el-dropdown-item
                v-if="isAuth('sys.user.setMenus')"
                command="authorize"
                :disabled="btnDisabled(scope.row)"
              >授权访问资源</el-dropdown-item>
              <el-dropdown-item
                v-if="isAuth('sys.user.unlock')"
                command="unlock"
                :disabled="btnDisabled(scope.row)"
              >解锁</el-dropdown-item>
              <el-dropdown-item
                v-if="isAuth('sys.user.access')"
                command="access"
                :disabled="btnDisabled(scope.row)"
              >访问控制</el-dropdown-item>
              <el-dropdown-item
                v-if="isAuth('sys.user.status') && scope.row.isValid !== 1"
                command="disable"
                :disabled="btnDisabled(scope.row)"
              >禁用</el-dropdown-item>
              <el-dropdown-item
                v-if="isAuth('sys.user.status') && scope.row.isValid !== 2"
                command="deactive"
                :disabled="btnDisabled(scope.row)"
              >注销</el-dropdown-item>
              <el-dropdown-item
                v-if="isAuth('sys.user.status') && scope.row.isValid !== 0"
                command="enable"
                :disabled="btnDisabled(scope.row)"
              >启用</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
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
    <detail ref="detail" :statusList="statusList"></detail>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdate" :statusList="statusList" @onSubmit="getDataList"></add-or-update>
    <!-- 访问控制 -->
    <access-setting ref="accessSetting"></access-setting>
    <!-- 授权访问资源 -->
    <authorize-setting ref="authorizeSetting" @onSubmit="getDataList"></authorize-setting>
  </div>
</template>

<script>
import api_user from "@/api/sys/user";
import Detail from "./detail";
import AddOrUpdate from "./add-or-update";
import AccessSetting from "./access-setting";
import AuthorizeSetting from "./authorize-setting";
import { formatTime } from "@/utils/time";
import { getCfgDic, getValueByKey } from "@/utils/cfgDic";
import StatusTag from "./status-tag";

export default {
  components: {
    Detail,
    AddOrUpdate,
    AccessSetting,
    StatusTag,
    AuthorizeSetting
  },
  data() {
    return {
      comcodeList: [],
      dataForm: {
        userName: "",
        realName: "",
        tel: "",
        email: "",
        isValid: null,
        orgName: ""
      },
      dataList: [],
      paging: 1,
      limit: 10,
      total: 0,
      dataListLoading: false,
      statusList: [
        {
          value: 0,
          label: "正常",
        },
        {
          value: 1,
          label: "禁用",
          type: "danger",
        },
        {
          value: 2,
          label: "注销",
          type: "warning",
        },
      ]
    };
  },
  created() {
    this.getDataList();

    getCfgDic("comcode", (data) => {
      this.comcodeList = data;
    });
  },
  computed: {
    startPosition() {
      return this.limit * (this.paging - 1);
    },
  },
  methods: {
    btnDisabled(row) {
      // 不能操作除自己外的其他同级用户（admin除外）
      if (!window.globalCache.userInfo) return false;
      const userName = window.globalCache.userInfo.userName;
      const currentOrgId =
        window.globalCache.userInfo.org && window.globalCache.userInfo.org.id;
      return (
        userName !== "admin" &&
        userName !== row.userName &&
        currentOrgId === row.org.id
      );
    },
    colFormatter(row, column, cellValue, index) {
      const tmp = getValueByKey(cellValue, this.comcodeList) || cellValue;
      row.comcodeLabel = tmp;
      return tmp;
    },
    getIndex(index) {
      return index + 1 + this.startPosition;
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      api_user
        .getList({
          // bindCurrentUser: false,
          fetchOrg: true,
          fetchRole: true,
          startPosition: this.startPosition,
          maxResult: this.limit,
          // userName: this.dataForm.userName,
          // realName: this.dataForm.realName,
          ...this.dataForm,
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
    resetQueryForm() {
      this.$refs.queryForm.resetFields();
      this.submitQueryForm();
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
    },
    // 新增 / 修改
    addOrUpdateHandle(row) {
      this.$refs.addOrUpdate.init({ ...row }, this.comcodeList);
    },
    // 删除
    deleteHandle(id) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          api_user.delete(id).then(() => {
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
    handleCommand(command, row) {
      switch (command) {
        case "resetPassword":
          // 重置密码
          this.$confirm("确定要重置密码吗?", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          })
            .then(() => {
              api_user.resetPassword(row.id).then((data) => {
                /* this.$message({
                  message: "操作成功",
                  type: "success",
                  duration: 1500
                }); */
                this.$alert("密码被重置为：" + data, "操作成功", {
                  type: "success",
                });
              });
            })
            .catch(() => {});
          break;
        case "authorize":
          // 授权资源
          this.$refs.authorizeSetting.init(row.id);
          break;
        case "unlock":
          // 解锁
          api_user.unlock(row.id).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
          break;
        case "access":
          this.$refs.accessSetting.init(row);
          break;
        case "disable":
          // 禁用
          api_user.setStatus(row.id, 1).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
          break;
        case "deactive":
          // 注销
          api_user.setStatus(row.id, 2).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
          break;
        case "enable":
          // 启用
          api_user.setStatus(row.id, 0).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
          break;
      }
    }
  },
};
</script>
