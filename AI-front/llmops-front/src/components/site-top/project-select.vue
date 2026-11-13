<template>
  <div class="project-select">
    <div class="project-info-box" @click="openDialog">
      <span class="info-label">集群:</span>
      <span class="info-value">{{ currentCluster.name }}</span>
      <span class="info-label ml10">项目:</span>
      <span class="info-value">{{ currentProject.name }}</span>
    </div>

    <el-dialog
      title="切换到项目"
      :visible.sync="dialogVisible"
      width="800px"
      append-to-body
      class="project-switch-dialog"
    >
      <div class="dialog-content">
        <el-input
          v-model="searchKeyword"
          placeholder="按项目名称或别名筛选"
          prefix-icon="el-icon-search"
          class="mb20"
          clearable
        ></el-input>

        <el-table
          v-loading="loading"
          :data="filteredProjectList"
          style="width: 100%"
          @row-click="handleProjectSelect"
          highlight-current-row
        >
          <el-table-column prop="name" label="项目" min-width="120"></el-table-column>
          <el-table-column prop="describe" label="别名" min-width="120"></el-table-column>
          <el-table-column prop="groupName" label="项目分组" min-width="100">
             <template slot-scope="scope">
              {{ (scope.row.projectManager && scope.row.projectManager.groupName) || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="nodeGroupName" label="节点分组" min-width="100">
            <template slot-scope="scope">
              {{ (scope.row.projectManager && scope.row.projectManager.nodeGroupName) || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="projectManager.clusterName" label="集群" min-width="100"></el-table-column>
          <el-table-column prop="createdOn" label="创建时间" min-width="120">
             <template slot-scope="scope">
              {{ scope.row.createdOn | timeAgo }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
  name: 'ProjectSelect',
  data() {
    return {
      dialogVisible: false,
      searchKeyword: '',
      loading: false
    };
  },
  computed: {
    ...mapState('project', ['currentProject', 'currentCluster', 'projectList']),
    filteredProjectList() {
      if (!this.searchKeyword) return this.projectList;
      const keyword = this.searchKeyword.toLowerCase();
      return this.projectList.filter(p => 
        (p.name && p.name.toLowerCase().includes(keyword)) ||
        (p.describe && p.describe.toLowerCase().includes(keyword)) ||
        (p.projectManager && p.projectManager.bz && p.projectManager.bz.toLowerCase().includes(keyword))
      );
    }
  },
  filters: {
    timeAgo(val) {
      if (!val) return '-';
      const date = new Date(val);
      const now = new Date();
      const diffDays = Math.round((now - date) / (1000 * 60 * 60 * 24));

      if (diffDays < 1) return '今天';
      if (diffDays === 1) return '昨天';  // 明确显示"昨天"
      if (diffDays < 30) return `${diffDays}天前`;
      if (diffDays < 365) return `${Math.floor(diffDays / 30)}个月前`;
      return `${Math.floor(diffDays / 365)}年前`;
    }
  },
  methods: {
    ...mapActions('project', ['fetchProjectList', 'switchProject']),
    async openDialog() {
      this.dialogVisible = true;
      this.loading = true;
      await this.fetchProjectList();
      this.loading = false;
    },
    handleProjectSelect(row) {
      this.switchProject(row);
      this.dialogVisible = false;
      this.$message.success(`已切换到项目: ${row.name}`);
      // Reload current page to refresh data based on new project
      location.reload();
    }
  }
};
</script>

<style lang="scss" scoped>
.project-select {
  display: inline-block;
  margin-right: 15px;
}

.project-info-box {
  display: flex;
  align-items: center;
  padding: 0 12px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  height: 30px;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    background-color: #f5f7fa;
  }

  .info-label {
    font-size: 13px;
    color: #606266;
    margin-right: 8px;
  }

  .info-value {
    font-size: 14px;
    color: #409eff;
    font-weight: 400;
  }
}

.dialog-content {
  padding: 10px 0;
}

.mb20 {
  margin-bottom: 20px;
}

.ml10 {
  margin-left: 10px;
}

::v-deep .el-table__row {
  cursor: pointer;
}
</style>
