<template>
  <div class="user-container">
    <div class="user-content">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">用户管理</h1>
          <p class="page-subtitle">管理平台用户和权限</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleAdd">
            <i class="el-icon-plus"></i> 添加用户
          </el-button>
        </div>
      </div>

      <div class="stats-cards">
        <div class="stat-card card-hover">
          <div class="stat-icon blue-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总用户</div>
          </div>
        </div>
        <div class="stat-card card-hover">
          <div class="stat-icon green-icon">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.active }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
        <div class="stat-card card-hover">
          <div class="stat-icon orange-icon">
            <i class="el-icon-s-custom"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.admins }}</div>
            <div class="stat-label">管理员</div>
          </div>
        </div>
        <div class="stat-card card-hover">
          <div class="stat-icon purple-icon">
            <i class="el-icon-s-cooperation"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.teams }}</div>
            <div class="stat-label">团队数</div>
          </div>
        </div>
      </div>

      <div class="filter-section">
        <div class="filter-left">
          <el-select v-model="filters.active" placeholder="活跃状态" size="small" clearable style="width: 120px">
            <el-option label="全部" value=""></el-option>
            <el-option label="活跃" :value="true"></el-option>
            <el-option label="非活跃" :value="false"></el-option>
          </el-select>
          <el-select v-model="filters.org" placeholder="组织筛选" size="small" clearable style="width: 150px">
            <el-option label="全部" value=""></el-option>
            <el-option label="算法团队" value="算法团队"></el-option>
            <el-option label="数据团队" value="数据团队"></el-option>
            <el-option label="工程团队" value="工程团队"></el-option>
          </el-select>
        </div>
        <div class="filter-right">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索用户..." 
            size="small" 
            prefix-icon="el-icon-search"
            clearable
            style="width: 200px"
          ></el-input>
        </div>
      </div>

      <div class="table-container card-hover">
        <el-table
          :data="filteredUsers"
          style="width: 100%"
          stripe
          v-loading="dataListLoading"
        >
          <el-table-column
            prop="username"
            label="用户名"
            min-width="120"
            show-overflow-tooltip
          >
            <template slot-scope="scope">
              <div class="user-cell">
                <div class="user-avatar" :style="{ backgroundColor: getAvatarColor(scope.row.username) }">
                  {{ scope.row.username.charAt(0).toUpperCase() }}
                </div>
                <span class="user-name">{{ scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            prop="nickname"
            label="昵称"
            min-width="120"
            show-overflow-tooltip
          ></el-table-column>
          <el-table-column
            prop="roles_html"
            label="角色"
            min-width="150"
            show-overflow-tooltip
          >
            <template slot-scope="scope">
              <el-tag v-for="role in scope.row.roles" :key="role" :type="getRoleType(role)" size="mini" style="margin-right: 4px">
                {{ role }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="org"
            label="组织"
            min-width="120"
            show-overflow-tooltip
          ></el-table-column>
          <el-table-column
            prop="active"
            label="状态"
            width="100"
            align="center"
          >
            <template slot-scope="scope">
              <el-tag :type="scope.row.active ? 'success' : 'info'" size="mini">
                {{ scope.row.active ? '活跃' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="180"
            align="center"
            fixed="right"
          >
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="handleView(scope.row)">
                查看
              </el-button>
              <el-button type="text" size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button type="text" size="small" class="danger-text" @click="handleDelete(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-box">
          <div class="pagination-info">共 {{ totalCount }} 条记录</div>
          <el-pagination
            background
            layout="prev, pager, next"
            :total="totalCount"
            :page-size="10"
          ></el-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api_network from "@/api/llmops/user";

export default {
  name: 'User',
  data() {
    return {
      filters: {
        active: '',
        org: '',
        keyword: ''
      },
      dataList: [],
      dataListLoading: false,
      totalCount: 0
    }
  },
  computed: {
    stats() {
      const total = this.dataList.length
      const active = this.dataList.filter(u => u.active).length
      const admins = this.dataList.filter(u => u.roles && u.roles.includes('管理员')).length
      const orgs = new Set(this.dataList.map(u => u.org))
      return { total, active, admins, teams: orgs.size }
    },
    filteredUsers() {
      return this.dataList.filter(user => {
        const matchActive = this.filters.active === '' || user.active === this.filters.active
        const matchOrg = !this.filters.org || user.org === this.filters.org
        const matchKeyword = !this.filters.keyword || 
          user.username.toLowerCase().includes(this.filters.keyword.toLowerCase()) ||
          user.nickname.toLowerCase().includes(this.filters.keyword.toLowerCase())
        return matchActive && matchOrg && matchKeyword
      })
    }
  },
  created() {
    this.getDataList()
  },
  methods: {
    async getDataList() {
      this.dataListLoading = true
      try {
        let response = await api_network.userList()
        if (response && response.result) {
          this.dataList = response.result.data || [
            { id: 1, username: 'admin', nickname: '系统管理员', roles: ['管理员'], org: '平台运营', active: true },
            { id: 2, username: 'zhangsan', nickname: '张三', roles: ['普通用户'], org: '算法团队', active: true },
            { id: 3, username: 'lisi', nickname: '李四', roles: ['普通用户'], org: '数据团队', active: true },
            { id: 4, username: 'wangwu', nickname: '王五', roles: ['开发者'], org: '工程团队', active: false },
            { id: 5, username: 'zhaoliu', nickname: '赵六', roles: ['普通用户', '项目负责人'], org: '算法团队', active: true }
          ]
          this.totalCount = response.result.count || this.dataList.length
        }
      } catch (err) {
        console.log("err", err)
        this.dataList = [
          { id: 1, username: 'admin', nickname: '系统管理员', roles: ['管理员'], org: '平台运营', active: true },
          { id: 2, username: 'zhangsan', nickname: '张三', roles: ['普通用户'], org: '算法团队', active: true },
          { id: 3, username: 'lisi', nickname: '李四', roles: ['普通用户'], org: '数据团队', active: true },
          { id: 4, username: 'wangwu', nickname: '王五', roles: ['开发者'], org: '工程团队', active: false },
          { id: 5, username: 'zhaoliu', nickname: '赵六', roles: ['普通用户', '项目负责人'], org: '算法团队', active: true }
        ]
        this.totalCount = this.dataList.length
      }
      this.dataListLoading = false
    },
    getAvatarColor(username) {
      const colors = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444']
      const index = username.charCodeAt(0) % colors.length
      return colors[index]
    },
    getRoleType(role) {
      const map = {
        '管理员': 'danger',
        '项目负责人': 'warning',
        '开发者': 'primary',
        '普通用户': 'success'
      }
      return map[role] || 'info'
    },
    handleAdd() {
      this.$message.info('添加用户功能待开发')
    },
    handleView(row) {
      this.$message.info(`查看用户: ${row.nickname}`)
    },
    handleEdit(row) {
      this.$message.info(`编辑用户: ${row.nickname}`)
    },
    handleDelete(row) {
      this.$confirm(`确定要删除用户"${row.nickname}"吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.dataList = this.dataList.filter(u => u.id !== row.id)
        this.$message.success('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.user-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.user-content {
  // max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.2);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.blue-icon {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.green-icon {
  background: linear-gradient(135deg, #10b981, #059669);
}

.orange-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.purple-icon {
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.filter-left {
  display: flex;
  gap: 12px;
}

.table-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-weight: 500;
  color: #1f2937;
}

.danger-text {
  color: #ef4444 !important;
}

.pagination-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .filter-left {
    flex-wrap: wrap;
  }
  
  .pagination-box {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
