<template>
  <div>
    <page-title />
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="submitQueryForm">
      <el-form-item>
        <el-input v-model="dataForm.name" v-iePlaceholder placeholder="用户组名称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-select
          v-model="dataForm.type"
          placeholder="用户组类型"
          filterable
          clearable
          @clear="getDataList()"
        >
          <el-option
            v-for="item of groupTypeList"
            :key="item.key"
            :value="item.key"
            :label="item.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="submitQueryForm">查询</el-button>
        <el-button v-if="isAuth('sys.group.add')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataListWithDic" stripe style="width: 100%;">
      <el-table-column type="index" :index="getIndex" align="center" width="50" label="序号"></el-table-column>
      <el-table-column prop="name" align="center" label="用户组名称"></el-table-column>
      <el-table-column prop="typeDic.value" align="center" label="用户组类型"></el-table-column>
      <el-table-column prop="desc" align="center" label="描述"></el-table-column>
      <el-table-column
        prop="insertTime"
        align="center"
        width="165"
        label="创建时间"
        :formatter="timeFormatter"
      ></el-table-column>
      <el-table-column fixed="right" align="center" width="240" label="操作">
        <template slot-scope="scope">
          <el-button
            v-if="isAuth('sys.group.edit')"
            type="text"
            @click="addOrUpdateHandle(scope.row)"
          >修改</el-button>
          <el-button
            v-if="isAuth('sys.group.edit')"
            type="text"
            @click="editUserHandle(scope.row)"
          >修改用户</el-button>
          <el-button
            v-if="isAuth('sys.group.edit')"
            type="text"
            @click="editRoleHandle(scope.row)"
          >授权角色</el-button>
          <el-button
            v-if="isAuth('sys.group.del')"
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
    <!-- 修改用户组内用户 -->
    <edit-group-user ref="userModal"></edit-group-user>
    <!-- 弹窗, 授权角色 -->
    <edit-group-role ref="roleModal" @onSubmit="getDataList"></edit-group-role>
  </div>
</template>

<script>
import api_group from "@/api/sys/group";
import AddOrUpdate from "./add-or-update";
import EditGroupUser from "./edit-group-user";
import EditGroupRole from "./edit-group-role";
import { formatTime } from "@/utils/time";
import { find } from "lodash";
import { getCfgDicPromise } from "@/utils/cfgDic";

export default {
  data() {
    return {
      groupTypeList: [],
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
    EditGroupUser,
    EditGroupRole
  },
  created() {
    this.getDataList();
    getCfgDicPromise("groupType").then(data => {
      this.groupTypeList = data || [];
    });
  },
  computed: {
    startPosition() {
      return this.limit * (this.paging - 1);
    },
    dataListWithDic() {
      const list = [];
      this.dataList.forEach(item => {
        const row = { ...item };
        row.typeDic = find(this.groupTypeList, { key: row.type }) || { value: "未知" };
        list.push(row);
      });
      return list;
    }
  },
  methods: {
    getIndex(index) {
      return index + 1 + this.startPosition;
    },
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      api_group
        .getList({
          startPosition: this.startPosition,
          maxResult: this.limit,
          name: this.dataForm.name,
          orgStyle: this.dataForm.orgStyle,
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
      this.$refs.addOrUpdate.init({ ...row });
    },
    // 修改用户
    editUserHandle(row) {
      if (row) {
        this.$refs.userModal.init(row.id);
      }
    },
    // 授权角色
    editRoleHandle(row) {
      if (row) {
        this.$refs.roleModal.init(row.id);
      }
    },
    // 删除
    deleteHandle(id) {
      this.$confirm(`确定进行[删除]操作?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          api_group.delete(id).then(() => {
            this.getDataList();
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
            });
          });
        })
        .catch(() => {});
    }
  }
};
</script>
